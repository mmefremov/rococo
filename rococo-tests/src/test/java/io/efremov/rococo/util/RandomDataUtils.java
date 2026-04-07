package io.efremov.rococo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.InstancioGenApi;
import org.jspecify.annotations.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomDataUtils {

  private static final Faker FAKER = new Faker();
  public static final InstancioGenApi GEN = Instancio.gen();

  @NonNull
  public static String randomUsername() {
    return FAKER.credentials().username();
  }

  @NonNull
  public static String randomPassword() {
    return FAKER.credentials().password(3, 12);
  }

  @NonNull
  public static String randomFullName() {
    return FAKER.name().fullName();
  }

  @NonNull
  public static String randomFirstName() {
    return FAKER.name().firstName();
  }

  @NonNull
  public static String randomLastName() {
    return FAKER.name().lastName();
  }

  @NonNull
  public static String randomTitle() {
    return FAKER.lorem().sentence();
  }

  @NonNull
  public static String randomParagraph() {
    return FAKER.lorem().paragraph();
  }

  @NonNull
  public static String randomCity() {
    return FAKER.address().city();
  }

  @NonNull
  public static String randomPhoto() {
    return FAKER.image().base64PNG();
  }
}
