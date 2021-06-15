# Spring Security
- - -

## 프로젝트 구성 및 의존성 추가
~~~
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
~~~
**스프링 시큐리티의 의존성 추가 시**
1. 서버가 기동되면 스프링 시큐리티의 초기화 작업 및 보안 설정이 이루어진다.
2. 별도의 설정이나 구현을 하지 않아도 기본적인 웹 보안 기능이 현재 시스템에 연동되어 작동한다.
	- 모든 요청은 인증이 되어야하 자원에 접근이 가능
	- 인증 방식
		- 폼 로그인
		- httpBasic 로그인
	- 기본 로그인 페이지 제공
	- 기본 계정 한 개 제공
		- ID    : user
		- PWD : 랜덤 문자열

**문제점**
1. 계정 추가 권한 추가, DB 연동 등
2. 기본적인 보안 기능 외에 시스템에서 필요로 하는 더 세부적이고 추가적인 보안기능이 필요

## 사용자 정의 보안 기능 구현
- security 의존성 추가했을경우 보안설정을 할 수 있는 클래스로 해당 클래스를 상속을 받아 사용할 경우 HttpSecurity 클래스의 인증 API, 인가 API를 자유롭게 정의할 수 있다.    
- 다른요청들은 인증을 하지 않을경우 기본 폼로그인으로 이동된다.
~~~
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
    }
}
~~~

- 매번 랜덤 문자열로 로그인 해야하는 비밀번호의 경우 application.properties 에 등록해서 간편하게 사용할 수 있다.

~~~
spring.security.user.name=user
spring.security.user.password=1111
~~~

## Form Login 인증

### configure() 메소드 설정
~~~
http.formLogin()                              // Form 로그인 인증 기능이 작동함
    .loginPage("loginPage")                  // 사용자 정의 로그인 페이지
    .defaultSuccessUrl("errorPage")          // 로그인 실패 후 이동 페이지
    .usernameParameter("username")          // 아이디 파라미터명 설정
    .passwordParameter("password")          // 패스워드 파라미터명 설정
    .loginProcessingUrl("/login")           // 로그인 form action url
    .successHandler(loginSuccessHandler())  // 로그인 성공 후 핸들러
    .failureHandler(loginFailurHandler())   // 로그인 실패 후 핸들러
~~~
### 로그인 처리
1. 사용자 인증 시도 시 UsernamePasswordauthenticationFilter 에서 사용자 정보를 확인한다
2. AntPathRequestMatcher 에서 로그인 정보를 확인한다.
    - 정보가 미 일치 시 다른 필터로 이동
3. 정보가 일치 시 Authentication 에서 사용자가 입력한 로그인 값을 저장하여 인증 객체를 생성한다.
4. AuthenticationManager 에서 인증 처리를 하는데 AuthenticationProvider 에게 인증처리를 위임한다.
    - 인증 실패 시 AuthenticationException 발생
    - 인증 성공 시 권한 정보 및 인증 객체를 저장하여 AhthenticationManager 에게 전송한다.
5. AuthenticationManager 는 인증 객체를 Authentication 에게 반환한다.
6. Authentication 에서 인증 결과인 인증 객체를 Securitycontext 에 전송한다.
7. SecurityContext 에서 인증 객체를 저장한다
8. SuccessHandler 처리

![loginForm](/md-img/loginForm인증.PNG)

## Logout 처리, LogoutFilter

로그아웃이 일어날 경우 세션, 인증토큰, 쿠키정보를 삭제하며 로그인 페이지로 이동한다.

~~~
http.logout()                                           // 로그아웃 처리
    .logoutUrl("/logout")                               // 로그아웃 처리 URL
    .logoutSuccessUrl("/login")                         // 로그아웃 성공 후 이동 페이지
    .deleteCookies("JSESSIONIN", "remember-me")         // 로그아웃 후 쿠키 삭제
    .addLogouthandler(logouthandler())                  // 로그아웃 핸들러
    .logoutSuccessHandler(logoutSuccessHandler())       // 로그아웃 성공 후 핸들러
~~~
deleteCookies로 특정 쿠키를 삭제할 수 있으며, 만약 추가 작업이 필요하다면 로그아웃 핸들러를 이용하여 작업을 추가할 수 있다.

### 로그아웃 처리
1. LogoutFiter가 POST방식의 로그아웃을 받는다.
2. AntPathRequestMatcher에서 로그아웃을 요청하는건지 검사를 한다.
    - 미 일치시 chain.doFiter 그다음 필터로 보낸다
3. 일치 시 Authentication 에서 securityContext 에서 인증 객체를 꺼내온다.
4. SecurityContextLogoutHandler 에서 세션 무효화, 쿠키 삭제, securityContext.clearContext() 컨텍스트에서 정보를 삭제한다.
5. 로그아웃이 성공적 으로 끝날 경우 SimpleUrlLogoutSuccessHandler 에서 다시 login 페이지로 이동시킨다.

![loginForm](/resources/md-img/logout.PNG)

## Remember Me 인증
- 세션이 만료되고 웹 브라우저가 종료된 이후에도 애플리케이션이 사용자를 기억하는 기능
- RememberMe 쿠기에 대한 Http 요청을 확인 후 토큰 기반 인증을 사용해 유효성을 검사하고 토근이 검증되면 로그인이 된다.
- 사용자 라이프 사이클
    1. 인증 성공 (Rembmer-Me 쿠키 설정)
    2. 인증 실패(쿠키가 존재하면 쿠키 무효화)
    3. 로그아웃(쿠키가 존재하면 쿠키 무효화)

~~~
htpp.rememberMe()
    .rememberMeParameter("remember")        // 기본 파라미터명은 remember-me
    .tokenValiditySeconds(3600)             // 만료시간 default 는 14일
    .alwaysRemember(true)                   // 리멤버 기능이 활성화 되지 않아도 항상 실행 default 는 false
    .userDetailsService(userDetailsService) // remember-me 인증 후 처리할 서비스
~~~
**JSESSION을 삭제하더라도 Security에서 쿠키가 있는지 확인 후 있을경우 user객체를 얻어 그 객체로 로그인을 시도한다.** 

## RememberMeAuthenticationFilter

### Remember Me 인증 절차
1. 사용자의 세션이 있는지 필터를 통하여 검증
	세션이 없을 시 RememberMeAuthenticationFilter 가 사용자의 인증을 다시 받도록 시도한다.
2. 사용자의 재 인증 과정은 PersistentTokenBasedRememberMeService 가 사용자 토큰과 DB에 저장된 토큰이 일치한지 확인을 한다.
3. 토큰이 일치할 경우 토큰에서 쿠키를 추출한다.
4. 추출한 토큰이 rememberBe 인지 확인한다.
	미 일치 시 다른 필터로 이동
5. 토큰의 형식이 일치한지 확인한다.
	미 정상 시 예외를 발생시킨다.
6. 사용자의 토큰과 서버에 저장된 토큰이 일치한지 확인한다.
	미 일치 시 예외를 발생시킨다.
7. 토큰의 User 계정이랑 DB 에 저장된 User 계정이랑 동일한지 확인한다.
	미 일치 시 예외를 발생시킨다.
8. 전부 검증이 끝날경우 새로운 Authentication 객체를 만들어 AuthenticationManager 에게 전달한다.
9. 이후 JSESSION 을 재발급하며 사용자 정보를 담는다.
![loginForm](/resources/md-img/rememberMe인증.PNG)

