package com.oakpay.trading.p2p;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class P2PExchangeRateService {
    private static final String FRANKFURTER_USD_ZWG_URL = "https://api.frankfurter.dev/v2/rate/USD/ZWG";
    private static final String COINGECKO_USDT_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=usd";
    private static final long EXTERNAL_CACHE_SECONDS = 60;

    private static final Pattern FRANKFURTER_RATE_PATTERN = Pattern.compile(
            "\\\"rate\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern USDT_USD_PATTERN = Pattern.compile(
            "\\\"usd\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);

    private final P2PExchangeRateRepository repository;
    private final AdvertisementRepository advertisementRepository;
    private final RestClient restClient = RestClient.create();
    private volatile ExternalRateCache externalRateCache;

    public P2PExchangeRateService(P2PExchangeRateRepository repository, AdvertisementRepository advertisementRepository) {
        this.repository = repository;
        this.advertisementRepository = advertisementRepository;
    }

    public RateSnapshot getRate(String baseCurrency, String quoteCurrency) {
        String base = normalize(baseCurrency);
        String quote = normalize(quoteCurrency);
        Optional<RateSnapshot> liveP2p = getLiveP2pRate(base, quote);
        if (liveP2p.isPresent()) return liveP2p.get();
        return getExternalRate(base, quote);
    }

    public BigDecimal getRateValue(String baseCurrency, String quoteCurrency) {
        return getRate(baseCurrency, quoteCurrency).rate();
    }

    private Optional<RateSnapshot> getLiveP2pRate(String base, String quote) {
        if (!base.equals("USDT") || !quote.equals("ZWG")) return Optional.empty();
        var buyOffers = advertisementRepository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceDescCreatedAtAsc(
                OrderSide.BUY, base, quote, AdStatus.ACTIVE, PageRequest.of(0, 1));
        var sellOffers = advertisementRepository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceAscCreatedAtAsc(
                OrderSide.SELL, base, quote, AdStatus.ACTIVE, PageRequest.of(0, 1));
        var bestBuy = buyOffers.stream().filter(a -> a.getAvailableQuantity().signum() > 0).findFirst();
        var bestSell = sellOffers.stream().filter(a -> a.getAvailableQuantity().signum() > 0).findFirst();
        if (bestBuy.isEmpty() && bestSell.isEmpty()) return Optional.empty();
        BigDecimal rate;
        if (bestBuy.isPresent() && bestSell.isPresent()) {
            rate = bestBuy.get().getPrice().add(bestSell.get().getPrice()).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
        } else {
            rate = bestBuy.isPresent() ? bestBuy.get().getPrice() : bestSell.get().getPrice();
        }
        return Optional.of(new RateSnapshot(base, quote, rate.setScale(4, RoundingMode.HALF_UP), "OAKPAY_P2P", LocalDateTime.now()));
    }

    private RateSnapshot getExternalRate(String base, String quote) {
        if (base.equals("USDT") && quote.equals("ZWG")) {
            try {
                ExternalRate external = getExternalRate();
                BigDecimal rate = external.usdtUsd().multiply(external.usdZwg()).setScale(4, RoundingMode.HALF_UP);
                return new RateSnapshot(base, quote, rate, "FRANKFURTER_USD_ZWG + COINGECKO_USDT_USD", external.updatedAt());
            } catch (RestClientException | IllegalStateException ex) {
                return getStoredRate(base, quote);
            }
        }
        if (base.equals("USDT") && quote.equals("USD")) {
            try {
                BigDecimal usdtUsd = getExternalRate().usdtUsd();
                return new RateSnapshot(base, quote, usdtUsd.setScale(6, RoundingMode.HALF_UP), "COINGECKO_USDT_USD", LocalDateTime.now());
            } catch (RestClientException | IllegalStateException ex) {
                return getStoredRate(base, quote);
            }
        }
        return getStoredRate(base, quote);
    }

    private RateSnapshot getStoredRate(String base, String quote) {
        return repository.findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc(base, quote)
                .map(rate -> new RateSnapshot(rate.getBaseCurrency(), rate.getQuoteCurrency(), rate.getRate(), rate.getSource(), rate.getEffectiveAt()))
                .orElseThrow(() -> new IllegalArgumentException("No live market rate configured for " + base + "/" + quote));
    }

    private ExternalRate getExternalRate() {
        ExternalRateCache cached = externalRateCache;
        if (cached != null && ChronoUnit.SECONDS.between(cached.updatedAt(), LocalDateTime.now()) < EXTERNAL_CACHE_SECONDS) {
            return cached.rate();
        }

        String frankfurterJson = restClient.get()
                .uri(FRANKFURTER_USD_ZWG_URL)
                .header("Accept", "application/json")
                .retrieve()
                .body(String.class);
        if (frankfurterJson == null || frankfurterJson.isBlank()) {
            throw new IllegalStateException("Frankfurter returned no USD/ZWG rate data");
        }

        Matcher rateMatcher = FRANKFURTER_RATE_PATTERN.matcher(frankfurterJson);
        if (!rateMatcher.find()) {
            throw new IllegalStateException("USD/ZWG rate was not found in Frankfurter response");
        }
        BigDecimal usdZwg = new BigDecimal(rateMatcher.group(1));
        if (usdZwg.signum() <= 0) {
            throw new IllegalStateException("Invalid USD/ZWG value returned by Frankfurter");
        }

        String usdtJson = restClient.get()
                .uri(COINGECKO_USDT_URL)
                .header("Accept", "application/json")
                .retrieve()
                .body(String.class);
        if (usdtJson == null || usdtJson.isBlank()) {
            throw new IllegalStateException("CoinGecko returned no USDT price");
        }
        Matcher usdtMatcher = USDT_USD_PATTERN.matcher(usdtJson);
        if (!usdtMatcher.find()) {
            throw new IllegalStateException("USDT/USD price was not found in CoinGecko response");
        }
        BigDecimal usdtUsd = new BigDecimal(usdtMatcher.group(1));
        if (usdtUsd.signum() <= 0) {
            throw new IllegalStateException("Invalid USDT/USD value returned by CoinGecko");
        }

        ExternalRate rate = new ExternalRate(usdtUsd, usdZwg, LocalDateTime.now());
        externalRateCache = new ExternalRateCache(rate, rate.updatedAt());
        return rate;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Currency is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public record RateSnapshot(String baseCurrency, String quoteCurrency, BigDecimal rate, String source, LocalDateTime effectiveAt) {}
    private record ExternalRate(BigDecimal usdtUsd, BigDecimal usdZwg, LocalDateTime updatedAt) {}
    private record ExternalRateCache(ExternalRate rate, LocalDateTime updatedAt) {}
}
