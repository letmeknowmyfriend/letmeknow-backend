SET REFERENTIAL_INTEGRITY FALSE; --제약조건 무효화

TRUNCATE TABLE member RESTART IDENTITY;
TRUNCATE TABLE stores RESTART IDENTITY;
TRUNCATE TABLE oauth2 RESTART IDENTITY;

SET REFERENTIAL_INTEGRITY TRUE; --제약조건 재설정