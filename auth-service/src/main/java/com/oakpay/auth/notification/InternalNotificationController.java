package com.oakpay.auth.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/notifications")
public class InternalNotificationController {
    private final NotificationService service;
    private final String internalSecret;
    public InternalNotificationController(NotificationService service,@Value("${oakpay.internal-secret}") String internalSecret){this.service=service;this.internalSecret=internalSecret;}
    @PostMapping
    public ResponseEntity<Void> create(@RequestHeader(value="X-OakPay-Internal-Secret",required=false) String supplied,@RequestBody NotificationDtos.InternalCreateRequest r){
        if(supplied==null||internalSecret==null||!supplied.equals(internalSecret))return ResponseEntity.status(403).build();
        service.createInternal(r); return ResponseEntity.noContent().build();
    }
}
