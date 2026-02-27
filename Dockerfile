# Stage 1: Build the application
# We use the full JDK image to compile the code
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build
WORKDIR /app
COPY . .
# Gradle build command (skipping tests and plain jar creation)
RUN ./gradlew clean bootJar -x test -x jar

# Stage 2: Run the application
# Microsoft provides a lightweight runtime for deployment
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=mysql", "-jar", "app.jar"]
