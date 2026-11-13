//For Maven
package com.parakeet;

import static com.mongodb.client.model.Filters.all;
//Imports
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

import com.mongodb.BasicDBObject;
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

    //Message Facilitation Methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    //Check the collection to see if a message with the given ID exists
    public static boolean messageIdExists(Session s, int id){
        Document doc = s.getMessageDB().find(eq("mID", id)).first();
        return doc!=null;
    }

    //Check the collection to see if a user with the given ID exists
    public static boolean userExists(Session s, int id){
        Document doc = s.getUsersDB().find(eq("uID", id)).first();
        return doc!=null;
    }

    //Fetch and return the message with the given ID
    public static String getMessage(Session s, int uID, int mID){

        BasicDBObject criteria = new BasicDBObject();
        criteria.append("mID",mID);
        criteria.append("uID",uID);
        
        String outString = "No Message Found for [uID,mID]: " + uID + " " + mID;

        Document doc = s.getMessageDB().find(criteria).first();

        if(doc!=null){
            outString = doc.toString();
            outString = outString.substring(outString.indexOf("msg=")+4,outString.indexOf(", uID="));
        }

        return outString;
    }

    //Add a message to the database
    public static void sendMessage(Session s, int uID, String msg){
        Document message = new Document("mID",getUserMsgCount(s, uID)+1).append("msg",msg).append("uID",uID);
        if(userExists(s, uID)){s.getMessageDB().insertOne(message);}
    }

    //Manually Check the number of messages a user has sent, given that user's id
    public static int getUserMsgCount(Session s, int uID){
        return (int) s.getMessageDB().countDocuments(eq("uID",uID));
    }

    public static void main(String[] args) {

        //Establish Session
        Session s = Session.getSession();

        //Test fetching items
        System.out.println(getMessage(s, 78, 1));
        System.out.println(getMessage(s, 52, 1));
        System.out.println(getMessage(s, 67, 1));

        //Test sending items
        //sendMessage(s, 78, "Akua078 sent a THIRD message"); 
        sendMessage(s, 3242, "User 52 sent a second message"); 
        //sendMessage(s, 13, "No one sent this message"); 

        //Test to see if message IDs exist
        //System.out.println("True:  " + messageIdExists(s, "0000 0000 0000 0001"));
        //System.out.println("False: " + messageIdExists(s, "F000 0000 0000 0001"));

        //Testing User Message Count
        //int testUID = 0;
        //sendMessage(s, testUID, "User " + testUID + " Speaking"); 
        //System.out.println("User: [" + String.format("%08X", testUID) + "] ; " + getUserMsgCount(s, testUID));

        //sendMessage(s, testUID, "User " + testUID + " Speaking Again"); 
        //System.out.println("User: [" + String.format("%08X", testUID) + "] ; " + getUserMsgCount(s, testUID));

        System.out.println(userExists(s, 78));
        System.out.println(userExists(s, 0));
        System.out.println(userExists(s, 52));

        
    }
}
