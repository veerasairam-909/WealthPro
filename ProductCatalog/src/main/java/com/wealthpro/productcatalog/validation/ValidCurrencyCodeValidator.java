package com.wealthpro.productcatalog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class ValidCurrencyCodeValidator
        implements ConstraintValidator<ValidCurrencyCode, String> {

    // Accepted ISO 4217 currency codes relevant to wealth management
    private static final Set<String> VALID_CURRENCIES = Set.of(
            "INR",  // Indian Rupee
            "USD",  // US Dollar
            "EUR",  // Euro
            "GBP",  // British Pound
            "JPY",  // Japanese Yen
            "AUD",  // Australian Dollar
            "CAD",  // Canadian Dollar
            "CHF",  // Swiss Franc
            "SGD",  // Singapore Dollar
            "HKD",  // Hong Kong Dollar
            "CNY",  // Chinese Yuan
            "AED",  // UAE Dirham
            "SAR",  // Saudi Riyal
            "ZAR",  // South African Rand
            "BRL"   // Brazilian Real
    );

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null) {
            return true;
        }

        if (!currency.matches("^[A-Z]{3}$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Currency must be exactly 3 uppercase letters (e.g. INR, USD, EUR)"
            ).addConstraintViolation();
            return false;
        }

        if (!VALID_CURRENCIES.contains(currency)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Currency '" + currency + "' is not a supported currency code. "
                            + "Allowed: INR, USD, EUR, GBP, JPY, AUD, CAD, CHF, SGD, HKD, CNY, AED, SAR, ZAR, BRL"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}