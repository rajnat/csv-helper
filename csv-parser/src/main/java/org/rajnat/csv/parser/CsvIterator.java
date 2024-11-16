package org.rajnat.csv.parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class CsvIterator<T> implements Iterator<T> {

    private static final Logger log = LoggerFactory.getLogger(CsvIterator.class);
    private BufferedReader reader;
    private String currentLine;
    private final String headerLine;
    private boolean endOfFile = false;
    private final Deserializer deserializer;
    private String[] headers;
    private Class<T> clazz;

    public CsvIterator(String filePath, Deserializer deserializer, Class<T> clazz) throws IOException {
        this.deserializer = deserializer;
        this.clazz = clazz;
        this.reader = new BufferedReader(new FileReader(filePath));
        this.headerLine = reader.readLine(); // Read the first line
        if (!deserializer.validateHeaders(headerLine, clazz)) {
            throw new IllegalArgumentException("CSV headers are invalid.");
        }
        headers = headerLine.split(",");
    }

    @Override
    public boolean hasNext() {
        return !endOfFile;
    }

    @Override
    public T next() {
        if (endOfFile) {
            throw new NoSuchElementException("End of file reached");
        }

        try {
            T pojo = deserializer.mapCsvToObject(currentLine.split(","), headers, clazz); // Your method to convert CSV line to POJO
            currentLine = reader.readLine(); // Read next line
            if (currentLine == null) {
                endOfFile = true;
                try {
                    reader.close(); // Close the reader at the end
                } catch (IOException e) {
                    log.error("Failed to close the fileReader with the error:", e);
                }
            }
            return pojo;
        } catch (IOException e) {
            throw new RuntimeException("Error reading line from file", e);
        }
    }
}
