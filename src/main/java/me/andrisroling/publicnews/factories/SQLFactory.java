package me.andrisroling.publicnews.factories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import me.andrisroling.publicnews.constants.CONSTANTS;
import me.andrisroling.publicnews.factories.objects.NewsProvider;
import me.andrisroling.publicnews.rest.newsapi.json.Article;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: Andris Roling - andrisroling.me
 * @description: -
 **/
public class SQLFactory {
	private static final Logger logger = LogManager.getLogger(SQLFactory.class);

	/**
	 * Creates Connection Link to MySQL DB
	 * @return ConnectionLink
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		//Secure that SQL Data is set
		if(!CONSTANTS.isSQLDataSet()) {
			logger.error("SQL Data not Set");
			throw new SQLException("SQL Data in CONSTANTS not set.");
		}

		//Creating URL for SQL Connection
		String mysql_url = "jdbc:mysql://" + CONSTANTS.MYSQL_HOST + ":" + CONSTANTS.MYSQL__PORT +  "/" + CONSTANTS.MYSQL_DATABASE;

		//Create Connection
		return DriverManager.getConnection(mysql_url, CONSTANTS.MYSQL_USERNAME, CONSTANTS.MYSQL_PASSWORD);
	}

	/**
	 * Inserts a Single Article into DB
	 * @param connection
	 * @param publisherId
	 * @param article
	 * @throws SQLException
	 */
	public static void insertArticle(Connection connection, int publisherId, Article article) throws SQLException {
		List<Article> articleList = new ArrayList<Article>();
		articleList.add(article);

		insertMultipleArticles(connection, publisherId, articleList);
	}

	/**
	 * Inserts multiple Articles into DB
	 * @param connection
	 * @param publisherID
	 * @param articleList
	 * @return Number of Inserted Articles
	 * @throws SQLException
	 */

	public static int insertMultipleArticles(Connection connection, int publisherID, List<Article> articleList)
			throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO publicnews_articles (AuthorID, Title, Description, Url, UrlToImage, PublishedAt, PublisherID) VALUES(?,?,?,?,?,?,?)");

		int articlesInserted = 0;
		
		for (Article article : articleList) {
			int authorID = getAuthorID(connection, article.getAuthor());

			if (!doesArticleExists(connection, article.getTitle(), authorID, article.getPublishedAt())) {
				preparedStatement.setInt(1, authorID);
				preparedStatement.setString(2, article.getTitle());

				logger.trace("Article Description Length "+article.getDescription().length());
				preparedStatement.setString(3, article.getDescription());
				preparedStatement.setString(4, article.getUrl());
				preparedStatement.setString(5, article.getUrlToImage());
				preparedStatement.setString(6, article.getPublishedAt());
				preparedStatement.setInt(7, publisherID);

				logger.debug("Insert Article with Title " + article.getTitle());
				//System.out.println("Article : "+article.toString());
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
				
				articlesInserted++;
			} else {
				logger.trace("Do not Insert Article with Title " + article.getTitle() + " - it already exists");
			}

		}

		preparedStatement.close();
		
