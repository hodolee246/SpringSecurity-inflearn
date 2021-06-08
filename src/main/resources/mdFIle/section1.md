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
1. 서버가 기동되면 ㅅ프링 시큐리티의 초기화 작업 및 보안 설정이 이루어진다.
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