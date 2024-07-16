package events;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA.ShardInfo;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class UserManager extends ListenerAdapter  
{
	
	final int MAX = 2200; 
	public UserManager() 
	{
		
	}
	
	
	/* Method will be called when finished loading the bot use it to check if any users have left the server and 
	 * manage the tables in the database accordingly  */ 
	@Override 
	public void onReady(ReadyEvent event) 
	{
		System.out.println("onReady event fired for UserManger class "+ event.getJDA().getShardInfo()); 
		ShardInfo shardInfo = event.getJDA().getShardInfo(); 
		int guildSize = event.getJDA().getShardManager().getGuilds().size(); 
		ShardManager shMan = event.getJDA().getShardManager(); 
		shMan.setActivity(Activity.listening("/help | " + guildSize + " servers"));
		
		try 
		{ 
			
			CharacterSelection select = new CharacterSelection(); 
		
			// iterate every guild from every shard 
			for(int i = 0; i < guildSize; ++i) 
			{
				
				// Get users from current guild from the database
				ArrayList<Long> userIds = select.getServerUsers(shMan.getGuilds().get(i).getIdLong()); 
				System.out.println(userIds); 
				if(!userIds.isEmpty()) 
				{
					// load current members from given guild index
					shMan.getGuilds().get(i).loadMembers().onSuccess( users -> 
					{
						int count =0; 
						ArrayList<Long> memberIds = new ArrayList<Long>(); 
						// get all users from current server using their ids.
						for(Member user : users)
						{
							memberIds.add(user.getIdLong()); 
						}
						
						for (int x = 0; x < userIds.size(); ++x) 
						{
							// Check if current guild has this member if not delete their data
							if(memberIds.contains(userIds.get(x)) == false)
							{
								select.removeSona(userIds.get(x), users.get(0).getGuild().getIdLong()); 
					    		select.removeAllOcs(userIds.get(x), users.get(0).getGuild().getIdLong());
								select.removeWaifu(userIds.get(x), users.get(0).getGuild().getIdLong()); 
								select.removeFavList(userIds.get(x), users.get(0).getGuild().getIdLong());	
								select.removeCollect(userIds.get(x), users.get(0).getGuild().getIdLong());  
								System.out.println(userIds.get(x) + " left server in SHARD: " + users.get(0).getJDA().getShardInfo());
								System.out.println(++count); 
							}
							
						}
					} ); 
				}
			}
			
			/* 
			// Check if any users left the server 
			for(int i = 0; i < event.getJDA().getGuilds().size(); ++i) 
			{ 
				ArrayList<Long> userIds = select.getServerUsers(event.getJDA().getGuilds().get(i).getIdLong());
				if(!userIds.isEmpty()) {
				event.getJDA().getShardManager();
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
			
			*/ 
			
			
			/*Get all guilds if bot was kicked from the server when offline it must detect this and delete any 
			 * tuples corresponding to those tuples 
			 * */ 
			System.out.println("Checking in servers in Database and JDA"); 
			// each server from the database 
			Map<Long, Long> dbServers = select.getAllServersDB(); 
			for(Map.Entry<Long, Long> server : dbServers.entrySet()) 
			{
				long idGuild = server.getValue();   
				// Not in any shards delete it from the database 
				if ( shMan.getGuildById(idGuild) == null) 
				{
					System.out.println("Delete Server: " + idGuild ); 
					select.removeAllSonas(idGuild);
					select.removeAllOcsInGuild(idGuild);
					select.removeAllWaifus(idGuild); 
					select.removeFavListGuild(idGuild);
					select.removeAllPlayersCollectInGuild(idGuild); 
					select.removeFromWhiteList(idGuild); 

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
		int guildSize  = event.getJDA().getShardManager().getGuilds().size(); 
		// leave server if number exceeds a const
		
		if(guildSize > MAX) 
		{
			// Leave the server 
			event.getGuild().leave().queue(); 
			System.out.println("Left server"); 
			return; 
		}
		System.out.println("Bot join event success"); 

		// update amount of servers   
		event.getJDA().getShardManager().setActivity(Activity.listening("/help | " + guildSize + " servers"));
		// Leave server if number exceeds  a const 
		 
		// Notify roles to add 
		if ( event.getGuild().getRolesByName("Helluva Admin", false).isEmpty())
		{
			// empty send a message to the general chat
			EmbedBuilder builder = new EmbedBuilder(); 
			builder.setTitle("Recommended roles"); 
			builder.setImage("https://i.imgur.com/x5zkc8p.jpg");
			builder.setDescription("Create and assign this role to your Admins of the server.No special permissions required! Role needed for using following commands");
			builder.addField("/require-permission","Only allow users with 'Helluva Admin' and 'Helluva Permission' role to insert a OCs/Sonas into the bot",true);
			builder.addField("/reset-collect","Only users with 'Helluva Admin' role can reset the collect game!",true); 
			builder.addField("/remove-sona [user]", "Only users with 'Helluva Admin' role can remove other users' sonas!", true); 
			builder.addField("/remove-user-oc <user> <customcharacter>"," Only users with 'Helluva Admin' role can remove another users' OC!",true); 
			builder.addField("/remove-all-ocs [user]"," Only users with 'Helluva Admin' role can remove another users' OCs!",true); 
			builder.setColor(Color.RED);
			event.getJDA().retrieveUserById(event.getGuild().getOwnerId()).queue( (owner) -> 
			{
				owner.openPrivateChannel().flatMap(channel -> channel.sendMessage(owner.getAsMention() +
						" Hello please make sure to add the role 'Hellua Admin' in " + event.getGuild().getName() + " to use Admin only commands! "
						+ "There are important for management of the collect game! Use /help command for more infromation on each command!")).queue(null,new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
								(ex) -> {System.out.println("Unable to send to user");}));
				owner.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(builder.build())).queue(null,new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
						(ex) -> {System.out.println("Unable to send to user");}));
			}); 
			
			
		}
		
	}
	
	/* Delete all waifus, ocs , sonas, playersInCollect and wishList  from the server the bot left from */
	@Override
	public void onGuildLeave(GuildLeaveEvent event)
	{
		int size  = event.getJDA().getShardManager().getGuilds().size(); 
		
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
			select.removeFromWhiteList(idGuild); 
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
	
	/* Called when the bot is terminaed */ 
	@Override 
	public void onShutdown(ShutdownEvent event)
	{
		System.out.println("Bot shutdown"); 
		CharacterSelection select = new CharacterSelection(); 
		
		if ( select.getPool() != null )
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
	
	
	// Allow admins to insert their server into the whitelist
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		CharacterSelection select = new CharacterSelection(); 
		switch (event.getName()) 
		{ 
			case "require-permission":
				if(event.getOption("permission").getAsBoolean()) 
				{
					
					if(!Helper.checkAdminRole(event.getMember().getRoles()))
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " +  MarkdownUtil.bold("Helluva Admins") +" can use that command!").queue();
						return;
						
					}
					else if(select.serverWhiteList(event.getGuild().getIdLong())) 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage("This server currently requires users to have the roles " + MarkdownUtil.bold("Helluva Admin") + " or " + MarkdownUtil.bold("Helluva Permission") + " to insert OC/Sonas!").queue(); 
					}
					else 
					{
						// true insert server into white list 
						select.insertWhiteList(event.getGuild().getIdLong());
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to insert OCs/Sonas!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage("Permissions are now " + MarkdownUtil.bold("required") + " for users to insert OCs/Sonas into the bot!").queue(); 
					}
					
				}
				else 
				{
					// Check if Admin permission if so delete permission 
					if(!Helper.checkAdminRole(event.getMember().getRoles()))
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only Helluva Admins can use that command!").queue();
						return;
						
					}
					else 
					{
						select.removeFromWhiteList(event.getGuild().getIdLong()); 
						event.getHook().sendMessage("Permissions are "+  MarkdownUtil.bold("disabled!") + " Any user can insert OCs/Sonas into the bot!").queue(); 
					}
					
				}
				break; 
		}
			
		
	}
	
}
