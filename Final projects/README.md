Final Project:

This project aims in bulding a pipeline where data was read from a file, put into kafka, transformed and put in Elastic search for conducting further analysis. To build the above pipeline I am bulding micro services whose main purpose is to do one single business process. I am using Akka framework to build these microservices as it provides a resilient, scalable and pluggable architecture.

Dataset:

The dataset that being used is of genome data which is a csv file with records of different samples.

To build this project using Akka micro services, four individual micro services are defined.

1. Micro service to read from File and put data into Kakfa:
	
This micro services will create an akka stream which will read data from a file and put the data into kafka framework, which can be read using consumers. 

2. Micro service to read data from kafka to another Kafka topic:

As the data needs to be transformed another microservice is built which builds a stream to read data from one kafka topic and put the data to another kafka topic by transforming data. Here the akka stream will use kafka consumer to subscribe to one topic and put the data to another topic in kafka cluster after performing transformations.

3.Micro service to read from Kafka to Elasticsearch:

This micro service will subscribe to a topic in Kafka, the data is retrieved and a new http request is formed with the data and sent to Elastic search with REST Api

4. REST Micro service:

The data can be searched using this micro service. This micro service can handler two url /genome/search/{id} or /genome/fuzzy/{term}. Former will search with id and fetch the record matching it, the second will do fuzzy matching and says if it is found or not.

