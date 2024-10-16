package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void transformToString() {
		assertThat(StringUtils.toReadableString(null)).isBlank();
		assertThat(StringUtils.toReadableString(emptyList())).isBlank();
		assertThat(StringUtils.toReadableString(List.of("Lorem Ipsum"))).isEqualTo("lorem ipsum");
		assertThat(StringUtils.toReadableString(List.of("Lorem", "Ipsum"))).isEqualTo("lorem and ipsum");
		assertThat(StringUtils.toReadableString(List.of("Lorem Ipsum", "Neque", "porro"))).isEqualTo("lorem ipsum, neque and porro");
	}

}
