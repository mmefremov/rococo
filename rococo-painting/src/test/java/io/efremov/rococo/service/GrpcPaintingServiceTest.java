package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.PaintingEntity;
import io.efremov.rococo.exception.PaintingNotFoundException;
import io.efremov.rococo.grpc.CreatePaintingRequest;
import io.efremov.rococo.grpc.PaintingByArtistRequest;
import io.efremov.rococo.grpc.PaintingByIdRequest;
import io.efremov.rococo.grpc.PaintingPageRequest;
import io.efremov.rococo.grpc.PaintingPageResponse;
import io.efremov.rococo.grpc.PaintingResponse;
import io.efremov.rococo.grpc.PaintingsByIdRequest;
import io.efremov.rococo.grpc.PaintingsResponse;
import io.efremov.rococo.grpc.UpdatePaintingRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class GrpcPaintingServiceTest {

  @Mock
  private PaintingService paintingService;

  @Mock
  private StreamObserver<PaintingResponse> responseObserver;

  @Mock
  private StreamObserver<PaintingPageResponse> pageResponseObserver;

  @Mock
  private StreamObserver<PaintingsResponse> paintingsResponseObserver;

  @InjectMocks
  private GrpcPaintingService grpcPaintingService;

  @Test
  void getPaintingById_existingId_sendsResponse() {
    UUID id = UUID.randomUUID();
    UUID artistId = UUID.randomUUID();
    PaintingEntity entity = createPainting(id, "Mona Lisa", artistId);
    when(paintingService.findById(id)).thenReturn(entity);

    grpcPaintingService.getPaintingById(
        PaintingByIdRequest.newBuilder().setId(id.toString()).build(),
        responseObserver
    );

    ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();
    verifyNoMoreInteractions(responseObserver);

    assertThat(captor.getValue().getId()).as("id").isEqualTo(id.toString());
    assertThat(captor.getValue().getTitle()).as("title").isEqualTo("Mona Lisa");
    assertThat(captor.getValue().getArtistId()).as("artist_id").isEqualTo(artistId.toString());
    assertThat(captor.getValue().hasMuseumId()).as("museum_id absent").isFalse();
  }

  @Test
  void getPaintingById_withMuseum_sendsResponseWithMuseumId() {
    UUID id = UUID.randomUUID();
    UUID artistId = UUID.randomUUID();
    UUID museumId = UUID.randomUUID();
    PaintingEntity entity = createPainting(id, "Sunflowers", artistId, museumId);
    when(paintingService.findById(id)).thenReturn(entity);

    grpcPaintingService.getPaintingById(
        PaintingByIdRequest.newBuilder().setId(id.toString()).build(),
        responseObserver
    );

    ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();

    assertThat(captor.getValue().getMuseumId()).as("museum_id").isEqualTo(museumId.toString());
  }

  @Test
  void getPaintingById_invalidIdFormat_sendsInvalidArgumentError() {
    grpcPaintingService.getPaintingById(
        PaintingByIdRequest.newBuilder().setId("not-a-uuid").build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void getPaintingById_notFound_sendsNotFoundError() {
    UUID id = UUID.randomUUID();
    when(paintingService.findById(id)).thenThrow(new PaintingNotFoundException(id));

    grpcPaintingService.getPaintingById(
        PaintingByIdRequest.newBuilder().setId(id.toString()).build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
  }

  @Test
  void getPaintingsByIds_existingIds_sendsResponse() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID artistId = UUID.randomUUID();
    PaintingEntity e1 = createPainting(id1, "P1", artistId);
    PaintingEntity e2 = createPainting(id2, "P2", artistId);
    when(paintingService.findAllByIds(List.of(id1, id2))).thenReturn(List.of(e1, e2));

    grpcPaintingService.getPaintingsByIds(
        PaintingsByIdRequest.newBuilder()
            .addId(id1.toString())
            .addId(id2.toString())
            .build(),
        paintingsResponseObserver
    );

    ArgumentCaptor<PaintingsResponse> captor = ArgumentCaptor.forClass(PaintingsResponse.class);
    verify(paintingsResponseObserver).onNext(captor.capture());
    verify(paintingsResponseObserver).onCompleted();
    assertThat(captor.getValue().getPaintingsList()).hasSize(2);
  }

  @Test
  void getPaintingsByIds_emptyRequest_sendsEmptyResponse() {
    when(paintingService.findAllByIds(List.of())).thenReturn(List.of());

    grpcPaintingService.getPaintingsByIds(
        PaintingsByIdRequest.newBuilder().build(),
        paintingsResponseObserver
    );

    ArgumentCaptor<PaintingsResponse> captor = ArgumentCaptor.forClass(PaintingsResponse.class);
    verify(paintingsResponseObserver).onNext(captor.capture());
    verify(paintingsResponseObserver).onCompleted();
    assertThat(captor.getValue().getPaintingsList()).isEmpty();
  }

  @Test
  void getPaintingsByIds_invalidIdFormat_sendsInvalidArgumentError() {
    grpcPaintingService.getPaintingsByIds(
        PaintingsByIdRequest.newBuilder().addId("not-a-uuid").build(),
        paintingsResponseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(paintingsResponseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void getPaintingsByArtist_invalidArtistId_sendsInvalidArgumentError() {
    grpcPaintingService.getPaintingsByArtist(
        PaintingByArtistRequest.newBuilder().setArtistId("not-a-uuid").setPage(0).setSize(9).build(),
        pageResponseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(pageResponseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void getPaintingsByArtist_validArtistId_sendsPageResponse() {
    UUID artistId = UUID.randomUUID();
    PaintingEntity entity = createPainting(UUID.randomUUID(), "Sunflowers", artistId);
    when(paintingService.findByArtistId(eq(artistId), any())).thenReturn(new PageImpl<>(List.of(entity)));

    grpcPaintingService.getPaintingsByArtist(
        PaintingByArtistRequest.newBuilder().setArtistId(artistId.toString()).setPage(0).setSize(9).build(),
        pageResponseObserver
    );

    ArgumentCaptor<PaintingPageResponse> captor = ArgumentCaptor.forClass(PaintingPageResponse.class);
    verify(pageResponseObserver).onNext(captor.capture());
    verify(pageResponseObserver).onCompleted();
    verifyNoMoreInteractions(pageResponseObserver);

    assertThat(captor.getValue().getContentList()).hasSize(1);
    assertThat(captor.getValue().getContent(0).getArtistId()).as("artist_id").isEqualTo(artistId.toString());
  }

  @Test
  void getAllPaintings_multipleArtists_returnsAllWithArtistIds() {
    UUID artist1 = UUID.randomUUID();
    UUID artist2 = UUID.randomUUID();
    PaintingEntity p1 = createPainting(UUID.randomUUID(), "P1", artist1);
    PaintingEntity p2 = createPainting(UUID.randomUUID(), "P2", artist2);
    PaintingEntity p3 = createPainting(UUID.randomUUID(), "P3", artist1);

    when(paintingService.findAll(isNull(), any())).thenReturn(new PageImpl<>(List.of(p1, p2, p3)));

    grpcPaintingService.getAllPaintings(
        PaintingPageRequest.newBuilder().setPage(0).setSize(9).build(),
        pageResponseObserver
    );

    ArgumentCaptor<PaintingPageResponse> captor = ArgumentCaptor.forClass(PaintingPageResponse.class);
    verify(pageResponseObserver).onNext(captor.capture());
    verify(pageResponseObserver).onCompleted();
    assertThat(captor.getValue().getContentList()).hasSize(3);
  }

  @Test
  void getAllPaintings_emptyPage_doesNotCallPaintingServiceFindAll() {
    when(paintingService.findAll(isNull(), any())).thenReturn(new PageImpl<>(List.of()));

    grpcPaintingService.getAllPaintings(
        PaintingPageRequest.newBuilder().setPage(0).setSize(9).build(),
        pageResponseObserver
    );

    ArgumentCaptor<PaintingPageResponse> captor = ArgumentCaptor.forClass(PaintingPageResponse.class);
    verify(pageResponseObserver).onNext(captor.capture());
    verify(pageResponseObserver).onCompleted();
    assertThat(captor.getValue().getContentList()).isEmpty();
  }

  @Test
  void createPainting_invalidArtistId_sendsInvalidArgumentError() {
    grpcPaintingService.createPainting(
        CreatePaintingRequest.newBuilder()
            .setTitle("Title")
            .setDescription("desc")
            .setContent("content")
            .setArtistId("not-a-uuid")
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void createPainting_validRequest_sendsResponseWithArtistId() {
    UUID artistId = UUID.randomUUID();
    UUID paintingId = UUID.randomUUID();
    when(paintingService.create(eq("Title"), eq("desc"), eq("content"), eq(artistId), isNull()))
        .thenReturn(createPainting(paintingId, "Title", artistId));

    grpcPaintingService.createPainting(
        CreatePaintingRequest.newBuilder()
            .setTitle("Title")
            .setDescription("desc")
            .setContent("content")
            .setArtistId(artistId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();

    assertThat(captor.getValue().getArtistId()).as("artist_id").isEqualTo(artistId.toString());
    assertThat(captor.getValue().hasMuseumId()).as("no museum_id").isFalse();
  }

  @Test
  void createPainting_withMuseum_sendsResponseWithMuseumId() {
    UUID artistId = UUID.randomUUID();
    UUID museumId = UUID.randomUUID();
    UUID paintingId = UUID.randomUUID();
    when(paintingService.create(eq("Title"), eq("desc"), eq("content"), eq(artistId), eq(museumId)))
        .thenReturn(createPainting(paintingId, "Title", artistId, museumId));

    grpcPaintingService.createPainting(
        CreatePaintingRequest.newBuilder()
            .setTitle("Title")
            .setDescription("desc")
            .setContent("content")
            .setArtistId(artistId.toString())
            .setMuseumId(museumId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();

    assertThat(captor.getValue().getMuseumId()).as("museum_id").isEqualTo(museumId.toString());
  }

  @Test
  void createPainting_blankTitle_sendsInvalidArgumentError() {
    UUID artistId = UUID.randomUUID();
    when(paintingService.create(eq(""), any(), any(), eq(artistId), isNull()))
        .thenThrow(new IllegalArgumentException("title: must not be blank"));

    grpcPaintingService.createPainting(
        CreatePaintingRequest.newBuilder()
            .setTitle("")
            .setDescription("desc")
            .setContent("content")
            .setArtistId(artistId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    assertThat(ex.getStatus().getDescription()).contains("title");
  }

  @Test
  void updatePainting_invalidIdFormat_sendsInvalidArgumentError() {
    grpcPaintingService.updatePainting(
        UpdatePaintingRequest.newBuilder()
            .setId("not-a-uuid")
            .setTitle("Title")
            .setDescription("desc")
            .setContent("content")
            .setArtistId(UUID.randomUUID().toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void updatePainting_notFound_sendsNotFoundError() {
    UUID id = UUID.randomUUID();
    UUID artistId = UUID.randomUUID();
    when(paintingService.update(eq(id), any(), any(), any(), eq(artistId), isNull()))
        .thenThrow(new PaintingNotFoundException(id));

    grpcPaintingService.updatePainting(
        UpdatePaintingRequest.newBuilder()
            .setId(id.toString())
            .setTitle("Title")
            .setDescription("desc")
            .setContent("content")
            .setArtistId(artistId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
  }

  @Test
  void updatePainting_validRequest_sendsResponseWithArtistId() {
    UUID id = UUID.randomUUID();
    UUID artistId = UUID.randomUUID();
    when(paintingService.update(eq(id), eq("Title"), eq("desc"), eq("content"), eq(artistId), isNull()))
        .thenReturn(createPainting(id, "Title", artistId));

    grpcPaintingService.updatePainting(
        UpdatePaintingRequest.newBuilder()
            .setId(id.toString())
            .setTitle("Title")
            .setDescription("desc")
            .setContent("content")
            .setArtistId(artistId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();

    assertThat(captor.getValue().getArtistId()).as("artist_id").isEqualTo(artistId.toString());
  }

  private PaintingEntity createPainting(UUID id, String title, UUID artistId) {
    PaintingEntity entity = new PaintingEntity();
    entity.setId(id);
    entity.setTitle(title);
    entity.setDescription("description");
    entity.setArtistId(artistId);
    entity.setContent(new byte[0]);
    return entity;
  }

  private PaintingEntity createPainting(UUID id, String title, UUID artistId, UUID museumId) {
    PaintingEntity entity = createPainting(id, title, artistId);
    entity.setMuseumId(museumId);
    return entity;
  }
}
