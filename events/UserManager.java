package events;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserManager extends ListenerAdapter  
{
	
	
	public UserManager() 
	{
	}
	
	
	/* Method will be called when finished loading the bot use it to check if any users have left the server and 
	 * manage the tables in the database accordingly  */ 
	@Override 
	public void onReady(ReadyEvent event) 
	{
		System.out.println("onReady event fired for UserManger class "); 
		int size  = event.getJDA().getGuilds().size(); 
		event.getJDA().getPresence().setActivity(Activity.listening("/help | " + size + " servers")); 
		
		try 
		{ 
			
			CharacterSelection select = new CharacterSelection(); 
		// pull all users from the database in particular server 
			for(int i = 0; i < event.getJDA().getGuilds().size(); ++i) 
			{ 
				ArrayList<Long> userIds = select.getServerUsers(event.getJDA().getGuilds().get(i).getIdLong());
				
				System.out.println(userIds); 
				if(!userIds.isEmpty()) {
				event.getJDA().getGuilds().get(i).loadMembers().onSuccess( users -> 
					{
						ArrayList<Long> memberIds = new ArrayList<Long>(); 
						// get all longs 
						for(Member user : users)
						{
							memberIds.add(user.getIdLong()); 
						}
						
						
						for(int x = 0; x < userIds.size(); ++x) 
						{
								// if not in server remove it from database 
								if(!memberIds.contains(userIds.get(x)))
							 			{
								    		select.removeSona(userIds.get(x), users.get(0).getGuild().getIdLong()); 
								    		select.removeAllOcs(userIds.get(x), users.get(0).getGuild().getIdLong());
											select.removeWaifu(userIds.get(x), users.get(0).getGuild().getIdLong()); 
											select.removeFavList(userIds.get(x), users.get(0).getGuild().getIdLong());	
											select.removeCollect(userIds.get(x), users.get(0).getGuild().getIdLong());  
											System.out.println(userIds.get(x) + " no longer in the server has been removed at start up"); 
							 			} 
							}
					
						}
						); 
				}
			}
		} 
		catch(Exception e) 
		{
			e.printStackTrace(); 
			System.out.println("Something went wrong"); 
		}
		
		// if not here remove them from database 
	}
	
	// Update number of server bot is in 
	public void onGuildJoin(GuildJoinEvent event) 
	{
		int size  = event.getJDA().getGuilds().size(); 
		
		event.getJDA().getPresence().setActivity(Activity.listening("/help | " + size + " servers")); 
		
	}
	
	/* Delete all waifus, ocs , sonas, playersInCollect and wishList  from the server the bot left from */
	@Override
	public void onGuildLeave(GuildLeaveEvent event)
	{
		int size  = event.getJDA().getGuilds().size(); 
		
		event.getJDA().getPresence().setActivity(Activity.listening("/help | " + size + " servers")); 
		
		Long idGuild = event.getGuild().getIdLong(); 
		
		// Now delete all sonas and waifus from the server 
		try 
		{	
			CharacterSelection select = new CharacterSelection(); 
			select.removeAllSonas(idGuild);
			select.removeAllOcsInGuild(idGuild);
			select.removeAllWaifus(idGuild); 
			select.removeFavListGuild(idGuild);
			select.removeAllPlayersCollectInGuild(idGuild); 
			System.out.println("Bot leave event success"); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Bot leave event failed"); 
			e.printStackTrace();
		} 
		
	}
	
	/* User leaves guild remove there sonas, ocs , waifus and favorite lists, playesInCollect, playersCollection 
	 * and wishlist from that server */ 
	@Override  
	public void onGuildMemberRemove(GuildMemberRemoveEvent event)
	{

		Long serverId = event.getGuild().getIdLong(); 
		Long userId = event.getUser().getIdLong(); 
		
		try 
		{
			CharacterSelection select = new CharacterSelection();
			select.removeSona(userId, serverId); 
			select.removeAllOcs(userId, serverId);
			select.removeWaifu(userId, serverId); 
			select.removeFavList(userId, serverId);	
			select.removeCollect(userId, serverId);  
			System.out.println("Member leave event success"); 
		}
		catch(Exception e) 
		{
			System.out.println("Member leave event failed"); 
			e.printStackTrace(); 
		}
	}
	
}
