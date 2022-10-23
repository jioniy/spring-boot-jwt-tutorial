## 1. JWT 소개, 프로젝트 생성

### 강의 목표 : Spring Boot를 이용한 JWT 인증 구현

<br/>

### 🖋 JWT(JSON Web Token) 소개

- RFC 7519 웹 표준
- JSON 객체를 사용해서 토큰 자체에 정보들을 저장하고 있는 Web Token
- **Header, Payload, Signature 3 부분으로 구성**
    - **Header** - Signature를 해싱하기 위한 알고리즘 정보
    - **Payload** - 서버와 클라이언트가 주고 받는, 시스템에서 실제로 사용될 정보에 대한 내용
    - **Signature** - 토큰의 유효성 검증을 위한 문자열
    
<p align="center"><img src="https://user-images.githubusercontent.com/68148196/197383230-cf52e410-fee0-418d-8f0c-815fc3446114.png" width="400" height="400"/></p>


<br/>

- **JWT의 장점**
    - 중앙의 인증 서버, 데이터 스토어에 대한 의존성 없음, 시스템 수평 확장 유리
    - Base64 URL Safe Encoding > URL, Cookie, Header 모두 사용 가능

<br/>

- **JWT의 단점**
    - Payload의 정보가 많아지면 네트워크 사용량 증가, 데이터 설계 고려 필요
    - 토큰이 클라이언트에 저장, 서버에서 클라이언트의 토큰을 조작할 수 없음

<br/>

### 🖋 프로젝트 생성

**1) Dependencies**

- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database
- Lombok
- Validation

<br/>

**2) Controller GetMapping 함수 작성**

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {
    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("hello");
    }
}
```

<br/>

**3) 결과 확인**

<p align="center"><img src="https://user-images.githubusercontent.com/68148196/197383163-81af5b05-2b24-4d1a-b833-e89a66f8b03c.png" width="450" height="400"/></p>

- spring 버전에 따라 get 요청 시 body를 반환하지 않을 수 있음 → 2.4.1 버전으로 변환
```java
plugins {
	id 'org.springframework.boot' version '2.4.1'
...
}
```
