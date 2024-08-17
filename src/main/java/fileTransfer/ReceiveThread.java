package fileTransfer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.UUID;


public class ReceiveThread extends Thread{

    private long fileSize;
    private UUID uuid;
    private String fileName;
    private Path downloadPath;
    private Socket socket;
    private OutputStream os;
    private PrintWriter writer;
    private InputStream inputStream;
    private BufferedReader bufferedReader;

    public ReceiveThread(Path downloadPath, String fileName, long fileSize, UUID uuid ) throws IOException {
     this.fileSize = fileSize;
     this.uuid = uuid;
     this.fileName = fileName;
     this.downloadPath = downloadPath;

        socket = new Socket("127.0.1",1338);
        os = socket.getOutputStream();
        writer = new PrintWriter(os);
        inputStream = socket.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {
        try
        {
            writer.println("LOG" + " " + "receiver" + ":" + uuid);
            writer.flush();

            boolean waitingConfirmation = true;

            //waiting for OK LOG confirmation from server
            while (waitingConfirmation) {
                String lineFromServer = bufferedReader.readLine();

                //when OK LOG received => start receiving
                if (lineFromServer != null){

                    if (lineFromServer.equals("OK LOG")){
                        waitingConfirmation = false;
                        receiveFile(socket);
                    }
                }}
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert socket != null;
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveFile(Socket socket) throws IOException
    {
        byte [] bytes = new byte[(int) fileSize];
        inputStream = socket.getInputStream();
        FileOutputStream fr = new FileOutputStream(downloadPath+ File.separator  + fileName);
        inputStream.read(bytes,0,bytes.length);
        fr.write(bytes,0,bytes.length);
        System.out.println("The file was downloaded!");
    }
}