		return articlesInserted;
	}

	/**
	 * Controls if article already exists in DB
	 * @param connection
	 * @param articleTitle
	 * @param authorID
	 * @param publishedAt
	 * @return true = article exists, false = article doesn't exist
	 * @throws SQLException
	 */
	public static boolean doesArticleExists(Connection connection, String articleTitle, int authorID,
			String publishedAt) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT ArticleID FROM publicnews_articles WHERE Title = ? AND AuthorID = ? AND PublishedAt = ?");

		preparedStatement.setString(1, articleTitle);
		preparedStatement.setInt(2, authorID);
		preparedStatement.setString(3, publishedAt);

		preparedStatement.executeQuery();

		ResultSet resultSet = preparedStatement.getResultSet();

		if (resultSet.next()) {
			preparedStatement.close();
			return true;
		} else {
			preparedStatement.close();
			return false;
		}
	}

	/**
	 * Gets the Author ID of an Author from DB if author exists, if not it creates the author
	 * @param connection
	 * @param authorName
	 * @return authorID
	 * @throws SQLException
	 */
	public static int getAuthorID(Connection connection, String authorName) throws SQLException {
		if (authorName == null) {
			return 1;
		} else {

			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT AuthorID FROM publicnews_authors WHERE AuthorName = ?");

			preparedStatement.setString(1, authorName);

			preparedStatement.executeQuery();

			ResultSet resultSet = preparedStatement.getResultSet();

			if (!resultSet.next()) {
				preparedStatement.close();
				return insertAuthor(connection, authorName);
			} else {
				int autoIncrementKey = resultSet.getInt(1);

				preparedStatement.close();
				return autoIncrementKey;
			}
		}
	}

	/**
	 * Creates an Author in DB
	 * @param connection
	 * @param authorName
	 * @return
	 * @throws SQLException
	 */
	public static int insertAuthor(Connection connection, String authorName) throws SQLException {
		logger.trace("Insert Author with Author Name " + authorName);

		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO publicnews_authors (AuthorName) VALUES (?)",
				PreparedStatement.RETURN_GENERATED_KEYS);

		preparedStatement.setString(1, authorName);

		preparedStatement.executeUpdate();

		preparedStatement.getWarnings();

		ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

		generatedKeys.next();

		int key = generatedKeys.getInt(1);

		preparedStatement.close();

		return key;
	}

	/**
	 * Get News Providers from DB
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	public static List<NewsProvider> getNewsProviders(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeQuery(
				"SELECT * FROM publicnews_publisher INNER JOIN publicnews_publisher_api ON publicnews_publisher.PublisherID = publicnews_publisher_api.PublisherID ");

		ResultSet resultSet = statement.getResultSet();

		List<NewsProvider> newsProviderToReturn = new ArrayList<NewsProvider>();

		while (resultSet.next()) {
			String publisherApiPath = resultSet.getString("PublisherAPIPath");

			logger.trace("Publisher Api Path " + publisherApiPath.length() + " " + publisherApiPath);

			NewsProvider newsProviderToAdd = new NewsProvider(resultSet.getInt("PublisherID"),
					resultSet.getString("PublisherName"), publisherApiPath);

			newsProviderToReturn.add(newsProviderToAdd);
		}

		statement.close();

		return newsProviderToReturn;
	}

	/**
	 * Set the "LastUpdated" Variable in DB to Current Timestamp
	 * @param connection
	 * @throws SQLException
	 */
	public static void setLastUpdated(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate("UPDATE publicnews_variables SET VariableValue = NOW() WHERE VariableName = 'LastUpdated'");
	}

	/**
	 * Formats the Dates to the SQL Timestamp
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	public static int formatDates(Connection connection) throws SQLException {
		logger.trace("Format the Dates");
		
		int convertedRows = 0; 
		
		Statement statement = connection.createStatement();
		statement.executeQuery("SELECT * FROM publicnews_articles WHERE DateFormatted = NULL OR FormattedArticle != 1");
		
		ResultSet resultSet = statement.getResultSet();
		
		PreparedStatement updateStatement = connection.prepareStatement("UPDATE publicnews_articles SET FormattedArticle = ?, DateFormatted = ? WHERE ArticleID = ?");
		
		/*
		 * Search Results
		 */
		
		String dateString;
		String[] dateStringSplitted; 
		String dateString_1;
		String dateString_2;
		String convertedDateStringInSQLFormat; 
		
		while(resultSet.next()) {
			logger.trace("Format Date for Article "+resultSet.getInt("ArticleID"));
			//Date Format 
			//2017-07-18T13:14:36Z
			//Convert to Format
			//2017-04-27 21:41:54
			dateString = resultSet.getString("PublishedAt");
			
			dateString = dateString.substring(0,dateString.length()-1); // Last Char removed
			
			dateStringSplitted = dateString.split("T");
			dateString_1 = dateStringSplitted[0];
			dateString_2 = dateStringSplitted[1];

			if(dateString.endsWith("+00:0")) {
				dateString_2 = dateString_2.replace("+00:0","");
			}
			
			convertedDateStringInSQLFormat = dateString_1+" "+dateString_2;
			
			updateStatement.setInt(1, 1);
			updateStatement.setString(2, convertedDateStringInSQLFormat);
			updateStatement.setInt(3,resultSet.getInt("ArticleID"));
			
			logger.trace("New Format for Article: "+convertedDateStringInSQLFormat);
			
			updateStatement.executeUpdate();
			
			updateStatement.clearParameters();
			convertedRows++;
		}
		
		logger.debug("Converted "+convertedRows+" Rows");
		
		return convertedRows;
	}

	public static void main(String[] args) throws SQLException {
		getNewsProviders(getConnection());
	}
}
