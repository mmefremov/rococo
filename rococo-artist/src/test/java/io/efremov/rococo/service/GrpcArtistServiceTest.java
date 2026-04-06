package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.ArtistEntity;
import io.efremov.rococo.grpc.ArtistByIdRequest;
import io.efremov.rococo.grpc.ArtistResponse;
import io.efremov.rococo.grpc.ArtistsResponse;
import io.efremov.rococo.grpc.CreateArtistRequest;
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

@ExtendWith(MockitoExtension.class)
class GrpcArtistServiceTest {

  @Mock
  private ArtistService artistService;

  @Mock
  private StreamObserver<ArtistsResponse> artistsResponseObserver;

  @Mock
  private StreamObserver<ArtistResponse> responseObserver;

  @InjectMocks
  private GrpcArtistService grpcArtistService;

  @Test
  void getArtistById_singleExistingId_returnsSingleArtist() {
    UUID id = UUID.randomUUID();
    ArtistEntity entity = new ArtistEntity();
    entity.setId(id);
    entity.setName("Rembrandt");
    entity.setBiography("Dutch master");
    entity.setPhoto(new byte[0]);

    when(artistService.findAllByIds(List.of(id))).thenReturn(List.of(entity));

    grpcArtistService.getArtistById(
        ArtistByIdRequest.newBuilder().addId(id.toString()).build(),
        artistsResponseObserver
    );

    ArgumentCaptor<ArtistsResponse> captor = ArgumentCaptor.forClass(ArtistsResponse.class);
    verify(artistsResponseObserver).onNext(captor.capture());
    verify(artistsResponseObserver).onCompleted();
    verifyNoMoreInteractions(artistsResponseObserver);

    ArtistsResponse response = captor.getValue();
    assertThat(response.getArtistsList()).hasSize(1);
    assertThat(response.getArtists(0).getId()).isEqualTo(id.toString());
    assertThat(response.getArtists(0).getName()).isEqualTo("Rembrandt");
  }

  @Test
  void getArtistById_nonExistentId_returnsEmptyList() {
    UUID id = UUID.randomUUID();
    when(artistService.findAllByIds(List.of(id))).thenReturn(List.of());

    grpcArtistService.getArtistById(
        ArtistByIdRequest.newBuilder().addId(id.toString()).build(),
        artistsResponseObserver
    );

    ArgumentCaptor<ArtistsResponse> captor = ArgumentCaptor.forClass(ArtistsResponse.class);
    verify(artistsResponseObserver).onNext(captor.capture());
    verify(artistsResponseObserver).onCompleted();
    verifyNoMoreInteractions(artistsResponseObserver);

    assertThat(captor.getValue().getArtistsList()).isEmpty();
  }

  @Test
  void getArtistById_batchSomeFound_returnsOnlyExisting() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    ArtistEntity entity = new ArtistEntity();
    entity.setId(id1);
    entity.setName("Dali");
    entity.setBiography("Spanish surrealist");
    entity.setPhoto(new byte[0]);

    when(artistService.findAllByIds(List.of(id1, id2))).thenReturn(List.of(entity));

    grpcArtistService.getArtistById(
        ArtistByIdRequest.newBuilder().addId(id1.toString()).addId(id2.toString()).build(),
        artistsResponseObserver
    );

    ArgumentCaptor<ArtistsResponse> captor = ArgumentCaptor.forClass(ArtistsResponse.class);
    verify(artistsResponseObserver).onNext(captor.capture());
    verify(artistsResponseObserver).onCompleted();

    assertThat(captor.getValue().getArtistsList()).hasSize(1);
    assertThat(captor.getValue().getArtists(0).getName()).isEqualTo("Dali");
  }

  @Test
  void getArtistById_invalidIdFormat_sendsInvalidArgumentError() {
    grpcArtistService.getArtistById(
        ArtistByIdRequest.newBuilder().addId("not-a-uuid").build(),
        artistsResponseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(artistsResponseObserver).onError(captor.capture());
    verifyNoMoreInteractions(artistsResponseObserver);

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void createArtist_validRequest_sendsCreatedArtist() {
    ArtistEntity entity = new ArtistEntity();
    entity.setId(UUID.randomUUID());
    entity.setName("Dali");
    entity.setBiography("Spanish surrealist");
    entity.setPhoto(new byte[0]);

    when(artistService.create(eq("Dali"), eq("Spanish surrealist"), any())).thenReturn(entity);

    grpcArtistService.createArtist(
        CreateArtistRequest.newBuilder().setName("Dali").setBiography("Spanish surrealist").build(),
        responseObserver
    );

    ArgumentCaptor<ArtistResponse> captor = ArgumentCaptor.forClass(ArtistResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();

    assertThat(captor.getValue().getName()).isEqualTo("Dali");
  }
}
