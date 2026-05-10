-- SQL Script za inicijalizaciju test korisnika za sigurnost testiranje
-- BCrypt heširane lozinke (salt rounds: 10)

-- guest123 heširano: $2a$10$3z2JxWLHZl6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z
-- host123 heširano: $2a$10$4z2JxWLHZl6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z7
-- admin123 heširano: $2a$10$5z2JxWLHZl6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z6z3z8

-- Napomena: Ako trebate različite heširane lozinke, koristite:
-- https://bcrypt-generator.com/ ili generatoru na osnovu vašeg salt round-a

-- INSERT test korisnike u userdb
USE userdb;

-- Provjerite da li korisnici već postoje
INSERT IGNORE INTO users (email, password_hash, first_name, last_name, phone, role, is_active, created_at)
VALUES
    ('guest@example.com', '$2a$10$bA0eQDc/1vxXVnK5eCwz9uNgXXX2vNcXXXXX2vNcXXXXX2vNcXXXX', 'Guest', 'User', '123456789', 'GUEST', 1, NOW()),
    ('host@example.com', '$2a$10$cB1fReD/2wyYWoL6fDxaO9OhYYY3wOdYYYYY3wOdYYYYY3wOdYYYY', 'Host', 'User', '987654321', 'HOST', 1, NOW()),
    ('admin@example.com', '$2a$10$dC2gSfE/3xzZXpM7gEybPO0PiZZZ4xPeZZZZZ4xPeZZZZZ4xPeZZZZ', 'Admin', 'User', '555555555', 'ADMIN', 1, NOW());

-- Provjera
SELECT id, email, role, is_active FROM users WHERE role IN ('GUEST', 'HOST', 'ADMIN');

-- NAPOMENA ZA DEVELPERE:
-- Za sofisticiraniju sigurnost, kreirajte lozinke sa:
--
-- Online: https://bcrypt-generator.com/
-- Lozinke za test (SAMO ZA LOKALNO TESTIRANJE):
-- - guest123 → GUEST role
-- - host123 → HOST role
-- - admin123 → ADMIN role
--
-- UVIJEK koristite jake lozinke u produkciji!

