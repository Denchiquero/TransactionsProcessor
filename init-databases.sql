-- init-databases.sql
-- Гарантированно создаем роль postgres если не существует
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'postgres') THEN
        CREATE USER postgres WITH SUPERUSER PASSWORD 'admin';
    END IF;
END
$$;

-- Создаем БД для payment-service
SELECT 'CREATE DATABASE payment_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_service')\gexec

-- Создаем БД для order-service  
SELECT 'CREATE DATABASE order_service'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'order_service')\gexec

-- Выдаем права (с задержкой для гарантии)
DO $$
BEGIN
    PERFORM pg_sleep(2); -- Ждем 2 секунды для создания роли
    
    -- Пробуем выдать права несколько раз
    FOR i IN 1..5 LOOP
        BEGIN
            GRANT ALL PRIVILEGES ON DATABASE payment_service TO postgres;
            GRANT ALL PRIVILEGES ON DATABASE order_service TO postgres;
            EXIT;
        EXCEPTION
            WHEN OTHERS THEN
                PERFORM pg_sleep(1);
        END;
    END LOOP;
END
$$;