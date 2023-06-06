package CharactersPack;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.select.Elements;
import org.jsoup.safety.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;


public class Character 
{
	private int id; 
	private String name;
	private static String url = "https://hazbinhotel.fandom.com/wiki/";	// should be the same only one instance is needed   
	private String defImage;
	private ArrayList<String> imageList;
	private String rawData ;
	private String asideData; 
	private Date date; 
	
	/* Initialize the type with id, name and url given at construction */ 
	public Character(int arg_Id, String arg_Name ) 
	{
		id = arg_Id; 
		name = arg_Name; 
		defImage = null;
		imageList = new ArrayList<String>();
		rawData = null;
		date = null; 
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
		SimpleDateFormat frm = new SimpleDateFormat("HH:mm"); 
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
		return url + URLEncoder.encode(name,StandardCharsets.UTF_8); 
	}
	
	/* Function gets the general content of the character for string to parse */ 
	public void setContent()  
	{
		Document doc = null; 
		// Use jsoup to extract contents form html format
		try 
		{
			doc = Jsoup.connect("https://hazbinhotel.fandom.com/api.php?action=parse&format=php&page="+ URLEncoder.encode(name,StandardCharsets.UTF_8) + "&prop=text").ignoreContentType(true).get();    
		}
		catch(IOException e ) 
		{
			e.printStackTrace(); 
		}
		OutputSettings out = new Document.OutputSettings(); 
		out.prettyPrint(false); 
		
		
		// Now get anything dealing with quotes and basic stuff
		
		// Get a list of elements dealing with asides   
		Elements e = doc.select("aside"); 
		asideData = Jsoup.clean(e.toString(), "",Safelist.none(),out); 
		
		
		String res = Jsoup.clean(doc.toString(), "",Safelist.none() , out); 
		res = res.substring(res.indexOf("Gallery")+7); 
		System.out.println(res); 
		try 
		{
			rawData = res.substring(0,res.indexOf("Gallery[]")); 
		} 
		catch(Exception e1) 
		{
			try 
			{ 
				rawData = res.substring(0, res.indexOf("Gallery[")); 
			}
			catch(Exception e2) 
			{
				try 
				{
					rawData = res.substring(0, res.indexOf("References[]")); 
				}
				catch(Exception e3) 
				{
					rawData = res.substring(0,res.indexOf("References[")); 
				}
			}
		}
			System.out.println(rawData); 
		
		 
	}
	
	/* Function returns quote of the character */ 
	public String getQuote() 
	{
		
		String result = rawData; 
		
		String[] arr = result.split("\n"); 
		
		int count = 0; 
		// Check if it exists if not return null 
		for(int i =0; i < arr.length; ++i) 
		{
			if(arr[i].contains("―")) 
			{
				count = 0; 
				break; 
			}
			count++; 
		}
		
		if(count == arr.length) {return "none"; }
		
		result = ""; 
		for(int i =0; i < arr.length; ++i) 
		{
			
			if(arr[i].contains("―")) 
			{
				result += arr[i]; 
				break;
			} 
			result += arr[i]; 
		}
		
		result = result.trim(); 
		return result ; 
	}
	
	
	/* Function returns basic information of the character */
	public String getBasic() 
	{
		String result = rawData; 
		result = result.substring(rawData.indexOf("First appearance")+16,result.length()); 
		try 
		{
			result = result.substring(0,result.indexOf("Contents"));
		}
		catch(Exception e )
		{
			result = result.substring(0,result.indexOf("]")); 
		}
		
		String[] arr = result.split("\n"); 
		
		List<String> list = new ArrayList<String>(); 
		list = Arrays.asList(arr); 
		
		result = ""; 
		for(int i =0; i < list.size(); ++i) 
		{
			if(list.get(i).contains(".")) 
			{
				result += list.get(i); 
			}
		}
		
		return result; 
	}
	
	/* Getter function getting information about the character */ 
	public String getNickNames() 
	{	  
		String[] delimit = {"Likes","DisLikes","Characteristics"}; 
		return helperInfo("Nicknames", delimit);
	}
	
