# Java Chat Application

## Overview
This educational project is a Java-based chat system between multiple clients. The server manages user connections, handles logins, and ensures message delivery.

## Client-Server Communication
- **Protocol(own):** TCP/IP for reliable communication.
- **Technology:** Java sockets for connection management.

## Testcases
The project is supported by JUnit tests. They can be found in the usual test directory.

## Protocol Overview
The protocol describes the following scenarios:

- Establishing a connection between client and server.
- Broadcasting messages to all connected clients.
- Sending heartbeat messages to check client activity.
- Handling client disconnection.
- Managing invalid messages.
- Displaying a list of all connected users.
- Sending private and broadcast messages.
- Creating and sending surveys.
- Sending files.
- Sending encrypted messages. (RSA and AES)

**Note:** Most messages between the server and clients that include usernames must be separated by a `:` character.

## Protocol Details

### 1. Establish Connection
- **Client:** Sets up a socket connection.
- **Server:** Responds with a welcome message.
- **Client:** Supplies a username.
- **Server:** Responds with `OK` if the username is accepted, or an error code if not.
  
  **Happy Flow:**
  - **Client:** `INIT`
  - **Server:** `INIT <welcome message>`
  - **Client:** `IDENT <username>`
  - **Server:** `OK IDENT <username>`
  - **Server (to other clients):** `JOINED <username>`
  
  **Error Messages:**
  - `FAIL01 User already logged in`
  - `FAIL02 Username has an invalid format or length`
  - `FAIL04 User cannot login twice`

### 2. Broadcast Message
- **Client:** Sends a message to all other clients.
- **Server:** Confirms receipt and sends the message to other clients.
  
  **Happy Flow:**
  - **Client:** `BCST <message>`
  - **Server:** `OK BCST <message>`
  - **Server (to other clients):** `BCST <username>:<message>`
  
  **Error Messages:**
  - `FAIL03 Please log in first`

### 3. Heartbeat Message
- **Server:** Sends periodic `PING` messages to check if clients are active.
- **Client:** Responds with `PONG` to confirm activity.
  
  **Happy Flow:**
  - **Server:** `PING`
  - **Client:** `PONG`
  
  **Error Messages:**
  - `DSCN Pong timeout Server disconnects the client.`
  - `FAIL05 Pong without ping`

### 4. Terminate Connection
- **Client:** Sends a quit message to terminate the connection.
- **Server:** Responds with an acknowledgment and closes the socket.
  
  **Happy Flow:**
  - **Client:** `QUIT`
  - **Server:** `OK Goodbye`

### 5. Receive a List of All Connected Users
- **Client:** Requests a list of all connected users.
- **Server:** Sends a comma-separated list of usernames, excluding the requester.
  
  **Happy Flow:**
  - **Client:** `LIST`
  - **Server:** `OK LIST <USER01>,<USER03>,`
  
  **Error Messages:**
  - `FAIL03 Please log in first`

### 6. Sending a Private Message
- **Client:** Sends a message to a specific user.
- **Server:** Sends the message to the recipient and confirms the action.
  
  **Happy Flow:**
  - **Client:** `PRV <username1>:<message>`
  - **Server:** `OK PRV`
  - **Server (to recipient):** `PRV <username1>:<message>`
  
  **Error Messages:**
  - `FAIL03 Please log in first`
  - `FAIL07 You must specify the requested details fully`
  - `FAIL08 No other clients than you are logged in`
  - `FAIL09 Username not found!`

### 7. Creating and Sending Survey
- **Client:** Requests to create a survey.
- **Server:** Confirms if there are at least 3 users connected.
- **Client:** Sends survey questions and answers.
- **Server:** Distributes the survey.
  
  **Happy Flow:**
  - **Client:** `SRV`
  - **Server:** `OK SRV`
  - **Client:** `QST <USER01>;<USER02>%<question1>;<answerA>,<answerB>,<answerC>/`
  - **Server:** `OK QST`
  
  **Error Messages:**
  - `FAIL03 Please log in first`
  - `FAIL06 Less than 3 users connected`

### 8. Sending and Executing the Survey
- **Server:** Sends survey questions and answers.
- **Client:** Sends back survey responses.
- **Server:** Collects responses and sends a summary after 5 minutes or when all users have responded.
  
  **Happy Flow:**
  - **Server:** `SND <surveyID>%<question1>;<answerA>;<answerB>;`
  - **Client:** `ANS <surveyID>#<answerB>%<answerB>%`
  - **Server:** `RSL <amountUsersParticipated>%<question1>;<answerA>,<answerB>,<answerC>,1,2,0/`
  
### 9. Encryption
- **Client:** Requests to send an encrypted message by providing the recipient’s public RSA key.
- **Server:** Facilitates the exchange of public and AES keys.
- **Client:** Sends encrypted messages.
  
  **Happy Flow:**
  - **Client1:** `EAS <username2>:<publicRSAKey1>`
  - **Server:** `PBK <username1>:<publicRSAKey1>`
  - **Client2:** `ESI <username1>:<encryptedAESKey>`
  - **Server:** `ESI <username2>:<encryptedAESKey>`
  - **Client1:** `MSG <username2>:<encryptedMessage>`
  - **Server:** `MSG <username2>:<encryptedMessage>`
  
  **Error Messages:**
  - `FAIL03 Please log in first`
  - `FAIL07 You must specify the requested details fully`
  - `FAIL09 Username not found!`

### 10. File Transfer
- **Client:** Requests a file transfer.
- **Server:** Forwards the request and handles the response.
- **Client:** Starts file transfer upon confirmation.
  
  **Happy Flow:**
  - **Client1:** `FCM <username2>:<fileName>:<fileSize>`
  - **Server:** `FCM <username1>:<fileName>:<fileSize>`
  - **Client2:** `FCN <response>`
  - **Server:** `OK FCN <response>:<uuid>`
  - **Server:** `UUID <uuid>`
  
### 11. File Uploading and Downloading from a Separate Thread
- **Thread1 & Thread2:** Handle file upload and download.
  
  **Happy Path:**
  - **Thread1:** `LOG <role>:<uuid>`
  - **Thread2:** `LOG <role>:<uuid>`
  - **Thread:** Uploads and downloads files using UUIDs.
  
  **Error Messages:**
  - `FAIL03 Please log in first`
  - `FAIL07 You must specify the requested details fully`

### 12. Invalid Messages
- **Server:** Responds to unknown commands with an error.
  
  **Example Flow:**
  - **Client:** `MSG This is an invalid message`
  - **Server:** `FAIL00 Unknown command`
 
## Feedback and Contributions

We welcome your feedback and ideas to improve this project! If you have suggestions for new features, improvements, or if you encounter any issues, please let us know. You can contribute by:

- Opening an issue on the [GitHub repository](https://github.com/andreea-cazac/java_client_server).
- Submitting a pull request with your improvements or bug fixes.
- Sharing your thoughts or ideas for new games or enhancements.

Your contributions help make this project better for everyone. Thank you for being a part of this educational adventure!
