package moanote.backend.security;

import moanote.backend.entity.UserData;

import java.util.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

public class CustomUserDetails implements UserDetails {

  private UserData user;

  // 생성자
  public CustomUserDetails(UserData user) {
    this.user = user;
  }

  // 권한정보 제공
  // 딱히 권한 정보를 사용하지 않을 시 Collections.emptyList()를 반환
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // ArrayList<GrantedAuthority> auths = new ArrayList<>();
    // for(String role : user.getHasRole()){
    //     auths.add(new GrantedAuthority() {
    //         @Override
    //         public String getAuthority() {
    //             return role;
    //         }
    //     });
    // }
    // return auths;
    return Collections.emptyList();
  }

  // 비밀번호 정보 제공
  @Override
  public String getPassword() {
    return user.getPassword();
  }

  // ID 정보 제공
  @Override
  public String getUsername() {
    return user.getUsername();
  }

  // 계정 만료여부 제공
  // 특별히 사용을 안할시 항상 true를 반환하도록 처리
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  // 계정 비활성화 여부 제공
  // 특별히 사용 안할시 항상 true를 반환하도록 처리
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  // 계정 인증 정보를 항상 저장할지에 대한 여부
  // false로 처리할시 인증정보가 만료된 것으로 간주됨
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  // 계정의 활성화 여부
  // 딱히 사용안할시 항상 true를 반환하도록 처리
  @Override
  public boolean isEnabled() {
    return true;
  }
}