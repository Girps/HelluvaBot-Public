package CharactersPack;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;


 

/* Class will hold a series of functions to help  
 * query characters from the database 
 * */ 

 

public class CharacterSelection {
	public static Connection conn; 
	
	public CharacterSelection(Connection arg)
	{
		conn = arg;  
	}
	
	
	/*Method to get all characters on the database */
	public ArrayList<String> getAllCharacterNames(GAMETYPE type) throws SQLException
	{
		// Query 
		Statement stat = conn.createStatement(); 
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
					+ "SELECT name FROM gamecharacters\r\n"
					+ "WHERE gamecharacters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inKDM = \"T\" "
					+ "UNION "
					+ "SELECT name FROM customCharacters\r\n"
					+ "WHERE inKDM = \"T\"" ; 
		break; 
		case SMASHPASS :
			query = "SELECT name FROM characters \r\n"
					+ "WHERE characters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM gamecharacters\r\n"
					+ "WHERE gamecharacters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inSP = \"T\"\r\n"
					+ "UNION "
					+ "SELECT name FROM customCharacters\r\n"
					+ "WHERE inSP = \"T\""; 
			break; 
		case FAVORITES :
			query = "SELECT name FROM characters \r\n"
					+ "UNION \r\n"
					+ "SELECT name FROM gamecharacters\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inFav = \"T\" " 
					+ "UNION "
					+ "SELECT name FROM customCharacters\r\n"
					+ "WHERE inFav = \"T\""; 
			break; 
		}
		ResultSet res = stat.executeQuery(query); 
		
		res.next(); 
		
		ArrayList<String> names = new ArrayList<String>(); 
		
		do 
		{
			names.add(res.getString(1));
		}
		while(res.next()); 
		
