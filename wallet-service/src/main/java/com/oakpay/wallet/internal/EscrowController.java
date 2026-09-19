package com.oakpay.wallet.internal;

import com.oakpay.wallet.security.AuditLogClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/internal/escrow")
public class EscrowController {
    private final EscrowService escrowService;
    private final String secret;
    private final AuditLogClient auditLogClient;
    public EscrowController(EscrowService escrowService,@Value("${oakpay.internal-secret}") String secret,AuditLogClient auditLogClient){this.escrowService=escrowService;this.secret=secret;this.auditLogClient=auditLogClient;}
    @PostMapping("/release") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@RequestHeader(value="X-OakPay-Internal-Secret",required=false) String supplied,
                        @RequestHeader(value="X-OakPay-Admin-Actor",required=false) String actor,
                        @RequestBody InternalWalletDtos.EscrowReleaseRequest request){
        if(secret==null||secret.isBlank()||!secret.equals(supplied)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,"Invalid internal credential");
        escrowService.release(request.sellerId(),request.buyerId(),request.asset(),request.amount(),request.reference());
        UUID actorId=null; if(actor!=null&&!actor.isBlank()){try{actorId=UUID.fromString(actor.trim());}catch(IllegalArgumentException ignored){}}
        auditLogClient.record(actorId,"WALLET_ESCROW_RELEASED","WALLET_ESCROW",request.reference(),"SUCCESS","{\"asset\":\""+request.asset()+"\",\"amount\":"+request.amount()+"}");
    }
}