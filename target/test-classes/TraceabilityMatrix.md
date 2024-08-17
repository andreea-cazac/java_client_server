# Traceability Matrix

| Testcases                                                  | RQ-U100 | RQ-U101 | RQ-U102 | RQ-U103 | RQ-S100 | RQ-S101 | RQ-S102 | ..TODO.. | RQ-U209 | ..TODO.. |
|------------------------------------------------------------|---------|---------|---------|---------|---------|---------|---------|----------|---------|----------|
| TC1_1_userNameWithThreeCharactersIsAccepted                | v       |         |         |         |         |         |         |          |         |          |
| TC1_2_userNameWithTwoCharactersReturnsError                | v       |         |         |         |         |         |         |          |         |          |
| TC1_3_userNameWith14CharectersIsAccepted                   | v       |         |         |         |         |         |         |          |         |          |
| TC1_4_userNameWith15CharectersReturnsError                 | v       |         |         |         |         |         |         |          |         |          |
| TC1_5_userNameWithBracketReturnsError                      | v       |         |         |         |         |         |         |          |         |          |
| TC2_1_identFollowedByBCSTWithWindowsLineEndingsReturnsOk   |         | v       |         |         |         |         |         |          |         |          |
| TC2_2_identFollowedByBCSTWithLinuxLineEndingsReturnsOk     |         | v       |         |         |         |         |         |          |         |          |
| TC2_3_identFollowedByBCSTWithSlashRLineEndingsReturnsOk    |         | v       |         |         |         |         |         |          |         |          |
| TC3_1_joinedIsReceivedByOtherUserWhenUserConnects          |         |         |         |         |         |         |         |          | x[1]    |          |
| TC3_2_bcstMessageIsReceivedByOtherConnectedClients         |         | x[1]    |         |         |         |         |         |          |         |          |
| TC3_3_identMessageWithAlreadyConnectedUsernameReturnsError | v       |         |         |         |         |         |         |          |         |          |
| TC4_1_identFollowedByBCSTWithMultipleFlushReturnsOk        |         | v       |         |         |         |         |         |          |         |          |
| TC5_1_initialConnectionToServerReturnsInitMessage          | v       |         |         |         |         |         |         |          |         |          |
| TC5_2_validIdentMessageReturnsOkMessage                    | v       |         |         |         |         |         |         |          |         |          |
| TC5_3_emptyIdentMessageReturnsErrorMessage                 | v       |         |         |         |         |         |         |          |         |          |
| TC5_4_invalidIdentMessageReturnsErrorMessage               | v       |         |         |         |         |         |         |          |         |          |
| TC5_5_pongWithoutPingReturnsErrorMessage                   |         |         |         |         |         |         | v       |          |         |          |
| TC5_6_logInTwiceReturnsErrorMessage                        | v       |         |         |         |         |         |         |          |         |          |
| TC5_7_pingIsReceivedAtExpectedTime                         |         |         |         |         |         |         | v       |          |         |          |

# Notes
[1] These testcases only work when you implement your own server in level 2. Rest of the testcases and requirements can be added to this table.