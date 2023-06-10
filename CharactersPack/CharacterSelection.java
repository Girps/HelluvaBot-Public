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
					+ "WHERE sonas.inKDM = \"T\""; 
		break; 
		case SMASHPASS :
			query = "SELECT name FROM characters \r\n"
					+ "WHERE characters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM gamecharacters\r\n"
					+ "WHERE gamecharacters.is_Adult = \"T\"\r\n"
					+ "UNION\r\n"
					+ "SELECT name FROM sonas\r\n"
					+ "WHERE sonas.inSP = \"T\"; "; 
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
			
			CharacterFactory factor = new CharacterFactory(Integer.valueOf(columnData[1]), columnData[2], columnData[3],columnData[5],set);
			
			Character chtr = factor.getCharacter(); 
			// Done get data now instantiate character 
			list.add(chtr ); 
			
		}while(res.next()); 
		
		return list; 
	}
	
	/* Method seraches if user has a waifu in a specifed guild */ 
	public boolean searchUserInWaifu(String userID, String serverID) throws SQLException 
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
	public void updateWaifuCharacter(String userID, String serverID, Character chtr) throws SQLException
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
		res.updateInt(3, chtr.getId());
		res.updateRow(); // update the row
	}
	
	
	/* Method adds user and there character to waifu table */ 
	public void insertWaifu(String userID, String serverID, Character chtr) throws SQLException 
	{
		String query = "INSERT waifus (user_id, server_id, waifu_id)" + 
						" VALUES(" + userID + "," + serverID + "," + chtr.getId() + ")";
		
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		 
		
		// Now insert the data 
		stat.executeUpdate(query); 
		
	}
	
	/* Method gets a user's character waifu from a specified guild */ 
	public Character getUserWaifu(String userID, String serverID) throws SQLException
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
				+ "  LIMIT 1  "; 
		
		
		res = stat.executeQuery(query); 
		res.next();
		// We have the character now return it 
		CharacterFactory factory = new CharacterFactory(Integer.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
		Character found = factory.getCharacter(); 
		// Now get the date for next switch 
		query = "SELECT * FROM timeTable";
		
		res = stat.executeQuery(query);
		
		res.next(); 
		
		Date date = res.getTimestamp(1); 
		
		
		found.setDate(date);
		
		return found;
	}
	
	/* Function will serve to return a random character the condition is determined by the enum type 
	 * on what character we would like */ 
	public  Character getRandomCharacter(SELECTIONTYPE type, SETUPTYPE set) throws SQLException 
	{
		Character found = null; 
		String query = ""; 
		switch(type) 
		{
		case ALL: 
			query = "SELECT * FROM characters " + 
					"ORDER BY RAND() " + "LIMIT 1"; 
			found = processQueryGetCharacters(query,set)[0]; 

			break; 
		case ADULT:
			query = "SELECT * FROM characters " + "WHERE is_Adult = \"T\"" +  
					"ORDER BY RAND() " + "LIMIT 1"; 
			found = processQueryGetCharacters(query,set)[0]; 
			break; 
		case MINOR:
			query = "SELECT * FROM characters " + "WHERE is_Adult = \"F\"" +  
					"ORDER BY RAND() " + "LIMIT 1"; 
			found = processQueryGetCharacters(query,set)[0]; 
			break; 
		case HAZBIN:
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Hazbin Hotel\" "
					+ "ORDER BY RAND() "
					+ "LIMIT 1 "; 
			found = processQueryGetCharacters(query,set)[0]; 
			break; 
		case HELLUVA:
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Helluva Boss\" "
					+ "ORDER BY RAND() "
					+ "LIMIT 1"; 
			found = processQueryGetCharacters(query,set)[0]; 
			break; 
		}
		
		
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
					+ " ORDER BY RAND() LIMIT " + n; 
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
					+ " ORDER BY RAND() LIMIT " + n; 
			found = processQueryGetCharacters(query,set); 
			break; 
		case KINS:
			query = " SELECT characters.char_Id, characters.name, characters.show_Name, characters.is_Major_Character FROM characters"
					+ " UNION"
					+ " SELECT gamecharacters.gameCharacter_Id, gamecharacters.name, gamecharacters.show_Name, gamecharacters.imgur_Url FROM gamecharacters"
					+ " UNION "
					+ " SELECT sonas.sona_Id, sonas.name, sonas.user_Id, sonas.url FROM sonas"
					+ " WHERE sonas.inKins = \"T\" "
					+ " ORDER BY RAND() LIMIT " + n;  
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
					+ " ORDER BY RAND() LIMIT " + n;
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
		int col = res.getMetaData().getColumnCount() + 1;   
		
		
		
		do {
	
			CharacterFactory factory = null; 
			
				factory = new CharacterFactory(Integer.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4),type); 
			
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
	public boolean searchUserInSona(String userID, String serverID) throws SQLException 
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
	public Character getUserSona(String userID, String serverID) throws SQLException
	{
		Statement stat = conn.createStatement(); 
		String query = "SELECT * FROM sonas " + 
						"WHERE user_Id = " + userID + " AND server_Id = " + serverID + 
						 " LIMIT 1";
		ResultSet res = stat.executeQuery(query); 
		
		res.next(); 
		
				
		// We have the character now return it 
		CharacterFactory factory = new CharacterFactory(Integer.valueOf(res.getString(1)), res.getString(2), res.getString(3), res.getString(4), SETUPTYPE.LIGHT); 
		Character found = factory.getCharacter(); 
		
		return found; 
	}
	
	// Remove sona from the table 
	public boolean removeSona(String userID, String serverID) throws SQLException 
	{
		Statement stat = conn.createStatement();
		String query = "DELETE FROM sonas " + 
		"WHERE user_Id = " + userID + " AND " + "server_Id = " + serverID; 
		boolean result =  !stat.execute(query); 
		return result; 
	}
	
	// Remove waifu from the table
	public boolean removeWaifu(String userID, String serverID) throws SQLException 
	{
		Statement stat = conn.createStatement();
		String query = "DELETE FROM waifus " + 
		"WHERE user_Id = " + userID + " AND " + "server_Id = " + serverID; 
		boolean result =  !stat.execute(query); 
		return result; 
	}
	
	/* Method will insert a character to the database on the sona table */ 
	public boolean insertSona(String name, String userId, String url , String serverId, String inKDM, String inSP, String inSimps,String inShips, String
			inKins, String inWaifu) throws SQLException 
	{
		String queryOne = " INSERT INTO character_Ids(id) VALUES(NULL)"; 
		  System.out.println(serverId); 
		String queryTwo = " INSERT INTO sonas (sona_Id, name, user_Id, url, server_Id, inKDM, inSP, inSimps, inShips, inKins, inWaifu) " +  
				 " VALUES (last_insert_id()," + "\"" + name + "\"" +  "," + "\"" + userId + "\"" +  "," + "\"" +  url + "\"" + " , " + "\"" + serverId + "\"" + " , " + "\"" +inKDM + "\"" + "," + "\"" + inSP + "\"" + " ," + "\"" + inSimps +"\"" + ", "+  "\"" + inShips + "\"" +"," + "\""+  inKins + "\"" +", " + "\"" + inWaifu + "\"" + ")";  
		
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		
		stat.addBatch(queryOne);
		stat.addBatch(queryTwo);
		
		int[] arr = stat.executeBatch();
		
		if(arr.length > 0 ) 
		{
			return true; 
		}
		else 
		{
			return false; 
		}
	}
	
	
	public void removeAllSonas(String id) throws SQLException 
	{
		// rid of sonas
		String query = "DELETE FROM sonas " 
						+ "WHERE sonas.server_Id= " + id; 
		Statement stat = conn.createStatement(); 
		stat.execute(query); 
		
		
		
	}
	
	public void removeAllWaifus(String id) throws SQLException
	{
		// rid of waifus
		String query = "DELETE FROM waifus " 
				+ "WHERE waifus.server_id = " +  id ;
		Statement stat = conn.createStatement(); 
		stat.execute(query); 
		
	}
	
}
