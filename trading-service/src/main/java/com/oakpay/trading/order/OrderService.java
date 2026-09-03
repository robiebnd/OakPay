package com.oakpay.trading.order;

import com.oakpay.trading.api.TradingDtos;
import com.oakpay.trading.asset.AssetStatus;
import com.oakpay.trading.asset.SupportedAsset;
import com.oakpay.trading.asset.SupportedAssetRepository;
import com.oakpay.trading.market.TradingPair;
import com.oakpay.trading.market.TradingPairRepository;
import com.oakpay.trading.market.TradingPairStatus;
import com.oakpay.trading.wallet.WalletClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final WalletClient walletClient;
    private final TradingPairRepository tradingPairRepository;
    private final SupportedAssetRepository supportedAssetRepository;
    private final BigDecimal feeRate;

    public OrderService(OrderRepository orderRepository, TradeRepository tradeRepository, WalletClient walletClient,
                        TradingPairRepository tradingPairRepository, SupportedAssetRepository supportedAssetRepository,
                        @Value("${oakpay.trading.fee-rate:0.001}") BigDecimal feeRate) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.walletClient = walletClient;
        this.tradingPairRepository = tradingPairRepository;
        this.supportedAssetRepository = supportedAssetRepository;
        this.feeRate = feeRate;
    }

    @Transactional
    public TradingDtos.OrderResponse place(UUID userId, TradingDtos.CreateOrderRequest request) {
        TradingPair pair = validateAndResolvePair(request);

        Order order = new Order();
        order.setUserId(userId);
        order.setSide(request.side());
        order.setBaseCurrency(pair.getBaseCurrency());
        order.setQuoteCurrency(pair.getQuoteCurrency());
        order.setPrice(scale(request.price()));
        order.setQuantity(scale(request.quantity()));
        order.setRemainingQuantity(scale(request.quantity()));
        order.setStatus(OrderStatus.OPEN);
        order = orderRepository.save(order);

        BigDecimal reserve = order.getSide() == OrderSide.BUY
                ? order.getPrice().multiply(order.getQuantity()).multiply(BigDecimal.ONE.add(feeRate))
                : order.getQuantity();
        walletClient.lock(userId, reserveCurrency(order), reserve, order.getId());
        match(order);
        return response(order);
    }

    @Transactional
    public void cancel(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getUserId().equals(userId)) throw new IllegalArgumentException("Order does not belong to user");
        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.CANCELLED)
            throw new IllegalStateException("Order cannot be cancelled");
        BigDecimal reserve = order.getSide() == OrderSide.BUY
                ? order.getPrice().multiply(order.getRemainingQuantity()).multiply(BigDecimal.ONE.add(feeRate))
                : order.getRemainingQuantity();
        if (reserve.signum() > 0) walletClient.unlock(userId, reserveCurrency(order), reserve, order.getId());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<TradingDtos.OrderResponse> myOrders(UUID userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<TradingDtos.OrderResponse> orderBook(String base, String quote) {
        String b = normalize(base), q = normalize(quote);
        List<Order> buys = orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndSideAndStatusOrderByCreatedAtAsc(b, q, OrderSide.BUY, OrderStatus.OPEN);
        List<Order> sells = orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndSideAndStatusOrderByCreatedAtAsc(b, q, OrderSide.SELL, OrderStatus.OPEN);
        buys.sort(Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getCreatedAt));
        sells.sort(Comparator.comparing(Order::getPrice).thenComparing(Order::getCreatedAt));
        return java.util.stream.Stream.concat(buys.stream(), sells.stream()).map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<TradingDtos.TradeResponse> myTrades(UUID userId) {
        return tradeRepository.findAllByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId).stream().map(this::tradeResponse).toList();
    }

    private void match(Order incoming) {
        OrderSide opposite = incoming.getSide() == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
        List<Order> candidates = orderRepository.findAllByBaseCurrencyAndQuoteCurrencyAndSideAndStatusOrderByCreatedAtAsc(
                incoming.getBaseCurrency(), incoming.getQuoteCurrency(), opposite, OrderStatus.OPEN);
        Comparator<Order> comparator = incoming.getSide() == OrderSide.BUY
                ? Comparator.comparing(Order::getPrice).thenComparing(Order::getCreatedAt)
                : Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getCreatedAt);
        candidates.sort(comparator);

        for (Order resting : candidates) {
            if (incoming.getRemainingQuantity().signum() <= 0) break;
            if (resting.getUserId().equals(incoming.getUserId())) continue;
            if (!crosses(incoming, resting)) break;

            BigDecimal fill = incoming.getRemainingQuantity().min(resting.getRemainingQuantity());
            BigDecimal executionPrice = resting.getPrice();
            BigDecimal gross = executionPrice.multiply(fill);
            BigDecimal buyerFee = gross.multiply(feeRate);
            BigDecimal sellerFee = gross.multiply(feeRate);
            UUID buyerId = incoming.getSide() == OrderSide.BUY ? incoming.getUserId() : resting.getUserId();
            UUID sellerId = incoming.getSide() == OrderSide.SELL ? incoming.getUserId() : resting.getUserId();
            Order buyOrder = incoming.getSide() == OrderSide.BUY ? incoming : resting;
            Order sellOrder = incoming.getSide() == OrderSide.SELL ? incoming : resting;

            walletClient.settle(new WalletClient.Settlement(buyerId, sellerId, incoming.getBaseCurrency(), incoming.getQuoteCurrency(),
                    fill, gross, buyerFee, sellerFee, "TRADE-" + UUID.randomUUID()));

            Trade trade = new Trade();
            trade.setBuyOrderId(buyOrder.getId()); trade.setSellOrderId(sellOrder.getId());
            trade.setBuyerId(buyerId); trade.setSellerId(sellerId);
            trade.setBaseCurrency(incoming.getBaseCurrency()); trade.setQuoteCurrency(incoming.getQuoteCurrency());
            trade.setPrice(executionPrice); trade.setQuantity(fill); trade.setGrossValue(gross);
            trade.setBuyerFee(buyerFee); trade.setSellerFee(sellerFee);
            tradeRepository.save(trade);

            incoming.setRemainingQuantity(incoming.getRemainingQuantity().subtract(fill));
            resting.setRemainingQuantity(resting.getRemainingQuantity().subtract(fill));
            updateStatus(incoming); updateStatus(resting);
            orderRepository.save(incoming); orderRepository.save(resting);

            if (buyOrder == incoming) {
                BigDecimal improvement = incoming.getPrice().subtract(executionPrice).multiply(fill);
                if (improvement.signum() > 0) walletClient.unlock(buyerId, incoming.getQuoteCurrency(), improvement, incoming.getId());
            }
        }
    }

    private boolean crosses(Order incoming, Order resting) {
        return incoming.getSide() == OrderSide.BUY
                ? incoming.getPrice().compareTo(resting.getPrice()) >= 0
                : incoming.getPrice().compareTo(resting.getPrice()) <= 0;
    }

    private void updateStatus(Order order) {
        order.setStatus(order.getRemainingQuantity().signum() == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);
    }

    private String reserveCurrency(Order order) {
        return order.getSide() == OrderSide.BUY ? order.getQuoteCurrency() : order.getBaseCurrency();
    }

    private TradingPair validateAndResolvePair(TradingDtos.CreateOrderRequest r) {
        if (r.side() == null) throw badRequest("Order side is required");
        String base = normalize(r.baseCurrency());
        String quote = normalize(r.quoteCurrency());

        if (base.equals(quote)) throw badRequest("Base and quote currencies must differ");
        if (r.price() == null || r.price().signum() <= 0) throw badRequest("Price must be greater than zero");
        if (r.quantity() == null || r.quantity().signum() <= 0) throw badRequest("Quantity must be greater than zero");

        TradingPair pair = tradingPairRepository.findByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCase(base, quote)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trading pair is not supported"));

        if (pair.getStatus() != TradingPairStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trading pair is not active");
        }

        requireSpotEnabledAsset(pair.getBaseCurrency());
        requireSpotEnabledAsset(pair.getQuoteCurrency());

        BigDecimal price = r.price();
        BigDecimal quantity = r.quantity();
        if (price.compareTo(pair.getMinPrice()) < 0 || price.compareTo(pair.getMaxPrice()) > 0) {
            throw badRequest("Price is outside the allowed trading range");
        }
        if (!aligned(price, pair.getPriceTickSize())) {
            throw badRequest("Price does not match the market tick size");
        }
        if (quantity.compareTo(pair.getMinQuantity()) < 0) {
            throw badRequest("Quantity is below the market minimum");
        }
        if (!aligned(quantity, pair.getQuantityStepSize())) {
            throw badRequest("Quantity does not match the market step size");
        }
        return pair;
    }

    private void requireSpotEnabledAsset(String symbol) {
        SupportedAsset asset = supportedAssetRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset " + symbol + " is not supported"));
        if (asset.getStatus() != AssetStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset " + symbol + " is not active");
        }
        if (!Boolean.TRUE.equals(asset.getSpotEnabled())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Spot trading is disabled for asset " + symbol);
        }
    }

    private boolean aligned(BigDecimal value, BigDecimal step) {
        return value.remainder(step).compareTo(BigDecimal.ZERO) == 0;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private BigDecimal scale(BigDecimal value) { return value.setScale(18, RoundingMode.DOWN); }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw badRequest("Currency is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private TradingDtos.OrderResponse response(Order o) {
        return new TradingDtos.OrderResponse(o.getId(), o.getUserId(), o.getSide(), o.getStatus(), o.getBaseCurrency(), o.getQuoteCurrency(), o.getPrice(), o.getQuantity(), o.getRemainingQuantity(), o.getCreatedAt(), o.getUpdatedAt());
    }

    private TradingDtos.TradeResponse tradeResponse(Trade t) {
        return new TradingDtos.TradeResponse(t.getId(), t.getBuyOrderId(), t.getSellOrderId(), t.getBuyerId(), t.getSellerId(), t.getBaseCurrency(), t.getQuoteCurrency(), t.getPrice(), t.getQuantity(), t.getGrossValue(), t.getBuyerFee(), t.getSellerFee(), t.getCreatedAt());
    }
}
