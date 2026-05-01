package com.wealthpro.productcatalog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;

public class ValidContentUriValidator
        implements ConstraintValidator<ValidContentUri, String> {

    @Override
    public boolean isValid(String contentUri, ConstraintValidatorContext context) {

        // Allow null — @NotBlank handles null check separately
        if (contentUri == null || contentUri.isBlank()) {
            return true;
        }

        // Must start with https:// — no http, ftp, or plain text
        if (!contentUri.startsWith("https://")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Content URI must start with 'https://' — plain http or other protocols are not allowed"
            ).addConstraintViolation();
            return false;
        }

        // Must be a valid URL structure
        try {
            URI uri = new URI(contentUri);

            // Must have a valid host — e.g. https:// alone is not valid
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Content URI must be a valid URL with a proper domain name"
                ).addConstraintViolation();
                return false;
            }

            // Host must contain a dot — e.g. screener.in not just screener
            if (!uri.getHost().contains(".")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Content URI must contain a valid domain (e.g. https://www.screener.in/...)"
                ).addConstraintViolation();
                return false;
            }

            return true;

        } catch (URISyntaxException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Content URI is not a valid URL format"
            ).addConstraintViolation();
            return false;
        }
    }
}