package com.app.core.validation;

import org.springframework.stereotype.Component;

import com.app.core.annotation.ProductCodeExistsConstraint;
import com.app.core.service.ProductService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCodeExistsConstraintValidation implements ConstraintValidator<ProductCodeExistsConstraint, String>{

	private final ProductService productService;
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		try {
			return productService.isCodeValid(value);	
		}catch (Exception e) {
				log.error(e.toString());
		}
		return false;
	}

}
