# Spring Boot Security ワークショップ 第二回

## はじめに

前回は、次の作業を行ってきました。

- プロジェクトの作成
- デフォルト時の動作確認
- ログインユーザー取得ロジックのカスタマイズ
- 認可のカスタマイズ
- ログアウトの実装

これで、基本的な「ユーザー名とパスワードを用いたログイン」のカスタマイズポイントがわかってきたと思います。
今回は、次の作業を行っていきます。

- ログイン・ログアウトのカスタマイズ
- DB からユーザー情報を取得するように修正
- ユーザー登録


## ログイン・ログアウトのカスタマイズ

これまでは Spring Security デフォルトのログイン・ログアウト画面を利用していましたが、今回はこれをカスタマイズしましょう。

### SecurityConfig の修正

まずは、 SecurityConfig の `formLogin` と `logout` を修正します。

`src/main/java/dev/mikoto2000/security/configuration/SecurityConfig.java`:

```java
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
```

### ログインページ用のコントローラーを作成

今回はログインページを自作するので、ログインページ用のコントローラーも作成します。

`src/main/java/dev/mikoto2000/security/controller/LoginController.java`:

```java
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
```

### ログイン画面の作成

ログイン画面を用意します。

今回は、ユーザー名とパスワードを入れるテキストエリアとログインボタンがあるだけのシンプルなページにします。

`src/main/resources/templates/login.html`:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>LOGIN</title>
    <meta charset="UTF-8">
  </head>
  <body>
    <h1>ログインページ</h1>
    <div th:if="${param.error}">
      ユーザー名かパスワードが違います。
    </div>
    <form th:action="@{/login}" method="post">
      <div>
        <input type="text" name="username" placeholder="Username"/>
      </div>
      <div>
        <input type="password" name="password" placeholder="Password"/>
      </div>
      <input type="submit" value="ログイン" />
    </form>
  </body>
</html>
```

### ログアウトの仕組みを修正

今回は、ログアウト確認画面を表示せず、ログアウトボタンを押下したらすぐにログアウトするようにします。

第一回でも述べた通り、 Spring Security のデフォルトでは `/logout` に `POST` リクエストを送ることでログアウトを行います。

CSRF 対策のため、 Thymeleaf の機能を用いて `form` を構築します。
(`th:action` を使用すると、 form に自動で CSRF 対策のパラメーターが挿入される)

`src/main/resources/templates/index.html`:

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>index</title>
</head>
<body>
  Hello Spring Security.

  <p>
    <a href="/private">認証必須ページ</a>
  </p>
  <!-- 変更ここから -->
  <form method="post" th:action="@{/logout}">
    <button type="submit">ログアウト</button>
  </form>
  <!-- 変更ここまで -->
</body>
</html>
```

`src/main/resources/templates/private.html`:

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>認証必須ページ</title>
</head>
<body>
  これが見えているという事は、認証に成功しています。
  <!-- 変更ここから -->
  <form method="post" th:action="@{/logout}">
    <button type="submit">ログアウト</button>
  </form>
  <!-- 変更ここまで -->
</body>
</html>
```

ここまでの変更で、「自作ログインページでのログイン」と「ボタン押下で即ログアウト」が実現できます。

ログイン画面の自作はしていますが、ログイン処理は依然として Spring Security デフォルトのままです。
(今回は、 Spring Security に「ユーザー名とパスワード」を渡すところまでをカスタマイズしたということ)


## DB からユーザー情報を取得するように修正

さて、これまでは簡単のためにユーザー情報を HashMap で保持していましたが、ここで DB から取得するように修正しましょう。

今回は、インメモリの H2 DB と MyBatis の組み合わせを使います。

### DB・テーブル定義について

本プロジェクトには、既に H2 データベースの起動・接続・初期化の設定がされています。
これらについてはワークショップの本題とはずれるので、テーブル定義の説明のみを行います。

#### テーブル定義

ユーザー情報を格納するテーブルは、 `USERS` テーブルとして定義しています。
CREATE 文は `src/main/resources/schema.sql`, データは `src/main/resources/data.sql` で確認できます。

また、ロールについてはテーブルに保持せず、 `ADMIN` 固定としています。

`src/main/resources/schema.sql`:

```sql
CREATE TABLE USERS (
  username VARCHAR(50) PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL
);
```

`src/main/resources/data.sql`:

```sql
INSERT INTO USERS (username, password, enabled)
VALUES
(
  'mikoto2000',
  -- "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC" = "password"
  '{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC',
  true
),
(
  'mikoto2001',
  -- "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC" = "password"
  '{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC',
  true
);
```

### エンティティの作成

テーブルから取得した値を格納するためのクラスを作成します。

`src/main/java/dev/mikoto2000/security/entity/User.java`:

```java
package dev.mikoto2000.security.entity;

import lombok.Data;

/**
 * User
 */
@Data
public class User {
  private String username;
  private String password;
  private Boolean enabled;
}
```

### マッパーの作成

テーブルから情報を取得する Mapper インターフェースを作成します。

`username` を基にユーザー情報を取得する IF を定義します。

`src/main/java/dev/mikoto2000/security/reporitory/UsersMapper.java`:

```java
package dev.mikoto2000.security.reporitory;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import dev.mikoto2000.security.entity.User;

/**
 * UsersMapper
 */
@Mapper
public interface UsersMapper {
  @Select("""
          SELECT
            username,
            password,
            enabled
          FROM
            USERS
          WHERE
            USERS.username = #{username}
          """)
  Optional<User> findByUsername(String username);
}
```

### userDetailsServiceImpl の修正

これまでに作ったエンティティとマッパーを利用して、 DB からユーザー情報を取得するように修正します。

`src/main/java/dev/mikoto2000/security/configuration/UserDetailsServiceImpl.java`:

```java
package dev.mikoto2000.security.configuration;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import dev.mikoto2000.security.reporitory.UsersMapper;
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

    // 見つけたユーザーの情報を返却(今回はユーザー名・パスワード以外は固定位置で返却)
    return User.withUsername(user.getUsername())
      .password(user.getPassword())
      .roles("ADMIN")
      .disabled(!user.getEnabled())
      .build();
  }
}
```

`loadUserByUsername` メソッド内で、 DI した `UsersMapper` を利用し、ユーザー情報を取得し、
`User.withUsername` で Spring Security に返却するユーザー情報を組み立てます。


## ユーザー登録


## 参考資料

- CSRF
    - [安全なウェブサイトの作り方 - 1.6 CSRF（クロスサイト・リクエスト・フォージェリ） | 情報セキュリティ | IPA 独立行政法人 情報処理推進機構](https://www.ipa.go.jp/security/vuln/websecurity/csrf.html)

