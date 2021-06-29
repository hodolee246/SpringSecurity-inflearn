# 스프링 시큐리티 주요 아키텍처 이해
## DelegatingFilterProxy, FIlterChainProxy

![delegating_filter_proxy, filter_chain_proxy](../md-img/section2/delegating_filter_proxy,filter_chain_proxy.PNG)

### DelegatingFilterProxy

1. 서블릿 필터는 스프링에서 정의된 빈을 주입해서 사용할 수 없음
2. 특정한 이름을 가진 스프링 빈을 찾아 그 빈에게 요청을 위임
    - springSecurityFilterChain 이름으로 생성된 빈을 ApplicationContext 에서 찾아 요청을 위임함
    - 실제 보안처리를 하지 않음

### FilterChainProxy

1. springSecurityChain 의 이름으로 생성되는 필터 빈
2. DelegatingFIlterProxy 으로 부터 요청을 위임 받고 실제 보안을 처리
3. 스프링 시큐리티 초기화 시 생성되는 필터들을 관리하고 제어
    - 스프링 시큐리티가 기본적으로 생성하는 필터
    - 설정 클래스에서 API 추가 시 생성되는 필터
4. 사용자의 요청을 필터 순서대로 호출하여 전달
5. 사용자정의 필터를 생성해서 기존의 필터 전.후로 추가 가능
    - 필터의 순서를 잘 정의
6. 마지막 필터까지 인증 및 인가 예외가 발생하지 않으며 보안 통과

## 필터 초기화와 다중 보안 설정

![필터초기화_다중설정_클래스](../md-img/section2/필터초기화_다중설정_클래스.PNG)

