#!/usr/bin/env bash

# This script is used to start zookeeper servers
# Usage: ./startup.sh

for i in {1..3}; do
  # Create folder for myid 
  mkdir -p /tmp/zk/"$i"
  echo "$i" > /tmp/zk/"$i"/myid
  zookeeper-server-start zoo."$i".properties >> zoo."$i".log 2>&1 &
done


