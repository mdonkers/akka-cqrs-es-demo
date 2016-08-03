/*
 * Copyright 2016 Miel Donkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.codecentric.coffee.readside

import akka.actor.Status.Failure
import akka.actor.{ ActorLogging, Props }
import akka.camel.{ Ack, CamelMessage, Consumer }
import cats.data.Xor
import nl.codecentric.coffee.ActorSettings
import nl.codecentric.coffee.domain.User
import org.apache.camel.component.rabbitmq.RabbitMQConstants

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object EventReceiver {
  final val Name = "event-receiver"

  def props(userRepository: UserRepository): Props = Props(new EventReceiver(userRepository))
}

class EventReceiver(userRepository: UserRepository) extends Consumer with ActorSettings with ActorLogging {

  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._
  import context.dispatcher

  override def endpointUri: String = settings.rabbitMQ.uri

  override def autoAck = false

  override def receive: Receive = {
    case msg: CamelMessage =>
      val origSender = sender
      val body: Xor[Error, User] = decode[User](msg.bodyAs[String])

      body.fold({ error =>
        log.error("Could not parse message: {}", msg)
        origSender ! Failure(error)
      }, { user =>
        log.info(
          "Event Received with id {} and for user: {}",
          msg.headers.getOrElse(RabbitMQConstants.MESSAGE_ID, ""),
          user.name
        )
        userRepository.createUser(UserEntity(name = user.name)).onComplete {
          case scala.util.Success(_) => origSender ! Ack // Send ACK when storing User succeeded
          case scala.util.Failure(t) => log.error(t, "Failed to persist user with name: {}", user.name)
        }
      })
    case _ => log.warning("Unexpected event received")
  }

}
