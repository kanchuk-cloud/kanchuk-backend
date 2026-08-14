-- Fix admin password hash (bcrypt of Admin@123)
UPDATE admin_users
SET password_hash = '$2b$12$9eK6aMy2DN6YxdD3IEqHoeUg3YzJ33w1gT1DvxQMidrVGZgBdwgI.'
WHERE email = 'admin@kanchuk.in';
