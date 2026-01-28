package dev.mikoto2000.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 */
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    /* 修正ここから */
    // ログインフォームを自作し、ログイン関連 URL は誰でもアクセスできるよう指定
    // ログイン失敗時には "/login?error" へリダイレクトする
    http.formLogin(login -> {
      login
        .loginPage("/login")
        .permitAll()
        .failureUrl("/login?error");
    })
    // ログアウトは、画面の自作はせず
    // POST する URL とログアウト成功後のリダイレクト URL を指定する
    .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/"))
    /* 修正ここまで */
    .authorizeHttpRequests(auth -> {
      auth
        // "/" は誰でも表示できる
        .requestMatchers("/").permitAll()
        // その他ページは、ログイン済みでないと表示できない
        .anyRequest().authenticated();
    });
    return http.build();
  }
}
