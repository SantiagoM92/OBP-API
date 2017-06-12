package code.bankconnectors

import akka.pattern.ask
import code.actorsystem.{ObpActorInit, ObpLookupSystem}
import code.util.Helper.MdcLoggable
import net.liftweb.json.JValue

import scala.concurrent.Await

object KafkaHelper extends KafkaHelper

trait KafkaHelper extends ObpActorInit with MdcLoggable {

  override val actorName = "kafkaHelper" //CreateActorNameFromClassName(this.getClass.getName)
  override val actor = ObpLookupSystem.getRemotedataActor(actorName)

  /**
    * Have this function just to keep compatibility for KafkaMappedConnector_vMar2017 and  KafkaMappedConnector.scala
    * In KafkaMappedConnector.scala, we use Map[String, String]. Now we change to case class
    * eg: case class Company(name: String, address: String) -->
    * Company("TESOBE","Berlin")
    * Map(name->"TESOBE", address->"2")
    *
    * @param caseClassObject
    * @return Map[String, String]
    */
  def transferCaseClassToMap(caseClassObject: scala.Product) =
    caseClassObject.getClass.getDeclaredFields.map(_.getName) // all field names
    .zip(caseClassObject.productIterator.to).toMap.asInstanceOf[Map[String, String]] // zipped with all values

  def process(request: scala.Product): JValue = {
    val mapRequest:Map[String, String] = transferCaseClassToMap(request)
    process(mapRequest)
  }

  def process (request: Map[String, String]): JValue ={
    extractFuture(actor ? request)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  def process(request: GetBanks): List[InboundBank] = {
    Await.result((actor ? request).mapTo[List[InboundBank]], TIMEOUT)
  }

}
