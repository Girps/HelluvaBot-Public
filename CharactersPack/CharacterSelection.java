package CharactersPack;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.cache.AbstractCache.StatsCounter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;




 

/* Class will hold a series of functions to help  
 * query characters from the database 
 * */ 

 

public class CharacterSelection {
	
	private static String AdminName; 
	private  static String password; 
	private static String urlDbs; 
	private static HikariConfig config = new HikariConfig(); 
	private static HikariDataSource dataSource; 
	//volatile public  ConcurrentHashMap<String, ArrayList<String>> imageInfo = null; 

	public CharacterSelection()
	{
		
	}
	
	public CharacterSelection(String urlArg, String nameArg, String passwordArg)
	{
		urlDbs = urlArg; 
		AdminName = nameArg; 
		password = passwordArg;
		config.setJdbcUrl(urlArg);
		config.setUsername(nameArg);
		config.setPassword(passwordArg);
		config.setConnectionTimeout(6000); // will throw if connection not retireved in time
		config.setLeakDetectionThreshold(6000); 
		config.setMaximumPoolSize( 3 * 2 + 1); // old pool size 24 new pool size is ((cores) * 2 + 1) = 
		dataSource = new HikariDataSource(config);
		 
	}
	
	public HikariDataSource getPool() 
	{
		return dataSource;
	}
	