	public String getLikes() 
	{
		String[] delimit = {"Dislikes","Characteristics"}; 
		return helperInfo("Likes", delimit); 
	}
	
	public String getDisLikes() 
	{
		String[] delimit = {"Characteristics"}; 
		return helperInfo("Dislikes", delimit); 	
	}
	
	public String getSpecies() 
	{
		String[] delimit = {"Gender", "Age", "Abilities", "Status", "Abilities","Professional Status"}; 
		return helperInfo("Species",delimit); 
	}
	
	public String getGender() 
	{
		String[] delimit = {"Abilities", "Status", "Abilities","Professional Status"}; 
		return helperInfo("Gender",delimit); 
	}
	
	public String getAge() 
	{
		String[] delimit = { "Abilities", "Status", "Abilities","Professional Status"}; 
		return helperInfo("Age",delimit);  
	}
	
	/*
	 * */
	public String getAbilities() 
	{
		String[] delimit = { "Status", "Abilities","Professional Status"}; 
		return helperInfo("Abilities", delimit); 
	}
	
	public String getStatus() 
	{
		String[] delimit = { "Professional Status"}; 
		return helperInfo("Status",delimit); 
	}
	
	public String getOccup() 
	{
		String[] delimit = { "Relationships"}; 
		return helperInfo("Occupation", delimit); 
	}
	
	public String getFamily() 
	{
		String[] delimit = { "Friends", "Romantic interests", "Enemies", "Others", "Other" }; 
		return helperInfo("Family",delimit); 
	}
	
	public String getFriends() 
	{
		String[] delimit = {  "Romantic interests", "Enemies", "Others", "Other" }; 
		return helperInfo("Friends", delimit); 
	}
	
	public String getRomance() 
	{
		String[] delimit = {  "Enemies", "Others", "Other" }; 
		return helperInfo("Romantic interests",delimit); 
	}
	
	public String getEnemies() 
	{
		String[] delimit = { "Others", "Other" }; 
		return helperInfo("Enemies",delimit); 
	}
	
	public String getOthers() 
	{
		String[] delimit = { "Other" }; 
		return helperInfo("Others",delimit); 
	}
	
	public String getVoiceActors() 
	{
		String[] delimit = { "First appearance" }; 
		return helperInfo("Voice actor",delimit); 
	}
	
	/***************************************************************/
	
	/*
	 *  Helper function help get sections of wanted strings of the data 
	 * */ 
	private String helperInfo(String arg1, String[] delimit) 
	{
		
		String result = ""; 
		// Find section 
		try
		{ 
			result = asideData.substring(asideData.indexOf(arg1),asideData.length());
		}
		catch(Exception e)
		{
			return "none"; 
		}
		
		result = result.replace(arg1, ""); 
		
		String[] arr = result.split("\n");
		List<String> list = new ArrayList<String>(); 
		list = Arrays.asList(arr);
		result = ""; 
		
		boolean end = false; 
		
		for(int i = 0 ; i < list.size(); ++i) 
		{
			// Skip any empty space 
			if (!list.get(i).isBlank()) 
			{
				// Now check if part of the delimiter list 
				for(int d = 0; d < delimit.length; ++d) 
				{
					if(list.get(i).trim().equalsIgnoreCase(delimit[d])) 
					{
						end = true; 
					}
				}
				
				// break out of the loop its done 
				if(end) {break; }

				result += "-" + list.get(i).trim() + "\n"; 
			}
		}
		
		return result; 
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
	private void setUpImages() 
	{
		
		Document doc = null; 
		
		try // Check if connection is Successfull 
		{
			// Convert the name to encoding standards to handle special characters
			doc = Jsoup.connect(url + URLEncoder.encode(name,StandardCharsets.UTF_8)).get();
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
			   
			   if(images.get(i).attr("src").contains("static.wikia"))
			   {
				   imageList.add(images.get(i).attr("src")); 
			   }
		   }
		   
		   this.defImage = this.imageList.get(0); 
	}
	
}
