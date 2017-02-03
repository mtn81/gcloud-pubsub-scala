import java.util
import java.util.concurrent.Executors

import com.google.auth.oauth2.GoogleCredentials
import com.google.protobuf.ByteString
import com.google.pubsub.v1._
import io.grpc.ClientInterceptors
import io.grpc.auth.ClientAuthInterceptor
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
import io.grpc.stub.StreamObserver

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Channel {
  def create = {
    val channelImpl = NettyChannelBuilder
      .forAddress("pubsub.googleapis.com", 443)
      .negotiationType(NegotiationType.TLS)
      .build();

    val creds = GoogleCredentials.getApplicationDefault()
      .createScoped(util.Arrays.asList("https://www.googleapis.com/auth/pubsub"));

    val interceptor = new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor());

    ClientInterceptors.intercept(channelImpl, interceptor);
  }
}

object SampleApp extends App {

  // Create a stub using the channel that has the bound credential
  val publisherStub = PublisherGrpc.newBlockingStub(Channel.create)
  val request = ListTopicsRequest.newBuilder()
    .setPageSize(10)
    .setProject("projects/alpine-effort-157513")
    .build()
  val resp = publisherStub.listTopics(request)

  println("Found " + resp.getTopicsCount() + " topics.")
  for (topic <- resp.getTopicsList()) {
    println(topic.getName());
  }

}

object SubscribeApp extends App {

  val subscriber = SubscriberGrpc.newStub(Channel.create)


  def pull: Future[Unit] = Future {
    subscriber.pull(
      PullRequest.newBuilder()
        .setSubscription("projects/alpine-effort-157513/subscriptions/my-sub2")
        .setMaxMessages(10)
        .build()
      ,
      new StreamObserver[PullResponse] {
        override def onError(t: Throwable): Unit = println(t)
        override def onCompleted(): Unit = {
          println("completed!")
          pull
        }
        override def onNext(res: PullResponse): Unit = {
          println("next!")
          println(s"received: ${res.getReceivedMessagesCount}")
        }
      }
    )
  }

  pull

  while (true) {
    Thread.sleep(10000)
  }

}

object PublishApp extends App {

  val msg = PubsubMessage.newBuilder()
    .setData(ByteString.copyFrom("app pub msg", "UTF-8"))
    .build()
  val req = PublishRequest.newBuilder()
    .setTopic("projects/alpine-effort-157513/topics/my-topic")
    .addMessages(msg)
    .build()

  PublisherGrpc.newBlockingStub(Channel.create).publish(req)

}
