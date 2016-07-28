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

import akka.actor.{ Actor, ActorLogging, ActorPath, Props, Status }
import akka.camel.{ CamelMessage, Producer }
import nl.codecentric.coffee.domain.User
import org.apache.camel.component.rabbitmq.RabbitMQConstants

import scala.collection.immutable

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object EventSender {

  final val Name = "event-sender"

  def props(): Props = Props(new EventSender())

  case class Msg(deliveryId: Long, user: User)

  case class Confirm(deliveryId: Long)
}

class EventSender extends Actor with ActorLogging {
  import io.circe.generic.auto._
  import io.circe.syntax._
  import EventSender._

  private val camelSender = context.watch(context.actorOf(Props[CamelSender]))

  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  override def receive: Receive = {
    case Msg(deliveryId, user) =>
      log.info("Sending msg for user: {}", user.name)
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      val headersMap = Map(RabbitMQConstants.MESSAGE_ID -> deliveryId, RabbitMQConstants.CORRELATIONID -> deliveryId)
      camelSender ! CamelMessage(user.asJson.noSpaces, headersMap)

    case CamelMessage(_, headers) =>
      val deliveryId: Long = headers.getOrElse(RabbitMQConstants.MESSAGE_ID, -1L).asInstanceOf[Long]
      log.info("Event successfully delivered for id {}, sending confirmation", deliveryId)
      unconfirmed
        .get(deliveryId)
        .foreach(
          senderActor => {
            unconfirmed -= deliveryId
            context.actorSelection(senderActor) ! Confirm(deliveryId)
          }
        )

    case Status.Failure(ex) =>
      log.error("Event delivery failed. Reason: {}", ex.toString)
  }
}

class CamelSender extends Actor with Producer with ActorSettings {
  override def endpointUri: String = settings.rabbitMQ.uri

  override def headersToCopy: Set[String] =
    super.headersToCopy + RabbitMQConstants.CORRELATIONID + RabbitMQConstants.MESSAGE_ID
}
