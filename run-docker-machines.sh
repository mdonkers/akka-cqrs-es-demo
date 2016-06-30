#!/bin/bash

del_stopped(){
  local name=$1
  local state=$(docker inspect --format "{{.State.Running}}" $name 2>/dev/null)

  if [[ "$state" == "false" ]]; then
    docker rm $name
  fi
}

## Cassandra
del_stopped akka-cassandra
docker run --name akka-cassandra -v /app/cassandra:/var/lib/cassandra -p 7000:7000 -p 7001:7001 -p 7199:7199 -p 9042:9042 -p 9160:9160 -d cassandra:latest

## RabbitMQ (with management interface; guest/guest)
del_stopped akka-rabbit
docker run --name akka-rabbit --hostname akka-rabbit -p 8080:15672 -p 15671:15671 -d rabbitmq:3-management

