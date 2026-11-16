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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;


public class ParakeetBackend {

    //Classes - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /* 
    
    Session class
    
    */

    //Session object class to keep one connection open, instead of using a new connection for every function call
    //Utilizes a Singleton class implementation
    public static class Session {

        //Attributes
        private static Session instance;                      //instance variable to enforce singleton design pattern
        private final User user;                              //The user logged into this session
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
        private Session(){

            //Mongo setup
            this.client = MongoClients.create("mongodb+srv://akua078:rainier0529!@mydeployment.foxjxtk.mongodb.net");
            this.database = this.client.getDatabase("parakeetDatabase");
            this.messagesDB = this.database.getCollection("parakeetMessages");
            this.usersDB = this.database.getCollection("parakeetUsers");
            this.chatroomsDB = this.database.getCollection("parakeetChatrooms");

            //Setup User
            this.user = User.getUser();
            this.auth = this.user.getAuth();

        }

        //Singleton structured instance accessor 
        public static Session getSession(){

            //Creating a new instance if none exists
            if(instance==null){instance = new Session();}

            //Returning instance that may or may not have already existed
            return instance;
        }

        //Check the collection to see if a message with the given ID exists
        public boolean messageIdExists(int uID, int mID){
            Document doc = findMessage(uID, mID);
            return doc!=null;
        }

        //Check the collection to see if a user with the given ID exists
        //(Overloaded to check with either uID or Username)
        public boolean userExists(int id){
            Document doc = this.getUsersDB().find(eq("uID", id)).first();
            return doc!=null;
        }
        public boolean userExists(String username){
            Document doc = this.getUsersDB().find(eq("username", username)).first();
            return doc!=null;
        }

        //Find a message and return it, if it exists. Returns null otherwise
        public Document findMessage(int uID, int mID){

            //Setup an object to serve as our search criteria (In this case, uID and mID)
            BasicDBObject criteria = new BasicDBObject();
            criteria.append("mID",mID);
            criteria.append("uID",uID);

            //Return the result (null if none is found)
            return this.getMessagesDB().find(criteria).first();
        }

        //Find a user and return it, if it exists. Returns null otherwise
        //Overloaded to allow searching by uID or Username
        public Document findUser(int uID){

            //Setup an object to serve as our search criteria (In this case, uID and mID)
            BasicDBObject criteria = new BasicDBObject();
            criteria.append("uID",uID);

            //Return the result (null if none is found)
            return this.getMessagesDB().find(criteria).first();
        }

