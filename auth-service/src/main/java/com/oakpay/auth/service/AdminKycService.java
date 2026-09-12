package com.oakpay.auth.service;

import com.oakpay.auth.api.AdminKycDtos;
import com.oakpay.auth.api.KycDtos;
import com.oakpay.auth.user.IdentityDocument;
import com.oakpay.auth.user.IdentityDocumentRepository;
import com.oakpay.auth.user.KycProfile;
import com.oakpay.auth.user.KycProfileRepository;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminKycService {
    private final KycProfileRepository profiles; private final IdentityDocumentRepository documents; private final UserRepository users;
    public AdminKycService(KycProfileRepository profiles,IdentityDocumentRepository documents,UserRepository users){this.profiles=profiles;this.documents=documents;this.users=users;}
    @Transactional(readOnly=true) public List<AdminKycDtos.QueueItem> queue(String status){String normalized=status==null||status.isBlank()?"PENDING":status.trim().toUpperCase();return profiles.findByStatusOrderBySubmittedAtAsc(normalized).stream().map(this::item).toList();}
    @Transactional(readOnly=true) public AdminKycDtos.QueueSummary summary(){return new AdminKycDtos.QueueSummary(profiles.countByStatus("PENDING"),profiles.countByStatus("VERIFIED"),profiles.countByStatus("REJECTED"));}
    @Transactional(readOnly=true) public AdminKycDtos.QueueItem get(UUID id){return item(profiles.findById(id).orElseThrow(()->new IllegalArgumentException("KYC application not found")));}
    @Transactional public AdminKycDtos.DecisionResponse approve(UUID id){KycProfile p=pending(id);p.setStatus("VERIFIED");p.setRejectionReason(null);p.setReviewedAt(LocalDateTime.now());profiles.save(p);return new AdminKycDtos.DecisionResponse(p.getId(),p.getStatus(),null,p.getReviewedAt());}
    @Transactional public AdminKycDtos.DecisionResponse reject(UUID id,String reason){if(reason==null||reason.trim().length()<5)throw new IllegalArgumentException("A clear rejection reason is required");KycProfile p=pending(id);p.setStatus("REJECTED");p.setRejectionReason(reason.trim());p.setReviewedAt(LocalDateTime.now());profiles.save(p);return new AdminKycDtos.DecisionResponse(p.getId(),p.getStatus(),p.getRejectionReason(),p.getReviewedAt());}
    private KycProfile pending(UUID id){KycProfile p=profiles.findById(id).orElseThrow(()->new IllegalArgumentException("KYC application not found"));if(!"PENDING".equals(p.getStatus()))throw new IllegalArgumentException("Only pending KYC applications can be reviewed");return p;}
    private AdminKycDtos.QueueItem item(KycProfile p){User u=users.findById(p.getUserId()).orElseThrow(()->new IllegalStateException("KYC user no longer exists"));List<KycDtos.DocumentResponse> docs=documents.findByKycProfileIdOrderByCreatedAtDesc(p.getId()).stream().map(this::document).toList();return new AdminKycDtos.QueueItem(p.getId(),u.getId(),u.getFirstName()+" "+u.getLastName(),u.getEmail(),u.getCountry(),p.getStatus(),p.getSubmittedAt(),docs);}
    private KycDtos.DocumentResponse document(IdentityDocument d){return new KycDtos.DocumentResponse(d.getId(),d.getDocumentType(),mask(d.getDocumentNumber()),d.getStatus(),d.getRejectionReason(),d.getFrontDocumentPath()!=null,d.getBackDocumentPath()!=null,d.getCreatedAt(),d.getUpdatedAt());}
    private String mask(String value){if(value==null||value.isBlank())return null;String v=value.trim();return v.length()<=4?"••••":"••••"+v.substring(v.length()-4);}
}