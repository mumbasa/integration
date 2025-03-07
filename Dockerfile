FROM eclipse-temurin:17-jdk-focal
 
WORKDIR /serenity-hub
 
COPY .mvn/ .mvn
RUN chmod +x mvnw
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
CMD ["./mvnw", "spring-boot:run"]