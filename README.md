# Backup export tool 
Tool for exporting and viewing Wire client's backup. 
Currently, supported clients are: webapps (including electron desktop apps), iOS and Android.

## Execution
There are two ways how to run the tool, using Docker (preferred) or Java.

### Docker
One should use attached [start.sh](start.sh) to run the tool.
Fill variables first.

To run it manually inside the docker execute following:
```bash
docker run --rm -it \
  -v </path/to/database/file>:/etc/backup-export/database-in \
  -v </path/to/output/folder>:/etc/backup-export/database-out \
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
  lukaswire/backup-export-tool:latest
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
  -v backups/dejan56.ios_wbu:/etc/backup-export/database-in \
  -v backup-exports/dejan:/etc/backup-export/database-out \
  -e CLIENT_TYPE=ios \ 
  -e WIRE_USER=my-testing-user@wire.com \
  -e WIRE_PASSWORD=VerySecretPassword1! \
  -e BACKUP_PASSWORD=Monkey123! \ 
  -e BACKUP_USERNAME=dejan56 \
  lukaswire/backup-export-tool:latest
```


### Bare metal JVM
One needs C library Libsodium installed. To install it, one should use [official documentation](https://libsodium.gitbook.io/doc/),
or to use included binaries.
One should use Java 8 to run the tool, but Java 11 seems to be working as well.

To create executable `jar` please run `mvn package -DskipTests=true` which produces `target/backup-export.jar`.
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
  export-yaml.yaml
```
