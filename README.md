# 🚀 Project Name

> 회원 서비스

---

## 🛠️ Tech Stack

<p align="left">
  <img src="https://img.shields.io/badge/Java%2024-007396?style=flat-square&logo=java&logoColor=white" alt="Java 24">
  <img src="https://img.shields.io/badge/Spring_Boot%203.5.4-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot 3.5.4">
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring-data-jpa&logoColor=white" alt="Spring Data JPA">
  <img src="https://img.shields.io/badge/MySQL%208.0.33-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL 8.0.33">
  <img src="https://img.shields.io/badge/QueryDSL%205.1.0-C6790C?style=flat-square&logo=querydsl&logoColor=white" alt="QueryDSL 5.1.0">
  <img src="https://img.shields.io/badge/Lombok%201.18.38-ec5300?style=flat-square&logo=lombok&logoColor=white" alt="Lombok 1.18.38">
  <img src="https://img.shields.io/badge/MapStruct%201.6.3-E44D26?style=flat-square&logo=apache&logoColor=white" alt="MapStruct 1.6.3">
  <img src="https://img.shields.io/badge/Bucket4j%208.14.0-F05032?style=flat-square&logo=apple&logoColor=white" alt="Bucket4j 8.14.0">
  <img src="https://img.shields.io/badge/JJWT%200.11.5-000000?style=flat-square&logo=json-web-tokens&logoColor=white" alt="JJWT 0.11.5">
  <img src="https://img.shields.io/badge/OpenAPI%202.8.9-6BA539?style=flat-square&logo=openapi&logoColor=white" alt="OpenAPI 2.8.9">
</p>

---

## 🗄️ Database Setup

프로젝트를 실행하기 전에 **Docker를 사용하여 MySQL 데이터베이스를 설정**해야 합니다.

1.  **MySQL Docker 이미지 다운로드**:
    ```bash
    docker pull mysql:latest
    ```
2.  **MySQL 컨테이너 실행**:
    데이터베이스 이름, 사용자, 비밀번호는 프로젝트의 `application.properties` 또는 `application.yml` 설정과 일치해야 합니다. (기본값: 데이터베이스 `member`, 사용자 `user`, 비밀번호 `password`)
    ```bash
    docker run -d -p 3306:3306 --name mysql-container \
      -e MYSQL_ROOT_PASSWORD=rootpassword \
      -e MYSQL_DATABASE=member \
      -e MYSQL_USER=user \
      -e MYSQL_PASSWORD=password \
      mysql:latest
    ```
---

## ⚙️ How to Run

1.  **클론**
    ```bash
    git clone https://github.com/HamTaehoon/member.git
    ```
2.  **의존성 설치**
    ```bash
    ./gradlew clean build
    ```
3.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```
    또는 빌드된 JAR 파일을 실행합니다.
    ```bash
    java -jar build/libs/member-0.0.1-SNAPSHOT.jar
    ```

---

## 📝 API Documentation

프로젝트 실행 후 다음 URL에서 OpenAPI (Swagger UI) 문서를 확인할 수 있습니다:

* `http://localhost:8080/swagger-ui.html`

---

## 💡 API 테스트 시나리오

API 테스트 시나리오를 설명합니다. 
각 시나리오에는 **HTTP 메서드**, **엔드포인트**, **요청 본문(해당하는 경우)**, 그리고 **예상/실제 응답**이 포함됩니다.

---

### 1. 회원가입 API

새로운 회원을 시스템에 등록합니다.

* **엔드포인트**: `POST /v1/api/members`
* **요청 본문**:
    ```json
    {
      "account": "thham",
      "password": "password1!",
      "name": "Taehoon Ham",
      "residentId": "1234567890123",
      "phoneNumber": "01012345678",
      "address": "서울특별시 OO구 OO로"
    }
    ```
* **성공 응답 (200 OK)**:
    ```json
    {
      "account": "thham"
    }
    ```

---

### 2. 회원 리스트 조회 API (관리자 권한)

