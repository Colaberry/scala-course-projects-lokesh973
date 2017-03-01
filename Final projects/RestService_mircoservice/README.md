Project 4: REST service

This project implements a REST service that provide end point to search for the data that is stored in the file. This project uses Akka HTTP DSL to bind and listen to incoming requests and query elastic search. 

Users can query data on the elastic search using two end points: One to search based on id(\{indexname}\search\{id}) which results in the record, to fuzzy match based on the term(\{indexname}\fuzzy\{search_term}), this result in the success or failure. To implement the Http request is created to query elastic search based on the input end point and query the elastic search whose output is sent as response to browser.

Run this project using:

sbt run