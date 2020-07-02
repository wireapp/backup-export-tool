FROM dejankovacevic/bots.runtime:2.10.3

COPY target/recording.jar   /opt/recording/recording.jar
COPY recording.yaml         /etc/recording/recording.yaml

RUN mkdir /opt/recording/assets
RUN mkdir /opt/recording/avatars
RUN mkdir /opt/recording/html

COPY src/main/resources/recording/assets/*         /opt/recording/assets/

WORKDIR /opt/recording
     
EXPOSE  8080 8081 8082

CMD ["sh", "-c","/usr/bin/java -Djava.library.path=/opt/wire/lib -jar recording.jar server /etc/recording/recording.yaml"]
