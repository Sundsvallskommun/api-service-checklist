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

	@Test
	void sanitizeAndCompress() {
		assertThat(StringUtils.sanitizeAndCompress(null)).isNull();
		assertThat(StringUtils.sanitizeAndCompress("Lorem Ipsum")).isEqualTo("Lorem Ipsum");
		assertThat(StringUtils.sanitizeAndCompress("Lorem\n\r\n\r\nIpsum")).isEqualTo("Lorem Ipsum");
		assertThat(StringUtils.sanitizeAndCompress("Lorem\n\rIpsum")).isEqualTo("Lorem Ipsum");
		assertThat(StringUtils.sanitizeAndCompress("Lorem\bIpsum")).isEqualTo("Lorem Ipsum");
		assertThat(StringUtils.sanitizeAndCompress("Lo%rem Ip\\\\\\\\sum")).isEqualTo("Lorem Ipsum");
		assertThat(StringUtils.sanitizeAndCompress("%Lorem\n\rIpsum")).isEqualTo("Lorem Ipsum");
		assertThat(StringUtils.sanitizeAndCompress("Lorem\nIpsum\nAbbacus")).isEqualTo("Lorem Ipsum Abbacus");
		assertThat(StringUtils.sanitizeAndCompress("Lorem\rIpsum\rAbbacus")).isEqualTo("Lorem Ipsum Abbacus");

	}
}