- 설정클래스 별로 보안 기능이 각각 작동
- 설정 클래스 별로 RequestMatcher 설정
    - http.antMatcher("/admin/**)
- 설정클래스 별로 필터가 생성
- FilterChainProxy 가 각 필터를 가지고 있음

![필터초기화_다중설정_클래스2](../md-img/section2/필터초기화_다중설정_클래스2.PNG)

- 요청에 따라 RequestMatcher와 매핑되는 필터가 작동하도록 함
    - FilterChainProxy 객체가 각각 SecurityFilterChain 을 List 형태로 저장하고 있음
    - 사용자 요청에 맞는 필터를 선택해 인증/인가가 이루어 진다.
        - Config가 여러개면 @Order(n) 어노테이션을 이용해 어떤 Config를 우선적으로 필터를 확인할지 정할 수 있다. 
    
## Authentication

당신이 누구인지 증명하는 것
    - 사용자의 인증 정보를 저장하는 토큰 개념
    - 인증 시 id 와 password 를 담고 인증 검증을 위해 전달되어 사용된다.
    - 인증 후 최종 인증 결과 (user 객체, 권한정보) 를 담고 SecurityContext 에 저장되어 전역적으로 참조가 가능하다.
        - Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
    - 구조
        1. principal : 사용자 아이디 혹은 User 객체를 저장
        2. credentials : 사용자 비밀번호
        3. authorities : 인증된 사용자의 권한 목록
        4. details : 인증 부가 정보
        5. Authenticated : 인증 여부ㅁ

### Authentication 인증 구조

![Authentication](../md-img/section2/Authentication.PNG)

1. username + password 를 UsernamePasswordAuthenticationFilter 에게 전달
2. Authentication 인증객체를 생성 후 AuthenticationManager 에게 전달
3. 인증에 성공 시 Authentication 최종 인증객체를 생성 후 SecurityContext 에 저장
    - 실패 시 예외 발생 후 chainFilter...
4. 최종 작업 이후 인증 객체를 전역적으로 사용이 가능해짐

## SecurityContextHolder, SecurityContext

1. SecurityContext
    - **Authentication 객체가 저장되는 보관소**로 필요 시 언제든지 Authentication 객체를 꺼내어 쓸 수 있도록 제공되는 클래스
        - Authentication 은 User 객체가 저장됨
            - User 객체는 인증 성공 시 만들어짐
    - ThreadLocal 에 저장되어 아무 곳에서나 참조가 가능하도록 설계함
        - Thread 마다 고유하게 할당된 로컬이 ThreadLocal 이며, 다른 Thread 가 접근할 수 없다.
    - 사용자의 인증이 완료되면 HttpSession 에 저장되어 애플리케이션 전반에 걸쳐 전역적인 참조가 가능하다.
2. SecurityContextHolder
    - SecurityContext 를 감싸고 있으며, SecurityContext 객체 저장 방식
        - MODE_THREADLOCAL : 스레드당 SecurityContext 객체를 할당 (default)
        - MODE_INHERITABLETHREADLOCAL : 메인 스레드와 자식 스레드에 관하여 동일한 SecurityContext 를 유지
        - MODE_GLOBAL : 응용 프로그램에서 단 하나의 SecurityContext 를 저장한다.
    - SecurityContextHolder.clearContext() : SecurityContext 기존 정보 초기화
3. Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        
### SecurityContextHolder, SecurityContext

![security_context_holder, security_context](../md-img/section2/security_context_holder,security_context.PNG)

1. 로그인 시 Authentication 객체를 만들고 인증여부를 판단한다.
    - 실패 시 clearContext()
    - 성공 시 SecurityContext 안에 있는 SecurityContext 에 Authentication 에 객체 정보를 변경하여 저장하며, 최종적으로 HttpSession 에 저장한다.
    
## SpringSecurityContextPersistentFilter
    
SecurityContext 객체의 생성, 조회, 저장 하는 필터이다.

1. 익명 사용자
    - 새로운 SecurityContext 객체를 생성하여 SecurityContextHolder 에 저장
    - AnonymousAuthenticationFilter 에서 AnonymousAutenticationToken 객체를 SecurityContext 에 저장

2. 인증 시
    - 새로운 SecurityContext 객체를 생성하여 SecurityContextHolder 에 저장
    - UsernamePasswordAuthenticationFilter 에서 인증 성공 후 SecurityContext 에 UsernamePasswordAuthenticationToken 객체를 SecurityContext 에 저장
    - 인증이 최종 완료되면 Session 에 SecurityContext 를 저장

3. 인증 후
    - Session 에서 SecurityContext 꺼내어 SecurityContextHolder 에서 저장
    - SecurityContext 안에 Authentication 객체가 존재하면 계속 인증을 유지한다.

4. 최종 응답 시 공통
    - SecurityContextHolder.clearContext() 메소드를 실행하여 Context 를 삭제한다.

### SecurityContextPersistenceFilter
![security_context_persistence_filter](../md-img/section2/security_context_persistence_filter.PNG)

1. 사용자 요청 시 사용자 요청 인증여부를 확인한다.
2. 인증 전 일 경우 새로운 SecurityContext 를 Holder 에서 생성한다
3. 인증 필터가 인증을 처리하며 SecurityContextHolder 안에서 Authentication 객체를 생성해 context 에 저장한다.
4. doFilter 이후 Session 에 Context 를 저장한다.
5. 앞으로 인증은 Session 을 통해 SecurityContext 가 있는지 확인 후 Session 에서 꺼내 사용한다.

## Authentication Flow

![authentication_flow](../md-img/section2/authentication_flow.PNG)

1. form 인증 로그인 요청 시
2. UsernamePasswordAuthenticationFilter 에서 요청을 받고 Authentication 객체를 생성해 요청에 들어온 id, password 를 담고 AuthenticationManager 에게 보낸다
3. AuthenticationManager 는 인증을 처리할 authenticationProvider 에게 인증을 위임한다.
4. AuthenticationProvider 는 인증 객체를 검증하면서 UserDetailsService 에게 id 를 전달하며 user 객체를 요청한다
5. UserDetailService 는 Repository 에서 유저 객체를 조회 하여 UserDetail 타입으로 반환하거나 예외를 발생시키며 인증을 종료한다.
6. AuthenticationProvider 는 반환받은 객체와 사용자 입력 패스워드가 일치한지 확인 후 Authentication(UserDetails + authorities) 토큰 객체를 AuthenticationManager 에게 전달한다.
7. AuthenticationManager 는 다시 filter 에게 인증객체를 다시 전달한다.
8. filter 는 인증객체를 다시 SecurityContext 에 저장한다.

## AuthenticationManager

- 초기화 시 AuthenticationProvider 를 입력받으며  해당 목록 중에서 인증 처리 요건에 맞는 AuthenticationProvider 를 찾아 인증 처리를 위임한다.
- 부모 ProviderManager 를 설정하여 AuthenticationProvider 를 계속 탐색할 수 있다.

## AuthenticationProvider

![authentication_provider](../md-img/section2/authentication_provider.PNG)

1. authenticate() : 검증 메서드
2. supports() : 검증을 할 수 있는지 여부를 판단하는 메서드

- authenticate
    - ID 검증
        - UserDetailService : 인증을 시도하는 사용자 계정이 디비에 있는지 확인 후 UserDetails 타입으로 반환 및 예외를 발생 시킨다.
    - password 검증
        - ID 검증을 통과하여 받은 UserDetails 타입의 객체의 비밀번호와 사용자가 입력한 비밀번호가 일치한지 확인 및 예외를 일으킨다. (비밀번호는 passwordEncoder 에서 동일한지 비교한다.)
    - 추가 검증
        -  모든 인증이 통과할 경우 Authentication 객체를 생성해 user 객체와, authorities 객체를 담아 AuthenticationManager 에게 반환한다.
        
## Authorization, FilterSecurityInterceptor
    
### Authorization
당신에게 무엇이 허가 되었는지 증명하는거

- 스프링 시큐리티가 지원하는 권한 계층
    - 웹 계층 : URL 요청에 따른 메뉴 혹은 화면단위의 레벨 보안
    - 서비스 계층 : 화면 단위가 아닌 메소드 같은 기능 단위의 레벨 보안
    - 도메인 계층 : 객체 단위의 레벨 보안
        
### FilterSecurityInterceptor
- 마지막에 위치한 필터로써 인증된 사용자에 대하여 특정 요청의 승인/거부 여부를 최종적으로 결정
- 인증객체 없이 보호차원에 접근을 시도할 경우 AuthenticationException 을 발생
- 인증 후 자원에 접근 가능한 권한이 존재하지 않을 경우 AccessDeniedException 을 발생
- 권한 제어 방식 중 HTTP 자원의 보안을 처리하는 필터
- 권한 처리를 AccessDecisionmanager 에게 맡김            

![filter_security_interceptor](../md-img/section2/filter_security_interceptor.PNG)
   
![filter_security_interceptor2](../md-img/section2/filter_security_interceptor2.PNG)
