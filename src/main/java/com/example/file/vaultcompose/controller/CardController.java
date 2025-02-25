package com.example.file.vaultcompose.controller;

import com.example.file.vaultcompose.entity.Card;
import com.example.file.vaultcompose.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/card")
public class CardController {

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
            // Vault'ga username asosida saqlaymiz
            vaultTemplate.write("secret/cards/" + card.getUsername(), card);
            return ResponseEntity.ok("Card created and stored in Vault successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error " + e.getMessage());
        }
    }

    // ✅ Vault'dan barcha kartalarni olish
    @GetMapping("/all")
    public ResponseEntity<?> getAllCards() {
        VaultResponse response = vaultTemplate.read("secret/cards");
        return ResponseEntity.ok(response.getData());
    }

    // ✅ Vault'dan username bo‘yicha kartani olish
    @GetMapping("/username")
    public ResponseEntity<?> getCard(@RequestParam String username) {
        VaultResponse response = vaultTemplate.read("secret/cards/" + username);
        return ResponseEntity.ok(response.getData());
    }
}