	/*Method to get all characters on the database */
	public ArrayList<String> getAllCharacterNames(GAMETYPE type, long serverId) 
	{
		// Query 
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		ArrayList<String> names = null; 
		try 
		{
			
			conn = dataSource.getConnection(); 
			String query = ""; 
		
			switch(type) 
			{
			case WIKI :
				query = "SELECT name FROM characters WHERE is_Major_Character = 'T' OR is_Major_Character = 'F' "; 
				stat = conn.prepareStatement(query);
				break; 
			case KDM :
			query = "SELECT name FROM characters \r\n"
					+ "WHERE characters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "WHERE is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inKDM = \"T\" " + " AND " + "server_Id = ? "   
					+ " UNION "
					+ "SELECT name FROM customCharacters\r\n"
					+ "WHERE inKDM = \"T\"" + " AND " + "server_Id = ? " ; 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			break; 
			case SMASHPASS :
			query = "SELECT name FROM characters \r\n"
					+ "WHERE characters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "WHERE is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inSP = \"T\" " + " AND " + "server_Id = ? "
					+ " UNION "
					+ " SELECT name FROM customCharacters\r\n"
					+ " WHERE inSP = \"T\""  + " AND " + "server_Id = ? ";  
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			break; 
		case FAVORITES :
			query = "SELECT name FROM characters \r\n"
					+ "UNION \r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inFav = \"T\" " + " AND " + "server_Id = ? "   
					+ " UNION "
					+ " SELECT name FROM customCharacters\r\n"
					+ " WHERE inFav = \"T\"" + " AND " + "server_Id = ? " ;  
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			break; 
			
		case COLLECT : 
			query = "SELECT name FROM characters \r\n"
					+ "UNION \r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inCollect = \"T\" " + " AND " + "server_Id = ? "
					+ " UNION "
					+ " SELECT name FROM customCharacters\r\n"
					+ " WHERE inCollect = \"T\"" + " AND " + "server_Id = ?";  
			
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			break; 
		}
			
			res = stat.executeQuery();  
		
			res.next(); 
		
			names = new ArrayList<String>(); 
		
			do 
			{
				names.add(res.getString(1));
			}
			while(res.next()); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return names;
	}
	
	/*Method to get all OC names on the table */
	public ArrayList<String> getUsersOCName(Long userId, Long serverId) 
	{
		ArrayList<String> names = new ArrayList<String>();
		
		String query = "SELECT name FROM customCharacters " 
		+ "WHERE user_Id = ? AND server_Id = ? " + 
		 " ORDER BY timeCreated DESC ";

		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			do 
			{
				names.add(res.getString(1));
			}
			while(res.next()); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return names; 
	}
	
	/* Method only used for testing purposes */
	public ArrayList<Character> getAllCharacters(SELECTIONTYPE type ,SETUPTYPE set) 
	{
		Statement stat = null;
		ResultSet res = null;
		Connection conn = null; 
		ArrayList<Character> list = null;  

		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			String query = ""; 
		switch(type) 
		{
		case ALL: 
			 query = "SELECT * FROM characters"; 
				break ; 
		case MAJOR_CHARACTER:
			 query = "SELECT * FROM characters" + " WHERE is_Major_Character = \"T\""; 
				break; 
		case MINOR_CHARACTER: 
			 query = "SELECT * FROM characters" + " WHERE is_Major_Character = \"F\""; 
				break; 
		}
		
		 res = stat.executeQuery(query); 
		
		 list = new ArrayList<Character>();  
		
		res.next(); 
		
		int col = res.getMetaData().getColumnCount(); 
		
		String[] columnData = new String[col+1]; 
		
		do {
			
			for(int i =1; i <= col; ++i) 
			{
				columnData[i] = res.getString(i); 
			}
			CharacterFactory factor = new CharacterFactory(Long.valueOf(columnData[1]), columnData[2], columnData[3],columnData[5],col, null, set);
			
			Character chtr = factor.getCharacter(); 
			// Done get data now instantiate character 
			list.add(chtr ); 
			
		}
		while(res.next()); 
		
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
				return list; 

	}
	
	/* Method seraches if user has a waifu in a specifed guild */ 
	public boolean searchUserInWaifu(Long userID, Long serverID)  
	{
		
		// Query the waifu database 
		
	
		PreparedStatement stat = null; 
		ResultSet res = null;
		Connection conn = null; 
		boolean result = false; 
		try 
		{	
			String query = "SELECT * FROM waifus " + 
						"WHERE user_id = ? AND server_id = ? " +  
						" LIMIT 1";  
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);  
			stat.setLong(1, userID);
			stat.setLong(2, serverID);
			res = stat.executeQuery();
			// Found then return true otherwise false 
			result = res.next(); 
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result ?  true : false;   

	}
	
	
	
	public void waifuSwap(Long traderId, Long tradeeId  , Long chtrOne, Long chtrTwo , Long serverId) 
	{
		String queryTrader = "UPDATE waifus "
				+ "SET waifu_Id = ? "
				+ "WHERE user_id = ? AND server_id = ?";
		String queryTradee = "UPDATE waifus "
				+ "SET waifu_Id = ? "
				+ "WHERE user_id = ? AND server_id = ?";
		PreparedStatement statTrader = null; 
		PreparedStatement statTradee = null; 
		Connection conn = null; 
		try 
		{
			// Make atmoic
			conn = dataSource.getConnection(); 
			conn.setAutoCommit(false);
			statTrader = conn.prepareStatement(queryTrader); 
			statTrader.setLong(1, chtrTwo);
			statTrader.setLong( 2, traderId);
			statTrader.setLong(3, serverId);
			statTradee = conn.prepareStatement(queryTradee); 
			statTradee.setLong(1, chtrOne);
			statTradee.setLong(2, tradeeId);
			statTradee.setLong(3, serverId);
			
			statTrader.execute(); 
			statTradee.execute();
			// Save updates 
			conn.commit();
		}
		catch(SQLException e) 
		{
			
			try 
			{
				conn.rollback();
			} catch (SQLException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();

		}
		finally 
		{
			try {  if(statTrader != null) { statTrader.close(); } } catch(Exception e){} 
			try {  if(statTradee != null) { statTradee.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
		}
	} 

	
	
	
	/* Method adds user and there character to waifu table */ 
	public void insertWaifu(Long userID, Long serverID, Character chtr)  
	{
		String query = "INSERT waifus (user_id, server_id, waifu_id)" + 
						" VALUES( ? , ? , ? )";
		
		PreparedStatement stat = null ;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userID);
			stat.setLong(2, serverID);
			stat.setLong(3, chtr.getId());
			// Now insert the data 
			stat.execute();  
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		 
	}
	
	/* Method gets a user's character waifu from a specified guild */ 
	public Character getUserWaifu(Long userID, Long serverID) 
	{
		PreparedStatement statWaifuId = null;
		PreparedStatement statTime = null;

		ResultSet res = null; 
		ResultSet res2 = null; 
		Character found = null; 
		Connection conn = null; 
		try 
		{	
			String query = "SELECT waifu_id FROM waifus " + 
					"WHERE user_id = ? AND server_id = ? " + 
					" LIMIT 1";
			conn = dataSource.getConnection(); 
			statWaifuId = conn.prepareStatement(query); 
			statWaifuId.setLong(1, userID);
			statWaifuId.setLong(2, serverID);
			res = statWaifuId.executeQuery();
		
			// Have the character now find it in the characters table 
			res.next(); 
			
			Long id = Long.valueOf(res.getString(1));
			
			// Now get the character with this id 
			found = this.getCharacterById(id); 
			
			query = "SELECT LAST_EXECUTED FROM INFORMATION_SCHEMA.events\r\n"
					+ "WHERE EVENT_NAME = \"waifu_Reset_Event\"";
			statTime = conn.prepareStatement(query);
			res2 = statTime.executeQuery();
			res2.next(); 
			
			Date date = res2.getTimestamp(1, Calendar.getInstance(TimeZone.getTimeZone("GMT")));  
			found.setDate(date);
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			
			try {  if(res2 != null ) { res2.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(statWaifuId != null) { statWaifuId.close(); } } catch(Exception e){} 
			try {  if(statTime != null) { statTime.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return found;
	}
	
	
	// Used to get n random characters
	public  Character[] getRandomCharacters(GAMETYPE type, SETUPTYPE set, Long serverId ,int n) throws Exception  
	{
		Character[] found = null; 
		String query = ""; 
		Connection conn = null;
		PreparedStatement stat = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			
		switch(type) 
		{
		case KDM: 
			query =  "SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character , "
					+ " COALESCE(server_def_chtrs.def , image_def ) as image_def , image_urls FROM characters "
					+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? " +  
					 " WHERE characters.is_Adult = \"T\"" + 
					 " UNION " + 
					 " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0, null FROM gameCharacters " +
					 " WHERE gameCharacters.is_Adult = \"T\"" + 
					 " UNION " + 
					 " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas " + 
					 " WHERE sonas.inKDM = \"T\" " + " AND server_Id = ?" +    
					 " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url , 0 , null FROM customCharacters " + 
					 " WHERE inKDM = \"T\" " + " AND server_Id = ?" +     
					 " ORDER BY RAND() LIMIT ?"; 
			
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case SIMPS:
			query = "SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character , "
					+ " COALESCE(server_def_chtrs.def , image_def ) as image_def , image_urls FROM characters "
					+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? "
					+ " WHERE characters.is_Adult = \"T\" "
					+ " UNION\r\n"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url , 0 , null  FROM gameCharacters "
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas "
					+ " WHERE sonas.inSimps = \"T\" " + " AND server_Id = ?"
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url , 0 , null FROM customCharacters " + 
					 " WHERE inSimps = \"T\" " + " AND server_Id = ?"+   
					 " ORDER BY RAND() LIMIT ?"; 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case SHIPS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
					+ " COALESCE(server_def_chtrs.def, image_def ) as image_def , image_urls FROM characters "
					+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url , 0 , null FROM gameCharacters"
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0, url FROM sonas"
					+ " WHERE sonas.inShips = \"T\""  +  " AND server_Id = ?" 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url , 0, null FROM customCharacters " + 
					 " WHERE inShips = \"T\" " + " AND server_Id = ?" +   
					 " ORDER BY RAND() LIMIT ?"; 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case KINS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character , "
					+ " COALESCE(server_def_chtrs.def , image_def ) as image_def , image_urls FROM characters "
					+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? "
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0, null FROM gameCharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 ,url FROM sonas"
					+ " WHERE sonas.inKins = \"T\" " + " AND server_Id = ?" 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url, 0 , null FROM customCharacters " + 
					 " WHERE inKins = \"T\" " + " AND server_Id = ?"  +    
					 " ORDER BY RAND() LIMIT ?"; 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case SMASHPASS: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character , "
					+ " COALESCE( server_def_chtrs.def , characters.image_def ) , image_urls FROM characters "
					+ "LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0, null FROM gameCharacters"
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url, 0 , url FROM sonas"
					+ " WHERE sonas.inSP = \"T\" " + " AND server_Id = ?" 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url , 0 , null FROM customCharacters " + 
					 " WHERE inSP = \"T\" " + " AND server_Id = ?"+     
					 " ORDER BY RAND() LIMIT ?"; 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case WAIFU:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character , "
					+ " COALESCE(server_def_chtrs.def, characters.image_def) , image_urls FROM characters "
					+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url,0,null FROM gameCharacters"
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url,0,url FROM sonas"
					+ " WHERE sonas.inWaifu = \"T\"" +   " AND server_Id = ?"  
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url,0,null FROM customCharacters " + 
					 " WHERE inWaifu = \"T\" " + " AND server_Id = ?" +    
					 " ORDER BY RAND() LIMIT ?"; 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case GUESS: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
					+ " COALESCE(server_def_chtrs.def , characters.image_def ) , image_urls FROM characters "
					+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
					+ " AND server_def_chtrs.server_id = ? "
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url,0 , null FROM gameCharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url, 0, url FROM sonas"
					+ " WHERE sonas.inGuess = \"T\" " + " AND server_Id = ?" 
					+ " UNION "  
					+ " SELECT cusChar_Id, name, user_Id, url, 0, null FROM customCharacters "  
					+ " WHERE inGuess = \"T\" " +   " AND server_Id = ?"       
					+ " ORDER BY RAND() LIMIT ?" ; 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set); 
			break; 
		case COLLECT: 
			query = "WITH chosen_rarity AS\r\n"
					+ "( "
					+ "	SELECT name\r\n"
					+ "    FROM rarity \r\n"
					+ "    ORDER BY RAND()/weight\r\n"
					+ "    LIMIT 1\r\n"
					+ ") "
					+ " "
					+ " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
					+ "					COALESCE(server_def_chtrs.def, characters.image_def ) , image_urls FROM characters "
					+ "					LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id  "
					+ "					 AND server_def_chtrs.server_id = ? "
					+ "                     WHERE rarity = (SELECT name FROM chosen_rarity) "
					+ "					UNION "
					+ "					 SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0, null FROM gameCharacters "
					+ "                     WHERE rarity = (SELECT name FROM chosen_rarity) "
					+ "					UNION "
					+ "					SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas "
					+ "					 WHERE sonas.inCollect = \"T\"  AND server_Id = ? AND rarity = (SELECT name FROM chosen_rarity) "
					+ "					UNION  "
					+ "					SELECT cusChar_Id, name, user_Id, url , 0, null FROM customCharacters "
					+ "					 WHERE inCollect = \"T\"  AND server_Id = ? AND rarity = (SELECT name FROM chosen_rarity) "
					+ "                    ORDER BY RAND() LIMIT ? " ; 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, serverId);
			stat.setLong(3, serverId);
			stat.setInt(4, n);
			found = processQueryGetCharacters(stat,set);  
			break; 
		}
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		catch(Exception e) 
		{
			throw new Exception(e.getMessage()); 
		}
		finally 
		{
			try  { if(stat != null) {stat.close();}}catch(Exception e) {}
			try  { if(conn != null) {conn.close();}}catch(Exception e) {}			
		}
		
		return found;  
	}
	
	
	

	/* Return a single character type of character based on enum SELECTIONTYPE
	 * Need to edit to use prepareStatements in queries 
	 *  */
	public  Character requestSingleCharacter(String name, long serverId,GAMETYPE type, SETUPTYPE set) throws Exception
	{
		Character found  = null; 
		Connection conn = null;
		PreparedStatement stat = null; 
		try
		{ 
			String query = ""; 
			conn = dataSource.getConnection(); 
			switch(type)
			{
				case KDM:
				
					query  = 
							 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
							 + " COALESCE(server_def_chtrs.def , image_def ) as image_def , image_urls FROM characters"
							 + " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
							 + " AND server_def_chtrs.server_id = ? "
							+ " WHERE LOWER(characters.name) = LOWER(?) AND characters.is_Adult = \"T\" "
							+ " UNION"
							+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url , 0 , null FROM gameCharacters"
							+ " WHERE LOWER(gameCharacters.name) = LOWER(?) AND gameCharacters.is_Adult = \"T\""
							+ " UNION"
							+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas"
							+ " WHERE  LOWER(sonas.name) = LOWER(?) AND sonas.inKDM = \"T\"" + " AND server_Id = ?" 
							+ " UNION "
							+ " SELECT cusChar_Id, name, user_Id, url , 0 , null FROM customCharacters"
							+ " WHERE  LOWER(name) = LOWER(?) AND inKDM = \"T\"" + " AND server_Id = ?"
							+ "  LIMIT 1"; 
				
				stat = conn.prepareStatement(query); 
				stat.setLong(1, serverId);
				stat.setString(2, name);
				stat.setString(3, name);
				stat.setString(4, name);
				stat.setLong(5,serverId);
				stat.setString(6, name);
				stat.setLong(7, serverId);
				found = processQueryGetCharacters(stat,set)[0]; 
				break; 
				
				case SMASHPASS:
					
					query  = 
							 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
							 + "COALESCE( server_def_chtrs.def , image_def) as image_def , image_urls FROM characters"
							 + " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id " 
							 + " AND server_def_chtrs.server_id = ? "
							+ " WHERE LOWER(characters.name) = LOWER(?) AND characters.is_Adult = \"T\" "
							+ " UNION"
							+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0 , null  FROM gameCharacters"
							+ " WHERE LOWER(gameCharacters.name) = LOWER(?) AND gameCharacters.is_Adult = \"T\""
							+ " UNION"
							+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas"
							+ " WHERE  LOWER(sonas.name) = LOWER(?) AND sonas.inSP = \"T\"" +  " AND server_Id = ?" 
							+ " UNION "
							+ " SELECT cusChar_Id, name, user_Id, url , 0, null FROM customCharacters"
							+ " WHERE  LOWER(name) = LOWER(?) AND inSP = \"T\"" +  " AND server_Id = ?"
							+ "  LIMIT 1"; 
					
					stat = conn.prepareStatement(query); 
					stat.setLong(1, serverId);
					stat.setString(2, name);
					stat.setString(3, name);
					stat.setString(4, name);
					stat.setLong(5, serverId);
					stat.setString(6, name);
					stat.setLong(7, serverId);
					found = processQueryGetCharacters(stat,set)[0]; 
					break; 
				case WIKI: 
					query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, 0, null FROM characters"
							+ " WHERE LOWER(characters.name) = LOWER(?) "; 
				
					stat = conn.prepareStatement(query); 
					stat.setString(1, name);
					
					found = processQueryGetCharacters(stat,set)[0]; 
					
					found.setContent();
					found.setUpImages();
					break;
				case FAVORITES: 
						query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
								+ " COALESCE( server_def_chtrs.def , image_def ) as image_def, image_urls FROM characters "
								+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
								+ " AND server_def_chtrs.server_id = ? "
								+ " WHERE LOWER(characters.name) = LOWER(?) "
								+ " UNION"
								+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0 , null FROM gameCharacters"
								+ " WHERE LOWER(gameCharacters.name) = LOWER(?) "
								+ "UNION"
								+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas"
								+ " WHERE  LOWER(sonas.name) = LOWER(?) AND sonas.inFav = \"T\"" +  " AND server_Id = ?" 
								+ " UNION "
								+ " SELECT cusChar_Id, name, user_Id, url , 0 , null FROM customCharacters"
								+ " WHERE  LOWER(name) = LOWER(?) AND inFav = \"T\"" +  " AND server_Id = ?" 
								+ "  LIMIT 1"; 
					
						stat = conn.prepareStatement(query); 
						stat.setLong(1, serverId);
						stat.setString(2, name);
						stat.setString(3, name);
						stat.setString(4, name);
						stat.setLong(5, serverId);
						stat.setString(6, name);
						stat.setLong(7, serverId);
						found = processQueryGetCharacters(stat,set)[0]; 
					break;
				case COLLECT:
					{
						
							query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, "
									+ " COALESCE(server_def_chtrs.def, image_def ) as image_def, image_urls FROM characters "
									+ " LEFT JOIN server_def_chtrs ON characters.char_id = server_def_chtrs.char_id "
									+ " AND server_def_chtrs.server_id = ? "
									+ " WHERE LOWER(characters.name) = LOWER(?) "
									+ " UNION"
									+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0 , null FROM gameCharacters"
									+ " WHERE LOWER(gameCharacters.name) = LOWER(?) "
									+ "UNION"
									+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , url FROM sonas"
									+ " WHERE  LOWER(sonas.name) = LOWER(?) AND sonas.inCollect = \"T\"" +  " AND server_Id = ?"
									+ " UNION "
									+ " SELECT cusChar_Id, name, user_Id, url , 0, null FROM customCharacters"
									+ " WHERE  LOWER(name) = LOWER(?) AND inCollect = \"T\"" +  " AND server_Id = ?"  
									+ "  LIMIT 1"; 
						
							stat = conn.prepareStatement(query); 
							stat.setLong(1, serverId);
							stat.setString(2, name);
							stat.setString(3, name);
							stat.setString(4, name);
							stat.setLong(5, serverId);
							stat.setString(6, name);
							stat.setLong(7, serverId);
							found = processQueryGetCharacters(stat,set)[0]; 
					}	
					
				default:	
			}
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
			throw new Exception(e.getMessage()); 
		}
		finally 
		{
			try  { if(stat != null) {stat.close();}}catch(Exception e) {}
			try  { if(conn != null) {conn.close();}}catch(Exception e) {}
		}
		
		// throw exception if null 
		
		return found; 
	}
	
	
	
	
	
	/* Request from database a character from given string using a prepared statement */ 
	private  Character[] processQueryGetCharacters(PreparedStatement stat, SETUPTYPE type) throws Exception  
	{
		PreparedStatement statTime = null;	

		ResultSet  res = null; 
		ResultSet timeRes = null;
		Character arr[] = null; 
		String errorName = null; 
		try (Connection conn = dataSource.getConnection())
		{
	
			// Now get the date for next switch 
			String queryLcl = "SELECT LAST_EXECUTED FROM INFORMATION_SCHEMA.events\r\n"
					+ "WHERE EVENT_NAME = \"waifu_Reset_Event\"";
				
			statTime = conn.prepareStatement(queryLcl); 
			timeRes = statTime.executeQuery();
				
			timeRes.next(); 
				
			Date date = timeRes.getTimestamp(1); 
			res = stat.executeQuery(); 
			ArrayList<Character> list = new ArrayList<Character>(); 
			res.next(); 
			// columns must return at least one character
		do {
	
			CharacterFactory factory = null; 
			errorName = res.getString(2); 
			// if main/minor uses id, name, show name , is major character
			// if game character uses id, names , show name, image url, 
			// otherwise sonas/cusChar uses , id, name, userid, url 
			
			ArrayList<JSONObject> imgLinks = new ArrayList<JSONObject>(); 
			
			// now check if field is not null
			if (res.getString(6) != null) 
			{
				// convert to json
				JSONObject jsonObj = new JSONObject(res.getString(6)); 
				JSONArray jsonArr = jsonObj.getJSONArray("links"); 
				// get the links 
				for(int i =0; i < jsonArr.length(); i++) 
				{
					imgLinks.add( jsonArr.getJSONObject(i)); 
				}
			}
			// now instantiate type of character
			factory = new CharacterFactory(Long.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), Integer.valueOf(res.getString(5)), imgLinks ,type); 
			
			Character chtr = factory.getCharacter(); 
			chtr.setDate(date);
			
			// Done get data now instantiate character 
			list.add(chtr ); 
			
			}
		while(res.next()); 	
		// Return as arraylist
		 arr = list.toArray(new Character[list.size()]); 
		
		}
		catch (SQLException e)
		{
			// case the input is invalid 
			e.printStackTrace();
			throw new Exception("Invalid input"); 
		} 
		catch(Exception e) 
		{
			// case the character is not corretly processed '
			e.printStackTrace();
			throw new Exception("Error on character: " + errorName); 
		}
		
		
		finally 
		{
			try {  if(timeRes!= null) {timeRes.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(statTime != null) { statTime.close(); } } catch(Exception e){} 
		}
		
		return arr;
	}
	

	/* Method seraches if user has a sona in a specifed guild */ 
	public boolean searchUserInSona(Long userID, Long serverID)  
	{
		
		// Query the waifu database 
		
		PreparedStatement stat = null ;
		ResultSet res = null; 
		boolean result = false; 
		Connection conn = null ;
		try 
		{
			String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = ? AND server_Id = ? "+ 
						" LIMIT 1";  
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userID);
			stat.setLong(2, serverID);
			res = stat.executeQuery(); 
			result  = res.next(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){}
			try {  if(conn!= null) { conn.close(); } } catch(Exception e){} 
		}
	
		// Found then return true otherwise false 
		return (result) ?  true : false;   
	}
	
	/* Method gets the characters from the database */
	public Character getUserSona(Long userID, Long serverID) 
	{
		PreparedStatement stat = null;
		Character found = null; 
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			
			String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = ? AND server_Id = ? " + 
						 " LIMIT 1";
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userID);
			stat.setLong(2, serverID);
			res = stat.executeQuery(); 
			res.next(); 
			ArrayList<JSONObject> imgLinks = new ArrayList<JSONObject>(); 
			// now check if field is not null
			if (res.getString("url") != null) 
			{
				// convert to json
				JSONObject jsonObj = new JSONObject(res.getString("url")); 
				JSONArray jsonArr = jsonObj.getJSONArray("links"); 
				// get the links 
				for(int i =0; i < jsonArr.length(); i++) 
				{
					imgLinks.add( jsonArr.getJSONObject(i)); 
				}
			} 
			
		// We have the character now return it 
			CharacterFactory factory = new CharacterFactory(Long.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), 0, imgLinks, SETUPTYPE.LIGHT); 
			found = factory.getCharacter(); 
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return found; 
	}
	
	/* Method get user's sonas fields from the database */
	public HashMap<String, String> getSonaFields(long userId, long serverId)
	{
		PreparedStatement stat = null; 
		ResultSet res= null; 
		Connection conn = null; 
		HashMap<String, String> fields = new HashMap<>(11); 
		try 
		{
			String query = "SELECT name, url , inKDM , inSP, inSimps , inShips , inKins , inWaifu, inFav , inGuess, inCollect FROM "
					+ "sonas WHERE  user_Id = ? AND server_Id = ?"; 
			
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			// Have results now add to hash Map 
			fields.put("name", res.getString("name"));
			fields.put("url", res.getString("url"));
			fields.put("kdm", res.getString("inKDM")); 
			fields.put("smashpass", res.getString("inSP")); 
			fields.put("simps", res.getString("inSimps")); 
			fields.put("ships", res.getString("inShips"));
			fields.put("kins", res.getString("inKins") );
			fields.put("waifu", res.getString("inWaifu")); 
			fields.put("favorite", res.getString("inFav")); 
			fields.put("guess", res.getString("inGuess"));
			fields.put("collect", res.getString("inCollect")); 
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return fields; 
	}
	
	
	public void removeSonaList(ArrayList<Long> userIds, Long serverId) 
	{
		PreparedStatement stat = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			String listStr =  userIds.toString().replace("[", "(");
			listStr = listStr.replace("]", ")"); 
			String query = " DELETE character_Ids FROM character_Ids "
					+ " INNER JOIN sonas ON sonas.sona_Id = character_Ids.id  "
					+ "	WHERE sonas.user_Id IN " + listStr  
					+ "	AND sonas.server_Id = " + serverId + " ";  
			stat = conn.prepareStatement(query); 
			stat.execute(); 
		}
		catch(SQLException e) 
		{
			e.printStackTrace(); 
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	// Remove sona from the table 
	public boolean removeSona(Long userId, Long serverId)  
	{
		PreparedStatement stat = null;
		boolean result = false; 
		Connection conn = null; 
		try
		{
			conn = dataSource.getConnection(); 
			
			String query = "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN sonas ON sonas.sona_Id = character_Ids.id \r\n"
				+ "WHERE sonas.user_Id = ? AND sonas.server_Id = ?"; 
			stat = conn.prepareStatement(query);
			stat.setLong(1,userId);
			stat.setLong(2, serverId);
			result =  !stat.execute(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
	}
	
	// Remove waifu from the table
	public boolean removeWaifu(Long  userID, Long serverID)  
	{
		Statement stat = null;
		boolean result = false; 
		Connection conn =null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			String query = "DELETE FROM waifus " + 
					"WHERE user_Id = " + userID + " AND " + "server_Id = " + serverID; 
			result =  !stat.execute(query); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}
	
	public void removeWaifuList(ArrayList<Long> userIds, Long serverId) 
	{
		PreparedStatement stat = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			String listStr =  userIds.toString().replace("[", "(");
			listStr = listStr.replace("]", ")"); 
			String query = " DELETE waifus FROM waifus "
					+ " WHERE user_id IN " + listStr
					+ " AND server_id = " + serverId + " ";  
			stat = conn.prepareStatement(query); 
			stat.execute(); 
		}
		catch(SQLException e) 
		{
			e.printStackTrace(); 
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	
	
	/* Method will insert a character to the database on the sona table */ 
	public void insertSona(String name, Long userId, String url , Long serverId, String inKDM, String inSP, String inSimps,String inShips, String
			inKins, String inWaifu, String inFav, String inGuess, String inCollect)  
	{
		String queryOne = " INSERT INTO character_Ids(id) VALUES(NULL)"; 
		
		String queryTwo =  " INSERT INTO sonas (sona_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess, inCollect) " +  
				 " VALUES (last_insert_id(),?, ? ,  '{ \"links\" : [ {\"url\": " + "\""  + url+ "\""  + " , \"author_link\": \"\" , \"author_name\": \"\" ,  \"art_name\": \"\" } ]}' "
				 + ", ? , ? , ? , ? , ? , ? , ? , ? , ? , ?  )";  
		
		PreparedStatement statInsertId = null;
		PreparedStatement statInsertSona = null; 
		Connection conn =null; 
		try 
		{
			conn = dataSource.getConnection(); 
			
			// Make atomic 
			conn.setAutoCommit(false);
			// Insert id first
			statInsertId = conn.prepareStatement(queryOne);
			statInsertId.execute(); 
			// Insert character 
			statInsertSona = conn.prepareStatement(queryTwo); 
			statInsertSona.setString(1, name);
			statInsertSona.setLong(2, userId);
			//statInsertSona.setString(3, url);
			statInsertSona.setLong(3, serverId);
			statInsertSona.setString(4, inKDM);
			statInsertSona.setString(5, inSP);
			statInsertSona.setString(6, inSimps);
			statInsertSona.setString(7, inShips);
			statInsertSona.setString(8, inKins);
			statInsertSona.setString(9, inWaifu);
			statInsertSona.setString(10, inFav);
			statInsertSona.setString(11, inGuess);
			statInsertSona.setString(12, inCollect);
			statInsertSona.execute(); 
			// Commit insertions 
			conn.commit(); 

		} catch (SQLException e)
		{
			try 
			{
				conn.rollback();
			} catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(statInsertId != null) { statInsertId.close(); } } catch(Exception e){} 
			try {  if(statInsertSona != null) { statInsertSona.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
		}
		
	}
	
	/* Method updates a users sona fields*/ 
	public void updateSona(long userId,long serverId, HashMap<String, String> newFields)
	{
		PreparedStatement stat = null; 
		Connection conn = null; 		
		String query=  null; 
		try 
		{
			query = "UPDATE sonas "
					+ " SET "
					+ " name = ? "
					+ ", url = ? "
					+ ", inKDM = ? "
					+ ", inSP = ? "
					+ ", inSimps = ?"
					+ ", inShips =? "
					+ ", inKins = ? "
					+ ", inWaifu = ? "
					+ ", inFav = ? "
					+ ", inGuess = ? "
					+ ", inCollect = ? "
					+ " WHERE user_Id = ? AND server_Id = ? "; 
			
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setString(1, newFields.get("name"));
			stat.setString(2, newFields.get("url"));
			stat.setString(3, newFields.get("kdm"));
			stat.setString(4, newFields.get("smashpass"));
			stat.setString(5, newFields.get("simps"));
			stat.setString(6, newFields.get("ships"));
			stat.setString(7, newFields.get("kins"));
			stat.setString(8, newFields.get("waifu"));
			stat.setString(9, newFields.get("favorite"));
			stat.setString(10, newFields.get("guess"));
			stat.setString(11, newFields.get("collect"));
			stat.setLong(12, userId);
			stat.setLong(13, serverId); 
			stat.execute(); 
		}
		catch(Exception e) 
		{
			e.printStackTrace(); 
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) {  conn.close(); } } catch(Exception e){} 
		}
	}
	
	
	public void removeAllSonas(Long  guildId)  
	{
		// rid of sonas
		String query = "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN sonas ON sonas.sona_Id = character_Ids.id\r\n"
				+ "WHERE sonas.server_Id = " + guildId ; 
		Statement stat = null; 
		Connection conn =null; 
		try 
		{		
			conn = dataSource.getConnection(); 
			stat = conn.createStatement(); 
			stat.execute(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	public void removeAllWaifus(Long idGuild) 
	{
		// rid of waifus
		String query = "DELETE FROM waifus " 
				+ "WHERE waifus.server_id = " +  idGuild ;
		Statement stat = null;
		Connection conn =null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			stat.execute(query); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
	}
	
	/* Get character ids from the data base*/ 
	public int getCharacterId(String characterName, long serverId )  
	{
		String queryCharacterId = "SELECT characters.char_Id FROM characters \r\n"
				+ "WHERE characters.name = ? \r\n"
				+ "UNION\r\n"
				+ "SELECT gameCharacter_Id FROM gameCharacters\r\n"
				+ "WHERE name = ? \r\n"
				+ "UNION\r\n"
				+ "SELECT sonas.sona_Id FROM sonas\r\n"
				+ "WHERE sonas.name =  ? AND server_Id = ? "  
				+ " UNION "
				+ "SELECT cusChar_Id FROM customCharacters " 
				+ "WHERE name = ? AND server_Id = ?"
				+ " LIMIT 1";
		

		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(queryCharacterId); 
			stat.setString(1, characterName);
			stat.setString(2, characterName);
			stat.setString(3, characterName);
			stat.setLong(4, serverId);
			stat.setString(5, characterName);
			stat.setLong(6, serverId);
			res =  stat.executeQuery();
			res.next();
			 value = Integer.valueOf(res.getString(1));
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}	
		
		return value;  
	}
	
	
	/* Get character ids of a from a playersCollection from the data base*/ 
	public int getCharacterIdFromPlayersCollect(String characterName, long userId ,long serverId )  
	{
		String queryCharacterId = "SELECT col_Id FROM playersCollection as chid \r\n"
				+ "INNER JOIN characters as ch ON ch.char_Id = chid.col_Id AND ch.name = ? AND chid.user_Id = ? AND chid.server_Id = ?\r\n"
				+ "UNION \r\n"
				+ "SELECT col_Id FROM playersCollection as chid \r\n"
				+ "INNER JOIN customCharacters as csh ON csh.cusChar_Id = chid.col_Id AND csh.name  = ? AND chid.server_Id =  csh.server_Id AND csh.server_Id = ? AND chid.user_Id = ? \r\n"
				+ "UNION \r\n"
				+ "SELECT col_Id FROM playersCollection as chid \r\n"
				+ "INNER JOIN gameCharacters as gch ON gch.gameCharacter_Id = chid.col_Id AND gch.name = ? AND chid.user_Id = ? AND chid.server_Id = ?\r\n"
				+ "UNION \r\n"
				+ "SELECT col_Id FROM playersCollection as chid \r\n"
				+ "INNER JOIN sonas as s ON s.sona_Id = chid.col_Id AND s.name = ? AND chid.server_Id = s.server_Id AND s.server_Id = ? AND chid.user_Id = ?";   
		

		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(queryCharacterId); 
			stat.setString(1, characterName);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			// 2nd command
			stat.setString(4, characterName);
			stat.setLong(5, serverId);
			stat.setLong(6, userId);
			// 3rd command
			stat.setString(7, characterName);
			stat.setLong(8, userId);
			stat.setLong(9, serverId);
			// 4th command 
			stat.setString(10, characterName);
			stat.setLong(11, serverId);
			stat.setLong(12, userId);
			res =  stat.executeQuery();
			res.next();
			 value = Integer.valueOf(res.getString(1));
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}	
		
		return value;  
	}
	
	// Method inserts a single character in the favorites table 
	public void insertFavorite(String name , Long  userId, Long serverId) throws Exception 
	{
		Character temp = this.requestSingleCharacter(name, serverId,GAMETYPE.FAVORITES, SETUPTYPE.LIGHT); 
		String query = ""; 
		
		
			 query = "INSERT INTO favorites (fav_Id, name ,user_Id,server_Id) " + 
						" VALUES( ? , ? , ? , ?  )"; 
		
			PreparedStatement stat = null;
			Connection conn =null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, temp.getId());
			stat.setString(2, name);
			stat.setLong(3, userId);
			stat.setLong(4, serverId);
			stat.execute(); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{		
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}

	}
	
	/* Return list of characters */ 
	public  ArrayList<Character> getFavoritesList(Long userId, Long serverId) 
	{
		
		CharacterSelection select = new CharacterSelection(); 
		ArrayList<Character > list = new ArrayList<Character>(); 
		String query = "SELECT fav_Id FROM favorites " 
				+ "WHERE user_Id = " + userId + 
				" AND server_Id = " + serverId + 
				 " ORDER BY timeCreated ASC ";
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();	
			res = stat.executeQuery(query); 
			res.next(); 
			do 
			{ 
				Long id = Long.valueOf(res.getString(1)); 

				Character temp = select.getCharacterById(id); 
				list.add(temp); 
			} 
		while(res.next()); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try { if (conn != null) {conn.close(); } } catch (Exception e) {}
		}
		
		return list; 
	}
	
	public String getTitleList(Long userId, Long serverId) 
	{
		String query = "SELECT * FROM favorites " 
				+ "WHERE user_id = ? AND server_id = ?";
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		String title = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			title =  new String(res.getString(5)); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return title; 
	}
	
	public Character getCharacterById(Long id)  
	{
		String query  = 
				 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character, characters.image_def , characters.image_urls FROM characters"
				+ " WHERE char_id = " + id
				+ " UNION"
				+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url, 0, null FROM gameCharacters"
				+ " WHERE gameCharacter_Id = " + id
				+ " UNION"
				+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url , 0 , null FROM sonas"
				+ " WHERE  sona_Id = " + id
				+ " UNION"
				+ " SELECT cusChar_Id, name, user_Id, url , 0 , null FROM customCharacters"
				+ " WHERE  cusChar_Id = " + id 
				+ "  LIMIT 1"; 
		
		Statement stat = null;
		ResultSet res = null;
		Connection conn =null; 
		CharacterFactory factory = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
			res.next(); 
			ArrayList<JSONObject> imgList = new ArrayList<JSONObject> (); 
			if (res.getString(6) != null) 
			{
				JSONObject jsonObj = new JSONObject(res.getString(6));
				JSONArray jarr = jsonObj.getJSONArray("links"); 
				for(int i =0 ; i < jarr.length() ; ++i) 
				{
					imgList.add(jarr.getJSONObject(i)); 
				}
			}
			
			factory = new CharacterFactory(Long.valueOf(res.getString(1)),res.getString(2), res.getString(3), res.getString(4), Integer.valueOf(res.getString(5)), imgList, SETUPTYPE.LIGHT); 
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 				
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		if(factory != null) 
		{
			return factory.getCharacter(); 
		} 
		else 
		{
			return null; 
		}
	}
	
	
	public String getCharacterNameById(Long id)  
	{
		String query  = 
				 " SELECT  characters.name FROM characters"
				+ " WHERE char_id = ?"
				+ " UNION"
				+ " SELECT name  FROM gameCharacters "
				+ " WHERE gameCharacter_Id = ?" 
				+ " UNION"
				+ " SELECT  sonas.name FROM sonas "
				+ " WHERE  sona_Id = ?"
				+ " UNION"
				+ " SELECT  name FROM customCharacters "
				+ " WHERE  cusChar_Id = ?"
				+ "  LIMIT 1"; 
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		String result = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 	
			stat.setLong(1, id);
			stat.setLong(2, id);
			stat.setLong(3, id);
			stat.setLong(4, id);
			res = stat.executeQuery();  
			res.next(); 
			result = res.getString(1); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result;  
	}
	
	
	
	/* Remove all characters of a user in the table */ 
	public void removeFavList(Long userId, Long serverId) 
	{
		String query = "DELETE FROM favorites\r\n"
				+ " WHERE favorites.user_Id = ? AND favorites.server_Id = ?"; 
		PreparedStatement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.execute();
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		} 
	}
	
	public void removeFavListArr(ArrayList<Long> userIds, Long serverId) 
	{
		PreparedStatement stat = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			String listStr =  userIds.toString().replace("[", "(");
			listStr = listStr.replace("]", ")"); 
			String query = "DELETE FROM favorites\r\n"
					+ "	WHERE favorites.user_Id IN " + listStr  + " "
					+ "    AND favorites.server_Id = "+ serverId + "";  
			stat = conn.prepareStatement(query); 
			stat.execute(); 
		}
		catch(SQLException e) 
		{
			e.printStackTrace(); 
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	
	/* Remove list from the database */ 
	public void removeFavCharacter(String name, Long userId, Long serverId) 
	{
		String query = "DELETE FROM favorites\r\n"
				+ " WHERE name = ?   AND user_Id  = ?  AND server_Id = ?"; 
		
		PreparedStatement stat = null;
		Connection conn = null; 
		try
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setString(1, name);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			stat.execute(); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 

		}
	}
	
	/* Search for favorites list from database */
	public boolean checkFavLimit(Long userId, Long serverId)  
	{
		String query = "SELECT COUNT(name) FROM favorites "
				+ "WHERE user_Id = ? AND server_Id = ?"; 
		
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			value = res.getInt(1); 
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 

		}			
		
		return (value < 10) ? true : (false); 
	}
	
	/*  Check user has a character in the server */ 
	public boolean checkFavList(Long userId, Long serverId)  
	{
		String query = "SELECT * FROM favorites " + 
				"WHERE user_Id = ? AND server_Id = ?";
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean result = false; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			result = res.next(); 
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
		
	}
	
	/* Check if name is available in a guild */ 
	public boolean isAvailable(String name, Long serverId) 
	{
		String query = "SELECT characters.name FROM characters \r\n"
					+ " WHERE characters.name = ? \r\n"
					+ " UNION\r\n"
					+ " SELECT name FROM gameCharacters \r\n"
					+ " WHERE name = ? \r\n"
					+ " UNION \r\n"
					+ " SELECT sonas.name FROM sonas\r\n"
					+ " WHERE sonas.name =  ? AND sonas.server_Id = ?"
					+ " UNION "
					+ " SELECT customCharacters.name FROM customCharacters\r\n"
					+ " WHERE customCharacters.name = ? AND customCharacters.server_Id = ? " ; 
	
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		boolean result = false; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 		
			stat = conn.prepareStatement(query); 
			stat.setString(1, name);
			stat.setString(2, name);
			stat.setString(3,name);
			stat.setLong(4,serverId);
			stat.setString(5,name); 
			stat.setLong(6, serverId);
			 res = stat.executeQuery();
			result = res.next(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}


	/* Insert orginal character into the custom character table */ 
	public void insertOrginalCharacter(String name, Long userId, String url , Long serverId, String inKDM, String inSP, String inSimps,String inShips, String
			inKins, String inWaifu, String inFav, String inGuess, String inCollect)  
	{
		String queryOne = " INSERT INTO character_Ids(id) VALUES(NULL)"; 
		String queryTwo =  " INSERT INTO customCharacters (cusChar_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess, inCollect) " +  
				 " VALUES (last_insert_id(),? , ? , " +"'{ \"links\" : [ {\"url\": " + "\""  + url + "\""  + " , \"author_link\": \"\" , \"author_name\": \"\" ,  \"art_name\": \"\" } ]}'" + 
				", ? , ? , ? ,?  , ? , ? , ? , ? , ? , ? )";  
		
		
		PreparedStatement statInsertId = null;
		PreparedStatement statInsertCharacter = null;

		Connection conn = null; 
		try 
		{
			
			// Connection
			conn = dataSource.getConnection();
			
			// Make atomic 
			conn.setAutoCommit(false);
			
			statInsertId = conn.prepareStatement(queryOne); 
			statInsertId.execute(); 
			// Insert Character next 
			statInsertCharacter = conn.prepareStatement(queryTwo); 
			statInsertCharacter.setString(1, name);
			statInsertCharacter.setLong(2, userId);
			statInsertCharacter.setString(3, url);
			statInsertCharacter.setLong(4, serverId);
			statInsertCharacter.setString(5, inKDM);
			statInsertCharacter.setString(6, inSP);
			statInsertCharacter.setString(7, inSimps);
			statInsertCharacter.setString(8, inShips);
			statInsertCharacter.setString(9, inKins);
			statInsertCharacter.setString(10, inWaifu);
			statInsertCharacter.setString(11, inFav);
			statInsertCharacter.setString(12, inGuess);
			statInsertCharacter.setString(13, inCollect);
			statInsertCharacter.execute(); 
			// Commit insertions 
			conn.commit();
		} 
		catch (SQLException e)
		{
				// roll back incase any errors
				try 
				{
					conn.rollback();
				} 
				catch (SQLException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(statInsertId != null) { statInsertId.close(); } } catch(Exception e){} 
			try {  if(statInsertCharacter != null) { statInsertCharacter.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
		}
		
	}
	
	public HashMap<String, String> getOCFields(String name, long userId, long serverId) 
	{
		PreparedStatement  stat =null; 
		Connection conn = null;
		ResultSet res  = null; 
		String query = "SELECT name , url , inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, "
				+ " inGuess, inCollect FROM customCharacters"
				+ " WHERE user_Id = ? AND server_Id = ? AND name = ?"; 
		HashMap<String, String> fields = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setString(3, name);
			res = stat.executeQuery();
			res.next(); 
			fields = new HashMap<String, String>(11); 
			fields.put("name", res.getString("name"));
			fields.put("url", res.getString("url"));
			fields.put("kdm", res.getString("inKDM")); 
			fields.put("smashpass", res.getString("inSP")); 
			fields.put("simps", res.getString("inSimps")); 
			fields.put("ships", res.getString("inShips"));
			fields.put("kins", res.getString("inKins") );
			fields.put("waifu", res.getString("inWaifu")); 
			fields.put("favorite", res.getString("inFav")); 
			fields.put("guess", res.getString("inGuess"));
			fields.put("collect", res.getString("inCollect")); 
		
		} 
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return fields; 
	}
	
	/* Method updates fields of the OC of given user */ 
	public void updateOC(String name, long userId, long serverId, HashMap<String, String> newFields) 
	{
		PreparedStatement stat = null; 
		Connection conn = null; 
		
		String query = null; 
		try 
		{
			query = "UPDATE customCharacters "
					+ " SET "
					+ " name = ? "
					+ ", url = ? "
					+ ", inKDM = ? "
					+ ", inSP = ? "
					+ ", inSimps = ?"
					+ ", inShips =? "
					+ ", inKins = ? "
					+ ", inWaifu = ? "
					+ ", inFav = ? "
					+ ", inGuess = ? "
					+ ", inCollect = ? "
					+ " WHERE user_Id = ? AND server_Id = ? AND name = ? "; 
			
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setString(1, newFields.get("name"));
			stat.setString(2, newFields.get("url"));
			stat.setString(3, newFields.get("kdm"));
			stat.setString(4, newFields.get("smashpass"));
			stat.setString(5, newFields.get("simps"));
			stat.setString(6, newFields.get("ships"));
			stat.setString(7, newFields.get("kins"));
			stat.setString(8, newFields.get("waifu"));
			stat.setString(9, newFields.get("favorite"));
			stat.setString(10, newFields.get("guess"));
			stat.setString(11, newFields.get("collect"));
			stat.setLong(12, userId);
			stat.setLong(13, serverId); 
			stat.setString(14, name); 
			stat.execute(); 
		}
		catch(SQLException  e)
		{
			e.printStackTrace(); 
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Check how many characters the user has in this server */ 
	public boolean checkOCLimit(Long userId, Long serverId)  
	{
		String query = "SELECT COUNT(name) FROM customCharacters "
				+ "WHERE customCharacters.user_Id = ? AND " + "customCharacters.server_Id = ?"; 
		
		PreparedStatement stat = null;
		ResultSet res = null ;
		Connection conn = null;
		int value = -1;
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1,userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery();  
			res.next(); 
			value = res.getInt(1); 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){}
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return (value < 10) ? true : (false); 

	}
	
	/* Remove custom characters from the table */ 
	public void removeCustomCharacter(String name, Long userId, Long serverId) 
	{
		String query =  "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_Ids.id\r\n"
				+ "WHERE customCharacters.user_Id = ? AND customCharacters.server_Id = ? AND customCharacters.name = ?"; 
		
		
		PreparedStatement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2,serverId);
			stat.setString(3, name);
			stat.execute(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
	}

	/* Search OC's name on the table */  
	public boolean searchOC(String name, Long userId, Long serverId)  {
		
		String query = "SELECT * FROM customCharacters "  
					+ "WHERE user_Id = ? AND server_Id = ? AND name = ? " ; 
		PreparedStatement stat = null; 
		ResultSet res = null;
		boolean result = false;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setString(3, name);
			res = stat.executeQuery();
			result = res.next(); 
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 

		}
		return result; 
	}

	/*
	 * Return a list of OC characters from a user in a server 
	 * */ 
	public ArrayList<Character> getOCList(Long userId, Long serverId) 
	{
		String query = "SELECT cusChar_Id , name, url  FROM customCharacters " + 
				"WHERE user_Id = "  + userId + " AND server_Id = " + serverId  + 
				" ORDER BY timeCreated DESC";  
	 Statement stat = null ;
	 ResultSet res = null;
	 Connection conn = null; 
	 ArrayList<Character> list = new ArrayList<Character>(); 
	try
	{
		conn = dataSource.getConnection(); 
		stat = conn.createStatement();
		res = stat.executeQuery(query); 
		
		// Check if false 
		if(!res.next()) 
		{
			list =  null; 
		}
		else
		{ 
			CharacterFactory factory = null; 
			do 
			{
				factory = new CharacterFactory(Long.valueOf( res.getString(1)), res.getString(2), "OC" ,res.getString(3), 0, null, SETUPTYPE.LIGHT); 
				list.add(factory.getCharacter()); 
			}
			while(res.next()); 
		}
	}
	catch (SQLException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	finally 
	{
		try {  if(res != null) { res.close(); } } catch(Exception e){} 
		try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
		try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
	}
		return list;  
	}

	/* Getting single oc from a users' list */  
	public Character getOC(String characterName, Long userId, Long serverId)  
	{
		
		String query = "SELECT cusChar_Id, name, url FROM customCharacters " 
				+ "WHERE user_Id = ? AND server_Id = ? AND name = ?"; 
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		Character result = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setString(3, characterName);
			res = stat.executeQuery(); 
			if(res.next()) 
			{
				CharacterFactory factory = new CharacterFactory(Long.valueOf( res.getString(1)), res.getString(2), "OC" ,res.getString(3), 0, null, SETUPTYPE.LIGHT); 
				result =  factory.getCharacter(); 
			}
			
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
	}

	/* Check if User has any Ocs */ 
	public boolean searchAllUserOcs(Long userId, Long serverId)  {
		String query = "SELECT * FROM customCharacters " 
					+ "WHERE user_Id = ? AND server_Id = ?";  
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean result = false; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			result =  res.next(); 

		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}

	/* Removes all ocs from a user */ 
	public void removeAllOcs(Long userId, Long serverId)  {
		
		
		String query = "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_Ids.id \r\n"
				+ " WHERE customCharacters.server_Id = ? AND customCharacters.user_Id = ?";
		
		PreparedStatement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, userId);
			stat.execute(); 
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
	}
	
	public void removeAllOcsList(ArrayList<Long> userIds, Long serverId) 
	{
		PreparedStatement stat = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			String listStr =  userIds.toString().replace("[", "(");
			listStr = listStr.replace("]", ")"); 
			String query = "DELETE character_Ids FROM character_Ids "
					+ "	INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_Ids.id "
					+ "	WHERE customCharacters.user_Id IN " + listStr 
					+ "    AND customCharacters.server_Id = " + serverId + " ";  
			stat = conn.prepareStatement(query); 
			stat.execute(); 
		}
		catch(SQLException e) 
		{
			e.printStackTrace(); 
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Update favorite list */ 
	public void changeFavTitle(String title, Long userId, Long serverId)  
	{
		String query = " UPDATE favorites SET title = ? " +  
				" WHERE user_Id = ? AND server_Id = ?" ;  
		
		PreparedStatement stat = null;
		Connection conn = null;
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setString(1, title);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			stat.execute(); 
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}

	// Return list of names in a users favorite list
	public ArrayList<String> getFavListNames(Long userId, Long serverId) 
	{
		ArrayList<String> names = new ArrayList<String>(); 
		String query = " SELECT name FROM favorites " + 
				"WHERE user_Id = ?  AND " + "server_Id = ? " + 
				" ORDER BY timeCreated ASC"; 
		PreparedStatement stat = null;
		ResultSet  res = null; 
		Connection conn = null ;
		try
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next();	
			do
			{
				names.add(res.getString(1));
			}
			while(res.next()); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return names; 
	}


	public void removeAllOcsInGuild(Long guildId) 
	{
		
		Statement stat = null; 
		String query = "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_Ids.id \r\n"
				+ " WHERE customCharacters.server_Id = " + guildId;
		Connection conn = null; 
		
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			stat.execute(query); 
		}
		catch ( SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}


	public void removeFavListGuild(Long guildId) {
		Statement stat = null; 
		String query = "DELETE FROM favorites " 
				+ " WHERE server_Id = " + guildId;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			stat.execute(query);
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Search for collection list from database */
	public boolean checkCollectLimit(Long userId, Long serverId)  
	{
		String query = "SELECT COUNT(col_Id) FROM playersCollection "
				+ "WHERE user_Id =  ?  AND  server_Id =  ? "; 
		
		PreparedStatement stat = null;
		ResultSet res = null;
		Connection conn =null; 
		int value = -1; 
		try
		{	
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery();
			res.next(); 
			value = res.getInt(1); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}			
		return (value >= 30) ? true : (false); 
	}
	
	
	/* Return list of characters */ 
	public ArrayList<String> getCollectionListNames(Long userId, Long serverId) 
	{
		ArrayList<String > list = new ArrayList<String>(); 
		long id =-1; 
		String temp = null; 
		String queryCollectList = "SELECT col_Id FROM playersCollection " 
				+ " WHERE user_Id = ? AND server_Id = ? ORDER BY timeCreated DESC ";
		
		String queryGetCharacter  = null; 
		PreparedStatement stat = null;
		ResultSet res = null;
		ResultSet res2 =null; 
		Connection conn = null; 
		PreparedStatement stat2 = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(queryCollectList);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			do 
			{ 
				id = Long.valueOf(res.getString(1)); 
				
				
				 queryGetCharacter  = 
						 " SELECT  characters.name FROM characters"
						+ " WHERE char_id = ?" 
						+ " UNION" 
						+ " SELECT name  FROM gameCharacters "
						+ " WHERE gameCharacter_Id = ?"
						+ " UNION"
						+ " SELECT  sonas.name FROM sonas "
						+ " WHERE  sona_Id = ?" 
						+ " UNION"
						+ " SELECT  name FROM customCharacters "
						+ " WHERE  cusChar_Id = ?";  
				
				stat2 = conn.prepareStatement(queryGetCharacter); 
				stat2.setLong(1, id);
				stat2.setLong(2, id);
				stat2.setLong(3, id);
				stat2.setLong(4, id);
				res2 = stat2.executeQuery(); 
				res2.next(); 
				temp = res2.getString(1); 
				list.add(temp); 
			} 
			while(res.next()); 
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e) {} 
			try {  if(res2 != null) { res2.close(); } } catch(Exception e) {} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e) {} 
			try {  if(stat2 != null) { stat2.close(); } } catch(Exception e) {} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e) {} 
		}

		return list; 
	}

	

	/* Method returns if user reached limit of turns */ 
	public boolean getPlayerRollsLimit(long userId, long serverId) {
		
		
		String query =  "SELECT rolls + consumable_rolls FROM unq_users "
				+ " WHERE user_Id = ? AND server_Id = ?";  
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1 ; 
		
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			 value = res.getInt(1); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
			return (value < 1) ? true : (false); 
	}
	
	
	/* Method returns amount of rolls a player has left */
	public ArrayList<String> getPlayerRolls(long userId, long serverId) 
	{
		ArrayList<String> result= new ArrayList<String>();  
		
		String query = "SELECT rolls,consumable_rolls FROM unq_users "
				+ "WHERE user_Id = ? AND server_Id = ? ";
		
		PreparedStatement stat = null; 
		ResultSet res = null; 
		Connection conn = null; 
		
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			result.add(  res.getString(1));
			result.add(  res.getString(2));
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
	}

	

	
	/* Method inserts character into characterId into players Collection table and decrements turn in playerInCollect table and initalizeing time */ 
	public void claimCharacter(Long characterId, long userId, long serverId) 
	{
		// 2 queries, update and insert
		String queryOne = "UPDATE unq_users "
				+ "SET consumable_claims = ( CASE WHEN consumable_claims > 0 AND claims <= 0  "
				+ "THEN consumable_claims - 1 ELSE consumable_claims END),"
				+ " claims = (CASE WHEN claims > 0 THEN claims - 1 ELSE claims END) "
				+ " WHERE server_id = " + serverId + " AND user_id = " + userId;  
		
		String quertyTwo = " INSERT INTO playersCollection (col_Id , user_Id , server_Id ) \r\n"
				+ " SELECT * FROM ( SELECT " + characterId + " ,  " + userId +" , " + serverId +" ) AS tmp "
				+ " WHERE NOT EXISTS "
				+ " ( "
				+ "	SELECT col_Id,server_Id FROM playersCollection \r\n"
				+ "    WHERE col_Id = " + characterId + " AND server_Id = " + serverId 
				+ "  ) LIMIT 1; " ;
		PreparedStatement updateStat = null ;
		Connection conn = null; 
		try
		{
			conn = dataSource.getConnection(); 
			updateStat = conn.prepareStatement(queryOne); 
			updateStat.addBatch(queryOne); 
			updateStat.addBatch(quertyTwo);
			updateStat.executeBatch();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} 
		finally // Make sure statement is closed
		{
			try {  if(updateStat != null) { updateStat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){}
		}
	}

	/*Get the time needed till the player can colllect again  */ 
	public String getCollectTime() 
	{
		String query = "SELECT LAST_EXECUTED FROM INFORMATION_SCHEMA.events "
				+ " WHERE EVENT_NAME = \"claim_Reset_Event\""; 
		PreparedStatement stat = null;
		ResultSet res = null;
		String result = ""; 
		Connection conn = null; 
		try
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			res = stat.executeQuery(); 
			res.next(); 
			Date now = new Date(); 
			Date end = res.getTimestamp(1, Calendar.getInstance(TimeZone.getTimeZone("GMT")));  
			
			long millDelta = ( end.getTime() + ( 3600000L * 2 ) )  - now.getTime(); 
			Long min =  millDelta / (60000) % 60; 
			Long hour = millDelta / (3600000); 
			
			if(hour == 1L) 
			{
				result = hour.toString() + " hour and " + min.toString() + " minutes"; 
			}
			else if(hour == 0L) 
			{
				result = min.toString() + " minutes"; 
			}
			else 
			{
				result = hour.toString() + " hours and " + min.toString() + " minutes"; 
			}
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		} 
		finally
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result;
	}

	/* Time till turns are reset */ 
	public String getRollRestTime()  {
		String query = "SELECT LAST_EXECUTED FROM INFORMATION_SCHEMA.events " 
				+ " WHERE EVENT_NAME = \"turn_Reset_Event\""; 
		PreparedStatement stat = null;
		ResultSet res = null; 
		String result= ""; 
		Connection conn = null ;
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			res = stat.executeQuery(); 
			res.next(); 
			Date end = res.getTimestamp(1, Calendar.getInstance(TimeZone.getTimeZone("GMT")));  
			Date now = new Date(); 
			long millDelta = ( end.getTime() + 3600000L) - now.getTime(); 
			Long min =  millDelta / (60000) % 60; 
			Long hour = millDelta / (3600000); 
			
			if(hour == 1L) 
			{
				result = hour.toString() + " hour and " + min.toString() + " minutes"; 
			}
			else if(hour == 0L) 
			{
				result = min.toString() + " minutes"; 
			}
			else 
			{
				result = hour.toString() + " hours and " + min.toString() + " minutes"; 
			}
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
		
	}

	/* Get amount of claims the player has in string  */ 
	public ArrayList<String> getClaims(long userId, long serverId)  {
		String query = "SELECT claims, consumable_claims FROM unq_users " 
				+ " WHERE user_Id = ? AND   server_Id  = ? ";
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		String claims = null; 
		String cons_claims = null; 
		ArrayList<String> result = new ArrayList<String>(); 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery();
			res.next(); 
			claims = res.getString(1);
			cons_claims = res.getString(2);
			result.add(claims); 
			result.add(cons_claims); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
	}
	
	
	public int getClaimsAmount(long userId, long serverId)  {
		String query = "SELECT claims, consumable_claims FROM unq_users " 
				+ " WHERE user_Id = ? AND   server_Id  = ? ";
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int  claims = 0; 
		int cons_claims = 0; 
		int result = 0; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery();
			res.next(); 
			claims = res.getInt(1) ;
			cons_claims = res.getInt(2);
			result = claims + cons_claims; 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
	}

	
	
	/* Inserts user into playersInCollect if they haven't been already */ 
	public void insertUserIntoCollect(long userId, long serverId)  {
		String query = "INSERT IGNORE playersInCollect (user_Id, server_Id) " + 
				" VALUES ( ? , ? )";
		PreparedStatement stat = null;
		Connection conn =null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.execute();
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 finally 
		 {
				try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
				try { if (conn != null) {conn.close();} } catch (Exception e ) {} 
		 }
	}

	/* Decrement player roll in playerInCollect table */ 
	public void decPlayerRoll(long userId, long serverId) 
	{
		String query = "UPDATE unq_users "
				+ " SET consumable_rolls = ( CASE WHEN consumable_rolls > 0 AND rolls <= 0  THEN consumable_rolls - 1 ELSE consumable_rolls END),  "
				+ " rolls = (CASE WHEN rolls > 0 THEN rolls - 1 ELSE rolls END) "
				+ " WHERE server_id = ? AND user_id = ?;";  
		PreparedStatement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection();
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, userId);
			stat.execute(); 
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
	}

	/* Check if character has already been claimed in the server */ 
	public Long hasBeenClaimed(long charId, long serverId ) 
	{
		String query = "SELECT user_Id FROM playersCollection " + 
				"WHERE server_Id = ? AND col_Id = ? " ; 
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Long result = (long) -1;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.setLong(2, charId);
			res = stat.executeQuery(); 
			
			if ( res.next())
			{  
				result = res.getLong(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 finally 
		 {
				try {  if(res != null) { res.close(); } } catch(Exception e){} 
				try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		 }
		return result; 
	}


	public long getCollectedCharPlayerId(Long charId, long serverId)  {
		
		
		
		String query = "SELECT user_Id FROM playersCollection " + 
				"WHERE server_Id = ?  AND col_Id = ?"; 
		
		PreparedStatement stat  = null; 
		ResultSet res = null; 
		Connection conn = null; 
		long value = -1; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, charId);
			res = stat.executeQuery(); 
			res.next(); 
			value = res.getLong(1); 
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return value ; 
	}


	public boolean hasCollectList(long userId, long serverId)  {
		String query = "SELECT * FROM playersCollection " + 
				"WHERE server_Id = ? AND user_Id = ?"; 
		
		PreparedStatement stat = null;
		ResultSet res = null;
		Connection conn = null; 
		boolean result = false; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, userId);
			res = stat.executeQuery(); 
			result = res.next(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 		
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}


	


	/* Remove collect character of the given name */ 
	public void removeCollectCharacter(String characterName , long userId, long serverId)  
	{
		long id = this.getCharacterId(characterName,serverId); 
		String query = "DELETE FROM playersCollection " + 
				" WHERE col_Id = ?  AND user_Id = ? AND server_Id = ?"; 
		PreparedStatement stat = null;
		Connection conn = null ; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, id);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			stat.execute(); 
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}

	/* Check playter has a character in their collection */ 
	public boolean searchCharacterCollectList(String characterName, long userId, long serverId) 
	{
		
		long id = this.getCharacterId(characterName,serverId);
		String query = "SELECT * FROM playersCollection " + 
				" WHERE col_Id = ?  AND user_Id = ? AND server_Id = ?";  
		PreparedStatement stat = null;
		boolean result = false; 
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, id);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			res = stat.executeQuery();
			result = res.next(); 
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}

	/* Return list of characters of a user in collect game */ 
	public ArrayList<String> getCollectNamesOfUser(long userId, long serverId) 
	{
		String query = " SELECT col_Id FROM playersCollection " 
					+ " WHERE user_Id = ? AND server_Id = ? "; 
		
		ArrayList<String > names = new ArrayList<String>(); 
		PreparedStatement stat = null;
		ResultSet res= null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery();
			res.next(); 
			do 
			{
				names.add ( getCharacterNameById(res.getLong(1))) ; 
			}
			while(res.next()); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 

		}
		return names; 
	}


