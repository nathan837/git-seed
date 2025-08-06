#!/bin/bash

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java and try again."
    exit 1
fi

# Check for arguments
if [ "$#" -lt 3 ]; then
    echo "Usage: $0 <command> <file> <password>"
    echo "Commands: encrypt, decrypt"
    exit 1
fi

COMMAND=$1
FILE=$2
PASSWORD=$3
JAR_FILE="AESFileCrypto.jar"

# Check if file exists
if [ ! -f "$FILE" ]; then
    echo "Error: File $FILE does not exist"
    exit 1
fi

# Compile Java if needed
if [ ! -f "$JAR_FILE" ]; then
    echo "Compiling Java code..."
    javac AESFileCrypto.java
    if [ $? -ne 0 ]; then
        echo "Error: Failed to compile Java code"
        exit 1
    fi
    
    # Create executable JAR
    echo "Main-Class: AESFileCrypto" > Manifest.txt
    jar cvfm $JAR_FILE Manifest.txt AESFileCrypto.class
    rm Manifest.txt AESFileCrypto.class
fi

# Execute the command
case $COMMAND in
    encrypt|decrypt)
        java -cp . AESFileCrypto $COMMAND $FILE "$PASSWORD"
        ;;
    *)
        echo "Invalid command: $COMMAND. Use 'encrypt' or 'decrypt'"
        exit 1
        ;;
esac