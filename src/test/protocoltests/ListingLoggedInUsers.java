package protocoltests;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class ListingLoggedInUsers {
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
    void listingAllLoggedInUsersToUser1WithSuccess(){
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

        outUser1.println("LIST");
        receiveLineWithTimeout(inUser1);

        //
        String resIdent = receiveLineWithTimeout(inUser1);
        assertEquals("OK LIST user2,", resIdent);

    }
    @Test
    void notLoggedInUserRequestsAllTheLoggedInClientsReceivesError(){
        receiveLineWithTimeout(inUser1); //init message for the first user
        receiveLineWithTimeout(inUser2); //init message for the second user

        // Connect user1
        outUser1.println("IDENT user1");
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK


        outUser2.println("LIST");

        //
        String resIdent = receiveLineWithTimeout(inUser2);
        assertEquals("FAIL03 Please log in first", resIdent);

    }

    private String receiveLineWithTimeout(BufferedReader reader){
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), () -> reader.readLine());
    }
}
