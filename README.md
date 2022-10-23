## 3. JWT 코드, Security 설정 추가

### 🖋 Contents

- JWT 설정 추가
- JWT 관련 코드 개발
- Security 설정 추가


<br/>


<br/>

### 🖋 JWT 설정 추가

- HS512 알고리즘 사용 → Secret Key는 64 Byte 이상
- 토큰 만료 시간 86400초
    
    ```markup
    **[application.yml]**
    jwt:
      header: Authorization
      #HS512 알고리즘을 사용할 것이기 때문에 512bit, 즉 64byte 이상의 secret key를 사용해야 한다.
      #echo 'silvernine-tech-spring-boot-jwt-tutorial-secret-silvernine-tech-spring-boot-jwt-tutorial-secret'|base64
      secret: c2lsdmVybmluZS10ZWNoLXNwcmluZy1ib290LWp3dC10dXRvcmlhbC1zZWNyZXQtc2lsdmVybmluZS10ZWNoLXNwcmluZy1ib290LWp3dC10dXRvcmlhbC1zZWNyZXQK
      token-validity-in-seconds: 86400
    ```
    
- build.gradle JWT 관련 라이브러리 추가
    
    ```tsx
    implementation group: 'io.jsonwebtoken', name: 'jjwt-api', version: '0.11.5'
    runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-impl', version: '0.11.5'
    runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-jackson', version: '0.11.5'
    ```
    


<br/>


<br/>

### 🖋 JWT 관련 코드 개발

***1) TokenProvider***

- **토큰의 생성, 토큰의 유효성 검증 등을 담당**
- InitializingBean을 implements →  **afterPropertiesSet** 메소드 오버라이딩
    - 빈이 생성되고 secret값을 주입을 받은 후에 해당 secret값을 Base64 Decode해서 key 변수에 할당
        
        ```java
        @Component /*빈 생성*/
        public class TokenProvider implements InitializingBean {
        		. . .
            public TokenProvider(/*의존성 주입*/
                    @Value("${jwt.secret}") String secret,
                    @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
                this.secret = secret;
                this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
            }
        
            @Override
            public void afterPropertiesSet() {/*주입 받은 secret값을 Base64 Decode해서 key 변수에 할당*/
                byte[] keyBytes = Decoders.BASE64.decode(secret);
                this.key = Keys.hmacShaKeyFor(keyBytes);
            }
        			. . .
        ```
        

- **createToken()**
    - Authentication객체의 권한 정보를 이용해서 토큰을 생성하는 메소드
        
        ```java
        /*
        * Authentication 객체를 파라미터로 받아 권한들, 만료시간을 설정하고 토큰을 생성하는 로직
        */
        public String createToken(Authentication authentication) {
                String authorities = authentication.getAuthorities().stream() //권한 설정
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
        
                long now = (new Date()).getTime();
                Date validity = new Date(now + this.tokenValidityInMilliseconds);//application.yml에서 설정한 만료시간으로
        
                return Jwts.builder() // jwt토큰 생성
                        .setSubject(authentication.getName())
                        .claim(AUTHORITIES_KEY, authorities)
                        .signWith(key, SignatureAlgorithm.HS512)
                        .setExpiration(validity)
                        .compact();
            }
        ```
        
    
- **getAuthentication()**
    - Token에 담겨있는 정보를 이용해 Authentication객체를 반환하는 메소드
        
        ```java
        public Authentication getAuthentication(String token) {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody(); // 토큰을 이용해 클레임 생성
        
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))//클레임에서 권한정보를 빼내서 
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
        
            User principal = new User(claims.getSubject(), "", authorities);//빼낸 권한 정보를 이용해 유저 객체 생성
        
            return new UsernamePasswordAuthenticationToken(principal, token, authorities); // 유저 객체, 토큰, 권한 정보 -> 최종적으로 Authentication객체를 반환
        }
        ```
        
