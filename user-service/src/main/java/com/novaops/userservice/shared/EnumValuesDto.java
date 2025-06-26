package com.novaops.userservice.shared;

import java.util.Map;

public record EnumValuesDto(String name, Map<String, String> translations) {
}