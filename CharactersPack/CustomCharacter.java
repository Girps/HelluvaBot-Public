package CharactersPack;


public class CustomCharacter extends MinorCharacter{

	public CustomCharacter(Long id_Arg,String name_Arg, String imgur_Url) 
	{
		super();
		this.name = name_Arg; 
		this.id = id_Arg; 
		this.defImage = imgur_Url; 
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
	protected  String getAttribute(String markUp, String startAttribute) 
	{
		return "none"; 
	}
}
