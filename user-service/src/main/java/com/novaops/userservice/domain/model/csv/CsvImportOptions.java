package com.novaops.userservice.domain.model.csv;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvImportOptions {
  private boolean skipInvalidRows;
  private boolean updateExistingUsers;
  private boolean sendWelcomeEmails;
  private List<Long> skipLineNumbers;
}
