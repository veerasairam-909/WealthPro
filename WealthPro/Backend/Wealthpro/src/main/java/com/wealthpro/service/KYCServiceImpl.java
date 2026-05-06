package com.wealthpro.service;

import com.wealthpro.dto.request.KYCStatusUpdateRequestDTO;
import com.wealthpro.dto.response.KYCDocumentResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.entities.KYCDocument;
import com.wealthpro.enums.KycStatus;
import com.wealthpro.exception.InvalidOperationException;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.KYCDocumentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class KYCServiceImpl implements KYCService{

    private final KYCDocumentRepository kycDocumentRepository;
    private final ClientService clientService;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    public KYCServiceImpl(KYCDocumentRepository kycDocumentRepository,
                          ClientService clientService,
                          FileStorageService fileStorageService,
                          ModelMapper modelMapper) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.clientService = clientService;
        this.fileStorageService=fileStorageService;
        this.modelMapper = modelMapper;
    }


    // ADD a KYC document for a client

    @Override
    public KYCDocumentResponseDTO addKYCDocument(Long clientId,
                                                 String documentType,
                                                 MultipartFile document) {
        // Check client exists
        Client client = clientService.findClientOrThrow(clientId);

        // Save file locally — get back the path
        String filePath = fileStorageService.saveFile(document, clientId, documentType);

        //  Building the entity
        KYCDocument kycDocument = new KYCDocument();
        kycDocument.setClient(client);
        kycDocument.setDocumentType(documentType);
        kycDocument.setDocumentRef(filePath); // save local path in DB
        kycDocument.setStatus(KycStatus.Pending);
        kycDocument.setVerifiedDate(null);

        KYCDocument saved = kycDocumentRepository.save(kycDocument);
        return mapToResponse(saved);
    }


    // GET all KYC documents for a client

    public List<KYCDocumentResponseDTO> getKYCDocumentsByClient(Long clientId) {
        // Verify client exists first
        clientService.findClientOrThrow(clientId);

        List<KYCDocument> docs = kycDocumentRepository.findByClientClientId(clientId);
        List<KYCDocumentResponseDTO> result = new ArrayList<KYCDocumentResponseDTO>();
        for (KYCDocument d : docs) {
            result.add(mapToResponse(d));
        }
        return result;
    }


    // GET a single KYC document by its ID

    public KYCDocumentResponseDTO getKYCDocumentById(Long kycId) {
        KYCDocument kycDocument = findKYCOrThrow(kycId);
        return mapToResponse(kycDocument);
    }


    // UPDATE KYC status (Pending → Verified / Expired)

    public KYCDocumentResponseDTO updateKYCStatus(Long kycId,
                                                  KYCStatusUpdateRequestDTO requestDTO) {
        KYCDocument kycDocument = findKYCOrThrow(kycId);

        KycStatus currentStatus = kycDocument.getStatus();
        KycStatus newStatus = requestDTO.getStatus();

        // Business rule: cannot go back from Verified to Pending
        if (currentStatus == KycStatus.Verified && newStatus == KycStatus.Pending) {
            throw new InvalidOperationException(
                    "Cannot revert KYC status from Verified back to Pending");
        }

        // Business rule: cannot change an Expired document
        if (currentStatus == KycStatus.Expired) {
            throw new InvalidOperationException(
                    "Cannot update status of an Expired KYC document");
        }

        kycDocument.setStatus(newStatus);

        // If being marked as Verified, set verifiedDate to today and expiryDate to 1 year from now
        if (newStatus == KycStatus.Verified) {
            kycDocument.setVerifiedDate(LocalDate.now());
            kycDocument.setExpiryDate(LocalDate.now().plusYears(1));
        }

        KYCDocument updated = kycDocumentRepository.save(kycDocument);
        return mapToResponse(updated);
    }


    // DELETE a KYC document

    public void deleteKYCDocument(Long kycId) {
        KYCDocument kycDocument = findKYCOrThrow(kycId);

        // Delete file from local storage
        fileStorageService.deleteFile(kycDocument.getDocumentRef());

        // Delete DB record
        kycDocumentRepository.deleteById(kycId);
    }


    // private helper methods

    public KYCDocument findKYCOrThrow(Long kycId) {
        Optional<KYCDocument> optional = kycDocumentRepository.findById(kycId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("KYC Document not found with ID: " + kycId);
        }
    }

    private KYCDocumentResponseDTO mapToResponse(KYCDocument kycDocument) {
        KYCDocumentResponseDTO response = new KYCDocumentResponseDTO();
        response.setKycId(kycDocument.getKycId());
        response.setClientId(kycDocument.getClient().getClientId());
        response.setDocumentType(kycDocument.getDocumentType());
        response.setDocumentRef(kycDocument.getDocumentRef());  //path of the image
        response.setVerifiedDate(kycDocument.getVerifiedDate());
        response.setExpiryDate(kycDocument.getExpiryDate());
        response.setStatus(kycDocument.getStatus());
        return response;
    }
}
