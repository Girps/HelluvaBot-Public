package CharactersPack;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;


import java.io.IOException;
import java.util.ArrayList;

import java.util.Date;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



public class Character 
{
	protected int id; 
	protected String name;
	protected static String url = "https://hazbinhotel.fandom.com/wiki/";	// should be the same only one instance is needed   
	protected String defImage;
	protected ArrayList<String> imageList;
	protected String rawData ;
	protected Date date; 
	private SETUPTYPE set; 
	public Character() 
	{
		defImage = null; 
		imageList = new ArrayList<String>(); 
		rawData = null; 
		date = null; 
	} 
	
	/* Initialize the type with id, name and url given at construction */ 
	public Character(int arg_Id, String arg_Name , SETUPTYPE setArg) 
	{
		id = arg_Id; 
		name = arg_Name; 
		defImage = null;
		imageList = new ArrayList<String>();
		rawData = null;
		date = null; 
		set = setArg; 
		setUpImages();	// Now connect to urls and set up the images 
		setContent();  // Now set up the raw data to be parsed in other functions 
	}
	
	/* Set date when given argument  */ 
	public void setDate(Date arg)
	{
		date = arg; 
	}
	
	/* Get the date assigned in */
	public String getDate() 
	{
	
		Date now = new Date();
		
		 long millDelta = date.getTime() - now.getTime() ; 
		 
		 
		 
		 Long min = millDelta / (60000) % 60;  
		 Long hour = millDelta / (3600000);
		 
		 if(hour == 1L) 
		 {
			 return hour.toString() + " hour and " + min.toString() + " minutes";
		 }
		 else if(hour == 0L) 
		 {
			 return min.toString() + " minutes";
		 }
		 else
		 {
			 return hour.toString() + " hours and " + min.toString() + " minutes";  
		 }
	}
	
	/* Get the id of the character */ 
	public int getId() 
	{
		return id; 
	}
	
	/* Get the name of the Character */
	public String getName() 
	{
		return name; 
	}
	
	/* Get the url of the character */
	public String getUrl()
	{
		return url + URLEncoder.encode(name,StandardCharsets.UTF_8).replace("+", "_"); 
	}
	
	/* Function gets the general content of the character for string to parse */ 
	public void setContent()  
	{
		// Set up rawData to be parsed by other functions 
		MediaWikiBot bot = new MediaWikiBot("https://hazbinhotel.fandom.com/"); 
		Article art = bot.getArticle(this.name); 
		
		rawData = art.getText();  
	}
	
	/* Function returns quote of the character */ 
	public String getQuote() 
	{
		
		String result = ""; 
		  try
		  {
		  // Trim markup
		  String quote = rawData.substring(rawData.indexOf("Quote|") + 6, rawData.indexOf("|center") );
		  
		  MediaWikiParserFactory factory = new MediaWikiParserFactory(Language.english); 
		  MediaWikiParser parser = factory.createParser(); 
		  ParsedPage pp = parser.parse(quote);
		  // Parse remaing markup 
		  result = pp.getText(); 
		  
		  // Now replace '|' character with  - 
		  result = result.replace("|", " —"); 
		  }
		  catch(Exception e) 
		  {
			  result  = "none";
			  return result; 
		  }
		  return result; 
	}
	
	
	/* Function returns basic information of the character */
	public String getBasic() 
	{
		  String basic = rawData.substring(0, rawData.indexOf("==")); 
		  String result = ""; 
		  try { 
		  
	
		  
		  
		  MediaWikiParserFactory factory = new MediaWikiParserFactory(Language.english); 
		  MediaWikiParser parser = factory.createParser(); 
		  ParsedPage pp = parser.parse(basic);
		  // Parse remaing markup 
		  result = pp.getText();  
		  result = result.trim(); 
		  
		  
		  }
		  catch(Exception e ) 
		  {
			  result = "none"; 
			  return result; 
		  }
		  return result; 
		
	}
	
	/* Getter function getting information about the character */ 
	public String getNickNames() 
	{	  
		return getAttribute(rawData,"nicknames"); 
	}
	
	public String getLikes() 
	{
		return getAttribute(rawData,"likes"); 
	}
	
	public String getDisLikes() 
	{
		return getAttribute(rawData,"dislikes");  	
	}
	
	public String getSpecies() 
	{
		return getAttribute(rawData,"species"); 
	}
	
	public String getGender() 
	{
		return getAttribute(rawData,"gender"); 
	}
	
	public String getAge() 
	{
		return getAttribute(rawData,"age");  
	}
	
	/*
	 * */
	public String getAbilities() 
	{
		return getAttribute(rawData,"abilities"); 
	}
	
	public String getStatus() 
	{
		return getAttribute(rawData,"status"); 
	}
	
	public String getOccup() 
	{
		return getAttribute(rawData,"occupation");  
	}
	
	public String getFamily() 
	{
		return getAttribute(rawData,"relatives"); 
	}
	
	public String getFriends() 
	{
		return getAttribute(rawData,"friends"); 
	}
	
