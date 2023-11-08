# Kafka-Examples

## Producing messages (GOT quotes) - 100 messages each 60 seconds

```bash
mvn install 
mvn exec:java -pl producer -Dexec.mainClass=io.confluent.prametta.producer.MyProducer
```

## Consuming messages (GOT quotes)

```bash
mvn install
mvn exec:java -pl consumer -Dexec.mainClass=com.github.prametta.consumer.MyConsumer
```