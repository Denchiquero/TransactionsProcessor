@echo off
echo ========================================
echo    Starting Microservices with Databases
echo ========================================

echo.
echo [1/5] Stopping existing containers...
docker-compose down

echo.
echo [2/5] Starting PostgreSQL with healthcheck...
docker-compose up postgres -d

echo.
echo [3/5] Waiting for PostgreSQL to be healthy...
:check_health
docker inspect --format "{{.State.Health.Status}}" postgres-db | find "healthy" >nul
if %errorlevel% neq 0 (
    echo Waiting for PostgreSQL to be ready...
    timeout /t 5 /nobreak
    goto check_health
)
echo ✓ PostgreSQL is healthy!

echo.
echo [4/5] Creating databases...
docker exec postgres-db psql -U postgres -c "CREATE DATABASE payment_service;" 2>nul && echo "✓ payment_service created" || echo "✓ payment_service already exists"
docker exec postgres-db psql -U postgres -c "CREATE DATABASE order_service;" 2>nul && echo "✓ order_service created" || echo "✓ order_service already exists"

echo.
echo [5/5] Starting all services...
docker-compose up -d

echo.
echo ========================================
echo            Final Status
echo ========================================

timeout /t 10 /nobreak

echo.
echo Container status:
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo If services are restarting, check logs with:
echo docker logs payment-service
echo docker logs order-service

pause