# Backup export tool 
[![GitHub version](https://badge.fury.io/gh/wireapp%2Fbackup-export-tool.svg)](https://github.com/wireapp/backup-export-tool/releases)
![CI/CD](https://github.com/wireapp/backup-export-tool/workflows/CI/CD/badge.svg)

Tool for generating PDF files from the backup.
All backup formats are supported (Desktop, iOS, Android)

## Download
The latest stable version of the tool: [![GitHub version](https://badge.fury.io/gh/wireapp%2Fbackup-export-tool.svg)](https://github.com/wireapp/backup-export-tool/releases)
Please use this version as the tag for the docker image - `lukaswire/backup-export-tool:<version>`.
If you'd like to run the tool on bare metal, download all assets on the [release page](https://github.com/wireapp/backup-export-tool/releases). 

## DL;DR
How to generate PDF files using Desktop app backup file
```bash
java -jar backup-export.jar desktop-pdf -in "mybackup.desktop_wbu" -e "dejan56@wire.com" -p "MyCoolPasswordForWire1" export.yaml
```

## Execution
There are two ways how to run the tool, using Docker (preferred) or Java.

### Docker
One should use attached [start.sh](start.sh) to run the tool.
Fill variables first.

To run it manually inside the docker execute following:
```bash
docker run --rm -it \
  -v </path/to/database/file>:/app/database-in \
  -v </path/to/output/folder>:/app/database-out \
  -e CLIENT_TYPE=<ios,android,desktop> \ 
  -e WIRE_USER=<user-with-wire-account> \
  -e WIRE_PASSWORD=<password-for-that-user> \
  -e BACKUP_PASSWORD=<password-for-backup-file> \ 
  -e BACKUP_USERNAME=<username-who-created-backup> \
  -e WIRE_API_HOST=<url-to-wire-backend> \
  -e USE_PROXY=true \
  -e PROXY_URL=<proxy-url> \
  -e PROXY_PORT=<proxy-port> \
  -e NON_PROXY_HOSTS=<proxy-hosts> \
  lukaswire/backup-export-tool:<version>
```
Where `<some value>` should be replaced by your own value. 
Following variables are optional:
- `PROXY_URL`, `PROXY_PORT`, `NON_PROXY_HOSTS` - proxy settings, use only if you need it, also
if you specify these values, you must also add `-e USE_PROXY=true` to activate proxy
- `WIRE_API_HOST` - URL to Wire backend, default value is used Wire public cloud
- `BACKUP_PASSWORD` and `BACKUP_USERNAME` - use only if `CLIENT_TYPE` is `ios` or `android` 

Example:
```bash
docker run --rm -it \
  -v backups/dejan56.ios_wbu:/app/database-in \
  -v backup-exports/dejan:/app/database-out \
  -e CLIENT_TYPE=ios \ 
  -e WIRE_USER=my-testing-user@wire.com \
  -e WIRE_PASSWORD=VerySecretPassword1! \
  -e BACKUP_PASSWORD=Monkey123! \ 
  -e BACKUP_USERNAME=dejan56 \
  lukaswire/backup-export-tool:1.1.3
```


### Bare metal JVM
One needs C library Libsodium installed. To install it, one should use [official documentation](https://libsodium.gitbook.io/doc/),
or to use included binaries.
One should use Java 8 (OpenJDK) to run the tool, but Java 11 (OpenJDK) seems to be working as well.
We observed some problems running the tool under Windows and Oracle JDK, thus OpenJDK is required.

Almost all parameters are set using arguments to the tool, however, in order to use different Wire backend,
one must set environmental variable with backend URL:
```bash
WIRE_API_HOST=<your-wire-backend>
```
For example to set it on unix systems: `export WIRE_API_HOST=https://staging-nginz-https.zinfra.io` for staging.

To create executable `jar` please run `./gradlew shadowJar` which produces `build/libs/backup-export.jar`.
Generic way how to run the tool is following:
```bash
java -Djna.library.path=<path-to-binaries> \
  -Xmx4g \
  -jar <path-to-jar> \
  <ios-pdf,android-pdf,desktop-pdf> \
  -in "</path/to/database/file>" \
  -out "</path/to/output/folder>" \
  -e "<user-with-wire-account>" \
  -p "<password-for-that-user>" \
  -u "<username-who-created-backup>" \
  -bp "<password-for-backup-file>" \
  <export.yaml,export-proxy.yaml>
```

An example for iOS backup without proxy (the build is downloaded and extracted `zip` from the release page)
```bash
java -Djna.library.path=libs \
  -Xmx4g \
  -jar backup-export.jar \
  ios-pdf \
  -in "backups/dejan56.ios_wbu" \
  -out "dejans-export" \
  -e "dejan56@wire.com" \
  -p "MyCoolPasswordForWire1" \
  -u "dejan56" \
  -bp "AnotherCoolPasswordForBackups" \
  export.yaml
```

If one needs to use proxy, `export-proxy.yaml` must be modified - replace `${PROXY_URL:}`, 
`${PROXY_PORT:-8080}` and `${NON_PROXY_HOSTS:-}` with correct values and execute (example with web client): 
```bash
java -Djna.library.path=libs \
  -Xmx4g \
  -jar backup-export.jar \
  desktop-pdf \
  -in "backups/Wire-fredjones_Demo_Backup.zip" \
  -out "backup-exports" \
  -e "dejan56@wire.com" \
  -p "MyCoolPasswordForWire1" \
  export-proxy.yaml
```
