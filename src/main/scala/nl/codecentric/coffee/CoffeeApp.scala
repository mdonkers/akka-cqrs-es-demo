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

import akka.actor.{ ActorRef, Props, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, DurationInt }

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object CoffeeApp {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("coffee")
    implicit val mat = ActorMaterializer()

    val userRepositoryActor = system.actorOf(Props(new UserRepository), "user-repository")

    Http(system).bindAndHandle(route(userRepositoryActor), "127.0.0.1", 8080)
    Await.ready(system.whenTerminated, Duration.Inf)
  }

  private def route(userRepository: ActorRef) = {
    import de.heikoseeberger.akkahttpcirce.CirceSupport._
    import akka.http.scaladsl.server.Directives._
    import io.circe.generic.auto._

    implicit val timeout = Timeout(1.second)

    // format: OFF
    pathPrefix("users") {
      get {
        complete {
          (userRepository ? UserRepository.GetUsers).mapTo[Set[UserRepository.User]]
        }
      } ~
      post {
        entity(as[UserRepository.User]) { user =>
          onSuccess(userRepository ? UserRepository.AddUser(user.name)) {
            case UserRepository.UserAdded(_) => complete(StatusCodes.Created)
            case UserRepository.UserExists(_) => complete(StatusCodes.Conflict)
          }
        }
      }
    }
    // format: ON
  }
}
