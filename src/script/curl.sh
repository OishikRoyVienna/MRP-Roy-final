#!/bin/bash

echo "🚀 Starting MRP Final Test Suite...🚀"

# 0. Reset database
echo -e "\n0. Reset database..."
curl -X POST http://localhost:8080/api/reset

# 1. Register user "alice"
echo -e "\n1. Register user \"alice\"..."
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123"}'

# 2. Login user "alice"
echo -e "\n2. Login user \"alice\"..."
login_response=$(curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123"}' \
  -s)
token=$(echo "$login_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "$login_response"

# 3. Create media "Inception"
echo -e "\n3. Create media \"Inception\"..."
media_response=$(curl -X POST http://localhost:8080/api/media \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"title":"Inception","mediaType":"movie","genres":["sci-fi"],"ageRestriction":12}' \
  -s)
media_id=$(echo "$media_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "$media_response"

# 4. Create rating (5★, comment)
echo -e "\n4. Create rating (5★, comment)..."
rating_response=$(curl -X POST http://localhost:8080/api/ratings \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d "{\"mediaId\":$media_id,\"stars\":5,\"comment\":\"Amazing film!\"}" \
  -s)
rating_id=$(echo "$rating_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "$rating_response"

# 5. Confirm rating (moderation)
echo -e "\n5. Confirm rating (moderation)..."
curl -X PUT http://localhost:8080/api/ratings/$rating_id/confirm \
  -H "Authorization: Bearer $token"

# 6. Update rating (→ 4★)
echo -e "\n6. Update rating (→ 4★)..."
curl -X PUT http://localhost:8080/api/ratings/$rating_id \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{"stars":4,"comment":"Still great, but slightly overrated"}'

# 7. Like rating
echo -e "\n7. Like rating..."
curl -X POST http://localhost:8080/api/ratings/$rating_id/like \
  -H "Authorization: Bearer $token"

# 8. Add to favorites
echo -e "\n8. Add to favorites..."
curl -X POST http://localhost:8080/api/favorites/$media_id \
  -H "Authorization: Bearer $token"

# 9. Search: sci-fi, minRating=3
echo -e "\n9. Search: sci-fi, minRating=3..."
curl -G \
  -H "Content-Type: application/json" \
  --data-urlencode "genre=sci-fi" \
  --data-urlencode "minRating=3" \
  http://localhost:8080/api/media

# 10. Get favorites (IDs)
echo -e "\n10. Get favorites (IDs)..."
curl -H "Authorization: Bearer $token" http://localhost:8080/api/favorites

# 11. Get profile (stats + favoriteGenre)
echo -e "\n11. Get profile (stats + favoriteGenre)..."
curl -H "Authorization: Bearer $token" http://localhost:8080/api/users/alice/profile

# 12. Delete rating
echo -e "\n12. Delete rating..."
curl -X DELETE http://localhost:8080/api/ratings/$rating_id \
  -H "Authorization: Bearer $token"

echo -e "\n🎉 Test suite completed successfully. 🎉"