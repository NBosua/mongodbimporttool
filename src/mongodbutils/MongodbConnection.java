/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongodbutils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;

/**
 *
 * @author NBosua
 */
public class MongodbConnection {

    MongoClient mongoClient;
    DB db;
    
    public boolean connect(String host, int portNo, String dbName, String username, String password, boolean authenticate) {
        try {

            //System.out.println("mongo start connect");
            //if (host.equals("")) { host = "s2.osh2.net"; }
            MongoClientOptions options = MongoClientOptions.builder()
                    .connectTimeout(30000)
                    //.socketTimeout(30000)
                    .autoConnectRetry(true)
                    .build();

            mongoClient = new MongoClient(new ServerAddress(host, portNo), options);

            //String dbURI = "mongodb://"+host+":27017/?ssl=true";
            //mongoClient = new MongoClient(new MongoClientURI(dbURI));
            //System.out.println("mongo get db:"+dbName);
            db = mongoClient.getDB(dbName);
            if (null == db) {
                return false;
            }

            //System.out.println("mongo authenticate");
            boolean auth = false;
            if (authenticate) {
                auth = db.authenticate(username, password.toCharArray());
            } else {
                auth = true;
            }

            return auth;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertJSON(String dbName, String collectionName, String json) {
        try {
            //System.out.println("mongo start insert");

            if (mongoClient == null) {
                //System.out.println("client is null");
                //connect("s3.osh3.net","","nico","nico");
                //return false;
            }

            db = mongoClient.getDB(dbName);

            //System.out.println("mongo get collection");
            DBCollection coll = db.getCollection(collectionName);

            //System.out.println("parse json");
            DBObject dbObject = (DBObject) JSON.parse(json);

            //System.out.println("insert data");
            //coll.insert(dbObject,WriteConcern.JOURNALED);
            coll.insert(dbObject, WriteConcern.NORMAL);

            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    public void disconnect() {
        if (null != mongoClient) {
            mongoClient.close();
        }
    }

}
