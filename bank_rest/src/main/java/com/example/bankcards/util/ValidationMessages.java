package com.example.bankcards.util;

// Утилитарный класс.
public class ValidationMessages {
    private ValidationMessages() {}

    public static final String CARD_NUMBER_NOT_BLANK =
            "Номер карты обязательное поле.";
    public static final String CARD_NUMBER_PATTERN =
            "Номер карты должен содержать от 16 до 19 цифр.";
    public static final String CARDHOLDER_NAME_NOT_BLANK =
            "Имя владельца обязательное поле.";
    public static final String CARDHOLDER_NAME_PATTERN =
            "Имя владельца должно содержать от 1 до 100 символов (буквы, пробелы, дефисы).";
    public static final String PASSWORD_PATTERN =
            "Пароль должен содержать от 8 до 50 символов, минимум одну цифру, одну строчную букву," +
                    " одну заглавную букву и один спецсимвол.";
    public static final String VALIDITY_PERIOD_PATTERN =
            "Требуемый формат: ММ.ГГГГ (например, 01.2030).";

    public static final String LOGIN_PATTERN = "Логин может содержать буквы, цифры и символы @#$%^&+=!_-";
}
