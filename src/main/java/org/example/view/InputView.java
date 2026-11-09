package org.example.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;

public class InputView {

    private final BufferedReader reader;

    public InputView(BufferedReader reader) {
        this.reader = reader;
    }

    public String readLine() {
        String line = readFromReader();
        if (line == null) {
            throw new InputEndedException();
        }
        return line;
    }

    private String readFromReader() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}