package com.oakpay.trading.p2p;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
    private static final String RBZ_RATES_URL = "https://www.rbz.co.zw/index.php/13-daily-exchange-rates/16-rates";
    private static final String COINGECKO_USDT_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=usd";
    private static final long EXTERNAL_CACHE_SECONDS = 60;

    private static final Pattern USD_ZWG_ROW_PATTERN = Pattern.compile(
            "USD\\s*/\\s*ZWG", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "(?<![A-Za-z])([0-9]+(?:\\.[0-9]+)?)(?![A-Za-z])");
    private static final Pattern USDT_USD_PATTERN = Pattern.compile(
            "\\\"usd\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)",
            Pattern.CASE_INSENSITIVE);

    private final P2PExchangeRateRepository repository;
    private final AdvertisementRepository advertisementRepository;
    private final RestClient restClient = RestClient.create();

    private volatile ExternalRateCache externalRateCache;

    public P2PExchangeRateService(P2PExchangeRateRepository repository,
                                  AdvertisementRepository advertisementRepository) {
        this.repository = repository;
        this.advertisementRepository = advertisementRepository;
    }

    public RateSnapshot getRate(String baseCurrency, String quoteCurrency) {
        String base = normalize(baseCurrency);
        String quote = normalize(quoteCurrency);

        // The OakPay P2P market is the primary source when there are active offers.
        Optional<RateSnapshot> liveP2p = getLiveP2pRate(base, quote);
        if (liveP2p.isPresent()) return liveP2p.get();

        // No OakPay offers yet: use current external market/reference data.
        // We deliberately do not fall back to the old fixed database rate for USDT/ZWG,
        // because that would make the UI claim LIVE while showing stale data.
        return getExternalRate(base, quote);
    }

    public BigDecimal getRateValue(String baseCurrency, String quoteCurrency) {
        return getRate(baseCurrency, quoteCurrency).rate();
    }

    private Optional<RateSnapshot> getLiveP2pRate(String base, String quote) {
        if (!base.equals("USDT") || !quote.equals("ZWG")) return Optional.empty();

        var buyOffers = advertisementRepository
                .findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceDescCreatedAtAsc(
                        OrderSide.BUY, base, quote, AdStatus.ACTIVE, PageRequest.of(0, 1));
        var sellOffers = advertisementRepository
                .findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceAscCreatedAtAsc(
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
                LocalDateTime.now()));
    }

    private RateSnapshot getExternalRate(String base, String quote) {
        if (base.equals("USDT") && quote.equals("ZWG")) {
            ExternalRate external = getExternalRate();
            BigDecimal rate = external.usdtUsd().multiply(external.usdZwg()).setScale(4, RoundingMode.HALF_UP);
            return new RateSnapshot(base, quote, rate, "RBZ_INTERBANK_AVG + COINGECKO_USDT_USD", external.updatedAt());
        }

        if (base.equals("USDT") && quote.equals("USD")) {
            BigDecimal usdtUsd = getExternalRate().usdtUsd();
            return new RateSnapshot(base, quote, usdtUsd.setScale(6, RoundingMode.HALF_UP), "COINGECKO_USDT_USD", LocalDateTime.now());
        }

        return repository.findFirstByBaseCurrencyAndQuoteCurrencyAndActiveTrueOrderByEffectiveAtDesc(base, quote)
                .map(rate -> new RateSnapshot(
                        rate.getBaseCurrency(),
                        rate.getQuoteCurrency(),
                        rate.getRate(),
                        rate.getSource(),
                        rate.getEffectiveAt()))
                .orElseThrow(() -> new IllegalArgumentException("No live market rate configured for " + base + "/" + quote));
    }

    private ExternalRate getExternalRate() {
        ExternalRateCache cached = externalRateCache;
        if (cached != null && ChronoUnit.SECONDS.between(cached.updatedAt(), LocalDateTime.now()) < EXTERNAL_CACHE_SECONDS) {
            return cached.rate();
        }

        String rbzHtml = restClient.get().uri(RBZ_RATES_URL).retrieve().body(String.class);
        if (rbzHtml == null || rbzHtml.isBlank()) {
            throw new IllegalStateException("RBZ returned no rate data");
        }

        BigDecimal usdZwg = extractRbzUsdZwgAverage(rbzHtml);

        String usdtJson = restClient.get().uri(COINGECKO_USDT_URL).retrieve().body(String.class);
        if (usdtJson == null || usdtJson.isBlank()) {
            throw new IllegalStateException("CoinGecko returned no USDT price");
        }

        Matcher usdtMatcher = USDT_USD_PATTERN.matcher(usdtJson);
        if (!usdtMatcher.find()) {
            throw new IllegalStateException("USDT/USD price was not found in CoinGecko response");
        }
        BigDecimal usdtUsd = new BigDecimal(usdtMatcher.group(1));

        ExternalRate rate = new ExternalRate(usdtUsd, usdZwg, LocalDateTime.now());
        externalRateCache = new ExternalRateCache(rate, rate.updatedAt());
        return rate;
    }

    private BigDecimal extractRbzUsdZwgAverage(String html) {
        // Do not depend on the exact HTML/table separators used by RBZ.
        // Locate the USD/ZWG row first, strip markup/entities, then read the first
        // three numeric cells in that row: BID, ASK, AVG. This survives changes such
        // as pipes, non-breaking spaces, <td> tags and line breaks.
        String normalized = html
                .replace('&nbsp;', ' ')
                .replace('\u00A0', ' ')
                .replaceAll("(?is)<br\\s*/?>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ");

        Matcher rowMatcher = USD_ZWG_ROW_PATTERN.matcher(normalized);
        if (!rowMatcher.find()) {
            throw new IllegalStateException("USD/ZWG rate row was not found on the RBZ page");
        }

        int start = rowMatcher.start();
        int end = Math.min(normalized.length(), start + 500);
        String row = normalized.substring(start, end);

        Matcher numberMatcher = NUMBER_PATTERN.matcher(row);
        BigDecimal bid = null;
        BigDecimal ask = null;
        BigDecimal avg = null;
        int count = 0;
        while (numberMatcher.find() && count < 3) {
            BigDecimal value = new BigDecimal(numberMatcher.group(1));
            if (count == 0) bid = value;
            else if (count == 1) ask = value;
            else avg = value;
            count++;
        }

        if (count < 3 || avg == null) {
            throw new IllegalStateException("USD/ZWG rate values were not found on the RBZ page");
        }

        // Sanity-check the extracted row before accepting it.
        if (bid.signum() <= 0 || ask.signum() <= 0 || avg.signum() <= 0) {
            throw new IllegalStateException("Invalid USD/ZWG values returned by RBZ");
        }

        return avg;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public record RateSnapshot(
            String baseCurrency,
            String quoteCurrency,
            BigDecimal rate,
            String source,
            LocalDateTime effectiveAt) {
    }

    private record ExternalRate(BigDecimal usdtUsd, BigDecimal usdZwg, LocalDateTime updatedAt) {
    }

    private record ExternalRateCache(ExternalRate rate, LocalDateTime updatedAt) {
    }
}
