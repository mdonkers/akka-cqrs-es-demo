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

package nl.codecentric.coffee.swagger

import com.github.swagger.akka.model.Info
import nl.codecentric.coffee.UserService

import scala.reflect.runtime.{ universe => ru }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka._

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
class SwaggerDocService(address: String, port: Int, system: ActorSystem) extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val apiTypes = Seq(ru.typeOf[UserService])
  override val host = address + ":" + port
  override val info = Info(version = "1.0")
}
