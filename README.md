# Kafka Examples  

## Requirements

- [docker, docker-compose](https://docs.docker.com/get-docker/)
- [kcat](https://github.com/edenhill/kcat)
- [kafka CLI utlities](https://kafka.apache.org/downloads)
- [jr](https://github.com/ugol/jr)
- [python3](https://www.python.org/downloads/)

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

### WITH COOPEERATIVE REBALANCING ###
# SHELL 1
kcat -b localhost:9092 -X partition.assignment.strategy=cooperative-sticky  -G mygroup shoes

# SHELL 2
kcat -b localhost:9092 -X partition.assignment.strategy=cooperative-sticky  -G mygroup shoes
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

## Compacted topic

```
kafka-topics --bootstrap-server localhost:9092 --delete --topic test 
kafka-topics --bootstrap-server localhost:9091 --create --topic test --replication-factor 3 --partitions 1 --config min.insync.replicas=2 --config cleanup.policy=compact --config min.cleanable.dirty.ratio=0.0 --config max.compaction.lag.ms=100 --config segment.ms=100 --config delete.retention.ms=100
kafka-topics --bootstrap-server localhost:9092 --describe --topic test 
kcat -b localhost:9092 -t test -P -K : -l data.txt

kcat -C -b localhost:9092 -t test \
 -f 'Key is %k, and message payload is: %s \n'

# ACTIVE SEGMENT ARE NOT ELIGIBLE FOR LOG COMPACTION -> FORCE COMPACTION WITH ONE NEW MESSAGE
echo "key9:message21" | kcat -b localhost:9092 -P -t test -K:
sleep 5

kcat -C -b localhost:9092 -t test \
 -f 'Key is %k, and message payload is: %s \n'
```

## Transactional Producer 

```
kafka-topics --bootstrap-server localhost:9092 --delete --topic test 
kafka-topics --bootstrap-server localhost:9091 --create --topic test --replication-factor 3 --partitions 1 --config min.insync.replicas=2

# SHELL 1 
kcat -C -b localhost:9092 -X isolation.level=read_uncommitted -t test \
 -f 'Key is %k, and message payload is: %s \n'

# SHELL 2 
kcat -C -b localhost:9092 -t test \
 -f 'Key is %k, and message payload is: %s \n'

# SHELL 3
# Python Transactional Producer SHELL 3
pip install -r python_examples/requirements.txt
python3 python_examples/transactional_producer.py
```

## ACLs 

```
docker-compose -f docker-compose.scram.yaml up -d

# CREATE USER
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=alice-secret],SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice"
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"

# CREATE TOPIC WITH SASL CREDENTIALS
kafka-topics --bootstrap-server localhost:9092 --command-config kafka/admin.properties --delete --topic test
kafka-topics --bootstrap-server localhost:9092 --command-config kafka/admin.properties --create --topic test

# SET ACLs
kafka-acls --bootstrap-server localhost:9092 \
  --command-config kafka/admin.properties  \
  --add \
  --allow-principal User:alice \
  --operation all \
  --topic test

kafka-acls --bootstrap-server localhost:9092 \
  --command-config kafka/admin.properties  \
  --add \
  --deny-principal User:alice \
  --operation delete \
  --topic test

# ALLOWED OPERATION
echo "test" | kcat -b localhost:9092 -P -t test -X security.protocol=SASL_PLAINTEXT -X sasl.mechanism=SCRAM-SHA-256 -X sasl.username=alice -X sasl.password=alice-secret
echo "test" | kcat -b localhost:9092 -C -o beginning -t test -X security.protocol=SASL_PLAINTEXT -X sasl.mechanism=SCRAM-SHA-256 -X sasl.username=alice -X sasl.password=alice-secret

# NOT ALLOWED OPERATION
kafka-topics --bootstrap-server localhost:9092 --command-config kafka/alice.properties --delete --topic test

docker-compose -f docker-compose.scram.yaml down -d
```