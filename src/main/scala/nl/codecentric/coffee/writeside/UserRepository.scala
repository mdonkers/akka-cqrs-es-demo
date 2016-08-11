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
import nl.codecentric.coffee.domain._

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object UserRepository {

  final val Name = "user-repository"

  def props(): Props = Props(new UserRepository())

}

class UserRepository extends PersistentActor with ActorLogging {

  override val persistenceId: String = "user-repository"
  private var users = Set.empty[User]

  override def receiveCommand: Receive = {
    case AddUser(name) if users.exists(_.name == name) =>
      sender() ! UserExists(name)
    case AddUser(user) =>
      log.info(s"Adding new user with name; ${user.name}")
      persist(user) { persistedUser =>
        receiveRecover(persistedUser)
        sender() ! UserAdded(persistedUser)
      }
  }

  override def receiveRecover: Receive = {
    case user: User => users += user
  }
}
