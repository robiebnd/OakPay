package com.oakpay.auth.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class NotificationService {
    private static final URI EXPO_PUSH_URI = URI.create("https://exp.host/--/api/v2/push/send");
    private final NotificationRepository notificationRepository;
    private final NotificationDeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public NotificationService(NotificationRepository notificationRepository, NotificationDeviceRepository deviceRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.deviceRepository = deviceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly=true)
    public List<NotificationDtos.NotificationResponse> list(UUID userId, int limit) {
        int safe=Math.min(Math.max(limit,1),100);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0,safe)).stream().map(this::response).toList();
    }

    @Transactional(readOnly=true)
    public long unreadCount(UUID userId){return notificationRepository.countByUserIdAndReadAtIsNull(userId);}

    @Transactional
    public void markRead(UUID userId, UUID id){
        Notification n=notificationRepository.findByIdAndUserId(id,userId).orElseThrow(()->new IllegalArgumentException("Notification not found"));
        if(n.getReadAt()==null){n.setReadAt(java.time.LocalDateTime.now());notificationRepository.save(n);}
    }

    @Transactional
    public void markAllRead(UUID userId){
        var rows=notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,PageRequest.of(0,100));
        var now=java.time.LocalDateTime.now();
        rows.stream().filter(n->n.getReadAt()==null).forEach(n->n.setReadAt(now));
        notificationRepository.saveAll(rows);
    }

    @Transactional
    public void registerDevice(UUID userId, NotificationDtos.DeviceRequest request){
        String token=request.expoPushToken().trim();
        NotificationDevice d=deviceRepository.findByExpoPushToken(token).orElseGet(NotificationDevice::new);
        d.setUserId(userId); d.setExpoPushToken(token); d.setPlatform(request.platform().trim().toUpperCase(Locale.ROOT));
        d.setDeviceId(request.deviceId()==null?null:request.deviceId().trim()); d.setActive(true); deviceRepository.save(d);
    }



    @Transactional
    public void createInternal(NotificationDtos.InternalCreateRequest request){
        if(request.userId()==null)throw new IllegalArgumentException("userId is required");
        Notification n=new Notification();
        n.setUserId(request.userId()); n.setType(request.type().trim().toUpperCase(Locale.ROOT));
        n.setTitle(request.title().trim()); n.setMessage(request.message().trim()); n.setData(request.data());
        notificationRepository.save(n);
        sendPushBestEffort(n);
    }

    private NotificationDtos.NotificationResponse response(Notification n){
        return new NotificationDtos.NotificationResponse(n.getId(),n.getType(),n.getTitle(),n.getMessage(),n.getData(),n.getReadAt()!=null,n.getCreatedAt());
    }

    private void sendPushBestEffort(Notification n){
        for(NotificationDevice d:deviceRepository.findAllByUserIdAndActiveTrue(n.getUserId())){
            try{
                Map<String,Object> p=new LinkedHashMap<>();
                p.put("to",d.getExpoPushToken()); p.put("title",n.getTitle()); p.put("body",n.getMessage()); p.put("sound","default");
                if(n.getData()!=null&&!n.getData().isBlank()){try{p.put("data",objectMapper.readValue(n.getData(),Object.class));}catch(Exception ignored){}}
                String json=objectMapper.writeValueAsString(p);
                HttpRequest req=HttpRequest.newBuilder(EXPO_PUSH_URI).header("Content-Type","application/json").header("Accept","application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
                HttpResponse<String> result=httpClient.send(req,HttpResponse.BodyHandlers.ofString());
                if(result.statusCode()==200&&result.body().contains("DeviceNotRegistered")){d.setActive(false);deviceRepository.save(d);}
            }catch(Exception ignored){}
        }
    }
}
