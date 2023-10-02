from confluent_kafka import Producer
import time

p = Producer(
    {
        'bootstrap.servers': 'localhost:9092',
        'transactional.id': 'txn-1'
    }
)


def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))


p.init_transactions()
p.begin_transaction()

for x in range(5):

    # Trigger any available delivery report callbacks from previous produce() calls
    p.poll(0)

    # Asynchronously produce a message. The delivery report callback will
    # be triggered from the call to poll() above, or flush() below, when the
    # message has been successfully delivered or failed permanently.
    p.produce('test', key='key' + str(x), value=str(x), callback=delivery_report)

time.sleep(10)
p.commit_transaction()
# Wait for any outstanding messages to be delivered and delivery report
# callbacks to be triggered.
p.flush()
