# Backup export tool 
One should use attached [start.sh](start.sh) to run the tool.
Fill variables first.

To run it manually inside the docker execute following:
```bash
docker run --rm -it \
  -v </path/to/database/file>:/etc/backup-export/database-in \
  -v </path/to/output/folder>:/etc/backup-export/database-out \
  -e CLIENT_TYPE=<ios,android,desktop> \ 
  -e WIRE_USER=<user-with-wire-account> \
  -e WIRE_PASSWORD<password-for-that-user> \
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
