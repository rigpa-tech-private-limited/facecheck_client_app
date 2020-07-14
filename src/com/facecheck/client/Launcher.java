package com.facecheck.client;

import com.facecheck.db.DB;
import com.facecheck.tools.Utils;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;

/**
 *
 * @author user
 */
public class Launcher {
    private static DB databaseConnection;
    public static DB getDatabaseConnection() {
        return databaseConnection;
    }
    
    private static MainFrame frame;
    public static MainFrame getFrame(){
        return frame;
    }
    
    public static void main(String[] args){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e){
            
        }
        
        // database connection information
        Map<String, String> dbInfo = new HashMap<String, String>();
        
        dbInfo.put("dbHost", "localhost");
        dbInfo.put("dbPort", "8889");
        dbInfo.put("dbUser", "root");
        dbInfo.put("dbPass", "root");
        dbInfo.put("dbName", "facecheck_client");
        
        databaseConnection = new DB(dbInfo);
        if(!databaseConnection.connectDB()){
            Utils.alert("Database connection failed.");
            return;
        }
        
        frame = new MainFrame();
        frame.setVisible(true);
        
        frame.setContent(new CameraList(), "Cameras");
    }
}
