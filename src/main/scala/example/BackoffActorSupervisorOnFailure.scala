package example

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.{Backoff, BackoffSupervisor}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._

class BackoffActorSupervisorOnFailure(childProps: Props, childName: String) extends Actor with ActorLogging {

  import BackoffActorSupervisor._

  val backoffSupervisorActorProps: Props = BackoffSupervisor.props(
    Backoff
      .onFailure(
        childProps = childProps,
        childName = childName,
        minBackoff = minBackoff,
        maxBackoff = maxBackoff,
        randomFactor = randomFactorBackoff // adds "noise" to vary the intervals slightly
      )
      .withAutoReset(resetBackoff)
  )

  context.actorOf(backoffSupervisorActorProps, s"backoff-supervisor-$childName")

  def receive: Receive = {
    case msg =>
      log.warning(s"This actor does not support any message... what is this? [$msg].")
  }
}

object BackoffActorSupervisorOnFailure {
  def props(childProps: Props, childName: String): Props =
    Props(new BackoffActorSupervisorOnFailure(childProps, childName))


  val config: Config                    = ConfigFactory.load()
  lazy val minBackoff: FiniteDuration   = 1.second
  lazy val maxBackoff: FiniteDuration   = 10.seconds
  lazy val resetBackoff: FiniteDuration = 50.seconds
  lazy val randomFactorBackoff: Double  = 1
}

