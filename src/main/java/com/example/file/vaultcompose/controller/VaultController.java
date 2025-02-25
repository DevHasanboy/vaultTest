package com.example.file.vaultcompose.controller;

import com.example.file.vaultcompose.config.VaultConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vault")
public class VaultController {

    private final VaultConfig vaultConfig;

    public VaultController(VaultConfig vaultConfig) {
        this.vaultConfig = vaultConfig;
    }

    @GetMapping("/secrets")
    public String getSecrets() {
        return "Username: " + vaultConfig.getUsername() + ", Password: " + vaultConfig.getPassword();
    }
}
