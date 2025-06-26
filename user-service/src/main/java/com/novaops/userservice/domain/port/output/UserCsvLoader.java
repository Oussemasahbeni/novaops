package com.novaops.userservice.domain.port.output;

import com.novaops.userservice.domain.model.csv.CsvImportOptions;
import com.novaops.userservice.domain.model.csv.CsvValidationResult;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface UserCsvLoader {

  CsvValidationResult parseAndValidateCsvUsers(MultipartFile file, CsvImportOptions options)
      throws IOException;
}
