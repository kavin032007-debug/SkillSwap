#!/usr/bin/env bash
set -e

# Detect Java 17+ or local JDK
if [ -x "$HOME/.jdk/Contents/Home/bin/javac" ]; then
    JAVAC="$HOME/.jdk/Contents/Home/bin/javac"
    JAVA="$HOME/.jdk/Contents/Home/bin/java"
elif command -v javac >/dev/null 2>&1; then
    JAVAC="javac"
    JAVA="java"
else
    echo "Error: javac not found. Please install Java 17+."
    exit 1
fi

echo "==> Compiling and running SkillSwap test suite..."
mkdir -p target/classes target/test-classes

find src/main/java -name "*.java" > target/main_sources.txt
"$JAVAC" -d target/classes @target/main_sources.txt

find src/test/java -name "*.java" > target/test_sources.txt
"$JAVAC" -cp target/classes -d target/test-classes @target/test_sources.txt

"$JAVA" -cp target/classes:target/test-classes com.skillswap.SkillSwapTestRunner
