package se.sundsvall.checklist.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.api.validation.RoleTypeSubset;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@ExtendWith(MockitoExtension.class)
class RoleTypeSubsetConstraintValidatorTest {

	@Mock
	private ConstraintValidatorContext mockContext;

	@Mock
	private ConstraintViolationBuilder mockConstraintViolationBuilder;

	@InjectMocks
	private final RoleTypeSubsetConstraintValidator validator = new RoleTypeSubsetConstraintValidator();

	@Test
	void nullableTrue() {
		final var roleTypeSubset = mock(RoleTypeSubset.class);
		when(roleTypeSubset.nullable()).thenReturn(true);

		validator.initialize(roleTypeSubset);

		assertThat(validator.isValid(null, mockContext)).isTrue();

		verifyNoInteractions(mockConstraintViolationBuilder, mockConstraintViolationBuilder);
	}

	@Test
	void nullableFalse() {
		final var roleTypeSubset = mock(RoleTypeSubset.class);

		validator.initialize(roleTypeSubset);

		assertThat(validator.isValid(null, mockContext)).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = RoleType.class)
	void emptySubset(RoleType roleType) {
		final var roleTypeSubset = mock(RoleTypeSubset.class);
		when(roleTypeSubset.oneOf()).thenReturn(new RoleType[] {});

		validator.initialize(roleTypeSubset);

		assertThat(validator.isValid(roleType, mockContext)).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = RoleType.class)
	void fullSubset(RoleType roleType) {
		final var roleTypeSubset = mock(RoleTypeSubset.class);
		when(roleTypeSubset.oneOf()).thenReturn(RoleType.values());

		validator.initialize(roleTypeSubset);

		assertThat(validator.isValid(roleType, mockContext)).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = RoleType.class)
	void halfSubset(RoleType roleType) {
		final var validRoles = new RoleType[] {
			RoleType.MANAGER_FOR_NEW_EMPLOYEE, RoleType.MANAGER_FOR_NEW_EMPLOYEE
		};
		final var roleTypeSubset = mock(RoleTypeSubset.class);
		when(roleTypeSubset.oneOf()).thenReturn(validRoles);

		validator.initialize(roleTypeSubset);

		assertThat(validator.isValid(roleType, mockContext)).isEqualTo(List.of(validRoles).contains(roleType));
	}
}
