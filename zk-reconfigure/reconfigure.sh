#!/usr/bin/env bash

# Start 3 nodes
./startup.sh

# Close 1 node at a time and reconfigure node 1
./shutdown.sh 3
sleep 5

./shutdown.sh 2
sleep 5

./shutdown.sh 1
sleep 5

# reconfigure 
zookeeper-server-start zoo.1.standalone.properties >> zoo.1.log 2>&1 &