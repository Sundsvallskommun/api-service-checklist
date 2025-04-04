package se.sundsvall.checklist.service.util;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
	private StringUtils() {}

	private static final String REGEXP_LAST_COMMA = "\\,(?=[^,]*$)";
	private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("(^\\d{3} )");

	public static String toReadableString(List<String> values) {
		return ofNullable(values)
			.orElse(Collections.emptyList())
			.stream()
			.map(String::toLowerCase)
			.collect(joining(", "))
			.replaceAll(REGEXP_LAST_COMMA, " and");
	}

	public static int getErrorCode(String errorString) {
		return ofNullable(errorString)
			.map(ERROR_CODE_PATTERN::matcher)
			.filter(Matcher::find)
			.map(Matcher::group)
			.map(String::trim)
			.map(Integer::parseInt)
			.orElse(500); // return 500 as default (if numeric error code can not be parsed)
	}
}
