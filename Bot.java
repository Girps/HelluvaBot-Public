
import events.TestCommand;
import events.UserInfoCommand;
import events.WaifuCommand;
import events.WikiCommand;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import events.CommandManger;
import events.EventWaiterCommand;
import events.KdmCommand;
import events.KinsCommand;
import events.ShipsCommand;
import events.SimpsCommand;
import events.SmashPassCommand;
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
		builder.setStatus(OnlineStatus.ONLINE); 
		 builder = builder.enableIntents( GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS
				,GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS); 
		builder.enableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY); 
		ShardManager jda = builder.build(); 
		
		// Connect to database 
 
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
		jda.addEventListener(new UserInfoCommand(prefix));
		jda.addEventListener(waiter);
		jda.addEventListener(new EventWaiterCommand(waiter));
		jda.addEventListener(new WikiCommand(prefix,  conn)); 
		jda.addEventListener(new SmashPassCommand(prefix,conn,waiter));
		jda.addEventListener(new SimpsCommand(prefix,conn));
		jda.addEventListener(new KinsCommand(prefix,conn));
		jda.addEventListener(new ShipsCommand(prefix,conn));
		jda.addEventListener(new WaifuCommand(prefix,conn,waiter));
		jda.addEventListener(new KdmCommand(prefix,conn,waiter));
		jda.addEventListener(new CommandManger(conn)); 
	}

}
