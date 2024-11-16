package org.rajnat.csv.api;

import org.rajnat.csv.exception.CsvParseException;
import org.rajnat.csv.parser.CsvExporter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface Exporter {

    public <T> CompletableFuture<?> exportToCsv(List<T> data, String fileName);
    public <T> CompletableFuture<?> exportToCsv(Stream<T> dataStream, String fileName);
}
