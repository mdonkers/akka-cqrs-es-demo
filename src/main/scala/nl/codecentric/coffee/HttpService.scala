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

import javax.ws.rs.Path

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives
import akka.stream.{ ActorMaterializer, Materializer }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport
import nl.codecentric.coffee.swagger.SwaggerDocService
import nl.codecentric.coffee.util.CorsSupport

import scala.concurrent.ExecutionContext
import io.swagger.annotations._
import nl.codecentric.coffee.domain._
import nl.codecentric.coffee.readside.UserRepository

import scala.util.{ Failure, Success }

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
object HttpService extends CorsSupport {

  private[coffee] case object Stop

  // $COVERAGE-OFF$
  final val Name = "http-service"
  // $COVERAGE-ON$

  def props(
    address: String,
    port: Int,
    internalTimeout: Timeout,
    userAggregate: ActorRef,
    userRepository: UserRepository
  ): Props =
    Props(new HttpService(address, port, internalTimeout, userAggregate, userRepository))

  private[coffee] def route(
    httpService: ActorRef,
    userService: UserService,
    swaggerDocService: SwaggerDocService
  )(implicit ec: ExecutionContext, mat: Materializer) = {
    import Directives._
    import io.circe.generic.auto._

    // format: OFF
    def assets = pathPrefix("swagger") {
      getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }

    def stop = pathSingleSlash {
      delete {
        complete {
          httpService ! Stop
          "Stopping ..."
        }
      }
    }

    assets ~ stop ~ corsHandler(userService.route) ~ corsHandler(swaggerDocService.routes)
  }
}

class HttpService(address: String, port: Int, internalTimeout: Timeout, userAggregate: ActorRef, userRepository: UserRepository)
    extends Actor with ActorLogging {
  import HttpService._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  Http(context.system)
    .bindAndHandle(
      route(self, new UserService(userAggregate, userRepository, internalTimeout), new SwaggerDocService(address, port, context.system)),
      address,
      port)
    .pipeTo(self)

  override def receive = binding

  private def binding: Receive = {
    case serverBinding @ Http.ServerBinding(address) =>
      log.info("Listening on {}", address)
      context.become(bound(serverBinding))

    case Status.Failure(cause) =>
      log.error(cause, s"Can't bind to $address:$port")
      context.stop(self)
  }

  private def bound(serverBinding: Http.ServerBinding): Receive = {
    case Stop =>
      serverBinding.unbind()
      context.stop(self)
  }
}

@Path("/users")  // @Path annotation required for Swagger
@Api(value = "/users", produces = "application/json")
class UserService(userAggregate: ActorRef, userRepository: UserRepository, internalTimeout: Timeout)(implicit executionContext: ExecutionContext) extends Directives {
  import CirceSupport._
  import io.circe.generic.auto._

  implicit val timeout = internalTimeout

  val route = pathPrefix("users") { usersGetAll ~ userPost }

  @ApiOperation(value = "Get list of all users", nickname = "getAllUsers", httpMethod = "GET",
    response = classOf[User], responseContainer = "Set")
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal error")
  ))
  def usersGetAll = get {
    onComplete(userRepository.getUsers()) {
      case Success(users) => complete(users.map(u => User(u.name)))
      case Failure(_) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "Internal error"))
    }
  }

  @ApiOperation(value = "Create new user", nickname = "userPost", httpMethod = "POST", produces = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "user", dataType = "nl.codecentric.coffee.domain.User", paramType = "body", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "User created"),
    new ApiResponse(code = 409, message = "User already exists")
  ))
  def userPost = post {
    entity(as[User]) { user =>
      onSuccess(userAggregate ? AddUser(user)) {
        case UserAdded(_)  => complete(StatusCodes.Created)
        case UserExists(_) => complete(StatusCodes.Conflict)
      }
    }
  }
}
