package com.evtrading.swp391.service;

import com.evtrading.swp391.dto.ContractCreateDTO;
import com.evtrading.swp391.dto.ContractDTO;
import com.evtrading.swp391.entity.Contract;
import com.evtrading.swp391.entity.Order;
import com.evtrading.swp391.entity.User;
import com.evtrading.swp391.repository.ContractRepository;
import com.evtrading.swp391.repository.OrderRepository;
import com.evtrading.swp391.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DocuSealService docuSealService;

    public ContractService(ContractRepository contractRepository,
                           OrderRepository orderRepository,
                           UserRepository userRepository,
                           DocuSealService docuSealService) {
        this.contractRepository = contractRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.docuSealService = docuSealService;
    }

    @Transactional
    public ContractDTO createDraftAndSend(ContractCreateDTO req, String currentUsername) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + req.getOrderId()));

        // authorize: current user must be buyer or seller
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));
        boolean isBuyer = order.getBuyer() != null && order.getBuyer().getUserID().equals(current.getUserID());
        boolean isSeller = order.getListing() != null && order.getListing().getUser() != null &&
                order.getListing().getUser().getUserID().equals(current.getUserID());
        if (!isBuyer && !isSeller) {
            throw new SecurityException("Not allowed to create contract for this order");
        }

        // existing contract? return existing DTO
        Optional<Contract> existingOpt = contractRepository.findByOrder(order);
        if (existingOpt.isPresent()) {
            Contract exists = existingOpt.get();
            // Nếu đã có contract nhưng vẫn là Draft và chưa có envelope thật, thử tạo mới trên DocuSeal
            String envId = exists.getDocusealEnvelopeID();
            boolean looksMock = envId != null && envId.startsWith("mock-env");
            boolean needResend = "Draft".equalsIgnoreCase(exists.getStatus()) && (envId == null || looksMock);
            if (needResend) {
                // Cập nhật nội dung và thông tin người ký nếu có thay đổi
                exists.setContent(req.getContent());
                exists.setDocusealTemplateID(req.getTemplateId());
                exists.setSignerEmail(req.getSignerEmail());
                exists.setSignerName(req.getSignerName());
                DocuSealService.CreateResult result = docuSealService.createEnvelope(
                        req.getTemplateId(), req.getSignerEmail(), req.getSignerName(), req.getContent()
                );
                exists.setDocusealEnvelopeID(result.envelopeId);
                exists.setSigningUrl(result.signingUrl);
                exists.setStatus("Pending");
                exists.setUpdateAt(new Date());
                contractRepository.save(exists);
            }
            return toDTO(exists);
        }

        // create draft contract
        Date now = new Date();
        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setContent(req.getContent());
        contract.setDocusealTemplateID(req.getTemplateId());
        contract.setSignerEmail(req.getSignerEmail());
        contract.setSignerName(req.getSignerName());
        contract.setStatus("Draft");
        contract.setCreateAt(now);
        contract.setUpdateAt(now);
        contract = contractRepository.save(contract);

        // call DocuSeal to create a submission/envelope
        DocuSealService.CreateResult result = docuSealService.createEnvelope(
                req.getTemplateId(), req.getSignerEmail(), req.getSignerName(), req.getContent()
        );

        // update contract with returned ids and signing url
        contract.setDocusealEnvelopeID(result.envelopeId);
        contract.setSigningUrl(result.signingUrl);
    contract.setStatus("Pending");
        contract.setUpdateAt(new Date());
        contract = contractRepository.save(contract);

        return toDTO(contract);
    }

    @Transactional(readOnly = true)
    public ContractDTO getByOrderId(Integer orderId) {
        return contractRepository.findByOrder_OrderID(orderId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public void handleWebhookUpdate(String envelopeId, String status, String signedFileUrl, Date signedAt) {
        Contract contract = contractRepository.findByDocusealEnvelopeID(envelopeId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found for envelope: " + envelopeId));
        if (status != null) contract.setStatus(normalizeStatus(status));
        if (signedFileUrl != null && !signedFileUrl.isBlank()) contract.setSignedFileUrl(signedFileUrl);
        if (signedAt != null) contract.setSignedAt(signedAt);
        contract.setUpdateAt(new Date());
        contractRepository.save(contract);
    }

    /**
     * Fallback for form.* webhooks that miss submission_id: locate the most recent Draft contract by signer email.
     */
    @Transactional
    public void handleWebhookFallbackByEmail(String email, String status, String signedFileUrl, Date signedAt) {
        contractRepository.findTopBySignerEmailOrderByUpdateAtDesc(email).ifPresent(contract -> {
            // Only update if likely the latest draft for this signer
            if (contract.getStatus() == null || "Draft".equalsIgnoreCase(contract.getStatus()) || "Pending".equalsIgnoreCase(contract.getStatus())) {
                if (status != null) contract.setStatus(normalizeStatus(status));
                if (signedFileUrl != null && !signedFileUrl.isBlank()) contract.setSignedFileUrl(signedFileUrl);
                if (signedAt != null) contract.setSignedAt(signedAt);
                contract.setUpdateAt(new Date());
                contractRepository.save(contract);
            }
        });
    }

    /**
     * Backwards-compatible overload: some callers may supply eventType as an extra string param.
     */
    @Transactional
    public void handleWebhookFallbackByEmail(String email, String status, String signedFileUrl, String eventType, Date signedAt) {
        // ignore eventType for now and delegate
        handleWebhookFallbackByEmail(email, status, signedFileUrl, signedAt);
    }

    /**
     * Fallback by email but also set envelopeId/submissionId when provided (to link future events by id).
     */
    @Transactional
    public void handleWebhookFallbackByEmailWithId(String email, String envelopeId, String status, String signedFileUrl, Date signedAt) {
        contractRepository.findTopBySignerEmailOrderByUpdateAtDesc(email).ifPresent(contract -> {
            if (contract.getDocusealEnvelopeID() == null && envelopeId != null && !envelopeId.isBlank()) {
                contract.setDocusealEnvelopeID(envelopeId);
            }
            if (status != null) contract.setStatus(normalizeStatus(status));
            if (signedFileUrl != null && !signedFileUrl.isBlank()) contract.setSignedFileUrl(signedFileUrl);
            if (signedAt != null) contract.setSignedAt(signedAt);
            contract.setUpdateAt(new Date());
            contractRepository.save(contract);
        });
    }

    private String normalizeStatus(String status) {
        String s = status.toLowerCase();
        return switch (s) {
            case "completed", "signed" -> "Signed";
            case "declined" -> "Declined";
            case "pending" -> "Pending";
            default -> status;
        };
    }

    private ContractDTO toDTO(Contract c) {
        ContractDTO dto = new ContractDTO();
        dto.setContractId(c.getContractID());
        dto.setOrderId(c.getOrder() != null ? c.getOrder().getOrderID() : null);
        dto.setTemplateId(c.getDocusealTemplateID());
        dto.setEnvelopeId(c.getDocusealEnvelopeID());
        dto.setStatus(c.getStatus());
        dto.setSignedFileUrl(c.getSignedFileUrl());
        dto.setSignedAt(c.getSignedAt());
        dto.setUpdateAt(c.getUpdateAt());
        dto.setSigningUrl(c.getSigningUrl());
        return dto;
    }
}
