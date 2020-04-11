package org.parser.orders.utils;

import java.util.Arrays;

public class Checker {

	private Checker() {
	}

	public static void checkSupportedFormats(String... files) {
		Arrays.stream(files).filter(arg -> !arg.endsWith(".csv"))
				.filter(arg -> !arg.endsWith(".json"))
				.forEach(arg -> {
					throw new UnsupportedOperationException("File have unsupported format" + arg);
				});
	}
}
