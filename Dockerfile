FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder
WORKDIR /time-api

# Copy pom and source for API service
COPY pom.xml .
COPY src ./src

# Build API JAR
RUN mvn clean package -DskipTests


FROM openjdk:19-ea-alpine

# Install bash (needed to start/stop the app) tzdata (for time zones) procps for correct "ps"
RUN apk add --no-cache bash curl tzdata procps && \
     ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
     echo $TZ && \
    rm -rf /var/lib/apt/lists/*



# Copy libfaketime from official image
COPY --from=trajano/alpine-libfaketime /faketime.so /lib/faketime.so

ENV LD_PRELOAD=/lib/faketime.so \
    DONT_FAKE_MONOTONIC=1 \
    FAKETIME=""

WORKDIR /app


COPY --from=builder /time-api/target/*.jar time-api.jar

COPY restart_app.sh /app/restart_app.sh
RUN chmod +x /app/restart_app.sh

# Mount your app.jar here
VOLUME /app/myapp

EXPOSE 8080

COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Start Spring Boot time wrapper
CMD ["./start.sh"]

# docker build -t ambu550/faketime-jdk19-alpine:0.3.4 .
# docker tag ambu550/faketime-jdk19-alpine:0.3.4 ambu550/faketime-jdk19-alpine:latest
# docker push ambu550/faketime-jdk19-alpine:0.3.4
# docker push ambu550/faketime-jdk19-alpine:latest