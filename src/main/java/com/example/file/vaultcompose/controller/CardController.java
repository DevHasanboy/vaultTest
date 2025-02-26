package com.example.file.vaultcompose.controller;

import com.example.file.vaultcompose.entity.Card;
import com.example.file.vaultcompose.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.vault.core.*;
import org.springframework.vault.core.VaultPkiOperations.*;
import org.springframework.vault.support.*;
import org.springframework.web.bind.annotation.*;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/v1/card")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);
    private final CardRepository cardRepository;

    @Autowired
    private VaultTemplate vaultTemplate;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    // ✅ Karta yaratish va Vault'ga saqlash
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Card card) {
        try {
            cardRepository.save(card);
            logger.info("Card {} saved to database", card.getUsername());

            Map<String, Object> vaultData = new HashMap<>();
            vaultData.put("data", card);

            String vaultPath = "secret/data/test/" + card.getUsername();
            vaultTemplate.write(vaultPath, vaultData);

            logger.info("Card {} stored in Vault at {}", card.getUsername(), vaultPath);
            return ResponseEntity.ok("Card created and stored in Vault successfully!");
        } catch (Exception e) {
            logger.error("Error saving card: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ✅ Vault'dan barcha kartalarni olish
    @GetMapping("/all")
    public ResponseEntity<?> getAllCards() {
        try {
            VaultResponse response = vaultTemplate.read("secret/data/test/");
            if (response == null || response.getData() == null) {
                logger.warn("No cards found in Vault");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No cards found in Vault");
            }
            return ResponseEntity.ok(response.getData());
        } catch (Exception e) {
            logger.error("Error retrieving all cards from Vault: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ✅ Vault'dan username bo‘yicha kartani olish
    @GetMapping("/username")
    public ResponseEntity<?> getCard(@RequestParam String username) {
        try {
            String vaultPath = "secret/data/test/" + username;
            VaultResponse response = vaultTemplate.read(vaultPath);

            if (response == null || response.getData() == null) {
                logger.warn("Card {} not found in Vault", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Card not found in Vault");
            }

            return ResponseEntity.ok(response.getData());
        } catch (Exception e) {
            logger.error("Error retrieving card {} from Vault: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ✅ Vault PKI orqali sertifikat yaratish
    @PostMapping("/generate-cert")
    public ResponseEntity<?> generateCertificate(@RequestParam String commonName) {
        try {
            VaultPkiOperations pkiOperations = vaultTemplate.opsForPki("pki");
            VaultCertificateRequest request = VaultCertificateRequest.builder()
                    .ttl(Duration.ofHours(48))
                    .commonName(commonName)
                    .altNames(Collections.singletonList("alt." + commonName))
                    .build();

            VaultCertificateResponse response = pkiOperations.issueCertificate("production", request);
            CertificateBundle certificateBundle = response.getRequiredData();

            Map<String, Object> certData = new HashMap<>();
            certData.put("certificate", certificateBundle.getX509Certificate().toString());
            certData.put("privateKey", certificateBundle.getPrivateKeySpec().toString());
            certData.put("caCertificate", certificateBundle.getX509IssuerCertificate().toString());

            return ResponseEntity.ok(certData);
        } catch (Exception e) {
            logger.error("Error generating certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ✅ Vault PKI orqali sertifikatni bekor qilish
    @DeleteMapping("/revoke-cert")
    public ResponseEntity<?> revokeCertificate(@RequestParam String serialNumber) {
        try {
            VaultPkiOperations pkiOperations = vaultTemplate.opsForPki("pki");
            pkiOperations.revoke(serialNumber);
            return ResponseEntity.ok("Certificate revoked successfully!");
        } catch (Exception e) {
            logger.error("Error revoking certificate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
