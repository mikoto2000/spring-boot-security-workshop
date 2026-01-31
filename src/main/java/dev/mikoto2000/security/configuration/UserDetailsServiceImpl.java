package dev.mikoto2000.security.configuration;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import dev.mikoto2000.security.repository.UsersMapper;
import lombok.RequiredArgsConstructor;

/**
 * UserDetailsServiceImpl
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UsersMapper usersMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // ユーザーの存在チェック
    var userOpt = usersMapper.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("User not found.");
    }
    var user = userOpt.get();

    // 見つけたユーザーの情報を返却
    return User.withUsername(user.getUsername())
      .password(user.getPassword())
      .roles(user.getRole())
      .disabled(!user.getEnabled())
      .build();
  }
}
