import sbtprotobuf.{ProtobufPlugin=>PB}

PB.protobufSettings

name := "gloud-pubsub-sample"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.google.api.grpc" % "grpc-google-cloud-pubsub-v1" % "0.1.5",
  "io.netty" % "netty-tcnative-boringssl-static" % "1.1.33.Fork22",
  "com.google.auth" % "google-auth-library-oauth2-http" % "0.6.0"
)
