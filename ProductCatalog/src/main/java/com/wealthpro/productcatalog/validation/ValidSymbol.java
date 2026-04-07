package com.wealthpro.productcatalog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidSymbolValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSymbol {

    String message() default "Symbol must be 1 to 20 uppercase letters or digits with no spaces or special characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}