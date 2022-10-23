package me.jioniy.jwttutorial.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @EnableWebSecurity - 기본적인 웹 보안 활성화
 * 추가적인 설정 - WebSecurityConfigurer implements or WebSecurityConfigurerAdapter extends
* */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers(
                        "/h2-console/**"
                        ,"/favicon.ico"
                );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests() /*HttpServletRequest를 사용하는 요청들에 대한 접근제한을 설정*/
                .antMatchers("/api/hello").permitAll()/* 설정 주소에 대한 요청은 인증없이 접근을 허용*/
                .anyRequest().authenticated();/*나머지 요청들에 대해서는 모두 인증을 받아야 한다.*/

    }
}
