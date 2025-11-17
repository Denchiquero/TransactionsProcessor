echo "=== Starting Payment Service ==="

# Проверяем секреты
if [ ! -f /run/secrets/db_username ] || [ ! -f /run/secrets/db_password ]; then
    echo "ERROR: Secrets not found!"
    exit 1
fi

DB_USERNAME=$(cat /run/secrets/db_username)
DB_PASSWORD=$(cat /run/secrets/db_password)

echo "DB Username: $DB_USERNAME"

# Запускаем с JVM параметрами
exec java \
    -Dspring.datasource.username="$DB_USERNAME" \
    -Dspring.datasource.password="$DB_PASSWORD" \
    -jar /app/app.jar