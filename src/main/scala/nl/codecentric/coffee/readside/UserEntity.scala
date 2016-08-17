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

import java.sql.Timestamp

import nl.codecentric.coffee.domain.User
import nl.codecentric.coffee.util.DatabaseService
import slick.profile.SqlProfile.ColumnOption.SqlType

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
final case class UserEntity(
  id: Option[Long] = None,
  createdAt: Option[Timestamp] = None,
  updatedAt: Option[Timestamp] = None,
  userInfo: User
)

trait UserEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Users(tag: Tag) extends Table[UserEntity](tag, "CFE_USERS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt =
      column[Timestamp](
        "CREATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on insert CURRENT_TIMESTAMP")
      )
    def updatedAt =
      column[Timestamp](
        "UPDATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
      )
    def email = column[String]("EMAIL")
    def firstName = column[String]("LAST_NAME")
    def lastName = column[String]("FIRST_NAME")

    def * =
      (id.?, createdAt.?, updatedAt.?, (email, firstName, lastName)).shaped <> ({
        case (id, createdAt, updatedAt, userInfo) =>
          UserEntity(id, createdAt, updatedAt, User.tupled.apply(userInfo))
      }, { ue: UserEntity =>
        def f1(u: User) = User.unapply(u).get
        Some((ue.id, ue.createdAt, ue.updatedAt, f1(ue.userInfo)))
      })

    def idx_user = index("idx_user", email, unique = true)
  }

  protected val users = TableQuery[Users]

}
