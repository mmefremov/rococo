package io.efremov.rococo.test.api.grpc;

import io.efremov.rococo.api.core.GrpcConsoleInterceptor;
import io.efremov.rococo.config.Config;
import io.efremov.rococo.grpc.ArtistServiceGrpc;
import io.efremov.rococo.grpc.ArtistServiceGrpc.ArtistServiceBlockingStub;
import io.efremov.rococo.grpc.GeoServiceGrpc;
import io.efremov.rococo.grpc.GeoServiceGrpc.GeoServiceBlockingStub;
import io.efremov.rococo.grpc.MuseumServiceGrpc;
import io.efremov.rococo.grpc.MuseumServiceGrpc.MuseumServiceBlockingStub;
import io.efremov.rococo.grpc.PaintingServiceGrpc;
import io.efremov.rococo.grpc.PaintingServiceGrpc.PaintingServiceBlockingStub;
import io.efremov.rococo.grpc.UserdataServiceGrpc;
import io.efremov.rococo.jupiter.annotation.meta.GrpcTest;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

@GrpcTest
abstract class BaseGrpcTest {

  static final Config CFG = Config.getInstance();

  private static final Channel ARTIST_CHANNEL = ManagedChannelBuilder
      .forAddress(CFG.artistUrl(), CFG.artistPort())
      .intercept(new GrpcConsoleInterceptor())
      .usePlaintext()
      .build();

  private static final Channel GEO_CHANNEL = ManagedChannelBuilder
      .forAddress(CFG.geoUrl(), CFG.geoPort())
      .intercept(new GrpcConsoleInterceptor())
      .usePlaintext()
      .build();

  private static final Channel MUSEUM_CHANNEL = ManagedChannelBuilder
      .forAddress(CFG.museumUrl(), CFG.museumPort())
      .intercept(new GrpcConsoleInterceptor())
      .usePlaintext()
      .build();

  private static final Channel PAINTING_CHANNEL = ManagedChannelBuilder
      .forAddress(CFG.paintingUrl(), CFG.paintingPort())
      .intercept(new GrpcConsoleInterceptor())
      .usePlaintext()
      .build();

  private static final Channel USERDATA_CHANNEL = ManagedChannelBuilder
      .forAddress(CFG.userdataUrl(), CFG.userdataPort())
      .intercept(new GrpcConsoleInterceptor())
      .usePlaintext()
      .build();

  static final ArtistServiceBlockingStub ARTIST_BLOCKING_STUB =
      ArtistServiceGrpc.newBlockingStub(ARTIST_CHANNEL);

  static final GeoServiceBlockingStub GEO_BLOCKING_STUB
      = GeoServiceGrpc.newBlockingStub(GEO_CHANNEL);

  static final MuseumServiceBlockingStub MUSEUM_BLOCKING_STUB
      = MuseumServiceGrpc.newBlockingStub(MUSEUM_CHANNEL);

  static final PaintingServiceBlockingStub PAINTING_BLOCKING_STUB
      = PaintingServiceGrpc.newBlockingStub(PAINTING_CHANNEL);

  static final UserdataServiceGrpc.UserdataServiceBlockingStub USERDATA_BLOCKING_STUB
      = UserdataServiceGrpc.newBlockingStub(USERDATA_CHANNEL);
}
