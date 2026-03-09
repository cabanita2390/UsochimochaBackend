UPDATE users 
SET password = '$2a$10$j1Mqyf09ftXBGhbXzywaCeN5cXWSypd0NgxnBCAdmvfUrdCRMpDAO',
    role = 'ADMIN',
    status = true
WHERE username = 'admin';

SELECT id, username, role, status, LEFT(password, 20) as hash_inicio FROM users ORDER BY id;
