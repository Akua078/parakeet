//For Maven
package com.parakeet;

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
        private static Session instance;                      //instance variable to enforce singleton design pattern
        private final String auth;                            //this session's authorization level (dependant on user)
        private final MongoClient client;                     //Mongo Client
        private final MongoDatabase database;                 //Mongo Parakeet Database
        private final MongoCollection<Document> messagesDB;   //Message Database
        private final MongoCollection<Document> usersDB;      //User Database
        private final MongoCollection<Document> chatroomsDB;  //Chatroom Database

        //Accessors
        public String getAuth(){return auth;}
        public MongoClient getClient(){return client;}
        public MongoDatabase getDB(){return database;}
        public MongoCollection<Document> getMessagesDB(){return messagesDB;}
        public MongoCollection<Document> getUsersDB(){return usersDB;}
        public MongoCollection<Document> getChatroomsDB(){return chatroomsDB;}

        //Constructor
        private Session(String auth){

            //Setup String identifiers
            this.auth = auth;

            //Mongo setup
            this.client = MongoClients.create("mongodb+srv://akua078:rainier0529!@mydeployment.foxjxtk.mongodb.net");
            this.database = this.client.getDatabase("parakeetDatabase");
            this.messagesDB = this.database.getCollection("parakeetMessages");
            this.usersDB = this.database.getCollection("parakeetUsers");
            this.chatroomsDB = this.database.getCollection("parakeetChatrooms");

        }

        //Singleton structured instance accessor 
        public static Session getSession(){

            //Creating a new instance if none exists
            if(instance==null){instance = new Session("parakeet-admin");}

            //Returning instance that may or may not have already existed
            return instance;
        }

        //Check the collection to see if a message with the given ID exists
        public boolean messageIdExists(int uID, int mID){
            BasicDBObject criteria = new BasicDBObject();
            criteria.append("mID",mID);
            criteria.append("uID",uID);
            Document doc = this.getMessagesDB().find(criteria).first();
            return doc!=null;
        }

        //Check the collection to see if a user with the given ID exists
        public boolean userExists(int id){
            Document doc = this.getUsersDB().find(eq("uID", id)).first();
            return doc!=null;
        }

        //Fetch a message from the messages database with the given uID and mIDs. 
        public String getMessage(int uID, int mID){

            //Setup criteria object and append both uID and mID
            // so our message must match both
            BasicDBObject criteria = new BasicDBObject();
            criteria.append("mID",mID);
            criteria.append("uID",uID);
            
            //Setup default output (if no message is found for the criteria, this is returned)
            String outString = "No Message Found for ID:[uID,mID]: " + uID + " " + mID;

            //Fetch the message from the database, if it exists. 
            //If the message does not exist, doc will be null
            Document doc = this.getMessagesDB().find(criteria).first();

            //If we found the message, extract the 'msg' component and assign outString to that
            if(doc!=null){
                outString = doc.toString();
                outString = outString.substring(outString.indexOf("msg=")+4,outString.indexOf(", uID="));
            }

            //Return outString
            return outString;
        }

        //Add a message to the database
        public boolean sendMessage(int uID, String msg){
            //If a user with the given uID exists
            if(this.userExists(uID)){
                //Set up a message object with the given uID and message
                //Determine the user's message count, then add one to get a new mID
                Document message = new Document("mID",getUserMsgCount(uID)+1).append("msg",msg).append("uID",uID);
                //Insert the message to the messages DB
                this.getMessagesDB().insertOne(message);
                return true;
            }
            return false;
        }

        //Manually Check the number of messages a user has sent, given that user's id
        public int getUserMsgCount(int uID){
            return (int) this.getMessagesDB().countDocuments(eq("uID",uID));
        }

    }

    //Chatroom class to facilitate processing teir logic
    public class Chatroom{

        int roomID;
        int[] userList;
        int[][] messageList;

        private Chatroom(){

        }


    }

    public static void main(String[] args) {

        //Establish Session
        Session s = Session.getSession();

        //Testing getMessage()
        System.out.println(s.getMessage(78, 1));
        System.out.println(s.getMessage(52, 1));
        System.out.println(s.getMessage(67, 1));

        //Testing sendMessage()
        //s.sendMessage(78, "Akua078 sent a second message"); 
        //s.sendMessage(52, "User 52 sent a second message"); 
        //s.sendMessage(13, "No one sent this message"); 

        //Testing userExists()
        System.out.println(s.userExists(78));
        System.out.println(s.userExists(0));
        System.out.println(s.userExists(52));

        
    }
}
