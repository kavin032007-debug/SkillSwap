#!/usr/bin/env bash
set -e

# Detect Java 17+ or local JDK
if [ -x "$HOME/.jdk/Contents/Home/bin/javac" ]; then
    JAVAC="$HOME/.jdk/Contents/Home/bin/javac"
    JAR="$HOME/.jdk/Contents/Home/bin/jar"
elif command -v javac >/dev/null 2>&1; then
    JAVAC="javac"
    JAR="jar"
else
    echo "Error: javac not found. Please install Java 17+."
    exit 1
fi

echo "==> Compiling SkillSwap Java sources..."
mkdir -p target/classes target/dist

# Find and compile all main sources
find src/main/java -name "*.java" > target/sources.txt
"$JAVAC" -d target/classes @target/sources.txt

echo "==> Packaging standalone executable JAR target/skillswap.jar..."
"$JAR" --create --file target/skillswap.jar --main-class com.skillswap.Main -C target/classes .

echo "==> Build successful! Run with: ./run.sh or java -jar target/skillswap.jar"
