package io.efremov.rococo.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@Entity
@Table(name = "museum")
public class MuseumEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false, columnDefinition = "UUID default uuid_generate_v4()")
  private UUID id;

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

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hp
        ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> oEffectiveClass = o instanceof HibernateProxy hp
        ? hp.getHibernateLazyInitializer().getPersistentClass()
        : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy hp
        ? hp.getHibernateLazyInitializer().getPersistentClass()
        : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    MuseumEntity that = (MuseumEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }
}
