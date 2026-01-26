package dev.mikoto2000.security.configuration;

import java.util.HashMap;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * UserDetailsServiceImpl
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

  private HashMap<String, String> users = new HashMap<>();
  {
    // "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC" = "password"
    users.put("mikoto2000", "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC");
    users.put("mikoto2001", "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC");
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // ユーザーの存在チェック
    if (!users.containsKey(username)) {
      throw new UsernameNotFoundException(username);
    }

    // 見つけたユーザーの情報を返却(今回はユーザー名・パスワード以外は固定位置で返却)
    return User.withUsername(username)
      .password(users.get(username))
      .roles("ADMIN")
      .disabled(false)
      .build();
  }
}
