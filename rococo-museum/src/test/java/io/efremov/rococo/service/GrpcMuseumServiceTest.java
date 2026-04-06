package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.MuseumEntity;
import io.efremov.rococo.exception.MuseumNotFoundException;
import io.efremov.rococo.grpc.CreateMuseumRequest;
import io.efremov.rococo.grpc.MuseumByIdRequest;
import io.efremov.rococo.grpc.MuseumPageRequest;
import io.efremov.rococo.grpc.MuseumPageResponse;
import io.efremov.rococo.grpc.MuseumResponse;
import io.efremov.rococo.grpc.MuseumsByIdRequest;
import io.efremov.rococo.grpc.MuseumsResponse;
import io.efremov.rococo.grpc.UpdateMuseumRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class GrpcMuseumServiceTest {

  @Mock
  private MuseumService museumService;

  @Mock
  private StreamObserver<MuseumResponse> responseObserver;

  @Mock
  private StreamObserver<MuseumsResponse> museumsResponseObserver;

  @Mock
  private StreamObserver<MuseumPageResponse> pageResponseObserver;

  @InjectMocks
  private GrpcMuseumService grpcMuseumService;

  @Test
  @DisplayName("getMuseumById: existing id → sends flat response with city and country_id")
  void getMuseumById_existingId_sendsResponse() {
    UUID id = UUID.randomUUID();
    UUID countryId = UUID.randomUUID();
    MuseumEntity entity = createMuseum(id, "Hermitage", "Saint Petersburg", countryId);
    when(museumService.findById(id)).thenReturn(entity);

    grpcMuseumService.getMuseumById(
        MuseumByIdRequest.newBuilder().setId(id.toString()).build(),
        responseObserver
    );

    ArgumentCaptor<MuseumResponse> captor = ArgumentCaptor.forClass(MuseumResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();
    verifyNoMoreInteractions(responseObserver);

    assertThat(captor.getValue().getId()).as("id").isEqualTo(id.toString());
    assertThat(captor.getValue().getTitle()).as("title").isEqualTo("Hermitage");
    assertThat(captor.getValue().getCity()).as("city").isEqualTo("Saint Petersburg");
    assertThat(captor.getValue().getCountryId()).as("country_id").isEqualTo(countryId.toString());
  }

  @Test
  @DisplayName("getMuseumById: invalid UUID → INVALID_ARGUMENT")
  void getMuseumById_invalidIdFormat_sendsInvalidArgumentError() {
    grpcMuseumService.getMuseumById(
        MuseumByIdRequest.newBuilder().setId("not-a-uuid").build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  @DisplayName("getMuseumById: not found → NOT_FOUND")
  void getMuseumById_notFound_sendsNotFoundError() {
    UUID id = UUID.randomUUID();
    when(museumService.findById(id)).thenThrow(new MuseumNotFoundException(id));

    grpcMuseumService.getMuseumById(
        MuseumByIdRequest.newBuilder().setId(id.toString()).build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.NOT_FOUND);
  }

  @Test
  @DisplayName("getMuseumsByIds: existing ids → sends batch response")
  void getMuseumsByIds_existingIds_sendsResponse() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID countryId = UUID.randomUUID();
    MuseumEntity e1 = createMuseum(id1, "Hermitage", "Saint Petersburg", countryId);
    MuseumEntity e2 = createMuseum(id2, "Louvre", "Paris", countryId);
    when(museumService.findAllByIds(List.of(id1, id2))).thenReturn(List.of(e1, e2));

    grpcMuseumService.getMuseumsByIds(
        MuseumsByIdRequest.newBuilder()
            .addId(id1.toString())
            .addId(id2.toString())
            .build(),
        museumsResponseObserver
    );

    ArgumentCaptor<MuseumsResponse> captor = ArgumentCaptor.forClass(MuseumsResponse.class);
    verify(museumsResponseObserver).onNext(captor.capture());
    verify(museumsResponseObserver).onCompleted();
    assertThat(captor.getValue().getMuseumsList()).hasSize(2);
  }

  @Test
  @DisplayName("getMuseumsByIds: empty request → sends empty response")
  void getMuseumsByIds_emptyRequest_sendsEmptyResponse() {
    when(museumService.findAllByIds(List.of())).thenReturn(List.of());

    grpcMuseumService.getMuseumsByIds(
        MuseumsByIdRequest.newBuilder().build(),
        museumsResponseObserver
    );

    ArgumentCaptor<MuseumsResponse> captor = ArgumentCaptor.forClass(MuseumsResponse.class);
    verify(museumsResponseObserver).onNext(captor.capture());
    verify(museumsResponseObserver).onCompleted();
    assertThat(captor.getValue().getMuseumsList()).isEmpty();
  }

  @Test
  @DisplayName("getMuseumsByIds: invalid UUID → INVALID_ARGUMENT")
  void getMuseumsByIds_invalidIdFormat_sendsInvalidArgumentError() {
    grpcMuseumService.getMuseumsByIds(
        MuseumsByIdRequest.newBuilder().addId("not-a-uuid").build(),
        museumsResponseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(museumsResponseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  @DisplayName("createMuseum: valid request → sends flat museum response")
  void createMuseum_validRequest_sendsCreatedMuseum() {
    UUID countryId = UUID.randomUUID();
    MuseumEntity entity = createMuseum(UUID.randomUUID(), "Louvre", "Paris", countryId);
    when(museumService.create(any(), any(), any(), any(), any())).thenReturn(entity);

    grpcMuseumService.createMuseum(
        CreateMuseumRequest.newBuilder()
            .setTitle("Louvre")
            .setDescription("French museum")
            .setPhoto("photo")
            .setCity("Paris")
            .setCountryId(countryId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<MuseumResponse> captor = ArgumentCaptor.forClass(MuseumResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();

    assertThat(captor.getValue().getTitle()).as("title").isEqualTo("Louvre");
    assertThat(captor.getValue().getCity()).as("city").isEqualTo("Paris");
    assertThat(captor.getValue().getCountryId()).as("country_id").isEqualTo(countryId.toString());
  }

  @Test
  @DisplayName("createMuseum: invalid countryId UUID → INVALID_ARGUMENT")
  void createMuseum_invalidCountryId_sendsInvalidArgumentError() {
    grpcMuseumService.createMuseum(
        CreateMuseumRequest.newBuilder()
            .setTitle("Museum")
            .setDescription("desc")
            .setPhoto("photo")
            .setCity("City")
            .setCountryId("not-a-uuid")
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  @DisplayName("createMuseum: blank title → INVALID_ARGUMENT")
  void createMuseum_blankTitle_sendsInvalidArgumentError() {
    UUID countryId = UUID.randomUUID();
    when(museumService.create(eq(""), any(), any(), any(), any()))
        .thenThrow(new IllegalArgumentException("title: must not be blank"));

    grpcMuseumService.createMuseum(
        CreateMuseumRequest.newBuilder()
            .setTitle("")
            .setDescription("desc")
            .setPhoto("photo")
            .setCity("City")
            .setCountryId(countryId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.INVALID_ARGUMENT);
    assertThat(ex.getStatus().getDescription()).as("description").contains("title");
  }

  @Test
  @DisplayName("updateMuseum: invalid id UUID → INVALID_ARGUMENT")
  void updateMuseum_invalidIdFormat_sendsInvalidArgumentError() {
    grpcMuseumService.updateMuseum(
        UpdateMuseumRequest.newBuilder()
            .setId("not-a-uuid")
            .setTitle("Title")
            .setDescription("desc")
            .setPhoto("photo")
            .setCity("City")
            .setCountryId(UUID.randomUUID().toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  @DisplayName("updateMuseum: museum not found → NOT_FOUND")
  void updateMuseum_notFound_sendsNotFoundError() {
    UUID id = UUID.randomUUID();
    UUID countryId = UUID.randomUUID();
    when(museumService.update(eq(id), any(), any(), any(), any(), any()))
        .thenThrow(new MuseumNotFoundException(id));

    grpcMuseumService.updateMuseum(
        UpdateMuseumRequest.newBuilder()
            .setId(id.toString())
            .setTitle("Title")
            .setDescription("desc")
            .setPhoto("photo")
            .setCity("City")
            .setCountryId(countryId.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).as("status code").isEqualTo(Status.Code.NOT_FOUND);
  }

  @Test
  @DisplayName("getAllMuseums: returns page response")
  void getAllMuseums_returnsPageResponse() {
    UUID countryId = UUID.randomUUID();
    MuseumEntity entity = createMuseum(UUID.randomUUID(), "Hermitage", "Saint Petersburg", countryId);
    when(museumService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));

    grpcMuseumService.getAllMuseums(
        MuseumPageRequest.newBuilder().setPage(0).setSize(9).build(),
        pageResponseObserver
    );

    ArgumentCaptor<MuseumPageResponse> captor = ArgumentCaptor.forClass(MuseumPageResponse.class);
    verify(pageResponseObserver).onNext(captor.capture());
    verify(pageResponseObserver).onCompleted();

    assertThat(captor.getValue().getContentList()).hasSize(1);
    assertThat(captor.getValue().getContent(0).getTitle()).isEqualTo("Hermitage");
    assertThat(captor.getValue().getContent(0).getCountryId()).as("country_id").isEqualTo(countryId.toString());
  }

  private MuseumEntity createMuseum(UUID id, String title, String city, UUID countryId) {
    MuseumEntity entity = new MuseumEntity();
    entity.setId(id);
    entity.setTitle(title);
    entity.setDescription("description");
    entity.setCity(city);
    entity.setCountryId(countryId);
    entity.setPhoto(new byte[0]);
    return entity;
  }
}
