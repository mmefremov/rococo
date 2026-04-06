package io.efremov.rococo.service;

import io.efremov.rococo.data.Authority;
import io.efremov.rococo.data.AuthorityEntity;
import io.efremov.rococo.data.UserEntity;
import io.efremov.rococo.data.repository.UserRepository;
import io.efremov.rococo.kafka.UserRegisteredProducer;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRegisteredProducer userRegisteredProducer;

  @Autowired
  public UserService(UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      UserRegisteredProducer userRegisteredProducer) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userRegisteredProducer = userRegisteredProducer;
  }

  @Transactional
  public @NonNull String registerUser(@NonNull String username, @NonNull String password) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new DataIntegrityViolationException("Username `" + username + "` already exists");
    }
    log.info("Registering new user: {}", username);
    UserEntity userEntity = new UserEntity();
    userEntity.setEnabled(true);
    userEntity.setAccountNonExpired(true);
    userEntity.setCredentialsNonExpired(true);
    userEntity.setAccountNonLocked(true);
    userEntity.setUsername(username);
    userEntity.setPassword(passwordEncoder.encode(password));

    AuthorityEntity readAuthorityEntity = new AuthorityEntity();
    readAuthorityEntity.setAuthority(Authority.read);
    AuthorityEntity writeAuthorityEntity = new AuthorityEntity();
    writeAuthorityEntity.setAuthority(Authority.write);

    userEntity.addAuthorities(readAuthorityEntity, writeAuthorityEntity);
    String savedUsername = userRepository.save(userEntity).getUsername();
    log.info("User registered successfully: {}", savedUsername);

    userRegisteredProducer.sendUserRegisteredEvent(savedUsername);
    return savedUsername;
  }
}
