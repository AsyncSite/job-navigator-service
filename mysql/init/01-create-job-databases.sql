-- Job Navigator Service 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS job_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 테스트 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS job_db_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 권한 설정
GRANT ALL PRIVILEGES ON job_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON job_db_test.* TO 'root'@'%';
FLUSH PRIVILEGES;