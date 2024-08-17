package client;

import fileTransfer.FileTransfer;
import fileTransfer.ReceiveThread;
import fileTransfer.SenderThread;
import models.Encryption;
import models.MessageParser;
import models.Survey;
import server.CommandsFailureMessage;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;

public class ServerListenThread extends Thread{

    private Socket socket;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private MessageParser messageParser;
    private OutputStream outputStream;
    private PrintWriter printWriter;
    private Encryption encryption;
    private boolean state = true;
    private FileTransfer fileTransfer;


    public ServerListenThread(Socket socket, Encryption encryption, FileTransfer fileTransfer){
        this.socket = socket;
        this.encryption = encryption;
        this.fileTransfer= fileTransfer;
    }

    public void run(){

        if (socket != null){
        while (state){

            try{

                if (!socket.isClosed()){
                messageParser = new MessageParser();
                inputStream = socket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String lineFromServer = bufferedReader.readLine();

                if (lineFromServer != null) {

                    String header = messageParser.getHeader(lineFromServer);
                    String body = messageParser.getBody(lineFromServer);
                    outputStream = socket.getOutputStream();
                    printWriter = new PrintWriter(outputStream);

                    switch (header) {
                        case "OK IDENT" -> System.out.println("You logged in successfully with a username: " + body);
                        case "OK SRV" -> System.out.println("Press A to send the questions for the survey!");
                        case "OK QST" -> System.out.println("Press U to select the users for the survey!");
                        case "OK BCST" -> System.out.println("You sent this message: " + body);
                        case "OK LIST" -> System.out.println("Logged in clients: " + body);
                        case "OK PRV" -> System.out.println("Private message sent!");
                        case "JOINED" -> System.out.println("New user joined the server:  " + body);
                        case "PRV" -> System.out.println("Private message from -> " + body);
                        case "BCST" -> System.out.println("Broadcast" + " message from -> " + body);
                        case "DSCN" -> {
                            socket.close();
                            System.out.println("Disconnected...");
                        }
                        case "SND" -> printSurvey(body);
                        case "RSL" -> printStatistics(body);
                        case "OK EAS" -> sendEncryptedPublicKey();
                        case "PBK" -> encryptPublicKeyWithAES(body);
                        case "ESI" -> encryptMessage(body);
                        case "MSG" -> decryptMessage(body);
                        case "FCM" -> fileTransferRequest(body);
                        case "OK FCN" -> startUploadingFile(body);
                        case "FRJ" -> System.out.println("File transfer was rejected!");
                        case "UUID" -> startDownloadingFile(body);
                         case "PING" -> sendPong();
                        case "OK" -> bye();
                        case "OK QUIT" -> System.out.println("Bye!");
                        case "FAIL00" -> throw new ChatException(CommandsFailureMessage.getFAIL00());
                        case "FAIL01" -> throw new ChatException(CommandsFailureMessage.getFAIL01());
                        case "FAIL02" -> throw new ChatException(CommandsFailureMessage.getFAIL02());
                        case "FAIL03" -> throw new ChatException(CommandsFailureMessage.getFAIL03());
                        case "FAIL04" -> throw new ChatException(CommandsFailureMessage.getFAIL04());
                        case "FAIL05" -> throw new ChatException(CommandsFailureMessage.getFAIL05());
                        case "FAIL06" -> throw new ChatException(CommandsFailureMessage.getFAIL06());
                        case "FAIL07" -> throw new ChatException(CommandsFailureMessage.getFAIL07());
                        case "FAIL08" -> throw new ChatException(CommandsFailureMessage.getFAIL08());
                        case "FAIL09" -> throw new ChatException(CommandsFailureMessage.getFAIL09());
                    }
                }} else {
                    state = false;
                }

                //When server was disconnected
                if (!socket.isConnected()){
                    System.out.println("Server got disconnected. Bye bye!");
                    state = false;
                    socket.close();
                }

            }catch (IOException | ChatException e){
                System.err.println(e.getMessage());
                if (e.getMessage().equals("Socket closed") || e.getMessage().contains("Connection reset")) {
                    state = false;
                    System.out.println("Socket was closed! Bye!");
                    break;
                }
            } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                     NoSuchAlgorithmException | InvalidKeyException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }}}

