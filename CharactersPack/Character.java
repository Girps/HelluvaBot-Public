package CharactersPack;
import org.json.JSONObject;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;



public class Character 
{
	protected Long id; 
	protected String name;
	protected static String url = "https://hazbinhotel.fandom.com/wiki/";	// should be the same only one instance is needed   
	protected String  defImage;
	String rarity = null; 
	protected ArrayList<JSONObject> jsonImages;
	protected ArrayList<String> imageList; 
	protected JSONObject perks; 
	protected String rawData ;
	protected Date date; 
	protected int def = 0; 
	private SETUPTYPE set; 

	
	/* Initialize the type with id, name and url given at construction */ 
	public Character(Long arg_Id, String arg_Name, int def , ArrayList<JSONObject> jsonList, JSONObject perks , String rarity , SETUPTYPE setArg) 
	{
		Instant now = Instant.now();  
		id = arg_Id; 
		name = arg_Name; 
		defImage = null;
		rawData = null;
		date = null; 
		set = setArg; 
		this.jsonImages = jsonList; // intially links from db
		this.imageList = new ArrayList<String>(); 
		this.perks = perks; 
		this.def = def; 
		this.rarity = rarity; 
		if( this.jsonImages != null && !this.jsonImages.isEmpty()) { 
		defImage  = this.jsonImages.get(def).getString("url"); // intial default image from db indexed 
		}
	}
	
	

	/* Set date when given argument  */ 
	public void setDate(Date arg)
	{
		date = arg; 
	}
	
	/* Get the date assigned in */
	public String getDate() 
	{
	
		Date now = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime(); 
		
		 long millDelta =  ( date.getTime() + 86400000L ) - now.getTime() ; 
		 
		 
		 
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
	public Long getId() 
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
	
	public String getDefJSONImage() 
	{
		return (!this.jsonImages.isEmpty()) ? this.jsonImages.get(def).getString("url") : ""; 
	}
	
	public String ArtName() 
	{
		return (!this.jsonImages.isEmpty()) ? this.jsonImages.get(def).getString("art_name") : ""; 
	}

	public String getAuthorName() 
	{
		return (!this.jsonImages.isEmpty() ) ? this.jsonImages.get(def).getString("author_name") : ""; 
	}
	
	public String getAuthorLink() 
	{
		return (!this.jsonImages.isEmpty()) ? this.jsonImages.get(def).getString("author_link") : ""; 
	}
	
	public String getCreditStr() 
	{
		String credit = ""; 
		String authorName = this.getAuthorName();  
		if(!authorName.equals("")) 
		{
			credit += "\nArt by "; 
			credit += authorName ; 
		}
		
		String authorLink =  this.getAuthorLink(); 
		
		if(!authorLink.equals("")) 
		{
			credit += " | link : " + authorLink; 
		}
		return credit; 
	}
	
	
	public String getRarity() 
	{
		return rarity; 
	}
	
	public JSONObject getPerks() 
	{
		return this.perks; 
	}
	
	public Color getColor() 
	{
		Color c = Color.GRAY; 
		switch(this.rarity) { 
			case "Common": 
			{
				c = Color.GRAY; 
			}
			break; 
			case "Uncommon": 
			{
				c = Color.GREEN; 
			}
			break; 
			case "Rare":
			{
				c = Color.BLUE; 
			} 
			break ;
			case "Ultra Rare":
			{
				c = Color.MAGENTA;
			}
			break; 
			
		} 
		return c; 
	}
	
	
	/***************************************************************/
	
	public  String getAttribute(String markUp, String startAttribute) 
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
	
	/*Get the default json object */
	public JSONObject getDefaultJSONObject()
	{
		return this.jsonImages.get(def); 
	}
	
	/* Get default author name*/
	public String getDefaultAuthorName()
	{
		return this.jsonImages.get(0).getString("author_name"); 
	}
	
	/* Get default author link*/
	public String getDefaultAuthorLink()
	{
		return this.jsonImages.get(0).getString("author_links"); 
	}
	
	/* Get the arrayList of urls stored in this instance */ 
	public ArrayList<JSONObject> getImageList() 
	{
		return jsonImages; 
	}
	
	/* Get the arrayList of urls stored in this instance list is of string from scrapped wiki */ 
	public ArrayList<String> getImageNonJSONList() 
	{
		return this.imageList; 
	}
	
	
	/*
	 * Private function sets up the links for images url
	 * */
	public void setUpImages() 
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
				   this.imageList.add(images.get(i).attr("src")); 
			   }
		   }
		   
		   // Default image of the character set 
		   this.defImage = this.imageList.get(0);
		   
		   // Check size 
		   try 
		   {
			URL imageUrl = new URL(this.imageList.get(0));
			BufferedImage c = ImageIO.read(imageUrl);
			Image image = c; 
			int width = image.getWidth(null);
			int height = image.getHeight(null); 
			
			if( (width < 50 || height < 50 )  && this.jsonImages.size() != 1 ) 
			{
				this.jsonImages.remove(0); 
				this.defImage = this.imageList.get(0); 
			}
			
		   }
		   catch (MalformedURLException e1)
		   {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		   } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
		   
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
			   	// now add it to the list
			   	
			   break;
		   } 
		   default: 
		   {
			   break; 
		   }
		   
		   }
		
	}
	
	// Return name of the character
	@Override
	public String toString() 
	{
		return this.getName();  
	}
}
