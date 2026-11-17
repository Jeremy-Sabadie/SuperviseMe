FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
RUN ./mvnw -B -DskipTests package && \
    find target -maxdepth 1 -name '*.jar' ! -name '*.original' \
      -exec cp {} /tmp/superviseme.jar \;

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build --chown=10001:10001 \
  /tmp/superviseme.jar /app/superviseme.jar
USER 10001:10001
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/superviseme.jar"]
