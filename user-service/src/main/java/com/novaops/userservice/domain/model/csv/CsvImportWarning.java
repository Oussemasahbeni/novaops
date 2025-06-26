package com.novaops.userservice.domain.model.csv;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvImportWarning {
  private Long lineNumber;
  private String field;
  private String message;
  private String originalValue;
  private String correctedValue;
}
