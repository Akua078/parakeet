package com.parakeet;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

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

    public static String getMessage(Session session, String id){

        GetItemRequest request = GetItemRequest.builder()
        .tableName(session.getMessagesTableName())
        .key(java.util.Map.of(
                "id", AttributeValue.builder()
                        .s(id)
                        .build()
        ))
        .build();

        // Retrieve the item from AWS
        var response = session.getClient().getItem(request);

        // Display its message
        if (response.hasItem()) {
            String message = response.item()
                    .get("message")
                    .s();

            return message;
        } else {
            return "No message found with id: " + id;
        }

    }

    public static void main(String[] args) {

        //Establish Session with AWS
        Session s = new Session("parakeet-dev", Region.US_WEST_2);

        //Test fetching items
        System.out.println(getMessage(s, "00000001"));
        System.out.println(getMessage(s, "00000002"));

        //Close session and end program
        s.close();
        
    }
}
