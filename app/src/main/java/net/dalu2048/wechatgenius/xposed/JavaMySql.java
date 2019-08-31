package net.dalu2048.wechatgenius.xposed;

import java.sql.*;

import de.robv.android.xposed.XposedBridge;

public class JavaMySql {

    private int dataKey = 0;
    private int dataMessage = 0;

    private Connection getConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://106.14.184.10:3306/wxrobot?useUnicode=" +
                            "yes&characterEncoding=UTF-8",
                    "wxrobot","hwi1VWA5R8VPdbKx");
            XposedBridge.log("Connected to" + con);
            return con;
        }
        catch (Exception e) {
            XposedBridge.log("Can't Connect to Database");
            XposedBridge.log(e);
            return null;
        }
    }

    public boolean insertKey(String term) {
        Connection con = this.getConnection();
        if (con == null ) {
            XposedBridge.log("No Connection");
            return false;
        }
        this.dataKey += 1;


        try {
            PreparedStatement insertKey = con.prepareStatement(
                    "insert into wx_keyinfo (id, keyName) values (?,?)");
            insertKey.setInt(1,dataKey);
            insertKey.setString(2,term);
            insertKey.execute();

            con.close();
            return true;
        }
        catch (Exception e) {
            XposedBridge.log(e);
            return false;
        }
    }

    public boolean insertMessage(String content, String key, String time) {
        Connection con = this.getConnection();
        if (con == null) {
            XposedBridge.log("No Connection");
            return false;
        }
        this.dataMessage += 1;

        try {

            int realTime = Integer.parseInt(time.substring(0,10));


            PreparedStatement insertMessage = con.prepareStatement(
                    "insert into wx_message (id, message, pregKeyName, addTime)" +
                            "values (?, ?, ?, ?)");
            insertMessage.setInt(1, dataMessage);
            insertMessage.setString(2, content);
            insertMessage.setString(3, key);
            insertMessage.setInt(4, realTime);
            insertMessage.execute();

            con.close();
            return true;
        }
        catch (Exception e) {
            XposedBridge.log(e);
            return false;
        }
    }


}
