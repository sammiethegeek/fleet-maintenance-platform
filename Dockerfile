FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar fleet-maintenance-system.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "fleet-maintenance-system.jar"]
