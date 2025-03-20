package CharactersPack;

import java.util.ArrayList;

import org.json.JSONObject;

public class CustomCharacter extends MinorCharacter{

	public CustomCharacter(Long id_Arg,String name_Arg, String imgur_Url,ArrayList<JSONObject> jsonList, JSONObject perks,String rarity  ,  SETUPTYPE set) 
	{
		super(id_Arg, imgur_Url, imgur_Url, 0, jsonList,perks ,rarity , set);
		this.name = name_Arg; 
		this.id = id_Arg; 
	}
	
	
	


	/* Get the url of the character */
	@Override
	public String getUrl()
	{
		return	this.defImage;  
	}
	
	
	/* Override getting the basic information of the character */
	@Override
	public String getBasic() 
	{
		 return "none"; 
	}
	
	@Override 
	public String getQuote() 
	{
		return "none"; 
	}
	
	
	@Override
	public  String getAttribute(String markUp, String startAttribute) 
	{
		return "none"; 
	}
}
