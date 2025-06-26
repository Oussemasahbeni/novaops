package com.novaops.userservice.domain.model.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novaops.userservice.domain.model.User;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvValidationResult {
  private boolean isValid;
  private List<CsvValidationError> errors;
  private List<CsvImportWarning> warnings;
  private int successfulImports;
  private int skippedRecords;
  private List<String> duplicateEmails;
  @JsonIgnore private List<User> users;
  private int totalRecords;
  private int validRecordCount;
  private int errorRecordCount;
}
