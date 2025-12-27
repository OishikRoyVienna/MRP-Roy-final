@echo off
setlocal
set URL=http://localhost:8080

echo 🚀 Starting MRP Final Test Suite...
echo.

:: 1. Register User
echo 1. Register user "alice"...
curl -s -X POST %URL%/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"alice\",\"password\":\"secure123\"}"

echo.
echo 2. Login user "alice"...
for /f "tokens=2 delims=:\" %%a in ('curl -s -X POST %URL%/api/users/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"secure123\"}"') do set TOKEN=%%a
set TOKEN=%TOKEN:~1,-1%
echo Token: %TOKEN%
echo.

:: 2. Create Media
echo 3. Create media "Inception"...
set MEDIA_ID=
for /f "tokens=2 delims=:" %%a in ('curl -s -X POST %URL%/api/media ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"title\":\"Inception\",\"mediaType\":\"movie\",\"genres\":[\"sci-fi\",\"action\"],\"ageRestriction\":12}" ^
  ^| findstr /r /c:"\"id\":[0-9]*"') do set MEDIA_ID=%%a
set MEDIA_ID=%MEDIA_ID:~1%
echo Media ID: %MEDIA_ID%
echo.

:: 3. Rate Media
echo 4. Rate media with 5 stars...
curl -s -X POST %URL%/api/media/%MEDIA_ID%/ratings ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"stars\":5,\"comment\":\"Mind-blowing!\"}"
echo.

:: 4. Get Ratings (should be empty – not confirmed yet!)
echo 5. Get ratings for media (should be empty – not confirmed)...
curl -s %URL%/api/media/%MEDIA_ID%/ratings
echo.

:: 5. Confirm Rating (as creator)
echo 6. Confirm rating (as creator)...
for /f "tokens=2 delims=:" %%a in ('curl -s %URL%/api/media/%MEDIA_ID%/ratings -H "Authorization: Bearer %TOKEN%" ^| findstr /r /c:"\"id\":[0-9]*"') do set RATING_ID=%%a
set RATING_ID=%RATING_ID:~1%
curl -s -X PUT %URL%/api/ratings/%RATING_ID%/confirm ^
  -H "Authorization: Bearer %TOKEN%"
echo.

:: 6. Get Ratings (now visible)
echo 7. Get ratings again (now visible)...
curl -s %URL%/api/media/%MEDIA_ID%/ratings
echo.

:: 7. Add to Favorites
echo 8. Add media to favorites...
curl -s -X POST %URL%/api/favorites/%MEDIA_ID% ^
  -H "Authorization: Bearer %TOKEN%"
echo.

:: 8. Get Favorites
echo 9. Get favorites...
curl -s -H "Authorization: Bearer %TOKEN%" %URL%/api/favorites
echo.

:: 9. Get Profile
echo 10. Get profile...
curl -s -H "Authorization: Bearer %TOKEN%" %URL%/api/users/alice/profile
echo.

echo.
echo 🎉 Test suite completed!