	/* Void method swap characters between users in Collect game */ 
	public void swapUserCollectible(long trader, long tradee, long traderCharacterId, long tradeeCharacterId, long serverId) 
	{
		
		String queryTrader = "UPDATE playersCollection " 
				+ " SET col_Id = "+ tradeeCharacterId +" WHERE col_Id = " + traderCharacterId +"  AND user_Id = " + trader +
				"  AND server_Id = " + serverId; 
		
		String queryTradee = "UPDATE playersCollection " 
				+ " SET col_Id = " + traderCharacterId +" WHERE col_Id = " + tradeeCharacterId +" AND user_Id = " + tradee +
				" AND server_Id = " + serverId; 
		
		PreparedStatement statTrader = null;
		PreparedStatement statTradee = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			conn.setAutoCommit(false);  // Make atomic
			statTrader = conn.prepareStatement(queryTrader);
			statTradee = conn.prepareStatement(queryTradee); 
			statTrader.addBatch(queryTrader);
			statTradee.addBatch(queryTradee);
			statTrader.executeBatch();
			statTradee.executeBatch(); 
			conn.commit();	// Commit
		} 
		catch (SQLException e) 
		{
			try 
			{
				//Undue any updates on failure
				conn.rollback();
			} catch (SQLException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			e.printStackTrace(); 
			
		} 
		finally 
		{
			
			try {  if(statTrader != null) { statTrader.close(); } } catch(Exception e){} 
			try {  if(statTradee != null) { statTradee.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.setAutoCommit(true);  conn.close(); } } catch(Exception e){} 
		}
		
	}

