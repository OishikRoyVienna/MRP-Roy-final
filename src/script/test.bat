@echo off
echo 🚀 Starting MRP Final Test Suite...
echo.

:: 0. Reset DB (sicherstellen: leerer Zustand)
echo 0. Reset database...
curl -s -X POST http://localhost:8080/api/reset > nul
timeout /t 1 > nul

:: 1. Register
echo 1. Register user "alice"...
curl -s -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"123\"}"
echo.

:: 2. Login
echo 2. Login user "alice"...
curl -s -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"123\"}"
echo.

:: 3. Create Media
echo 3. Create media "Inception"...
curl -s -X POST http://localhost:8080/api/media -H "Content-Type: application/json" -H "Authorization: Bearer alice-mrpToken" -d "{\"title\":\"Inception\",\"mediaType\":\"movie\",\"creatorUsername\":\"alice\"}"
echo.

:: 4. Rate Media (POST /api/ratings – dein Code unterstützt das!)
echo 4. Rate media...
curl -s -X POST http://localhost:8080/api/ratings -H "Content-Type: application/json" -H "Authorization: Bearer alice-mrpToken" -d "{\"mediaId\":1,\"stars\":5,\"comment\":\"Great!\"}"
echo.

:: 5. Confirm Rating (PUT /api/ratings/1/confirm)
echo 5. Confirm rating...
curl -s -X PUT http://localhost:8080/api/ratings/1/confirm -H "Authorization: Bearer alice-mrpToken"
echo.

:: 6. Add to Favorites (POST /api/favorites/1 – dein Handler unterstützt /api/favorites/\d+!)
echo 6. Add to favorites...
curl -s -X POST http://localhost:8080/api/favorites/1 -H "Authorization: Bearer alice-mrpToken"
echo.

:: 7. Get Favorites
echo 7. Get favorites...
curl -s -H "Authorization: Bearer alice-mrpToken" http://localhost:8080/api/favorites
echo.

:: 8. Get Profile
echo 8. Get profile...
curl -s -H "Authorization: Bearer alice-mrpToken" http://localhost:8080/api/users/alice/profile
echo.

echo.
echo 🎉 Test suite completed!
pause