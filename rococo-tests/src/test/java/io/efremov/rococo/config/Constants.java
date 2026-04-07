package io.efremov.rococo.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

  public static final String DEFAULT_PASSWORD = "123";
  public static final int MIN_TITLE_LENGTH = 3;
  public static final int MAX_TITLE_LENGTH = 255;
  public static final int MIN_DESCRIPTION_LENGTH = 10;
  public static final int MAX_DESCRIPTION_LENGTH = 1000;
  public static final int MIN_PARAGRAPH_LENGTH = 10;
  public static final int MAX_PARAGRAPH_LENGTH = 2000;
  public static final int MAX_PHOTO_SIZE = 1024 * 1024;

  public static final String INFO_API_TAG = "info";
  public static final String MUTATION_API_TAG = "mutation";
  public static final String UI_TAG = "ui";
}
