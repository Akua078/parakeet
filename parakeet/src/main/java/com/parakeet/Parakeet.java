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
        private static Session instance;                    //instance variable to enforce singleton design pattern
        private final String auth;                          //this session's authorization level (dependant on user)
        private final MongoClient client;                   //Mongo Client
        private final MongoDatabase database;               //Mongo Parakeet Database
        private final MongoCollection<Document> messageDB;  //Message Database
        private final MongoCollection<Document> usersDB;    //User Database

        //Accessors
        public String getAuth(){return auth;}
        public MongoClient getClient(){return client;}
        public MongoDatabase getDB(){return database;}
        public MongoCollection<Document> getMessageDB(){return messageDB;}
        public MongoCollection<Document> getUsersDB(){return usersDB;}

        //Constructor
        private Session(String auth){

            //Setup String identifiers
            this.auth = auth;

            //Mongo setup
            this.client = MongoClients.create("mongodb+srv://akua078:rainier0529!@mydeployment.foxjxtk.mongodb.net");
            this.database = this.client.getDatabase("parakeetMessages");
            this.messageDB = this.database.getCollection("parakeetMessages");
            this.usersDB = this.database.getCollection("parakeetUsers");

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
        String messageNumberString = String.format("%08X", messageNumber);

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


    //Message Facilitation Methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    //Check the table to see if a message with the given ID exists
    public static boolean messageIdExists(Session s, String id){
        Document doc = s.getMessageDB().find(eq("mID", id)).first();
        return doc!=null;
    }

    //Fetch and return the message with the given ID
    public static String getMessage(Session s, String id){
        String outString = "No Message Found for ID: " + id;

        Document doc = s.getMessageDB().find(eq("mID", id)).first();

        if(doc!=null){
            outString = doc.toString().substring(69,doc.toString().length()-2);
        }

        return outString;
    }

    //Add a message to the database
    public static void sendMessage(Session s, int uID, String msg){
        String id = mID(uID, getUserMsgCount(s,uID)+1);
        Document message = new Document("mID",id).append("msg",msg);
        if(!messageIdExists(s,id)){s.getMessageDB().insertOne(message);}
        else{System.out.println("Message ID: [" + id + "] Already Exists!");}
    }

    //Manually Check the number of messages a user has sent, given that user's id
    public static int getUserMsgCount(Session s, int uID){
        int count = 0;
        while(true){
            System.out.println("TESTING: " + mID(Integer.valueOf(uID),count+1));
            if(!messageIdExists(s, mID(Integer.valueOf(uID),count+1))){break;}
            count++;
        }
        return count;
    }

    public static void main(String[] args) {

        //Establish Session
        Session s = Session.getSession();

        //Test fetching items
        //System.out.println(getMessage(s, "0000 0000 0000 0000"));
        //System.out.println(getMessage(s, "0000 0000 0000 0001"));
        //System.out.println(getMessage(s, "0000 0000 0000 0003"));

        //Test sending items
        //sendMessage(s, 78, "Akua078 sent this message"); 

        //Test to see if message IDs exist
        //System.out.println("True:  " + messageIdExists(s, "0000 0000 0000 0001"));
        //System.out.println("False: " + messageIdExists(s, "F000 0000 0000 0001"));

        //Testing User Message Count
        int testUID = 0;
        //sendMessage(s, testUID, "User " + testUID + " Speaking"); 
        //System.out.println("User: [" + String.format("%08X", testUID) + "] ; " + getUserMsgCount(s, testUID));

        //sendMessage(s, testUID, "User " + testUID + " Speaking Again"); 
        System.out.println("User: [" + String.format("%08X", testUID) + "] ; " + getUserMsgCount(s, testUID));

        
    }
}
