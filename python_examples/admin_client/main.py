from confluent_kafka import Producer, Consumer, KafkaError

# Set up producer configuration
producer_conf = {
    'bootstrap.servers': 'localhost:9092',
    'client.id': 'python-producer'
}

# Set up consumer configuration
consumer_conf = {
    'bootstrap.servers': 'localhost:9092',
    'group.id': 'python-consumer',
    'auto.offset.reset': 'earliest'
}

# Create producer and consumer instances
producer = Producer(producer_conf)
consumer = Consumer(consumer_conf)

# Define callback function for delivery reports


def delivery_report(err, msg):
    if err is not None:
        print(f'Message delivery failed: {err}')
    else:
        print(f'Message delivered to {msg.topic()} [{msg.partition()}]')


# Produce a message
producer.produce('test-topic', key='key', value='value',
                 on_delivery=delivery_report)

# Wait for any outstanding messages to be delivered and delivery reports to be received
producer.flush()

# Subscribe to a topic
consumer.subscribe(['test-topic'])

# Consume messages
while True:
    msg = consumer.poll(1.0)

    if msg is None:
        continue

    if msg.error():
        if msg.error().code() == KafkaError._PARTITION_EOF:
            print('Reached end of partition')
        else:
            print(f'Error while consuming message: {msg.error()}')
    else:
        print(f'Received message: {msg.value().decode("utf-8")}')
