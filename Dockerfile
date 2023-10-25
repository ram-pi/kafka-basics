FROM azul/zulu-openjdk:17

LABEL owner="ram-pi"
LABEL name="kafka-multitool"

RUN apt update -y \
    && apt install -y netcat \
    && apt install -y curl \
    && apt install -y kafkacat \
    && apt install -y vim \
    && apt install -y dnsutils \
    && apt clean -y
RUN curl https://packages.confluent.io/archive/7.5/confluent-7.5.1.tar.gz -o kafka.tgz \
    && mkdir kafka \
    && tar -xvzf kafka.tgz -C kafka --strip-components=1 \
    && echo "alias kcat=kafkacat" >> ~/.bashrc \
    && echo "export PATH=$PATH:/kafka/bin" >> ~/.bashrc;

CMD ["tail", "-f", "/dev/null"]