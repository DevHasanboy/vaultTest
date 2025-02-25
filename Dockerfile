# Java 17 bazaviy rasm
FROM openjdk:17-jdk-alpine

# Jar faylini konteynerga ko'chirish
COPY target/app.jar /app/app.jar

# Konteyner ichida "/app" papkasini ishga tushirish
WORKDIR /app

# Spring Boot ilovasini ishga tushirish
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