모든 회원의 페이지네이션된 목록을 조회합니다. 이 API는 **ADMIN** 역할 인증이 필요합니다.

#### 2.1. 회원 리스트 조회 (권한 없음)

올바른 인증 없이 접근을 시도합니다.

* **엔드포인트**: `GET /v1/api/admin/members?page=0&size=10`
* **헤더**:
    * `Accept: */*`
* **예상 에러 응답 (401 Unauthorized)**:
    ```json
    {
      "code": 401,
      "success": false,
      "message": "Invalid or missing credentials"
    }
    ```

#### 2.2. 회원 리스트 조회 (권한 있음)

**ADMIN**으로 Basic 인증을 사용하여 접근합니다. (예시: `admin:1212`는 `YWRtaW46MTIxMg==`로 인코딩됩니다.)

* **엔드포인트**: `GET /v1/api/admin/members?page=0&size=10`
* **헤더**:
    * `Accept: */*`
    * `Authorization: Basic YWRtaW46MTIxMg==`
* **성공 응답 (200 OK)**:
    ```json
    {
      "content": [
        {
          "account": "thham",
          "name": "Taehoon Ham",
          "phoneNumber": "01012345678",
          "address": "서울특별시 OO구 OO로"
        }
      ],
      "page": {
        "size": 10,
        "number": 0,
        "totalElements": 1,
        "totalPages": 1
      }
    }
    ```

---

### 3. 회원 정보 수정 API (관리자 권한)

ID를 통해 특정 회원의 주소 및/또는 비밀번호를 업데이트합니다. 이 API는 **ADMIN** 역할 인증이 필요합니다.

#### 3.1. 회원 주소 수정

* **엔드포인트**: `PATCH /v1/api/admin/members/{id}` (예시: `/v1/api/admin/members/1`)
* **헤더**:
    * `Accept: */*`
    * `Authorization: Basic YWRtaW46MTIxMg==`
    * `Content-Type: application/json`
* **요청 본문**:
    ```json
    {
      "password": "newPassword1!",
      "address": "서울특별시 새주소로"
    }
    ```
* **성공 응답 (200 OK)**:
    ```json
    {
      "account": "thham",
      "name": "Taehoon Ham",
      "phoneNumber": "01012345678",
      "address": "서울특별시 새주소로"
    }
    ```

#### 3.2. 회원 비밀번호 수정 (관리자)

특정 회원의 비밀번호만 업데이트합니다.

* **엔드포인트**: `PATCH /v1/api/admin/members/{id}` (예시: `/v1/api/admin/members/1`)
* **헤더**:
    * `Accept: */*`
    * `Authorization: Basic YWRtaW46MTIxMg==`
    * `Content-Type: application/json`
* **요청 본문**:
    ```json
    {
      "password": "newpassword123",
      "address": "서울특별시 새주소로"
    }
    ```
* **성공 응답 (200 OK)**:
    ```json
    {
      "account": "thham",
      "name": "Taehoon Ham",
      "phoneNumber": "01012345678",
      "address": "서울특별시 새주소로"
    }
    ```

---

### 4. 회원 삭제 API

ID를 통해 특정 회원을 삭제합니다. 이 API는 **ADMIN** 역할 인증이 필요합니다.

* **엔드포인트**: `DELETE /v1/api/admin/members/{id}` (예시: `/v1/api/admin/members/1`)
* **헤더**:
    * `Accept: */*`
    * `Authorization: Basic YWRtaW46MTIxMg==`
* **성공 응답 (204 No Content)**:
  *(응답 본문 없음)*

---

### 5. 회원 로그인 API

회원을 인증하고 JWT 액세스 토큰을 발급합니다.

#### 5.1. 로그인 (성공)

* **엔드포인트**: `POST /v1/api/login`
* **요청 본문**:
    ```json
    {
      "account": "thham",
      "password": "password1!"
    }
    ```
