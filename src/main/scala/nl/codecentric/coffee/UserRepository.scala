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

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

object UserRepository {

  case class User(name: String)

  case object GetUsers

  case class AddUser(name: String)

  case class UserAdded(user: User)

  case class UserExists(name: String)

}

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
class UserRepository extends PersistentActor with ActorLogging {

  import UserRepository._

  override val persistenceId: String = "user-repository"
  private var users = Set.empty[User]

  override def receiveCommand: Receive = {
    case GetUsers                                      => sender() ! users
    case AddUser(name) if users.exists(_.name == name) => sender() ! UserExists(name)
    case AddUser(name) =>
      log.info(s"Adding new user with name; $name")
      val user = User(name)
      persist(user) { persistedUser =>
        receiveRecover(persistedUser)
        sender() ! UserAdded(persistedUser)
      }
  }

  override def receiveRecover: Receive = {
    case user: User => users += user
  }

}
