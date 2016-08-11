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
docker run --name akka-cassandra -v /tmp/docker/cassandra:/var/lib/cassandra -p 7000:7000 -p 7001:7001 -p 7199:7199 -p 9042:9042 -p 9160:9160 -d cassandra:latest

## MariaDB
del_stopped akka-mariadb
docker run --name akka-mariadb -v /tmp/docker/mariadb:/var/lib/mysql -p 3306:3306 -e MYSQL_DATABASE=COFFEEDB \
  -e MYSQL_USER=COFFEE -e MYSQL_PASSWORD=secret-coffee-pw -e MYSQL_ROOT_PASSWORD=secret-root-pw -d mariadb:latest

## RabbitMQ (with management interface; guest/guest)
del_stopped akka-rabbit
docker run --name akka-rabbit --hostname akka-rabbit -p 5671:5671 -p 5672:5672 -p 8180:15672 -p 15671:15671 -d rabbitmq:3-management

