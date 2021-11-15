
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Super class for the ChatClient and ChatBot and any other that need to connect to the server as a client.
 * Write and read methods need to be overwritten in the subclasses.
 */
abstract class Client {

    // socket and thread used to close the connection and join the thread to the main thread.
    Socket serverSocket;
    // Client Name
    String name = "Name";

    /**
     * Write method used to write to the console and send the input to the server.
     *
     */
    abstract void write();

    /**
     * read method used to read server output.
     */
    abstract void read();

    /**
     * Setter for name field
     * @param Name: name to be set as new name
     */
    public void setName(String Name){
        name = Name;
    }
    /**
     * get the name of the client Joining
     */
    public void setServerName(){
        try {
            PrintWriter send = new PrintWriter(serverSocket.getOutputStream(), true);
            send.println("/setName " + name);
        } catch (SocketException e){
            // if the disconnected from server.
            System.out.println("Lost connection to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Start method used to get the port and address of the connection. Connects the client to the server and starts to
     * read and write data to and from the server. Method is to be used in the main class of the client.
     *
     * @param args : the args passed from the main method.
     */
    public void start(String[] args){
        int port = 14001;
        String address = "localhost";
        // common ports that should not be passed, if given then uses default port

        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-cca")) {
                // get ip from the array of arguments.
                address = args[i++];
            }
            if (args[i].equals("-ccp")) {
                try {
                    if (Integer.parseInt(args[i++]) >= 1024 && Integer.parseInt(args[i++]) <= 49151) {
                        // get port from list of parameters.
                        port = Integer.parseInt(args[i++]);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        // start the server socket on given address.
        try {
            serverSocket = new Socket(address, port);
            System.out.println("Connected to: " + serverSocket.getInetAddress().getHostAddress() + " : " + serverSocket.getPort());
            setServerName();
            read();
            write();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + address + " : " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
