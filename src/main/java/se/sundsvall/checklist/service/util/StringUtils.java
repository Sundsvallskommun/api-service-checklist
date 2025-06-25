package se.sundsvall.checklist.service.util;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.Collections;
import java.util.List;

public final class StringUtils {
	private StringUtils() {}

	private static final String REGEXP_LAST_COMMA = "\\,(?=[^,]*$)";
	private static final String REGEXP_MULTIPLE_SPACES = "(\s)\\1+";
	private static final String REGEXP_NON_PRINTABLE_ASCII_CHARACTERS = "[^\\x20-\\x7E]";
	private static final String REGEXP_POTENTIAL_HARMFUL_CHARACTERS = "[%\\\\]";

	/**
	 * Helper method for creating a comma separated string with an ending 'and' for the values
	 * sent in to the method
	 *
	 * @param  values a list of strings that should be concatenated into a human readable string
	 * @return        a readable string based on the sent in list of strings
	 */
	public static String toReadableString(List<String> values) {
		return ofNullable(values)
			.orElse(Collections.emptyList())
			.stream()
			.map(String::toLowerCase)
			.collect(joining(", "))
			.replaceAll(REGEXP_LAST_COMMA, " and");
	}

	/**
	 * Helper method for sanitizing sent in string value by:
	 * - removing newlines and carriage returns
	 * - removing non-printable ASCII characters
	 * - removing other potentially harmful characters
	 * - compressing multiple spaces in a row into one space
	 *
	 * @param  value string to be sanitized
	 * @return       a sanitized string based on sent in string
	 */
	public static String sanitizeAndCompress(String value) {
		return ofNullable(value)
			.map(s -> s.replaceAll(REGEXP_NON_PRINTABLE_ASCII_CHARACTERS, " "))
			.map(s -> s.replaceAll(REGEXP_POTENTIAL_HARMFUL_CHARACTERS, ""))
			.map(s -> s.replaceAll(REGEXP_MULTIPLE_SPACES, "$1"))
			.orElse(null);
	}
}
