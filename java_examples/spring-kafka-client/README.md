# Spring Boot Kafka Client

## Run

### Option 1

```bash
mvn spring-boot:run
```

### Option 2

```bash
java -jar target/app.jar
```

### Option 3 - With Prometheus Java Agent

```bash
java -javaagent:jmx_prometheus_javaagent-0.20.0.jar=9191:prometheus_config.yml -jar target/app.jar --spring.config.location=application-dev.properties
```

### Option 4 - Docker

```bash
docker run -v ${PWD}/application-dev.properties:/application.properties --rm -d --name sample-spring-client rampi88/sample-spring-client:latest sh -c 'java -jar app.jar --spring.config.location=application.properties'
```