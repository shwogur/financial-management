package org.example.view.input;

import java.time.LocalDate;

public record CancelInput(int originalNumber, LocalDate cancelDate) {
}
