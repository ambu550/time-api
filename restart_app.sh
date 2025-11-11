#!/bin/bash

APP_NAME="myapp.jar" # Replace with your actual JAR file name
#JAVA_OPTS="-Xmx512m -Dsome.property=value" # Optional: Add JVM options
FAKETIME=$1 # empty rollback!
# Stop the existing Java application
PID=$(ps aux | grep "$APP_NAME" | grep -v grep | awk '{print $1}')

if [ -n "$PID" ]; then
    echo "Stopping $APP_NAME (PID: $PID)..."
    kill "$PID"
    # Wait for the process to terminate
    while ps -p "$PID" > /dev/null; do
        sleep 1
    done
    echo "$APP_NAME stopped."
else
    echo "$APP_NAME is not running, starting a new instance."
fi

# Start the Java application
echo "Starting $APP_NAME..."
echo "With fake time: $FAKETIME..."

#nohup java $JAVA_OPTS -jar "$APP_NAME" > /dev/null 2>&1 & # Use nohup to keep it running after script exit
java -jar myapp/"$APP_NAME"
