package me.andrisroling.publicnews.constants;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/

public class CONSTANTS {
    public static final String MYSQL_HOST = "andrisroling.me";
    public static final String MYSQL_USERNAME = "";
    public static final String MYSQL_PASSWORD = "";
    public static final String MYSQL_DATABASE = "";
    public static final int MYSQL__PORT = 3306;

    public static boolean isSQLDataSet() {
        if(MYSQL_HOST.equals("") || MYSQL_DATABASE.equals("") || MYSQL_DATABASE.equals("")) {
            return false;
        }

        return true;
    }
}