* **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "message": "Login successful"
    }
    ```
* **응답 헤더**:
    * `access-token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGhhbSIsImlhdCI6MTc1MzYwMjAxNCwiZXhwIjoxNzUzNjAzODE0fQ.Iyx9ZX6GgHpmazWAVlnwJZl4weGNB1g5D9doXZbpD-A` (예시 JWT)

#### 5.2. 로그인 (실패 - 비밀번호 불일치)

잘못된 비밀번호로 로그인을 시도합니다.

* **엔드포인트**: `POST /v1/api/login`
* **요청 본문**:
    ```json
    {
      "account": "thham",
      "password": "password1!"
    }
    ```
* **예상 에러 응답 (401 Unauthorized)**:
    ```json
    {
      "error": "Invalid account or password"
    }
    ```

#### 5.3. 로그인 (성공 - 변경된 비밀번호로)

비밀번호 업데이트 후, 새 비밀번호로 다시 로그인합니다.

* **엔드포인트**: `POST /v1/api/login`
* **요청 본문**:
    ```json
    {
      "account": "thham",
      "password": "newpassword123"
    }
    ```
* **성공 응답 (200 OK)**:
    ```json
    {
      "success": true,
      "message": "Login successful"
    }
    ```
* **응답 헤더**:
    * `access-token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGhhbSIsImlhdCI6MTc1MzYwMjE2NCwiZXhwIjoxNzUzNjAzOTY0fQ.VI45ir7iut_cJH6xHxqhGZ_Hcz4-Zg4fYmcjgLm9Wg0` (예시 JWT)

---

### 6. 회원 상세 조회 API (사용자 권한)

특정 회원의 상세 정보를 조회합니다. 이 API는 JWT를 사용한 **USER** 역할 인증이 필요합니다.

#### 6.1. 회원 상세 조회 (정상 토큰)

* **엔드포인트**: `GET /v1/api/members/{account}` (예시: `/v1/api/members/thham`)
* **헤더**:
    * `Accept: */*`
    * `Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGhhbSIsImlhdCI6MTc1MzYwMjE2NCwiZXhwIjoxNzUzNjAzOTY0fQ.VI45ir7iut_cJH6xHxqhGZ_Hcz4-Zg4fYmcjgLm9Wg0` (성공적인 로그인에서 얻은 예시 JWT)
* **성공 응답 (200 OK)**:
    ```json
    {
      "account": "thham",
      "name": "Taehoon Ham",
      "phoneNumber": "01012345678",
      "address": "서울특별시"
    }
    ```

#### 6.2. 회원 상세 조회 (비정상 토큰)

유효하지 않은 JWT로 접근을 시도합니다.

* **엔드포인트**: `GET /v1/api/members/{account}` (예시: `/v1/api/members/thham`)
* **헤더**:
    * `Accept: */*`
    * `Authorization: Bearer asdasd`
* **예상 에러 응답 (401 Unauthorized)**:
    ```json
    {
      "code": 401,
      "success": false,
      "message": "Invalid or expired JWT token"
    }
    ```

#### 6.3. 회원 상세 조회 (토큰 없음)

JWT를 제공하지 않고 접근을 시도합니다.

* **엔드포인트**: `GET /v1/api/members/{account}` (예시: `/v1/api/members/thham`)
* **헤더**:
    * `Accept: */*`
* **예상 에러 응답 (401 Unauthorized)**:
    ```json
    {
      "code": 401,
      "success": false,
      "message": "Invalid or missing JWT token"
    }
    ```

---

### 7. 메시지 발송 API (관리자 권한)

메시지(예: 프로모션 안내)를 발송합니다. 이 API는 **ADMIN** 역할 인증이 필요합니다.

* **엔드포인트**: `POST /v1/api/admin/messages`
* **헤더**:
    * `Accept: */*`
    * `Authorization: Basic YWRtaW46MTIxMg==`
    * `Content-Type: application/json`
* **요청 본문**:
    ```json
    {
      "message": "프로모션 안내드립니다."
    }
    ```
* **성공 응답 (200 OK)**:
    ```json
    {
      "id": 1,
      "status": "PENDING"
    }
    ```

---