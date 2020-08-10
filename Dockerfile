FROM maven:3.6.3-jdk-8-slim AS build
LABEL description="Wire Export Backup Tool"
LABEL project="wire-bots:exports"

WORKDIR /app

# download dependencies
COPY pom.xml ./
RUN mvn verify --fail-never -U

# build stuff
COPY src ./src
RUN mvn -Dmaven.test.skip=true package

FROM dejankovacevic/bots.runtime:2.10.3
# copy entrypoint
COPY entrypoint.sh /opt/backup-export/entrypoint.sh
RUN chmod +x /opt/backup-export/entrypoint.sh
# copy database decryption lib
COPY libs/libsodium.so /opt/wire/lib/libsodium.so
# copy configuration
COPY export.yaml /etc/backup-export/export.yaml
COPY export-proxy.yaml /etc/backup-export/export-proxy.yaml

# copy built jars
COPY --from=build /app/target/backup-export.jar /opt/backup-export/backup-export.jar

RUN mkdir /opt/backup-export/assets
RUN mkdir /opt/backup-export/avatars
RUN mkdir /opt/backup-export/html

COPY --from=build /app/src/main/resources/recording/assets/* /opt/backup-export/assets/

WORKDIR /opt/backup-export

ENTRYPOINT ["/bin/bash", "-c", "/opt/backup-export/entrypoint.sh"]
