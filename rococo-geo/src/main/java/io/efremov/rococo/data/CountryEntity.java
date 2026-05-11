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
@Table(name = "country")
public class CountryEntity extends BaseEntity {

  @Column(name = "code", length = 2)
  private String code;

  @Column(name = "name", nullable = false, unique = true)
  private String name;
}
