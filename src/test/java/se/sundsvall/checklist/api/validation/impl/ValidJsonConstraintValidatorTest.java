package se.sundsvall.checklist.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
class ValidJsonConstraintValidatorTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext mockContext;

	@InjectMocks
	private final ValidJsonConstraintValidator validator = new ValidJsonConstraintValidator();

	@Test
	void validJsonTest() {
		final var value = """
			{
				"name": "name",
				"roleType": "EMPLOYEE",
				"displayName": "displayName"
			}""";

		assertThat(validator.isValid(value, mockContext)).isTrue();
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "{", "}", "{]", "[}",
		"{}",
		"{\"name\": \"value\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"\"}",
		"{\"name\": \"value\", \"displayName\":\"value\"}",
		"{\"name\": \"\", \"roleType\": \"EMPLOYEE\", \"displayName\":\"value\"}" })
	void invalidJsonTest(final String value) {
		assertThat(validator.isValid(value, mockContext)).isFalse();
	}
}
