//For Maven
package com.parakeet;

//AWS Imports
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class Parakeet {

    //Session object class to keep one connection open, instead of using a new connection to AWS for every function call
    public static class Session {

        //Attributes
        private final String auth;
        private final String messagesTableName;
        private final String accountsTableName;
        private final DynamoDbClient client;

        //Accessors
        public String getAuth(){return auth;}
        public String getMessagesTableName(){return messagesTableName;}
        public String getAccountsTableName(){return accountsTableName;}
        public DynamoDbClient getClient(){return client;}

        //Close Session
        public void close(){client.close();}

        //Constructors
        public Session(String auth, Region region){

            //Setup String identifiers
            this.auth = auth;
            this.messagesTableName = "parakeetMessages";
            this.accountsTableName = "";
            
            //Setup region and client
            // Connect to DynamoDB in Oregon (us-west-2)
            this.client = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create(auth))
                .build();
        }

    }

    public static boolean messageIdExists(Session s, String id){
        return(!getMessage(s, id).substring(0,13).equals("[NO ID ERROR]"));
    }

    public static String getMessage(Session session, String id){

        //Begin Request
        GetItemRequest request = GetItemRequest.builder()                               //Get Item
        .tableName(session.getMessagesTableName())                                      //From table
        .key(java.util.Map.of("id", AttributeValue.builder().s(id).build())).build();   //With given ID

        // Retrieve the item from AWS
        var response = session.getClient().getItem(request);

        // Display its message if it has one, alternate message otherwise
        if (response.hasItem()) {
            return response.item().get("message").s();
        } else {
            return "[NO ID ERROR] - No message found with id: [" + id + "]";
        }

    }

    public static void sendMessage(Session session, String id, String message){

        session.getClient()                                         //Connect to DynamoDB
            .putItem(PutItemRequest.builder()                       //Send the Message
            .tableName(session.getMessagesTableName())              //Into the messages table
            .item(java.util.Map.of("id", AttributeValue.builder()   //At this ID
            .s(id).build(),"message", AttributeValue.builder()      //With this message
            .s(message).build())).build());                         //(send)
    }

    public static void main(String[] args) {

        //Establish Session with AWS
        Session s = new Session("parakeet-dev", Region.US_WEST_2);

        //Test fetching items
        System.out.println(getMessage(s, "00000001"));
        System.out.println(getMessage(s, "00000002"));
        System.out.println(getMessage(s, "00000003"));

        //Test sending items
        //sendMessage(s, "00000003", "This is a third message I sent from my application!"); 

        //Test to see if message IDs exist
        System.out.println("True:  " + messageIdExists(s, "00000002"));
        System.out.println("False: " + messageIdExists(s, "00000007"));

        //Close session and end program
        s.close();
        
    }
}
