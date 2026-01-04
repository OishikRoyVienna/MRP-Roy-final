@echo off
echo === 0. Reset Database ===
curl -s -X POST http://localhost:8080/api/reset > nul
timeout /t 1 > nul

echo === 1. Register ===
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"technikum\",\"password\":\"123\"}"

echo === 2. Login ===
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"technikum\",\"password\":\"123\"}"

echo === 3. Create Media ===
curl -X POST http://localhost:8080/api/media ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer technikum-mrpToken" ^
  -d "{\"title\":\"Batman\",\"mediaType\":\"movie\",\"creatorUsername\":\"technikum\"}"

echo === 4. Get Media ===
curl http://localhost:8080/api/media/1

pause