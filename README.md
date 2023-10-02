# Kafka Walkthrouth 

## Requirements

- [docker, docker-compose](https://docs.docker.com/get-docker/)
- [kcat](https://github.com/edenhill/kcat)
- [kafka CLI utlities](https://kafka.apache.org/downloads)
- [jr](https://github.com/ugol/jr)

### JR Config Generator

```
{
  "emitters": [
    {
      "name": "shoe",
      "locale": "us",
      "num": 1,
      "frequency": "5s",
      "duration": "10m",
      "preload": 10,
      "valueTemplate": "shoe",
      "output": "kafka",
      "keyTemplate": "null",
      "outputTemplate": "{{.V}}\n",
      "topic": "shoes"
    },
    {
      "name": "shoe_customer",
      "locale": "us",
      "num": 1,
      "frequency": "1s",
      "duration": "1s",
      "preload": 5,
      "valueTemplate": "shoe_customer",
      "output": "kafka",
      "keyTemplate": "null",
      "outputTemplate": "{{.V}}\n",
      "topic": "shoe_customers"
    },
    {
      "name": "shoe_order",
      "locale": "us",
      "num": 1,
      "frequency": "500ms",
      "duration": "1s",
      "preload": 0,
      "valueTemplate": "shoe_order",
      "output": "kafka",
      "keyTemplate": "null",
      "outputTemplate": "{{.V}}\n",
      "topic": "shoe_orders"
    },
    {
      "name": "shoe_clickstream",
      "locale": "us",
      "num": 1,
      "frequency": "100ms",
      "duration": "1s",
      "preload": 0,
      "valueTemplate": "shoe_clickstream",
      "output": "kafka",
      "keyTemplate": "null",
      "outputTemplate": "{{.V}}\n",
      "topic": "shoe_clickstream"
    }
  ],
  "global": {
    "seed": -1,
    "kafkaConfig": "./kafka/config.properties",
    "schemaRegistry": false,
    "registryConfig": "./kafka/registry.properties",
    "serializer": "json-schema",
    "autoCreate": true,
    "redisTtl": "1m",
    "redisConfig": "./redis/config.json",
    "mongoConfig": "./mongoDB/config.json",
    "elasticConfig": "./elastic/config.json",
    "s3Config": "./s3/config.json",
    "url": ""
  }
}
```

## Partitioning 

```
kafka-topics --bootstrap-server localhost:9092 --delete --topic test 
kafka-topics --bootstrap-server localhost:9091 --create --topic test --replication-factor 3 --partitions 3 --config min.insync.replicas=2
kafka-topics --bootstrap-server localhost:9092 --describe --topic test 
kcat -b localhost:9092 -t test -P -K : -l data.txt
kcat -C -b localhost:9092 -t test \
 -f 'Topic %t - Partition %p: Key is %k, and message payload is: %s \n'
```

## Consumer Group - Rebalancing

```
kafka-topics --bootstrap-server localhost:9092 --delete --topic shoes
kafka-topics --bootstrap-server localhost:9092 --create --topic shoes --replication-factor 3 --partitions 6 --config min.insync.replicas=2

kafka-topics --bootstrap-server localhost:9092 --describe --topic shoes

# GENERATE RANDOM DATA
jr emitter run shoe 

# SHELL 1
kcat -b localhost:9092 -G mygroup shoes

# SHELL 2
kcat -b localhost:9092 -G mygroup shoes
```

## ACKs and NOT ENOUGH REPLICAS

```
kafka-topics --bootstrap-server localhost:9092 --delete --topic test
kafka-topics --bootstrap-server localhost:9092 --create --topic test --replication-factor 3 --partitions 6 --config min.insync.replicas=2

kafka-topics --bootstrap-server localhost:9092 --describe --topic test

# PRODUCING WITH ACKs ALL
echo "test" | kafka-console-producer --bootstrap-server localhost:9092 --topic test

# CONSUMING
kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning --timeout-ms 5000

# STOP BROKER kafka1
docker stop kafka1

echo "test" | kafka-console-producer --bootstrap-server localhost:9092 --topic test

kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning --timeout-ms 5000

# STOP BROKER kafka3
docker stop kafka3

echo "test" | kafka-console-producer --bootstrap-server localhost:9092 --topic test

kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning --timeout-ms 5000

kafka-topics --bootstrap-server localhost:9092 --describe --topic test

# PRODUCING WITH ACK=1
echo "test" | kafka-console-producer --bootstrap-server localhost:9092 --topic test --request-required-acks 1

```