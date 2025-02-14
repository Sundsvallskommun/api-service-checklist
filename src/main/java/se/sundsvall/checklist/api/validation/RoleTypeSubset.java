package se.sundsvall.checklist.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.checklist.api.validation.impl.RoleTypeSubsetConstraintValidator;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Target({
	ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RoleTypeSubsetConstraintValidator.class)
public @interface RoleTypeSubset {
	RoleType[] oneOf() default {};

	/**
	 * Controls whether the value can be null or not.
	 * <p>
	 * If set to true, the validator will accept the value as valid when null.
	 * If set to false (default), the validator will reject the value as invalid when null.
	 *
	 * @return true if the value is accepted as nullable, false otherwise.
	 */
	boolean nullable() default false;

	String message() default "must be one of {oneOf}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
