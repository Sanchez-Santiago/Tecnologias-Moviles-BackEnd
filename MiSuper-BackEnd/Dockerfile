FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/MiSuper-BackEnd ./
EXPOSE 8080
CMD ["./bin/MiSuper-BackEnd"]