	/* Void method sets the collect character as default image by updating its date */ 
	public void setDefCollectCharacter(long charId, long userId, long serverId)  
	{
		String query = "UPDATE playersCollection " 
					+ " SET timeCreated = CURRENT_TIMESTAMP " 
					+ " WHERE col_Id = ? "
					+ " AND  user_Id = ?  "
					+ " AND  server_Id = ? "; 
		PreparedStatement stat = null;
		Connection conn = null;
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, charId);
			stat.setLong(2, userId);
			stat.setLong(3,serverId);
			stat.execute(); 

		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{	
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}

	/* Method searches user has a character in the collect list */ 
	public boolean getSearchCharIdSelect(int characterId, long userId, long serverId)  
	{
		String query = "SELECT * FROM playersCollection "
					+ " WHERE col_Id = " + characterId + " AND server_Id = " + serverId + " AND user_Id = " + userId; 
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean value = false; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();	
			res = stat.executeQuery(query);
			value = res.next();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return value; 
	}

	/* Add character to wish list */ 
	public void addToWishList(long charId, long userId, long serverId) 
	{
		String query = "INSERT INTO wishList (wish_Id, user_Id , server_Id)" 
					+ "  VALUES ( ? , ? , ? )";  
		PreparedStatement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, charId);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			stat.execute(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){}
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Method checks limit and returns if reached */ 
	public boolean wishListLimit(long userId, long serverId)  
	{
		
		String query = "SELECT COUNT(wish_Id) FROM wishList "
				+ "WHERE user_Id = ? AND  server_Id = ? "; 
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			value = res.getInt(1); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return (value < 5) ? true : (false); 

	}

