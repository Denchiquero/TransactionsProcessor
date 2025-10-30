docker-compose down

docker-compose up postgres -d

timeout /t 10 /nobreak

docker exec postgres-db psql -U postgres -c "CREATE DATABASE payment_service;" 2>nul && echo "✓ payment_service created" || echo "✓ payment_service already exists"
docker exec postgres-db psql -U postgres -c "CREATE DATABASE order_service;" 2>nul && echo "✓ order_service created" || echo "✓ order_service already exists"

docker-compose up -d

