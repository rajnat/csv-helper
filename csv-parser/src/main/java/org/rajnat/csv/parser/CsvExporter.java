package org.rajnat.csv.parser;
import org.rajnat.csv.api.Exporter;
import org.rajnat.csv.exception.CsvParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class CsvExporter implements Exporter {
    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    /**
     * Exports a list of objects to a CSV file asynchronously.
     *
     * @param <T>      the type of objects in the list
     * @param data     the list of objects to export
     * @param fileName the name of the output CSV file
     * @return a CompletableFuture representing the asynchronous operation
     */
    public <T> CompletableFuture<?> exportToCsv(List<T> data, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CsvExporter.writeDataToCsv(data, fileName);
                return "Export successful"; // return success message or status
            } catch (IOException | CsvParseException e) {
                CsvExporter.log.error("Failed to export data to CSV", e);
                throw new RuntimeException(e); // Rethrow as unchecked exception
            }
        });
    }

    /**
     * Exports a stream of objects to a CSV file asynchronously.
     *
     * @param <T>        the type of objects in the stream
     * @param dataStream the stream of objects to export
     * @param fileName   the name of the output CSV file
     * @return a CompletableFuture representing the asynchronous operation
     */
    public <T> CompletableFuture<?> exportToCsv(Stream<T> dataStream, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CsvExporter.writeDataToCsv(dataStream, fileName);
                return "Export successful"; // return success message or status
            } catch (IOException | CsvParseException e) {
                CsvExporter.log.error("Failed to export data to CSV", e);
                throw new RuntimeException(e); // Rethrow as unchecked exception
            }
        });
    }

    /**
     * Writes the stream of objects to a CSV file in a streaming fashion.
     *
     * @param <T> the type of objects in the stream
     * @param dataStream the stream of objects to export
     * @param fileName the name of the output CSV file
     * @throws IOException if an I/O error occurs while writing to the file
     */
    private static <T> void writeDataToCsv(Stream<T> dataStream, String fileName) throws IOException, CsvParseException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Obtain a sample object for header generation
            T firstObject = dataStream.findFirst().orElseThrow(() -> new RuntimeException("The stream is empty"));

            // Write header
            writer.write(getCsvHeader(firstObject));
            writer.newLine();

            // Reset the stream after finding the first element
            Stream<T> remainingData = dataStream.skip(1);

            // Write data rows
            for (T object : remainingData.collect(Collectors.toList())) {
                writer.write(getCsvRow(object));
                writer.newLine();
            }
        }
    }

    /**
     * Writes the list of objects to a CSV file.
     *
     * @param <T> the type of objects in the list
     * @param data the list of objects to export
     * @param fileName the name of the output CSV file
     * @throws IOException if an I/O error occurs while writing to the file
     * @throws CsvParseException if there is a problem parsing the CSV data
     */
    private static <T> void writeDataToCsv(List<T> data, String fileName) throws IOException, CsvParseException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Write header
            writer.write(getCsvHeader(data.get(0)));
            writer.newLine();

            // Write data rows
            for (T object : data) {
                writer.write(getCsvRow(object));
                writer.newLine();
            }
        }
    }

    /**
     * Generates the CSV header based on the fields of an object.
     *
     * @param <T> the type of the object
     * @param object the object to extract field names from
     * @return a comma-separated string representing the CSV header
     */
    private static <T> String getCsvHeader(T object) {
        StringBuilder header = new StringBuilder();
        Field[] fields = object.getClass().getDeclaredFields();

        // Sort fields based on 'order' value in CsvField
        List<Field> sortedFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(CsvField.class))
                .sorted((f1, f2) -> Integer.compare(f1.getAnnotation(CsvField.class).order(),
                        f2.getAnnotation(CsvField.class).order()))
                .toList();

        for (Field field : sortedFields) {
            CsvField annotation = field.getAnnotation(CsvField.class);
            header.append(annotation.name()).append(",");
        }

        return header.toString().replaceAll(",$", ""); // Remove the trailing comma
    }

    /**
     * Generates a CSV row for an object.
     *
     * @param <T> the type of the object
     * @param object the object to extract field values from
     * @return a comma-separated string representing the CSV row
     * @throws CsvParseException if there is a problem parsing the CSV data
     */
    private static <T> String getCsvRow(T object) throws CsvParseException {
        StringBuilder row = new StringBuilder();
        Field[] fields = object.getClass().getDeclaredFields();

        // Sort fields based on 'order' value in CsvField
        List<Field> sortedFields = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(CsvField.class))
                .sorted((f1, f2) -> Integer.compare(f1.getAnnotation(CsvField.class).order(),
                        f2.getAnnotation(CsvField.class).order()))
                .toList();

        for (Field field : sortedFields) {
            try {
                field.setAccessible(true); // Make the field accessible even if it's private
                Object value = field.get(object);
                row.append(value != null ? value.toString() : "").append(",");
            } catch (IllegalAccessException e) {
                log.error("Illegal access Exception while accessing field {}", field, e);
                throw new CsvParseException(format("Failed to access field: %s", field), e);
            }
        }

        return row.toString().replaceAll(",$", ""); // Remove the trailing comma
    }
}
