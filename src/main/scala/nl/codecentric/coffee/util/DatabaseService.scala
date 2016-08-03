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

package nl.codecentric.coffee.util

import com.zaxxer.hikari.HikariDataSource

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
class DatabaseService(jdbcUrl: String, dbUser: String, dbPassword: String) {
  private val ds = new HikariDataSource()
  ds.setMaximumPoolSize(20)
  ds.setDriverClassName("org.mariadb.jdbc.Driver")
  ds.setJdbcUrl(jdbcUrl)
  ds.addDataSourceProperty("user", dbUser)
  ds.addDataSourceProperty("password", dbPassword)
  //  ds.setAutoCommit(false)

  val driver = slick.driver.MySQLDriver

  import driver.api._
  val db = Database.forDataSource(ds)
  implicit val dbSession = db.createSession()
}
