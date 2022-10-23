

## 2. Security 설정, Data 설정

### 🖋 Contents

- 401 unauthorized 해결을 위한 Security 설정
- Datasource, JPA 설정
- Entity 생성
- H2 Console 결과 확인

<br/>

### 🖋 401 unauthorized 해결을 위한 Security 설정

**[ SecurityConfig.java ]**

```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/api/hello").permitAll()
                .anyRequest().authenticated();

    }
}
```

- **@EnableWebSecurity**은 기본적인 웹 보안 활성화하는 애노테이션
- **WebSecurityConfigurer**를 구현하거나 **WebSecurityConfigurerAdapter**를 상속하여 추가적인 설정이 가능하다.
- **.authorizeRequests()** : HttpServletRequest를 사용하는 요청들에 대한 접근 제한을 설정
- **.antMatchers(경로).permitAll()** : 설정 주소에 대한 요청은 인증없이 접근을 허용
- **.anyRequest().authenticated()** : 설정 값 이외에 나머지 요청들에 대해서는 모두 인증을 받아야 한다.

<br/>

<br/>

### 🖋 Datasource, JPA 설정

**[ application.yml ]**

```markup
spring:
  h2:
    console:
      enabled: true

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
        show_sql: true

logging:
  level:
    me.jioniy: DEBUG
```

- h2 console enable
- Datasource 설정 추가
- JPA 설정 추가
    - create-drop
        
        : SessionFactory가 시작될 때 Drop, Create, Alter하고 종료될 대 Drop을 진행한다 
        
        : SpringBoot 서버가 시작될 때마다 테이블들을 새로 생성
        
- properties 설정 - 콘솔 창에서 실행되는 sql들을 보기 좋게 보여주는 설정 추가
- logging - 로깅 레벨을 디버그로 설정

<br/>

<br/>

### 🖋 Entity 생성

**1) annotation 설명**

```java
**@Entity** : 데이터베이스의 테이블과 1:1 매핑되는 객체
**@Table(name="name")** : Table명을 클래스명과 독립적으로 지정
**@Getter / @Setter / @Builder / @Constructor** : 롬복 애노테이션으로, 관련 코드 자동 생성
```

<br/>

**2) User객체와 Authority 객체의 N:M 관계**

<p align="center"><img src="https://user-images.githubusercontent.com/68148196/197388898-0d5e3f11-c892-478a-a711-4a866481994d.png" width="500" height="230"/></p>


```java
[ User.java ]

@ManyToMany
@JoinTable(
        name = "user_authority",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
private Set<Authority> authorities;
```

- User Table과 Authority 테이블의 **N:M 관계**를 **1:N, N:1 관계의 조인 테이블**로 정의했다.

<br/>

**3) 서버 시작 시 Data 세팅**

- JPA는 현재 create-drop으로 설정되어 있어, 서버를 시작할 때마다 테이블을 새로 만들어주는 상태이다.
- 편의를 위해 서버를 시작할 때마다 Data를 자동으로 DB에 넣어주는 기능 필요
- data.sql 로 서버 시작 시 데이터 삽입 쿼리가 실행될 수 있도록 한다.

<br/>

<br/>

### 🖋 H2 Console 결과 확인

**1) h2-console 접근을 위한 Security 설정**

- h2-console 하위 모든 요청들과 favicon 관련 요청은 Spring Security 로직을 수행하지 않도록 설정 - **[SecurityConfig.java]**
    
    ```java
    @Override
        public void configure(WebSecurity web) throws Exception {
            web
                    .ignoring()
                    .antMatchers(
                            "/h2-console/**"
                            ,"/favicon.ico"
                    );
        }
    ```

<br/>

**2) 실행**

- 설정한 Entity 내용들을 기반으로 DB관련 정보들을 생성하는 쿼리 수행
<p align="center"><img src="https://user-images.githubusercontent.com/68148196/197388937-e5c9ac5f-89d6-4931-b9b7-9cf9cb95d503.png" width="450" height="700"/></p>

<br/>

- h2-database 확인(localhost:8080/h2-database)
<p align="center"><img src="https://user-images.githubusercontent.com/68148196/197389003-dfc867b9-ef3c-4e44-9624-d63a26da8869.png" width="380" height="300"/></p>
<p align="center"><img src="https://user-images.githubusercontent.com/68148196/197389009-3793597e-0504-440d-91f7-bbae93b5f68e.png" width="680" height="300"/></p>

<br/>

