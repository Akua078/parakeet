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
            this.database = this.client.getDatabase("parakeetDatabase");
            this.messageDB = this.database.getCollection("parakeetMessages");
            this.usersDB = this.database.getCollection("parakeetUsers");

        }

        //Singleton structured instance accessor 
        public static Session getSession(){

            //Creating a new instance if none exists
            if(instance==null){instance = new Session("parakeet-admin");}

            //Returning instance that may or may not have already existed
            return instance;
        }

        //Check the collection to see if a message with the given ID exists
        public boolean messageIdExists(int id){
            Document doc = this.getMessageDB().find(eq("mID", id)).first();
            return doc!=null;
        }

        //Check the collection to see if a user with the given ID exists
        public boolean userExists(int id){
            Document doc = this.getUsersDB().find(eq("uID", id)).first();
            return doc!=null;
        }

        public String getMessage(int uID, int mID){

            BasicDBObject criteria = new BasicDBObject();
            criteria.append("mID",mID);
            criteria.append("uID",uID);
            
            String outString = "No Message Found for [uID,mID]: " + uID + " " + mID;

            Document doc = this.getMessageDB().find(criteria).first();

            if(doc!=null){
                outString = doc.toString();
                outString = outString.substring(outString.indexOf("msg=")+4,outString.indexOf(", uID="));
            }

            return outString;
        }

        //Add a message to the database
        public void sendMessage(int uID, String msg){
            Document message = new Document("mID",getUserMsgCount(uID)+1).append("msg",msg).append("uID",uID);
            if(this.userExists(uID)){this.getMessageDB().insertOne(message);}
        }

        //Manually Check the number of messages a user has sent, given that user's id
        public int getUserMsgCount(int uID){
            return (int) this.getMessageDB().countDocuments(eq("uID",uID));
        }

    }

    //Message Facilitation Methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static void main(String[] args) {

        //Establish Session
        Session s = Session.getSession();

        //Testing getMessage()
        System.out.println(s.getMessage(78, 1));
        System.out.println(s.getMessage(52, 1));
        System.out.println(s.getMessage(67, 1));

        //Testing sendMessage()
        s.sendMessage(78, "Akua078 sent a second message"); 
        s.sendMessage(52, "User 52 sent a second message"); 
        s.sendMessage(13, "No one sent this message"); 

        //Testing userExists()
        System.out.println(s.userExists(78));
        System.out.println(s.userExists(0));
        System.out.println(s.userExists(52));

        
    }
}
