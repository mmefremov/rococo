package io.efremov.rococo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.efremov.rococo.data.Authority;
import io.efremov.rococo.data.AuthorityEntity;
import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.data.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  private DatabaseUserDetailsService databaseUserDetailsService;

  @BeforeEach
  void setUp() {
    databaseUserDetailsService = new DatabaseUserDetailsService(userRepository);
  }

  @Test
  void loadUserByUsername_existingUser_returnsUserDetails() {
    UserEntity userEntity = createUserEntity("testuser", "encodedPassword", true, true, true, true);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

    UserDetails userDetails = databaseUserDetailsService.loadUserByUsername("testuser");

    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo("testuser");
    assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
    assertThat(userDetails.isEnabled()).isTrue();
    assertThat(userDetails.isAccountNonExpired()).isTrue();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
    assertThat(userDetails.getAuthorities()).hasSize(2);
  }

  @Test
  void loadUserByUsername_nonExistentUser_throwsUsernameNotFoundException() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> databaseUserDetailsService.loadUserByUsername("nonexistent"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("nonexistent");
  }

  @Test
  void loadUserByUsername_mapsAuthoritiesCorrectly() {
    UserEntity userEntity = createUserEntity("user", "pass", true, true, true, true);
    when(userRepository.findByUsername("user")).thenReturn(Optional.of(userEntity));

    UserDetails userDetails = databaseUserDetailsService.loadUserByUsername("user");

    assertThat(userDetails.getAuthorities()).hasSize(2);
    assertThat(userDetails.getAuthorities())
        .extracting(authority -> authority.getAuthority())
        .containsExactlyInAnyOrder(Authority.READ.name(), Authority.WRITE.name());
  }

  @Test
  void loadUserByUsername_returnsCorrectAccountFlags() {
    UserEntity userEntity = createUserEntity("user", "pass", false, false, true, true);
    when(userRepository.findByUsername("user")).thenReturn(Optional.of(userEntity));

    UserDetails userDetails = databaseUserDetailsService.loadUserByUsername("user");

    assertThat(userDetails.isEnabled()).isFalse();
    assertThat(userDetails.isAccountNonExpired()).isFalse();
    assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    assertThat(userDetails.isAccountNonLocked()).isTrue();
  }

  private UserEntity createUserEntity(String username, String password,
      boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked) {
    UserEntity userEntity = new UserEntity();
    userEntity.setId(UUID.randomUUID());
    userEntity.setUsername(username);
    userEntity.setPassword(password);
    userEntity.setEnabled(enabled);
    userEntity.setAccountNonExpired(accountNonExpired);
    userEntity.setCredentialsNonExpired(credentialsNonExpired);
    userEntity.setAccountNonLocked(accountNonLocked);

    AuthorityEntity readAuthorityEntity = new AuthorityEntity();
    readAuthorityEntity.setAuthority(Authority.READ);
    AuthorityEntity writeAuthorityEntity = new AuthorityEntity();
    writeAuthorityEntity.setAuthority(Authority.WRITE);
    userEntity.addAuthorities(readAuthorityEntity, writeAuthorityEntity);

    return userEntity;
  }
}