        //Find a user and return it, if it exists. Returns null otherwise
        public Document findUser(String username){

            //Setup an object to serve as our search criteria (In this case, uID and mID)
            BasicDBObject criteria = new BasicDBObject();
            criteria.append("username",username);

            //Return the result (null if none is found)
            return this.getMessagesDB().find(criteria).first();
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

        //Returns the mID of the latest message sent by a given user
        //(finds the greatest mID for a given user; utilized in sending a new message to ensure a unique ID)
        public int getMaxMID(int uID){
            int max = 0;
            if(this.userExists(uID)){
                for(int i = (int)this.getMessagesDB().countDocuments(); i>0; i--){
                    if(this.messageIdExists(uID, i)){
                        String mStr = findMessage(uID, i).toString();
                        int foundUID = Integer.valueOf(mStr.substring(mStr.indexOf("mID=")+4,mStr.indexOf(", msg")));
                        //System.out.println(mStr.substring(mStr.indexOf("mID=")+4,mStr.indexOf(", msg")));
                        max = (foundUID < max) ? max : foundUID;
                    }
                }
            }
            else{return -1;};
            return max;
        }

        //Add a message to the database
        public boolean sendMessage(int uID, String msg){
            //If a user with the given uID exists
            if(this.userExists(uID)){

                int newMsgID = getMaxMID(uID)+1;

                //Set up a message object with the given uID and message
                Document message = new Document("mID",newMsgID).append("msg",msg).append("uID",uID);

                //Insert the message to the messages DB and indicate success 
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

        //Delete a User from the database (Could cause lots of issues, so use with caution!)
        //Only permitted to be called by users with higher authorizations!
        //Overloaded to allow for calls passing the username or the uID
        public boolean deleteUser(int uID){
            Document doc = findUser(uID);
            if(doc!=null){this.getUsersDB().deleteOne(doc);}
            return doc!=null;
        }

        //Creates a new user and assigns default authorization
        //Users must upgrade authorization using other functions
        public boolean createUser(String username, String password){
            if(!this.userExists(username)){

                //Get the minimum valid uID from those not taken (reserving the first 100 for development testing)
                //Stops before it hits Int Max so it doesn't crash and also doesn't go forever
                int newUID = 100;
                while(newUID < Integer.MAX_VALUE/2){
                    if(!this.userExists(newUID)){
                        this.getUsersDB().insertOne(
                            new Document("auth","standard")
                            .append("uID", newUID)
                            .append("password", password)
                            .append("username", username)
                        );
                    }
                    else{newUID++;}
                }

                //Create a new user document and send it into the user database
                return true; //New user is created, no issues
            }
            return false; //Username is taken, so we will not make a new user
        }
        //Overloaded createUser() function to specify a uID instead of automatically assigning one
        public boolean createUser(String username, String password, int uID){
            if(!this.userExists(username) && !this.userExists(uID)){

                //Create a new user document and send it into the user database
                this.getUsersDB().insertOne(
                    new Document("auth","standard")
                    .append("uID", uID)
                    .append("password", password)
                    .append("username", username)
                );

                return true; //New user is created, no issues
            }
            return false; //Username or uID is taken, so we will not make a new user
        }

    }


    /* 
    
    Chatroom class
    
    */

    //Chatroom class to facilitate processing tier logic
    public class Chatroom{

        int roomID;             //key
        int[] userList;         //Stores recipients of these messages
        int[][] messageList;    //Stores the mIDs of the messages and the uIDs of the senders [uID, mID]

        private Chatroom(){
            
        }


    }

    /* 
    
    User class
    
    */

    //User class to define the user of this session
    //The User class is also a singleton
    public static class User{

        private static User instance;       //Instance to enforce Singleton design pattern 
        private int uID;                    //User ID
        private String auth;                //Authorization level
        private String username;            //Username

        //Default Constructor (private)
        private User(){
            this.uID = 0;
            this.auth = "anon";
        }

        //Singleton structured instance accessor 
        //This function sets up a user to serve as a default user, if no login has occured. 
        //These attributes are updated upon login. 
        public static User getUser(){

            //Creating a new instance if none exists
            if(instance==null){instance = new User();}

            //Returning instance that may or may not have already existed
            return instance;
        }

        //Accessors
        public int getUID(){return this.uID;}
        public String getAuth(){return this.auth;}
        public String getUN(){return this.username;}

        //Mutators
        public boolean setUN(String newUsername){
            Session s = Session.getSession();
            if(!s.userExists(newUsername)){
                this.username = newUsername;
                s.getUsersDB().updateOne(Filters.eq("uID",this.uID),Updates.set("username", newUsername));
            }
            return false;
        }

        //Promotes the user by one auth level, but not to developer
        public boolean promote(){
            switch (this.getAuth()){
                case "anon":
                    this.auth = "standard";
                    break;
                case "standard": 
                    this.auth = "admin";
                    break;
                case "dev":
                    break;
                default:
                    this.auth = "anon";
                 
            }
            return false;
        }

    }

    /* 
    
    Main
    
    */

    //Main function (For testing; Outputs to console only)
    public static void main(String[] args) {

        //Establish Session
        Session s = Session.getSession();
        System.out.println("\nMAIN - - - - - - - - - - - - - - - - - - - - - -\n");

        //Testing getMessage()
        //System.out.println(s.getMessage(78, 1));
        //System.out.println(s.getMessage(52, 1));
        //System.out.println(s.getMessage(67, 1));

        //Testing sendMessage()
        //s.sendMessage(78, "Akua078 sent a second message"); 
        //s.sendMessage(78, "This message does not have a duplicate ID"); 
        //s.sendMessage(78, "This message has a duplicate id! What idiot programmed this?"); 

        //Testing userExists()
        //System.out.println(s.userExists(78));
        //System.out.println(s.userExists(0));
        //System.out.println(s.userExists(52));

        //Testing message deletion
        //s.deleteMessage(78, 7);
 
        //Testing finding a document with the max mID
        System.out.println("User 78: " + s.getMaxMID(78) + " Total Messages");
        System.out.println("User 5: " + s.getMaxMID(5) + " Total Messages");
        System.out.println("User 69: " + s.getMaxMID(69) + " Total Messages");





        System.out.println("\nEND  - - - - - - - - - - - - - - - - - - - - - -\n");


        
    }
}
