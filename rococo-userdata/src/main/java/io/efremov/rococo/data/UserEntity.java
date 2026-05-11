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
@Table(name = "\"user\"")
public class UserEntity extends BaseEntity {

  @Column(name = "user_name", nullable = false, unique = true)
  private String userName;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "avatar", columnDefinition = "BYTEA")
  private byte[] avatar;
}
