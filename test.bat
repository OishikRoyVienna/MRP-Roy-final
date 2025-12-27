@echo off
echo === 1. Register ===
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"technikum\",\"password\":\"geheim\"}"

echo.
echo === 2. Login ===
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"technikum\",\"password\":\"geheim\"}"

echo === 3. Create Media ===
curl -X POST http://localhost:8080/api/media ^
  -H "Authorization: Bearer technikum-mrpToken" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Batman\",\"mediaType\":\"movie\",\"releaseYear\":2010,\"genres\":[\"sci-fi\",\"action\"],\"ageRestriction\":16}"

echo.
echo === 4. Get Media ===
curl -X GET http://localhost:8080/api/media/1

pause