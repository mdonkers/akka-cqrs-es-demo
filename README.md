# coffee-at-work #

Repo for our coffee machine backend. Used to store coffee preferences for each user, so that the machine will automatically tap the right coffee.


Some code is copied from / based on the [Reactive-Flows example application]("https://github.com/hseeberger/reactive-flows")

## Useful commands ##

- sbt -> reStart / reStop             : Start and stop the system easily
- sbt -> ~reStart                     : Start and reload after source changes
- sbt -> reload / update              : Reload build.sbt and update dependencies
- sbt -> dependencyTree               : Print the dependency tree

- curl -i localhost:8080/users        : List all users
- curl -i localhost:8080/users -H "Content-Type: application/json" -d '{"name": "Miel"}'    : Add new user
- curl -i localhost:8080 -X DELETE    : Nicely shutdown the Akka application

[Swagger UI](http://localhost:8080/swagger/)

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
