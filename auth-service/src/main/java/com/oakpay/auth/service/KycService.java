package com.oakpay.auth.service;

import com.oakpay.auth.api.KycDtos;
import com.oakpay.auth.user.IdentityDocument;
import com.oakpay.auth.user.IdentityDocumentRepository;
import com.oakpay.auth.user.KycProfile;
import com.oakpay.auth.user.KycProfileRepository;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KycService {
    private static final List<String> DOCUMENT_TYPES = List.of("NATIONAL_ID", "PASSPORT", "DRIVERS_LICENSE");
    private final KycProfileRepository kycRepository;
    private final IdentityDocumentRepository documentRepository;
    private final UserRepository userRepository;

    public KycService(KycProfileRepository kycRepository, IdentityDocumentRepository documentRepository, UserRepository userRepository) {
        this.kycRepository = kycRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public KycDtos.KycResponse get(UUID userId) {
        User user = findUser(userId);
        KycProfile profile = kycRepository.findByUserId(user.getId()).orElseGet(() -> createProfile(user.getId()));
        return toResponse(profile);
    }

    @Transactional
    public KycDtos.DocumentResponse addDocument(UUID userId, KycDtos.DocumentRequest request) {
        User user = findUser(userId);
        KycProfile profile = kycRepository.findByUserId(user.getId()).orElseGet(() -> createProfile(user.getId()));
        if (!List.of("NOT_STARTED", "REJECTED").contains(profile.getStatus())) {
            throw new IllegalArgumentException("Documents cannot be changed while KYC is under review or verified");
        }
        String type = request.documentType().trim().toUpperCase(Locale.ROOT);
        if (!DOCUMENT_TYPES.contains(type)) throw new IllegalArgumentException("Unsupported document type");
        IdentityDocument document = new IdentityDocument();
        document.setKycProfileId(profile.getId());
        document.setDocumentType(type);
        document.setDocumentNumber(normalize(request.documentNumber()));
        document.setStatus("PENDING");
        return toDocumentResponse(documentRepository.save(document));
    }

    @Transactional
    public KycDtos.KycResponse submit(UUID userId) {
        User user = findUser(userId);
        KycProfile profile = kycRepository.findByUserId(user.getId()).orElseGet(() -> createProfile(user.getId()));
        if ("VERIFIED".equals(profile.getStatus())) throw new IllegalArgumentException("Your KYC is already verified");
        if ("PENDING".equals(profile.getStatus())) throw new IllegalArgumentException("Your KYC is already under review");
        List<IdentityDocument> documents = documentRepository.findByKycProfileIdOrderByCreatedAtDesc(profile.getId());
        if (documents.isEmpty()) throw new IllegalArgumentException("Add at least one identity document before submitting KYC");
        boolean hasDocumentNumber = documents.stream().anyMatch(d -> d.getDocumentNumber() != null && !d.getDocumentNumber().isBlank());
        if (!hasDocumentNumber) throw new IllegalArgumentException("Provide a document number before submitting KYC");
        profile.setStatus("PENDING");
        profile.setRejectionReason(null);
        profile.setSubmittedAt(LocalDateTime.now());
        profile.setReviewedAt(null);
        return toResponse(kycRepository.save(profile));
    }

    private KycProfile createProfile(UUID userId) {
        KycProfile profile = new KycProfile();
        profile.setUserId(userId);
        profile.setStatus("NOT_STARTED");
        return kycRepository.save(profile);
    }

    private User findUser(UUID id) { return userRepository.findById(id).orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists")); }
    private String normalize(String value) { if (value == null) return null; String v = value.trim(); return v.isEmpty() ? null : v; }

    private KycDtos.KycResponse toResponse(KycProfile profile) {
        List<KycDtos.DocumentResponse> docs = documentRepository.findByKycProfileIdOrderByCreatedAtDesc(profile.getId()).stream().map(this::toDocumentResponse).toList();
        return new KycDtos.KycResponse(profile.getId(), profile.getUserId(), profile.getStatus(), profile.getRejectionReason(), profile.getSubmittedAt(), profile.getReviewedAt(), docs);
    }

    private KycDtos.DocumentResponse toDocumentResponse(IdentityDocument d) {
        return new KycDtos.DocumentResponse(d.getId(), d.getDocumentType(), mask(d.getDocumentNumber()), d.getStatus(), d.getRejectionReason(), d.getFrontDocumentPath() != null, d.getBackDocumentPath() != null, d.getCreatedAt(), d.getUpdatedAt());
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        if (v.length() <= 4) return "••••";
        return "••••" + v.substring(v.length() - 4);
    }
}
