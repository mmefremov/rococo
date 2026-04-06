package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.CountryEntity;
import io.efremov.rococo.data.repository.CountryRepository;
import io.efremov.rococo.exception.CountryNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

  @Mock
  private CountryRepository countryRepository;

  @InjectMocks
  private CountryService countryService;

  @Test
  void findAll_returnsPageOfCountries() {
    CountryEntity entity = createCountry("RU", "Россия");
    when(countryRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(entity)));

    Page<CountryEntity> result = countryService.findAll(PageRequest.of(0, 20));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Россия");
  }

  private CountryEntity createCountry(String code, String name) {
    CountryEntity entity = new CountryEntity();
    entity.setId(UUID.randomUUID());
    entity.setCode(code);
    entity.setName(name);
    return entity;
  }

  @Test
  void findById_existingId_returnsCountry() {
    UUID id = UUID.randomUUID();
    CountryEntity entity = createCountry("DE", "Германия");
    entity.setId(id);
    when(countryRepository.findById(id)).thenReturn(Optional.of(entity));

    CountryEntity result = countryService.findById(id);

    assertThat(result.getCode()).isEqualTo("DE");
    assertThat(result.getName()).isEqualTo("Германия");
  }

  @Test
  void findById_nonExistingId_throwsNotFoundException() {
    UUID id = UUID.randomUUID();
    when(countryRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> countryService.findById(id))
        .isInstanceOf(CountryNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void findAllByIds_returnsMatchingCountries() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    CountryEntity e1 = createCountry("DE", "Германия");
    e1.setId(id1);
    CountryEntity e2 = createCountry("FR", "Франция");
    e2.setId(id2);
    Collection<UUID> ids = List.of(id1, id2);
    when(countryRepository.findAllById(ids)).thenReturn(List.of(e1, e2));

    List<CountryEntity> result = countryService.findAllByIds(ids);

    assertThat(result).hasSize(2);
  }

  @Test
  void findAllByIds_emptyInput_returnsEmpty() {
    List<CountryEntity> result = countryService.findAllByIds(List.of());
    assertThat(result).isEmpty();
  }
}
