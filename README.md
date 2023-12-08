# Reactive JsonGenerator

This is an attempt to implement reactive media support for JSON in Helidon SE <= 3.x.
The goal is to avoid buffering serialized entities.

```bash
mvn clean package
curl -vv http://localhost:8080\?size\=200 | jq
```
