#Stage 1: Build Angular
#FROM node:20-alpine AS frontend-build
#WORKDIR /app
#OPY frontend/package*.json ./
#RUN npm ci
#COPY frontend/ ./
#RUN npm run build --configuration=production

#Stage 2: Build SpringBoot with Angular Embedded
#FROM eclipse-temurin:21-jdk AS backend-build
#WORKDIR /app
#COPY pom.xml ./
#COPY .mvn/ .mvn/
#COPY mvnw ./
#COPY src/ ./src/
# copying angular build into static resource
#COPY --from=frontend-build frontend/dist/fleet-maintenance-frontend ./src/main/resources/static
#RUN mvn clean package -DskipTests

#Stage 3: Run the app
FROM eclipse-temurin:21-jdk
COPY target/*.jar fleet-maintenance-system.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "fleet-maintenance-system.jar"]
