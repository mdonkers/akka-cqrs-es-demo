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

import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import nl.codecentric.coffee.domain.User
import nl.codecentric.coffee.writeside.UserRepository.{ AddUser, ConfirmAddUser, GetUsers }

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object UserRepository {

  final val Name = "user-repository"

  def props(): Props = Props(new UserRepository())

  case object GetUsers
  final case class AddUser(deliveryId: Long, user: User)
  final case class ConfirmAddUser(deliveryId: Long)
}

class UserRepository extends PersistentActor with ActorLogging {

  override val persistenceId: String = "user-repository"
  private var users = Set.empty[User]

  override def receiveCommand: Receive = {
    case GetUsers =>
      sender() ! users
    case AddUser(id, user) =>
      log.info(s"Adding $id new user with email; ${user.email}")
      persist(user) { persistedUser =>
        receiveRecover(persistedUser)
        sender() ! ConfirmAddUser(id)
      }
  }

  override def receiveRecover: Receive = {
    case user: User => users += user
  }
}
