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

import akka.actor.{ Actor, ActorLogging, Props }
import nl.codecentric.coffee.domain.User

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object EventSender {

  case class Msg(deliveryId: Long, user: User)

  case class Confirm(deliveryId: Long)

  final val Name = "event-sender"

  def props(): Props = Props(new EventSender())
}

class EventSender extends Actor with ActorLogging {

  import EventSender._

  override def receive: Receive = {
    case Msg(deliveryId, user) =>
      log.info("Received msg for user: {}", user.name)
      // ...
      sender() ! Confirm(deliveryId)
  }
}
