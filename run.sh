#!/usr/bin/env bash
set -e

# Detect Java 17+ or local JDK
if [ -x "$HOME/.jdk/Contents/Home/bin/java" ]; then
    JAVA="$HOME/.jdk/Contents/Home/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA="java"
else
    echo "Error: java not found. Please install Java 17+."
    exit 1
fi

if [ ! -f target/skillswap.jar ]; then
    echo "target/skillswap.jar not found. Running build first..."
    ./build.sh
fi

PORT="${PORT:-8080}"
echo "Starting SkillSwap on port $PORT..."
exec "$JAVA" -jar target/skillswap.jar
