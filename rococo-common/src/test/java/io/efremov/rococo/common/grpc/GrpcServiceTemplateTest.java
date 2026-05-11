package io.efremov.rococo.common.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.efremov.rococo.common.exception.EntityNotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class GrpcServiceTemplateTest {

  @Mock
  private StreamObserver<String> observer;

  @Test
  void unary_normalFlow_callsOnNextAndOnCompleted() {
    GrpcServiceTemplate.unary(observer, () -> "result");

    verify(observer).onNext("result");
    verify(observer).onCompleted();
    verifyNoMoreInteractions(observer);
  }

  @Test
  void unary_entityNotFoundException_callsOnErrorWithNotFound() {
    UUID id = UUID.randomUUID();
    EntityNotFoundException cause = new EntityNotFoundException("Artist", id);

    GrpcServiceTemplate.unary(observer, () -> { throw cause; });

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(observer).onError(captor.capture());
    verifyNoMoreInteractions(observer);

    StatusRuntimeException thrown = (StatusRuntimeException) captor.getValue();
    assertThat(thrown.getStatus().getCode())
        .as("status code")
        .isEqualTo(Status.Code.NOT_FOUND);
    assertThat(thrown.getStatus().getDescription())
        .as("status description")
        .isEqualTo(cause.getMessage());
  }

  @Test
  void unary_dataIntegrityViolationException_callsOnErrorWithAlreadyExists() {
    DataIntegrityViolationException cause =
        new DataIntegrityViolationException("Artist name already taken: Picasso");

    GrpcServiceTemplate.unary(observer, () -> { throw cause; });

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(observer).onError(captor.capture());
    verifyNoMoreInteractions(observer);

    StatusRuntimeException thrown = (StatusRuntimeException) captor.getValue();
    assertThat(thrown.getStatus().getCode())
        .as("status code")
        .isEqualTo(Status.Code.ALREADY_EXISTS);
    assertThat(thrown.getStatus().getDescription())
        .as("status description")
        .isEqualTo("Artist name already taken: Picasso");
  }

  @Test
  void unary_illegalArgumentException_callsOnErrorWithInvalidArgument() {
    IllegalArgumentException cause = new IllegalArgumentException("name: must not be blank");

    GrpcServiceTemplate.unary(observer, () -> { throw cause; });

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(observer).onError(captor.capture());
    verifyNoMoreInteractions(observer);

    StatusRuntimeException thrown = (StatusRuntimeException) captor.getValue();
    assertThat(thrown.getStatus().getCode())
        .as("status code")
        .isEqualTo(Status.Code.INVALID_ARGUMENT);
    assertThat(thrown.getStatus().getDescription())
        .as("status description")
        .isEqualTo("name: must not be blank");
  }

  @Test
  void unary_genericException_callsOnErrorWithInternal() {
    RuntimeException cause = new RuntimeException("boom");

    GrpcServiceTemplate.unary(observer, () -> { throw cause; });

    ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
    verify(observer).onError(captor.capture());
    verifyNoMoreInteractions(observer);

    StatusRuntimeException thrown = (StatusRuntimeException) captor.getValue();
    assertThat(thrown.getStatus().getCode())
        .as("status code")
        .isEqualTo(Status.Code.INTERNAL);
    assertThat(thrown.getStatus().getDescription())
        .as("status description")
        .isEqualTo("Internal server error");
  }
}
