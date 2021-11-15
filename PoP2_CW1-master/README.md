# PoP2_CW1
Simple Java Chat.

## Getting Started
After all code is compiled first run the ChatServer.java. <br>
Once the server is running you are free to run any number of ChatClient.java instances but please only run only 1 ChatBot to avoid text confusion.

The server will start by default on port 14001; clients will also connect by default to this port and localhost as its default address. However, this can be changed by giving it optional parameters.

### Arguments
The server and client can be run by giving it arguments. The server will accept -ccp "port" is the only argument the server will accept. The port must also be in the range of 1024 and 49151. If anything, other than an integer is given then a number format exception will be thrown and the server will not start.

The client accepts 2 different arguments when running the program. First is -csp which acts exactly the same as for the server. Second is -cca which works the same as -csp but instead of the port it allows the user to change the ip address. If an incorrect address/port is given a unknown host exception is thrown.
 
## Server Input
The only recognisable server input is "EXIT" (all caps). This command when inputted on the server it will close all socket connections to the server and close the server. The clients connected to the server will also quit after informing the user that the sever connection has ended.

This is the preferred method to close a server however if server terminates by closing the window the clients will handle the leaving, as the clients will receive a socket exception error. However, if the server terminates correctly the clients will also close after informing the user of this. This is done by checking the output from the server socket. When the client reads output from the server if the socket is not connected the data received is null allowing the user to know that the server connection should end, from here the client will terminate using System.Exit().

## Giving input to the server
To give input to the server simple type to the console and press enter to send. The client can also type /Exit to server. This is recognised in the server and it will remove the client from its current clients. The client who typed exit will terminate after finishing all its threads. The user can also rename themselves in the server by using the /setName command followed by a string as their name.

## Classes
The chat system consists of 3 classes and 1 abstract class and they all except the abstract class are used and run independently but rely on the others on running.

The ChatServer class is used to create and run a server instance and should be run before anything else. The class uses anonymous inner classes and inner classes for threads. The anonymous class in listenToClient method, is used to read input from a client connection and the Connection Handling inner class is used to accept connections from multiple clients. Once a connection is started the socket and the output stream is saved to a HashMap of clients, which used to send data to all connected clients, and an anonymous thread will start. The main thread in the class is used to read and validate input from the server terminal. If EXIT is typed the server will close all its sockets and finish all the threads then close the server.

The Client abstract class both ChatClient and ChatBot extend from. This class is used to define the start method which allows the client and bot to start with arguments and starts their connection to the server. It also defines abstract methods read and write which are the main methods used to send and receive data from the server. Allows all Client subclasses to use the run using the console flags -cca and -ccp.

The ChatClient Class extends from the Client abstract class works using 2 threads. The main thread is used to read client input and send said input to the server it is connected on. The other thread is started withing the read method using an anonymous inner class and constantly reads client server output. The read method also checks if the server connection is still ok.

The ChatBot class all runs under one thread and will connect and wait for user input from other clients. Client messages and bot messages are differentiated using "Bot: " as an indicator that the message was sent by a bot. The bot will only react to other client messages and will only start working once the user greets the bot using "Hello". The bot will then randomly decide on an answer based on the minimal input that the user sends to the server and the bot reads. The messages are taken from https://rubberduckdebugging.com/. The possible input for the server is (Hello, yes, ye, yeah, no, i don't know, bye, why, when, where, what, how, who) any other input will give a random response from a default response list. If the user inputs bye, the bot will leave the server after responding. The Chat bot needs to have its own ran like any other client to connect to the server, user clients will therefore be able to interact with the ChatBot if and only if connected to the server.

## DoD implementation
The Dungeon of Doom Coursework was implemented with slight changes to support multiplayer gameplay. The DoD client connects to the server as a normal client and thus extends from the client superclass. In this implementation of DoD the bot is replaced by another player and both players take turns to input to the DoD client. The DoD client itself will not diferentiate between users to check which player is which, thus meaning players must take turns to input or they will make a move for the other player; it also means that only one instance of the DoD client per server is supported, more than that can have undesired and unexpected results. 
