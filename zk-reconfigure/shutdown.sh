#!/usr/bin/env bash

# This script is used to shutdown zookeeper servers
# Usage: ./shutdown.sh <server_id>
# If server_id is not provided, all zookeeper servers will be shutdown

# get first argument
server_id=$1

# if server_id exists, kill it and exit
if [ -n "$server_id" ]; then
  pid=$(ps -fe | grep -i zoo."$server_id.properties" | awk '{print $2}' | head -n 1)
  kill -9 "$pid"
  exit 0
fi

pid_list=$(ps -fe | grep -i zookeeper | awk '{print $2}')

for pid in $pid_list; do
  kill -9 "$pid" &> /dev/null
done