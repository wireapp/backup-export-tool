FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine AS build
LABEL description="Wire Export Backup Tool"
LABEL project="wire-bots:exports"

ENV PROJECT_ROOT /src
WORKDIR $PROJECT_ROOT

# Copy gradle settings
COPY build.gradle.kts settings.gradle.kts gradle.properties gradlew $PROJECT_ROOT/
# Make sure gradlew is executable
RUN chmod +x gradlew
# Copy gradle specification
COPY gradle $PROJECT_ROOT/gradle
# Download gradle
RUN ./gradlew --version --no-daemon
# download and cache dependencies
RUN ./gradlew resolveDependencies --no-daemon

# Copy project and build
COPY src $PROJECT_ROOT/src
RUN ./gradlew shadowJar --no-daemon

# Runtime
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
RUN apk add bash

ENV APP_ROOT /app
WORKDIR $APP_ROOT

# Obtain built from the base
COPY --from=build /src/build/libs/backup-export.jar $APP_ROOT/

# copy entrypoint
COPY entrypoint.sh $APP_ROOT/entrypoint.sh
# ensure it is executable
RUN chmod +x $APP_ROOT/entrypoint.sh

# copy database decryption lib
RUN mkdir $APP_ROOT/libs
COPY libs/libsodium.so $APP_ROOT/libs/
# copy configuration
COPY export.yaml $APP_ROOT/
COPY export-proxy.yaml $APP_ROOT/

# execute run script
ENTRYPOINT ["/bin/sh", "-c", "$APP_ROOT/entrypoint.sh"]
