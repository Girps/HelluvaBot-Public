

import events.UserInfoCommand;
import events.UserManager;
import events.WaifuCommand;
import events.WikiCommand;

import java.io.InputStream;
import java.util.Properties;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import events.CollectCommand;
import events.CommandManger;
import events.FrameCommand;
import events.GuessCommand;
import events.HelpCommand;
import events.KdmCommand;
import events.KinsCommand;
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


public class Bot {

	private final static EventWaiter waiter = new EventWaiter(); 
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
						
				
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(TOKEN); 
		builder.setActivity(Activity.listening("/help for help")); 
		builder.setStatus(OnlineStatus.ONLINE); 
		 builder = builder.enableIntents( GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS
				,GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS); 
		
		ShardManager jda = builder.build(); 
		
		// Connect to database 
		
		String url = MYURL; 
		String name = NAME; 
		String password = PASSWORD;
		
		
			// Now connect 
			CharacterSelection select = new CharacterSelection(url, name, password); 
			Runtime.getRuntime().addShutdownHook(new Thread() 
					A
			{
				public void run() 
				{
					System.out.println("Shutdown bot with interrupt"); 
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

			
		// Add event listners
		jda.addEventListener(waiter);
		jda.addEventListener(new UserInfoCommand());
		jda.addEventListener(new WikiCommand( )); 
		jda.addEventListener(new SmashPassCommand(waiter));
		jda.addEventListener(new SimpsCommand());
		jda.addEventListener(new KinsCommand());
		jda.addEventListener(new ShipsCommand());
		jda.addEventListener(new WaifuCommand(waiter));
		jda.addEventListener(new KdmCommand(waiter));
		jda.addEventListener(new CommandManger()); 
		jda.addEventListener(new SonasCommand());
		jda.addEventListener(new FavoriteCommand()); 
		jda.addEventListener(new OriginalCharacterCommand()); 
		jda.addEventListener(new GuessCommand(waiter));
		jda.addEventListener(new FrameCommand()); 
		jda.addEventListener(new CollectCommand(waiter)); 
		jda.addEventListener(new UserManager()); 
		jda.addEventListener(new HelpCommand()); 
	}

}