		return names;
	}
	
	/*Method to get all OC names on the table */
	public ArrayList<String> getUsersOCName(Long userId, Long serverId) throws SQLException
	{
		ArrayList<String> names = new ArrayList<String>();
		
		String query = "SELECT name FROM customCharacters " 
		+ "WHERE user_Id = " + userId + " AND server_Id = " + serverId ;  
		
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		res.next(); 
		do 
		{
			names.add(res.getString(1));
		}
		while(res.next()); 
		
		return names; 
	}
	
	public ArrayList<Character> getAllCharacters(SELECTIONTYPE type ,SETUPTYPE set) throws SQLException
	{
		Statement stat = conn.createStatement();
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
		
		ResultSet res = stat.executeQuery(query); 
		
		ArrayList<Character> list = new ArrayList<Character>();  
		
		res.next(); 
		
		int col = res.getMetaData().getColumnCount(); 
		
		String[] columnData = new String[col+1]; 
		
		do {
			
			for(int i =1; i <= col; ++i) 
			{
				columnData[i] = res.getString(i); 
			}
			System.out.println(columnData[2]);
			CharacterFactory factor = new CharacterFactory(Long.valueOf(columnData[1]), columnData[2], columnData[3],columnData[5],set);
			
			Character chtr = factor.getCharacter(); 
			// Done get data now instantiate character 
			list.add(chtr ); 
			
		}while(res.next()); 
		
		return list; 
	}
	
	/* Method seraches if user has a waifu in a specifed guild */ 
	public boolean searchUserInWaifu(Long userID, Long serverID) throws SQLException 
	{
		
		// Query the waifu database 
		
		Statement stat = conn.createStatement(); 
		
		String query = "SELECT * FROM waifus " + 
						"WHERE user_id = " + userID + " AND server_id = " + serverID + 
						" LIMIT 1";  
		
		ResultSet res = stat.executeQuery(query); 
		
		// Found then return true otherwise false 
		return (res.next()) ?  true : false;   
		
	}
	
	/* Method to update character of a user in the waifu table */
	public void updateWaifuCharacter(Long userID, Long serverID, Character chtr) throws SQLException
	{
		String query = ""; 
	
		// Query the waifu database 
		
		Statement stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE); 
				
		query = "SELECT * FROM waifus " + 
				"WHERE user_id = " + userID + " AND server_id = " + serverID + 
				" LIMIT 1";  
				
		ResultSet res = stat.executeQuery(query); 
		
		// Now update the query [ userid, serverid, id] 
		res.first(); 
		res.updateLong(3, chtr.getId());
		res.updateRow(); // update the row
	}
	
	
	/* Method adds user and there character to waifu table */ 
	public void insertWaifu(Long userID, Long serverID, Character chtr) throws SQLException 
	{
		String query = "INSERT waifus (user_id, server_id, waifu_id)" + 
						" VALUES(" + userID + "," + serverID + "," + chtr.getId() + ")";
		
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		 
		
		// Now insert the data 
		stat.executeUpdate(query); 
		
	}
	
	/* Method gets a user's character waifu from a specified guild */ 
	public Character getUserWaifu(Long userID, Long serverID) throws SQLException
	{
		Statement stat = conn.createStatement(); 
		
		String query = "SELECT waifu_id FROM waifus " + 
				"WHERE user_id = " + userID + " AND server_id = " + serverID + 
				" LIMIT 1";
		
		ResultSet res = stat.executeQuery(query);
		
		// Have the character now find it in the characters table 
		res.next(); 
		
		Integer id = Integer.valueOf(res.getString(1));
		
		// Now get the character with this id 
		
		query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters\r\n"
				+ " WHERE characters.char_Id = " + id.toString()
				+ " UNION\r\n"
				+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters\r\n"
				+ " WHERE gamecharacters.gameCharacter_Id = " + id.toString() 
				+ " UNION \r\n"
				+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas\r\n"
				+ " WHERE  sonas.sona_Id = " + id.toString()
				+ " UNION " + 
				 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
				 " WHERE cusChar_Id = " + id.toString() ;  
		
		
		res = stat.executeQuery(query); 
		res.next();
		// We have the character now return it 
		CharacterFactory factory = new CharacterFactory(Long.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
		Character found = factory.getCharacter(); 
		// Now get the date for next switch 
		query = "SELECT * FROM timeTable";
		
		res = stat.executeQuery(query);
		
		res.next(); 
		
		Date date = res.getTimestamp(1); 
		
		
		found.setDate(date);
		
		return found;
	}
	
	
	
	public  Character[] getRandomCharacters(GAMETYPE type, SETUPTYPE set, int n) throws SQLException 
	{
		Character[] found = null; 
		String query = ""; 
		switch(type) 
		{
		case KDM: 
			query =  "SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters " +  
					 " WHERE characters.is_Adult = \"T\"" + 
					 " UNION " + 
					 " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters " +
					 " WHERE gamecharacters.is_Adult = \"T\"" + 
					 " UNION " + 
					 " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas " + 
					 " WHERE sonas.inKDM = \"T\" " +  
					 " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inKDM = \"T\" " +  
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 

			break; 
		case SIMPS:
			query = "SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters "
					+ " WHERE characters.is_Adult = \"T\" "
					+ " UNION\r\n"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters "
					+ " WHERE gamecharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas "
					+ " WHERE sonas.inSimps = \"T\" "
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inSimps = \"T\" " +  
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case SHIPS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE gamecharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inShips = \"T\"" 
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inShips = \"T\" " +  
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case KINS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inKins = \"T\" "
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inKins = \"T\" " +  
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case SMASHPASS: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters "
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE gamecharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inSP = \"T\" "
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inSP = \"T\" " +  
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case WAIFU:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE characters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE gamecharacters.is_Adult = \"T\" "
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inWaifu = \"T\""
					+ " UNION " + 
					 " SELECT cusChar_Id, name, user_Id, url FROM customCharacters " + 
					 " WHERE inWaifu = \"T\" " +  
					 " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case GUESS: 
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inGuess = \"T\" "
					+ " UNION "  
					+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters "  
					+ " WHERE inGuess = \"T\" "   
					+ " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		}
		
		return found;  
	}
	
	
	

	/* Return a single character type of character based on enum SELECTIONTYPE */
	public  Character requestSingleCharacter(String name, GAMETYPE type, SETUPTYPE set) throws SQLException 
	{
		
		System.out.println(type.toString()); 
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
				+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
				+ " WHERE LOWER(gamecharacters.name) = LOWER(" +"\"" + name + "\"" + ") AND gamecharacters.is_Adult = \"T\""
				+ " UNION"
				+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
				+ " WHERE  LOWER(sonas.name) = LOWER("+ "\"" + name + "\"" +") AND sonas.inKDM = \"T\""
				+ " UNION "
				+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
				+ " WHERE  LOWER(name) = LOWER("+ "\"" + name + "\"" +") AND inKDM = \"T\""
				+ "  LIMIT 1"; 
		}
		else 
		{
			
			query  =
					 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE LOWER(characters.name) = LOWER(\'" + name + "\') AND characters.is_Adult = \"T\" "
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE LOWER(gamecharacters.name) = LOWER(\'"+ name+ "\') AND gamecharacters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE  LOWER(sonas.name) = LOWER(\'" + name +"\') AND sonas.inKDM = \"T\""
					+ " UNION "
				+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
				+ " WHERE  LOWER(name) = LOWER("+ "\'" + name + "\'" +") AND inKDM = \"T\""
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
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE LOWER(gamecharacters.name) = LOWER(" + "\"" + name + "\"" + ") AND gamecharacters.is_Adult = \"T\""
					+ " UNION"
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE  LOWER(sonas.name) = LOWER(" +"\"" +name + "\"" +") AND sonas.inSP = \"T\""
					+ " UNION "
					+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
					+ " WHERE  LOWER(name) = LOWER("+ "\"" + name + "\"" +") AND inSP = \"T\""
					+ "  LIMIT 1"; 
			}
			else 
			{
				
				query  = 
						 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
						+ " WHERE LOWER(characters.name) = LOWER(\'" + name + "\') AND characters.is_Adult = \"T\" "
						+ " UNION"
						+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
						+ " WHERE LOWER(gamecharacters.name) = LOWER(\'"+ name+ "\') AND gamecharacters.is_Adult = \"T\""
						+ " UNION"
						+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
						+ " WHERE  LOWER(sonas.name) = LOWER(\'" + name +"\') AND sonas.inSP = \"T\""
						+ " UNION "
						+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
						+ " WHERE  LOWER(name) = LOWER("+ "\'" + name + "\'" +") AND inSP = \"T\""
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
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE LOWER(gamecharacters.name) = LOWER(" + "\'" + name + "\'" + ") " + " LIMIT 1"; 
		}
		else 
		{
			query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " WHERE LOWER(characters.name) = LOWER( \""  + name  + "\") "
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " WHERE LOWER(gamecharacters.name) = LOWER( \"" + name + "\") " + " LIMIT 1"; 
		}
			found = processQueryGetCharacters(query,set)[0]; 
			break;
		case FAVORITES: 
			if(name.contains("\""))
			{
				query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
						+ " WHERE LOWER(characters.name) = LOWER(" + "\'" + name + "\'" + ") "
						+ " UNION"
						+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
						+ " WHERE LOWER(gamecharacters.name) = LOWER(" + "\'" + name + "\'" + ") "
						+ "UNION"
						+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
						+ " WHERE  LOWER(sonas.name) = LOWER(\'" + name +"\') AND sonas.inFav = \"T\""
						+ " UNION "
						+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
						+ " WHERE  LOWER(name) = LOWER("+ "\'" + name + "\'" +") AND inFav = \"T\""
						+ "  LIMIT 1"; 
			}
			else 
			{
				query  = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
						+ " WHERE LOWER(characters.name) = LOWER( \""  + name  + "\") "
						+ " UNION"
						+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
						+ " WHERE LOWER(gamecharacters.name) = LOWER( \"" + name + "\") " 	
						+ " UNION "
						+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
						+ " WHERE LOWER(sonas.name) = LOWER(\"" + name + "\"" + ") AND sonas.inFav = \"T\""
						+ " UNION "
						+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
						+ " WHERE  LOWER(name) = LOWER("+ "\"" + name + "\"" + ") AND inFav = \"T\""
						+ "  LIMIT 1";  
			}
				found = processQueryGetCharacters(query,set)[0]; 
		default:	
		}
		return found; 
	}
	
	
	
	
	
	/* Request from database a character from given string */ 
	private static Character[] processQueryGetCharacters(String query, SETUPTYPE type) throws SQLException 
	{
		String q = query;
		Statement stat = conn.createStatement(); 
		ResultSet  res = null; 
		ResultSet timeRes = null;
		
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
			
		}while(res.next()); 
		
		
		
		
		// Return as arraylist
		Character[] arr = list.toArray(new Character[list.size()]); 
		
		return arr; 
	}
	

	/* Method seraches if user has a sona in a specifed guild */ 
	public boolean searchUserInSona(Long userID, Long serverID) throws SQLException 
	{
		
		// Query the waifu database 
		
		Statement stat = conn.createStatement(); 
		
		String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = " + userID + " AND server_Id = " + serverID + 
						" LIMIT 1";  
		
		ResultSet res = stat.executeQuery(query); 
		
		// Found then return true otherwise false 
		return (res.next()) ?  true : false;   
	}
	
	/* Method gets the characters from the database */
	public Character getUserSona(Long userID, Long serverID) throws SQLException
	{
		Statement stat = conn.createStatement(); 
		String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = " + userID + " AND server_Id = " + serverID + 
						 " LIMIT 1";
		ResultSet res = stat.executeQuery(query); 
		
		res.next(); 
		
				
		// We have the character now return it 
		CharacterFactory factory = new CharacterFactory(Long.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
		Character found = factory.getCharacter(); 
		
		return found; 
	}
	
	// Remove sona from the table 
	public boolean removeSona(Long userId, Long serverId) throws SQLException 
	{
		Statement stat = conn.createStatement();
		String query = "DELETE character_ids FROM character_ids \r\n"
				+ "INNER JOIN sonas ON sonas.sona_Id = character_ids.id \r\n"
				+ "WHERE sonas.user_Id = " + userId +   " AND sonas.server_Id = " + serverId; 
		boolean result =  !stat.execute(query); 
		return result; 
	}
	
	// Remove waifu from the table
	public boolean removeWaifu(Long  userID, Long serverID) throws SQLException 
	{
		Statement stat = conn.createStatement();
		String query = "DELETE FROM waifus " + 
		"WHERE user_Id = " + userID + " AND " + "server_Id = " + serverID; 
		boolean result =  !stat.execute(query); 
		return result; 
	}
	
	/* Method will insert a character to the database on the sona table */ 
	public void insertSona(String name, Long userId, String url , Long serverId, String inKDM, String inSP, String inSimps,String inShips, String
			inKins, String inWaifu, String inFav, String inGuess) throws SQLException 
	{
		String queryOne = " INSERT INTO character_Ids(id) VALUES(NULL)"; 
		  System.out.println(serverId); 
		String queryTwo = "";   
		if(!name.contains("\"")) 
		{  
			queryTwo = " INSERT INTO sonas (sona_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess ) " +  
				 " VALUES (last_insert_id()," + "\"" + name + "\"" +  "," + userId +  "," + "\"" +  url + "\"" + " , "  + serverId + " , " + "\"" +inKDM + "\"" + "," + "\"" + inSP + "\"" + " ," + "\"" + inSimps +"\"" + ", "+  "\"" + inShips + "\"" +"," + "\""+  inKins + "\"" 
				+", " + "\"" + inWaifu + "\"" + "," + "\"" + inFav + "\"" + "," + "\"" + inGuess + "\"" +  " )";  
		}
		else 
		{
			queryTwo = " INSERT INTO sonas (sona_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav ) " +  
					 " VALUES (last_insert_id()," + "\'" + name + "\'" +  "," + userId +  "," + "\'" +  url + "\'" + " , " + serverId + " , " + "\'" +inKDM + "\'" + "," + "\'" + inSP + "\'" + " ," + "\'" + inSimps +"\'" + ", "+  "\'" + inShips + "\'" +"," + "\'"+  inKins + "\'" 
					+", " + "\'" + inWaifu + "\'" + "," + "\'" + inFav + "\'" + "," + "\'" + inGuess + "\'" + " )";  
		}
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		
		stat.addBatch(queryOne);
		stat.addBatch(queryTwo);
		
		stat.executeBatch();
	}
	
	
	public void removeAllSonas(Long  guildId) throws SQLException 
	{
		// rid of sonas
		String query = "DELETE character_ids FROM character_ids \r\n"
				+ "INNER JOIN sonas ON sonas.sona_Id = character_ids.id\r\n"
				+ "WHERE sonas.server_Id = " + guildId ; 
		Statement stat = conn.createStatement(); 
		stat.execute(query); 
		
		
		
	}
	
	public void removeAllWaifus(Long idGuild) throws SQLException
	{
		// rid of waifus
		String query = "DELETE FROM waifus " 
				+ "WHERE waifus.server_id = " +  idGuild ;
		Statement stat = conn.createStatement(); 
		stat.execute(query); 
		
	}
	
	/* Get character ids from the data base*/ 
	public int getCharacterId(String name ) throws SQLException 
	{
		String query = ""; 
		if(!name.contains("\"")) {
			query = "SELECT characters.char_Id FROM characters \r\n"
				+ "WHERE characters.name = \"" + name + "\"\r\n"
				+ "UNION\r\n"
				+ "SELECT gamecharacters.gameCharacter_Id FROM gamecharacters\r\n"
				+ "WHERE gamecharacters.name = \"" + name + "\"\r\n"
				+ "UNION\r\n"
				+ "SELECT sonas.sona_Id FROM sonas\r\n"
				+ "WHERE sonas.name = \"" + name + "\"\r\n"
				+ "LIMIT 1";
		}
		else 
		{
			query = "SELECT characters.char_Id FROM characters \r\n"
					+ "WHERE characters.name = \'" + name + "\'\r\n"
					+ "UNION\r\n"
					+ "SELECT gamecharacters.gameCharacter_Id FROM gamecharacters\r\n"
					+ "WHERE gamecharacters.name = \'" + name + "\'\r\n"
					+ "UNION\r\n"
					+ "SELECT sonas.sona_Id FROM sonas\r\n"
					+ "WHERE sonas.name = \'" + name + "\'\r\n"
					+ "LIMIT 1";
		}
		Statement stat = conn.createStatement(); 
		ResultSet res =  stat.executeQuery(query);
		res.next();
		int value = Integer.valueOf(res.getString(1));
		return value; 
	}
	
	// Method inserts a single character in the favorites table 
	public void insertFavorite(String name , Long  userId, Long serverId) throws SQLException
	{
		Character temp = this.requestSingleCharacter(name, GAMETYPE.FAVORITES, SETUPTYPE.LIGHT); 
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
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		
		stat.execute(query); 
		
	}
	
	/* Return list of characters */ 
	public ArrayList<Character> getFavoritesList(Long userId, Long serverId) throws SQLException
	{
		CharacterSelection select = new CharacterSelection(conn); 
		ArrayList<Character > list = new ArrayList<Character>(); 
		String query = "SELECT fav_Id FROM favorites " 
				+ "WHERE user_Id = " + userId + 
				" AND server_Id = " + serverId + 
				 " ORDER BY timeCreated DESC ";
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		res.next(); 
		do 
		{ 
			Long id = Long.valueOf(res.getString(1)); 
			System.out.println(id); 
			Character temp = select.getCharacterById(id); 
			list.add(temp); 
		} 
		while(res.next()); 
		
		return list; 
	}
	
	public String getTitleList(Long userId, Long serverId) throws SQLException
	{
		String query = "SELECT * FROM favorites " 
				+ "WHERE user_id = " + userId + 
				" AND server_id = "  + serverId ;
		
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		res.next(); 
		String title =  new String(res.getString(5)); 
		return title; 
	}
	
	public Character getCharacterById(Long id) throws SQLException 
	{
		String query  = 
				 " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
				+ " WHERE char_id = " + id
				+ " UNION"
				+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
				+ " WHERE gameCharacter_Id = " + id
				+ " UNION"
				+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
				+ " WHERE  sona_Id = " + id
				+ " UNION"
				+ " SELECT cusChar_Id, name, user_Id, url FROM customCharacters"
				+ " WHERE  cusChar_Id = " + id
				+ "  LIMIT 1"; 
		
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		CharacterFactory factory = null; 
		res.next(); 
			factory = new CharacterFactory(Long.valueOf(res.getString(1)),res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
			return factory.getCharacter(); 
		
		
	}
	
	/* Remove all characters of a user in the table */ 
	public void removeFavList(Long userId, Long serverId) throws SQLException
	{
		String query = "DELETE FROM favorites\r\n"
				+ " WHERE favorites.user_Id = " + userId + " AND favorites.server_Id = " + serverId ; 
		Statement stat = conn.createStatement(); 
		
		stat.execute(query); 
	}
	
	/* Remove list from the database */ 
	public void removeFavCharacter(String name, Long userId, Long serverId) throws SQLException
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
		Statement stat = conn.createStatement(); 
		
		stat.execute(query); 
	}
	
	/* Search for favorites list from database */
	public boolean checkFavLimit(Long userId, Long serverId) throws SQLException 
	{
		String query = "SELECT COUNT(name) FROM favorites "
				+ "WHERE user_Id = " + userId + " AND " + "server_Id = " + serverId ; 
		
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		res.next(); 
		int value = res.getInt(1); 
		
		return (value < 10) ? true : (false); 
	}
	
	/*  Check user has a character in the server */ 
	public boolean checkFavList(Long userId, Long serverId) throws SQLException {
		String query = "SELECT * FROM favorites " + 
				"WHERE user_Id = "  +  userId + " AND server_Id = " + serverId ;
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		return res.next(); 
	}
	
	
	public boolean isAvailable(String name, Long userId, Long serverId) throws SQLException
	{
		String query = ""; 
		if(!name.contains("\"")) 
		{
			query = "SELECT characters.name FROM characters \r\n"
					+ " WHERE characters.name = " +  "\"" + name + "\""+ " \r\n"
					+ " UNION\r\n"
					+ " SELECT gamecharacters.name FROM gamecharacters \r\n"
					+ " WHERE gamecharacters.name = " + "\"" + name + "\"" + "\r\n"
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
					+ " SELECT gamecharacters.name FROM gamecharacters \r\n"
					+ " WHERE gamecharacters.name = " + "\'" + name + "\'" + "\r\n"
					+ " UNION \r\n"
					+ "SELECT sonas.name FROM sonas\r\n"
					+ "WHERE sonas.name = " + "\'" + name +"\'" + " AND sonas.server_Id = " + serverId + " AND sonas.user_Id = " +  userId 
					+ " UNION  "
					+ " SELECT customCharacters.name FROM customCharacters\r\n"
					+ " WHERE customCharacters.name = " + "\'" + name + "\'" + " AND customCharacters.server_Id = " + serverId + " AND customCharacters.user_Id = " + userId ;
		}
		
		Statement stat = conn.createStatement(); 
		
		ResultSet res = stat.executeQuery(query); 
		
		return res.next(); 
	}


	/* Insert orginal character into the custom character table */ 
	public void insertOrginalCharacter(String name, Long userId, String url , Long serverId, String inKDM, String inSP, String inSimps,String inShips, String
			inKins, String inWaifu, String inFav, String inGuess) throws SQLException 
	{
		String queryOne = " INSERT INTO character_Ids(id) VALUES(NULL)"; 
		  System.out.println(serverId); 
		String queryTwo = "";   
		if(!name.contains("\"")) 
		{  
			queryTwo = " INSERT INTO customCharacters (cusChar_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess ) " +  
				 " VALUES (last_insert_id()," + "\"" + name + "\"" +  ","  + userId +  "," + "\"" +  url + "\"" + " , " + serverId + " , " + "\"" +inKDM + "\"" + "," + "\"" + inSP + "\"" + " ," + "\"" + inSimps +"\"" + ", "+  "\"" + inShips + "\"" +"," + "\""+  inKins + "\"" 
				+", " + "\"" + inWaifu + "\"" + "," + "\"" + inFav + "\"" + " ," + "\"" + inGuess + "\"" + " )";  
		}
		else 
		{
			queryTwo = " INSERT INTO customCharacters (cusChar_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu, inFav, inGuess ) " +  
					 " VALUES (last_insert_id()," + "\'" + name + "\'" +  "," + userId +  "," + "\'" +  url + "\'" + " , " + serverId + " , " + "\'" +inKDM + "\'" + "," + "\'" + inSP + "\'" + " ," + "\'" + inSimps +"\'" + ", "+  "\'" + inShips + "\'" +"," + "\'"+  inKins + "\'" 
					+", " + "\'" + inWaifu + "\'" + "," + "\'" + inFav + " ," + "\'" + inGuess + "\'" + " )";  
		}
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		
		stat.addBatch(queryOne);
		stat.addBatch(queryTwo);
		
		stat.executeBatch();
	}
	
	/* Check how many characters the user has in this server */ 
	public boolean checkOCLimit(Long userId, Long serverId) throws SQLException 
	{
		String query = "SELECT COUNT(name) FROM customCharacters "
				+ "WHERE customCharacters.user_Id = " + userId + " AND " + "customCharacters.server_Id = " + serverId ; 
		
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		res.next(); 
		int value = res.getInt(1); 
		
		return (value < 10) ? true : (false); 
	}
	
	/* Remove custom characters from the table */ 
	public void removeCustomCharacter(String name, Long userId, Long serverId) throws SQLException
	{
		String query =""; 
		if(!name.contains("\""))
		{
		query = "DELETE character_ids FROM character_ids \r\n"
				+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_ids.id\r\n"
				+ "WHERE customCharacters.user_Id = " + userId + " AND customCharacters.server_Id = " + serverId + " AND customCharacters.name = " + "\"" + name + "\""; 
		}
		else 
		{
			query = "DELETE character_ids FROM character_ids \r\n"
					+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_ids.id\r\n"
					+ "WHERE customCharacters.user_Id = " + userId + " AND customCharacters.server_Id = " + serverId + " AND customCharacters.name = " + "\'" + name + "\'"; 
		}
		Statement stat = conn.createStatement(); 
		
		stat.execute(query); 
		
	}

	/* Search OC's name on the table */  
	public boolean searchOC(String name, Long userId, Long serverId) throws SQLException {
		
		String query = "SELECT * FROM customCharacters "  
					+ "WHERE user_Id = "  + userId + " AND server_Id = " + serverId + " AND name = " + "\"" + name + "\"" ; 
		Statement stat = conn.createStatement();
		ResultSet res = stat.executeQuery(query); 
		return res.next(); 
	}

	/*
	 * Return a list of OC characters from a user in a server 
	 * */ 
	public ArrayList<Character> getOCList(Long userId, Long serverId) throws SQLException
	{
		String query = "SELECT cusChar_Id , name, url  FROM customCharacters " + 
				"WHERE user_Id = "  + userId + " AND server_Id = " + serverId  + 
				" ORDER BY cusChar_Id DESC";  
	 Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		
		ArrayList<Character> list = new ArrayList<Character>(); 
		// Check if false 
		if(!res.next()) 
		{
			return null; 
		}
		CharacterFactory factory = null; 
		do 
		{
			factory = new CharacterFactory(Long.valueOf( res.getString(1)), res.getString(2), "OC" ,res.getString(3), SETUPTYPE.LIGHT); 
			list.add(factory.getCharacter()); 
		}
		while(res.next()); 
		
		return list; 
	}

	/* Getting single oc from a users' list */  
	public Character getOC(String characterName, Long userId, Long serverId) throws SQLException {
		
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
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		
		if(!res.next()) 
		{
			return null; 
		}
		CharacterFactory factory = new CharacterFactory(Long.valueOf( res.getString(1)), res.getString(2), "OC" ,res.getString(3), SETUPTYPE.LIGHT); 
		return factory.getCharacter(); 
	}

	/* Check if User has any Ocs */ 
	public boolean searchAllUserOcs(Long userId, Long serverId) throws SQLException {
		String query = "SELECT * FROM customCharacters " 
					+ "WHERE user_Id = " + userId + " AND server_Id = " + serverId ;  
		Statement stat = conn.createStatement(); 
		ResultSet res = stat.executeQuery(query); 
		return res.next(); 
	}

	/* Removes all ocs from a user */ 
	public void removeAllOcs(Long userId, Long serverId) throws SQLException {
		String query = "DELETE FROM customCharacters\r\n"
				+ " WHERE user_Id = " + userId + " AND server_Id = " + serverId ; 
		Statement stat = conn.createStatement(); 
		
		stat.execute(query); 
		
	}

	/* Update favorite list */ 
	public void changeFavTitle(String title, Long userId, Long serverId) throws SQLException 
	{
		String query = ""; 
		if(!title.contains("\"")) {
		query = " UPDATE favorites " + " SET title = " + "\"" + title + "\"" +  
				" WHERE user_Id = " + userId  + " AND " + serverId ;  
		}
		else 
		{
			query = " UPDATE favorites " + " SET title = " + "\'" + title + "\'" +  
					" WHERE user_Id = " + userId  + " AND " + serverId ;  
		}
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		stat.execute(query); 
	}

	// Return list of names in a users favorite list
	public ArrayList<String> getFavListNames(Long userId, Long serverId) throws SQLException {
		ArrayList<String> names = new ArrayList<String>(); 
		String query = " SELECT name FROM favorites " + 
				"WHERE user_Id = " + userId + " AND " + "server_Id = " + serverId;  
		Statement stat = conn.createStatement(); 
		ResultSet  res =stat.executeQuery(query); 
		res.next();
		do
		{
			names.add(res.getString(1));
		}
		while(res.next()); 
		return names; 
	}


	public void removeAllOcsInGuild(Long guildId) throws SQLException {
		
		Statement stat = conn.createStatement();
		String query = "DELETE character_ids FROM character_ids \r\n"
				+ "INNER JOIN customCharacters ON customCharacters.cusChar_Id = character_ids.id \r\n"
				+ " WHERE customCharacters.server_Id = " + guildId; 
		stat.execute(query); 

	}


	public void removeFavListGuild(Long guildId) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "DELETE FROM favorites " 
				+ " WHERE server_Id = " + guildId; 
		stat.execute(query); 
		
	}

	


}