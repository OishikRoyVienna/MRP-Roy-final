#!/bin/bash
URL="http://localhost:8080"

echo "🚀 Starting MRP Final Test Suite..."
echo

# 1. Register & Login
echo "1. Register user 'alice'..."
curl -s -X POST "$URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secure123"}'

echo
echo "2. Login user 'alice'..."
TOKEN=$(curl -s -X POST "$URL/api/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secure123"}' | jq -r '.token')
echo "Token: $TOKEN"
echo

# 2. Create Media
echo "3. Create media 'Inception'..."
MEDIA_ID=$(curl -s -X POST "$URL/api/media" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Inception","mediaType":"movie","genres":["sci-fi","action"],"ageRestriction":12}' | jq -r '.id')
echo "Media ID: $MEDIA_ID"
echo

# 3. Rate & Confirm
echo "4. Rate media..."
curl -s -X POST "$URL/api/media/$MEDIA_ID/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":5,"comment":"Mind-blowing!"}'

echo
echo "5. Get ratings (should be empty – not confirmed)..."
curl -s "$URL/api/media/$MEDIA_ID/ratings"

echo
echo "6. Confirm rating..."
RATING_ID=$(curl -s "$URL/api/media/$MEDIA_ID/ratings" -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id')
curl -s -X PUT "$URL/api/ratings/$RATING_ID/confirm" -H "Authorization: Bearer $TOKEN"

echo
echo "7. Get ratings (now visible)..."
curl -s "$URL/api/media/$MEDIA_ID/ratings"
echo

# 4. Favorites & Profile
echo "8. Add to favorites..."
curl -s -X POST "$URL/api/favorites/$MEDIA_ID" -H "Authorization: Bearer $TOKEN"

echo
echo "9. Get favorites..."
curl -s -H "Authorization: Bearer $TOKEN" "$URL/api/favorites"

echo
echo "10. Get profile..."
curl -s -H "Authorization: Bearer $TOKEN" "$URL/api/users/alice/profile"

echo
echo "🎉 Test suite completed!"