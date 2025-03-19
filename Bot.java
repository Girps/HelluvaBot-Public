

import events.UserInfoCommand;
import events.UserManager;
import events.WaifuCommand;
import events.WikiCommand;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import events.CollectCommand;
import events.CommandManger;
import events.EconomyCommand;
import events.FrameCommand;
import events.GuessCommand;
import events.HelpCommand;
import events.KdmCommand;
import events.KinsCommand;
import events.MemesCommand;
import events.FavoriteCommand;
import events.OriginalCharacterCommand;
import events.ShipsCommand;
import events.SimpsCommand;
import events.SmashPassCommand;
import events.SonasCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {

	private static ShardManager shardManager; 
	public static ConcurrentHashMap<Integer,Character> characterImages =
			new ConcurrentHashMap<Integer,Character>(); // store both character names and url images 
	
	public static ExecutorService executor = Executors.newFixedThreadPool( (int)( Runtime.getRuntime().availableProcessors() * (1 + (0.90/0.10))) );
	public static ScheduledExecutorService sexecutor = Executors.newSingleThreadScheduledExecutor();
	
	public static void main(String[] args) throws Exception
	{

		// Load the configuration file as a resource using ClassLoader
				InputStream inputStream = null;
				try
				{
					inputStream = Bot.class.getResourceAsStream("config.properties"); 
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("failed to load config file"); 
				}

				// Use the inputStream to load the properties
				Properties properties = new Properties();
				properties.load(inputStream);
				final String TOKEN = properties.getProperty("TOKEN"); 
				final String PASSWORD = properties.getProperty("DATABASE_PASSWORD"); 
				final String MYURL = properties.getProperty("MYURL") ; 
				final String NAME = properties.getProperty("NAME"); 
				final String clientId = properties.getProperty("CLIENTID"); 
				final String  clientSecret = properties.getProperty("CLIENTSECRET"); 
				final String rusername = properties.getProperty("USERNAME"); 
				final String rpassword = properties.getProperty("PASSWORD"); 
						
				
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(TOKEN);  
		
		builder.setActivity(Activity.listening("/help for help")); 
		builder.setStatus(OnlineStatus.ONLINE); 
		 builder = builder.enableIntents( GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS
				,GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS); 
		  
		 // Add event listeners
		 
		builder.addEventListeners( new UserInfoCommand(), new events.CommandManger(executor), new WikiCommand(executor)
				, new SmashPassCommand(executor,sexecutor), new SimpsCommand(executor), new KinsCommand(executor), new ShipsCommand(executor), new WaifuCommand(executor,sexecutor)
				, new KdmCommand(executor, sexecutor), new SonasCommand(executor,sexecutor), new FavoriteCommand(executor), new OriginalCharacterCommand(executor, sexecutor)
				, new GuessCommand(executor,sexecutor), new FrameCommand(executor) ,new CollectCommand(executor, sexecutor), new UserManager(executor), new HelpCommand(), 
				new MemesCommand( executor, sexecutor, clientId, clientSecret, rusername, rpassword ), new EconomyCommand(executor, sexecutor) ); 
		

		// Have shard manager
		shardManager = builder.build();
		
		// Connect to database 
		
		String url = MYURL; 
		String name = NAME; 
		String password = PASSWORD;
		
		 
		// Delete 
		
			// Now connect 
			CharacterSelection select = new CharacterSelection(url, name, password); 
			
			Runtime.getRuntime().addShutdownHook(new Thread() 
			{
				public void run() 
				{
					 
					if(select.getPool() != null) 
					{
						try 
						{
							select.getPool().close(); 
						}
						catch (Exception e)
						{
							
						}
					} 
				}
			
			}); 
		
		
	}

}
