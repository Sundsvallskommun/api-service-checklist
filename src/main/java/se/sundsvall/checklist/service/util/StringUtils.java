package se.sundsvall.checklist.service.util;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.Collections;
import java.util.List;

public final class StringUtils {
	private StringUtils() {}

	private static final String REGEXP_LAST_COMMA = "\\,(?=[^,]*$)";

	public static String toReadableString(List<String> values) {
		return ofNullable(values)
			.orElse(Collections.emptyList())
			.stream()
			.map(String::toLowerCase)
			.collect(joining(", "))
			.replaceAll(REGEXP_LAST_COMMA, " and");
	}

	public static String toSecureString(String value) {
		return ofNullable(value)
			.map(s -> s.replaceAll("[\n\r]", " "))
			.map(s -> s.replaceAll("(.)\\1+", "$1"))
			.orElse(null);
	}
}
