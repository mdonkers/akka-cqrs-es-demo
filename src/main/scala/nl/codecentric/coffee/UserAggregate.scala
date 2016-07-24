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

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import nl.codecentric.coffee.EventSender.{ Confirm, Msg }
import nl.codecentric.coffee.UserAggregate.{ Evt, MsgConfirmed, MsgSent }
import nl.codecentric.coffee.domain._

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object UserAggregate {

  final val Name = "user-aggregate"

  def props(): Props = Props(new UserAggregate())

  sealed trait Evt

  case class MsgSent(s: User) extends Evt

  case class MsgConfirmed(deliveryId: Long) extends Evt

}

class UserAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  import scala.concurrent.duration._

  override val persistenceId: String = "user-aggregate"
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(1 seconds)

  private val userRepository = context.watch(createUserRepository())
  private val eventSender = context.watch(createEventSender())

  protected def createUserRepository(): ActorRef = {
    context.actorOf(UserRepository.props(), UserRepository.Name)
  }

  protected def createEventSender(): ActorRef = {
    context.actorOf(EventSender.props(), EventSender.Name)
  }

  override def receiveCommand: Receive = {
    case GetUsers => // This case will later be moved to the 'write' side
      userRepository forward GetUsers // Forward to keep original sender

    // TODO also track state of persisting the User inside the repository
    // Make sure the repository can handle duplicate messages (keep track of message id's)
    case addUserCmd: AddUser =>
      persist(MsgSent(addUserCmd.user)) { persistedMsg =>
        updateState(persistedMsg)
        val addUserAnswer = userRepository ? addUserCmd
        pipe(addUserAnswer) to sender()
      }

    case Confirm(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
  }

  override def receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
  }

  def updateState(evt: Evt): Unit = evt match {
    case MsgSent(s) =>
      deliver(eventSender.path)(deliveryId => Msg(deliveryId, s))
    case MsgConfirmed(deliveryId) =>
      confirmDelivery(deliveryId)
  }

}
