package se.sundsvall.checklist.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.api.validation.ValidChannels;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

@ExtendWith(MockitoExtension.class)
class ValidChannelsConstraintValidatorTest {

	@Mock
	private ConstraintValidatorContext mockContext;

	@Mock
	private ConstraintViolationBuilder mockConstraintViolationBuilder;

	@InjectMocks
	private final ValidChannelsConstraintValidator validator = new ValidChannelsConstraintValidator();

	private static Stream<Arguments> validListsProvider() {
		return Stream.of(
			Arguments.of(Set.of(CommunicationChannel.EMAIL)),
			Arguments.of(Set.of(CommunicationChannel.NO_COMMUNICATION)));
	}

	@ParameterizedTest
	@MethodSource("validListsProvider")
	void validListTest(final Set<CommunicationChannel> value) {
		assertThat(validator.isValid(value, mockContext)).isTrue();

		verifyNoInteractions(mockConstraintViolationBuilder, mockConstraintViolationBuilder);
	}

	@Test
	void invalidListTest() {
		when(mockContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockConstraintViolationBuilder);

		final var set = Set.of(CommunicationChannel.EMAIL, CommunicationChannel.NO_COMMUNICATION);
		assertThat(validator.isValid(set, mockContext)).isFalse();

		verify(mockContext).disableDefaultConstraintViolation();
		verify(mockContext).buildConstraintViolationWithTemplate("Can not have NO_COMMUNICATION in combination with other values");
	}

	@Test
	void nullableTrueTest() {
		final var validChannels = mock(ValidChannels.class);
		when(validChannels.nullable()).thenReturn(true);

		validator.initialize(validChannels);

		assertThat(validator.isValid(null, mockContext)).isTrue();

		verifyNoInteractions(mockConstraintViolationBuilder, mockConstraintViolationBuilder);
	}

	@Test
	void nullableFalseTest() {
		when(mockContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockConstraintViolationBuilder);
		final var validChannels = mock(ValidChannels.class);
		validator.initialize(validChannels);

		assertThat(validator.isValid(null, mockContext)).isFalse();

		verify(mockContext).disableDefaultConstraintViolation();
		verify(mockContext).buildConstraintViolationWithTemplate("must not be empty");
	}
}
