package client;

import fileTransfer.FileTransfer;
import models.Encryption;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
public class KeyboardListenThread extends Thread{

    private Socket socket;
    private String username;
    private Scanner s;
    private PrintWriter writer;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader reader;
    private Encryption encryption;
    private FileTransfer fileTransfer;
    private boolean state = true;
    public KeyboardListenThread(Socket socket, Encryption encryption, FileTransfer fileTransfer) throws IOException {
        this.encryption = encryption;
        this.socket = socket;
        this.fileTransfer = fileTransfer;
    }

    public void run() {
        if (socket != null){

            try {
                outputStream = socket.getOutputStream();
                writer = new PrintWriter(outputStream);

                inputStream = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                checkSocketClosed();

                    String line = reader.readLine();
                    System.out.println("-------------------------------");
                    System.out.println(line);
                    System.out.println("-------------------------------");
                    System.out.println();


                    String help = "Commands:\n" +
                            "\n" +
                            "L: Login\n" +
                            "Help: Help\n" +
                            "B: Broadcast \n" +
                            "LI: List \n" +
                            "P: Private message\n" +
                            "S: Create survey \n" +
                            "E: Encrypted private message \n" +
                            "F: Send a file \n" +
                            "Q: Quit\n";

                    System.out.println(help);

                while (state) {
                    if (!socket.isClosed()) {
                    s = new Scanner(System.in);
                    String c = s.nextLine().toLowerCase();

                    switch (c) {
                        case "help" -> System.out.println(help);
                        case "l" -> login();
                        case "b" -> broadcast();
                        case "li" -> requestList();
                        case "p" -> privateMessage();
                        case "s" -> requestSurvey();
                        case "a" -> sendSurvey();
                        case "u" -> selectUsers();
                        case "r" -> answerQuestions();
                        case "e" -> sendPublicKeyEncryption();
                        case "q" -> quit();
                        case "f" -> sendFile();
                        case "y" -> specifyPath();
                        case "n" ->{
                            writer.println("FRJ" + " " + "n");
                            writer.flush();
                        }
                    }

                    //if socket was closed
                } else {
                    state = false;
                }}

                //When server was disconnected
                if (!socket.isConnected()){
                    System.out.println("Server got disconnected. Bye bye!");
                    state = false;
                    socket.close();
                }

            } catch (IOException ioException) {
                System.err.println(ioException.getMessage());
            }
    }}

    private void quit() {
        writer.println("QUIT");
        writer.flush();
        state = false;
    }

    public void checkSocketClosed(){
        if (socket.isClosed()) {
            writer.println("QUIT");
            writer.flush();
            state = false;
        }
    }
    private String selectUsers() throws IOException {
        boolean usersNotSelected = true;
        StringBuilder result = new StringBuilder();
        String users = "";
        System.out.println("Select the user you want to send the survey");
        while (usersNotSelected) {
            System.out.println("Type the username:");
            String user = s.nextLine();
            if (!user.equalsIgnoreCase(username)) {
                result.append(user);
            } else {
                System.out.println("You are not allowed to add yourself as a user");
            }

            boolean validAnswer = false;

            while (!validAnswer) {
                System.out.println("Do you want to add more users? Y/N");
                String answer = s.nextLine();
                if (answer.equalsIgnoreCase("y")) {
                    result.append(";");
                    System.out.println("Go on...");
                    validAnswer = true;
                } else if (answer.equalsIgnoreCase("n")) {
                    users = result.toString();
                    usersNotSelected = false;
                    System.out.println("Bye then!");
                    validAnswer = true;
                } else {
                    System.out.println("Please type Y or N");
                }
            }
        }
        return users;
    }

    private void requestSurvey() throws IOException {
        writer.println("SRV");
        writer.flush();
    }

    private void sendSurvey() throws IOException {
        boolean surveyCreated = false;
        int maxQuestions = 0;

        HashMap<String, ArrayList<String>> questions = new HashMap<>();
         while (!surveyCreated ){

             boolean invalidAnswer = true;

             while(invalidAnswer) {
                 ArrayList<String> answers = new ArrayList<>();
                 String question = null;

                 boolean invalidQuestion = true;
                 while (invalidQuestion) {
                     System.out.println("Type your question:");
                     question = s.nextLine();

                     //if question is not empty => question is valid => going on
                     if (!question.isBlank()){
                         invalidQuestion = false;
                     } else {
                         System.out.println("Your question cannot be empty, try again!");
                     }
                 }
                 System.out.println("Type your correct answer:");
                 String correctAnswer = s.nextLine();
                 answers.add(correctAnswer);
                 for (int i = 0; i < 2; i++) {
                     System.out.println("Type the wrong answer:");
                     String answer = s.nextLine();
                     answers.add(answer);
                 }

                 if (checkForAllAnswers(answers)){
                     invalidAnswer = false;
                     questions.put(question, answers);
                     maxQuestions++;
                 } else {
                     System.out.println("You didn't provide 3 valid answers, try to rewrite the question again.");
                 }
             }

             if (maxQuestions == 10){
                 surveyCreated = true;
                 System.out.println("You have added 3 questions!");
                 sendSurveyData(questions, selectUsers());
             } else{
                  System.out.println("Do you want to add more questions? Y/N");
                  String answer = s.nextLine();
                  if (answer.equalsIgnoreCase("y")){
                      System.out.println("Go on...");
                  } else if (answer.equalsIgnoreCase("n")) {
                      surveyCreated = true;
                      System.out.println("You added " + questions.size() + " questions!");
                      sendSurveyData(questions, selectUsers());
                  } else {
                      throw new IllegalStateException();
                  }
          }}
        }

