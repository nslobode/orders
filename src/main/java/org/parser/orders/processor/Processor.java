package org.parser.orders.processor;

import org.parser.orders.model.Order;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface Processor {

	CompletableFuture<List<Order>> parseToObjectsFromStream(Stream<String> filenames);
}
