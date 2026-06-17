CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO _user(dtype,id,create_at,email,firstname,lastname,password,role,username) 
SELECT 'SecurityUser',uuid_generate_v1(),now(),'admin@admin.com','admin','admin','$2a$10$0hcG7w1vstFYfSwJ7G5sjeJomt6x9typiDI0uFJrCJfnl4FrQ6/CK','ADMIN','admin'
WHERE
NOT EXISTS (
SELECT username FROM _user WHERE username = 'admin'
);