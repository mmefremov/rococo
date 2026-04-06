package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.MuseumEntity;
import io.efremov.rococo.data.repository.MuseumRepository;
import io.efremov.rococo.exception.MuseumNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MuseumServiceTest {

  @Mock
  private MuseumRepository museumRepository;

  @InjectMocks
  private MuseumService museumService;

  @Test
  void findAll_withoutTitle_returnsAllMuseums() {
    MuseumEntity entity = createMuseum("Hermitage");
    when(museumRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(entity)));

    Page<MuseumEntity> result = museumService.findAll(null, PageRequest.of(0, 4));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Hermitage");
    verify(museumRepository).findAll(any(PageRequest.class));
  }

  private MuseumEntity createMuseum(String title) {
    MuseumEntity entity = new MuseumEntity();
    entity.setId(UUID.randomUUID());
    entity.setTitle(title);
    entity.setCountryId(UUID.randomUUID());
    return entity;
  }

  @Test
  void findAll_withTitle_searchesByTitle() {
    MuseumEntity entity = createMuseum("Hermitage");
    when(museumRepository.findAllByTitleContainingIgnoreCase(eq("herm"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));

    Page<MuseumEntity> result = museumService.findAll("herm", PageRequest.of(0, 4));

    assertThat(result.getContent()).hasSize(1);
    verify(museumRepository).findAllByTitleContainingIgnoreCase(eq("herm"), any(PageRequest.class));
  }

  @Test
  void findById_existingId_returnsMuseum() {
    UUID id = UUID.randomUUID();
    MuseumEntity entity = createMuseum("Louvre");
    entity.setId(id);
    when(museumRepository.findById(id)).thenReturn(Optional.of(entity));

    MuseumEntity result = museumService.findById(id);

    assertThat(result.getTitle()).isEqualTo("Louvre");
  }

  @Test
  void findById_nonExistingId_throwsNotFoundException() {
    UUID id = UUID.randomUUID();
    when(museumRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> museumService.findById(id))
        .isInstanceOf(MuseumNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void create_validData_savesMuseum() {
    UUID countryId = UUID.randomUUID();
    MuseumEntity saved = createMuseum("Prado");
    saved.setCountryId(countryId);
    when(museumRepository.existsByTitleIgnoreCaseAndCityIgnoreCase("Prado", "Madrid")).thenReturn(false);
    when(museumRepository.save(any(MuseumEntity.class))).thenReturn(saved);

    MuseumEntity result = museumService.create("Prado", "Spanish museum", "photo", "Madrid", countryId);

    assertThat(result.getTitle()).isEqualTo("Prado");
    verify(museumRepository).save(any(MuseumEntity.class));
  }

  @Test
  void create_duplicateTitleAndCity_throwsDataIntegrityViolationException() {
    UUID countryId = UUID.randomUUID();
    when(museumRepository.existsByTitleIgnoreCaseAndCityIgnoreCase("Hermitage", "St. Petersburg")).thenReturn(true);

    assertThatThrownBy(
        () -> museumService.create("Hermitage", "valid description text", "photo", "St. Petersburg", countryId))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("already exists")
        .hasMessageContaining("Hermitage")
        .hasMessageContaining("St. Petersburg");
  }

  @Test
  void update_existingMuseum_updatesFields() {
    UUID id = UUID.randomUUID();
    UUID countryId = UUID.randomUUID();
    MuseumEntity existing = createMuseum("Old Title");
    existing.setId(id);
    MuseumEntity updated = createMuseum("New Title");
    updated.setId(id);

    when(museumRepository.findById(id)).thenReturn(Optional.of(existing));
    when(museumRepository.save(any(MuseumEntity.class))).thenReturn(updated);

    MuseumEntity result = museumService.update(id, "New Title", "Description", "photo", "City", countryId);

    assertThat(result.getTitle()).isEqualTo("New Title");
  }

  @Test
  void create_nullTitle_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> museumService.create(null, "valid description text", "photo", "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title");
  }

  @Test
  void create_titleTooLong_throwsIllegalArgumentException() {
    String longTitle = "t".repeat(256);
    assertThatThrownBy(
        () -> museumService.create(longTitle, "valid description text", "photo", "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title");
  }

  @Test
  void create_nullDescription_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> museumService.create("Title", null, "photo", "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void create_descriptionTooLong_throwsIllegalArgumentException() {
    String longDesc = "d".repeat(1001);
    assertThatThrownBy(() -> museumService.create("Title", longDesc, "photo", "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void create_nullCity_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> museumService.create("Title", "valid description text", "photo", null, UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("city");
  }

  @Test
  void create_nullPhoto_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> museumService.create("Title", "valid description text", null, "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void create_nullCountryId_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> museumService.create("Title", "valid description text", "photo", "City", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("countryId");
  }

  @Test
  void update_nullTitle_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(museumRepository.findById(id)).thenReturn(Optional.of(createMuseum("Test")));
    assertThatThrownBy(
        () -> museumService.update(id, null, "valid description text", "photo", "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title");
  }

  @Test
  void update_descriptionTooLong_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(museumRepository.findById(id)).thenReturn(Optional.of(createMuseum("Test")));
    String longDesc = "d".repeat(1001);
    assertThatThrownBy(() -> museumService.update(id, "Title", longDesc, "photo", "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void update_nullPhoto_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(museumRepository.findById(id)).thenReturn(Optional.of(createMuseum("Test")));
    assertThatThrownBy(
        () -> museumService.update(id, "Title", "valid description text", null, "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void create_photoTooLarge_throwsIllegalArgumentException() {
    String largePhoto = "a".repeat(1_048_577);
    assertThatThrownBy(
        () -> museumService.create("Title", "valid description text", largePhoto, "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void update_photoTooLarge_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(museumRepository.findById(id)).thenReturn(Optional.of(createMuseum("Test")));
    String largePhoto = "a".repeat(1_048_577);
    assertThatThrownBy(
        () -> museumService.update(id, "Title", "valid description text", largePhoto, "City", UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void findAllByIds_returnsMatchingMuseums() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    MuseumEntity e1 = createMuseum("Hermitage");
    e1.setId(id1);
    MuseumEntity e2 = createMuseum("Louvre");
    e2.setId(id2);
    Collection<UUID> ids = List.of(id1, id2);
    when(museumRepository.findAllById(ids)).thenReturn(List.of(e1, e2));

    List<MuseumEntity> result = museumService.findAllByIds(ids);

    assertThat(result).hasSize(2);
  }

  @Test
  void findAllByIds_emptyInput_returnsEmpty() {
    List<MuseumEntity> result = museumService.findAllByIds(List.of());
    assertThat(result).isEmpty();
  }
}
