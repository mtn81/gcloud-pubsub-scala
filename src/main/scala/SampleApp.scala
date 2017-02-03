import java.util
import java.util.concurrent.Executors

import com.google.auth.Credentials
import com.google.auth.oauth2.GoogleCredentials
import com.google.pubsub.v1.{ListTopicsRequest, PublisherGrpc}
import io.grpc.ClientInterceptors
import io.grpc.auth.ClientAuthInterceptor
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}

import scala.collection.JavaConversions._

object SampleApp extends App {

  val channelImpl = NettyChannelBuilder
    .forAddress("pubsub.googleapis.com", 443)
    .negotiationType(NegotiationType.TLS)
    .build();

  val creds = GoogleCredentials.getApplicationDefault()
              .createScoped(util.Arrays.asList("https://www.googleapis.com/auth/pubsub"));

  val interceptor = new ClientAuthInterceptor(creds, Executors.newSingleThreadExecutor());

  val channel = ClientInterceptors.intercept(channelImpl, interceptor);
  // Create a stub using the channel that has the bound credential
  val publisherStub = PublisherGrpc.newBlockingStub(channel);
  val request = ListTopicsRequest.newBuilder()
    .setPageSize(10)
    .setProject("projects/alpine-effort-157513")
    .build();
  val resp = publisherStub.listTopics(request);
  System.out.println("Found " + resp.getTopicsCount() + " topics.");
  for (topic <- resp.getTopicsList()) {
    println(topic.getName());
  }

}
