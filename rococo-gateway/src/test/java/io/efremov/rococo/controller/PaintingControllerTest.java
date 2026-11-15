package io.efremov.rococo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.efremov.rococo.exception.GrpcExceptionHandler;
import io.efremov.rococo.model.ArtistInfoRequest;
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.CreatePaintingInfoRequest;
import io.efremov.rococo.model.GeoInfoResponse;
import io.efremov.rococo.model.MuseumInfoRequest;
import io.efremov.rococo.model.MuseumInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.PaintingInfoResponse;
import io.efremov.rococo.model.UpdatePaintingInfoRequest;
import io.efremov.rococo.service.GrpcPaintingClient;
import io.grpc.Status;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class PaintingControllerTest {

  @Mock
  private GrpcPaintingClient client;
  @InjectMocks
  private PaintingController controller;

  private static final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID countryId = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID artistId = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID museumId = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private final PaintingInfoResponse entity = new PaintingInfoResponse(id, "Water lilies", "Painting description",
      "image",
      new ArtistInfoResponse(artistId, "Monet", "French painter", "photo"),
      new MuseumInfoResponse(museumId, "Louvre", "French museum", "photo",
          new GeoInfoResponse("Paris", new CountryInfoResponse(countryId, "France"))));
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GrpcExceptionHandler()).build();
  }

  @Test
  void getAllPaintings_withoutParameters_usesDefaults() throws Exception {
    var page = new PageResponse<>(List.of(entity), 1, 1, 0, 9, true, true, 1);
    when(client.getAllPaintings(0, 9, null)).thenReturn(page);

    var result = mockMvc.perform(get("/api/painting")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("page JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getAllPaintings_withParameters_bindsPaginationAndFilter() throws Exception {
    var page = new PageResponse<>(List.of(entity), 3, 2, 1, 2, true, false, 1);
    when(client.getAllPaintings(1, 2, "Water lilies")).thenReturn(page);

    var result = mockMvc.perform(get("/api/painting").param("page", "1").param("size", "2")
        .param("title", "Water lilies")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("filtered page JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getPaintingById_validUuid_returnsEntityJson() throws Exception {
    when(client.getPaintingById(id)).thenReturn(entity);

    var result = mockMvc.perform(get("/api/painting/{id}", id)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("entity JSON")
        .isEqualTo(objectMapper.valueToTree(entity));
  }

  @Test
  void getPaintingsByArtist_withoutParameters_usesDefaults() throws Exception {
    var page = new PageResponse<>(List.of(entity), 1, 1, 0, 9, true, true, 1);
    when(client.getPaintingsByArtist(artistId, 0, 9)).thenReturn(page);

    var result = mockMvc.perform(get("/api/painting/author/{artistId}", artistId)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("artist paintings JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getPaintingsByArtist_withParameters_bindsPaginationAndArtistId() throws Exception {
    var page = new PageResponse<>(List.of(entity), 3, 2, 1, 2, true, false, 1);
    when(client.getPaintingsByArtist(artistId, 1, 2)).thenReturn(page);

    var result = mockMvc.perform(get("/api/painting/author/{artistId}", artistId)
        .param("page", "1").param("size", "2")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("artist paintings JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getPaintingsByArtist_invalidUuid_returnsBadRequest() throws Exception {
    var result = mockMvc.perform(get("/api/painting/author/not-a-uuid")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(400);
    verifyNoInteractions(client);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/painting/not-a-uuid", "/api/painting?page=invalid", "/api/painting?size=invalid"})
  void get_invalidParameter_returnsBadRequest(String url) throws Exception {
    var result = mockMvc.perform(get(url)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(400);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("parameter error").isNotBlank();
    verifyNoInteractions(client);
  }

  @ParameterizedTest
  @ValueSource(strings = {"POST", "PATCH"})
  void mutate_validBody_bindsRequestAndReturnsEntity(String method) throws Exception {
    if (method.equals("POST")) {
      when(client.createPainting(new CreatePaintingInfoRequest("Water lilies", "Painting description", "image",
          new ArtistInfoRequest(artistId), new MuseumInfoRequest(museumId)))).thenReturn(entity);
    } else {
      when(client.updatePainting(new UpdatePaintingInfoRequest(id, "Water lilies", "Painting description", "image",
          new ArtistInfoRequest(artistId), new MuseumInfoRequest(museumId)))).thenReturn(entity);
    }

    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/painting")
        .contentType(MediaType.APPLICATION_JSON).content(validBody(method).toString())).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("mutation response JSON")
        .isEqualTo(objectMapper.valueToTree(entity));
  }

  @ParameterizedTest(name = "{0}: invalid {1} = {2}")
  @MethodSource("invalidRequests")
  void mutate_invalidBody_rejectsBeforeCallingClient(String method, String field, String value) throws Exception {
    ObjectNode body = validBody(method);
    String[] path = field.split("\\.");
    ObjectNode parent = body;
    for (int i = 0; i < path.length - 1; i++) {
      parent = (ObjectNode) parent.get(path[i]);
    }
    if (value == null) {
      parent.putNull(path[path.length - 1]);
    } else {
      parent.put(path[path.length - 1], value);
    }

    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/painting")
        .contentType(MediaType.APPLICATION_JSON).content(body.toString())).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(400);
    assertThat(result.getResolvedException()).as("validation exception")
        .isInstanceOf(MethodArgumentNotValidException.class);
    var exception = (MethodArgumentNotValidException) result.getResolvedException();
    assertThat(exception.getBindingResult().getFieldErrors()).as("invalid field")
        .extracting(error -> error.getField()).contains(field);
    verifyNoInteractions(client);
  }

  @ParameterizedTest
  @CsvSource({"POST,{", "PATCH,{", "POST,", "PATCH,"})
  void mutate_malformedOrMissingBody_returnsBadRequest(String method, String body) throws Exception {
    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/painting")
        .contentType(MediaType.APPLICATION_JSON).content(body == null ? "" : body)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(400);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("body error").isNotBlank();
    verifyNoInteractions(client);
  }

  @ParameterizedTest
  @CsvSource({"NOT_FOUND,404", "INTERNAL,500"})
  void getPaintingById_grpcFailure_returnsMappedHttpError(Status.Code code, int expectedStatus) throws Exception {
    when(client.getPaintingById(id)).thenThrow(
        Status.fromCode(code).withDescription("Service error").asRuntimeException());

    var result = mockMvc.perform(get("/api/painting/{id}", id)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()))
        .as("error JSON").isEqualTo(objectMapper.readTree("{\"error\":\"Service error\"}"));
  }

  @ParameterizedTest
  @CsvSource({"POST,ALREADY_EXISTS,409", "PATCH,ALREADY_EXISTS,409",
      "POST,INVALID_ARGUMENT,400", "PATCH,INVALID_ARGUMENT,400", "PATCH,NOT_FOUND,404"})
  void mutate_grpcFailure_returnsMappedHttpError(String method, Status.Code code, int expectedStatus) throws Exception {
    var error = Status.fromCode(code).withDescription("Mutation error").asRuntimeException();
    if (method.equals("POST")) {
      when(client.createPainting(any())).thenThrow(error);
    } else {
      when(client.updatePainting(any())).thenThrow(error);
    }

    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/painting")
        .contentType(MediaType.APPLICATION_JSON).content(validBody(method).toString())).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("error description").isEqualTo("Mutation error");
  }

  private static ObjectNode validBody(String method) throws Exception {
    ObjectNode body = (ObjectNode) new ObjectMapper().readTree("""
        {"title": "Water lilies", "description": "Painting description", "content": "image", "artist": {"id": "%s"}, "museum": {"id": "%s"}}
        """.formatted(artistId, museumId));
    if (method.equals("PATCH")) {
      body.put("id", id.toString());
    }
    return body;
  }

  private static Stream<Arguments> invalidRequests() {
    return Stream.concat(Stream.of("POST", "PATCH").flatMap(method -> Stream.of(
        Arguments.of(method, "title", null),
        Arguments.of(method, "title", " "),
        Arguments.of(method, "title", "ab"),
        Arguments.of(method, "title", "a".repeat(256)),
        Arguments.of(method, "description", "short"),
        Arguments.of(method, "description", "a".repeat(1001)),
        Arguments.of(method, "content", " "),
        Arguments.of(method, "artist", null),
        Arguments.of(method, "artist.id", null),
        Arguments.of(method, "museum", null)
    )), Stream.of(Arguments.of("PATCH", "id", null)));
  }
}
