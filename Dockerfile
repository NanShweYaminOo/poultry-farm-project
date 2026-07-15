# 1. Build stage (Maven သုံးပြီး Java 21 Project ကို Build လုပ်ခြင်း)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# 2. Run stage (ထွက်လာတဲ့ .jar ဖိုင်ကို Java 21 Runtime ပေါ်မှာ Run ခြင်း)
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]