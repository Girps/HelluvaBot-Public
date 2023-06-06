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
	public ArrayList<String> getAllCharacterNames() throws SQLException
	{
		// Query 
		Statement stat = conn.createStatement(); 
		
		String query = "SELECT name FROM characters"; 
		
		ResultSet res = stat.executeQuery(query); 
		
		res.next(); 
		
		
		
		ArrayList<String> names = new ArrayList<String>(); 
		
		do {
		
			names.add(res.getString(1));
			System.out.println(res.getString(1)); 
		
		}
		while(res.next()); 
		
		return names;
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
		String query = "INSERT waifus (user_id, server_id, id)" + 
						" VALUES(" + userID + "," + serverID + "," + chtr.getId() + ")";
		
		Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		 
		
		// Now insert the data 
		stat.executeUpdate(query); 
		
		System.out.println("Inserted it to the waifu table database "); 
		
	}
	
	/* Method gets a user's character waifu from a specified guild */ 
	public Character getUserWaifu(String userID, String serverID) throws SQLException
	{
		Statement stat = conn.createStatement(); 
		
		String query = "SELECT id FROM waifus " + 
				"WHERE user_id = " + userID + " AND server_id = " + serverID + 
				" LIMIT 1";
		
		ResultSet res = stat.executeQuery(query);
		
		// Have the character now find it in the characters table 
		res.next(); 
		
		Integer id = Integer.valueOf(res.getString(1));
		
		// Now get the character with this id 
		
		query = "SELECT * FROM characters " + 
				"WHERE id = " + id.toString() + 
				" LIMIT 1";
		
		
		res = stat.executeQuery(query); 
		res.next(); 
		// now iterate 
		int columns = res.getMetaData().getColumnCount(); 
		
		String[] fields = new String[columns]; 
		
		for(int i = 1; i < columns; ++i) 
		{
			fields[i] = res.getString(i);  
		}
		
		// Now get the date for next switch 
		query = "SELECT * FROM timeTable";
		
		res = stat.executeQuery(query);
		
		res.next(); 
		
		Date date = res.getTimestamp(1); 
		
		// We have the character now return it 
		Character found = new Character(Integer.valueOf(fields[1]),fields[2]); 
		found.setDate(date);
		
		return found;
	}
	
	/* Function will serve to return a random character the condition is determined by the enum type 
	 * on what character we would like */ 
	public  Character getRandomCharacter(SELECTIONTYPE type) throws SQLException 
	{
		Character found = null; 
		String query = ""; 
		switch(type) 
		{
		case ALL: 
			query = "SELECT * FROM characters " + 
					"ORDER BY RAND() " + "LIMIT 1"; 
			found = processQueryGetCharacters(query)[0]; 

			break; 
		case ADULT:
			query = "SELECT * FROM characters " + "WHERE is_Adult = \"T\"" +  
					"ORDER BY RAND() " + "LIMIT 1"; 
			found = processQueryGetCharacters(query)[0]; 
			break; 
		case MINOR:
			query = "SELECT * FROM characters " + "WHERE is_Adult = \"F\"" +  
					"ORDER BY RAND() " + "LIMIT 1"; 
			found = processQueryGetCharacters(query)[0]; 
			break; 
		case HAZBIN:
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Hazbin Hotel\" "
					+ "ORDER BY RAND() "
					+ "LIMIT 1 "; 
			found = processQueryGetCharacters(query)[0]; 
			break; 
		case HELLUVA:
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Helluva Boss\" "
					+ "ORDER BY RAND() "
					+ "LIMIT 1"; 
			found = processQueryGetCharacters(query)[0]; 
			break; 
		}
		
		
		return found;  
	}
	
	
	/* Get a list of character of n size depending on the condition*/ 
	public Character[] getRandomCharacters(SELECTIONTYPE type, int n) throws SQLException 
	{
		Character list[] = null; 
		String query = ""; 
		switch(type) 
		{
		case ALL: 
			query = "SELECT * FROM characters " + 
					"ORDER BY RAND() " + "LIMIT " + n; 
			list = processQueryGetCharacters(query); 

			break; 
		case ADULT:
			query = "SELECT * FROM characters " + "WHERE is_Adult = \"T\"" +  
					"ORDER BY RAND() " + "LIMIT " + n; 
			list = processQueryGetCharacters(query); 
			break; 
		case MINOR:
			query = "SELECT * FROM characters " + "WHERE is_Adult = \"F\"" +  
					"ORDER BY RAND() " + "LIMIT " + n; 
			list = processQueryGetCharacters(query); 
			break; 
		case HAZBIN:
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Hazbin Hotel\" " + "AND is_Adult = \"T\" "   
					+ "ORDER BY RAND() "
					+ "LIMIT " + n; 
			list = processQueryGetCharacters(query); 
			break; 
		case HAZBIN_ADULT: 
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Hazbin Hotel\" " + "AND is_Adult = \"T\" "   
					+ "ORDER BY RAND() "
					+ "LIMIT " + n; 
			list = processQueryGetCharacters(query); 
			break; 
		case HELLUVA_ADULT: 
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Helluva Boss\" " + "AND is_Adult = \"T\" "   
					+ "ORDER BY RAND() "
					+ "LIMIT " + n; 
			list = processQueryGetCharacters(query); 
			break; 
		case HELLUVA:
			query = "SELECT * FROM characters "
					+ "WHERE show_name = \"Helluva Boss\" "
					+ "ORDER BY RAND() "
					+ "LIMIT " + n; 
			list = processQueryGetCharacters(query); 
			break; 
		}
		
		
		return list; 
	}
	

	/* Return a single character type of character based on enum SELECTIONTYPE */
	public  Character requestSingleCharacter(String name, SELECTIONTYPE type) throws SQLException 
	{
		Character found  = null; 
		String query = ""; 
		switch(type)
		{
		case ADULT:
		query  = "SELECT * FROM characters " + 
				"WHERE LOWER(name) = "  + "LOWER(" + "\"" + name  + "\""+ ")" + " AND is_Adult = " 
				+ "\"T\"" + " LIMIT 1";
		found = processQueryGetCharacters(query)[0]; 
			break; 
		
		case ALL: 
			query = "SELECT * FROM characters " + "WHERE LOWER(name) = LOWER(" + "\"" + name + "\"" +")" +  
					" LIMIT 1"; 
			found = processQueryGetCharacters(query)[0]; 
			break;
		default:	
		}
		return found; 
	}
	
	/* Request from database a character from given string */ 
	private static Character[] processQueryGetCharacters(String query) throws SQLException 
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
		int col = res.getMetaData().getColumnCount(); 
		
		String[] columnData = new String[col];  
		
		
		
		do {
			for(int i =1; i < col; ++i) 
			{
				columnData[i] = res.getString(i); 
			}
			
			Character chtr = new Character(Integer.valueOf(columnData[1]), columnData[2]);
			chtr.setDate(date);
			// Done get data now instantiate character 
			list.add(chtr ); 
			
		}while(res.next()); 
		
		
		
		
		// Return as arraylist
		Character[] arr = list.toArray(new Character[list.size()]); 
		
		return arr; 
	}
	
	
	
	
	
}
