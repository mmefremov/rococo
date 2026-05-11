package io.efremov.rococo.common.grpc;

import io.efremov.rococo.common.exception.EntityNotFoundException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
public final class GrpcServiceTemplate {

  private GrpcServiceTemplate() {}

  public static <T> void unary(@NonNull StreamObserver<T> observer, @NonNull Supplier<T> action) {
    try {
      T result = action.get();
      observer.onNext(result);
      observer.onCompleted();
    } catch (EntityNotFoundException e) {
      observer.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (DataIntegrityViolationException e) {
      observer.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (IllegalArgumentException e) {
      observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("Unexpected gRPC error", e);
      observer.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }
}
