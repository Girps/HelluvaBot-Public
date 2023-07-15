
import events.TestCommand;
import events.UserInfoCommand;
import events.WaifuCommand;
import events.WikiCommand;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import events.CollectCommand;
import events.CommandManger;
import events.EventWaiterCommand;
import events.FrameCommand;
import events.GuessCommand;
import events.KdmCommand;
import events.KinsCommand;
import events.ListCommand;
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
		
		
		Connection conn = null; 
		
		// Now connect 
		try 
		{
			conn = DriverManager.getConnection(url, name, password); 
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		// Add event listners 
		jda.addEventListener(new TestCommand(prefix, conn));
		jda.addEventListener(new UserInfoCommand());
		jda.addEventListener(waiter);
		jda.addEventListener(new EventWaiterCommand(waiter));
		jda.addEventListener(new WikiCommand(  conn)); 
		jda.addEventListener(new SmashPassCommand(conn,waiter));
		jda.addEventListener(new SimpsCommand(conn));
		jda.addEventListener(new KinsCommand(conn));
		jda.addEventListener(new ShipsCommand(conn));
		jda.addEventListener(new WaifuCommand(conn,waiter));
		jda.addEventListener(new KdmCommand(conn,waiter));
		jda.addEventListener(new CommandManger(conn)); 
		jda.addEventListener(new SonasCommand(conn));
		jda.addEventListener(new ListCommand(conn)); 
		jda.addEventListener(new OriginalCharacterCommand(conn)); 
		jda.addEventListener(new GuessCommand(conn, waiter));
		jda.addEventListener(new FrameCommand()); 
		jda.addEventListener(new CollectCommand(conn,waiter)); 
	}

}
