FROM maven:3.8.4-openjdk-17

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

EXPOSE 8080
CMD java -Dspring.profiles.active=prod -jar target/resume-ai-backend-0.0.1-SNAPSHOT.jar