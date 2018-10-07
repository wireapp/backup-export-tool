# Alert-bot
[![Build Status](https://travis-ci.org/wireapp/recording-bot.svg?branch=master)](https://travis-ci.org/wireapp/recording-bot)

This is recording bot for Wire.

## Prometheus Alert manager config
```
...

receivers:
  - name: 'alertbot'
    webhook_configs:
    - url: https://services.wire.com/alert/prometheus
      send_resolved: true
      http_config:
          bearer_token: '$ALERT_PROMETHEUS_TOKEN'
          tls_config:
            insecure_skip_verify: true
...

```
## How to trigger a _Prometheus_ alert manually
```
curl 'localhost:8080/alert/prometheus' \
    -H "Authorization:Bearer $ALERT_PROMETHEUS_TOKEN" \
    -H "Content-Type:Application/Json" \
    -d @examples/prometheus.json
```

## How to trigger a _Simple_ alert manually
```
curl 'localhost:8080/alert/simple' \
    -H "Authorization:Bearer $ALERT_PROMETHEUS_TOKEN" \
    -H "Content-Type:Application/Json" \
    -d '{ "message" : "This is just a test" }'
```

## How the rendered alerts look like
![Wire Desktop](https://i.imgur.com/AOQ7Ecq.png)

## How to filter out alerts
```
/label add service ibis
```
This will make that only warnings that contain _label_ `service=ibis` would be displayed
</b>
```
/label remove service ibis
```

## Whitelist users that can receive alerts
comma separated list of Wire `usernames` in `whitelist` in the `config`.
Leave the list empty if you want to allow everybody to join.            