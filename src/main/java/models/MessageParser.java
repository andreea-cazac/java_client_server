package models;

import java.util.ArrayList;

public class MessageParser {
    String header = "";
    String body = "";

public String getHeader(String serverMessage) {
    String[] splitMessage = serverMessage.split(" ");
    header = splitMessage[0];

    if (splitMessage[0].startsWith("OK")) {
        //case OK Goodbye
        if (splitMessage[1].equals("Goodbye")) {
            return header;
            //All other OK cases (OK PRV, OK IDENT...)
        } else {
            header = splitMessage[0] + " " + splitMessage[1];
        }
    }
    return header;
}

    public String getBody(String serverMessage){
    if (noBodyHeaders().contains(header)){
        return body;
    } else {
        if (serverMessage.length() > header.length()){
            body = serverMessage.substring(header.length() + 1);
        }
    }

 return body;
}

   private ArrayList<String> noBodyHeaders(){
       ArrayList<String> noBodyHeaders = new ArrayList<>();

       noBodyHeaders.add("PING");
       noBodyHeaders.add("PONG");
       noBodyHeaders.add("DSCN");
       noBodyHeaders.add("QUIT");
       noBodyHeaders.add("SRV");
       noBodyHeaders.add("LIST");
       noBodyHeaders.add("OK PRV");
       noBodyHeaders.add("OK SRV");
       noBodyHeaders.add("OK QST");

       return noBodyHeaders;
   }
}
