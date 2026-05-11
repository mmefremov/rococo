package io.efremov.rococo.data;

import io.efremov.rococo.common.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "museum")
public class MuseumEntity extends BaseEntity {

  @Column(name = "title", nullable = false, unique = true)
  private String title;

  @Column(name = "description", nullable = false, length = 1000)
  private String description;

  @Column(name = "city", nullable = false)
  private String city;

  @Column(name = "photo", nullable = false, columnDefinition = "BYTEA")
  private byte[] photo;

  @Column(name = "country_id", nullable = false)
  private UUID countryId;
}
