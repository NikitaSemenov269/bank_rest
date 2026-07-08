package com.example.bankcards.service.card;

import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardEncryptorAndDecrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServicesImpl implements CardServices {

    private final CardEncryptorAndDecrypt cardEncryptor;
    private final CardRepository repository;


}
