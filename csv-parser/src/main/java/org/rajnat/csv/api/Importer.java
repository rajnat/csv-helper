package org.rajnat.csv.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface Importer {
    <T> CompletableFuture<List<T>> importFromCsvAsync(String fileName, Class<T> clazz);
    <T> CompletableFuture<Stream<T>> importFromCsvStream(String filename, Class<T> clazz);
}
