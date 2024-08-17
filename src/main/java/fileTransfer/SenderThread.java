package fileTransfer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class SenderThread extends Thread{

    private File file;
    private UUID uuid;
    private Socket socket;
    private OutputStream os;
    private PrintWriter writer;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private static final String CHECKSUM_ALGORITHM = "MD5";
    public SenderThread(File file, UUID uuid) throws IOException {
        this.file = file;
        this.uuid = uuid;
        socket = new Socket("127.0.1", 1338);
         os = socket.getOutputStream();
         writer = new PrintWriter(os);
         inputStream = socket.getInputStream();
         bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {

            try {
                writer.println("LOG" + " " + "sender" + ":" + uuid);
                writer.flush();
                boolean waitingConfirmation = true;

                //waiting for OK LOG confirmation from server
                while (waitingConfirmation) {

                    String lineFromServer = bufferedReader.readLine();

                    if (lineFromServer != null){

                    if (lineFromServer.equals("OK LOG")){
                        sendFile();
                        waitingConfirmation = false;
                    } }}

            } catch (IOException e) {
                System.err.println(e);
            } finally {
                try {
                    assert socket != null;
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }}

           private void sendFile() {
               try {

                   byte[] bytearray = Files.readAllBytes(Path.of(file.getAbsolutePath()));
                   String fileBytesString = Base64.getEncoder().encodeToString(bytearray);

                   byte[] checksum = getChecksum(bytearray);
                   String sumBytesString = Base64.getEncoder().encodeToString(checksum);

                   System.out.println("Uploading Files...");
                   writer.println(fileBytesString + " " + sumBytesString);
                   writer.flush();

               } catch (IOException e) {
                   e.printStackTrace();
               } catch (NoSuchAlgorithmException e) {
                   throw new RuntimeException(e);
               }
           }

    private byte[] getChecksum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
        return md.digest(bytes);
    }
}



