package com.app.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.app.core.validation.ProductCodeExistsConstraintValidation;

import jakarta.validation.Constraint;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ProductCodeExistsConstraintValidation.class)
public @interface ProductCodeExistsConstraint {
	String message() default "Code already exists";
	 @SuppressWarnings("rawtypes")
	Class[] groups() default {};
	 @SuppressWarnings("rawtypes")
	Class[] payload() default {};
}
