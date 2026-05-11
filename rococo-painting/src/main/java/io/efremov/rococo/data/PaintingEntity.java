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
@Table(name = "painting")
public class PaintingEntity extends BaseEntity {

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false, length = 1000)
  private String description;

  @Column(name = "artist_id", nullable = false)
  private UUID artistId;

  @Column(name = "museum_id")
  private UUID museumId;

  @Column(name = "content", nullable = false, columnDefinition = "BYTEA")
  private byte[] content;
}
