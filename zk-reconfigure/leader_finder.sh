#!/usr/bin/env bash

# This script is used to find the leader of zookeeper cluster
# Usage: ./leader_finder.sh

for i in {1..5}; do
  role=$(echo stat | nc localhost 218"$i" | grep Mode)
  if [ "$role" == "Mode: leader" ]; then
    echo "The leader of zookeeper cluster is node $i"
  fi
done