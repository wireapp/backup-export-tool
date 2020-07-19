FROM maven:3.6.3-jdk-8-slim AS build
LABEL description="Wire Recording bot"
LABEL project="wire-bots:recording"

WORKDIR /app

# download dependencies
COPY pom.xml ./
RUN mvn verify --fail-never -U

# build stuff
COPY . ./
RUN mvn -Dmaven.test.skip=true package

FROM dejankovacevic/bots.runtime:2.10.3
# copy database decryption lib
COPY --from=build /app/libs/libsodiumjni.so /opt/wire/lib/libsodiumjni.so
# copy built jars
COPY --from=build /app/target/recording.jar /opt/recording/recording.jar
COPY --from=build /app/recording.yaml /etc/recording/recording.yaml

RUN mkdir /opt/recording/assets
RUN mkdir /opt/recording/avatars
RUN mkdir /opt/recording/html

COPY --from=build /app/src/main/resources/recording/assets/* /opt/recording/assets/

WORKDIR /opt/recording

EXPOSE  8080 8081 8082

CMD ["sh", "-c","/usr/bin/java -Djava.library.path=/opt/wire/lib -jar recording.jar server /etc/recording/recording.yaml"]
