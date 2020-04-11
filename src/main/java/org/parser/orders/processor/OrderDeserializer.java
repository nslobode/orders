package org.parser.orders.processor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.parser.orders.model.Order;
import org.springframework.util.StringUtils;
import static org.parser.orders.utils.ErrorMessages.ERROR;
import static org.parser.orders.utils.ErrorMessages.FIELD_IS_MISSING;

import java.io.IOException;

public class OrderDeserializer extends StdDeserializer<Order> {

	public OrderDeserializer(Class<?> vc) {
		super(vc);
	}

	public OrderDeserializer() {
		this(Order.class);
	}

	@Override
	public Order deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		ObjectCodec codec = parser.getCodec();
		JsonNode node = codec.readTree(parser);
		return parseOrder(node);
	}

	private Order parseOrder(JsonNode node) {
		Order order = new Order();
		StringBuilder errors = new StringBuilder();
		JsonNode id = node.get("orderId");
		if (id != null) {
			try {
				order.setId(Integer.parseInt(id.asText()));
			} catch (NumberFormatException e) {
				order.setId(-1);
				errors.append("Couldn't parse id, not a valid number. ");
			}
		} else {
			order.setId(-1);
			errors.append("Couldn't get id field");
		}
		JsonNode amount = node.get("amount");
		if (amount != null) {
			try {
				order.setAmount(Integer.parseInt(amount.asText()));
			} catch (NumberFormatException e) {
				order.setAmount(-1L);
				errors.append("Couldn't parse amount, not a valid number. ");
			}
		} else {
			order.setAmount(-1L);
			errors.append(FIELD_IS_MISSING + "amount.");
		}
		JsonNode currency = node.get("currency");
		if (currency == null) {
			order.setCurrency(ERROR);
			errors.append(FIELD_IS_MISSING + "currency. ");
		} else {
			order.setCurrency(currency.asText());
		}
		JsonNode comment = node.get("comment");
		if (comment == null) {
			order.setComment(ERROR);
			errors.append(FIELD_IS_MISSING + "comment. ");
		} else {
			order.setComment(comment.asText());
		}
		if (StringUtils.isEmpty(errors.toString())) {
			order.setResult("OK");
		} else {
			order.setResult(errors.toString());
		}
		return order;
	}
}
