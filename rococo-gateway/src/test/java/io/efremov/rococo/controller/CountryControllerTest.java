package io.efremov.rococo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.efremov.rococo.exception.GrpcExceptionHandler;
import io.efremov.rococo.model.CountryInfoResponse;
import io.efremov.rococo.model.PageResponse;
import io.efremov.rococo.service.GrpcGeoClient;
import io.grpc.Status;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

  @Mock
  private GrpcGeoClient client;
  @InjectMocks
  private CountryController controller;

  private final CountryInfoResponse country = new CountryInfoResponse(UUID.randomUUID(), "France");
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GrpcExceptionHandler()).build();
  }

  @Test
  void getAllCountries_withoutParameters_usesDefaults() throws Exception {
    var page = new PageResponse<>(List.of(country), 1, 1, 0, 20, true, true, 1);
    when(client.getAllCountries(0, 20)).thenReturn(page);

    var result = mockMvc.perform(get("/api/country")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("country page JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @Test
  void getAllCountries_withParameters_bindsPagination() throws Exception {
    var page = new PageResponse<>(List.of(country), 3, 2, 1, 2, true, false, 1);
    when(client.getAllCountries(1, 2)).thenReturn(page);

    var result = mockMvc.perform(get("/api/country").param("page", "1").param("size", "2")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(200);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString())).as("country page JSON")
        .isEqualTo(objectMapper.valueToTree(page));
  }

  @ParameterizedTest
  @ValueSource(strings = {"page", "size"})
  void getAllCountries_invalidParameter_returnsBadRequest(String parameter) throws Exception {
    var result = mockMvc.perform(get("/api/country").param(parameter, "invalid")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(400);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("parameter error").isNotBlank();
    verifyNoInteractions(client);
  }

  @Test
  void getAllCountries_grpcUnavailable_returnsInternalServerError() throws Exception {
    when(client.getAllCountries(0, 20))
        .thenThrow(Status.UNAVAILABLE.withDescription("Geo unavailable").asRuntimeException());

    var result = mockMvc.perform(get("/api/country")).andReturn();

    assertThat(result.getResponse().getStatus()).as("HTTP status").isEqualTo(500);
    assertThat(objectMapper.readTree(result.getResponse().getContentAsString()).path("error").asText())
        .as("error description").isEqualTo("Geo unavailable");
  }
}
