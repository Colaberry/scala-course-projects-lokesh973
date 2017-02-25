Project 1: Microservice - File Source to Kafka Sink

This project implements the first stage of the data pipeline being built. Akka streams are used to build each of the micro services. Akka stream mainly need three components to be configured Source, Sink and Flow. In this project the source if file. An iterator to the lines from file is given as source and the sink is configured to kafka producer. 

The file that need to be read, Kafka configuration and topic names can be configured using the config file application.conf

Run this project using:

sbt run