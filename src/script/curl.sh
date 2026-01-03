#!/bin/bash
echo "🚀 Starting MRP Final Test Suite..."
echo

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
TOKEN=$(echo "$RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
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
MEDIA_ID=$(echo "$RESP" | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Media ID: $MEDIA_ID"
echo

# 4. Rate media
echo "4. Rate media..."
curl -s -X POST http://localhost:8080/api/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"mediaId\": $MEDIA_ID,
    \"stars\": 5,
    \"comment\": \"Great movie!\"
  }"
echo

# 5. Get ratings (unconfirmed → leer)
echo "5. Get ratings (unconfirmed)..."
curl -s "http://localhost:8080/api/media/$MEDIA_ID/ratings?confirmed=false"
echo

# 6. Confirm rating (als Creator: 'alice')
echo "6. Confirm rating..."
RATING_ID=$(curl -s "http://localhost:8080/api/media/$MEDIA_ID/ratings?confirmed=false" | jq -r '.[0].id // "1"')
curl -s -X PUT "http://localhost:8080/api/ratings/$RATING_ID/confirm" \
  -H "Authorization: Bearer $TOKEN"
echo

# 7. Get ratings (confirmed → sichtbar)
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
curl -s "http://localhost:8080/api/users/alice/profile"
echo
echo "🎉 Test suite completed!"