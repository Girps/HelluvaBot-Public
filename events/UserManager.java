package events;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import events.Helper;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.ShardInfo;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
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
	
	final int MAX = 10000;
	static volatile int shards  = 0; 
	private ExecutorService executor; 
	public UserManager(ExecutorService executor) 
	{
		this.executor = executor; 
	}
	
	
	synchronized int getIncrement() 
	{
		return ++shards; 
	}
	
	
	/* Method will be called when finished loading the bot use it to check if any users have left the server and 
	 * manage the tables in the database accordingly  */ 
	@Override 
	public void onReady(ReadyEvent event) 
	{
		
		
		try { 
		int total = event.getJDA().getShardManager().getShardsTotal(); 
		int shardReady = getIncrement(); 
		if (total == shardReady) 
		{ 
		
			/* Add all servers in the shard into the db */ 
			this.executor.submit( () -> 
			{
				ShardManager shardManager = event.getJDA().getShardManager(); 
				CharacterSelection select = new CharacterSelection(); 
				List<Guild> guilds = shardManager.getGuilds();
				Integer guildSize = guilds.size(); 
				ArrayList<Long> guildIds = new ArrayList<Long>(); 
				for (int i =0; i < guildSize; ++i ) 
				{
					guildIds.add(guilds.get(i).getIdLong()); 
				}
				// we have the guilds now add and delete non-existing servers  
				select.insertExistingGuilds(guildIds);
				
				for (int i= 0; i < guildSize; ++i) 
				{
					ArrayList<Long> getUsers = select.getServerUsers(guilds.get(i).getIdLong()); 
					for (int j =0 ; j < getUsers.size(); ++j)
					{  
						final int x  = j ; 
						Long id = guilds.get(i).getIdLong(); 
						guilds.get(i).retrieveMemberById(getUsers.get(j)).queue(  (member) -> 
						{
							// exists do nothing they are in the server 
						} , error -> 
						{
 
							// not exist delete them 
							ArrayList<Long> user = new ArrayList<Long>(); 
							user.add(getUsers.get(x)); 
							select.deleteUser(id, user); 
						}) ;
					}
				}
				// now record amount of servers 
				event.getJDA().getShardManager().setActivity(Activity.listening("/help | " + guildSize + " servers"));

			}); 
						
		
		}
		} 
		catch(Exception e)
		{
			e.printStackTrace(); 
		}
		
	
	}
	
	// Update number of servers bot is in 
	public void onGuildJoin(GuildJoinEvent event) 
	{
		
		this.executor.submit( () -> 
		{
			int guildSize  = event.getJDA().getShardManager().getGuilds().size(); 
			// leave server if number exceeds a const
			if(guildSize > MAX) 
			{
				// Leave the server 
				event.getGuild().leave().queue(); 
			}
			else 
			{
				
				// add server into db 
				CharacterSelection select = new CharacterSelection(); 
				// now add members into the database 
				Long serverId = event.getGuild().getIdLong(); 
				select.addServers(serverId); // add server to db
					
				
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
								" Hello please make sure to add the role 'Helluva Admin' in " + event.getGuild().getName() + " to use Admin only commands! "
								+ "These roles are important for management of the collect game! Use /help command for more information on each command!")).
						queue(null,new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
										(ex) -> {}));
						owner.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(builder.build())).queue(null,new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
								(ex) -> {}));
					}); 
				}
			} 
		}); 
	}
	
	/* Delete all waifus, ocs , sonas, playersInCollect and wishList  from the server the bot left from */
	@Override
	public void onGuildLeave(GuildLeaveEvent event)
	{
		
		this.executor.submit(() -> 
		{
			int size  = event.getJDA().getShardManager().getGuilds().size(); 
			event.getJDA().getPresence().setActivity(Activity.listening("/help | " + size + " servers")); 
			Long idGuild = event.getGuild().getIdLong();
			// Now delete all sonas and waifus from the server 
			
			CharacterSelection select = new CharacterSelection();
			ArrayList<Long> server = new ArrayList<Long>(); 
			server.add(idGuild); 
			select.deleteServer(server); 
		}); 	
	}
	
	@Override 
	public void onGuildMemberJoin(GuildMemberJoinEvent event) 
	{
		this.executor.submit(() -> 
		{
			CharacterSelection select = new CharacterSelection();
			Long serverId = event.getGuild().getIdLong(); 
			ArrayList<Member> memebers = new ArrayList<Member>(); 
			memebers.add(event.getMember()); 
			select.addUsersToUnqueUsers(serverId, memebers ); // add users to the db

		}); 
	}
	
	/* User leaves guild remove there sonas, ocs , waifus and favorite lists, playesInCollect, playersCollection 
	 * and wishlist from that server */ 
	@Override  
	public void onGuildMemberRemove(GuildMemberRemoveEvent event)
	{
		
		
		this.executor.submit( () -> 
		{
			Long serverId = event.getGuild().getIdLong(); 
			
				CharacterSelection select = new CharacterSelection();
				// parallel the queries 
				ArrayList<Long> user = new ArrayList<Long>();
				Long id = event.getUser().getIdLong(); 
				user.add(id); 
				select.deleteUser(serverId, user); 

		}); 
	}
	
	/* Called when the bot is terminaed */ 
	@Override 
	public void onShutdown(ShutdownEvent event)
	{
		CharacterSelection select = new CharacterSelection(); 
		
		if ( select.getPool() != null )
		{
			try
			{
				select.getPool().close();
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
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
				this.executor.submit( () -> 
				{
					event.deferReply().queue(); 
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
						}
						else 
						{
							select.removeFromWhiteList(event.getGuild().getIdLong()); 
							event.getHook().sendMessage("Permissions are "+  MarkdownUtil.bold("disabled!") + " Any user can insert OCs/Sonas into the bot!").queue(); 
						}
					}	
				});
				
				break; 
		}
			
		
	}
	
}
