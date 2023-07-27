package ru.practicum.shareit.exception.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.exception.exceptions.Violation;

import java.util.List;

@RequiredArgsConstructor
@Getter
@ToString
public class ValidationErrorResponse {
    private final String invalidEntity;
    private final List<Violation> violations;

    public String getLogMessage() {
        StringBuilder sb = new StringBuilder(String.format("invalidEntity: %s. ", invalidEntity));
        sb.append("Violations: ");
        for (Violation violation :
                violations) {
            sb.append(String.format("fieldName: %s / message: %s", violation.getFieldName(), violation.getMessage()));
        }
        return sb.toString();
    }
}
