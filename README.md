# Spring Boot Security ワークショップ

## Spring Initializr

[これ](https://start.spring.io/#!type=maven-project&language=java&platformVersion=4.0.2&packaging=jar&configurationFileFormat=yaml&jvmVersion=25&groupId=dev.mikoto2000&artifactId=security&name=security&description=Demo%20project%20for%20Spring%20Security&packageName=dev.mikoto2000.security&dependencies=devtools,lombok,web,postgresql,mybatis,security,thymeleaf)

## インデックスページの作成

### HTML の配置

`src/main/resources/templates/index.html` に、以下 HTML ファイルを配置します。

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>index</title>
</head>
<body>
  Hello Spring Security.
</body>
</html>
```

### コントローラーの作成

`src/main/java/dev/mikoto2000/security/controller/IndexController.java` に、以下 java ファイルを配置します。

```java
package dev.mikoto2000.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * IndexController
 */
@Controller
public class IndexController {
  @GetMapping("/")
  public String index() {
    return "index";
  }
}
```


## デフォルト時の動作確認

Spring Security の設定をしていない場合、起動時に以下のようにログインパスワードが表示されます。

```
2026-01-26T09:53:39.057Z  WARN 6610 --- [security] [  restartedMain] .s.a.UserDetailsServiceAutoConfiguration : 

Using generated security password: 5c89d5fc-d4ff-49b9-8bc0-62cb35f4f13d

This generated password is for development use only. Your security configuration must be updated before running your application in production.

```

デフォルトユーザーは `user` なので、このユーザー名・パスワードでログインできます。

`http://localhost:8080` を開くとログインフォームが表示されるので、ユーザー名: `user`, パスワード: `<起動時に表示されたパスワード>` でログインし、インデックスページが表示されれば OK です。

もちろんこれでは業務要件を満たすわけないので、これからワークショップで行うようなカスタマイズをしていくこととなります。


## ログインユーザー取得ロジックのカスタマイズ

ここでは、 Spring Security のカスタマイズポイントのひとつである「ログインユーザー取得ロジック」のカスタマイズを行います。

Spring Security では、ログイン時に「ユーザー名から認証対象ユーザーを取得する処理」を `UserDetailsService` というインタフェースに切り出しています。

この `UserDetailsService` を差し替えることで、「どこからユーザー情報を取得するか」をカスタマイズできるようになっています。

`src/main/java/dev/mikoto2000/security/configuration/UserDetailsServiceImpl.java` を作成し、次のように実装します。

```java
package dev.mikoto2000.security.configuration;

import java.util.HashMap;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * UserDetailServiceImpl
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
```

こうすることで、「ユーザーを探して Spring Security の認証処理に必要なユーザー情報を返却する」という処理を自分で実装できます。
(UserDetailsService を実装したクラスを Bean として登録すると、Spring Security が自動的にこれを認証処理に利用します)

今回の実装では、メモリ上に HashMap でユーザー名とパスワードを保持し、そこからフォームから渡されたユーザー( `loadUserByUsername` の仮引数 `username` ) を探すように実装しています。

今回は仕組み理解が目的のため、DB ではなく HashMap を使って最小構成で実装していますが、
本格的に実装するなら `loadUserByUsername` の中で DB 接続してユーザー情報を検索し、返却することになります。

これはワークショップの後半でやってみましょう。

また、パスワードについても、デフォルトで対応している `bcrypt` 形式でハッシュ化された文字列を使っています。
ここもカスタマイズポイントですので、後の方で取り上げます。

