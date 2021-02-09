build:
	./gradlew shadowJar

docker-build:
	docker build -t lukaswire/backup-export-tool .

docker-push:
	docker push lukaswire/backup-export-tool
