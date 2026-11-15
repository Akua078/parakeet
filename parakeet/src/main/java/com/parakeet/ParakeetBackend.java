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

public class ParakeetBackend {

    //Classes - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /* 
    
    Session class
    
    */

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
            Document doc = findMessage(uID, mID);
            return doc!=null;
        }

        //Check the collection to see if a user with the given ID exists
        public boolean userExists(int id){
            Document doc = this.getUsersDB().find(eq("uID", id)).first();
            return doc!=null;
        }

        //Find a message and return it, if it exists. Returns null otherwise
        public Document findMessage(int uID, int mID){

            //Setup an object to serve as our search criteria (In this case, uID and mID)
            BasicDBObject ids = new BasicDBObject();
            ids.append("mID",mID);
            ids.append("uID",uID);

            //Return the result (null if none is found)
            return this.getMessagesDB().find(ids).first();
        }

        //Fetch a message from the messages database with the given uID and mIDs. 
        public String getMessage(int uID, int mID){
            
            //Setup default output (if no message is found for the criteria, this is returned)
            String outString = "No Message Found for ID:[uID,mID]: " + uID + " " + mID;

            //Fetch the message from the database, if it exists. 
            //If the message does not exist, doc will be null
            Document doc = findMessage(uID, mID);

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

        //Delete a message with a given mID and uID
        //NOTE: using this function does not alter the mIDs of the other messages sent by the given user, 
        //  and this is intentional, as it would corrupt the data of chatrooms.
        //mIDs are used for reference only and not for counting messages, thats what getUserMsgCount() is for. 
        public boolean deleteMessage(int uID, int mID){
            Document doc = findMessage(uID, mID);
            if(doc!=null){this.getMessagesDB().deleteOne(doc);}
            return doc!=null;
        }

        //Manually Check the number of messages a user has sent, given that user's id
        public int getUserMsgCount(int uID){
            return (int) this.getMessagesDB().countDocuments(eq("uID",uID));
        }

    }


    /* 
    
    Chatroom class
    
    */

    //Chatroom class to facilitate processing teir logic
    public class Chatroom{

        int roomID;             //key
        int[] userList;         //Stores recipients of these messages
        int[][] messageList;    //Stores the mIDs of the messages and the uIDs of the senders [mID, uID]

        private Chatroom(){
            
        }


    }

    /* 
    
    User class
    
    */

    //Chatroom class to facilitate processing teir logic
    public class User{

        int uID;    //User ID

        private User(int uID){
            this.uID = uID;
        }


    }

    /* 
    
    Main
    
    */

    //Main function (For testing; Outputs to console only)
    public static void main(String[] args) {

        //Establish Session
        Session s = Session.getSession();

        //Testing getMessage()
        //System.out.println(s.getMessage(78, 1));
        //System.out.println(s.getMessage(52, 1));
        //System.out.println(s.getMessage(67, 1));

        //Testing sendMessage()
        //s.sendMessage(78, "Akua078 sent a second message"); 
        //s.sendMessage(78, "This message is about to be deleted"); 
        //s.sendMessage(13, "No one sent this message"); 

        //Testing userExists()
        //System.out.println(s.userExists(78));
        //System.out.println(s.userExists(0));
        //System.out.println(s.userExists(52));

        //Testing message deletion
        s.deleteMessage(78, 7);


        
    }
}
