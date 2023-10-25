# Kafka Examples

## Requirements

- [docker, docker-compose](https://docs.docker.com/get-docker/)
- [kcat](https://github.com/edenhill/kcat)
- [java + maven](https://sdkman.io/)
- [kafka CLI utlities](https://kafka.apache.org/downloads)
- [jr](https://github.com/ugol/jr)
- [python3](https://www.python.org/downloads/)
- [jq](https://jqlang.github.io/jq/download/)
- [ksql-datagen](https://docs.ksqldb.io/en/0.10.1-ksqldb/developer-guide/test-and-debug/generate-custom-test-data/)
- [curl](https://curl.se/)
- [kind](https://kind.sigs.k8s.io/)

### JR Config Generator

<details>
<summary>Configuration</summary>
<br>

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

</details>

## Docker Kafka Toolbox

<details>
<summary>Example</summary>
<br>

```
docker run --rm -d --name kafka-multitool rampi88/kafka-multitool:v1
docker exec -it kafka-multitool bash
docker stop kafka-multitool

# build and push multi-tool
docker build -t rampi88/kafka-multitool:v1 .
```

</details>

## Partitioning

<details>
<summary>Example</summary>
<br>

```
kafka-topics --bootstrap-server localhost:9092 --delete --topic test
kafka-topics --bootstrap-server localhost:9091 --create --topic test --replication-factor 3 --partitions 3 --config min.insync.replicas=2
kafka-topics --bootstrap-server localhost:9092 --describe --topic test
kcat -b localhost:9092 -t test -P -K : -l data.txt
kcat -C -b localhost:9092 -t test \
 -f 'Topic %t - Partition %p: Key is %k, and message payload is: %s \n'
```

</details>

## Consumer Group - Rebalancing

<details>
<summary>Example</summary>
<br>

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

</details>

## ACKs and NOT ENOUGH REPLICAS

<details>
<summary>Example</summary>
<br>

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

</details>

## Compacted topic

<details>
<summary>Example</summary>
<br>

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

</details>

## Transactional Producer

<details>
<summary>Example</summary>
<br>

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

</details>

## ACLs

<details>
<summary>Example</summary>
<br>

```
docker-compose -f docker-compose.scram.yaml up -d

# CREATE USER
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=alice-secret],SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice"

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

# DENIED OPERATION
kafka-topics --bootstrap-server localhost:9092 --command-config kafka/alice.properties --delete --topic test

docker-compose -f docker-compose.scram.yaml down -d
```

</details>

## Schema Registry

<details>
<summary>Example</summary>
<br>

```
# GENERATE RANDOM DATA
# jr run shoe_order -o kafka -t shoe_order -s --serializer json-schema -f 1s -d 10m
ksql-datagen value-format=avro quickstart=pageviews msgRate=1 bootstrap-server=localhost:9092 topic=pageviews iterations=100

curl localhost:8081/subjects/

kcat -b localhost:9092 -t pageviews -s value=avro -r http://localhost:8081 -C -o beginning
```

</details>

## Kafka Connect

<details>
<summary>Example</summary>
<br>

```
curl --request PUT \
  --url http://localhost:8083/connectors/transactions/config \
  --header 'content-type: application/json' \
  --data '{"connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector", "max.interval": 1000, "iterations": 100, "value.converter": "io.confluent.connect.avro.AvroConverter", "quickstart": "transactions", "kafka.topic": "transactions", "value.converter.schema.registry.url": "http://schema-registry:8081"}'

curl localhost:8083/connectors | jq
curl localhost:8083/connectors/transactions/status | jq
curl -X DELETE localhost:8083/connectors/transactions | jq
```

</details>

## ConfigProvider

<details>
<summary>Example</summary>
<br>

```
docker-compose -f docker-compose.scram.yaml up -d
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"

kafka-topics --bootstrap-server localhost:9092 --command-config kafka/admin_with_file_config_provider.properties --create --topic test
kafka-topics --bootstrap-server localhost:9092 --command-config kafka/admin_with_file_config_provider.properties --list

```

</details>

## Prometheus JMX exporter

<details>
<summary>Example</summary>
<br>

```
# SHELL 1
docker-compose -f docker-compose.kraft.yml up -d
export KAFKA_OPTS="-javaagent:volumes/jmx_prometheus_javaagent-0.20.0.jar=9191:volumes/kafka_client.yml"
kafka-topics --bootstrap-server localhost:9092 --create --topic test
kafka-console-consumer --bootstrap-server localhost:9092 --topic test

# SHELL 2
curl localhost:9191
```

## Schema Registry Maven Plugin

<details>
<summary>Example</summary>
<br>

```
cd java_examples/kafka-examples
```

</details>

</details>

## Kafka Streams Java example

<details>
<summary>Example</summary>
<br>

```
cd java_examples/kafka-examples
mvn package
# Shell 1 - Produce
java -javaagent:jmx_prometheus_javaagent-0.20.0.jar=9191:prometheus_config.yml -cp producer/target/producer-1.0-SNAPSHOT.jar com.github.prametta.producer.MyBeerProducer
# Shell 2 - Consumer
java -javaagent:jmx_prometheus_javaagent-0.20.0.jar=9192:prometheus_config.yml -cp consumer/target/consumer-1.0-SNAPSHOT.jar com.github.prametta.consumer.MyBeerConsumer
# Shell 3 - Process
java -javaagent:jmx_prometheus_javaagent-0.20.0.jar=9193:prometheus_config.yml -cp  streams/target/streams-1.0-SNAPSHOT.jar com.github.prametta.streams.MyKafkaBeerStreamApp
```

</details>

## Kafka Streams Java example on docker-compose

<details>
<summary>Example</summary>
<br>

```
cd java_examples/kafka-examples
mvn package
cd ../../
docker-compose -f docker-compose.kraft.clients.yml up -d --build
# tierdown
docker-compose -f docker-compose.kraft.clients.yml down -v
```

</details>

## Confluent for Kubernetes - Dynamic client switch from cluster 1 to cluster 2 ([Reloader](https://github.com/stakater/Reloader]))

<details>
<summary>Example</summary>
<br>

```
cd k8s
alias k="kubectl"
kind create cluster
# Create namespaces
k create namespace confluent
k create namespace confluent-dr
k config set-context --current --namespace=confluent

# install Reloader
kubectl apply -f https://raw.githubusercontent.com/stakater/Reloader/master/deployments/kubernetes/reloader.yaml

# Install Confluent for Kubernetes Operator
helm repo add confluentinc https://packages.confluent.io/helm
helm repo update
helm upgrade --install confluent-operator \
  confluentinc/confluent-for-kubernetes \
  --set namespaced=false \
  --namespace confluent

# install the confluent kubernetes plugin (OPTIONAL)
... [Confluent Plugin](https://docs.confluent.io/operator/current/co-deploy-cfk.html#co-install-plugin)

# Create main cluster
k apply -f cluster.1.yaml

# Create DR cluster
k config set-context --current --namespace=confluent-dr
k apply -f cluster.2.yaml

# Create conifgmap used by the producer
k create configmap client-properties --from-file=client.properties

# Create the producer - the client will produce to the main cluster
k apply -f producer.dpl.yaml

# Edit the configmap and auto reboot the producer - the client will produce against the DR cluster now
kubectl edit configmap client-properties # change the bootstrap server to kafka.confluent-dr
```

</details>

## Confluent for Kubernetes - Injecting secrets with Vault (TODO)

<details>
<summary>Example</summary>
<br>

```
cd k8s
alias k="kubectl"
kind create cluster
# Create namespaces
k create namespace confluent
k config set-context --current --namespace=confluent

# install Vault
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update
helm install vault hashicorp/vault --set "server.dev.enabled=true"

# Install Confluent for Kubernetes Operator
helm repo add confluentinc https://packages.confluent.io/helm
helm repo update
helm upgrade --install confluent-operator \
  confluentinc/confluent-for-kubernetes \
  --set namespaced=false \
  --namespace confluent

# install the confluent kubernetes plugin (OPTIONAL)
... [Confluent Plugin](https://docs.confluent.io/operator/current/co-deploy-cfk.html#co-install-plugin)

# Create main cluster
k apply -f cluster.1.yaml

# Create conifgmap used by the producer
k create configmap client-properties --from-file=client.properties

# Create the producer - the client will produce to the main cluster
k apply -f producer.dpl.yaml
```

</details>

## Confluent for Kubernetes - Consumer AutoScaling with KEDA (TODO)

<details>
<summary>Example</summary>
<br>

```
cd k8s
alias k="kubectl"
kind create cluster
# Create namespaces
k create namespace confluent
k config set-context --current --namespace=confluent

# Install Confluent for Kubernetes Operator
helm repo add confluentinc https://packages.confluent.io/helm
helm repo update
helm upgrade --install confluent-operator \
  confluentinc/confluent-for-kubernetes \
  --set namespaced=false \
  --namespace confluent

# install the confluent kubernetes plugin (OPTIONAL)
... [Confluent Plugin](https://docs.confluent.io/operator/current/co-deploy-cfk.html#co-install-plugin)

# Create main cluster
k apply -f cluster.1.yaml

# Install KEDA
helm repo add kedacore https://kedacore.github.io/charts
helm repo update

```

</details>

## MirrorMaker2 with ACLs

<details>
<summary>Example</summary>
<br>

```
docker-compose -f docker-compose.migration.yaml up -d

# Create Users in Cluster A (Source)
docker exec -it kafka1 sh -c "kafka-configs --bootstrap-server kafka1:19091 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"
docker exec -it kafka1 sh -c "kafka-configs --bootstrap-server kafka1:19091 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=alice-secret],SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice"
# Create Users in Cluster B (Destination)
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=alice-secret],SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice"

# Open a bash session from kafka-multitool
docker exec -it kafka-multitool bash

# Create topics on Cluster A
kafka-topics --bootstrap-server kafka1:9091 --command-config /tmp/admin.properties --create --topic Test-mytopic

# Set prefixed ACLs on Cluster A
kafka-acls --bootstrap-server kafka1:9091 \
  --command-config /tmp/admin.properties \
  --add \
  --allow-principal User:alice \
  --operation all \
  --topic Test- \
  --resource-pattern-type prefixed

# Set literal ACLs on Cluster A
kafka-acls --bootstrap-server kafka1:9091 \
  --command-config /tmp/admin.properties \
  --add \
  --allow-principal User:alice \
  --operation all \
  --topic Test-mytopic

# Run MirrorMaker2 from kafka-multitool
connect-mirror-maker /tmp/mm2.properties

# List Topics from both clusters
kafka-topics --bootstrap-server kafka1:9091 --command-config /tmp/admin.properties --list
kafka-topics --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --list

# List ACLs from both clusters
kafka-acls --bootstrap-server kafka1:9091 --command-config /tmp/admin.properties --list
kafka-acls --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --list

# Note that only the literal ACL has been migrated

docker-compose -f docker-compose.migration.yaml down -d

```

</details>

## ClusterLinking ACLs migration

<details>
<summary>Example</summary>
<br>

```
docker-compose -f docker-compose.migration.yaml up -d

# Create Users in Cluster A (Source)
docker exec -it kafka1 sh -c "kafka-configs --bootstrap-server kafka1:19091 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"
docker exec -it kafka1 sh -c "kafka-configs --bootstrap-server kafka1:19091 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=alice-secret],SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice"
# Create Users in Cluster B (Destination)
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=admin-secret],SCRAM-SHA-512=[password=admin-secret]' --entity-type users --entity-name admin"
docker exec -it kafka2 sh -c "kafka-configs --bootstrap-server kafka2:19092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=alice-secret],SCRAM-SHA-512=[password=alice-secret]' --entity-type users --entity-name alice"

# Open a bash session from kafka-multitool
docker exec -it kafka-multitool bash

# Create topics on Cluster A
kafka-topics --bootstrap-server kafka1:9091 --command-config /tmp/admin.properties --create --topic Test-mytopic

# Set prefixed ACLs on Cluster A
kafka-acls --bootstrap-server kafka1:9091 \
  --command-config /tmp/admin.properties \
  --add \
  --allow-principal User:alice \
  --operation all \
  --topic Test- \
  --resource-pattern-type prefixed

# Set literal ACLs on Cluster A
kafka-acls --bootstrap-server kafka1:9091 \
  --command-config /tmp/admin.properties \
  --add \
  --allow-principal User:alice \
  --operation all \
  --topic Test-mytopic

# Create Cluster Link
# https://docs.confluent.io/platform/current/multi-dc-deployments/cluster-linking/configs.html#configuration-options
kafka-cluster-links --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --create --link demo-link \
--config-file /tmp/link.properties --acl-filters-json-file /tmp/acl_filters.json \
--consumer-group-filters-json-file /tmp/consumer_group_filters.json  \
--topic-filters-json-file /tmp/topic_filters.json

kafka-cluster-links --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --list
kafka-cluster-links --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --link demo-link --describe

# List Topics from both clusters
kafka-topics --bootstrap-server kafka1:9091 --command-config /tmp/admin.properties --list
kafka-topics --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --list

# List ACLs from both clusters
kafka-acls --bootstrap-server kafka1:9091 --command-config /tmp/admin.properties --list
kafka-acls --bootstrap-server kafka2:9092 --command-config /tmp/admin.properties --list

# Note that only the literal ACL has been migrated

docker-compose -f docker-compose.migration.yaml down -d

```

</details>
