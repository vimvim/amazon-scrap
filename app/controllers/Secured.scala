package controllers

import play.api.mvc._
import scala.util.Random
import play.api.mvc.Results._
import scala.Some
import scala.Some
import scala.Some
import scala.concurrent.Future
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import scala.Some

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.{Json, JsValue}
import play.api.libs.concurrent.Execution.Implicits._

trait Secured {

  def userid(request: RequestHeader) = {
    //verify or create session, this should be a real login
    request.session.get(Security.username)
  }

  /**
   * When user not have a session, this function create a
   * random userId and reload index page
   */
  def unauthF(request: RequestHeader) = {
    val newId: String = new Random().nextInt().toString()
    Redirect(routes.Application.index).withSession(Security.username -> newId)
  }

  /**
   * Basi authentication system
   * try to retieve the username, call f() if it is present,
   * or unauthF() otherwise
   */

  def withAuth(f: => Int => Request[_ >: AnyContent] => Result): EssentialAction = {
    Security.Authenticated(userid, unauthF) {
      username =>
        Action(request => f(username.toInt)(request))
    }
  }


  /**
   * This function provide a basic authentication for
   * WebSocket, lekely withAuth function try to retrieve the
   * the username form the session, and call f() funcion if find it,
   * or create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
   * if username is none
   */
  def withAuthWS(f: => String => Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])]): WebSocket[JsValue, JsValue] = {

    def errorFuture = {
      // Just consume and ignore the input
      val in = Iteratee.ignore[JsValue]

      // Send a single 'Hello!' message and close
      val out = Enumerator(Json.toJson("not authorized")).andThen(Enumerator.eof)

      Future {
        (in, out)
      }
    }

    WebSocket.async[JsValue] {
      request =>
        userid(request) match {
          case None => errorFuture
          case Some(userId) => f(userId)
        }
    }
  }
}