	public String getRomance() 
	{
		return getAttribute(rawData,"romance"); 
	}
	
	public String getEnemies() 
	{
		return getAttribute(rawData,"enemies"); 
	}
	
	public String getOthers() 
	{
		return getAttribute(rawData,"others"); 
	}
	
	public String getVoiceActors() 
	{
		return getAttribute(rawData,"voice_actor"); 
	}
	
	/***************************************************************/
	
	protected  String getAttribute(String markUp, String startAttribute) 
	  {
		  MediaWikiParserFactory factory = new MediaWikiParserFactory(Language.english); 
		  MediaWikiParser parser = factory.createParser(); 
		  String result = ""; 
		  try 
		  { 
		  String atr = markUp.substring(markUp.indexOf("|" + startAttribute ) + startAttribute.length() + 1, markUp.length()); 
		  atr = atr.substring(atr.indexOf("=")+1, atr.length()); 
		  String[] list = atr.split("\n"); 
		 
		  
		  for(int i = 0; i < list.length; ++i) 
		  {
			  // Skip white space
			  if(list[i].isBlank() || list[i].contains("{{Scroll")) 
			  {
				  continue; 
			  } // Delimited a filed if it's blank after parsing 
			  else if(parser.parse(list[i]).getText().contains("=") &&
					  !parser.parse(list[i]).getText().contains("{") ||
					  parser.parse(list[i]).getText().contains("}}"))
			  { 
				  break; 
			  }
			  else 
			  {
				   
				  // Remove any special characters 
				  if(list[i].contains("<")) 
				  {
					  // remove it 
					 list[i] = list[i].substring(0,list[i].indexOf("<")); 
				  }
				  result += "-" + parser.parse(list[i]).getText() + "\n"; 
			  }
			  
		  }
		  
		  }
		  catch(Exception e) 
		  {
			  result = "none"; 
			  return result; 
		  }
		  
		  return (result.equals("") ? "none" : result); 
	  }
	
	
	/* Get the url of the default image of this instance*/ 
	public String getDefaultImage() 
	{
		return defImage; 
	}
	
	/* Get the arrayList of urls stored in this instance */ 
	public ArrayList<String> getImageList() 
	{
		return imageList; 
	}
	
	
	/*
	 * Private function sets up the links for images url
	 * */
	protected void setUpImages() 
	{
		
		Document doc = null; 
		Document docGallery = null;
		
		
		try // Check if connection is Successfull 
		{
			// Convert the name to encoding standards to handle special characters
			doc = Jsoup.connect(url + URLEncoder.encode(name,StandardCharsets.UTF_8).replace("+", "_")).get();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		// now get the image data urls
		Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]"); 
		
		
		// Iterate the list of images now stored those urls  in the arrayList
		   for(int i = 0; i < images.size(); ++i)
		   {
			   
			   // Skip if its the site logo 
			   if(images.get(i).attr("src").contains("Site-logo") ||
	images.get(i).attr("src").contains("https://static.wikia.nocookie.net/spotlightsimagestemporary/images/0/0e/UC.JPG/revision/latest/scale-to-height-down/56?cb=20200923195325") ||
	images.get(i).attr("src").contains("https://static.wikia.nocookie.net/letsgoluna/images/c/cd/Erik-Luna-article-2-copy.png/revision/latest/scale-to-height-down/56?cb=20200327133046")	||
	images.get(i).attr("src").contains("https://static.wikia.nocookie.net/club57/images/b/b2/Rainbow-Club-57-cover.jpg/revision/latest/scale-to-height-down/56?cb=20200522172522")) 
			   {
				   continue; 
			   }
			   
			   // Skip if its that attention logo 
			   if(images.get(i).attr("src").contains("Attention.png")) 
			   {
				   continue; 
			   }
			   
			   // Only get image link with following string
			   if(images.get(i).attr("src").contains("static.wikia"))
			   {
				   imageList.add(images.get(i).attr("src")); 
			   }
		   }
		   
		   // Default image of the character set 
		   this.defImage = this.imageList.get(0);
		   
		   switch(this.set) 
		   {
		   case HEAVY: 
		   {  
			   // Now fetch gifs from the gallery link 
			   try 
			   {
				   docGallery = Jsoup.connect(url + name + "/Gallery").get(); 
			   }
			   catch(IOException e) 
			   {
				   return; 
			   }
		   
			   // now get the image data urls
			   Elements imagesGallery = docGallery.select("img[src~=(?i)\\.(gif)]"); 
			
			   String tempUrl = ""; 
			   // Iterate the list of images now stored those urls  in the arrayList
			   	for(int i = 0; i < imagesGallery.size(); ++i)
			   	{
				      tempUrl = imagesGallery.get(i).attr("src"); 
				      // Check only valid gifs can be added 
				      if(tempUrl.contains(".gif")) 
				      {
				    	   tempUrl = tempUrl.substring(0, tempUrl.indexOf(".gif") + 4); 
				    	   imageList.add(tempUrl); 
				      }
			   }
			   break;
		   } 
		   default: 
		   {
			   break; 
		   }
		   
		   }
	}
	
}
