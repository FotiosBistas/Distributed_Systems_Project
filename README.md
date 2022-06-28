# Distributed_Systems_Project

This is a 3rd year semester project for Athens University of Economics And Business. 
Includes introductory pub-sub,sockets,dht,brokers, chunking etc. 



Phase 1: Create the pub/sub system 




Phase 2: Create an android app that will use the services created above.





The idea is the following: 
![image](https://user-images.githubusercontent.com/83087431/163029057-25537434-6e3c-4f19-af4f-2e512d87facd.png)




The broker class is the server component of the system. It handles publisher and consumer requests using the publisher handler class and the consumer handler class. It achieves that using 2 ports for each type of service. 


The user node class is essentially a temporary class used for the phase 1 meant to be replaced by the android implementation of the project. 


User sends files, images and text messages on the network. These messages are directed towards topics that the user must be subscribed to see and publish them. All files are chunked into 512KB chunks and then sent on to the network. We were not instructed that to be able to handle chunks seperately and we used specifically tcp object streams for the sockets so its kinda pointeless the way its implemented. 









