package org.example.view;

import java.time.LocalDate;

public record CancelInput(int originalNumber, LocalDate cancelDate) {
}