    private void startDownloadingFile(String body) throws IOException {
        UUID uuid = UUID.fromString(body);
        new ReceiveThread(fileTransfer.getDownloadPath(), fileTransfer.getName(), fileTransfer.getFileLength(), uuid).start();
    }

    private void sendPong() {
        printWriter.println("PONG");
        printWriter.flush();
    }

    private void fileTransferRequest(String body) {
        String[] line = body.split(":");
        String userFrom = line[0];
        String filePath = line[1];
        String fileSize = line[2];
        fileTransfer.setName(filePath);
        fileTransfer.setFileLength(Long.parseLong(fileSize));

        System.out.println("A file transfer request comes from: " + userFrom);
        System.out.println("Press Y if you accept the file and N if you refuse the transfer. \n" +
                "You have to respond with Y or N , nothing more, otherwise you will get error!");
    }

    private void startUploadingFile(String body) throws IOException {
        String[] line = body.split(":");
        UUID uuid = null;

        if (line.length > 1) {
            uuid = UUID.fromString(line[1]);
        }

        System.out.println("File transfer was accepted!");

        new SenderThread(fileTransfer.getFile(), uuid).start();
    }

    private void bye() throws IOException {
        System.out.println("Bye!");
        socket.close();
        state = false;
    }

    private void printSurvey(String body) {
        String[] bodyLine = body.split("%");
        String surveyID = bodyLine[0];
        String surveyLine = bodyLine[1];

        Survey survey = new Survey(surveyLine);

        System.out.println("New survey arrived!");
        System.out.println(" ");
        System.out.println( "The ID of the survey is: " + surveyID);
        System.out.println(survey);
        System.out.println(" ");
        System.out.println("If you want to participate in the survey press R");
    }

    public void printStatistics(String statisticsLine){

        System.out.println("Survey has finished! Look at the summary: ");

        String[] line = statisticsLine.split("%");
        int participatedUsers = Integer.parseInt(line[0]);
        String results = line[1];

        System.out.println(" ");
        System.out.println("Number of participants: " + participatedUsers);
        String[] line1 = results.split("/");
        for (int i = 0; i < line1.length; i++) {
            String questionLine = line1[i];

            String[] line2 = questionLine.split(";");
            String question = line2[0];
            String answersVotes = line2[1];

            String[] line3 = answersVotes.split(",");

            String answer1 = line3[0];
            String answer2 = line3[1];
            String answer3 = line3[2];

            String votes1 = line3[3];
            String votes2 = line3[4];
            String votes3 = line3[5];


            System.out.println((i+1) + ": " + question);
            System.out.println(answer1 + ": " + votes1 + " votes");
            System.out.println(answer2 + ": " + votes2 + " votes");
            System.out.println(answer3 + ": " + votes3 + " votes");
            System.out.println(" ");
        }
    }

    private void encryptPublicKeyWithAES(String body) throws IOException, ClassNotFoundException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String[] line = body.split(":");
        String userToSend = line[0];
        String publicKeyLine = line[1];

        PublicKey publicKey = (PublicKey) deserialize(Base64.getDecoder().decode(publicKeyLine));
        String toBeSent = encryption.encryptPublicKey(publicKey);
        printWriter.println("ESI" + " " + userToSend + ":"  + toBeSent);
        printWriter.flush();
    }
    private void sendEncryptedPublicKey() throws IOException {
        byte[] objectToSend = serialize(encryption.getPublicKey());
        String toBeSent = Base64.getEncoder().encodeToString(objectToSend);
        printWriter.println("KEY" + " " + toBeSent);
        printWriter.flush();
    }
    private void encryptMessage(String body) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String[] line = body.split(":");
        String userToSend = line[0];
        String symmetricKeyLine = line[1];

        SecretKey AES = encryption.decryptAESKey(symmetricKeyLine);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, AES);
        byte[] encryptedBytes = cipher.doFinal(encryption.getMessage().getBytes());
        String toBeSent = Base64.getEncoder().encodeToString(encryptedBytes);
        printWriter.println("MSG" +  " " + userToSend + ":" + toBeSent );
        printWriter.flush();
    }
    private void decryptMessage(String body) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String[] line = body.split(":");
        String user = line[0];
        String messageEncrypted = line[1];

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, encryption.getAesKey());
        byte[] decodedBytes = Base64.getDecoder().decode(messageEncrypted);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        String message = new String(decryptedBytes);
        System.out.println("Encrypted message receieved from " + user + " -> " + message);
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
}

