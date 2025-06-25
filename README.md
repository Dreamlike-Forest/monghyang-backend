# monghyang-backend
양조장 체험 예약부터 전통주 구매까지 가능한 통합 플랫폼 서비스
> **개발기간:**

---
## ✍🏻 규칙
> **GIT 커밋 메시지 규칙** : [참고 블로그 링크](https://velog.io/@chojs28/Git-%EC%BB%A4%EB%B0%8B-%EB%A9%94%EC%8B%9C%EC%A7%80-%EA%B7%9C%EC%B9%99) <br>
> **변수/메소드 네이밍 규칙** : 카멜 표기(구분되는 글자마다 첫글자를 대문자로 작성, ex: veryFunnyCoding)

---
## 패키지 구조
> **도메인 위주 설계** : 패키지명 첫글자는 소문자
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