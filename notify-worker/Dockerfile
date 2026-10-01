FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S notify && adduser -S -G notify notify
WORKDIR /app
COPY target/notify-worker-*.jar app.jar
USER notify
ENTRYPOINT ["java", "-jar", "app.jar"]
