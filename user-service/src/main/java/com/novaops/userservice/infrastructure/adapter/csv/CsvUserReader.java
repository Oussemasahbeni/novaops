package com.novaops.userservice.infrastructure.adapter.csv;

import static com.novaops.userservice.infrastructure.adapter.csv.CsvUserNormalizer.normalize;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.enums.ValidationErrorType;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.model.csv.CsvImportOptions;
import com.novaops.userservice.domain.model.csv.CsvImportWarning;
import com.novaops.userservice.domain.model.csv.CsvValidationError;
import com.novaops.userservice.domain.model.csv.CsvValidationResult;
import com.novaops.userservice.domain.port.output.UserCsvLoader;
import com.novaops.userservice.infrastructure.dto.request.CsvUserRecord;
import com.novaops.userservice.infrastructure.mapper.UserMapper;
import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvUserReader implements UserCsvLoader {

  private final UserJpaRepository userRepository;
  private final CsvUserValidator csvUserValidator;
  private final UserMapper userMapper;

  @Override
  public CsvValidationResult parseAndValidateCsvUsers(MultipartFile file, CsvImportOptions options)
      throws IOException {

    List<CsvValidationError> errors = new ArrayList<>();
    List<CsvUserRecord> validRecords = new ArrayList<>();
    Set<String> emailsInFile = new HashSet<>();
    Set<String> existingEmails = new HashSet<>(userRepository.findAllEmails());

    try (Reader reader = new InputStreamReader(file.getInputStream());
        CSVParser csvParser =
            CSVFormat.DEFAULT
                .builder()
                .setHeader("firstName", "lastName", "email", "phoneNumber", "address", "role")
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get()
                .parse(reader)) {

      for (CSVRecord csvRecord : csvParser) {
        long lineNumber = csvRecord.getRecordNumber();

        // Optionally skip specific lines
        if (options.getSkipLineNumbers() != null
            && options.getSkipLineNumbers().contains(lineNumber)) {
          continue;
        }

        try {
          CsvUserRecord user = parseRecord(csvRecord);

          List<CsvImportWarning> warnings = new ArrayList<>();
          user = normalize(user, warnings, lineNumber);

          List<CsvValidationError> recordErrors =
              csvUserValidator.validateRecord(user, lineNumber, emailsInFile, existingEmails);

          if (recordErrors.isEmpty()) {
            validRecords.add(user);
            emailsInFile.add(user.email().toLowerCase());
          } else if (!options.isSkipInvalidRows()) {
            errors.addAll(recordErrors); // fail the whole process if even one error
          } else {
            errors.addAll(recordErrors); // collect errors, skip this record
          }

        } catch (Exception e) {
          errors.add(
              CsvValidationError.builder()
                  .lineNumber(lineNumber)
                  .field("general")
                  .errorMessage("Failed to parse record: " + e.getMessage())
                  .type(ValidationErrorType.SOMETHING_WENT_WRONG)
                  .build());
          if (!options.isSkipInvalidRows()) {
            break; // fail fast if we’re not skipping invalid rows
          }
        }
      }
    }

    List<User> users = validRecords.stream().map(userMapper::fromCsvRecord).toList();

    return CsvValidationResult.builder()
        .isValid(errors.isEmpty())
        .errors(errors)
        .users(users)
        .totalRecords(validRecords.size() + errors.size())
        .validRecordCount(validRecords.size())
        .errorRecordCount(errors.size())
        .build();
  }

  private CsvUserRecord parseRecord(CSVRecord csvRecord) {
    return new CsvUserRecord(
        getFieldValue(csvRecord, "firstName"),
        getFieldValue(csvRecord, "lastName"),
        getFieldValue(csvRecord, "email"),
        getFieldValue(csvRecord, "phoneNumber"),
        getFieldValue(csvRecord, "address"),
        getFieldValue(csvRecord, "role") != null
            ? Enum.valueOf(RoleType.class, Objects.requireNonNull(getFieldValue(csvRecord, "role")))
            : null);
  }

  private String getFieldValue(CSVRecord csvRecord, String fieldName) {
    try {
      String value = csvRecord.get(fieldName);
      return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
