# monghyang-backend
양조장 체험 예약부터 전통주 구매까지 가능한 통합 플랫폼 서비스
> **개발기간:**

---
## ✍🏻 규칙
> **0. ddl-auto 옵션** <br> - 로컬 개발 환경: create, update <br> - 배포 환경: validate, none <br>
> **1. GIT 커밋 메시지 규칙** : [참고 블로그 링크](https://velog.io/@chojs28/Git-%EC%BB%A4%EB%B0%8B-%EB%A9%94%EC%8B%9C%EC%A7%80-%EA%B7%9C%EC%B9%99) <br>
> **2. 변수/메소드 네이밍 규칙** : 카멜 표기(구분되는 글자마다 첫글자를 대문자로 작성, ex: veryFunnyCoding) <br>
> **3. 로컬 데이터베이스 연결** : application-local.yml의 datasource란을 자신의 로컬 환경에 맞게 수정 <br>
> **4. 필드 타입** : boolean을 제외한 모든 필드 타입은 wrapper 객체 타입을 사용합니다. <br>
> **5. 날짜 객체 타입** : LocalDate(yyyy-mm-dd), LocalDateTime(밀리초 단위까지 표현)만 사용 <br>
> **6. 개발 시 기본키 네이밍 규칙** : 객체 기본키 필드 이름은 'id'로 하되, @Column(name = "ERDCloud상 기본키 이름") 설정 // 개발 편의성 위함 <br>
> **7. Setter 생성 규칙** : 기본키 필드의 Setter는 생성하지 않습니다. <br>
> **8. 연관관계 설정** : 모든 연관관계(@ManyToOne 등)는 지연로딩(LAZY) 전략을 사용합니다.
## ▶️ 실행 가이드
### IntelliJ 환경 변수 주입
1. 팀원으로부터 환경변수 파일(setting.env) 전달받기
2. 환경변수 파일을 ./src/main/resources 디렉토리에 넣기(위치 중요)
3. IDE를 통한 로컬 빌드 시 application-local.yml의 datasource 수정하기
4. 빌드
### JVM: 프로파일 지정하여 jar 파일 실행 예시
- !!주의사항!! 'setting.env', jar 파일은 **반드시 동일한 경로**에 위치해야 합니다.
```bash
java -Dspring.profiles.active=[프로파일명] -jar [jar파일명].jar &
```

---
## 📚 STACKS

<div>
  <img src="https://img.shields.io/badge/Java%2021-007396?style=for-the-badge&logo=Java&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot%203.5.3-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white" alt="Spring Boot 3.5.3"><br>
  <img src="https://img.shields.io/badge/MariaDB%2010-003545?style=for-the-badge&logo=MariaDB&logoColor=white" alt="MariaDB 10"><br>
  <img src="https://img.shields.io/badge/Nginx%20-339900?style=for-the-badge&logo=Nginx&logoColor=white" alt="nginx"><br>
  <img src="https://img.shields.io/badge/Aws%20-A22846?style=for-the-badge&logo=Aws&logoColor=white" alt="Aws">
</div>

### STACK VERSION

- Spring Boot 3.5.3
- Spring Dependency Management 1.1.7
---
## 📝 API 명세서 및 ERD
> **ERD** : [ERD Cloud](https://www.erdcloud.com/d/XQDRHTfgwSz4uP8mv) <br>

---
## 📎 패키지 구조
> **도메인 위주 설계** : 패키지명 첫글자는 소문자
### 예시
```bash
.
├── java
│   └── com
│       └── example
│           └── monghyang
│               └── MonghyangApplication.java # Spring Application 파일(main 메소드 느낌)
│               └── domain
│                   ├── util # 기능 모듈 패키지
│                   │   └── ...
│                   ├── config # 각종 설정 파일 패키지
│                   │   └── ...
│                   ├── global # 전역 제어 파일 패키지(ex: 전역 예외 처리 등)
│                   │   └── ...
│                   ├── 특정 도메인 # 도메인(Entity)마다 아래의 구조를 기본적으로 가집니다.
│                   │   ├── dto # 데이터 전달용 객체 패키지
│                   │   │   ├── ... # (Getter/Setter 메소드만 존재)
│                   │   ├── controller # 컨트롤러 계층 패키지
│                   │   │   └── ...
│                   │   ├── entity # 엔티티 계층 패키지
│                   │   │   └── ...
│                   │   ├── repository # 레포지토리 계층 패키지(JpaRepository 인터페이스 상속)
│                   │   │   └── ...
│                   │   └── service # 서비스 계층 패키지
│                   │       └── ...
└── resources
    ├── application.yml # 기본 프로파일, 추후 환경 별 프로파일 분리 예정(local, dev, prod etc..)
    ├── static
    └── templates
```
### ▶️ 전체 패키지 구조
```bash
.
├── java
│   └── com
│       └── example
│           └── monghyang
│               ├── MonghyangApplication.java
│               └── domain
│                   ├── admin
│                   │   ├── controller
│                   │   ├── dto
│                   │   └── service
│                   ├── brewery # 양조장은 양조장, 체험프로그램으로 하위 패키지 분리
│                   │   ├── joy # 체험 프로그램
│                   │   │   ├── controller
│                   │   │   ├── dto
│                   │   │   ├── entity
│                   │   │   ├── repository
│                   │   │   └── service
│                   │   └── main # 양조장
│                   │       ├── controller
│                   │       ├── dto
│                   │       ├── entity
│                   │       ├── repository
│                   │       └── service
│                   ├── config
│                   ├── follow
│                   │   ├── controller
│                   │   ├── dto
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── service
│                   ├── global
│                   ├── product
│                   │   ├── controller
│                   │   ├── dto
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── service
│                   ├── qna
│                   │   ├── controller
│                   │   ├── dto
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── service
│                   ├── seller
│                   │   ├── controller
│                   │   ├── dto
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── service
│                   ├── users
│                   │   ├── controller
│                   │   ├── dto
│                   │   ├── entity
│                   │   ├── repository
│                   │   └── service
│                   └── util
└── resources
    ├── application-local.yml # BE 로컬 개발용 프로파일(로컬DB)
    ├── application-prod.yml # 배포: 정식 배포용 프로파일
    ├── application-test.yml # 배포: FE API 테스트용 프로파일
    ├── application.yml
    ├── setting.env # 환경변수 파일(커밋 절대 금지)
    ├── static
    └── templates
```