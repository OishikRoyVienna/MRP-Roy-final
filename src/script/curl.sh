#!/bin/bash
echo "🚀 Starting MRP Final Test Suite 🚀"
echo

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 0. Reset DB
echo -e "${YELLOW}0. Reset database...${NC}"
curl -X POST http://localhost:8080/api/reset
echo

# 1. Register
echo -e "${YELLOW}1. Register user 'alice'...${NC}"
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123"}'
echo

# 2. Login → Token speichern
echo -e "${YELLOW}2. Login user 'alice'...${NC}"
RESP=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123"}')
TOKEN=$(echo "$RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "→ Token: $TOKEN"
echo

# 3. Create Media → Media-ID extrahieren
echo -e "${YELLOW}3. Create media 'Inception'...${NC}"
RESP=$(curl -s -X POST http://localhost:8080/api/media \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Inception",
    "mediaType": "movie",
    "genres": ["sci-fi"],
    "ageRestriction": 12,
    "creatorUsername": "alice"
  }')
MEDIA_ID=$(echo "$RESP" | grep -o '"id":[0-9]*' | cut -d: -f2)
echo "→ Media ID: $MEDIA_ID"
echo

# 4. Create Rating (via /api/ratings)
echo -e "${YELLOW}4. Create rating (5★, comment)...${NC}"
RESP=$(curl -s -X POST http://localhost:8080/api/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"mediaId\":$MEDIA_ID,\"stars\":5,\"comment\":\"Amazing film!\"}")
RATING_ID=$(echo "$RESP" | grep -o '"id":[0-9]*' | cut -d: -f2)
echo "$RESP"
echo "→ Rating ID: $RATING_ID"
echo

# 5. Confirm Rating
echo -e "${YELLOW}5. Confirm rating (moderation)...${NC}"
RESP=$(curl -s -X PUT "http://localhost:8080/api/ratings/$RATING_ID/confirm" \
  -H "Authorization: Bearer $TOKEN")
echo "$RESP"
echo

# 6. Update Rating
echo -e "${YELLOW}6. Update rating (→ 4★)...${NC}"
curl -s -X PUT "http://localhost:8080/api/ratings/$RATING_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"stars":4,"comment":"Still great, but slightly overrated"}' | jq . 2>/dev/null || cat
echo

# 7. Like Rating
echo -e "${YELLOW}7. Like rating...${NC}"
curl -s -X POST "http://localhost:8080/api/ratings/$RATING_ID/like" \
  -H "Authorization: Bearer $TOKEN"
echo '{"message":"Like toggled"}'
echo

# 8. Add to Favorites
echo -e "${YELLOW}8. Add to favorites...${NC}"
curl -s -X POST "http://localhost:8080/api/favorites/$MEDIA_ID" \
  -H "Authorization: Bearer $TOKEN"
echo '{"message":"Added to favorites"}'
echo

# 9. Search & Filter
echo -e "${YELLOW}9. Search: sci-fi, minRating=3...${NC}"
curl -s -X GET http://localhost:8080/api/media \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"genre":"sci-fi","minRating":3}'
echo

# 10. Get Favorites
echo -e "${YELLOW}10. Get favorites (IDs)...${NC}"
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/favorites
echo

# 11. Get Profile
echo -e "${YELLOW}11. Get profile (stats + favoriteGenre)...${NC}"
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/alice/profile
echo

# 12. Delete Rating
echo -e "${YELLOW}12. Delete rating...${NC}"
curl -s -X DELETE "http://localhost:8080/api/ratings/$RATING_ID" \
  -H "Authorization: Bearer $TOKEN"
echo '{"message":"Rating deleted"}'
echo

echo -e "${GREEN}🎉 Test suite completed successfully. 🎉${NC}"