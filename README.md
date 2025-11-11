Java19 Docker image with API for time change in container

- Run your app in this container for test
- Change date & time when eou need by API
- wait for app restart (in future will be optional health-check that return response when your app restarted and ready to work)
- test your app in specific date and time (freeze)
- reset-time to normal (local)
- continue test in local time

### [Docker HUB](https://hub.docker.com/repository/docker/ambu550/faketime-jdk19-alpine/general)
### [RELEASES](https://github.com/ambu550/time-api/releases)


#### mount your app to container
```
  my-application:
    container_name: my-application
    image: ambu550/faketime-jdk19-alpine:0.2.0 # use latest tag
    working_dir: /app
    environment:
      - TZ=Europe/Kiev
      - JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8
    ports:
      - "80:80" # your app port (may be different)
      - "8080:8080"
    volumes:
      - ./my-dir/target/some-application.jar:/app/myapp/myapp.jar # path to your app:required path & name in container
    # NOT NEED !! command: sh -c "java -jar /app/myapp/myapp.jar"
```

#### Set custom time in container timeZone (and restart app)
```
curl -X POST http://localhost:8080/set-time \
-H "Content-Type: application/json" \
-d '{"time": "2025-11-09 08:30:00"}'
```

#### Set custom time in container timeZone restart app AND wait for health (OPTIONAL)
```
curl -X POST http://localhost:8080/set-time \
     -H "Content-Type: application/json" \
     -d '{
           "time": "2025-05-31 23:00:00",
           "healthUrl": "http://localhost:9999/actuator/health"
         }'
```

#### Set time shift (and restart app)
```
curl -X POST http://localhost:8080/set-time \
-H "Content-Type: application/json" \
-d '{"time": "+15d"}'
```
time shift examples
- "-2h", "+10m", "+5d", "+1y"

#### Reset time to host (and restart app)
```
curl -X POST http://localhost:8080/reset-time
```

#### Reset time to host restart app AND wait for health (OPTIONAL)
```
curl -X POST http://localhost:8080/reset-time \
     -H "Content-Type: application/json" \
     -d '{
           "healthUrl": "http://localhost:28443/actuator/health"
         }'
```

## Powered by
#### https://hub.docker.com/r/trajano/alpine-libfaketime
#### https://github.com/wolfcw/libfaketime
