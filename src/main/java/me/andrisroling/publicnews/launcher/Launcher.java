package me.andrisroling.publicnews.launcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;

import me.andrisroling.publicnews.factories.SQLFactory;
import me.andrisroling.publicnews.factories.UpdateSQLDataFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/
public class Launcher {
	private static final Logger logger = LogManager.getLogger(Launcher.class);

	public static void main(String[] args) throws MalformedURLException, SQLException, IOException, InterruptedException {
		while(true) {

			logger.debug("Execute an Update");
			
			try {
				Connection connection = SQLFactory.getConnection();
				UpdateSQLDataFactory.doUpdate(connection, true);
				connection.close();
			} catch(Exception e) {
				//TODO
				logger.error("Got an Error while getting / inserting the News ",e);
				e.printStackTrace();
			}
			
			logger.debug("Sleep for 1 Minute");
			Thread.sleep(60000);
			
			
		}
	}
}
