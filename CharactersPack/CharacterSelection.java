package CharactersPack;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;




 

/* Class will hold a series of functions to help  
 * query characters from the database 
 * */ 

 

public class CharacterSelection {
	
	public static String AdminName; 
	public static String password; 
	public static String urlDbs; 
	
	public CharacterSelection()
	{
		
	}
	
	public CharacterSelection(String urlArg, String nameArg, String passwordArg)
	{
		urlDbs = urlArg; 
		AdminName = nameArg; 
		password = passwordArg; 
	}
	
	/*Method to get all characters on the database */
	public ArrayList<String> getAllCharacterNames(GAMETYPE type, long serverId) 
	{
		// Query 
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		ArrayList<String> names = null; 
		try 
		{
			
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			String query = ""; 
		
			switch(type) 
			{
			case WIKI :
				query = "SELECT name FROM characters"; 
				break; 
			case KDM :
			query = "SELECT name FROM characters \r\n"
					+ "WHERE characters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "WHERE is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inKDM = \"T\" " + " AND " + "server_Id = " + serverId  
					+ " UNION "
					+ "SELECT name FROM customCharacters\r\n"
					+ "WHERE inKDM = \"T\"" + " AND " + "server_Id = " + serverId ; 
			break; 
			case SMASHPASS :
			query = "SELECT name FROM characters \r\n"
					+ "WHERE characters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "WHERE is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inSP = \"T\" " + " AND " + "server_Id = " + serverId   
					+ " UNION "
					+ " SELECT name FROM customCharacters\r\n"
					+ " WHERE inSP = \"T\""  + " AND " + "server_Id = " + serverId ;  
			break; 
		case FAVORITES :
			query = "SELECT name FROM characters \r\n"
					+ "UNION \r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inFav = \"T\" " + " AND " + "server_Id = " + serverId  
					+ " UNION "
					+ " SELECT name FROM customCharacters\r\n"
					+ " WHERE inFav = \"T\"" + " AND " + "server_Id = " + serverId ;  
			
		case COLLECT : 
			query = "SELECT name FROM characters \r\n"
					+ "UNION \r\n"
					+ "SELECT name FROM gameCharacters\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inCollect = \"T\" " + " AND " + "server_Id = " + serverId  
					+ " UNION "
					+ " SELECT name FROM customCharacters\r\n"
					+ " WHERE inCollect = \"T\"" + " AND " + "server_Id = " + serverId ;  
			break; 
		}
		
			res = stat.executeQuery(query); 
		
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
		+ "WHERE user_Id = " + userId + " AND server_Id = " + serverId ;  
		
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
	
	public ArrayList<Character> getAllCharacters(SELECTIONTYPE type ,SETUPTYPE set) 
	{
		Statement stat = null;
		ResultSet res = null;
		Connection conn = null; 
		ArrayList<Character> list = null;  

		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
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
			CharacterFactory factor = new CharacterFactory(Long.valueOf(columnData[1]), columnData[2], columnData[3],columnData[5],set);
			
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
		
	
		Statement stat = null; 
		ResultSet res = null;
		Connection conn = null; 
		boolean result = false; 
		try 
		{	
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement(); 
			String query = "SELECT * FROM waifus " + 
						"WHERE user_id = " + userID + " AND server_id = " + serverID + 
						" LIMIT 1";  
			res = stat.executeQuery(query);
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
	
	/* Method to update character of a user in the waifu table */
	public void updateWaifuCharacter(Long userID, Long serverID, Character chtr) 
	{
		String query = ""; 
	
		// Query the waifu database 
		
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		try
		{
			
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			query = "SELECT * FROM waifus " + 
				"WHERE user_id = " + userID + " AND server_id = " + serverID + 
				" LIMIT 1";  
				
		res = stat.executeQuery(query); 
		
		// Now update the query [ userid, serverid, id] 
		res.first(); 
		res.updateLong(3, chtr.getId());
		res.updateRow(); // update the row
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
		
	}
	
	
	/* Method adds user and there character to waifu table */ 
	public void insertWaifu(Long userID, Long serverID, Character chtr)  
	{
		String query = "INSERT waifus (user_id, server_id, waifu_id)" + 
						" VALUES(" + userID + "," + serverID + "," + chtr.getId() + ")";
		
		Statement stat = null ;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			// Now insert the data 
			stat.executeUpdate(query); 
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
		Statement stat = null;
		ResultSet res = null; 
		ResultSet res2 = null; 
		ResultSet res3 = null; 
		Character found = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			String query = "SELECT waifu_id FROM waifus " + 
					"WHERE user_id = " + userID + " AND server_id = " + serverID + 
					" LIMIT 1";
			
			 res = stat.executeQuery(query);
			
			// Have the character now find it in the characters table 
			res.next(); 
			
			Integer id = Integer.valueOf(res.getString(1));
			
			// Now get the character with this id 
			
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters\r\n"
					+ " WHERE characters.char_Id = " + id.toString()
					+ " UNION\r\n"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters\r\n"
					+ " WHERE gameCharacter_Id = " + id.toString() 
					+ " UNION \r\n"
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas\r\n"
					+ " WHERE  sonas.sona_Id = " + id.toString()
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE cusChar_Id = " + id.toString() ;  
			
			
			res3 = stat.executeQuery(query); 
			res3.next();
			// We have the character now return it 
			CharacterFactory factory = new CharacterFactory(Long.valueOf(res3.getString(1)), res3.getString(2), res3.getString(3), res3.getString(4), SETUPTYPE.LIGHT); 
			 found = factory.getCharacter(); 
			// Now get the date for next switch 
			query = "SELECT * FROM timeTable";
			
			res2 = stat.executeQuery(query);
			
			res2.next(); 
			
			Date date = res2.getTimestamp(1); 
			
			
			found.setDate(date);
			
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			
			try {  if(res3 != null) { res3.close() ;}} catch (Exception e ) {}
			try {  if(res2 != null ) { res.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
					
		return found;

	}
	
	
	
	public  Character[] getRandomCharacters(GAMETYPE type, SETUPTYPE set, Long serverId ,int n)  
	{
		Character[] found = null; 
		String query = ""; 
		switch(type) 
		{
		case KDM: 
			query =  "SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters " +  
					 " WHERE characters.is_Adult = \"T\"" + 
					 " UNION " + 
					 " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters " +
					 " WHERE gameCharacters.is_Adult = \"T\"" + 
					 " UNION " + 
					 " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas " + 
					 " WHERE sonas.inKDM = \"T\" " + " AND server_Id = " + serverId +   
					 " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inKDM = \"T\" " + " AND server_Id = " + serverId +    
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 

			break; 
		case SIMPS:
			query = "SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters "
					+ " WHERE characters.is_Adult = \"T\" "
					+ " UNION\r\n"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters "
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas "
					+ " WHERE sonas.inSimps = \"T\" " + " AND server_Id = " + serverId 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inSimps = \"T\" " + " AND server_Id = " + serverId +   
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case SHIPS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inShips = \"T\""  +  " AND server_Id = " + serverId 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inShips = \"T\" " + " AND server_Id = " + serverId +   
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case KINS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inKins = \"T\" " + " AND server_Id = " + serverId 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inKins = \"T\" " + " AND server_Id = " + serverId +    
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case SMASHPASS: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inSP = \"T\" " + " AND server_Id = " + serverId 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inSP = \"T\" " + " AND server_Id = " + serverId +     
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case WAIFU:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE gameCharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inWaifu = \"T\"" +   " AND server_Id = " + serverId 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inWaifu = \"T\" " + " AND server_Id = " + serverId +    
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case GUESS: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inGuess = \"T\" " + " AND server_Id = " + serverId 
					+ " UNION "  
					+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters "  
					+ " WHERE inGuess = \"T\" " +   " AND server_Id = " + serverId      
					+ " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case COLLECT: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inCollect = \"T\" " +  " AND server_Id = " + serverId 
					+ " UNION "  
					+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters "  
					+ " WHERE inCollect = \"T\" " +  " AND server_Id = " + serverId   
					+ " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		}
		
		return found;  
	}
	
	
	

	/* Return a single character type of character based on enum SELECTIONTYPE */
	public  Character requestSingleCharacter(String name, long serverId,GAMETYPE type, SETUPTYPE set)  
	{
		
		
		Character found  = null; 
		String query = ""; 
		switch(type)
		{
		case KDM:
		if(!name.contains("\"")) 
		{
		query  = 
				 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
				+ " WHERE LOWER(characters.name) = LOWER(" + "\"" +name + "\"" + ") AND characters.is_Adult = \"T\" "
				+ " UNION"
				+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
				+ " WHERE LOWER(gameCharacters.name) = LOWER(" +"\"" + name + "\"" + ") AND gameCharacters.is_Adult = \"T\""
				+ " UNION"
				+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
				+ " WHERE  LOWER(sonas.name) = LOWER("+ "\"" + name + "\"" +") AND sonas.inKDM = \"T\"" + " AND server_Id = " + serverId 
				+ " UNION "
				+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
				+ " WHERE  LOWER(name) = LOWER("+ "\"" + name + "\"" +") AND inKDM = \"T\"" + " AND server_Id = " + serverId 
				+ "  LIMIT 1"; 
		}
		else 
		{
			
			query  =
					 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE LOWER(characters.name) = LOWER(\'" + name + "\') AND characters.is_Adult = \"T\" "
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE LOWER(gameCharacters.name) = LOWER(\'"+ name+ "\') AND gameCharacters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE  LOWER(sonas.name) = LOWER(\'" + name +"\') AND sonas.inKDM = \"T\"" +  " AND server_Id = " + serverId 
					+ " UNION "
				+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
				+ " WHERE  LOWER(name) = LOWER("+ "\'" + name + "\'" +") AND inKDM = \"T\"" +  " AND server_Id = " + serverId 
					+ "  LIMIT 1"; 
		}
		found = processQueryGetCharacters(query,set)[0]; 
		break; 
		
		case SMASHPASS:
			if(!name.contains("\"")) 
			{
			query  = 
					 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE LOWER(characters.name) = LOWER(" +"\"" + name + "\"" +") AND characters.is_Adult = \"T\" "
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE LOWER(gameCharacters.name) = LOWER(" + "\"" + name + "\"" + ") AND gameCharacters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE  LOWER(sonas.name) = LOWER(" +"\"" +name + "\"" +") AND sonas.inSP = \"T\"" +  " AND server_Id = " + serverId 
					+ " UNION "
					+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
					+ " WHERE  LOWER(name) = LOWER("+ "\"" + name + "\"" +") AND inSP = \"T\"" +  " AND server_Id = " + serverId 
					+ "  LIMIT 1"; 
			}
			else 
			{
				
				query  = 
						 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
						+ " WHERE LOWER(characters.name) = LOWER(\'" + name + "\') AND characters.is_Adult = \"T\" "
						+ " UNION"
						+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
						+ " WHERE LOWER(gameCharacters.name) = LOWER(\'"+ name+ "\') AND gameCharacters.is_Adult = \"T\""
						+ " UNION"
						+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
						+ " WHERE  LOWER(sonas.name) = LOWER(\'" + name +"\') AND sonas.inSP = \"T\"" + " AND server_Id = " + serverId 
						+ " UNION "
						+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
						+ " WHERE  LOWER(name) = LOWER("+ "\'" + name + "\'" +") AND inSP = \"T\"" + " AND server_Id = " + serverId  
						+ "  LIMIT 1"; 
			}
			found = processQueryGetCharacters(query,set)[0]; 
			break; 
		case WIKI: 
		if(name.contains("\""))
		{
			query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE LOWER(characters.name) = LOWER(" + "\'" + name + "\'" + ") "
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE LOWER(gameCharacters.name) = LOWER(" + "\'" + name + "\'" + ") " + " LIMIT 1"; 
		}
		else 
		{
			query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE LOWER(characters.name) = LOWER( \""  + name  + "\") "
					+ " UNION"
					+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
					+ " WHERE LOWER(gameCharacters.name) = LOWER( \"" + name + "\") " + " LIMIT 1"; 
		}
			found = processQueryGetCharacters(query,set)[0]; 
			break;
		case FAVORITES: 
			if(name.contains("\""))
			{
				query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
						+ " WHERE LOWER(characters.name) = LOWER(" + "\'" + name + "\'" + ") "
						+ " UNION"
						+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
						+ " WHERE LOWER(gameCharacters.name) = LOWER(" + "\'" + name + "\'" + ") "
						+ "UNION"
						+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
						+ " WHERE  LOWER(sonas.name) = LOWER(\'" + name +"\') AND sonas.inFav = \"T\"" +  " AND server_Id = " + serverId   
						+ " UNION "
						+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
						+ " WHERE  LOWER(name) = LOWER("+ "\'" + name + "\'" +") AND inFav = \"T\"" +  " AND server_Id = " + serverId  
						+ "  LIMIT 1"; 
			}
			else 
			{
				query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
						+ " WHERE LOWER(characters.name) = LOWER( \""  + name  + "\") "
						+ " UNION"
						+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
						+ " WHERE LOWER(name) = LOWER( \"" + name + "\") " 	
						+ " UNION "
						+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
						+ " WHERE LOWER(sonas.name) = LOWER(\"" + name + "\"" + ") AND sonas.inFav = \"T\"" +  " AND server_Id = " + serverId 
						+ " UNION "
						+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
						+ " WHERE  LOWER(name) = LOWER("+ "\"" + name + "\"" + ") AND inFav = \"T\"" +  " AND server_Id = " + serverId 
						+ "  LIMIT 1";  
			}
				found = processQueryGetCharacters(query,set)[0]; 
		default:	
		}
		return found; 
	}
	
	
	
	
	
	/* Request from database a character from given string */ 
	private  Character[] processQueryGetCharacters(String query, SETUPTYPE type)  
	{
		String q = query;
		Statement stat = null;	
		ResultSet  res = null; 
		ResultSet timeRes = null;
		Character arr[] = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
	
			// Now get the date for next switch 
			String queryLcl = "SELECT * FROM timeTable";
				
			timeRes = stat.executeQuery(queryLcl);
				
			timeRes.next(); 
				
			Date date = timeRes.getTimestamp(1); 
				
			timeRes.close(); 
		 
			res = stat.executeQuery(q); 
			ArrayList<Character> list = new ArrayList<Character>(); 
			res.next(); 
		// columns 
		
		do {
	
			CharacterFactory factory = null; 
			
				factory = new CharacterFactory(Long.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4),type); 
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		finally 
		{
			try {  if(timeRes!= null) {timeRes.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 

		}
		
		return arr;
	}
	

	/* Method seraches if user has a sona in a specifed guild */ 
	public boolean searchUserInSona(Long userID, Long serverID)  
	{
		
		// Query the waifu database 
		
		Statement stat = null ;
		ResultSet res = null; 
		boolean result = false; 
		Connection conn = null ;
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName,password); 
			stat = conn.createStatement();
			String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = " + userID + " AND server_Id = " + serverID + 
						" LIMIT 1";  
		
			res = stat.executeQuery(query); 
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
		Statement stat = null;
		Character found = null; 
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = " + userID + " AND server_Id = " + serverID + 
						 " LIMIT 1";
			res = stat.executeQuery(query); 
		
			res.next(); 
		
				
		// We have the character now return it 
			CharacterFactory factory = new CharacterFactory(Long.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
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
	
	// Remove sona from the table 
	public boolean removeSona(Long userId, Long serverId)  
	{
		Statement stat = null;
		boolean result = false; 
		Connection conn = null; 
		try
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			String query = "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN sonas ON sonas.sona_Id = character_Ids.id \r\n"
				+ "WHERE sonas.user_Id = " + userId +   " AND sonas.server_Id = " + serverId; 
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
	
	// Remove waifu from the table
	public boolean removeWaifu(Long  userID, Long serverID)  
	{
		Statement stat = null;
		boolean result = false; 
		Connection conn =null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
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
	
	/* Method will insert a character to the database on the sona table */ 
	public void insertSona(String name, Long userId, String url , Long serverId, String inKDM, String inSP, String inSimps,String inShips, String
			inKins, String inWaifu, String inFav, String inGuess, String inCollect)  
	{
		String queryOne = " INSERT INTO character_Ids(id) VALUES(NULL)"; 
		
		String queryTwo = "";   
		if(!name.contains("\"")) 
		{  
			queryTwo = " INSERT INTO sonas (sona_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess, inCollect) " +  
				 " VALUES (last_insert_id()," + "\"" + name + "\"" +  "," + userId +  "," + "\"" +  url + "\"" + " , "  + serverId + " , " + "\"" +inKDM + "\"" + "," + "\"" + inSP + "\"" + " ," + "\"" + inSimps +"\"" + ", "+  "\"" + inShips + "\"" +"," + "\""+  inKins + "\"" 
				+", " + "\"" + inWaifu + "\"" + "," + "\"" + inFav + "\"" + "," + "\"" + inGuess + "\"" + "," + "\""+inCollect + "\"" + " )";  
		}
		else 
		{
			queryTwo = " INSERT INTO sonas (sona_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inCollect) " +  
					 " VALUES (last_insert_id()," + "\'" + name + "\'" +  "," + userId +  "," + "\'" +  url + "\'" + " , " + serverId + " , " + "\'" +inKDM + "\'" + "," + "\'" + inSP + "\'" + " ," + "\'" + inSimps +"\'" + ", "+  "\'" + inShips + "\'" +"," + "\'"+  inKins + "\'" 
					+", " + "\'" + inWaifu + "\'" + "," + "\'" + inFav + "\'" + "," + "\'" + inGuess + "\'" + ","+  "\""+ inCollect + "\"" + " )";  
		}
		Statement stat = null;
		
		Connection conn =null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs,AdminName,password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.addBatch(queryOne);
			stat.addBatch(queryTwo);
			stat.executeBatch();
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
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
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
		String query = ""; 
		if(!characterName.contains("\"")) {
			query = "SELECT characters.char_Id FROM characters \r\n"
				+ "WHERE characters.name = \"" + characterName + "\"\r\n"
				+ "UNION\r\n"
				+ "SELECT gameCharacter_Id FROM gameCharacters\r\n"
				+ "WHERE name = \"" + characterName + "\"\r\n"
				+ "UNION\r\n"
				+ "SELECT sonas.sona_Id FROM sonas\r\n"
				+ "WHERE sonas.name = \"" + characterName + "\"\r\n"
				+ "UNION "
				+ "SELECT cusChar_Id FROM customCharacters " 
				+ "WHERE name = \"" + characterName + "\" "
				+ "LIMIT 1";
		}
		else 
		{
			query = "SELECT characters.char_Id FROM characters \r\n"
					+ "WHERE characters.name = \'" + characterName + "\'\r\n"
					+ "UNION\r\n"
					+ "SELECT gameCharacter_Id FROM gameCharacters\r\n"
					+ "WHERE name = \'" + characterName + "\'\r\n"
					+ "UNION\r\n"
					+ "SELECT sonas.sona_Id FROM sonas\r\n"
					+ "WHERE sonas.name = \'" + characterName + "\'\r\n"
					+ "UNION "
					+ "SELECT cusChar_Id FROM customCharacters " 
					+ "WHERE name = \'" + characterName + "\' "
					+ "LIMIT 1";
		}
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res =  stat.executeQuery(query);
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
	public void insertFavorite(String name , Long  userId, Long serverId) 
	{
		Character temp = this.requestSingleCharacter(name, serverId,GAMETYPE.FAVORITES, SETUPTYPE.LIGHT); 
		String query = ""; 
		
		if(!name.contains("\"")) 
		{
			 query = "INSERT INTO favorites (fav_Id, name ,user_Id,server_Id) " + 
						" VALUES(" + temp.getId() + "," + "\"" + name +"\"" + ","  + userId + "," + serverId + " )"; 
		}
		else 
		{
			 query = "INSERT INTO favorites (fav_Id, name ,user_Id,server_Id) " + 
					" VALUES(" + temp.getId() + "," + "\'" + name +"\'" + ","  + userId + ","  + serverId + " )"; 
		}
			Statement stat = null;
			Connection conn =null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.execute(query); 
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
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
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
		}
		
		return list; 
	}
	
	public String getTitleList(Long userId, Long serverId) 
	{
		String query = "SELECT * FROM favorites " 
				+ "WHERE user_id = " + userId + 
				" AND server_id = "  + serverId ;
		
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		String title = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
				 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
				+ " WHERE char_id = " + id
				+ " UNION"
				+ " SELECT gameCharacter_Id, name, show_Name, imgur_Url FROM gameCharacters"
				+ " WHERE gameCharacter_Id = " + id
				+ " UNION"
				+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
				+ " WHERE  sona_Id = " + id
				+ " UNION"
				+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
				+ " WHERE  cusChar_Id = " + id 
				+ "  LIMIT 1"; 
		
		Statement stat = null;
		ResultSet res = null;
		Connection conn =null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName ,password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
			CharacterFactory factory = null; 
			res.next(); 
			factory = new CharacterFactory(Long.valueOf(res.getString(1)),res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
			return factory.getCharacter(); 
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
		return null; 
	}
	
	
	public String getCharacterNameById(Long id)  
	{
		String query  = 
				 " SELECT  characters.name FROM characters"
				+ " WHERE char_id = " + id
				+ " UNION"
				+ " SELECT name  FROM gameCharacters "
				+ " WHERE gameCharacter_Id = " + id
				+ " UNION"
				+ " SELECT  sonas.name FROM sonas "
				+ " WHERE  sona_Id = " + id
				+ " UNION"
				+ " SELECT  name FROM customCharacters "
				+ " WHERE  cusChar_Id = " + id 
				+ "  LIMIT 1"; 
		
		Statement stat = null;
		ResultSet res = null; 
		String result = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();	
			res = stat.executeQuery(query);  
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
				+ " WHERE favorites.user_Id = " + userId + " AND favorites.server_Id = " + serverId ; 
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();
			stat.execute(query);
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
	
	/* Remove list from the database */ 
	public void removeFavCharacter(String name, Long userId, Long serverId) 
	{
		String query = ""; 
		if(!name.contains("\"")) {
			query = "DELETE FROM favorites\r\n"
				+ " WHERE name = " + "\"" + name + "\"" +  " AND user_Id = " + userId + " AND server_Id = " + serverId ; 
		}
		else 
		{
			query = "DELETE FROM favorites\r\n"
					+ " WHERE name = " + "\'" + name + "\'" +  " AND user_Id = " + userId + " AND server_Id = " + serverId ; 
		}
		
		Statement stat = null;
		Connection conn = null; 
		try
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password) ; 
			stat = conn.createStatement();	
			stat.execute(query); 
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
				+ "WHERE user_Id = " + userId + " AND " + "server_Id = " + serverId ; 
		
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName ,password); 
			stat = conn.createStatement();
			 res = stat.executeQuery(query); 
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
				"WHERE user_Id = "  +  userId + " AND server_Id = " + serverId ;
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean result = false; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
	
	
	public boolean isAvailable(String name, Long userId, Long serverId) 
	{
		String query = ""; 
		if(!name.contains("\"")) 
		{
			query = "SELECT characters.name FROM characters \r\n"
					+ " WHERE characters.name = " +  "\"" + name + "\""+ " \r\n"
					+ " UNION\r\n"
					+ " SELECT name FROM gameCharacters \r\n"
					+ " WHERE name = " + "\"" + name + "\"" + "\r\n"
					+ " UNION \r\n"
					+ " SELECT sonas.name FROM sonas\r\n"
					+ " WHERE sonas.name = " + "\"" + name + "\"" + " AND sonas.server_Id = " +  serverId + " AND sonas.user_Id = " +  userId 
					+ " UNION "
					+ " SELECT customCharacters.name FROM customCharacters\r\n"
					+ " WHERE customCharacters.name = " + "\"" + name + "\"" + " AND customCharacters.server_Id = "  + serverId + " AND customCharacters.user_Id = " + userId ; 
		}
		else 
		{
			query = "SELECT characters.name FROM characters \r\n"
					+ " WHERE characters.name =" +  "\'" + name + "\'"+ " \r\n"
					+ " UNION\r\n"
					+ " SELECT name FROM gameCharacters \r\n"
					+ " WHERE name = " + "\'" + name + "\'" + "\r\n"
					+ " UNION \r\n"
					+ "SELECT sonas.name FROM sonas\r\n"
					+ "WHERE sonas.name = " + "\'" + name +"\'" + " AND sonas.server_Id = " + serverId + " AND sonas.user_Id = " +  userId 
					+ " UNION  "
					+ " SELECT customCharacters.name FROM customCharacters\r\n"
					+ " WHERE customCharacters.name = " + "\'" + name + "\'" + " AND customCharacters.server_Id = " + serverId + " AND customCharacters.user_Id = " + userId ;
		}
		
		Statement stat = null;
		ResultSet res = null; 
		boolean result = false; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password ); 
			stat = conn.createStatement();
			 res = stat.executeQuery(query); 
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
		  System.out.println(serverId); 
		String queryTwo = "";   
		if(!name.contains("\"")) 
		{  
			queryTwo = " INSERT INTO customCharacters (cusChar_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess, inCollect) " +  
				 " VALUES (last_insert_id()," + "\"" + name + "\"" +  ","  + userId +  "," + "\"" +  url + "\"" + " , " + serverId + " , " + "\"" +inKDM + "\"" + "," + "\"" + inSP + "\"" + " ," + "\"" + inSimps +"\"" + ", "+  "\"" + inShips + "\"" +"," + "\""+  inKins + "\"" 
				+", " + "\"" + inWaifu + "\"" + "," + "\"" + inFav + "\"" + " ," + "\"" + inGuess + "\"" +  "," + "\"" + inCollect + "\"" + " )";  
		}
		else 
		{
			queryTwo = " INSERT INTO customCharacters (cusChar_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess, inCollect) " +  
					 " VALUES (last_insert_id()," + "\'" + name + "\'" +  "," + userId +  "," + "\'" +  url + "\'" + " , " + serverId + " , " + "\'" +inKDM + "\'" + "," + "\'" + inSP + "\'" + " ," + "\'" + inSimps +"\'" + ", "+  "\'" + inShips + "\'" +"," + "\'"+  inKins + "\'" 
					+", " + "\'" + inWaifu + "\'" + "," + "\'" + inFav + " ," + "\'" + inGuess + "\'" + "," + "\'" + inCollect + "\'" + " )";  
		}
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName ,password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.addBatch(queryOne);
			stat.addBatch(queryTwo);
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
	
	/* Check how many characters the user has in this server */ 
	public boolean checkOCLimit(Long userId, Long serverId)  
	{
		String query = "SELECT COUNT(name) FROM customCharacters "
				+ "WHERE customCharacters.user_Id = " + userId + " AND " + "customCharacters.server_Id = " + serverId ; 
		
		Statement stat = null;
		ResultSet res = null ;
		Connection conn = null;
		int value = -1;
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName,password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
		String query =""; 
		if(!name.contains("\""))
		{
		query = "DELETE character_Ids FROM character_Ids \r\n"
				+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_Ids.id\r\n"
				+ "WHERE customCharacters.user_Id = " + userId + " AND customCharacters.server_Id = " + serverId + " AND customCharacters.name = " + "\"" + name + "\""; 
		}
		else 
		{
			query = "DELETE character_Ids FROM character_Ids \r\n"
					+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_Ids.id\r\n"
					+ "WHERE customCharacters.user_Id = " + userId + " AND customCharacters.server_Id = " + serverId + " AND customCharacters.name = " + "\'" + name + "\'"; 
		}
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
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

	/* Search OC's name on the table */  
	public boolean searchOC(String name, Long userId, Long serverId)  {
		
		String query = "SELECT * FROM customCharacters "  
					+ "WHERE user_Id = "  + userId + " AND server_Id = " + serverId + " AND name = " + "\"" + name + "\"" ; 
		Statement stat = null; 
		ResultSet res = null;
		boolean result = false;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName ,password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query);
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
		conn = DriverManager.getConnection(urlDbs , AdminName , password); 
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
				factory = new CharacterFactory(Long.valueOf( res.getString(1)), res.getString(2), "OC" ,res.getString(3), SETUPTYPE.LIGHT); 
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
		
		String query = ""; 
		if(!characterName.contains("\""))
		{
			query = "SELECT cusChar_Id, name, url FROM customCharacters " 
				+ "WHERE user_Id = " + userId + " AND server_Id = " + serverId +  " AND name = " + "\"" + characterName + "\""; 
		}
		else 
		{
			query = "SELECT cusChar_Id, name, url FROM customCharacters " 
					+ "WHERE user_Id = "  + userId + " AND server_Id = " + serverId + " AND name = " + "\'" + characterName + "\'"; 
		}
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		Character result = null; 
		try 
		{
			conn =DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
			if(!res.next()) 
			{
				return null; 
			}
			CharacterFactory factory = new CharacterFactory(Long.valueOf( res.getString(1)), res.getString(2), "OC" ,res.getString(3), SETUPTYPE.LIGHT); 
			result =  factory.getCharacter(); 
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
					+ "WHERE user_Id = " + userId + " AND server_Id = " + serverId ;  
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean result = false; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName ,password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
		String query = "DELETE FROM customCharacters\r\n"
				+ " WHERE user_Id = " + userId + " AND server_Id = " + serverId ; 
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
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

	/* Update favorite list */ 
	public void changeFavTitle(String title, Long userId, Long serverId)  
	{
		String query = ""; 
		if(!title.contains("\"")) 
		{
			query = " UPDATE favorites " + " SET title = " + "\"" + title + "\"" +  
				" WHERE user_Id = " + userId  + " AND " + serverId ;  
		}
		else 
		{
			query = " UPDATE favorites " + " SET title = " + "\'" + title + "\'" +  
					" WHERE user_Id = " + userId  + " AND " + serverId ;  
		}
		
		
		Statement stat = null;
		Connection conn = null;
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password);  
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.execute(query); 
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
				"WHERE user_Id = " + userId + " AND " + "server_Id = " + serverId;  
		Statement stat = null;
		ResultSet  res = null; 
		Connection conn = null ;
		try
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
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
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
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
				+ "WHERE user_Id = " + userId + " AND " + "server_Id = " + serverId ; 
		
		Statement stat = null;
		ResultSet res = null;
		Connection conn =null; 
		int value = -1; 
		try
		{	
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement(); 	
			res = stat.executeQuery(query);
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
	public ArrayList<Character> getCollectionList(Long userId, Long serverId) 
	{
		CharacterSelection select = new CharacterSelection(); 
		ArrayList<Character > list = new ArrayList<Character>(); 
		String query = "SELECT col_Id FROM playersCollection " 
				+ "WHERE user_Id = " + userId + 
				" AND server_Id = " + serverId + 
				 " ORDER BY timeCreated DESC ";
		Statement stat = null;
		ResultSet res = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName,password); 
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
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			try {  if(res != null) { res.close(); } } catch(Exception e) {} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e) {} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e) {} 
		}

		return list; 
	}

	

	/* Method returns if user reached limit of turns */ 
	public boolean getPlayerRollsLimit(long userId, long serverId) {
		
		
		String query =  "SELECT turns FROM playersInCollect \r\n"
				+ "WHERE user_Id = " +  userId + " AND server_Id = " + serverId;  
		
		Statement stat = null;
		ResultSet res = null; 
		int value = -1 ; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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

	

	
	/* Method inserts character into characterId into players Collection table and decrements turn in playerInCollect table and initalizeing time */ 
	public void claimCharacter(Long characterId, long userId, long serverId) 
	{
		// Add 3 hour wait time 
		String queryOne = "UPDATE playersInCollect " + 
				"SET claim = " + "\"F\""   
				+ " WHERE user_Id = " + userId + " AND server_Id = " + serverId ; 
		
		String quertyTwo = "INSERT INTO playersCollection(col_Id ,user_Id , server_Id ) " 
				+ "VALUES (" + characterId + "," + userId + "," + serverId + ")" ;
		Statement stat = null;
		Connection conn = null; 
		try
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.addBatch(queryOne); 
			stat.addBatch(quertyTwo);
			stat.executeBatch(); 
		} catch (SQLException e)
		{
			e.printStackTrace();
		} 
		finally // Make sure statement is closed
		{
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 

			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		
	}

	/*Get the time needed till the player can colllect again  */ 
	public String getPlayerCollectTime(long userId, long serverId) 
	{
		String query = "SELECT LAST_EXECUTED FROM INFORMATION_SCHEMA.events "
				+ " WHERE EVENT_NAME = \"claim_Reset_Event\""; 
		Statement stat = null;
		ResultSet res = null;
		String result = ""; 
		Connection conn = null; 
		try
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
			res.next(); 
			Date now = new Date(); 
			Date end = res.getTimestamp(1); 
			
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
	public String getRollRestTime(long userId, long serverId)  {
		String query = "SELECT LAST_EXECUTED FROM INFORMATION_SCHEMA.events " 
				+ " WHERE EVENT_NAME = \"turn_Reset_Event\""; 
		Statement stat = null;
		ResultSet res = null; 
		String result= ""; 
		Connection conn = null ;
		try 
		{
			conn  = DriverManager.getConnection(urlDbs ,AdminName, password ); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
			res.next(); 
			Date end = res.getTimestamp(1);
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

	/* Check if time pulled is null if so false otherwise true */ 
	public boolean getClaimLimit(long userId, long serverId)  {
		String query = "SELECT claim FROM playersInCollect " 
				+ " WHERE user_Id = " + userId + " AND " + " server_Id  = " + serverId;
		Statement stat = null;
		ResultSet res = null; 
		String value = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query);
			res.next(); 
			value = res.getString(1); 
			System.out.println(value); 
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
		
		return !value.equals("T"); 
	}

	
	
	/* Inserts user into playersInCollect if they haven't been already */ 
	public void insertUserIntoCollect(long userId, long serverId)  {
		String query = "INSERT IGNORE playersInCollect (user_Id, server_Id) " + 
				" VALUES ( " + userId + " , " + serverId + ")";
		Statement stat = null;
		Connection conn =null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
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
		 }
	}

	/* Decrement player roll in playerInCollect table */ 
	public void decPlayerRoll(long userId, long serverId) 
	{
		String query = "UPDATE playersInCollect " 
				+ " SET turns = turns - 1 " + 
				" WHERE user_Id = " + userId + " AND server_Id = " +  serverId ;  
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
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

	/* Check if character has already been claimed in the server */ 
	public boolean hasBeenClaimed(long charId, long serverId ) 
	{
		String query = "SELECT * FROM playersCollection " + 
				"WHERE server_Id = " + serverId + " AND col_Id = " + charId; 
		
		Statement stat = null;
		ResultSet res = null; 
		boolean result = false;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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


	public long getCollectedCharPlayerId(Long charId, long serverId)  {
		
		
		
		String query = "SELECT user_Id FROM playersCollection " + 
				"WHERE server_Id = " + serverId + " AND col_Id = " + charId; 
		
		Statement stat  = null; 
		ResultSet res = null; 
		Connection conn = null; 
		long value = -1; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
				"WHERE server_Id = " + serverId + " AND user_Id = " + userId; 
		
		Statement stat = null;
		ResultSet res = null;
		Connection conn = null; 
		boolean result = false; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
				" WHERE col_Id = " + id + " AND user_Id = " + userId + " AND server_Id = " + serverId; 
		Statement stat = null;
		Connection conn = null ; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();
			stat.execute(query); 
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
				" WHERE col_Id = " + id + " AND user_Id = " + userId + " AND server_Id = " + serverId;  
		Statement stat = null;
		boolean result = false; 
		ResultSet res = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query);
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
					+ " WHERE user_Id = " + userId + " AND server_Id = " + serverId; 
		
		ArrayList<String > names = new ArrayList<String>(); 
		Statement stat = null;
		ResultSet res= null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query);
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
		}
		return names; 
	}


	/* Void method swap characters between users in Collect game */ 
	public void swapUserCollectible(long trader, long tradee, long traderCharacterId, long tradeeCharacterId, long serverId) 
	{
		
		String queryOne = "UPDATE playersCollection " 
				+ " SET col_Id = " + tradeeCharacterId 
				+ " WHERE col_Id = " + traderCharacterId + " AND user_Id = " + trader + " AND server_Id = " + serverId   ; 
		
		String queryTwo = "UPDATE playersCollection " 
				+ " SET col_Id = " + traderCharacterId 
				+ " WHERE col_Id = " + tradeeCharacterId + " AND user_Id = " + tradee + " AND server_Id = " + serverId   ; 
		
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.addBatch(queryOne); 
			stat.addBatch(queryTwo);
			stat.executeBatch(); 
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

	/* Void method sets the collect character as default image by updating its date */ 
	public void setDefCollectCharacter(long charId, long userId, long serverId)  
	{
		String query = "UPDATE playersCollection " 
					+ " SET timeCreated = CURRENT_TIMESTAMP " 
					+ " WHERE col_Id = " + charId; 
		Statement stat = null;
		Connection conn = null;
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
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

	/* Method searches user has a character in the collect list */ 
	public boolean getSearchCharIdSelect(int characterId, long userId, long serverId)  
	{
		String query = "SELECT * FROM playersCollection "
					+ " WHERE col_Id = " + characterId + " AND server_Id = " + serverId + " AND user_Id = " + userId; 
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean value = false; 
		try {
			conn = DriverManager.getConnection(urlDbs ,AdminName , password); 
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
					+ "  VALUES (" + charId +"," + userId +"," + serverId + ")";  
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName ,password); 
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
	
	/* Method checks limit and returns if reached */ 
	public boolean wishListLimit(long userId, long serverId)  
	{
		
		String query = "SELECT COUNT(wish_Id) FROM wishList "
				+ "WHERE user_Id = " + userId + " AND " + " server_Id = " + serverId ; 
		
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		int value = -1; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
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
				+ "WHERE user_Id = " + userId + 
				" AND server_Id = " + serverId ; 
		Statement stat = null;
		ResultSet res = null ; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName ,password); 
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
				"WHERE server_Id = " + serverId + " AND user_Id = " + userId; 
		
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		boolean value = false; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();	
			res = stat.executeQuery(query); 
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
				" WHERE wish_Id = " + id + " AND user_Id = " + userId + " AND server_Id = " + serverId;  
		Statement stat = null;
		ResultSet res = null; 
		boolean result = false; 
		Connection conn = null; 
		try 
		{
			conn  = DriverManager.getConnection(urlDbs , AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query);
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
				" WHERE wish_Id = " + id + " AND user_Id = " + userId + " AND server_Id = " + serverId;  
		Statement stat = null;
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
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


	public ArrayList<String> getWishListNames(long userId, long serverId) 
	{
		String query = " SELECT wish_Id FROM wishList" 
				+ " WHERE user_Id = " + userId + " AND server_Id = " + serverId; 
	
		ArrayList<String > names = new ArrayList<String>(); 
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null ;
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query);
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
					+ " WHERE server_Id = " + serverId  + " AND wish_Id = " + charId; 
		
		Statement stat = null;
		ResultSet res = null; 
		ArrayList<String> userIds = null; 
		Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement();
			res = stat.executeQuery(query); 
			
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
		}
		return userIds; 
	}

	/* Delete users wish list*/ 
	public void clearWishList(long userId, long serverId) 
	{
		String query = "DELETE FROM wishList "
				+ " WHERE user_Id = " + userId + " AND server_Id = " + serverId; 
		Statement stat = null;
		Connection conn = null ; 
		try 
		{
			conn= DriverManager.getConnection(urlDbs , AdminName , password); 
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
			conn = DriverManager.getConnection(urlDbs, AdminName, password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.addBatch(queryOne); 
			stat.addBatch(queryTwo);
			stat.addBatch(queryThree);
			stat.executeBatch(); 
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
			conn = DriverManager.getConnection(urlDbs , AdminName, password); 
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
				+ "UNION \r\n"
				+ "SELECT DISTINCT user_Id FROM favorites  \r\n"
				+ "UNION \r\n"
				
				+ "SELECT DISTINCT user_Id FROM customCharacters\r\n"
				+ "UNION \r\n"
				+ "SELECT DISTINCT user_Id FROM playersInCollect\r\n"
				+ "UNION	\r\n"
				+ "SELECT DISTINCT user_Id FROM playersCollection "; 
		Statement stat = null;
		ResultSet res = null; 
		Connection conn = null; 
		ArrayList<Long> users = null;
		
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
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


	/* Swap rank of characters in the favorites list */ 
	public void favSwapCharacter(String characterOne, String characterTwo, long userId, long serverId)   
	{
		long charOne = this.getCharacterId(characterOne, serverId); 
		long charTwo = this.getCharacterId(characterTwo, serverId); 
		 String queryOne = "SELECT timeCreated FROM favorites "
		 		+ " WHERE fav_Id = " + charOne + " AND user_Id = " + userId + " AND server_Id = " + serverId; 
		 String queryTwo = "SELECT timeCreated FROM favorites " 
		 		+ " WHERE fav_Id = " + charTwo + " AND user_Id = " + userId + " AND server_Id = " + serverId;
		 
		 Statement stat = null;
		 Statement stat2 = null; 
		 ResultSet res = null;
		 ResultSet res2 = null; 
		 Connection conn = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs ,AdminName ,password); 
			stat = conn.createStatement();
			res = stat.executeQuery(queryOne); 
			res.next(); 
		 
			Timestamp timeOne = res.getTimestamp(1); // first character time 
			res2 = stat.executeQuery(queryTwo); 
			res2.next(); 
			Timestamp timeTwo = res2.getTimestamp(1); // second character time  
		 
			// now swap 
			stat2 = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		 
		 
			queryOne = " UPDATE favorites " 
				 + " SET timeCreated = '" + timeTwo +  "' " + 
				 " WHERE fav_Id = " + charOne + " AND user_Id = " +  userId + " AND server_Id = " + serverId; 
		
			queryTwo = " UPDATE favorites " 
				 + " SET timeCreated = '" + timeOne +  "' "+ 
				 " WHERE fav_Id = " + charTwo + " AND user_Id = " + userId + " AND server_Id = " + serverId;  
		 
			stat2.execute(queryOne); 
			stat2.execute(queryTwo); 
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 
		finally 
		{
			try {  if(res2 != null) { res.close(); } } catch(Exception e){} 
			try {  if(res != null) { res.close(); } } catch(Exception e){} 
			try {  if(stat != null) { stat.close(); } } catch(Exception e){} 
			try {  if(stat2 != null) { res.close(); } } catch(Exception e){} 
			try {  if(conn != null) { conn.close(); } } catch(Exception e){} 
		}
		
		
	}

	/* Void method will set OC character as a default picuture by update timeCreated 
	 * 	field */ 
	public void setDefOcCharacter(String name, long userId, long serverId) 
	{
		String query= ""; 
		
		if(!name.contains("\"")) 
		{ 
			query = "UPDATE customCharacters " + 
						" SET timeCreated = CURRENT_TIMESTAMP " + 
						" WHERE name = " + "\"" + name + "\"" +" AND user_Id = " + userId + " AND server_Id = " + serverId;
		
		}
		else 
		{
			query = "UPDATE customCharacters " + 
					" SET timeCreated = CURRENT_TIMESTAMP " + 
					"WHERE name = " + "\'" +  name + "\'" + " AND user_Id = " + userId + " AND server_Id = " + serverId;
			
			
		}
		Statement stat = null;
		Connection conn  = null; 
		try 
		{
			conn = DriverManager.getConnection(urlDbs , AdminName , password); 
			stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			stat.execute(query); 
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
}