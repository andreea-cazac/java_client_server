package client;

import fileTransfer.FileTransfer;
import models.Encryption;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


public class Client {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        Socket socket = null;

        Encryption encryption = new Encryption();
        FileTransfer fileTransfer = new FileTransfer();
        try {
            socket = new Socket("localhost", 3000);
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage());
        }

        new KeyboardListenThread(socket, encryption, fileTransfer).start();
        new ServerListenThread(socket, encryption, fileTransfer).start();
    }



}
