package dev.mikoto2000.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PrivateController
 */
@Controller
public class PrivateController {
  @GetMapping("/private")
  public String privatePage() {
    return "private";
  }
}

