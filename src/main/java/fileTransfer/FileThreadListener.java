package fileTransfer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class FileThreadListener extends Thread {

    private static final int PORT = 1338;
    private InputStream is;
    private BufferedReader bufferedReader;
    private int fileLength;

    private ArrayList<UUID> pendingFileTransfers;
    private Socket sender;
    private Socket receiver;
    private boolean matchChecksum = false;
    private static final String CHECKSUM_ALGORITHM = "MD5";
    public FileThreadListener(int fileLength) {
        this.fileLength = fileLength;
        pendingFileTransfers = new ArrayList<>();
    }

    @Override
    public void run() {
        try
        {
            ServerSocket serverSocket = new ServerSocket(PORT);

            boolean listening = true;

            while (listening){
            Socket socket1 = serverSocket.accept();

            is = socket1.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            String lineFromClient = bufferedReader.readLine();

            if (lineFromClient != null){
                String[] store = lineFromClient.split(" ");
                String header = store[0];
                String body = store[1];

                //Thread sent LOG message
                if (header.equals("LOG")) {
                    String[] line = body.split(":");
                    String role = line[0];
                    UUID uuid = UUID.fromString(line[1]);

                    if (role.equals("sender")){
                        sender = socket1;
                    } else if (role.equals("receiver")){
                        receiver = socket1;
                    }

                    if (existsUUID(uuid)) {

                        OutputStream os = sender.getOutputStream();
                        PrintWriter writer = new PrintWriter(os);
                        writer.println("OK LOG");
                        writer.flush();

                        byte[] bytes = readBytes(sender);

                        if (matchChecksum){
                            OutputStream os1 = receiver.getOutputStream();
                            PrintWriter writer1 = new PrintWriter(os1);
                            writer1.println("OK LOG");
                            writer1.flush();

                            sleep(500);
                            flushBytes(bytes, receiver);
                            listening = false;
                        }
                    } else {
                        pendingFileTransfers.add(uuid);
                    }
                }
            }}
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void flushBytes(byte[] b, Socket receiver) throws IOException {
        OutputStream os = receiver.getOutputStream();
        os.write(b,0, b.length);
        os.flush();
        System.out.println("Bytes sent to receiver!");
        receiver.close();
    }

    private byte[] readBytes(Socket sender) throws IOException, NoSuchAlgorithmException {
        byte bytes[] = new byte[(int) fileLength];

        System.out.println("Bytes received from sender!");

        InputStream is = sender.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        String lineFromServer = bufferedReader.readLine();
        String[] filePlusChecksum = lineFromServer.split(" ");
        String file = filePlusChecksum[0];
        byte[] fileBytes = Base64.getDecoder().decode(file);

        String checksum = filePlusChecksum[1];
        byte[] checksumBytes = Base64.getDecoder().decode(checksum);

        matchChecksum = compareChecksum(fileBytes, checksumBytes);
        sender.close();
        return bytes;
    }
    private boolean existsUUID(UUID uuid) {
        boolean exists = false;

        for (UUID id:pendingFileTransfers
             ) {
            exists = id.compareTo(uuid) == 0;
        }
            return exists;
    }

    private boolean compareChecksum(byte[] fileBytes, byte[] checksumBytes) throws NoSuchAlgorithmException, IOException {

        // Calculate checksum of received file
        byte[] localChecksum = getChecksum(fileBytes);

        // Compare checksums
        if (MessageDigest.isEqual(checksumBytes, localChecksum)) {
            return true;
        } else {
            System.err.println("Checksums do not match! File transfer will be aborted");
            return false;
        }
    }

    private static byte[] getChecksum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
        return md.digest(bytes);
    }
}
