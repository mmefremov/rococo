package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.CountryEntity;
import io.efremov.rococo.grpc.CountriesByIdRequest;
import io.efremov.rococo.grpc.CountriesResponse;
import io.efremov.rococo.grpc.CountryPageRequest;
import io.efremov.rococo.grpc.CountryPageResponse;
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
class GrpcGeoServiceTest {

  @Mock
  private CountryService countryService;

  @Mock
  private StreamObserver<CountriesResponse> responseObserver;

  @Mock
  private StreamObserver<CountryPageResponse> pageResponseObserver;

  @InjectMocks
  private GrpcGeoService grpcGeoService;

  @Test
  void getCountriesByIds_existingIds_sendsResponse() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    CountryEntity e1 = createCountry(id1, "DE", "Германия");
    CountryEntity e2 = createCountry(id2, "FR", "Франция");
    when(countryService.findAllByIds(List.of(id1, id2))).thenReturn(List.of(e1, e2));

    grpcGeoService.getCountriesByIds(
        CountriesByIdRequest.newBuilder()
            .addId(id1.toString())
            .addId(id2.toString())
            .build(),
        responseObserver
    );

    ArgumentCaptor<CountriesResponse> captor = ArgumentCaptor.forClass(CountriesResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();
    verifyNoMoreInteractions(responseObserver);

    assertThat(captor.getValue().getCountriesList()).hasSize(2);
  }

  @Test
  void getCountriesByIds_emptyRequest_sendsEmptyResponse() {
    when(countryService.findAllByIds(List.of())).thenReturn(List.of());

    grpcGeoService.getCountriesByIds(
        CountriesByIdRequest.newBuilder().build(),
        responseObserver
    );

    ArgumentCaptor<CountriesResponse> captor = ArgumentCaptor.forClass(CountriesResponse.class);
    verify(responseObserver).onNext(captor.capture());
    verify(responseObserver).onCompleted();
    assertThat(captor.getValue().getCountriesList()).isEmpty();
  }

  @Test
  void getCountriesByIds_invalidIdFormat_sendsInvalidArgumentError() {
    grpcGeoService.getCountriesByIds(
        CountriesByIdRequest.newBuilder().addId("not-a-uuid").build(),
        responseObserver
    );

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(responseObserver).onError(captor.capture());

    StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void getAllCountries_returnsPageResponse() {
    CountryEntity entity = createCountry(UUID.randomUUID(), "RU", "Россия");
    when(countryService.findAll(any())).thenReturn(new PageImpl<>(List.of(entity)));

    grpcGeoService.getAllCountries(
        CountryPageRequest.newBuilder().setPage(0).setSize(20).build(),
        pageResponseObserver
    );

    ArgumentCaptor<CountryPageResponse> captor = ArgumentCaptor.forClass(CountryPageResponse.class);
    verify(pageResponseObserver).onNext(captor.capture());
    verify(pageResponseObserver).onCompleted();
    assertThat(captor.getValue().getContentList()).hasSize(1);
    assertThat(captor.getValue().getContent(0).getName()).isEqualTo("Россия");
  }

  private CountryEntity createCountry(UUID id, String code, String name) {
    CountryEntity entity = new CountryEntity();
    entity.setId(id);
    entity.setCode(code);
    entity.setName(name);
    return entity;
  }
}
