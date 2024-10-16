package se.sundsvall.checklist.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.checklist.api.validation.ValidPeriod;

@ExtendWith(MockitoExtension.class)
class ValidPeriodConstraintValidatorTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext mockContext;

	@InjectMocks
	private final ValidPeriodConstraintValidator validator = new ValidPeriodConstraintValidator();

	@ParameterizedTest
	@ValueSource(strings = { "P1D", "P1M", "P1Y", "P1Y2M3D", "P1Y2M3W4D", "P-1Y2M" })
	void validPeriodTest(final String value) {
		assertThat(validator.isValid(value, mockContext)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "ABC123", "A5M", "TEST" })
	void invalidPeriodTest(final String value) {
		assertThat(validator.isValid(value, mockContext)).isFalse();
	}

	@Test
	void nullableFalseTest() {
		ValidPeriod validPeriod = mock(ValidPeriod.class);
		when(validPeriod.nullable()).thenReturn(false);

		validator.initialize(validPeriod);

		assertThat(validator.isValid(null, mockContext)).isFalse();
	}

	@Test
	void nullableTrueTest() {
		ValidPeriod validPeriod = mock(ValidPeriod.class);
		when(validPeriod.nullable()).thenReturn(true);

		validator.initialize(validPeriod);

		assertThat(validator.isValid(null, mockContext)).isTrue();
	}
}
