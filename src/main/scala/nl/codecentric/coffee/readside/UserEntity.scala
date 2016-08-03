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

import nl.codecentric.coffee.util.DatabaseService

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
final case class UserEntity(id: Option[Long] = None, name: String) {
  require(!name.isEmpty, "username.empty")
}

trait UserEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Users(tag: Tag) extends Table[UserEntity](tag, "CFE_USERS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")

    def * = (id.?, name) <> (UserEntity.tupled, UserEntity.unapply)
  }

  protected val users = TableQuery[Users]

}
