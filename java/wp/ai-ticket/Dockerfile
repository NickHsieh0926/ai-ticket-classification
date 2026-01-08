# ===== Stage 1: Build =====
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

# 先複製 pom.xml，利用 Docker cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 再複製原始碼
COPY src ./src

# 打包（略過測試，加快 demo）
RUN mvn -B package -DskipTests


# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre

WORKDIR /app

# 只複製 jar
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
