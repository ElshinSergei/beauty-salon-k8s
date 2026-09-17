# --- ЭТАП СБОРКИ ВСЕХ МОДУЛЕЙ ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 1. Копируем корневой POM и POM-файлы всех модулей для кэширования зависимостей
COPY pom.xml .
COPY user-service/pom.xml ./user-service/
COPY booking-service/pom.xml ./booking-service/
COPY notification-service/pom.xml ./notification-service/

# Скачиваем зависимости для всех модулей
RUN mvn dependency:go-offline -B

# 2. Копируем исходный код всех сервисов и компилируем проект целиком

COPY user-service/src ./user-service/src
COPY booking-service/src ./booking-service/src
COPY notification-service/src ./notification-service/src

# Force invalidation
RUN echo "Invalidating cache"

RUN mvn clean package -DskipTests

# --- ЭТАПЫ ДЛЯ КОНКРЕТНЫХ СЕРВИСОВ ---
# Мы используем таргеты сборки (targets), чтобы docker-compose мог брать нужный jar из общего билдера

FROM eclipse-temurin:21-jre-alpine AS user-service
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && chown -R spring:spring /app
USER spring:spring
COPY --from=builder /app/user-service/target/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS booking-service
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && chown -R spring:spring /app
USER spring:spring
COPY --from=builder /app/booking-service/target/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine AS notification-service
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && chown -R spring:spring /app
USER spring:spring
COPY --from=builder /app/notification-service/target/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
