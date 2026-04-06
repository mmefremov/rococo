package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.ArtistEntity;
import io.efremov.rococo.data.repository.ArtistRepository;
import io.efremov.rococo.exception.ArtistNotFoundException;
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
class ArtistServiceTest {

  @Mock
  private ArtistRepository artistRepository;

  @InjectMocks
  private ArtistService artistService;

  @Test
  void findAll_withoutName_returnsAllArtists() {
    ArtistEntity entity = createArtistEntity("Monet");
    Page<ArtistEntity> page = new PageImpl<>(List.of(entity));
    when(artistRepository.findAll(any(PageRequest.class))).thenReturn(page);

    Page<ArtistEntity> result = artistService.findAll(null, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Monet");
    verify(artistRepository).findAll(any(PageRequest.class));
    verifyNoMoreInteractions(artistRepository);
  }

  private ArtistEntity createArtistEntity(String name) {
    ArtistEntity entity = new ArtistEntity();
    entity.setId(UUID.randomUUID());
    entity.setName(name);
    entity.setBiography("Some biography");
    return entity;
  }

  @Test
  void findAll_withName_searchesByName() {
    ArtistEntity entity = createArtistEntity("Monet");
    Page<ArtistEntity> page = new PageImpl<>(List.of(entity));
    when(artistRepository.findAllByNameContainingIgnoreCase(eq("mon"), any(PageRequest.class))).thenReturn(page);

    Page<ArtistEntity> result = artistService.findAll("mon", PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    verify(artistRepository).findAllByNameContainingIgnoreCase(eq("mon"), any(PageRequest.class));
  }

  @Test
  void findById_existingId_returnsArtist() {
    UUID id = UUID.randomUUID();
    ArtistEntity entity = createArtistEntity("Van Gogh");
    entity.setId(id);
    when(artistRepository.findById(id)).thenReturn(Optional.of(entity));

    ArtistEntity result = artistService.findById(id);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo("Van Gogh");
  }

  @Test
  void findById_nonExistingId_throwsNotFoundException() {
    UUID id = UUID.randomUUID();
    when(artistRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> artistService.findById(id))
        .isInstanceOf(ArtistNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void create_validData_savesAndReturnsArtist() {
    ArtistEntity saved = createArtistEntity("Picasso");
    when(artistRepository.existsByNameIgnoreCase("Picasso")).thenReturn(false);
    when(artistRepository.save(any(ArtistEntity.class))).thenReturn(saved);

    ArtistEntity result = artistService.create("Picasso", "Biography text", "photo");

    assertThat(result.getName()).isEqualTo("Picasso");
    verify(artistRepository).save(any(ArtistEntity.class));
  }

  @Test
  void create_duplicateName_throwsDataIntegrityViolationException() {
    when(artistRepository.existsByNameIgnoreCase("Monet")).thenReturn(true);

    assertThatThrownBy(() -> artistService.create("Monet", "Biography text", "photo"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("already exists")
        .hasMessageContaining("Monet");
  }

  @Test
  void update_existingArtist_updatesFields() {
    UUID id = UUID.randomUUID();
    ArtistEntity existing = createArtistEntity("Old Name");
    existing.setId(id);
    ArtistEntity updated = createArtistEntity("New Name");
    updated.setId(id);

    when(artistRepository.findById(id)).thenReturn(Optional.of(existing));
    when(artistRepository.save(any(ArtistEntity.class))).thenReturn(updated);

    ArtistEntity result = artistService.update(id, "New Name", "New biography", "photo");

    assertThat(result.getName()).isEqualTo("New Name");
    verify(artistRepository).save(any(ArtistEntity.class));
  }

  @Test
  void update_nonExistingArtist_throwsNotFoundException() {
    UUID id = UUID.randomUUID();
    when(artistRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> artistService.update(id, "Name", "Bio", "photo"))
        .isInstanceOf(ArtistNotFoundException.class);
  }

  @Test
  void create_nullName_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> artistService.create(null, "Biography text", "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void create_blankName_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> artistService.create("   ", "Biography text", "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void create_nameTooLong_throwsIllegalArgumentException() {
    String longName = "a".repeat(256);
    assertThatThrownBy(() -> artistService.create(longName, "Biography text", "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void create_nullBiography_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> artistService.create("Monet", null, "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("biography");
  }

  @Test
  void create_biographyTooLong_throwsIllegalArgumentException() {
    String longBio = "b".repeat(2001);
    assertThatThrownBy(() -> artistService.create("Monet", longBio, "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("biography");
  }

  @Test
  void create_nullPhoto_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> artistService.create("Monet", "Biography text", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void update_nullName_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    mockFindById(id);
    assertThatThrownBy(() -> artistService.update(id, null, "Biography text", "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void update_nameTooLong_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    mockFindById(id);
    String longName = "a".repeat(256);
    assertThatThrownBy(() -> artistService.update(id, longName, "Biography text", "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void update_biographyTooLong_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    mockFindById(id);
    String longBio = "b".repeat(2001);
    assertThatThrownBy(() -> artistService.update(id, "Monet", longBio, "photo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("biography");
  }

  @Test
  void update_nullPhoto_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    mockFindById(id);
    assertThatThrownBy(() -> artistService.update(id, "Monet", "Biography text", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void create_photoTooLarge_throwsIllegalArgumentException() {
    String largePhoto = "a".repeat(1_048_577);
    assertThatThrownBy(() -> artistService.create("Monet", "Biography text", largePhoto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  @Test
  void update_photoTooLarge_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    mockFindById(id);
    String largePhoto = "a".repeat(1_048_577);
    assertThatThrownBy(() -> artistService.update(id, "Monet", "Biography text", largePhoto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("photo");
  }

  private void mockFindById(UUID id) {
    ArtistEntity entity = createArtistEntity("Existing");
    entity.setId(id);
    when(artistRepository.findById(id)).thenReturn(Optional.of(entity));
  }
}
