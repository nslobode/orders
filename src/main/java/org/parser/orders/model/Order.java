package org.parser.orders.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Order {

	private long id;

	private long amount;

	private String currency;

	private String comment;

	private String filename;

	private long line;

	private String result;
}
