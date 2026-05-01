package com.wealthpro.productcatalog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidSymbolValidator
        implements ConstraintValidator<ValidSymbol, String> {

    @Override
    public boolean isValid(String symbol, ConstraintValidatorContext context) {

        if (symbol == null) {
            return true;
        }
        return symbol.matches("^[A-Z0-9]{1,20}$");
    }
}