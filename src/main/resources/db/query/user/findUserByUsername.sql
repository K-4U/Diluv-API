SELECT id,
       username,
       email,
       password,
       password_type,
       mfa,
       mfa_secret,
       avatar_url,
       created_at
FROM users
WHERE username=?
LIMIT 1