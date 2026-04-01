# After-Buy Admin Service (관리자 서비스)

After-Buy의 관리자 웹 프론트엔드와 연동되어 인증, 공지사항, FAQ, 에러 로그 등의 관리자 도메인 기능을 담당하는 Spring Boot 마이크로서비스입니다.

---

## 🚀 로컬 서버 실행 방법

이 프로젝트는 Gradle을 기반으로 합니다. 수월한 프론트엔드 연동 테스트를 위해 다음 방법으로 로컬 서버를 실행할 수 있습니다.

### 1. 환경 변수(`.env`) 설정
프로젝트 루트 경로(`after-buy-admin-service`)에 있는 `.env_example` 파일을 참고하여 **`.env`** 파일을 생성하고 아래와 같이 로컬 환경에 맞게 값을 채워 넣어야 합니다.

```env
# Database Settings (로컬 MySQL)
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=admin_db
DB_USERNAME=root
DB_PASSWORD=본인의_로컬_DB_비밀번호

# MSA Internal Auth Header Secret (테스트용 임의 문자열)
INTERNAL_SECRET_KEY=test_secret_key

# 관리자 웹사이트 프론트엔드 URL (Vite 기본 포트 사용 시 5173)
ADMIN_FRONTEND_URL=http://localhost:5173
```
- **DB_PASSWORD**: 로컬 MySQL의 실제 비밀번호를 입력해 주세요.
- **ADMIN_FRONTEND_URL**: 뷰(React/Vite)가 실행 중인 주소를 입력합니다. (예: `http://localhost:5173`) CORS 정책 허용에 사용됩니다.

### 2. 포트 및 DB 스키마 확인
- **포트**: `8084` (기본 설정)
- **DB**: 위 `.env`에서 지정한 대로 `admin_db` (MySQL 8.0) 스키마가 로컬에 생성되어 있어야 합니다.

### 3. 실행 명령어 (터미널)
프로젝트 루트 디렉토리(`after-buy-admin-service`)에서 아래 명령어를 실행합니다.

**Windows 환경**
```bash
gradlew.bat bootRun
```

**Mac/Linux 환경**
```bash
./gradlew bootRun
```

*IDE(예: IntelliJ IDEA)를 사용할 경우, `AdminServiceApplication.java` 클래스에서 `Run` 버튼을 클릭하여 바로 실행하는 것이 가장 수월합니다.*

---

## 🛠 더미 테스트 데이터 (관리자 계정 추가)

프론트엔드 로그인 기능 연동 테스트를 위해 `admin_db` 스키마 내 `admins` 테이블에 테스트 계정을 추가하는 SQL 스크립트입니다. 
관리자 서비스는 `BCrypt` 기반 비밀번호 암호화를 사용하므로, 평문을 직접 넣으면 로그인이 실패합니다.

아래 SQL을 MySQL Workbench나 터미널에서 실행하여 테스트 계정을 생성하세요.

```sql
USE admin_db;

-- --------------------------------------------------------
-- 테스트 관리자 계정 정보
-- 아이디: testadmin
-- 비밀번호: password
-- --------------------------------------------------------
INSERT INTO admins (
    admin_account, 
    password_hash, 
    login_fail_count, 
    is_locked, 
    created_at
) VALUES (
    'testadmin', 
    -- 'password' 의 BCrypt 해시값
    '$2a$10$rJ6aQ./.1p.Uf5E3AQQ0Ou.P3qP.wQoNw6aB8b/H8j/v6A1t8yD0S',
    0, 
    0, 
    NOW()
);
```

### 💡 기타 데이터 초기화 팁
로그인 후 대시보드 조회를 테스트하려면 더미 `faqs`나 `announcements` 데이터도 1~2건 추가해 두는 것이 프론트엔드 화면 구성 확인에 큰 도움이 됩니다.

```sql
INSERT INTO announcements (title, category, content, is_pinned, created_by, created_at, updated_at)
VALUES ('관리자 웹 오픈 안내', 'NOTICE', '프론트엔드 테스트를 위한 공지사항입니다.', 1, 1, NOW(), NOW());
```
