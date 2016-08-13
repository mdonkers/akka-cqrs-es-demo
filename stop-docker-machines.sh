#!/bin/bash

stop_del(){
  local name=$1
  local state=$(docker inspect --format "{{.State.Running}}" $name 2>/dev/null)

  if [[ "$state" == "true" ]]; then
    docker stop $name
    docker rm $name
  fi
}

## Cassandra
stop_del akka-cassandra

## MariaDB
stop_del akka-mariadb

## RabbitMQ (with management interface; guest/guest)
stop_del akka-rabbit

