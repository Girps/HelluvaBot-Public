
import events.TestCommand;
import events.UserInfoCommand;
import events.UserManager;
import events.WaifuCommand;
import events.WikiCommand;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import events.CollectCommand;
import events.CommandManger;
import events.EventWaiterCommand;
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
import net.dv8tion.jda.api.utils.cache.CacheFlag;


public class Bot {

	public static String prefix = "$"; 
	private final static EventWaiter waiter = new EventWaiter(); 
	public static void main(String[] args) throws Exception
	{
		 
		 
		builder.setActivity(Activity.listening("~man for help")); 


		
	

		



		// Check if we have driver 
		try 
		{
			
			Class.forName( "com.mysql.cj.jdbc.Driver" );
		}
		catch(ClassNotFoundException e) 
		{
			e.printStackTrace();
			System.out.println("Failed to connect to mySQL database"); 
		}	
		
		
			// Now connect 
			CharacterSelection select = new CharacterSelection(url, name, password); 
			Runtime.getRuntime().addShutdownHook(new Thread() 
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
		jda.addEventListener(new TestCommand(prefix));
		jda.addEventListener(new UserInfoCommand());
		jda.addEventListener(waiter);
		jda.addEventListener(new EventWaiterCommand(waiter));
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
