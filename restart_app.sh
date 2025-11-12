#!/bin/bash

APP_NAME="myapp.jar" # Replace with your actual JAR file name
#JAVA_OPTS="-Xmx512m -Dsome.property=value" # Optional: Add JVM options

# Stop the existing Java application
PID=$(ps aux | grep "$APP_NAME" | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
    echo "Stopping $APP_NAME (PID: $PID)..."
    kill -9 "$PID"

    attempt=1
    max_attempts=10

    while [ $attempt -le $max_attempts ]; do

        echo "Attempt $attempt of $max_attempts..."

        # Check is process still alive
        if $(ps -p "$PID" > /dev/null) > /dev/null; then
            echo "Still killing...$PID"
            ((attempt++))
            sleep 1  # optional delay before next attempt
        else
            echo "$APP_NAME stopped."
            break
        fi
    done

else
    echo "$APP_NAME is not running, starting a new instance."
fi

FAKETIME=$1 # empty rollback!

# Start the Java application
echo "Starting $APP_NAME..."
echo "With fake time: $FAKETIME..."

#nohup java $JAVA_OPTS -jar "$APP_NAME" > /dev/null 2>&1 & # Use nohup to keep it running after script exit
nohup java -jar myapp/"$APP_NAME" &
exit 0
