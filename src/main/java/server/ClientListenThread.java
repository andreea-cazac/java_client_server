package server;

import models.MessageParser;
import fileTransfer.FileThreadListener;
import models.Survey;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientListenThread extends Thread {
    private Socket socket;
    private InputStream is;
    private BufferedReader bufferedReader;
    private OutputStream os;
    private PrintWriter writer;
    private boolean state = true;
    private String header;
    private String body;
    private PingClientThread pingClientThread;

    private static ArrayList<Survey> surveys = new ArrayList<>();

    //user details
    private boolean loggedIn;
    private String user;
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,14}$";
    private MessageParser messageParser;
    public ClientListenThread(Socket socket) throws IOException {
        this.socket = socket;

        os = socket.getOutputStream();
        writer = new PrintWriter(os);
        writer.println(CommandsFailureMessage.getCMD_INIT() + " " + "Welcome to the server!");
        writer.flush();
        is = socket.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(is));


        pingClientThread = new PingClientThread(socket);
        pingClientThread.start();
        messageParser = new MessageParser();
    }

    public void run() {
        while (state) {
            try {
                String lineFromClient = bufferedReader.readLine();

                if (lineFromClient != null){
                    header = messageParser.getHeader(lineFromClient);
                    body = messageParser.getBody(lineFromClient);

                    if (!header.equals("PONG")){
                        System.out.println(user + "  ----->  server : " + header + " " + body);
                    }

                    if (CommandsFailureMessage.getHeaders().contains(header)) {
                        switch (header) {
                            case "IDENT" -> login(body);
                            case "BCST" -> sendEveryoneMessage(body);
                            case "LIST" -> sendLoggedClients();
                            case "PRV" -> handlePrivateMessage(body);
                            case "PONG" -> pongResponse();
                            case "SRV" -> approveSurvey();
                            case "QST" -> handleCreateSurvey(body);
                            case "ANSW" -> handleCheckAnswers(body);
                            case "EAS" -> handleSendPublicKey(body);
                            case "ESI" -> handleSendEncryptedAESKey(body);
                            case "MSG" -> handleSendEncryptedMessage(body);
                            case "FCM" -> handlePendingFileTransfer(body);
                            case "FCN" -> sendUUIDs();
                            case "FRJ" -> sendRejectToFileSender();
                            case "QUIT" -> goodbye();
                        }
                    } else {
                        //Unknown command
                        writer.println(CommandsFailureMessage.getFAIL00());
                        writer.flush();
                        System.out.println(CommandsFailureMessage.getFAIL00());
                    }
                }
            } catch (SocketException e) {
                if (e.getMessage().equals("Socket closed")) {
                    state = false;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void handlePendingFileTransfer(String body) {
        String[] line = body.split(":");
        String userToSend = line[0];
        String filePath = line[1];
        String fileSize = line[2];

        pendingFileTransfer( userToSend, filePath, fileSize);
    }

    private void handlePrivateMessage(String body) {
        String[] line = body.split(":");
        String userToSend = line[0];
        String message = line[1];

        privateMessage(userToSend, message);
    }

    private void handleCreateSurvey(String body) {
        String[] line0 = body.split("%");
        String usersLine = line0[0];
        String surveyLine = line0[1];

        createSurvey(surveyLine, usersLine);
    }

    private void handleCheckAnswers(String body) {
        String[] line0 = body.split("#");
        String surveyID = line0[0];
        String answers = line0[1];

        checkAnswers(Integer.parseInt(surveyID), answers);
    }

    private void handleSendPublicKey(String body) {
        String[] line = body.split(":");
        String userToSend = line[0];
        String publicKey = line[1];

        sendPublicKey(userToSend, publicKey);
    }

    private void handleSendEncryptedAESKey(String body) {
        String[] line = body.split(":");
        String userToSend = line[0];
        String encryptedAES = line[1];

        sendEncryptedAESKey(userToSend, encryptedAES);
    }

    private void handleSendEncryptedMessage(String body) {
        String[] line = body.split(":");
        String userToSend = line[0];
        String encryptedMessage = line[1];

        sendEncryptedMessage(userToSend, encryptedMessage);
    }


        private void pongResponse() {
        if (pingClientThread.isPingSent()) {
            pingClientThread.setPongReceived();
            pingClientThread.setPingSent(false);
        } else {
            writer.println(CommandsFailureMessage.getFAIL05());
            writer.flush();

            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL05());
        }
    }

    private void sendRejectToFileSender() {
        Server.getPendingFileTransfers().forEach((socketSender, receiverPerson) ->{

            if (receiverPerson.equals(user)){
                try {
                    OutputStream os = socketSender.getOutputStream();
                    PrintWriter writer = new PrintWriter(os);
                    writer.println(CommandsFailureMessage.getCMD_FILE_REJECT());
                    writer.flush();
                    System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_FILE_REJECT());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }});
    }

    private void sendLoggedClients() {

        if (!loggedIn){
            writer.println(CommandsFailureMessage.getFAIL03());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL03());
        } else {

            if (Server.getLoggedInClients().size() > 1){
                StringBuilder sb = new StringBuilder();

            Server.getLoggedInClients().forEach((socket1, user1) ->{
                if (!socket1.equals(socket)){
                sb.append(user1).append(",");
            }});

                writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_LIST() + " " + sb);
                writer.flush();
                System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_LIST() + " " + sb);
        } else {
                writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_LIST() + " " + "No other logged in clients in this server");
                writer.flush();
                System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_LIST() + " " + "No other logged in clients in this server");
            }
    }}
    private void sendPublicKey(String userToSend, String publicKey) {

        if (loggedIn){
            if (userToSend.isBlank()){
                writer.println(CommandsFailureMessage.getFAIL07());
                writer.flush();
            }

        if (Server.getLoggedInClients().containsValue(userToSend)){
            Server.getLoggedInClients().forEach((socket1, user1) -> {
            if (user1.equals(userToSend)) {
                try {
                    OutputStream os = socket1.getOutputStream();
                    PrintWriter writer = new PrintWriter(os);
                    writer.println(CommandsFailureMessage.getCMD_PBK() + " " + user + ":" + publicKey);
                    writer.flush();
                    System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_PBK() + " " + user + ":"  + publicKey);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    } else{
       writer.println(CommandsFailureMessage.getFAIL09());
       writer.flush();
   }
    } else {
            writer.println(CommandsFailureMessage.getFAIL03());
            writer.flush();
        }}

    private void sendEncryptedAESKey(String userToSend, String encryptedAES) {

        Server.getLoggedInClients().forEach((socket1, user1) -> {
            if (user1.equals(userToSend)) {
                try {
                    OutputStream os = socket1.getOutputStream();
                    PrintWriter writer = new PrintWriter(os);
                    writer.println(CommandsFailureMessage.getCMD_ENCRYPTION_SYMMETRIC() + " " + user + ":" + encryptedAES);
                    writer.flush();
                    System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_ENCRYPTION_SYMMETRIC() + " "  + user + ":" + encryptedAES);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void sendEncryptedMessage(String userToSend, String encryptedMessage) {
        Server.getLoggedInClients().forEach((socket1, user1) -> {
            if (user1.equals(userToSend)) {
                try {
                    OutputStream os = socket1.getOutputStream();
                    PrintWriter writer = new PrintWriter(os);
                    writer.println(CommandsFailureMessage.getCMD_MSG() + " " + user + ":" + encryptedMessage );
                    writer.flush();
                    System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_MSG() + " "  + user + ":" + encryptedMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void login(String username) {

        if (!isUsernameValid(username)){
            writer.println(CommandsFailureMessage.getFAIL02());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL02());
        }
        //check if username is logged in
        else if(checkIfUserExists(username)) {
            writer.println(CommandsFailureMessage.getFAIL01());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL01());
        }//check if user is logged in
        else if (loggedIn) {

            writer.println(CommandsFailureMessage.getFAIL04());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL04());


    } else if (isUsernameValid(username)) {
            //set username and logged in
            loggedIn = true;
            this.user = username;

            //sending confirm message
            writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_LOGIN() + " " + username);
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_LOGIN() + " " + user);

            Server.getLoggedInClients().put(socket, username);
            //pingClientThread.setUsername(username);

            sendInfoSomeoneLogged();
        }

    }
    public static boolean isUsernameValid(String str) {
        return str.matches(USERNAME_REGEX);
    }
    private boolean checkIfUserExists(String username){
        AtomicBoolean exists = new AtomicBoolean(false);

        Server.getLoggedInClients().forEach((socket1, user)->{
            if (username.equalsIgnoreCase(user)){
                exists.set(true);
            }
        });

        return exists.get();
    }
    private void sendInfoSomeoneLogged(){
        if (Server.getLoggedInClients().size() > 1){

            Server.getLoggedInClients().forEach((socket1, user1) -> {
                //send message to everyone except user who just logged in
                if (!user1.equals(user)) {
                    try {
                        OutputStream os = socket1.getOutputStream();
                        PrintWriter writer = new PrintWriter(os);
                        writer.println(CommandsFailureMessage.getCMD_JOINED() + " " + user);
                        writer.flush();
                        System.out.println("server -----> " + user1 + ":" + CommandsFailureMessage.getCMD_JOINED() + " "  + user);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    private void sendEveryoneMessage(String message) {

        if (!loggedIn) {
            writer.println(CommandsFailureMessage.getFAIL03());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL03());
        } else {
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_BROADCAST() + " " + message);
            writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_BROADCAST() + " " + message);
            writer.flush();

            //send message to everyone except who sent message itself
            Server.getLoggedInClients().forEach((socket1, user1) -> {
                try {
                    if (socket1 != socket) {
                        OutputStream os = socket1.getOutputStream();
                        PrintWriter writer = new PrintWriter(os);
                        writer.println(CommandsFailureMessage.getCMD_BROADCAST() + " " + user + ":" + message);
                        writer.flush();
                        System.out.println("server -----> " + user1 + ":" + CommandsFailureMessage.getCMD_BROADCAST() + " " + user + ":" + message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }

    }

    private void privateMessage(String username, String message) {
        if (!loggedIn) {
            writer.println(CommandsFailureMessage.getFAIL03());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL03());
        } else if (username.isEmpty() || username.isBlank()) {
            writer.println(CommandsFailureMessage.getFAIL07());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL07());
        } else if (Server.getLoggedInClients().size()==1) {
            writer.println(CommandsFailureMessage.getFAIL08());
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL08());
        } else {

            if (Server.getLoggedInClients().containsValue(username)) {
                Server.getLoggedInClients().forEach((socket1, user1) -> {
                    if (user1.equals(username)) {
                        try {
                            OutputStream os = socket1.getOutputStream();
                            PrintWriter printWriter = new PrintWriter(os);
                            //send message to receiver
                            printWriter.println(CommandsFailureMessage.getCMD_PRV() + " " + user + ":" + message);
                            printWriter.flush();
                            System.out.println("server -----> " + user1 + ":" + CommandsFailureMessage.getCMD_PRV() + " " + user + ":" + message);

                            //send confirmation to sender
                            writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_PRV() );
                            writer.flush();
                            System.out.println("server -----> " + user1 + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_PRV());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } else{
                writer.println(CommandsFailureMessage.getFAIL09());
                writer.flush();
                System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL09());
            }}
        }


    private void approveSurvey(){
        if (loggedIn){
        if (Server.getLoggedInClients().size() >= 3) {
            writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_SRV() + " " + "Approved!");
            writer.flush();

            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_SRV());
        } else {
            writer.println(CommandsFailureMessage.getFAIL06());
            writer.flush();

            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL06());
        }
    } else {
            writer.println(CommandsFailureMessage.getFAIL03());
            writer.flush();

            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL03());
        }
    }

    private void createSurvey(String surveyLine, String usersLine) {

        Survey survey = new Survey(surveyLine, usersLine);
        surveys.add(survey);

        int surveyID = surveys.indexOf(survey) + 1;

        sendEveryoneSurvey(surveyID, survey.getServerMessage(), survey.getUsers());
    }
    private void sendEveryoneSurvey(int surveyID, String serverMessage, ArrayList<String> users) {

            for (String user: users
            ) {
                Server.getLoggedInClients().forEach((socket1, user1) -> {
                    try {
                        if (user1.equals(user)) {
                            OutputStream os = socket1.getOutputStream();
                            PrintWriter writer = new PrintWriter(os);
                            writer.println(CommandsFailureMessage.getCMD_SND() + " " + surveyID + "%" + serverMessage);
                            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_SND() + " " + surveyID + "%" + serverMessage);
                            writer.flush();
                        } }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
    }

    private void checkAnswers(int surveyID, String answers){

        for (int i = 0; i < surveys.size(); i++) {
            //we increase +1 to i because we increased the assigned serverID by 1 for convenience
            //We increased initially the surveyID so the user will never see an ID = 0.
            if (surveyID == i+1){
            surveys.get(i).checkAnswers(answers);

                //when all users voted => statistics are sent to the server
                if (surveys.get(i).getAmountOfUsersVoted() == surveys.get(i).getUsers().size()){
                    surveys.get(i).statisticsMessage();

                    sendStatistics(surveys.get(i).statisticsMessage(), surveys.get(i).getAmountOfUsersVoted());
                }
             }
        }
    }

    private void sendStatistics(String message, int usersParticipated){
        Server.getLoggedInClients().forEach((socket1, user1) -> {
            try {
                OutputStream os = socket1.getOutputStream();
                PrintWriter writer = new PrintWriter(os);
                writer.println(CommandsFailureMessage.getCMD_RSL() + " " + usersParticipated + "%" + message);
                writer.flush();
                System.out.println("server -----> " + user + ":" +CommandsFailureMessage.getCMD_RSL() + " " + usersParticipated + "%" + message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void sendUUIDs() {
        UUID uuid = null;
        if (body.equalsIgnoreCase("Y")){
            uuid = UUID.randomUUID();

            writer.println(CommandsFailureMessage.getCMD_UUID_FILE() + " " + uuid);
            writer.flush();
            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_UUID_FILE() + " " + uuid);

        }
        sendResponseToSender(user, body, uuid);
    }

    private void pendingFileTransfer(String userToSend, String filePath, String fileSize) {
        if (!userToSend.isBlank() && !filePath.isBlank() && !fileSize.isBlank()) {

            Server.getPendingFileTransfers().put(socket, userToSend);

            askForConfimation(userToSend, filePath, fileSize);
            new FileThreadListener(Integer.parseInt(fileSize)).start();
        } else {
            writer.println(CommandsFailureMessage.getFAIL09());
            writer.flush();

            System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getFAIL09());
        }
    }
    private void sendResponseToSender(String receiver, String response, UUID uuid) {

        Server.getPendingFileTransfers().forEach((socketSender, receiverPerson) ->{

            if (receiverPerson.equals(receiver)){
                try {
                    OutputStream os = socketSender.getOutputStream();
                    PrintWriter writer = new PrintWriter(os);
                    writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_FILE_CONFIRMATION() + " " + response + ":" + uuid);
                    writer.flush();
                    System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + CommandsFailureMessage.getCMD_FILE_CONFIRMATION() + " " + response + ":" + uuid);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }});

    }


    private void askForConfimation(String receiver,String filePath, String fileSize) {

        Server.getLoggedInClients().forEach((socket1, user1) -> {
            if (user1.equals(receiver)) {
                try {
                    OutputStream os = socket1.getOutputStream();
                    PrintWriter writer = new PrintWriter(os);
                    writer.println(CommandsFailureMessage.getCMD_FILE_TRANSFER() + " " + user + ":" + filePath + ":" + fileSize);
                    writer.flush();
                    System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_FILE_TRANSFER() + " " + user + ":" + filePath + ":" + fileSize);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void goodbye() {

        if (Server.getLoggedInClients().containsKey(socket)){
            Server.getLoggedInClients().remove(socket, user);
        }

        writer.println(CommandsFailureMessage.getCMD_CONFIRM() + " " + "Goodbye");
        writer.flush();
        System.out.println("server -----> " + user + ":" + CommandsFailureMessage.getCMD_CONFIRM() + " " + "Goodbye");
    }
}

