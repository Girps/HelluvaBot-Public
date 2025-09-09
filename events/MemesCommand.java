package events;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MemesCommand extends ListenerAdapter{

	private static ExecutorService executor = null;
	private static ScheduledExecutorService sexecutor =null; 

	private static AtomicReference<String> token = new AtomicReference<String>(); 
	public MemesCommand(ExecutorService executor,ScheduledExecutorService sexecutor, String clientId, String clientSecret, String username, String password) {
		// TODO Auto-generated constructor stub
		 this.executor = executor;
		 this.sexecutor = sexecutor; 
		 // now set up the reddit auth
		 token.set(null);
			try
			{
				setUpToken(clientId, clientSecret, username, password); 
			} 
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("failed to load config file"); 
			}
			// every 24 hours get a new token
			this.sexecutor.scheduleAtFixedRate(
	        		() ->
	        		{
	        			try {
							setUpToken(clientId, clientSecret, username,password);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
	        		},0, 
	        		86400, TimeUnit.SECONDS); 
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		// Check valid command 
		if(event.getName().equals("memes")) 
		{
			
		// submit task 
		 this.executor.submit( () -> 
		 {
			 	try
			 	{
			 		
			 		event.deferReply().queue(); 
			 		// now request a random image be called from the HelluvaBossmemes subbreddit
			 		HttpClient client = HttpClient.newHttpClient(); 
					Random ran = new Random();
					String category = "hot"; 
					String time = "week";
					// Build request
			        HttpRequest request = HttpRequest.newBuilder()
			            .uri(URI.create("https://oauth.reddit.com/r/Helluvabossmemes/"+ category + ".json?t=" + time +""))
		                .header("User-Agent", "HelluvaBot2.0/1.0")  // Custom User-Agent
		                .header("Authorization", this.token.get()) // Oath
			            .build(); 
			        // Send the request and get the response
			        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			        // Print the JSON response
			        System.out.println(response.body()); 
			        JSONObject obj = new JSONObject(response.body());
			        JSONArray posts = obj.getJSONObject("data").getJSONArray("children");

			        // get a random post 
			        int i = -1; 
				    i = ran.nextInt( posts.length() -1);
				    
				
			        
			     // now have json body will get fields 
					EmbedBuilder builder = new EmbedBuilder(); 
					 // get individual post
			        JSONObject post = null;
			        String title = null; 
			        String url = null; 
			        String image = null; 
					boolean flag=  true; 
					do 
					{ 
						
						 	post = posts.getJSONObject(i).getJSONObject("data"); 
					        title = post.getString("title"); 
					        url = post.getString("url"); 
					        image = post.optString("url", null);
					    if (image != null && ( url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".gif") || url.endsWith(".jpeg") ) ) 
					    {
					    	builder.setImage(image); 
					    	flag = false; 
					    }
					    else 
					    {
					    	i = ran.nextInt(posts.length() - 1); 		
					    }
					} while(flag); 
				    
				    builder.setImage(image); 
					builder.setTitle(title, url); 
					builder.setColor(Color.RED);
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
			 	}
			 	catch(Exception ex) 
			 	{
			 		event.getHook().sendMessage(ex.getMessage()).queue(); 
			 	}
		 }); 
		}
	}
	
	
	
	
	public  void setUpToken(String clientId, String clientSecret, String username, String password) throws IOException, InterruptedException 
	{
		HttpClient client = HttpClient.newHttpClient(); 
		String cred = clientId + ":" + clientSecret; 
		String ecred = Base64.getEncoder().encodeToString(cred.getBytes()); 
        String body = "grant_type=password&username=" + username + "&password=" + password;
		
		// Build request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.reddit.com/api/v1/access_token"))
            .header("Authorization", "Basic " + ecred) // Basic Auth
            .header("User-Agent", "HelluvaBot/1.0") // Custom User-Agent required by Reddit
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(BodyPublishers.ofString(body))
            .build();
        
        
        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Parse JSON response
        JSONObject json = new JSONObject(response.body());
        String accessToken = json.getString("access_token");
        int expiresIn = json.getInt("expires_in"); // Usually 3600 seconds (1 hour)
        token.set(accessToken); 
	}
}

