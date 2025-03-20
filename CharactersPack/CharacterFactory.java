package CharactersPack;

import java.util.ArrayList;

import org.json.JSONObject;

public class CharacterFactory {

	Long id; 
	String name; 
	String	type; 
	String showName;
	String rarity; 
	Integer defaultImgIndex = 0; 
	ArrayList<JSONObject> imgLinks = null; 
	JSONObject perks = null; 
	SETUPTYPE setType; 
	public CharacterFactory(Long argId, String nameArg, String showNameArg,String typeArg, Integer defaultImgIndex,
			ArrayList<JSONObject> imgLinks, JSONObject perks ,String rarity, SETUPTYPE setArg)
	{
		id = argId; 
		name = nameArg; 
		type = typeArg;
		showName = showNameArg;
		setType = setArg;
		
		this.rarity = rarity; 
		this.imgLinks = imgLinks; 
		this.perks = perks; 
		this.defaultImgIndex = defaultImgIndex; 
	}
	
	/*
	 * Generate character with given fields 
	 * */ 
	public Character getCharacter() 
	{
		
		
		if (this.type.equals("T")) 
		{
			return new Character(id, name, defaultImgIndex, imgLinks, perks, rarity ,setType); 	// Main character

		}
		else if (this.type.equals("F")) 
		{
			return new MinorCharacter(id, name, showName, defaultImgIndex, imgLinks,perks , rarity ,setType);  // Minor character

		}
		else 
		{
			return new CustomCharacter(id, name, type, imgLinks, perks, rarity ,setType); // Custom characters includes ocs
		}
		
	}
	
}
