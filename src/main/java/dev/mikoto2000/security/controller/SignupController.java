package dev.mikoto2000.security.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.mikoto2000.security.entity.User;
import dev.mikoto2000.security.repository.UsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SignupController
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SignupController {

  private final UsersMapper usersMapper;
  private final PasswordEncoder passwordEncoder;

  @GetMapping("/signup")
  public String signupPage() {
    return "signup";
  }

  @PostMapping("/signup")
  public String signup(
      @RequestParam String username,
      @RequestParam String password
      ) {

    // パスワードのハッシュ化
    var hashedPassword = passwordEncoder.encode(password);

    try {
      // ユーザーをテーブルへインサート
      User user = new User(username, hashedPassword, true, "ADMIN");
      usersMapper.insert(user);
    } catch (RuntimeException e) {
      log.error("ユーザー登録で例外が発生しました", e);
      return "redirect:/signup?error";
    }

    // ログイン画面へリダイレクト
    return "redirect:/login";
  }

}

