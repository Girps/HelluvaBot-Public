package CharactersPack;

public class CharacterFactory {

	Long id; 
	String name; 
	String	type; 
	String showName;
	SETUPTYPE setType; 
	public CharacterFactory(Long argId, String nameArg, String showNameArg,String typeArg, SETUPTYPE setArg)
	{
		id = argId; 
		name = nameArg; 
		type = typeArg;
		showName = showNameArg;
		setType = setArg; 
	}
	
	/*
	 * Generate character with given fields 
	 * */ 
	public Character getCharacter() 
	{
		switch(type) 
		{
			case ("T") :
				return new Character(id, name, setType); 	// Main character
			case ("F"):
				return new MinorCharacter(id,name,showName);  // Minor character
			default : 
				return new CustomCharacter(id, name, type); // Custom characters
		}
	}
	
}
