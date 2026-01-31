package dev.mikoto2000.security.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * User
 */
@AllArgsConstructor
@Data
public class User {
  private String username;
  private String password;
  private Boolean enabled;
  private String role;
}
