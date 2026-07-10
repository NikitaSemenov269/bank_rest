package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BankCardsApplication {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Admin@123");
        System.out.println("==========================================");
        System.out.println("BCRYPT HASH: " + hash);
        System.out.println("==========================================");

        SpringApplication.run(BankCardsApplication.class, args);
    }
}