    private String separateAnswers(ArrayList<String> answers) {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).startsWith(" ")){
                String answer = answers.get(i).substring(1, answers.get(i).length()-1);
                answers.set(i, answer);
            }
            if (answers.get(i).endsWith(" ")){
                String answer = answers.get(i).substring(0, answers.get(i).length()-2);
                answers.set(i, answer);
            }

            if (i==answers.size()-1){
                stb.append(answers.get(i));
            } else {
            stb.append(answers.get(i)).append(",");
        }}
        return stb.toString();
    }

    private boolean checkForAllAnswers(ArrayList<String> answers){
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i).isBlank()){
                return false;
            }
        }
        return true;
    }


private void sendSurveyData(HashMap<String, ArrayList<String>> hashMap, String users) {
        StringBuilder result = new StringBuilder();

        result.append(users).append("%");
    hashMap.forEach((k,v) -> {
       if (k.startsWith(" ")) {
           k = k.substring(1, k.length()-1);
       }

      if(k.endsWith(" ")){
           k = k.substring(0, k.length()-2);
       }

        result.append(k + ";" + separateAnswers(v) + "/");
    });

    String serverMessageSurvey = result.toString();

    writer.println("QST" + " " + serverMessageSurvey);
    writer.flush();
}

private void answerQuestions(){

    System.out.println("You have to answer all questions in one line. Please remember to insert -> %  after each answer. \n" +
            "If you want to choose answer 1 => type 1; Same for the rest of answers \n" +
            "If you do not know the right answer just leave an empty space between the separators \n" +
            "If you will forget to put the separator sign after each answer, your answers will be automatically wrong for this survey! \n" +
            "You have 5 minutes to answer them.");

    System.out.println("Type the ID of the survey you want to answer");
    int surveyID = Integer.parseInt(s.nextLine());
    System.out.println("\n");

    boolean invalidAnswer = true;
    String answer = null;

    while (invalidAnswer) {
        System.out.println("Type your answer here:");
        answer = s.nextLine();

        if (!answer.isBlank() && isValidAnswer(answer)){
            invalidAnswer = false;

            writer.println("ANSW" + " " + surveyID + "#" + answer);
            writer.flush();
            System.out.println("Answers successfully sent!");
        } else {
            System.err.println("Your answer is invalid! \n Take into account that you have to answer at least one question, and each answer MUST BE in the range of 1-3 because you have only questions with 3 choices.");
        }
    }
}

    private boolean isValidAnswer(String answer) {
        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            if (c != '1' && c != '2' && c != '3' && c != '%') {
                return false;
            }
        }
        return true;
    }
    private void requestList() throws IOException {
        writer.println("LIST");
        writer.flush();
    }

    public void login() throws IOException {
        System.out.println("Login to our system by providing an username:\n(A username may only consist of characters, numbers, and underscores ('_') and has a length between 3 to 14 characters)");
        username = s.nextLine();
        writer.println("IDENT" + " " + username);
        writer.flush();
    }
    public void broadcast() throws IOException {
        System.out.println("Type your message here: ");
        String message = s.nextLine();
        writer.println("BCST" + " " + message);
        writer.flush();
    }
    public void privateMessage() throws IOException{
        System.out.println("Type username you want to send a message: ");
        String username = s.nextLine();
        System.out.println("Type your message here: ");
        String message = s.nextLine();

        writer.println("PRV" + " " + username + ":" + message);
        writer.flush();
    }
    private void sendFile() {

        System.out.println("To send a file, you are going to wait for the confirmation of this user! Write his username: ");
        String userFileTransfer = s.nextLine();
        System.out.println("Enter the path with the file name \n" +
                "Example: D:\\Documents\\Word\\helloWorld.docx ): ");
        String fileName = s.nextLine();
        File file = new File(fileName);
        fileTransfer.setFile(file);
        if (!file.isFile()) {
            System.out.println("File transfer failed because the path specified is not valid. Try again!");
        } else {
            writer.println("FCM" + " " + userFileTransfer + ":" + file.getName() + ":" + file.length());
            writer.flush();
            System.out.println("Destination path for the file is valid.");
        }
    }
    private void specifyPath() {
        boolean invalidAnswer = true;

        while (invalidAnswer) {
            System.out.println("Specify the path where you want to download the file:");
            String receiversPath = s.nextLine();
            Path path = Paths.get(receiversPath);
            if (Files.exists(path) && Files.isDirectory(path) && Files.isWritable(path)) {
                // Check if there is enough free space available
                File file = path.toFile();
                long freeSpace = file.getFreeSpace();
                long requiredSpace = 1024 * 1024 * 100; // 100 MB
                if (freeSpace < requiredSpace) {
                    System.out.println("Not enough free space available to download the file.");
                } else {
                    System.out.println("The path is a valid directory for downloading a file.");
                    fileTransfer.setDownloadPath(path);

                    writer.println("FCN" + " " + "y");
                    writer.flush();
                    invalidAnswer = false;
                }
            } else {
                System.out.println("The path is not a valid directory for downloading a file.");
            }
        }
    }

    private void sendPublicKeyEncryption() throws IOException {
        System.out.println("Type username you want to send a message: ");
        String userEncrypt = s.nextLine();
        System.out.println("Type the message you want to send: ");
        String messageToSend = s.nextLine();
        encryption.setMessage(messageToSend);
        byte[] objectToSend = serialize(encryption.getPublicKey());
        String toBeSent = Base64.getEncoder().encodeToString(objectToSend);
        writer.println("EAS" + " " + userEncrypt + ":" + toBeSent);
        writer.flush();

        System.out.println("Message sent!");
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
}


