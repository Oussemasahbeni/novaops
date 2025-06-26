package com.novaops.gateway.routes;

import static org.apache.commons.lang.StringUtils.upperCase;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

  @RequestMapping("/{segment}")
  public ResponseEntity<Object> fallback(@PathVariable String segment) {
    Map<String, String> mapResponse = new HashMap<>();
    mapResponse.put("status", upperCase(segment) + " IS UNAVAILABLE, PLEASE TRY AGAIN LATER");
    return new ResponseEntity<>(mapResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
