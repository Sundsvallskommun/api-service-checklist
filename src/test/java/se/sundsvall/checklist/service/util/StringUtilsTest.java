package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.zalando.problem.Status;

class StringUtilsTest {

	@Test
	void transformToString() {
		assertThat(StringUtils.toReadableString(null)).isBlank();
		assertThat(StringUtils.toReadableString(emptyList())).isBlank();
		assertThat(StringUtils.toReadableString(List.of("Lorem Ipsum"))).isEqualTo("lorem ipsum");
		assertThat(StringUtils.toReadableString(List.of("Lorem", "Ipsum"))).isEqualTo("lorem and ipsum");
		assertThat(StringUtils.toReadableString(List.of("Lorem Ipsum", "Neque", "porro"))).isEqualTo("lorem ipsum, neque and porro");
	}

	@Test
	void getErrorCodeWhenNotParsable() {
		assertThat(StringUtils.getErrorCode(null)).isEqualTo(500);
		assertThat(StringUtils.getErrorCode("")).isEqualTo(500);
		assertThat(StringUtils.getErrorCode("20OK")).isEqualTo(500);
		assertThat(StringUtils.getErrorCode("2001OK")).isEqualTo(500);
		assertThat(StringUtils.getErrorCode("2001 OK")).isEqualTo(500);
		assertThat(StringUtils.getErrorCode("200OK")).isEqualTo(500);
	}

	@ParameterizedTest
	@EnumSource(value = Status.class)
	void getErrorCodeWhenParsable(Status status) {
		assertThat(StringUtils.getErrorCode(status.toString())).isEqualTo(status.getStatusCode());
	}
}
