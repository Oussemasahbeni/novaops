package com.novaops.notificationservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum EmailType {
  ADMIN_NEW_CONTACT_MESSAGE(
      "New Contact Message Received", "email/admin-contact-notification", null),
  SCRAPING_COMPLETE("Web Scraping Complete", "scrapingComplete", "/admin/articles/web-scraped");

  @Setter private static String frontendUrl;

  private final String subject;
  private final String templateName;
  private final String path;

  public String getFullPath() {
    return frontendUrl + path;
  }
}
