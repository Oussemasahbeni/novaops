package com.novaops.userservice.infrastructure.adapter.csv;

import com.novaops.userservice.domain.enums.ValidationErrorType;
import com.novaops.userservice.domain.model.csv.CsvValidationError;
import com.novaops.userservice.infrastructure.dto.request.CsvUserRecord;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CsvUserValidator {

  private final Validator validator;

  public List<CsvValidationError> validateRecord(
      CsvUserRecord user, Long lineNumber, Set<String> emailsInFile, Set<String> existingEmails) {

    List<CsvValidationError> errors = new ArrayList<>();

    // 1. JSR-303 Bean Validation
    Set<ConstraintViolation<CsvUserRecord>> violations = validator.validate(user);
    for (ConstraintViolation<CsvUserRecord> violation : violations) {
      errors.add(
          CsvValidationError.builder()
              .lineNumber(lineNumber)
              .field(violation.getPropertyPath().toString())
              .value(getViolationValue(violation))
              .errorMessage(violation.getMessage())
              .type(getValidationErrorType(violation))
              .build());
    }

    // 2. Email uniqueness validation
    if (user.email() != null) {
      String emailLower = user.email().toLowerCase();

      // Check against existing database emails
      if (existingEmails.contains(emailLower)) {
        errors.add(
            CsvValidationError.builder()
                .lineNumber(lineNumber)
                .field("email")
                .value(user.email())
                .errorMessage("Email already exists in database")
                .type(ValidationErrorType.DUPLICATE_EMAIL)
                .build());
      }

      // Check against emails in the current file
      if (emailsInFile.contains(emailLower)) {
        errors.add(
            CsvValidationError.builder()
                .lineNumber(lineNumber)
                .field("email")
                .value(user.email())
                .errorMessage("Duplicate email found in file")
                .type(ValidationErrorType.DUPLICATE_EMAIL)
                .build());
      }
    }

    return errors;
  }

  private String getViolationValue(ConstraintViolation<CsvUserRecord> violation) {
    Object invalidValue = violation.getInvalidValue();
    return invalidValue != null ? invalidValue.toString() : null;
  }

  private ValidationErrorType getValidationErrorType(ConstraintViolation<CsvUserRecord> violation) {
    String annotationType =
        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();

    return switch (annotationType) {
      case "NotBlank", "NotNull", "NotEmpty" -> ValidationErrorType.MISSING_REQUIRED_FIELD;
      case "Email" -> ValidationErrorType.INVALID_EMAIL;
      case "Size", "Length" -> ValidationErrorType.FIELD_TOO_LONG;
      default -> ValidationErrorType.SOMETHING_WENT_WRONG;
    };
  }
}
