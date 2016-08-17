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

import scala.concurrent.{ ExecutionContext, Future }

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
class UserRepository(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext)
    extends UserEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getUsers(): Future[Seq[UserEntity]] = db.run(users.result)

  def getUserById(id: Long): Future[Option[UserEntity]] = db.run(users.filter(_.id === id).result.headOption)

  def getUserByEmail(email: String): Future[Option[UserEntity]] =
    db.run(users.filter(_.email === email).result.headOption)

  def createUser(user: UserEntity): Future[Long] = db.run((users returning users.map(_.id)) += user)

  def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)

  def createTable(): Future[Unit] = {
    db.run(DBIO.seq(users.schema.create))
  }
}
