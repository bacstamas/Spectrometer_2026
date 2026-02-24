#!/bin/bash
set -e

echo "Cleaning..."
rm -rf bin temp Main.jar

mkdir bin temp

echo "Unpacking libraries..."
cd temp
for jar in ../lib/*.jar; do
  jar xf "$jar"
done
cd ..

echo "Compiling..."
javac \
  -cp "lib/*" \
  -d bin \
  src/*.java

echo "Building fat JAR..."
cd bin
jar cfm ../Main.jar ../manifest.txt *.class -C ../temp .
cd ..

echo "Running..."
java -jar Main.jar
