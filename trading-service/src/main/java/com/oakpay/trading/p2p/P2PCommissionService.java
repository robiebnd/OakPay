package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class P2PCommissionService {
    private final P2PCommissionRepository repository;
    private final BigDecimal rate;
    public P2PCommissionService(P2PCommissionRepository repository,@Value("${oakpay.p2p.commission-rate:0.001}") BigDecimal rate){
        if(rate.signum()<0||rate.compareTo(BigDecimal.ONE)>0)throw new IllegalArgumentException("P2P commission rate must be between 0 and 1");
        this.repository=repository;this.rate=rate;
    }
    @Transactional public P2PCommission assess(P2PTrade trade){
        if(trade==null||trade.getId()==null)throw new IllegalArgumentException("Trade is required");
        return repository.findByTradeId(trade.getId()).orElseGet(()->{BigDecimal amount=trade.getFiatAmount().multiply(rate).setScale(2,RoundingMode.HALF_UP);P2PCommission c=new P2PCommission();c.setTradeId(trade.getId());c.setPayerId(trade.getSellerId());c.setFiatCurrency(trade.getFiatCurrency());c.setFiatAmount(trade.getFiatAmount().setScale(2,RoundingMode.HALF_UP));c.setRate(rate);c.setCommissionAmount(amount);c.setStatus(P2PCommissionStatus.ASSESSED);return repository.save(c);});
    }
    @Transactional public P2PCommission collect(UUID tradeId,P2PCommissionDtos.CollectionRequest request){
        P2PCommission c=getByTrade(tradeId); if(request==null)throw new IllegalArgumentException("Collection request is required");
        if(c.getStatus()==P2PCommissionStatus.COLLECTED){String ref=request.collectionReference()==null?"":request.collectionReference().trim();if(!ref.equals(c.getCollectionReference()))throw new IllegalStateException("Commission has already been collected with another reference");return c;}
        if(c.getStatus()==P2PCommissionStatus.WAIVED)throw new IllegalStateException("Waived commission cannot be collected");
        String ref=request.collectionReference()==null?"":request.collectionReference().trim();String method=request.collectionMethod()==null?"":request.collectionMethod().trim().toUpperCase();
        if(ref.isBlank()||method.isBlank())throw new IllegalArgumentException("Collection reference and method are required");
        c.setCollectionReference(ref);c.setCollectionMethod(method);c.setCollectedAt(LocalDateTime.now());c.setStatus(P2PCommissionStatus.COLLECTED);return repository.save(c);
    }
    @Transactional(readOnly=true) public P2PCommission getByTrade(UUID tradeId){return repository.findByTradeId(tradeId).orElseThrow(()->new IllegalArgumentException("Commission record not found"));}
}
