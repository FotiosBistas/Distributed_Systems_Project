# Distributed_Systems_Project

This is a 3rd year semester project for Athens University of Economics And Business. 
Includes introductory pub-sub,sockets,dht,brokers, chunking etc. 



Phase 1: Create the pub/sub system 




Phase 2: Create an android app that will use the services created above.





The idea is the following: 

## Introduction
![image](https://user-images.githubusercontent.com/83087431/163029057-25537434-6e3c-4f19-af4f-2e512d87facd.png)

In order to distribute the content, we need to know: 
- **who is interested** (_subscribers_)
- **how how can they express interest** (_topic subscription_) and
- **how can they receive it**.

## Event Delivery System

This repository accommodates the implementation of the multimedia streaming framework (_Event Delivery System_) which is responsible to support the forwarding and receiving (_streaming_) of multimedia conent. 

The Event Delivery System is a programming framework that allows sending and receiving data that fulfill specific criteria. The advantage of the Event Delivery System is the immediate forwarding of data in real time via two fundamental functions; `push` and `pull`. These two functions are independent of each other. 

During each `push` call, the intermediate system node (_broker_) should;
- **be able to handle data incoming from different _publishers_ concurrently** (in our case users) and
- **be able to deliver the results to the _subscribers_** (also called _consumers_ as they "consume" the data)

Concurrency is inevitable because the system is required to offer simultaneous data delivery from _publishers_ to _brokers_ and from _brokers_ to _subscribers_. All subscribed users must simultaneously receive the same content.

### - `push`

The sole role of the `push` function is to forward to a _broker_ a value which is stored in a data structure (e.g. queue), so that the value can be delivered upon requested. This intermediate data structure plays the role of the topic's _chat history_. As a result, once a new user subscribes to the topic, they will be able to see the previous messages. In our case, `push` takes as input the information required for the immaculate delivery of the content (ex. _username, topic name/id, video data, etc._). 

A significant software requirement that needs to be addressed for better comprehension of the model's functionality is **multimedia chunking**; a photo or a video streamed to and from the framework is *never sent wholly*. On the contrary, multimedia content is cut down to smaller, equal in size, fragments (_chunks_) in order to achieve higher communication efficiency[^1].

### - `pull`

The role of `pull` is to deliver all the data of an intermediate node that concern the user (_subscriber_) that calls the function. Values from each topic are collected and delivered to the _subscriber_ that issued the request. 

## The above part part of the read me was conceived by [Georgios E. Syros](https://github.com/gsiros "Georgios E. Syros")

The broker class is the server component of the system. It handles publisher and consumer requests using the publisher handler class and the consumer handler class. It achieves that using 2 ports for each type of service. 


The user node class is essentially a temporary class used for the phase 1 meant to be replaced by the android implementation of the project. All the requests that the user node sends are not persistent meaning each request needs a new connection. 



Using the messages enum the user prompts the broker the server the specific request for a specific topic. There is a main broker which distributes requests based on the hashed SHA1 value of the topic. Example: C++ topic is hashed to 150 the default broker will redirect the request for that topic to the broker with id 200. 




User sends files, images and text messages on the network. These messages are directed towards topics that the user must be subscribed to see and publish them. All files are chunked into 512KB chunks and then sent on to the network. We were not instructed that to be able to handle chunks seperately and we used specifically tcp object streams for the sockets so its kinda pointless the way its implemented. 









