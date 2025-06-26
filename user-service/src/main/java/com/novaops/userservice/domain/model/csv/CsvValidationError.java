package com.novaops.userservice.domain.model.csv;

import com.novaops.userservice.domain.enums.ValidationErrorType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvValidationError {
  private Long lineNumber;
  private String field;
  private String value;
  private String errorMessage;
  private ValidationErrorType type;
}