	/* Return users wish list! */ 
	public ArrayList<Character> getWishList(long userId, long serverId) 
	{
		CharacterSelection select = new CharacterSelection(); 
		ArrayList<Character > list = new ArrayList<Character>(); 
		String query = "SELECT wish_Id FROM wishList " 
				+ "WHERE user_Id = ? " + 
				" AND server_Id = ?" ; 
		PreparedStatement stat = null;
		ResultSet res = null ; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			do 
			{ 
				Long id = Long.valueOf(res.getString(1)); 
				Character temp = select.getCharacterById(id); 
				list.add(temp); 
			} 
			while(res.next()); 
		
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return list; 
	}

	/* Check user has any character in wishlist table */ 
	public boolean hasWishList(long userId, long serverId) 
	{
		String query = "SELECT * FROM wishList " + 
				"WHERE server_Id = ? AND user_Id = ?"; 
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean value = false; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 	
			stat.setLong(1, serverId);
			stat.setLong(2, userId);
			res = stat.executeQuery(); 
			value = res.next(); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
			return value; 
	}

	/* Check player has this character in wish list*/ 
	public boolean searchWishList(String characterName, long userId, long serverId) 
	{
		long id = this.getCharacterId(characterName,serverId);
		String query = "SELECT * FROM wishList " + 
				" WHERE wish_Id = ?  AND user_Id = ?  AND server_Id = ?";  
		PreparedStatement stat = null;
		ResultSet res = null; 
		boolean result = false; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, id);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			res = stat.executeQuery();
			result = res.next(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 	
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}

