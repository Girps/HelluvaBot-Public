package CharactersPack;

import java.util.ArrayList;

import org.json.JSONObject;

public class CharacterFactory {

	Long id; 
	String name; 
	String	type; 
	String showName;
	Integer defaultImgIndex = 0; 
	ArrayList<JSONObject> imgLinks = null; 
	SETUPTYPE setType; 
	public CharacterFactory(Long argId, String nameArg, String showNameArg,String typeArg, Integer defaultImgIndex, ArrayList<JSONObject> imgLinks, SETUPTYPE setArg)
	{
		id = argId; 
		name = nameArg; 
		type = typeArg;
		showName = showNameArg;
		setType = setArg; 
		this.imgLinks = imgLinks; 
		this.defaultImgIndex = defaultImgIndex; 
	}
	
	/*
	 * Generate character with given fields 
	 * */ 
	public Character getCharacter() 
	{
		
		
		if (this.type.equals("T")) 
		{
			return new Character(id, name, defaultImgIndex, imgLinks, setType); 	// Main character

		}
		else if (this.type.equals("F")) 
		{
			return new MinorCharacter(id, name, showName, defaultImgIndex, imgLinks, setType);  // Minor character

		}
		else 
		{
			return new CustomCharacter(id, name, type, imgLinks, setType); // Custom characters includes ocs
		}
		
	}
	
}
