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
import io.efremov.rococo.model.ArtistInfoResponse;
import io.efremov.rococo.model.CreateArtistInfoRequest;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.model.UpdateArtistInfoRequest;
import io.efremov.rococo.service.GrpcArtistClient;
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
class ArtistControllerTest {

  @Mock
  private GrpcArtistClient client;
  @InjectMocks
  private ArtistController controller;

  private static final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final ArtistInfoResponse entity = new ArtistInfoResponse(id, "Monet", "French painter", "image");
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GrpcExceptionHandler()).build();
  }

  @Test
  void getAllArtists_withoutParameters_usesDefaults() throws Exception {
    var page = new PageResponse<>(List.of(entity), 1, 1, 0, 10, true, true, 1);
    when(client.getAllArtists(0, 10, null)).thenReturn(page);

    var result = mockMvc.perform(get("/api/artist")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("page JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getAllArtists_withParameters_bindsPaginationAndFilter() throws Exception {
    var page = new PageResponse<>(List.of(entity), 3, 2, 1, 2, true, false, 1);
    when(client.getAllArtists(1, 2, "Monet")).thenReturn(page);

    var result = mockMvc.perform(get("/api/artist").param("page", "1").param("size", "2")
        .param("name", "Monet")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("filtered page JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getArtistById_validUuid_returnsEntityJson() throws Exception {
    when(client.getArtistById(id)).thenReturn(entity);

    var result = mockMvc.perform(get("/api/artist/{id}", id)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("entity JSON")
        .isEqualTo(objectMapper.valueToTree(entity));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/artist/not-a-uuid", "/api/artist?page=invalid", "/api/artist?size=invalid"})
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
      when(client.createArtist(new CreateArtistInfoRequest("Monet", "French painter", "image"))).thenReturn(entity);
    } else {
      when(client.updateArtist(new UpdateArtistInfoRequest(id, "Monet", "French painter", "image"))).thenReturn(entity);
    }

    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/artist")
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

    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/artist")
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
    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/artist")
        .contentType(MediaType.APPLICATION_JSON).content(body == null ? "" : body)).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(400);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("body error").isNotBlank();
    verifyNoInteractions(client);
  }

  @ParameterizedTest
  @CsvSource({"NOT_FOUND,404", "INTERNAL,500"})
  void getArtistById_grpcFailure_returnsMappedHttpError(Status.Code code, int expectedStatus) throws Exception {
    when(client.getArtistById(id)).thenThrow(
        Status.fromCode(code).withDescription("Service error").asRuntimeException());

    var result = mockMvc.perform(get("/api/artist/{id}", id)).andReturn();

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
      when(client.createArtist(any())).thenThrow(error);
    } else {
      when(client.updateArtist(any())).thenThrow(error);
    }

    var result = mockMvc.perform(request(HttpMethod.valueOf(method), "/api/artist")
        .contentType(MediaType.APPLICATION_JSON).content(validBody(method).toString())).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("error description").isEqualTo("Mutation error");
  }

  private static ObjectNode validBody(String method) throws Exception {
    ObjectNode body = (ObjectNode) new ObjectMapper().readTree("""
        {"name": "Monet", "biography": "French painter", "photo": "image"}
        """);
    if (method.equals("PATCH")) {
      body.put("id", id.toString());
    }
    return body;
  }

  private static Stream<Arguments> invalidRequests() {
    return Stream.concat(Stream.of("POST", "PATCH").flatMap(method -> Stream.of(
        Arguments.of(method, "name", null),
        Arguments.of(method, "name", " "),
        Arguments.of(method, "name", "ab"),
        Arguments.of(method, "name", "a".repeat(256)),
        Arguments.of(method, "biography", null),
        Arguments.of(method, "biography", "short"),
        Arguments.of(method, "biography", "a".repeat(2001)),
        Arguments.of(method, "photo", " ")
    )), Stream.of(Arguments.of("PATCH", "id", null)));
  }
}
