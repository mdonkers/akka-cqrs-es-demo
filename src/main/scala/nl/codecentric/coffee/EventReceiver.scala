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

package nl.codecentric.coffee

import akka.actor.{ ActorLogging, Props }
import akka.camel.{ CamelMessage, Consumer }
import cats.data.Xor
import nl.codecentric.coffee.domain.User

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object EventReceiver {
  final val Name = "event-receiver"

  def props(): Props = Props(new EventReceiver())
}

class EventReceiver extends Consumer with ActorLogging {

  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._

  override def endpointUri: String = "rabbitmq://192.168.99.100:5672/userevents?username=guest&password=guest"

  override def receive: Receive = {
    case msg: CamelMessage =>
      val body: Xor[Error, User] = decode[User](msg.bodyAs[String])
      body.fold({ error =>
        log.error("Could not parse message: {}", msg)
      }, { user =>
        log.info("Event Received: {}", user.name)
      })
    case _ => log.warning("Unexpected event received")
  }

}
