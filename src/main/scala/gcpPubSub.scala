import org.apache.flink.api.common.serialization.{
  AbstractDeserializationSchema,
  SerializationSchema
}
import org.apache.flink.streaming.connectors.gcp.pubsub.{
  PubSubSink,
  PubSubSource
}
import org.apache.flink.configuration.{Configuration, ConfigConstants}
import org.apache.flink.configuration.RestOptions.BIND_PORT
import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*
import com.google.auth.oauth2.GoogleCredentials
import upickle.default.*

import scala.jdk.CollectionConverters.*

import java.io.FileInputStream
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate}

import models.*

object models:
  case class CreatedCustomer(fullName: String, birthDate: String)
      derives ReadWriter:
    val firstName = fullName.split(" ").head
    val lastName = fullName.split(" ").last
    val age = ChronoUnit.YEARS
      .between(LocalDate.parse(birthDate), LocalDate.now())
      .toInt

  case class RegisteredCustomer(
      firstName: String,
      lastName: String,
      age: Int,
      isActive: Boolean,
      createdAt: String
  ) derives ReadWriter

  object RegisteredCustomer:
    def apply(
        firstName: String,
        lastName: String,
        age: Int
    ): RegisteredCustomer =
      RegisteredCustomer(firstName, lastName, age, true, Instant.now().toString)

// JSON serializers and deserializers
class CreatedCustomerDeserializer
    extends AbstractDeserializationSchema[CreatedCustomer]:
  override def deserialize(message: Array[Byte]): CreatedCustomer =
    read[CreatedCustomer](new String(message, "UTF-8"))

class RegisteredCustomerSerializer
    extends SerializationSchema[RegisteredCustomer]:
  override def serialize(element: RegisteredCustomer): Array[Byte] =
    write[RegisteredCustomer](element).getBytes("UTF-8")

@main def pubSub(
    projectName: String,
    subscriptionName: String,
    topicName: String,
    //credsFilePath: String,
    localMode: Boolean*
) =    
  //val credentials = GoogleCredentials.fromStream(FileInputStream(credsFilePath))
  val pubsubSource = PubSubSource
    .newBuilder()
    .withDeserializationSchema(CreatedCustomerDeserializer())
    .withProjectName(projectName)
    .withSubscriptionName(subscriptionName)    
    //.withCredentials(credentials)
    .build()

  val pubsubSink = PubSubSink
    .newBuilder()
    .withSerializationSchema(RegisteredCustomerSerializer())
    .withProjectName(projectName)
    .withTopicName(topicName)
    //.withCredentials(credentials)
    .build()

  val local = localMode.headOption.exists(_ == true)
  val env =
    if local then
      val config = Configuration.fromMap(
        Map(
          ConfigConstants.LOCAL_START_WEBSERVER -> "true",
          BIND_PORT.key -> "8081",
          "execution.checkpointing.interval" -> "10 s"
        ).asJava
      )
      StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(config)
    else StreamExecutionEnvironment.getExecutionEnvironment(Configuration())

  env
    .addSource(pubsubSource)
    .map(cc => RegisteredCustomer(cc.firstName, cc.lastName, cc.age))
    .map(rc => if rc.age >= 30 then rc.copy(isActive = false) else rc)
    .addSink(pubsubSink)

  env.execute("customerRegistering")
