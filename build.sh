#!/usr/bin/env bash
#docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/recording-bot:latest .
docker push $DOCKER_USERNAME/recording-bot
kubectl delete pod -l name=recording -n prod
kubectl get pods -l name=recording -n prod

