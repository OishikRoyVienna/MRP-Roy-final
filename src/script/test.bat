@echo off
echo 🚀 Starting MRP Final Test Suite...
echo.

:: 0. Reset DB
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
curl -s -X POST http://localhost:8080/api/media -H "Content-Type: application/json" -H "Authorization: Bearer alice-mrpToken" -d "{\"title\":\"Inception\",\"mediaType\":\"movie\",\"genres\":[\"sci-fi\"],\"ageRestriction\":12}"
echo.

:: 4. Create Rating (via /api/ratings)
echo 4. Create rating...
curl -s -X POST http://localhost:8080/api/ratings -H "Content-Type: application/json" -H "Authorization: Bearer alice-mrpToken" -d "{\"mediaId\":1,\"stars\":5,\"comment\":\"Amazing film!\"}"
echo.

:: 5. Confirm Rating
echo 5. Confirm rating...
curl -s -X PUT http://localhost:8080/api/ratings/1/confirm -H "Authorization: Bearer alice-mrpToken"
echo.

:: 6. Update Rating
echo 6. Update rating...
curl -s -X PUT http://localhost:8080/api/ratings/1 -H "Content-Type: application/json" -H "Authorization: Bearer alice-mrpToken" -d "{\"stars\":4,\"comment\":\"Still great, but slightly overrated\"}"
echo.

:: 7. Like Rating
echo 7. Like rating...
curl -s -X POST http://localhost:8080/api/ratings/1/like -H "Authorization: Bearer alice-mrpToken"
echo.

:: 8. Add to Favorites
echo 8. Add to favorites...
curl -s -X POST http://localhost:8080/api/favorites/1 -H "Authorization: Bearer alice-mrpToken"
echo.

:: 9. Search & Filter (✅ +3 Punkte!)
echo 9. Search and filter (genre=sci-fi, minRating=3)...
curl -s -X GET http://localhost:8080/api/media -H "Authorization: Bearer alice-mrpToken" -H "Content-Type: application/json" -d "{\"genre\":\"sci-fi\",\"minRating\":3}"
echo.

:: 10. Get Favorites
echo 10. Get favorites...
curl -s -H "Authorization: Bearer alice-mrpToken" http://localhost:8080/api/favorites
echo.

:: 11. Get Profile
echo 11. Get profile...
curl -s -H "Authorization: Bearer alice-mrpToken" http://localhost:8080/api/users/alice/profile
echo.

:: 12. Delete Rating
echo 12. Delete rating...
curl -s -X DELETE http://localhost:8080/api/ratings/1 -H "Authorization: Bearer alice-mrpToken"
echo.

echo.
echo 🎉 Test suite completed!
pause