Project 2: Microservice - Kafka Source to Kafka Sink

The data that is read to kafka will be transformed into kafka again in this stage. A commitable source is created which will subscribe to the topic defined in Project 1 and then process it asyncronously using mapAsync on each message. The transformation that is done here is to replace empty values in relationship column and make all the values in this column to lower case. After transforming data this data is put to another kafka topic using a method that is called in Flow part of the stream and the sink is ignored in this case.

Run this project using:

sbt run