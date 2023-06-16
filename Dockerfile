FROM openjdk:17-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven
# Set the current working directory inside the image
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .

# Copy the pom.xml file
COPY pom.xml .


# Copy the project source
COPY src src

# Package the application
RUN mvn clean install package -DskipTests=true

CMD ["mvn","spring-boot:run"]
