# Spring Security
- - -

## 01. 인증 API - 스프링 시큐리티 의존성 추가
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

## WebSecurityConfigurerAdapter
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

## 인증 API - Form 인증
1. 서버 접근의 경우 인증받은 사용자만 접근할 수 있기에 인증되지 않은사용자가 방문한 경우 로그인 페이지로 이동한다.
2. POST방식의 form data : username + password를 이용하여 로그인을 시도한다.
3. 로그인 성공 시 SESSION 및 인증 토큰을 생성 및 저장한다. 
4. 사용자가 자원에 재 접근할 경우 세션에 저장된 인증 토큰을 확인하여 인증을 유지한다.

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