package io.efremov.rococo.data;

import io.efremov.rococo.common.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "artist")
public class ArtistEntity extends BaseEntity {

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "biography", nullable = false, length = 2000)
  private String biography;

  @Column(name = "photo", nullable = false, columnDefinition = "BYTEA")
  private byte[] photo;
}