- **validateToken()**
    - 토큰의 유효성 검사
    - 토큰 파싱 후 발생하는 Exception들을 캐치, 문제가 있으면 false, 정상이면 true
        
        ```java
        public boolean validateToken(String token) {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); 
                return true;
            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                logger.info("잘못된 JWT 서명입니다.");
            } catch (ExpiredJwtException e) {
                logger.info("만료된 JWT 토큰입니다.");
            } catch (UnsupportedJwtException e) {
                logger.info("지원되지 않는 JWT 토큰입니다.");
            } catch (IllegalArgumentException e) {
                logger.info("JWT 토큰이 잘못되었습니다.");
            }
            return false;
        }
        ```


<br/>
        

***2) JwtFilter*** 

- **JWT를 위한 커스텀 필터**
- 기본 코드
    
    ```java
    public class JwtFilter extends GenericFilterBean {
    
        private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    
        public static final String AUTHORIZATION_HEADER = "Authorization";
    
        private TokenProvider tokenProvider;
    
        public JwtFilter(TokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
        }
    
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    			// jwt토큰의 인증 정보를 SecurityContext에 저장하는 역할 수행
    			//GenericFilterBean의 메소드 오버라이딩
    			//필터링 로직은 내부에 작성
        }
    }
    ```
    

- **resolveToken()**
    - 필터링을 하기 위해서 토큰 정보 필요 → RequestHeader에서 토큰 정보를 꺼내오기 위한 메소드
    
    ```java
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
    
        return null;
    }
    ```
    

- **doFilter()**
    - jwt토큰의 인증 정보를 SecurityContext에 저장하는 메소드
    - resolveToken을 통해 토큰을 받아온 후, 유효성 검증을 하고 정상 토큰이면 SecurityContext에 저장한다.
    
    ```java
    @Override
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
    
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
           Authentication authentication = tokenProvider.getAuthentication(jwt);
           SecurityContextHolder.getContext().setAuthentication(authentication);
           logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
           logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }
    
        filterChain.doFilter(servletRequest, servletResponse);
     }
    ```


<br/>
    

***3) JwtSecurityConfig***

- **TokenProvider, JwtFilter를 SecurityConfig에 적용**

```java
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private TokenProvider tokenProvider;

    public JwtSecurityConfig(TokenProvider tokenProvider){
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void configure(HttpSecurity http){
        JwtFilter customFilter = new JwtFilter(tokenProvider);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class); //Security 로직에 JwtFilter 등록
    }
}
```


<br/>

***4) JwtAuthenticationEntryPoint***

- **유효한 자격 증명을 제공하지 않고 접근하려 할 때 401 Unauthorized 에러를 반환**
- `AuthenticationEntryPoint` 구현


<br/>

***5) JwtAccessDeniedHandler***

- **필요한 권한이 존재하지 않는 경우 403 Forbidden 에러 반환**
- `AccessDeniedHandler` 구현


<br/>


<br/>


### 🖋 SecurityConfig 설정

- 앞서 만들었던 5개의 클래스를 SecurityConfig에 적용
- `@EnableGlobalMethodSecurity(prePostEnabled = true)`
    - @PreAuthorized 를 메소드 단위로 추가하기 위해 적용
- **TokenProvider, JwtAuthenticationEntryPoint, JwtAccessDeniedHandler** 주입
- HttpSecurity 설정

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
            /*토큰 사용 -> csrf disable*/
            .csrf().disable()

            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            /*h2-console을 위한 설정*/
            .and()
            .headers()
            .frameOptions()
            .sameOrigin()

            /*세션 사용 X*/
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            /*요청 허용 설정 - 로그인 API, 회원가입 API는 토큰이 없는 상태에서 요청*/
            .and()
            .authorizeRequests()
            .antMatchers("/api/hello").permitAll()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/signup").permitAll()
            .anyRequest().authenticated()

            /*JwtFilter를 addFilterBefore로 등록했던 JwtSecurityConfig 클래스 적용*/
            .and()
            .apply(new JwtSecurityConfig(tokenProvider));
}
```

<br/>

