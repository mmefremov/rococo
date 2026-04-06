package io.efremov.rococo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RococoGeoApplication {

  public static void main(String[] args) {
    SpringApplication.run(RococoGeoApplication.class, args);
  }
}