	/* Remove character from wish list*/ 
	public void removeWish(String characterName, long userId, long serverId)  
	{
		long id = this.getCharacterId(characterName,serverId);

		String query = "DELETE FROM wishList " + 
				" WHERE wish_Id = ?  AND user_Id = ?  AND server_Id = ?" ;  
		PreparedStatement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, id);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			stat.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	
	}


	public ArrayList<String> getWishListNames(long userId, long serverId) 
	{
		String query = " SELECT wish_Id FROM wishList" 
				+ " WHERE user_Id = ?  AND server_Id = ? "; 
	
		ArrayList<String > names = new ArrayList<String>(); 
		PreparedStatement stat = null;
		ResultSet res = null; 
		Connection conn = null ;
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery();
			res.next(); 
			do 
			{
				names.add ( getCharacterNameById(res.getLong(1))) ; 
			}
			while(res.next()); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return names; 
	
	}

	/* Return list of users that want a specific character */ 
	public ArrayList<String> getUsersOfWish(long charId, long serverId) 
	{
		
		String query = "SELECT user_Id FROM wishList " 
					+ " WHERE server_Id =  ? AND wish_Id = ?"; 
		
		PreparedStatement stat = null;
		ResultSet res = null; 
		ArrayList<String> userIds = null; 
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, serverId);
			stat.setLong(2, charId);
			res = stat.executeQuery(); 
			
			// return filled list
			if(res.next()) 
			{
				userIds = new ArrayList<String>(); 
				
				do 
				{
					userIds.add("<@" + res.getString(1) + ">"); 
				}
				while(res.next()); 
				

			}
			else // return an empty list
			{
				userIds = new ArrayList<String>(); 
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 

		}
		return userIds; 
	}

	/* Delete users wish list*/ 
	public void clearWishList(long userId, long serverId) 
	{
		String query = "DELETE FROM wishList "
				+ " WHERE user_Id = ?" + " AND server_Id = ?"; 
		PreparedStatement stat = null;
		Connection conn = null ; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.execute(); 
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){}
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}

	/* Method will delete all players and corresponding collection and wish list of a guild */ 
	public void removeAllPlayersCollectInGuild(long serverId) 
	{
		String queryOne = " DELETE FROM playersInCollect "
				+ "WHERE server_Id = " + serverId;
		String queryTwo = "DELETE FROM playersCollection "
				+ "WHERE server_Id = " + serverId; 
		String queryThree = "DELETE FROM wishList "
				+ "WHERE server_Id = " + serverId; 
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			conn.setAutoCommit(false); // atomic
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.addBatch(queryOne); 
			stat.addBatch(queryTwo);
			stat.addBatch(queryThree);
			stat.executeBatch(); 
			conn.commit(); // save deletes
		} 
		catch (SQLException e) 
		{
			try 
			{
				//Undue any deletes on failure
				conn.rollback();
			} 
			catch (SQLException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
		}
		
	}

	/* Method will remove a particular user from tables playersInCollect , playersCollection and wishlist*/ 
	public void removeCollect(Long userId, Long serverId) 
	{
		String queryOne = " DELETE FROM playersInCollect "
				+ "WHERE server_Id = " + serverId + " AND user_Id = " + userId;
		String queryTwo = "DELETE FROM playersCollection "
				+ "WHERE server_Id = " + serverId + " AND user_Id = " + userId;
		String queryThree = "DELETE FROM wishList "
				+ "WHERE server_Id = " + serverId + " AND user_Id = " + userId;
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			stat.addBatch(queryOne); 
			stat.addBatch(queryTwo);
			stat.addBatch(queryThree);
			
		
			stat.executeBatch(); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Method will remove a particular user from tables playersInCollect , playersCollection and wishlist*/ 
	public void removeCollectList(ArrayList<Long> userIds, Long serverId) 
	{
		
		String listStr =  userIds.toString().replace("[", "(");
		listStr = listStr.replace("]", ")"); 
		String queryOne = "DELETE FROM playersInCollect "
				+ "WHERE user_Id IN " + listStr + " " 
				+ "	 AND server_Id = " + serverId ;
		String queryTwo = "DELETE FROM playersCollection "
				+ "	WHERE user_Id IN " + listStr + " " 
				+ "    AND server_Id = " + serverId;
		String queryThree = "DELETE FROM wishList "
				+ "	WHERE user_Id IN " + listStr + " "
				+ "	AND server_Id = " + serverId;
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			stat.addBatch(queryOne); 
			stat.addBatch(queryTwo);
			stat.addBatch(queryThree);
			stat.executeBatch(); 
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	


	/* Get all user ids from the server */ 
	public ArrayList<Long> getServerUsers(long serverId )  
	{
		String query = "SELECT DISTINCT user_Id FROM sonas \r\n"
				+ "  WHERE sonas.server_Id = " + serverId +" \r\n"
				+ "  UNION SELECT DISTINCT user_Id FROM favorites\r\n"
				+ "  WHERE favorites.server_Id = "+ serverId +" \r\n"
				+ "  UNION SELECT DISTINCT user_Id FROM customCharacters\r\n"
				+ "  WHERE customCharacters.server_Id = "+ serverId +" \r\n"
				+ "  UNION SELECT DISTINCT user_Id FROM playersInCollect \r\n"
				+ "  WHERE playersInCollect.server_Id = "+serverId+" \r\n"
				+ "  UNION	SELECT DISTINCT user_Id FROM playersCollection \r\n"
				+ "  WHERE playersCollection.server_Id = "+serverId+" ; "; 
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		ArrayList<Long> users = null;
		
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			
			res = stat.executeQuery(query); 
			
			users = new ArrayList<Long>(); 
			if(res.next())
			{
				do 
				{ 
					users.add(res.getLong(1)); 
				}
				while(res.next()); 
			}
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return users;
		
	}
	
	/* Get all servers present in the database */
	public Map<Long, Long> getAllServersDB()
	{
		Connection conn = null; 
		Statement stat = null;
		Statement stat2 = null;
		Statement stat3 = null;
		Statement stat4 = null;
		Statement stat5 = null;
		Statement stat6 = null;
		Statement stat7 = null;
		ResultSet res = null;
		ResultSet res2 = null; 
		ResultSet res3 = null; 
		ResultSet res4 = null; 
		ResultSet res5 = null; 
		ResultSet res6 = null;
		ResultSet res7 = null;
		Map<Long, Long> dbServers = new TreeMap<>(); 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.createStatement();
			stat2 = conn.createStatement(); 
			stat3 = conn.createStatement();
			stat4 = conn.createStatement();
			stat5 = conn.createStatement();
			stat6 = conn.createStatement();
			stat7 = conn.createStatement(); 
			String query = "SELECT server_Id FROM playersInCollect";
			res = stat.executeQuery(query); 
			query = "SELECT server_Id FROM playersCollection"; 
			res2 = stat2.executeQuery(query); 
			query = "SELECT server_Id FROM sonas";
			res3 = stat3.executeQuery(query); 
			query = "SELECT server_Id FROM waifus"; 
			res4 = stat4.executeQuery(query); 
			query = "SELECT server_Id FROM wishList"; 
			res5 = stat5.executeQuery(query); 
			query = "SELECT server_Id FROM customCharacters"; 
			res6 = stat6.executeQuery(query); 
			query = "SELECT server_Id FROM favorites"; 
			res7 = stat7.executeQuery(query); 
			// Now add every thing to RB tree 
			
			if(res.next()) 
			{
				do
				{
					dbServers.put(res.getLong(1), res.getLong(1)); 
				}
				while(res.next()); 
			}
			if(res2.next())
			{
				do
				{
					dbServers.put(res2.getLong(1), res2.getLong(1)); 
				}
				while(res2.next()); 
			}
			if(res3.next()) 
			{
				do
				{
					dbServers.put(res3.getLong(1), res3.getLong(1)); 
				}
				while(res3.next()); 
			}
			if(res4.next())
			{
				do
				{
					dbServers.put(res4.getLong(1), res4.getLong(1)); 
				}
				while(res4.next()); 
			}	
			if(res5.next()) 
			{
				do
				{
					dbServers.put(res5.getLong(1), res5.getLong(1)); 
				}
				while(res5.next()); 
			}
			if(res6.next())
			{
				do
				{
					dbServers.put(res6.getLong(1), res6.getLong(1)); 
				}
				while(res6.next()); 
			}
			if(res7.next())
			{
				do
				{
					dbServers.put(res7.getLong(1), res7.getLong(1)); 
				}
				while(res7.next()); 
			}
		}
			
			
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res5 != null) { res5.close(); } } catch(Exception e){} 
			try {  if(res4 != null) { res4.close(); } } catch(Exception e){} 
			try {  if(res3 != null) { res3.close(); } } catch(Exception e){} 
			try {  if(res2 != null) { res2.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(stat2 != null) { stat.close(); } } catch(Exception e){} 
			try {  if(stat3 != null) { stat.close(); } } catch(Exception e){} 
			try {  if(stat4 != null) { stat.close(); } } catch(Exception e){} 
			try {  if(stat5!= null) { stat.close(); } } catch(Exception e){} 
			try {  if(stat6 != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		
		
		return dbServers;
	}
	


	/* Swap rank of characters in the favorites list */ 
	public void favSwapCharacter(String characterOne, String characterTwo, long userId, long serverId) throws Exception   
	{
		long charOne = this.getCharacterId(characterOne, serverId); 
		long charTwo = this.getCharacterId(characterTwo, serverId); 
		
		// Check invalid character 
		if(charOne == -1 || charTwo == -1) 
		{
			String errorMsg = ""; 
			if(charOne == -1) 
			{
				errorMsg = characterOne; 
			} 
			else 
			{
				errorMsg = characterTwo; 
			}
			throw new Exception("Invalid character entered : " + errorMsg); 
		}
		
		 String queryOne = "SELECT timeCreated FROM favorites "
		 		+ " WHERE fav_Id = ? AND user_Id = ? AND server_Id = ?"; 
		 String queryTwo = "SELECT timeCreated FROM favorites " 
		 		+ " WHERE fav_Id = ? AND user_Id = ? AND server_Id = ?";
		 
		 PreparedStatement  statOne = null;
		 PreparedStatement  statTwo = null;
		 PreparedStatement statOneUpdate = null; 
		 PreparedStatement statTwoUpdate = null; 

		 ResultSet res = null;
		 ResultSet res2 = null; 
		 Connection conn = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			conn.setAutoCommit(false); // make atomic 
			statOne = conn.prepareStatement(queryOne); 
			statOne.setLong(1, charOne);
			statOne.setLong(2, userId);
			statOne.setLong(3, serverId);
			res = statOne.executeQuery(); 
			res.next(); 
			
			Timestamp timeOne = res.getTimestamp(1); // first character time 
			statTwo = conn.prepareStatement(queryTwo); 
			statTwo.setLong(1, charTwo);
			statTwo.setLong(2, userId);
			statTwo.setLong(3, serverId);
			res2 = statTwo.executeQuery(); 
			res2.next(); 
			Timestamp timeTwo = res2.getTimestamp(1); // second character time  
			
			// First update
			queryOne = " UPDATE favorites " 
				 + " SET timeCreated =  ? " + 
				 " WHERE fav_Id = ? AND user_Id = ? AND server_Id = ?"; 
		 
			statOneUpdate = conn.prepareStatement(queryOne); 
			statOneUpdate.setTimestamp(1, timeTwo);
			statOneUpdate.setLong(2, charOne);
			statOneUpdate.setLong(3, userId);
			statOneUpdate.setLong(4, serverId);
			
			// 2nd update 
			queryTwo = " UPDATE favorites " 
				 + " SET timeCreated = ?"+ 
				 " WHERE fav_Id = ? AND user_Id = ? AND server_Id = ?";  
			statTwoUpdate = conn.prepareStatement(queryTwo); 
			statTwoUpdate.setTimestamp(1, timeOne);
			statTwoUpdate.setLong(2, charTwo);
			statTwoUpdate.setLong(3, userId);
			statTwoUpdate.setLong(4, serverId);
			// now swap
			statOneUpdate.execute(); 
			statTwoUpdate.execute(); 
			conn.commit(); // commit updates 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			try
			{
				conn.rollback();
			} catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
 
		} 
		 
		finally 
		{
			try {  if(res2 != null) { res2.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(statOne != null) { statOne.close(); } } catch(Exception e){} 
			try {  if(statTwo != null) { statTwo.close(); } } catch(Exception e){} 
			try {  if(statOneUpdate != null) { statOneUpdate.close(); } } catch(Exception e){} 
			try {  if(statTwoUpdate != null) { statTwoUpdate.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
		}
		
		
	}

	/* Void method will set OC character as a default picuture by update timeCreated 
	 * 	field */ 
	public void setDefOcCharacter(String name, long userId, long serverId) 
	{
		String query = "UPDATE customCharacters " + 
						" SET timeCreated = CURRENT_TIMESTAMP " + 
						" WHERE name = ?  AND user_Id = ? AND server_Id = ?";
		
		
		PreparedStatement stat = null;
		Connection conn  = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setString(1, name);
			stat.setLong(2, userId);
			stat.setLong(3,serverId);
			stat.execute(); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}

	/* Method to give character from collect list of gifter to giftee  */ 
	public void giveCharacter(long giverId, long recieverId, long serverId,String giftCharacterName) 
	{
		
			// Remove character from givers database
			long characterId = this.getCharacterIdFromPlayersCollect(giftCharacterName, giverId, serverId); 
			String queryOne = "DELETE FROM playersCollection "
					+ " WHERE user_Id = ? AND server_Id = ? AND col_Id = ?"; 
			String quertyTwo = "INSERT INTO playersCollection(col_Id ,user_Id , server_Id ) " 
					+ " VALUES ( ? , ? , ? )" ;
			PreparedStatement statDelete = null;
			PreparedStatement statInsert = null; 
			Connection conn = null; 
			try
			{
				conn = dataSource.getConnection(); 
				conn.setAutoCommit(false); // make atomic 
				statDelete = conn.prepareStatement(queryOne); 
				statInsert = conn.prepareStatement(quertyTwo); 
				statDelete.setLong(1, giverId);
				statDelete.setLong(2, serverId);
				statDelete.setLong(3, characterId);
				statInsert.setLong(1, characterId);
				statInsert.setLong(2, recieverId);
				statInsert.setLong(3,serverId); 
				statDelete.execute(); 
				statInsert.execute(); 
				conn.commit(); // commit insertion and deletion
			} catch (SQLException e)
			{
				e.printStackTrace();
			} 
			finally // Make sure statement is closed
			{
				try {  if(statDelete != null) { statDelete.close(); } } catch(Exception e){} 
				try {  if(statInsert != null) { statInsert.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
			}
	}
	
	/* Method to return string showing if sona is available in following game modes*/ 
	public ArrayList<String> CharacterGameModesSona(long userId, long serverId) {
		String query = "SELECT * FROM sonas "
				+ "WHERE user_Id = ?  AND server_Id = ?";
	
		Connection conn = null; 
		PreparedStatement stat = null; 
		ResultSet res = null; 
		ArrayList<String> result = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			res.next(); 
			int column = res.getMetaData().getColumnCount(); 
			result = new ArrayList<String>(); 
			for(int i = 6; i <= column-1; ++i) 
			{
				if(res.getString(i).equals("T")) 
				{
					result.add(res.getMetaData().getColumnName(i) + ": "+ ":white_check_mark:"); 
				} 
				else 
				{
					result.add(res.getMetaData().getColumnName(i) + ": " + ":x:"); 
				}
			} 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}

	public ArrayList<String> CharacterGameModesOc(Long userId, Long serverId, long charId) 
	{
		String query = "SELECT * FROM customCharacters "
				+ "WHERE user_Id = ? AND server_Id = ?  AND cusChar_Id = ?";
	
		Connection conn = null; 
		PreparedStatement stat = null; 
		ResultSet res = null; 
		ArrayList<String> result = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);  
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setLong(3, charId);
			res = stat.executeQuery(); 
			res.next(); 
			int column = res.getMetaData().getColumnCount(); 
			result = new ArrayList<String>(); 
			for(int i = 7; i <= column; ++i) 
			{
				if(res.getString(i).equals("T")) 
				{
					result.add(res.getMetaData().getColumnName(i) + ": "+ ":white_check_mark:"); 
				} 
				else 
				{
					result.add(res.getMetaData().getColumnName(i) + ": " + ":x:"); 
				}
			} 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}
	
	/* Bool method check if has whitelisting enabled in the database */ 
	public boolean serverWhiteList(long serverId)
	{
		String query = "SELECT * FROM whitelist WHERE server_Id = ?"; 
		Connection conn = null; 
		PreparedStatement stat = null;
		ResultSet res = null; 
		boolean result = false; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			res = stat.executeQuery(); 
			result = res.next(); 
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result;
	}
	
	/* Method inserts white list into database */ 
	public void insertWhiteList(long serverId)
	{
		
		String query = "INSERT IGNORE INTO whitelist (server_Id) VALUES (?) "; 
		Connection conn = null; 
		PreparedStatement stat = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.execute(); 
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
	}
	
    /* Delete from white list */ 
	public void removeFromWhiteList(long serverId) 
	{
		String query = "DELETE FROM whitelist WHERE server_Id = ? "; 
		Connection conn = null; 
		PreparedStatement stat = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query); 
			stat.setLong(1, serverId);
			stat.execute(); 
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Update number of members estimated of each server */ 
	public void updateMemberCount(int memberCount) 
	{
		String query = "REPLACE INTO server_info VALUES (1, ?)"; 
		Connection conn = null; 
		PreparedStatement stat = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setInt(1, memberCount);
			stat.execute(); 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/*Get a random job returns its fields as string */
	public ArrayList<String> getJob()
	{
		ArrayList<String> result = new ArrayList<String>(); 
		String query = "SELECT * FROM jobs ORDER BY RAND() LIMIT 1"; 
		Connection conn = null; 
		PreparedStatement stat = null;
		ResultSet res = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			res = stat.executeQuery(); 
			res.next(); 
			result.add(res.getString("job_id")); 
			result.add( res.getString("occupation")); 
			result.add( res.getString("job_desc")); 
			result.add( res.getString("min")); 
			result.add( res.getString("max")); 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		return result; 
	}
	
	/*Return String list of players collection that called a job */
	public HashMap<String , JSONObject > getJobCharacters(Long userId,Long serverId, String occupation)
	{
		HashMap<String, JSONObject> result = new HashMap<String,JSONObject>(); 
		String query = "SELECT c.name, c.perks From characters as c "
				+ "INNER JOIN playerscollection as pc ON c.char_Id AND pc.user_Id = ? AND pc.server_Id = ? "
				+ " INNER JOIN chtr_jobs as cj ON c.char_Id = cj.char_Id AND cj.occupation = ?"; 
		Connection conn = null; 
		PreparedStatement stat = null;
		ResultSet res = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setString(3, occupation);
			res = stat.executeQuery(); 
			// iterate through rows 
			while(res.next()) 
			{
				String first=  res.getString(1); 
				String sec = res.getString(2); 
				JSONObject jsonObj = new JSONObject(sec);
				result.put(first, jsonObj); 
			}
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result; 
	}
	
	/* Update cash amount of a user  */ 
	public void sendCash(Long userId, Long serverId, int income) 
	{
		String query = "UPDATE unq_users "
				+ " SET cash = cash + ? "
				+ " WHERE user_id = ? AND server_id = ?"; 
		Connection conn = null; 
		PreparedStatement stat = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setInt(1, income);
			stat.setLong(2, userId);
			stat.setLong(3, serverId);
			stat.execute(); 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
	}
	
	/* Get cash and bank checking from user */
	public ArrayList<String> getBalance(Long userId, Long serverId)
	{
		ArrayList<String> result = new ArrayList<String>();
		String query = "SELECT cash , bank FROM unq_users"
				+ " WHERE unq_users.user_id = ? AND "
				+ " unq_users.server_id = ? "; 
		Connection conn = null; 
		PreparedStatement stat = null;
		ResultSet res = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			res = stat.executeQuery(); 
			// iterate through rows 
			if ( res.next()) 
			{ 
				result.add(res.getString(1)); 
				result.add(res.getString(2)); 
			}
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return result;
	}
	
	// Checks if user has enough to deposit 
	public boolean checkDeposit(Long userId, Long serverId, int amount) 
	{
		boolean enough = true;
		String query = "SELECT * FROM unq_users "
				+ "WHERE user_id = ? AND server_id = ? "
				+ " AND cash >= ? "; 
		Connection conn = null; 
		PreparedStatement stat = null; 
		ResultSet res = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setInt(3, amount);
			res = stat.executeQuery(); 
			
			enough = res.next(); 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return enough; 
	}
	
	// Deposit cash into the bank account 
	public void deposit(Long userId, Long serverId, int amount) 
		{
			String query = "UPDATE unq_users "
					+ " SET cash = cash - ? , bank = bank + ? "
					+ "WHERE user_id = ? AND server_id = ? "
					+ " AND cash >= ? "; 
			Connection conn = null; 
			PreparedStatement stat = null; 
			try 
			{
				conn = dataSource.getConnection(); 
				stat = conn.prepareStatement(query);
				stat.setInt(1, amount);
				stat.setInt(2, amount);
				stat.setLong(3, userId);
				stat.setLong(4, serverId);
				stat.setInt(5, amount);
				stat.execute(); 
			}
			catch(SQLException e) 
			{
				e.printStackTrace();
			}
			finally 
			{
				try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
			}
		}
	
	public boolean checkWithDraw(Long userId,Long serverId,int amount) 
	{
		boolean enough = true;
		String query = "SELECT * FROM unq_users "
				+ "WHERE user_id = ? AND server_id = ? "
				+ " AND bank >= ? "; 
		Connection conn = null; 
		PreparedStatement stat = null; 
		ResultSet res = null; 
		try 
		{
			conn = dataSource.getConnection(); 
			stat = conn.prepareStatement(query);
			stat.setLong(1, userId);
			stat.setLong(2, serverId);
			stat.setInt(3, amount);
			res = stat.executeQuery(); 
			
			enough = res.next(); 
			
		}
		catch(SQLException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		return enough; 
	} 
	
	// Deposit cash into the bank account 
		public void withDraw(Long userId, Long serverId, int amount) 
			{
				String query = "UPDATE unq_users "
						+ " SET bank = bank - ? , cash = cash + ? "
						+ "WHERE user_id = ? AND server_id = ? "
						+ " AND bank >= ? "; 
				Connection conn = null; 
				PreparedStatement stat = null; 
				try 
				{
					conn = dataSource.getConnection(); 
					stat = conn.prepareStatement(query);
					stat.setInt(1, amount);
					stat.setInt(2, amount);
					stat.setLong(3, userId);
					stat.setLong(4, serverId);
					stat.setInt(5, amount);
					stat.execute(); 
				}
				catch(SQLException e) 
				{
					e.printStackTrace();
				}
				finally 
				{
					try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
					try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
				}
			}
		
		// Get fields and value from items table
		public HashMap<String, Pair<String, Integer>> getItems() 
		{
			HashMap<String, Pair<String,Integer>>result=  new HashMap<String, Pair<String, Integer>>(); 
			String query = "SELECT * FROM items ";  
			Connection conn = null; 
			PreparedStatement stat = null; 
			ResultSet res = null; 
			try 
			{
				conn = dataSource.getConnection(); 
				stat = conn.prepareStatement(query);
				res = stat.executeQuery(); 
				while(res.next()) 
				{   
					String name = res.getString(1); 
					if(name.contains("_")) 
					{
						name = name.replace("_", " "); 
					}
					result.put(name, Pair.of(res.getString(2), res.getInt(3)) ); 
				}
			}
			catch(SQLException e) 
			{
				e.printStackTrace();
			}
			finally 
			{
				try {  if(res != null) { res.close(); } } catch(Exception e){} 
				try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
			}
			return result; 
		}
		
		// Method check if user has enough money to buy an item. 
		public boolean checkItemPrice(Long userId, Long serverId, String choice) 
		{
			boolean enough = true;
			String query = "SELECT * FROM items, unq_users "
					+ " WHERE items.item_name = ? AND unq_users.user_id = ?"
					+ " AND unq_users.server_id = ? "
					+ " AND unq_users.cash >= items.price ";  
			Connection conn = null; 
			PreparedStatement stat = null; 
			ResultSet res = null; 
			try 
			{
				conn = dataSource.getConnection(); 
				stat = conn.prepareStatement(query);
				stat.setString(1, choice);
				stat.setLong(2, userId);
				stat.setLong(3, serverId);
				res = stat.executeQuery(); 
				enough = res.next(); 
				
			}
			catch(SQLException e) 
			{
				e.printStackTrace();
			}
			finally 
			{
				try {  if(res != null) { res.close(); } } catch(Exception e){} 
				try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
			}
			return enough; 
		}
		
		// Method check if user has enough money to buy an item. 
		public int getItemPrice( String choice) 
		{
			String query = "SELECT price FROM items "
					+ " WHERE items.item_name = ? ";  
			Connection conn = null; 
			PreparedStatement stat = null; 
			ResultSet res = null;
			int price = 0; 
			try 
			{
				conn = dataSource.getConnection(); 
				stat = conn.prepareStatement(query);
				stat.setString(1, choice);
				res = stat.executeQuery(); 
				res.next(); 
				price = res.getInt(1); 
				
			}
			catch(SQLException e) 
			{
				e.printStackTrace();
			}
			finally 
			{
				try {  if(res != null) { res.close(); } } catch(Exception e){} 
				try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
			}
			return price; 
		}
		
		// Method decrements players cash and gives them their items 
		public void  performItemTransaction(Long userId,Long serverId, String choice) 
		{
			String queryTransaction = "UPDATE unq_users, items "
					+ "SET unq_users.cash = unq_users.cash - items.price , "
					+ " unq_users." + choice + " = unq_users." + choice + " + 1 "
							+ " WHERE unq_users.cash >= items.price AND unq_users.user_id = ? "
							+ " AND unq_users.server_id = ? AND items.item_name = ? "; 
			Connection conn = null; 
			PreparedStatement statTrans = null;  
			try
			{
				conn = dataSource.getConnection(); 
				statTrans = conn.prepareStatement(queryTransaction);
				statTrans.setLong(1, userId);
				statTrans.setLong(2, serverId);
				statTrans.setString(3, choice);
				statTrans.execute();
			}
			catch(SQLException e) 
			{
				e.printStackTrace(); 
			}
			finally 
			{
				try {  if(statTrans != null) { statTrans.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
			}
		}
		
		// Perform action and decrement cash 
		public void performServiceTransaction(Long userId, Long serverId, String choice)
		{
			String queryTransaction = "UPDATE unq_users , items "
					+ " SET unq_users.cash = unq_users.cash - items.price " 	
					+ " WHERE unq_users.cash >= items.price AND unq_users.user_id = ? "
					+ " AND unq_users.server_id = ? AND items.item_name = ? ";
			
			String queryService = "DELETE FROM waifus WHERE waifus.user_id = ? AND waifus.server_id =? ";   
			Connection conn = null; 
			PreparedStatement statTrans = null;  
			PreparedStatement statWaifu = null; 
			try
			{
				conn = dataSource.getConnection();
				conn.setAutoCommit(false); // start transaction 
				
				statTrans = conn.prepareStatement(queryTransaction);
				statTrans.setLong(1, userId);
				statTrans.setLong(2, serverId);
				statTrans.setString(3, choice);
				
				// means row has been updated 
				if ( statTrans.executeUpdate() != 0) 
				{
					statWaifu = conn.prepareStatement(queryService); 
					statWaifu.setLong(1, userId);
					statWaifu.setLong(2, serverId);
				}
				else 
				{
					// no rows roll back execit
					conn.rollback();
				}
				conn.commit(); // complete transaction 
			}
			catch(SQLException e) 
			{
				e.printStackTrace(); 
				try {
					conn.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			finally 
			{
				try {  if(statWaifu != null) { statWaifu.close(); } } catch(Exception e){} 
				try {  if(statTrans != null) { statTrans.close(); } } catch(Exception e){} 
				try {  if(conn != null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception e){} 
				
			}
		}

}