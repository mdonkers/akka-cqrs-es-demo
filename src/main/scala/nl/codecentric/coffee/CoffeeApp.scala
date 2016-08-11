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

import akka.actor._
import nl.codecentric.coffee.readside.{ EventReceiver, UserRepository }
import nl.codecentric.coffee.util.DatabaseService
import nl.codecentric.coffee.writeside.UserAggregate

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object CoffeeApp {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("coffee")

    system.actorOf(Props(new Master), "coffee-app-master")

    Await.ready(system.whenTerminated, Duration.Inf)
  }
}

class Master extends Actor with ActorLogging with ActorSettings {
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  private val databaseService = createDatabaseService()
  private val userRepository = createUserRepository(databaseService)
  context.watch(createEventReceiver(userRepository))
  private val userAggregate = context.watch(createUserAggregate())
  context.watch(createHttpService(userAggregate, userRepository))

  log.info("Up and running")

  override def receive = {
    case Terminated(actor) => onTerminated(actor)
  }

  protected def createUserAggregate(): ActorRef = {
    context.actorOf(UserAggregate.props(), UserAggregate.Name)
  }

  protected def createDatabaseService(): DatabaseService = {
    import settings.mariaDB._
    new DatabaseService(uri, user, password)
  }

  protected def createUserRepository(databaseService: DatabaseService): UserRepository = {
    import context.dispatcher
    val repository = new UserRepository(databaseService)
    repository.createTable()
    return repository
  }

  protected def createEventReceiver(userRepository: UserRepository): ActorRef = {
    context.actorOf(EventReceiver.props(userRepository), EventReceiver.Name)
  }

  protected def createHttpService(userAggregateActor: ActorRef, userRepository: UserRepository): ActorRef = {
    import settings.httpService._
    context
      .actorOf(HttpService.props(address, port, selfTimeout, userAggregateActor, userRepository), HttpService.Name)
  }

  protected def onTerminated(actor: ActorRef): Unit = {
    log.error("Terminating the system because {} terminated!", actor)
    context.system.terminate()
    databaseService.dbSession.close()
    databaseService.db.close()
  }
}
