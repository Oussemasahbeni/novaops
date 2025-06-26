package com.novaops.userservice.infrastructure.adapter.csv;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.csv.CsvImportWarning;
import com.novaops.userservice.infrastructure.dto.request.CsvUserRecord;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class CsvUserNormalizer {

  private CsvUserNormalizer() {
    // Prevent instantiation
  }

  public static CsvUserRecord normalize(
      CsvUserRecord user, List<CsvImportWarning> warnings, long lineNumber) {

    String correctedFirstName = user.firstName();
    String correctedLastName = user.lastName();
    String correctedEmail = user.email();
    String correctedPhoneNumber = user.phoneNumber();
    String correctedAddress = user.address();
    RoleType correctedRoles = user.roles();

    // 1. Trim and capitalize first name
    if (correctedFirstName != null) {
      String trimmed = correctedFirstName.trim();
      String capitalized = StringUtils.capitalize(trimmed);
      if (!capitalized.equals(correctedFirstName)) {
        warnings.add(
            CsvImportWarning.builder()
                .lineNumber(lineNumber)
                .field("firstName")
                .message("Auto-corrected name capitalization")
                .originalValue(correctedFirstName)
                .correctedValue(capitalized)
                .build());
        correctedFirstName = capitalized;
      }
    }

    // 2. Normalize email
    if (correctedEmail != null) {
      String normalized = correctedEmail.toLowerCase().trim();
      if (!normalized.equals(correctedEmail)) {
        warnings.add(
            CsvImportWarning.builder()
                .lineNumber(lineNumber)
                .field("email")
                .message("Auto-corrected email format")
                .originalValue(correctedEmail)
                .correctedValue(normalized)
                .build());
        correctedEmail = normalized;
      }
    }

    // 3. Format phone number
    if (correctedPhoneNumber != null) {
      String formatted = formatPhoneNumber(correctedPhoneNumber);
      if (!formatted.equals(correctedPhoneNumber)) {
        warnings.add(
            CsvImportWarning.builder()
                .lineNumber(lineNumber)
                .field("phoneNumber")
                .message("Auto-corrected phone number format")
                .originalValue(correctedPhoneNumber)
                .correctedValue(formatted)
                .build());
        correctedPhoneNumber = formatted;
      }
    }

    return new CsvUserRecord(
        correctedFirstName,
        correctedLastName,
        correctedEmail,
        correctedPhoneNumber,
        correctedAddress,
        correctedRoles);
  }

  private static String formatPhoneNumber(String phone) {
    // Remove all non-digit characters
    return phone.replaceAll("[^\\d]", "");
  }
}
