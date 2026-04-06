package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.PaintingEntity;
import io.efremov.rococo.data.repository.PaintingRepository;
import io.efremov.rococo.exception.PaintingNotFoundException;
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
class PaintingServiceTest {

  @Mock
  private PaintingRepository paintingRepository;

  @InjectMocks
  private PaintingService paintingService;

  @Test
  void findAll_withoutTitle_returnsAllPaintings() {
    PaintingEntity entity = createPainting("Starry Night");
    when(paintingRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(entity)));

    Page<PaintingEntity> result = paintingService.findAll(null, PageRequest.of(0, 9));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Starry Night");
    verify(paintingRepository).findAll(any(PageRequest.class));
  }

  private PaintingEntity createPainting(String title) {
    PaintingEntity entity = new PaintingEntity();
    entity.setId(UUID.randomUUID());
    entity.setTitle(title);
    entity.setArtistId(UUID.randomUUID());
    return entity;
  }

  @Test
  void findAll_withTitle_searchesByTitle() {
    PaintingEntity entity = createPainting("Starry Night");
    when(paintingRepository.findAllByTitleContainingIgnoreCase(eq("starry"), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));

    Page<PaintingEntity> result = paintingService.findAll("starry", PageRequest.of(0, 9));

    assertThat(result.getContent()).hasSize(1);
    verify(paintingRepository).findAllByTitleContainingIgnoreCase(eq("starry"), any(PageRequest.class));
  }

  @Test
  void findById_existingId_returnsPainting() {
    UUID id = UUID.randomUUID();
    PaintingEntity entity = createPainting("Mona Lisa");
    entity.setId(id);
    when(paintingRepository.findById(id)).thenReturn(Optional.of(entity));

    PaintingEntity result = paintingService.findById(id);

    assertThat(result.getTitle()).isEqualTo("Mona Lisa");
  }

  @Test
  void findById_nonExistingId_throwsNotFoundException() {
    UUID id = UUID.randomUUID();
    when(paintingRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paintingService.findById(id))
        .isInstanceOf(PaintingNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void findByArtistId_returnsArtistPaintings() {
    UUID artistId = UUID.randomUUID();
    PaintingEntity entity = createPainting("Sunflowers");
    entity.setArtistId(artistId);
    when(paintingRepository.findAllByArtistId(eq(artistId), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));

    Page<PaintingEntity> result = paintingService.findByArtistId(artistId, PageRequest.of(0, 9));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getArtistId()).isEqualTo(artistId);
  }

  @Test
  void create_withMuseum_savesPainting() {
    UUID artistId = UUID.randomUUID();
    UUID museumId = UUID.randomUUID();
    PaintingEntity saved = createPainting("The Scream");
    saved.setArtistId(artistId);
    saved.setMuseumId(museumId);
    when(paintingRepository.existsByTitleIgnoreCaseAndArtistId("The Scream", artistId)).thenReturn(false);
    when(paintingRepository.save(any(PaintingEntity.class))).thenReturn(saved);

    PaintingEntity result = paintingService.create("The Scream", "valid description text", "content", artistId,
        museumId);

    assertThat(result.getArtistId()).isEqualTo(artistId);
    assertThat(result.getMuseumId()).isEqualTo(museumId);
  }

  @Test
  void create_withoutMuseum_savesPaintingWithNullMuseumId() {
    UUID artistId = UUID.randomUUID();
    PaintingEntity saved = createPainting("Guernica");
    saved.setArtistId(artistId);
    when(paintingRepository.existsByTitleIgnoreCaseAndArtistId("Guernica", artistId)).thenReturn(false);
    when(paintingRepository.save(any(PaintingEntity.class))).thenReturn(saved);

    PaintingEntity result = paintingService.create("Guernica", "valid description text", "content", artistId, null);

    assertThat(result.getMuseumId()).isNull();
  }

  @Test
  void create_duplicateTitleAndArtist_throwsDataIntegrityViolationException() {
    UUID artistId = UUID.randomUUID();
    when(paintingRepository.existsByTitleIgnoreCaseAndArtistId("Mona Lisa", artistId)).thenReturn(true);

    assertThatThrownBy(() -> paintingService.create("Mona Lisa", "valid description text", "content", artistId, null))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("already exists")
        .hasMessageContaining("Mona Lisa");
  }

  @Test
  void encodeContent_nullContent_returnsEmptyString() {
    assertThat(PaintingService.encodeContent(null)).isEmpty();
  }

  @Test
  void create_nullTitle_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> paintingService.create(null, "valid description text", "content", UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title");
  }

  @Test
  void create_titleTooLong_throwsIllegalArgumentException() {
    String longTitle = "t".repeat(256);
    assertThatThrownBy(
        () -> paintingService.create(longTitle, "valid description text", "content", UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title");
  }

  @Test
  void create_nullDescription_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> paintingService.create("Title", null, "content", UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void create_descriptionTooLong_throwsIllegalArgumentException() {
    String longDesc = "d".repeat(1001);
    assertThatThrownBy(() -> paintingService.create("Title", longDesc, "content", UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void create_nullContent_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> paintingService.create("Title", "valid description text", null, UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("content");
  }

  @Test
  void create_nullArtistId_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> paintingService.create("Title", "valid description text", "content", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("artistId");
  }

  @Test
  void update_nullTitle_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(paintingRepository.findById(id)).thenReturn(Optional.of(createPainting("Test")));
    assertThatThrownBy(
        () -> paintingService.update(id, null, "valid description text", "content", UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("title");
  }

  @Test
  void update_descriptionTooLong_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(paintingRepository.findById(id)).thenReturn(Optional.of(createPainting("Test")));
    String longDesc = "d".repeat(1001);
    assertThatThrownBy(() -> paintingService.update(id, "Title", longDesc, "content", UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void update_nullContent_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(paintingRepository.findById(id)).thenReturn(Optional.of(createPainting("Test")));
    assertThatThrownBy(
        () -> paintingService.update(id, "Title", "valid description text", null, UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("content");
  }

  @Test
  void create_contentTooLarge_throwsIllegalArgumentException() {
    String largeContent = "a".repeat(1_048_577);
    assertThatThrownBy(
        () -> paintingService.create("Title", "valid description text", largeContent, UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("content");
  }

  @Test
  void update_contentTooLarge_throwsIllegalArgumentException() {
    UUID id = UUID.randomUUID();
    when(paintingRepository.findById(id)).thenReturn(Optional.of(createPainting("Test")));
    String largeContent = "a".repeat(1_048_577);
    assertThatThrownBy(
        () -> paintingService.update(id, "Title", "valid description text", largeContent, UUID.randomUUID(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("content");
  }

  @Test
  void findAllByIds_returnsMatchingPaintings() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    PaintingEntity e1 = createPainting("P1");
    e1.setId(id1);
    PaintingEntity e2 = createPainting("P2");
    e2.setId(id2);
    Collection<UUID> ids = List.of(id1, id2);
    when(paintingRepository.findAllById(ids)).thenReturn(List.of(e1, e2));

    List<PaintingEntity> result = paintingService.findAllByIds(ids);

    assertThat(result).hasSize(2);
  }

  @Test
  void findAllByIds_emptyInput_returnsEmpty() {
    List<PaintingEntity> result = paintingService.findAllByIds(List.of());
    assertThat(result).isEmpty();
  }
}
