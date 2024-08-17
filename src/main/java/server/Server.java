package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static HashMap<Socket, String> pendingFileTransfers = new HashMap<>();
    private static HashMap<Socket, String> loggedInClients = new HashMap<>() {
        @Override
        public String toString() {
            StringBuilder stb = new StringBuilder();
            for (Entry<Socket, String> entry : this.entrySet()) {
                System.out.println(entry);
                stb.append(entry.getValue()).append(",");
            }
            return stb.toString();
        }
    };
    public static void main(String[] args) throws IOException {

        final int SERVER_PORT = 3000;
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Server started on port " + SERVER_PORT);
        
        while(true){
            checkClosedSockets();
            checkServerSocketClosed(serverSocket);
            Socket socket = serverSocket.accept();
            new ClientListenThread(socket).start();

        }
    }

    private static void checkServerSocketClosed(ServerSocket serverSocket) {
        if (serverSocket.isClosed()) {
            // If the server socket is closed, delete the stored array
            loggedInClients = null;
        }
    }

    private static void checkClosedSockets(){
        for (HashMap.Entry<Socket, String> entry : Server.getLoggedInClients().entrySet()) {
            Socket socket1 = entry.getKey();
            String user1 = entry.getValue();

            if (socket1.isClosed()) {
                loggedInClients.remove(socket1, user1);
                System.out.println("Closed socket of user: " + user1);
            }
        }
    }

    public static HashMap<Socket, String> getPendingFileTransfers() {
        return pendingFileTransfers;
    }

    public static HashMap<Socket, String> getLoggedInClients() {
        return loggedInClients;
    }
}