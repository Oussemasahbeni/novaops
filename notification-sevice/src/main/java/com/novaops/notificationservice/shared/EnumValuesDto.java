package com.novaops.notificationservice.shared;

import java.util.Map;

public record EnumValuesDto(String name, Map<String, String> translations) {}
