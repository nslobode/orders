package org.parser.orders.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckerTest {

	@Test
	void checkSupportedFormatsFails() {
		String wrong = "order.xlsx";
		assertThrows(UnsupportedOperationException.class, () -> Checker.checkSupportedFormats(wrong));
	}
	@Test
	void checkSupportedFormatsPasses() {
		String[] right = {"order.json", "order.csv"};
		assertDoesNotThrow(() -> Checker.checkSupportedFormats(right));
	}
}