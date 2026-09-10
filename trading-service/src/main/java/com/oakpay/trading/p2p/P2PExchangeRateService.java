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
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class P2PExchangeRateService {
    private static final String FRANKFURTER_USD_ZWG_URL = "https://api.frankfurter.dev/v2/rate/USD/ZWG";
    private static final String COINGECKO_SIMPLE_PRICE_URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd";
    private static final long EXTERNAL_CACHE_SECONDS = 60;

    private static final Map<String, String> COINGECKO_IDS = Map.ofEntries(
            Map.entry("USDT", "tether"),
            Map.entry("BTC", "bitcoin"),
            Map.entry("ETH", "ethereum"),
            Map.entry("BNB", "binancecoin"),
            Map.entry("SOL", "solana"),
            Map.entry("USDC", "usd-coin"),
            Map.entry("XRP", "ripple"),
            Map.entry("ADA", "cardano"),
            Map.entry("DOGE", "dogecoin"),
            Map.entry("TRX", "tron"),
            Map.entry("LTC", "litecoin"),
            Map.entry("AVAX", "avalanche-2")
    );

    private static final Pattern RATE_PATTERN = Pattern.compile(
            "\\\"rate\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);

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

        if (base.equals(quote)) {
            return new RateSnapshot(base, quote, BigDecimal.ONE, "MARKET_REFERENCE", LocalDateTime.now());
        }

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
            rate = bestBuy.get().getPrice().add(bestSell.get().getPrice())
                    .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
        } else {
            rate = bestBuy.isPresent() ? bestBuy.get().getPrice() : bestSell.get().getPrice();
        }

        return Optional.of(new RateSnapshot(
                base,
                quote,
                rate.setScale(4, RoundingMode.HALF_UP),
                "OAKPAY_P2P",
                LocalDateTime.now()
        ));
    }

    private RateSnapshot getExternalRate(String base, String quote) {
        try {
            BigDecimal baseUsd = getUsdValue(base);
            BigDecimal quoteUsd = getUsdValue(quote);
            if (baseUsd.signum() <= 0 || quoteUsd.signum() <= 0) {
                throw new IllegalStateException("Invalid external market rate for " + base + "/" + quote);
            }

            BigDecimal rate = baseUsd.divide(quoteUsd, 12, RoundingMode.HALF_UP);
            int scale = quote.equals("USD") ? 6 : 4;
            String source = base.equals("USD") && quote.equals("ZWG")
                    ? "FRANKFURTER_USD_ZWG"
                    : base.equals("USDT") && quote.equals("USD")
                    ? "COINGECKO_USDT_USD"
                    : "COINGECKO_USD_REFERENCE + FRANKFURTER_USD_ZWG";

            return new RateSnapshot(base, quote, rate.setScale(scale, RoundingMode.HALF_UP), source, LocalDateTime.now());
        } catch (RestClientException | IllegalStateException | NumberFormatException ex) {
            return getStoredRate(base, quote);
        }
    }

    private BigDecimal getUsdValue(String currency) {
        if (currency.equals("USD")) return BigDecimal.ONE;
        if (currency.equals("ZWG")) {
            return BigDecimal.ONE.divide(getUsdZwg(), 12, RoundingMode.HALF_UP);
        }

        String coinId = COINGECKO_IDS.get(currency);
        if (coinId == null) {
            throw new IllegalArgumentException("No external market mapping configured for " + currency);
        }
        return getCoinUsdPrice(coinId);
    }

    private BigDecimal getUsdZwg() {
        ExternalRateCache cached = externalRateCache;
        if (cached != null && ChronoUnit.SECONDS.between(cached.updatedAt(), LocalDateTime.now()) < EXTERNAL_CACHE_SECONDS) {
            return cached.usdZwg();
        }

        String json = restClient.get()
                .uri(FRANKFURTER_USD_ZWG_URL)
                .header("Accept", "application/json")
                .retrieve()
                .body(String.class);
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("Frankfurter returned no USD/ZWG rate data");
        }

        Matcher matcher = RATE_PATTERN.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("USD/ZWG rate was not found in Frankfurter response");
        }

        BigDecimal usdZwg = new BigDecimal(matcher.group(1));
        if (usdZwg.signum() <= 0) {
            throw new IllegalStateException("Invalid USD/ZWG value returned by Frankfurter");
        }

        ExternalRateCache previous = externalRateCache;
        BigDecimal usdtUsd = previous != null ? previous.usdtUsd() : null;
        externalRateCache = new ExternalRateCache(usdtUsd, usdZwg, LocalDateTime.now());
        return usdZwg;
    }

    private BigDecimal getCoinUsdPrice(String coinId) {
        ExternalRateCache cached = externalRateCache;
        if (cached != null && cached.coinPricesUsd().containsKey(coinId)
                && ChronoUnit.SECONDS.between(cached.updatedAt(), LocalDateTime.now()) < EXTERNAL_CACHE_SECONDS) {
            return cached.coinPricesUsd().get(coinId);
        }

        String json = restClient.get()
                .uri(COINGECKO_SIMPLE_PRICE_URL.formatted(coinId))
                .header("Accept", "application/json")
                .retrieve()
                .body(String.class);
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("CoinGecko returned no price data for " + coinId);
        }

        Pattern pricePattern = Pattern.compile(
                "\\\"" + Pattern.quote(coinId) + "\\\"\\s*:\\s*\\{\\s*\\\"usd\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*\\}",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pricePattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("USD price was not found for " + coinId);
        }

        BigDecimal price = new BigDecimal(matcher.group(1));
        if (price.signum() <= 0) {
            throw new IllegalStateException("Invalid USD price returned for " + coinId);
        }

        ExternalRateCache previous = externalRateCache;
        Map<String, BigDecimal> prices = previous == null ? new java.util.HashMap<>() : new java.util.HashMap<>(previous.coinPricesUsd());
        prices.put(coinId, price);
        BigDecimal usdZwg = previous != null ? previous.usdZwg() : null;
        externalRateCache = new ExternalRateCache(price, usdZwg, LocalDateTime.now(), prices);
        return price;
    }

    private RateSnapshot getStoredRate(String base, String quote) {
        return repository.findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc(base, quote)
                .map(rate -> new RateSnapshot(
                        rate.getBaseCurrency(),
                        rate.getQuoteCurrency(),
                        rate.getRate(),
                        rate.getSource(),
                        rate.getEffectiveAt()))
                .orElseThrow(() -> new IllegalArgumentException("No live market rate configured for " + base + "/" + quote));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Currency is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public record RateSnapshot(String baseCurrency, String quoteCurrency, BigDecimal rate, String source, LocalDateTime effectiveAt) {}

    private record ExternalRateCache(
            BigDecimal usdtUsd,
            BigDecimal usdZwg,
            LocalDateTime updatedAt,
            Map<String, BigDecimal> coinPricesUsd) {
        private ExternalRateCache(BigDecimal usdtUsd, BigDecimal usdZwg, LocalDateTime updatedAt) {
            this(usdtUsd, usdZwg, updatedAt, Map.of());
        }
    }
}
