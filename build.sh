#!/usr/bin/env bash
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t dejankovacevic/recording-bot:latest .
docker push dejankovacevic/recording-bot
kubectl delete pod -l name=recording -n prod
kubectl get pods -l name=recording -n prod

