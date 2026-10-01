FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S notify && adduser -S -G notify notify
WORKDIR /app
COPY target/notify-public-api-*.jar app.jar
USER notify
EXPOSE 5081
ENTRYPOINT ["java", "-jar", "app.jar"]
