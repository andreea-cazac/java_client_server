package server;

import java.util.ArrayList;

public class CommandsFailureMessage {

    //commands
    public static final String CMD_BROADCAST = "BCST";
    public static final String CMD_CONFIRM = "OK";
    public static final String CMD_DISCONNECT = "DSCN";
    public static final String CMD_INIT = "INIT";
    public static final String CMD_LOGIN = "IDENT";
    public static final String CMD_JOINED = "JOINED";
    public static final String CMD_PRV = "PRV";
    public static final String CMD_LIST = "LIST";
    public static final String CMD_SRV = "SRV";
    public static final String CMD_QST = "QST";
    public static final String CMD_ANSW = "ANSW";
    public static final String CMD_SND = "SND";
    public static final String CMD_RSL = "RSL";
    public static final String CMD_ENCRYPTION_ASYMMETRIC = "EAS";
    public static final String CMD_PBK = "PBK";
    public static final String CMD_ENCRYPTION_SYMMETRIC = "ESI";
    public static final String CMD_MSG = "MSG";
    public static final String CMD_FILE_TRANSFER = "FCM";
    public static final String CMD_FILE_CONFIRMATION = "FCN";
    public static final String CMD_FILE_REJECT = "FRJ";
    public static final String CMD_UUID_FILE = "UUID";
    public static final String CMD_PING = "PING";
    public static final String CMD_PONG = "PONG";
    public static final String CMD_QUIT = "QUIT";
    public static final String FAIL00 = "FAIL00 Unknown command";
    public static final String FAIL01 = "FAIL01 User already logged in";
    public static final String FAIL02 = "FAIL02 Username has an invalid format or length";
    public static final String FAIL03 = "FAIL03 Please log in first";
    public static final String FAIL04 = "FAIL04 User cannot login twice";
    public static final String FAIL05 = "FAIL05 Pong without ping";
    public static final String FAIL06 = "FAIL06 More than 3 users must be logged in!";
    public static final String FAIL07 = "FAIL07 You must specify the requested details fully!";
    public static final String FAIL08 = "FAIL08 You are the only one logged in, wait for others to connect";
    public static final String FAIL09 = "FAIL09 User was not found in the list";

    public static ArrayList<String> getHeaders() {
   ArrayList<String> headers = new ArrayList<>();

        headers.add(CMD_INIT);
        headers.add(CMD_JOINED);
        headers.add(CMD_LOGIN);
        headers.add(CMD_BROADCAST);
        headers.add(CMD_CONFIRM);
        headers.add(CMD_PRV);
        headers.add(CMD_LIST);
        headers.add(CMD_SRV);
        headers.add(CMD_QST);
        headers.add(CMD_ANSW);
        headers.add(CMD_SND);
        headers.add(CMD_RSL);
        headers.add(CMD_FILE_REJECT);
        headers.add(CMD_PBK);
        headers.add(CMD_ENCRYPTION_ASYMMETRIC);
        headers.add(CMD_ENCRYPTION_SYMMETRIC);
        headers.add(CMD_MSG);
        headers.add(CMD_FILE_CONFIRMATION);
        headers.add(CMD_FILE_TRANSFER);
        headers.add(CMD_UUID_FILE);
        headers.add(CMD_DISCONNECT);
        headers.add(CMD_PING);
        headers.add(CMD_PONG);
        headers.add(CMD_QUIT);

        return headers;
    }

    public static String getFAIL09() {
        return FAIL09;
    }

    public static String getFAIL08() {
        return FAIL08;
    }

    public static String getFAIL07() {
        return FAIL07;
    }

    public static String getFAIL06() {return FAIL06;}

    public static String getCMD_SND() {
        return CMD_SND;
    }
    public static String getCMD_JOINED() {
        return CMD_JOINED;
    }

    public static String getCMD_RSL() {
        return CMD_RSL;
    }

    public static String getCMD_PBK() {
        return CMD_PBK;
    }

    public static String getCMD_ENCRYPTION_SYMMETRIC() {
        return CMD_ENCRYPTION_SYMMETRIC;
    }

    public static String getCMD_MSG() {
        return CMD_MSG;
    }

    public static String getCMD_FILE_TRANSFER() {
        return CMD_FILE_TRANSFER;
    }

    public static String getCMD_FILE_CONFIRMATION() {
        return CMD_FILE_CONFIRMATION;
    }

    public static String getCMD_UUID_FILE() {
        return CMD_UUID_FILE;
    }

    public static String getCMD_BROADCAST() {
        return CMD_BROADCAST;
    }
    public static String getCMD_FILE_REJECT() {
        return CMD_FILE_REJECT;
    }

    public static String getCMD_CONFIRM() {
        return CMD_CONFIRM;
    }

    public static String getCMD_DISCONNECT() {
        return CMD_DISCONNECT;
    }

    public static String getCMD_INIT() {
        return CMD_INIT;
    }

    public static String getCMD_LOGIN() {
        return CMD_LOGIN;
    }

    public static String getFAIL00() {
        return FAIL00;
    }

    public static String getFAIL01() {
        return FAIL01;
    }

    public static String getFAIL02() {
        return FAIL02;
    }

    public static String getFAIL03() {
        return FAIL03;
    }

    public static String getFAIL04() {
        return FAIL04;
    }

    public static String getFAIL05() {
        return FAIL05;
    }
    public static String getCMD_LIST() {
        return CMD_LIST;
    }

    public static String getCMD_PRV() {
        return CMD_PRV;
    }

    public static String getCMD_SRV() {
        return CMD_SRV;
    }
}
