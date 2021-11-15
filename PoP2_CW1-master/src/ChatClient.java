import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;


public class ChatClient extends Client {

    // socket and thread used to close the connection and join the thread to the main thread.
    private Thread readThread;

    /**
     * Write method used to write to the console and send the input to the server.
     * This method runs on the main thread of the process.
     * Has some validation to deal with the user quitting.
     *
     */
    @Override
    public void write(){
        // main thread used to write
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        // exit var used to escape while loop if user types exit command
        try {
            PrintWriter send = new PrintWriter(serverSocket.getOutputStream(), true);
            String inputString;
            // while loop only runs while connected to the server
            while (true) {
                // exit the loop straight away, prevents the loop from running twice
                inputString = input.readLine();
                if (inputString.equals("/Exit")){
                    readThread.interrupt();
                    send.println("/Exit");
                    break;
                } else {
                    // send input if enters loop
                    send.println(inputString);
                }
            }
            // close the read thread before the main thread finishes.
            readThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * read method used to read server output. Runs on a secondary thread to allow to read and write simultaneously.
     * Deals with Socket exception errors when the server ends before the user quits.
     * Also deals with the user trying to exit the server.
     */
    public void read() {
        // secondary thread to read server output
        readThread = new Thread(() -> {
            try {
                BufferedReader receive = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                String received;
                while (!Thread.interrupted()) {
                    // while connected to the server read the output
                    received = receive.readLine();
                    if (received == null){
                        System.out.println("Server Closed.");
                        break;
                    }
                    System.out.println(received);
                }
            } catch (SocketException e){
                // socket exception is thrown if the server doesn't close properly.
                System.out.println("Lost connection to server.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // if connection stops close the server socket.
                    serverSocket.close();
                    // server closed so no need to keep running.
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readThread.start();
    }

    @Override
    public void setServerName() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        // exit var used to escape while loop if user types exit command
        try {
            PrintWriter send = new PrintWriter(serverSocket.getOutputStream(), true);
            System.out.println("Enter your Name: ");
            send.println("/setName " + input.readLine());
        } catch (SocketException e) {
            // socket exception is thrown if the server doesn't close properly.
            System.out.println("Lost connection to server.");
            try {
                // if connection stops close the server socket.
                serverSocket.close();
                // server closed so no need to keep running.
                System.exit(0);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public static void main(String[] args){
        ChatClient chatClient = new ChatClient();
        chatClient.start(args);
    }
}
