package se.sundsvall.checklist.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import se.sundsvall.checklist.api.validation.impl.ValidPeriodConstraintValidator;

/**
 * Annotation for validating that a string can be parsed to a {@link java.time.Period}.
 */
@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPeriodConstraintValidator.class)
public @interface ValidPeriod {

	String message() default "text is not in ISO-8601 period format";

	/**
	 * Controls whether the value can be null or not.
	 * <p>
	 * If set to true, the validator will accept the value as valid when null.
	 * If set to false (default), the validator will reject the value as invalid when null.
	 *
	 * @return true if the value is accepted as nullable, false otherwise.
	 */
	boolean nullable() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
