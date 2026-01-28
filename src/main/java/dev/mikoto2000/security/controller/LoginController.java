package dev.mikoto2000.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LoginController
 */
@Controller
public class LoginController {

  @GetMapping("/login")
  public String login() {
    return "login";
  }

}
