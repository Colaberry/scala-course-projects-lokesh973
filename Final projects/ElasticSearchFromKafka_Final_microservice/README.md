Project 3: Microservice - Kafka to Elastic search

This project sends the data to Elastic search from the kafka source by listening to topics created in previous stages. To send data to Elastic seach the REST api is used. The akka stream is built with the kafka consumer as source that listens on the data coming on the topic created in previoud stage and bulds HttpRequest to send the data to Elastic search using REST end points.

Run this project using:

sbt run