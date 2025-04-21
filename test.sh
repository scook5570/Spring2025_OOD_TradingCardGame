#!/bin/bash

# Script to test just trading functionality

# Ensure directories exist
mkdir -p src/server/databases

# Set up test database files with known state
cat > src/server/databases/users.json << EOF
[
   {
      "user" : "player1",
      "password" : "password123"
   },
   {
      "user" : "player2",
      "password" : "password123"
   }
]
EOF

# Create usercards.json with cards for player1 to trade
cat > src/server/databases/usercards.json << EOF
[
   [
      "player1",
      [
         {
            "cardID": "card001",
            "name": "Fire Dragon",
            "rarity": 5,
            "imageLink": "src/server/cardinfo/images/fire_dragon.png"
         },
         {
            "cardID": "card002",
            "name": "Water Sprite",
            "rarity": 3,
            "imageLink": "src/server/cardinfo/images/water_sprite.png"
         }
      ]
   ],
   [
      "player2",
      [
      ]
   ]
]
EOF

# Create empty trades.json
cat > src/server/databases/trades.json << EOF
[]
EOF

# Compile the project
ant compile

# Start the server in the background
echo "Starting server..."
java -cp build:lib/merrimackutil.jar server.Server &
SERVER_PID=$!
sleep 5  # Give server time to start

# Player2 responds to trade
echo "Testing player2 (accepting trade)..."
java -cp build:lib/merrimackutil.jar client.TradingTest player2 password123 accept &
PLAYER2_PID=$!
sleep 5

# Player1 initiates trade
echo "Testing player1 (initiating trade)..."
java -cp build:lib/merrimackutil.jar client.TradingTest player1 password123 initiate player2 card001,card002 &
PLAYER1_PID=$!
sleep 20

# Kill processes
echo "Cleaning up processes..."
kill $PLAYER1_PID
kill $PLAYER2_PID
kill $SERVER_PID

# Verify result
echo "Checking result..."
cat src/server/databases/usercards.json
echo "Test complete"