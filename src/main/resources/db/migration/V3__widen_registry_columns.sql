-- 등기부등본 파싱 결과를 저장할 때 실제 표기 폭을 넘겨 저장이 실패하던 컬럼을 넓힌다.
--   share   : 집합건물 대지권 지분은 분모가 커서 '3567/100000'(11자) 형태가 나온다.
--   rank_no : 부기등기가 '2번의부기1호' 처럼 표기된다.
--   debtor  : 채무자가 법인이면 상호가 길다.
--
-- 이 테이블들은 Hibernate(ddl-auto=update)가 생성하고 Flyway가 그보다 먼저 실행되므로,
-- 신규 환경에서는 아직 테이블이 없다. IF EXISTS로 건너뛰게 하고,
-- 신규 환경은 엔티티에 정의된 길이로 Hibernate가 생성하도록 둔다.

ALTER TABLE IF EXISTS registry_owners
    ALTER COLUMN share TYPE varchar(30);

ALTER TABLE IF EXISTS registry_rights
    ALTER COLUMN rank_no TYPE varchar(20);

ALTER TABLE IF EXISTS registry_rights
    ALTER COLUMN debtor TYPE varchar(100);
