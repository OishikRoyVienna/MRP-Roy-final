@echo off
setlocal
set URL=http://localhost:8080

echo 🚀 Starting MRP Final Test Suite...
echo.

:: 0. Reset DB
echo 0. Reset database...
curl -s -X POST %URL%/api/reset > nul
timeout /t 1 > nul

:: 1. Register
echo 1. Register user "alice"...
curl -X POST %URL%/api/users/register -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"123\"}"
echo.

:: 2. Login (hardcoded token – für Test ausreichend)
echo 2. Login user "alice"...
set TOKEN=alice-mrpToken
echo Token: %TOKEN%
echo.

:: 3. Create Media (ID=4 erwartet)
curl -X POST %URL%/api/media -H "Authorization: Bearer %TOKEN%" -d "{\"title\":\"Inception\",\"mediaType\":\"movie\",\"creatorUsername\":\"alice\"}"

:: 4. Rate Media – ✅ RICHTIGER PFAD & mediaId im Body
curl -X POST %URL%/api/ratings -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"mediaId\":4,\"stars\":5,\"comment\":\"Great!\"}"

:: 5. Confirm rating (ID=1)
curl -X PUT %URL%/api/ratings/1/confirm -H "Authorization: Bearer %TOKEN%"

:: 6. Add to favorites – ✅ RICHTIGER PFAD & mediaId im Body
curl -X POST %URL%/api/favorites -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"mediaId\":4}"

:: 7. Get favorites
curl %URL%/api/users/alice/favorites -H "Authorization: Bearer %TOKEN%"

:: 8. Get profile
echo 8. Get profile...
curl %URL%/api/users/alice/profile -H "Authorization: Bearer %TOKEN%"
echo.

echo.
echo 🎉 Test suite completed!
pause