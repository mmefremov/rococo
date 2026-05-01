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
import io.efremov.rococo.grpc.UserdataServiceGrpc.UserdataServiceBlockingStub;
import io.efremov.rococo.jupiter.annotation.meta.GrpcTest;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcTest
abstract class BaseGrpcTest {

  static final Config CFG = Config.getInstance();

  private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "grpc-nio-" + counter.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  };

  private static ManagedChannel channel(String host, int port) {
    return NettyChannelBuilder.forAddress(host, port)
        .eventLoopGroup(new NioEventLoopGroup(1, DAEMON_THREAD_FACTORY))
        .channelType(NioSocketChannel.class)
        .intercept(new GrpcConsoleInterceptor())
        .usePlaintext()
        .build();
  }

  private static final ManagedChannel ARTIST_CHANNEL = channel(CFG.artistUrl(), CFG.artistPort());
  private static final ManagedChannel GEO_CHANNEL = channel(CFG.geoUrl(), CFG.geoPort());
  private static final ManagedChannel MUSEUM_CHANNEL = channel(CFG.museumUrl(), CFG.museumPort());
  private static final ManagedChannel PAINTING_CHANNEL = channel(CFG.paintingUrl(), CFG.paintingPort());
  private static final ManagedChannel USERDATA_CHANNEL = channel(CFG.userdataUrl(), CFG.userdataPort());

  static final ArtistServiceBlockingStub ARTIST_BLOCKING_STUB =
      ArtistServiceGrpc.newBlockingStub(ARTIST_CHANNEL);

  static final GeoServiceBlockingStub GEO_BLOCKING_STUB =
      GeoServiceGrpc.newBlockingStub(GEO_CHANNEL);

  static final MuseumServiceBlockingStub MUSEUM_BLOCKING_STUB =
      MuseumServiceGrpc.newBlockingStub(MUSEUM_CHANNEL);

  static final PaintingServiceBlockingStub PAINTING_BLOCKING_STUB =
      PaintingServiceGrpc.newBlockingStub(PAINTING_CHANNEL);

  static final UserdataServiceBlockingStub USERDATA_BLOCKING_STUB =
      UserdataServiceGrpc.newBlockingStub(USERDATA_CHANNEL);
}
