package se.sundsvall.checklist.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.checklist.api.validation.impl.ValidJsonConstraintValidator;

@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidJsonConstraintValidator.class)
public @interface ValidJson {
	String message() default "not a valid structure";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
