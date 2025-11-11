package moanote.backend.security;

import moanote.backend.entity.UserData;
import moanote.backend.repository.UserDataRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserDataRepository userDataRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserData user = userDataRepository.findByUsername(username).orElseThrow();

    if (user == null) {
      throw new UsernameNotFoundException("계정을 찾을 수 없습니다.");
    }

    return new CustomUserDetails(user);
  }
}
