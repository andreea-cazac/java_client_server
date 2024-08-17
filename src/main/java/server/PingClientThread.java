package server;

import java.io.*;
import java.net.Socket;

public class PingClientThread extends Thread {
    private boolean pongReceived;
    private PrintWriter printWriter;
    private final Socket socket;
    private String username;
    private boolean activePing;
    private boolean pingSent = false;

    public PingClientThread(Socket socket) {
        this.socket = socket;
        activePing = true;
    }

    public void run() {
         do {
            try {
                Thread.sleep(10000);
                OutputStream outputStream = socket.getOutputStream();
                printWriter = new PrintWriter(outputStream);
                printWriter.println("PING");
                printWriter.flush();
                pingSent = true;
                pongReceived = false;

                sleep(3000);

                if (!pongReceived){
                    System.out.println("Pong not received from socket: " + socket);
                 quit(socket, username);
                 break;
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (activePing);
    }
    public void setPongReceived() {
        this.pongReceived = true;
    }

    public boolean isPingSent() {
        return pingSent;
    }

    public void setPingSent(boolean pingSent) {
        this.pingSent = pingSent;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void quit(Socket socketToQuit, String username) {
        try {
                    printWriter.println(CommandsFailureMessage.getCMD_DISCONNECT());
                    printWriter.flush();
                    System.out.println(CommandsFailureMessage.getCMD_DISCONNECT());

                    if (Server.getLoggedInClients().containsKey(socketToQuit)) {
                        Server.getLoggedInClients().remove(socketToQuit, username);
                    }
                    socketToQuit.close();
                    activePing = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

    }
}
