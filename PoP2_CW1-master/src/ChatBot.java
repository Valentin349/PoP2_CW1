import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Bot class that connects to the server and listens to any input that is not from the bot.
 */
public class ChatBot extends Client{

    private String answer;
    // used to only start a conversation if the user first types Hello.
    private boolean startConversation = false;

    @Override
    public void write() {
        try {
            PrintWriter send = new PrintWriter(serverSocket.getOutputStream(), true);
            send.println(answer);
            if (answer.equals("goodbye.")){
                // send exit command to server so that it knows the bot is leaving
                send.println("/Exit");
            } else {
                // get response
                read();
            }
        } catch (SocketException e){
            // if the disconnected from server.
            System.out.println("Lost connection to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read() {
        try {
            BufferedReader receive = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String message = receive.readLine();
            // word list, used to check if the message came from the bot.
            String[] messageList = message.split(" ");
            while (messageList[0].equals("[Bot]") || !startConversation) {
                if (messageList[1].equals("Hello")){
                    startConversation = true;
                } else {
                    message = receive.readLine();
                    messageList = message.split(" ");
                }
            }
            // remove the name tag and send only the message as a string.
            messageList[0] = "";
            getAnswer(String.join(" ", messageList).trim());
            write();
        } catch (SocketException | NullPointerException e){
            // exception is thrown if the server doesn't close properly or messageList is empty caused by server quitting
            // before bot got input.
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // if connection stops close the server socket.
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to get a random response based on the input the bot receives.
     *
     * @param input : the string received from the user.
     */
    private void getAnswer(String input){
        // used to randomly select a response from a list.
        Random random = new Random();
        // responses for the default case
        List<String> defaults = Arrays.asList("Do you feel strongly about discussing such things ?",
                "Does talking about this bother you ?",
                "That is interesting. Please continue.",
                "Tell me more about that.",
                "What does that suggest to you ?",
                "Please go on.",
                "I'm not sure I understand you fully."
                );

        // responses for yes case
        List<String> yesList = Arrays.asList("I understand.",
                "You seem to be quite positive.",
                "I see.",
                "You are sure");

        // responses for no case
        List<String> noList = Arrays.asList("Why 'no' ?",
                "Are you saying no just to be negative?",
                "You are being a bit negative.",
                "Why not ?");

        // responses for why,where, who, how , etc.
        List<String> whyList = Arrays.asList("Have you asked such questions before ?",
                "Are such questions much on your mind ? ",
                "What answer would please you most ?",
                "Does that question interest you ?",
                "Have you asked anyone else ?",
                "What is it you really want to know ?",
                "Why do you ask ?",
                "What do you think ?",
                "What comes to mind when you ask that ?",
                "What is it you really want to know ?");

        // turns the string into lower case letters only
        String message = input.replaceAll("/[^A-Za-z]/", "").toLowerCase();
        switch (message){
            case "hello":
                answer = "Please tell me your problem. Be specific and go line by line.";
                break;
            case "yes":
            case "ye":
            case "yeah":
                answer = yesList.get(random.nextInt(yesList.size()));
                break;
            case "no":
            case "i dont know":
                answer = noList.get(random.nextInt(noList.size()));
                break;
            case "bye":
                // the bot will leave the server.
                answer = "goodbye.";
                break;
            case "why":
            case "when":
            case "what":
            case "how":
            case "where":
            case "who":
                answer = whyList.get(random.nextInt(whyList.size()));
                break;
            default:
                answer = defaults.get(random.nextInt(defaults.size()));
        }
    }

    public static void main(String[] args){
        ChatBot chatBot = new ChatBot();
        chatBot.setName("Bot");
        chatBot.start(args);
    }

}
