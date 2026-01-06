@echo off
echo 🚀 Starting MRP Final Test Suite...
echo.

:: 0. Reset DB
echo 0. Reset database...
curl -X POST http://localhost:8080/api/reset
echo.

:: 1. Register
echo 1. Register user "alice"...
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"123\"}"
echo.

:: 2. Login
echo 2. Login user "alice"...
curl -X POST http://localhost:8080/api/users/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"123\"}"
echo.

:: 3. Create Media
echo 3. Create media "Inception"...
curl -X POST http://localhost:8080/api/media ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer alice-mrpToken" ^
  -d "{\"title\":\"Inception\",\"mediaType\":\"movie\",\"genres\":[\"sci-fi\"],\"ageRestriction\":12}"
echo.

:: 4. Create Rating
echo 4. Create rating (5★, comment)...
curl -X POST http://localhost:8080/api/ratings ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer alice-mrpToken" ^
  -d "{\"mediaId\":1,\"stars\":5,\"comment\":\"Amazing film!\"}"
echo.

:: 5. Confirm Rating
echo 5. Confirm rating (moderation)...
curl -X PUT http://localhost:8080/api/ratings/1/confirm ^
  -H "Authorization: Bearer alice-mrpToken"
echo.

:: 6. Update Rating
echo 6. Update rating (→ 4★)...
curl -X PUT http://localhost:8080/api/ratings/1 ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer alice-mrpToken" ^
  -d "{\"stars\":4,\"comment\":\"Still great, but slightly overrated\"}"
echo.

:: 7. Like Rating
echo 7. Like rating...
curl -X POST http://localhost:8080/api/ratings/1/like ^
  -H "Authorization: Bearer alice-mrpToken"
echo.

:: 8. Add to Favorites
echo 8. Add to favorites...
curl -X POST http://localhost:8080/api/favorites/1 ^
  -H "Authorization: Bearer alice-mrpToken"
echo.

:: 9. Search & Filter
echo 9. Search: sci-fi, minRating=3...
curl -X GET http://localhost:8080/api/media ^
  -H "Authorization: Bearer alice-mrpToken" ^
  -H "Content-Type: application/json" ^
  -d "{\"genre\":\"sci-fi\",\"minRating\":3}"
echo.

:: 10. Get Favorites
echo 10. Get favorites (IDs)...
curl -H "Authorization: Bearer alice-mrpToken" http://localhost:8080/api/favorites
echo.

:: 11. Get Profile
echo 11. Get profile (stats + favoriteGenre)...
curl -H "Authorization: Bearer alice-mrpToken" http://localhost:8080/api/users/alice/profile
echo.

:: 12. Delete Rating
echo 12. Delete rating...
curl -X DELETE http://localhost:8080/api/ratings/1 ^
  -H "Authorization: Bearer alice-mrpToken"
echo.

echo 🎉 Test suite completed successfully. 🎉
echo.
pause