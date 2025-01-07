package com.example.petsitter.users;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTests {

    @ParameterizedTest
    @ValueSource(strings = {"1Password", "1Passwor", "1Password1Password12", "1Pass$ ^word"})
    void whenPasswordIsValidThenPasswordValidatorReturnsTrue(String password) {
        assertTrue(PasswordValidator.isValid(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "         ", "12345678", "abcdefgh", "ABCDEFGH", "1234abcd", "1234ABCD", "abcdABCD",
        "1Password1Password123"})
    void whenPasswordIsNotValidThenPasswordValidatorReturnsFalse(String password) {
        assertFalse(PasswordValidator.isValid(password));
    }
}
