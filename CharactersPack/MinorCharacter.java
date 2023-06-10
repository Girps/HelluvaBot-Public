package CharactersPack;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/*	Inherit from character change  some fields 
 * 
 * */ 
public class MinorCharacter extends Character
{
	protected  static String MinorCharacterURL = "https://hazbinhotel.fandom.com/wiki/Minor_Characters/";  
	protected String showName; 
	protected static String markUp; 
	
	public MinorCharacter() 
	{
		super(); 
	}
	
	public MinorCharacter(int arg_Id, String arg_Name, String arg_ShowName) {
		super(); 
		// TODO Auto-generated constructor stub
		this.id = arg_Id; 
		this.name = arg_Name;
		this.showName = arg_ShowName;
		setUpMarkUp(); 
		setContent(); // set up unparsed data 
		setUpImages(); // set up images 
	}
	
	/* Get the url of the character */
	@Override
	public String getUrl()
	{
		return MinorCharacterURL + URLEncoder.encode(showName, StandardCharsets.UTF_8).replace("+", "_"); 
	}
	
	private void setUpMarkUp() 
	{
		 MediaWikiBot wikiBot = new MediaWikiBot("https://hazbinhotel.fandom.com/");
		  Article article = wikiBot.getArticle("Minor Characters/" + this.showName); 
		  
		  markUp = article.getText();
	}
	
	/* Override getting the basic information of the character */
	@Override
	public String getBasic() 
	{
		 String temp = rawData.stripLeading(); 
		 String lclRaw = temp.substring(0, temp.indexOf("\n") + 1);
		
		
		  String result = lclRaw;
		  
		  //Set up parser 
		  MediaWikiParserFactory pf = new MediaWikiParserFactory(Language.english);
		  MediaWikiParser parser = pf.createParser();
		  
		  result = parser.parse(result).getText(); 
		  // Now remove the first "|" CHARACTER  FOUND
		  return  result;  
	}
	
	@Override 
	public String getQuote() 
	{
		return "none"; 
	}
	
	/* Override differen tway to get the markUp information of minor characters */ 
	@Override
	public void setContent()
	{
		 

		  
		 String rawData = ""; 
		  // Check names for "," or "and"
		  if( ( name.contains("and") || name.contains(",") ) && (name.split(" ").length >= 3 
				  && name.split(" ").length < 5) && !name.contains("Ollie and Bertha"))
		  {
			  String result = "";  
			  String temp  = ""; 
			  String[] list = name.split(" "); 
			  ArrayList<String> parsed = new ArrayList<String>(); 
			  
			  for(int outter = 0; outter < list.length; ++outter) 
			  {
				  if(list[outter].contains(",")) 
				  {
					  temp = list[outter].replace(",", ""); 
					  temp = "'''" + temp + "'''" + "," + " "; 
					  result += temp; 
					  temp = ""; 
					  parsed.clear(); 
				  }
				  else if(list[outter].equals("and")) 
				  {
					  // Now parse it and add the string 
					  for(int inner = 0 ; inner < parsed.size(); ++inner) 
					  {
						  temp += parsed.get(inner) + " ";  
					  }
					  temp = temp.stripTrailing(); 
					  temp = "'''" + temp + "'''";
					  if(list[outter].equals("and")) 
					  {
						  result += temp + " " + list[outter] + " ";
					  }
					  parsed.clear(); 
					  temp = "";
				  }
				  else 
				  {	
					  parsed.add(list[outter]); 
				  }
			  }
			  // Now add any remaining 
			  if(!parsed.isEmpty())
			  {
				// Now parse it and add the string 
				  for(int inner = 0 ; inner < parsed.size(); ++inner) 
				  {
					  temp += parsed.get(inner) + " "; 
				  }
				  temp = temp.stripTrailing(); 
				  result += "'''" + temp + "'''";
				  parsed.clear(); 
				  
			  }
			  
			
			  rawData = markUp.substring(markUp.indexOf(result) + result.length() + 2);
		  }
		  else 
		  {
			  rawData = markUp.substring(markUp.indexOf("'''" + this.name + "'''") + this.name.length() + 6 + 2);
		  }
		  if(rawData.contains("|-\n"))
		  {
			  rawData = rawData.substring(0, rawData.indexOf("|-\n"));
		  }
		  else 
		  {
			  rawData = rawData.substring(0, rawData.indexOf("==")); 
		  }
		  this.rawData = rawData;
	}
	
	
	@Override
	protected  void setUpImages() 
	{
		 // First truncate 
		  String res = rawData; 
		  ArrayList<String> names = new ArrayList<String>(); 
		  // Now trim to file and split by \n
		  String[]  raw = res.split("File:"); 
		  
		  for(int i =0; i < raw.length; ++i) 
		  {
			  // only add images 
			  if(raw[i].contains(".png") ) 
			  {
				  names.add(raw[i].substring(0,raw[i].indexOf(".png") + 4)); 
			  }
			  else if(raw[i].contains(".jpg")) 
			  {
				  names.add(raw[i].substring(0,raw[i].indexOf(".jpg") + 4)); 

			  }
		  }
		  
		  ArrayList<String> imageList = new ArrayList<String>(); 
		  
		  Document doc = null;
		  
		  // Now fetch gifs from the gallery link 
		   try 
		   {
			   doc = Jsoup.connect("https://hazbinhotel.fandom.com/Minor_Characters/" +URLEncoder.encode(this.showName,StandardCharsets.UTF_8).replace("+", "_")).get(); 
		   }
		   catch(IOException e) 
		   {
			    this.imageList = null; 
		   }
		   
			// now get the image data urls
			Elements imagesGallery = doc.select("a[href]"); 
			
			String tempUrl = ""; 
			// Iterate the list of images now stored those urls  in the arrayList
			   for(int i = 0; i < imagesGallery.size(); ++i)
			   {
				      tempUrl = imagesGallery.get(i).attr("href"); 
				      
				      // Check only names from the list are added to the array
				     
				      for(int inner = 0; inner < names.size(); ++inner)
				      {
				      
				    	  if(tempUrl.contains( URLEncoder.encode(names.get(inner), StandardCharsets.UTF_8).replace("+","_")) ) 
				      		{
				    		  imageList.add(tempUrl); 
				      		}
				      }
			   }
		  
	      this.defImage = imageList.get(0); 
		  this.imageList = imageList; 
	}
	
	@Override
	protected  String getAttribute(String markUp, String startAttribute) 
	{
		return "none"; 
	}
	
	
}
