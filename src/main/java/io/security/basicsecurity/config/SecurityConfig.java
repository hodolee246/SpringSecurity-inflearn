package io.security.basicsecurity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import javax.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated();
        http
                .formLogin();

        http
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .addLogoutHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    HttpSession session = httpServletRequest.getSession();
                    session.invalidate();
                })
                .logoutSuccessHandler((req, resp, auth) -> {
                    resp.sendRedirect("login");
                })
                .deleteCookies("remember-me");  // 로그아웃 할 경우 서버에서 만든 쿠키를 삭제함으로 해당 쿠키명을 적어주어야 한다.
    }
}
