package com.reviewservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * Validates that the endField date is strictly after the startField date.
 * Works generically on any class that has two LocalDate fields.
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;
    private String message;

    @Override
    public void initialize(ValidDateRange annotation) {
        this.startField = annotation.startField();
        this.endField   = annotation.endField();
        this.message    = annotation.message();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return true;
        }

        try {
            Field start = object.getClass().getDeclaredField(startField);
            Field end   = object.getClass().getDeclaredField(endField);

            start.setAccessible(true);
            end.setAccessible(true);

            LocalDate startDate = (LocalDate) start.get(object);
            LocalDate endDate   = (LocalDate) end.get(object);

            // If either is null, let @NotNull handle it separately
            if (startDate == null || endDate == null) {
                return true;
            }

            boolean valid = endDate.isAfter(startDate);

            if (!valid) {
                // Attach error to endField instead of the whole object
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(endField)
                        .addConstraintViolation();
            }

            return valid;

        } catch (NoSuchFieldException | IllegalAccessException e) {
            return true;
        }
    }
}
