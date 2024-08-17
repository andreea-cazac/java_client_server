package protocoltests;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class PrivateMessages {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = MultipleUserTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        outUser1.println("QUIT");
        outUser1.flush();
        outUser2.println("QUIT");
        outUser2.flush();
    }

    @Test
    void privateMessageIsReceivedByUser2FromUser1(){
        receiveLineWithTimeout(inUser1); //init message for the first user
        receiveLineWithTimeout(inUser2); //init message for the second user

        // Connect user1
        outUser1.println("IDENT user1");
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println("IDENT user2");
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        outUser1.println("PRV user2:hello");
        receiveLineWithTimeout(inUser1); //OK

        //Private message "hello" is received by user2 from user1
        String resIdent = receiveLineWithTimeout(inUser2);
        assertEquals("PRV user1:hello", resIdent);
    }
    @Test
    void usernameNotSpecifiedWhileSendingAPrivateMessageReceivesError(){
        receiveLineWithTimeout(inUser1); //init message for the first user
        receiveLineWithTimeout(inUser2); //init message for the second user

        // Connect user1
        outUser1.println("IDENT user1");
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println("IDENT user2");
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1);

        outUser1.println("PRV :hello");

        //Private message "hello" is received by user2 from user1
        String resIdent = receiveLineWithTimeout(inUser1);
        assertEquals("FAIL07 You must specify the requested details fully!", resIdent);

    }
    @Test
    void usernameNotFoundWhileSendingAPrivateMessageReceivesError(){
        receiveLineWithTimeout(inUser1); //init message for the first user
        receiveLineWithTimeout(inUser2); //init message for the second user

        // Connect user1
        outUser1.println("IDENT user1");
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println("IDENT user2");
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1);

        outUser1.println("PRV user3:hello");

        //Private message "hello" is received by user2 from user1
        String resIdent = receiveLineWithTimeout(inUser1);
        assertEquals("FAIL09 User was not found in the list", resIdent);

    }

    @Test
    void notLoggedInUserSendsPrivateMessageReceivesError(){
        receiveLineWithTimeout(inUser1); //init message for the first user
        receiveLineWithTimeout(inUser2); //init message for the second user

        // Connect user1
        outUser1.println("IDENT user1");
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        outUser2.println("PRV user1:hello");

        //Private message "hello" is received by user2 from user1
        String resIdent = receiveLineWithTimeout(inUser2);
        assertEquals("FAIL03 Please log in first", resIdent);

    }
    @Test
    void theOnlyOneLoggedUserSendsPrivateMessageReceivesError(){
        receiveLineWithTimeout(inUser1); //init message for the first user
        receiveLineWithTimeout(inUser2); //init message for the second user

        // Connect user1
        outUser1.println("IDENT user1");
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        outUser1.println("PRV user2:hello");

        //Private message "hello" is received by user2 from user1
        String resIdent = receiveLineWithTimeout(inUser1);
        assertEquals("FAIL08 You are the only one logged in, wait for others to connect", resIdent);

    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }
}
