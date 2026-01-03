#!/bin/bash
echo "🚀 Starting MRP Final Test Suite..."
echo

# 🔁 1. DB zurücksetzen (sicherstellen: leerer Zustand)
echo "1. Reset database..."
curl -s -X POST http://localhost:8080/api/reset > /dev/null

# 1. Register
echo "1. Register user 'alice'..."
curl -s -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123"}'
echo

# 2. Login
echo "2. Login user 'alice'..."
RESP=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123"}')
TOKEN=$(echo "$RESP" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
echo "Token: $TOKEN"
echo

# 3. Create media
echo "3. Create media 'Inception'..."
RESP=$(curl -s -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Inception",
    "mediaType": "movie",
    "creatorUsername": "alice"
  }')
MEDIA_ID=$(echo "$RESP" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo "Media ID: $MEDIA_ID"
echo

# 4. Rate media
echo "4. Rate media..."
curl -s -X POST "http://localhost:8080/api/media/$MEDIA_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":5,"comment":"Great!"}'
echo

# 5. Get ratings (unconfirmed)
echo "5. Get ratings (unconfirmed)..."
curl -s "http://localhost:8080/api/media/$MEDIA_ID/ratings?confirmed=false"
echo

# 6. Confirm rating
echo "6. Confirm rating..."
RATING_ID=$(curl -s "http://localhost:8080/api/media/$MEDIA_ID/ratings?confirmed=false" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
curl -s -X PUT "http://localhost:8080/api/ratings/$RATING_ID/confirm" \
  -H "Authorization: Bearer $TOKEN"
echo

# 7. Get ratings (confirmed)
echo "7. Get ratings (confirmed)..."
curl -s "http://localhost:8080/api/media/$MEDIA_ID/ratings?confirmed=true"
echo

# 8. Add to favorites
echo "8. Add to favorites..."
curl -s -X POST "http://localhost:8080/api/favorites/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN"
echo

# 9. Get favorites
echo "9. Get favorites..."
curl -s "http://localhost:8080/api/users/alice/favorites"
echo

# 10. Get profile
echo "10. Get profile..."
curl -s "http://localhost:8080/api/users/alice/profile" \
  -H "Authorization: Bearer $TOKEN"
echo
echo "🎉 Test suite completed!"