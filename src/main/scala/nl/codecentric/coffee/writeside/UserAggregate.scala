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

package nl.codecentric.coffee.writeside

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import nl.codecentric.coffee.domain._
import nl.codecentric.coffee.writeside.EventSender.{ Confirm, Msg }
import nl.codecentric.coffee.writeside.UserAggregate.{ Evt, GetUsersForwardResponse, MsgAddUser, MsgConfirmed }
import nl.codecentric.coffee.writeside.UserRepository.{ AddUser, ConfirmAddUser, GetUsers }

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object UserAggregate {

  final val Name = "user-aggregate"

  def props(): Props = Props(new UserAggregate())

  sealed trait Evt

  final case class MsgAddUser(u: User) extends Evt

  final case class MsgConfirmed(deliveryId: Long) extends Evt

  final case class GetUsersForwardResponse(senderActor: ActorRef, existingUsers: Set[User], newUser: User)
}

class UserAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  import scala.concurrent.duration._

  override val persistenceId: String = "user-aggregate"
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(100 milliseconds)

  private val userRepository = context.watch(createUserRepository())
  private val eventSender = context.watch(createEventSender())

  protected def createUserRepository(): ActorRef = {
    context.actorOf(UserRepository.props(), UserRepository.Name)
  }

  protected def createEventSender(): ActorRef = {
    context.actorOf(EventSender.props(), EventSender.Name)
  }

  override def receiveCommand: Receive = {
    /*
    Not the nicest solution, but it's non-blocking and sufficient to show the idea.
    Other solutions would be;
    - Have this UserAggregate also be the UserRepository, but that would mean mixing responsibilities
    - Use the pipe to self but change behaviour so that intermediate commands are re-queued
     */
    case AddUserCmd(newUser) =>
      val origSender = sender()
      val usersFuture = userRepository ? GetUsers
      pipe(usersFuture.mapTo[Set[User]].map(GetUsersForwardResponse(origSender, _, newUser))) to self

    case GetUsersForwardResponse(origSender, users, newUser) =>
      if (users.exists(_.email == newUser.email)) {
        origSender ! UserExistsResp(newUser)
      } else {
        persist(MsgAddUser(newUser)) { persistedMsg =>
          updateState(persistedMsg)
          origSender ! UserAddedResp(newUser)
        }
      }

    case ConfirmAddUser(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
    case Confirm(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
  }

  override def receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
  }

  def updateState(evt: Evt): Unit = evt match {
    case MsgAddUser(u) =>
      deliver(eventSender.path)(deliveryId => Msg(deliveryId, u))
      deliver(userRepository.path)(deliveryId => AddUser(deliveryId, u))
    case MsgConfirmed(deliveryId) =>
      confirmDelivery(deliveryId)
  }

}
