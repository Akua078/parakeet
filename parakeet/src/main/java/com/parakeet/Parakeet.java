//For Maven
package com.parakeet;

//Imports
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Parakeet {

    //Classes - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    //Session object class to keep one connection open, instead of using a new connection for every function call
    public static class Session {

        //Attributes
        private static Session instance;        //instance variable to enforce singleton design pattern
        private final String auth;
        private final String messagesTableName;
        private final String accountsTableName;

        //Accessors
        public String getAuth(){return auth;}
        public String getMessagesTableName(){return messagesTableName;}
        public String getAccountsTableName(){return accountsTableName;}

        //Constructor
        private Session(String auth){

            //Setup String identifiers
            this.auth = auth;
            this.messagesTableName = "parakeetMessages";
            this.accountsTableName = "";

        }

        public static Session getSession(){

            //Creating a new instance if none exists
            if(instance==null){
                instance = new Session("parakeet-admin");
            }

            //Returning instance that may or may not have already existed
            return instance;
        }

    }

    //Utility Methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    //Converts two given decimal values into their internal Message ID strings for quality of life
    //Message IDs are stored in hexadecimal strings, with the first eight characters representing the user ID, and the second four representing the message number (internal to that user)
    //In this way, every major message ID is unique, but each user can have a message number '0020' for their 20th message, for example
    public static String mID(int userID, int messageNumber){

        //Convert to hex string
        String userIDString = String.format("%08X", userID);
        String messageNumberString = String.format("%08X", userID);

        //Add a space every four characters
        for(int i=1; i<8; i++){
            if(i%4==0){
                userIDString = userIDString.substring(0,i) + " " + userIDString.substring(i);
                messageNumberString = messageNumberString.substring(0,i) + " " + messageNumberString.substring(i);
            }
        }

        //Return the two strings appended
        return userIDString.trim() + " " + messageNumberString.trim();
    }


    //Main Methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    //Check the table to see if a message with the given ID exists
    public static boolean messageIdExists(Session s, String id){
        return(!getMessage(s, id).substring(0,13).equals("[NO ID ERROR]"));
    }

    //Fetch and return the message with the given ID
    public static String getMessage(Session session, String id){
        return "TODO: Implement Message Fetching";
    }

    //Add a message to the database
    public static void sendMessage(Session session, String id, String message){

    }

    public static void main(String[] args) {

        /* //Establish Session
        Session s = Session.getSession();

        //Test fetching items
        System.out.println(getMessage(s, "0000 0000 0000 0000"));
        System.out.println(getMessage(s, "0000 0000 0000 0000"));
        System.out.println(getMessage(s, "0000 0000 0000 0000"));

        //Test sending items
        sendMessage(s, "0000 0000 0000 0000", "Moving to Mongo DB!"); 

        //Test to see if message IDs exist
        System.out.println("True:  " + messageIdExists(s, "0000 0001 0000 0001"));
        System.out.println("False: " + messageIdExists(s, "F000 0000 0000 0001")); */

        String uri = "mongodb+srv://akua078:rainier0529!@mydeployment.foxjxtk.mongodb.net";
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase db = mongoClient.getDatabase("parakeetMessages");
        MongoCollection<Document> collection = db.getCollection("parakeetMessages");
        Document doc = collection.find(eq("mID", mID(0,1))).first();
        if (doc != null) {
                System.out.println(doc.toString());
            } else {
                System.out.println("No matching documents found.");
            }

        System.out.println(mID(4,4));

        
    }
}
