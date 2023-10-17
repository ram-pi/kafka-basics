FROM azul/zulu-openjdk:17

LABEL owner="ram-pi"
LABEL name="kafka-multitool"

RUN apt update -y \
    && apt install -y netcat \
    && apt install -y curl \
    && apt install -y kafkacat \
    && apt clean -y
RUN curl https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz -o kafka.tgz \
    && mkdir kafka \
    && tar -xvzf kafka.tgz -C kafka --strip-components=1 \
    && echo "export PATH=$PATH:/kafka/bin" >> ~/.bashrc;

CMD ["tail", "-f", "/dev/null"]