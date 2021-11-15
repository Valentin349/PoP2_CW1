import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Threaded chat server, allows clients to connect to it on default 14001 unless specified another
 *
 */
public class ChatServer {

    // Hash map and set to keep record of client sockets, output streams and threads used for the connection
    private final HashMap<Socket, List<Object>> clients;
    private final HashSet<Thread> activeThreads;

    private ServerSocket serverSocket;
    // linked queue that deals with synchronization.
    private final LinkedBlockingQueue<String> messages;

    private boolean exit = false;

    /**
     * Constructor, starts up the server socket and instantiates main class variables
     *
     * @param port : The port used to bind the server socket to
     */
    public ChatServer(int port){
        clients = new HashMap<>();
        messages = new LinkedBlockingQueue<>();
        activeThreads = new HashSet<>();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Starting Server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs on main thread of the server instance.
     * Allows to write to console and validates the input to allow only EXIT as valid command to kill the server.
     * Calls connectionHandlingThread to start a listening thread.
     *
     */
    public void start() {
        try{
            // create thread object and start it to allow clients to connect to server
            // private class is used to allow to easily close the thread when closing the server
            ConnectionHandling connectionHandlingThread = new ConnectionHandling();
            System.out.println("Listening for connections...");
            connectionHandlingThread.start();

            // allows input to console on main thread
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
            String command = serverInput.readLine();
            while (!command.equals("EXIT")){
                // constantly checks if EXIT is give to close server
                command = serverInput.readLine();
            }

            // exit var used, stops infinite loop in read threads.
            exit = true;
            // close all client sockets
            for (Socket socket: clients.keySet()){
                socket.close();
            }
            // halts main thread until all read threads are finished so all client sockets are closed.
            for (Thread thread: activeThreads){
                thread.join();
            }
            // close server socket and wait halt main thread so that listening thread finishes.
            serverSocket.close();
            connectionHandlingThread.join();
            System.out.println("Connection handling thread finished.");

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inner class; creates a single thread to allow the server to listen for clients.
     * Has a infinite loop which constantly accepts new client connections.
     * On accepting client connection, it starts another thread for that connection to accept data from the connection.
     *
     */
    private class ConnectionHandling extends Thread{
        public void run(){
            try {
                // loop runs until the thread is interrupted.
                while (!interrupted()) {
                    // waits until a connection is made
                    Socket clientSocket = serverSocket.accept();
                    try {
                        System.out.println("Connection accepted from: " + clientSocket.getInetAddress().getHostAddress()
                        + " : " + clientSocket.getPort());
                        listenToClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                // main wait to get out of loop. The SocketException ends the loop when the server socket it closed.
                System.out.println("Closing Server Socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deals with the data received for every client. Every client will be connected to the server via its own thread.
     * Thread is started using anonymous inner class which reads the data received and sends to all client output streams.
     *
     * @param socket : the client socket for each individual socket connection
     * @throws IOException : the exception thrown by getting the input and output stream.
     */
    private void listenToClient(Socket socket) throws IOException {
        // input and output stream of the client socket connected
        InputStreamReader inStream = new InputStreamReader(socket.getInputStream());
        PrintWriter outStream = new PrintWriter(socket.getOutputStream(), true);

        // adds the socket and PrintWriter object to a hash map allowing the object to be found via the socket.
        clients.put(socket, new ArrayList<>());
        clients.get(socket).add(outStream);

        BufferedReader input = new BufferedReader(inStream);

        //anonymous thread to read client input and send back to all clients
        Thread readThread = new Thread(() -> {
            try {
                // loop is stopped only when thread is interrupted caused by the client requesting to exit, or when exit
                // var turns true since the server is shutting down.
                while (!exit && !Thread.interrupted()) {
                    read(socket, input);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // start the thread and add it to the set of active threads.
        readThread.start();
        activeThreads.add(readThread);
    }

    /**
     * read method used by listenToClient method. Deals with the specifics of reading inputs from clients and sending
     * the data back. Also deals with the exceptions caused by closing sockets and validates the input from clients to
     * allow them to leave the server. Server will then close the connection to the client and the thread used for it.
     *
     * @param socket : allows the socket for the current thread to be closed.
     * @param input : Buffered reader object used to write message to a linkedBlockingQueue which is used to send the
     *              messages back to the clients in the correct order.
     * @throws InterruptedException : thrown by put method used to put the message in a linkedBlockingQueue.
     */
    private void read(Socket socket, BufferedReader input) throws InterruptedException {
        try {
            // put the message read from the client to the linkedBlockingQueue
            // use of linked blocking queue allows messages to be uniform between different threads so that all clients
            // receive all messages.
            messages.put(input.readLine());
            ArrayList<PrintWriter> outputStreams = new ArrayList<>();
            for (List<Object> list: clients.values()){
                outputStreams.add((PrintWriter) list.get(0));
            }
            for (PrintWriter outputStream: outputStreams){
                if (messages.peek() != null && messages.peek().equals("/Exit")){
                    try {
                        outputStream.println(socket.getInetAddress().getHostAddress() + " : "  +
                                socket.getPort() + " has left.");

                        // close the clients socket
                        socket.close();

                        // interrupt the thread so that while loop stops and allow the thread end
                        // interrupt exception doesnt get thrown.#
                        Thread.currentThread().interrupt();

                        // remove the client socket and its output stream and remove the thread the set of active threads.
                        activeThreads.remove(Thread.currentThread());
                        clients.remove(socket);
                        System.out.println("Closing connection to client: " + socket.getInetAddress().getHostAddress()
                                + " : " + socket.getPort());
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (messages.peek() != null){
                    String[] wordList = messages.peek().split(" ");
                    if (wordList[0].equals("/setName")){
                        if (clients.get(socket).size() == 1){
                            clients.get(socket).add(wordList[1]);
                        } else {
                            clients.get(socket).set(1, wordList[1]);
                        }
                    } else {
                        // send item at the from of the queue with the name of the client
                        outputStream.println(String.format("[%s] %s", clients.get(socket).get(1), messages.peek()));
                    }

                }
            }
            // only remove the message from the queue once it has sent it to all clients
            messages.poll();
        } catch (SocketException e) {
            // socket exception happens when all sockets get closed by the server when shutting down or when the
            // client quits without /Exit so thread needs so interrupt flag needs to be raised to stop while loop.
            System.out.println("Closing connection to client: " + socket.getInetAddress().getHostAddress() + " : " +
                    socket.getPort());
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Main method when running
     * @param args : checks for -csp allowing to change the port
     */
    public static void main(String[] args){
        int port = 14001;
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-csp")){
                try {
                    if (Integer.parseInt(args[i++]) >= 1024 && Integer.parseInt(args[i++]) <= 49151) {
                        // port gets changed if -csp flag is given but the port doesn't get validated.
                        port = Integer.parseInt(args[i++]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Incorrect Port supplied. Run with a valid Port or no port at all.");
                }
            }
        }

        // create chatServer object and start running it by calling the start method.
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }
}