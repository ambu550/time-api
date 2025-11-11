Java19 Docker image with API for time change in container

- Run your app in this container for test
- Change date & time when eou need by API
- wait for app restart (in future will be optional health-check that return response when your app restarted and ready to work)
- test your app in specific date and time (freeze)
- reset-time to normal (local)
- continue test in local time

#### mount your app to container
```
  my-application:
    container_name: my-application
    image: ambu550/faketime-jdk19-alpine:0.1.0 ##use latest verion
    working_dir: /app
    environment:
      - TZ=Europe/Kiev
      - JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8
    volumes:
      - ./my-dir/target/some-application.jar:/app/myapp/myapp.jar # path to your app:required path & name in container
```

#### Set custom time in container timeZone (and restart app)
```
curl -X POST http://localhost:8080/set-time \
-H "Content-Type: application/json" \
-d '{"time": "2025-11-09 08:30:00"}'
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

## Powered by
#### https://hub.docker.com/r/trajano/alpine-libfaketime
#### https://github.com/wolfcw/libfaketime