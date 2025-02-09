package events;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
	
	final int MAX = 5000;
	static volatile int shards  = 0; 
	public UserManager() 
	{
		
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
		
		
		int total = event.getJDA().getShardManager().getShardsTotal(); 
		int shardReady = getIncrement(); 
		if (total == shardReady) 
		{ 
			/* Delete all user data that has left Guild when the bot is offline */ 
			CompletableFuture.runAsync(() -> 
			{	
				ShardInfo shardInfo = event.getJDA().getShardInfo(); 
				int guildSize = event.getJDA().getShardManager().getGuilds().size(); 
				ShardManager shardManager = event.getJDA().getShardManager(); 
				shardManager.setActivity(Activity.listening("/help | " + guildSize + " servers"));
				CharacterSelection select = new CharacterSelection(); 
				
				// iterate every guild from every shard 
				for(int i = 0; i < guildSize; ++i) 
				{
					// Get users from current guild from the database
					ArrayList<Long> userIds = select.getServerUsers(shardManager.getGuilds().get(i).getIdLong()); 
					if(!userIds.isEmpty()) 
					{
						// load current members from give guild index
						shardManager.getGuilds().get(i).loadMembers().onSuccess( users -> 
						{
							ArrayList<Long> nonMemberIds = new ArrayList<Long>(); 
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
									nonMemberIds.add(userIds.get(x)); 
								}
							}	
								// if array list not empty excute queries 
								if (!nonMemberIds.isEmpty()) 
								{
									// Excutute queries in parallel 
									CompletableFuture<Void> rmSonaFuture = CompletableFuture.runAsync( () 
											-> select.removeSonaList(nonMemberIds, users.get(0).getGuild().getIdLong())); 
									CompletableFuture<Void> rmOcsFuture = CompletableFuture.runAsync(() 
											-> select.removeAllOcsList(nonMemberIds, users.get(0).getGuild().getIdLong())) ; 
						    		CompletableFuture<Void> rmWaifuFuture = CompletableFuture.runAsync( () 
						    				-> select.removeWaifuList(nonMemberIds, users.get(0).getGuild().getIdLong()) ); 
									CompletableFuture<Void> rmFavFuture = CompletableFuture.runAsync( () 
											-> select.removeFavListArr(nonMemberIds, users.get(0).getGuild().getIdLong())); 
									CompletableFuture<Void> rmCollectFuture = CompletableFuture.runAsync(() 
											-> select.removeCollectList(nonMemberIds, users.get(0).getGuild().getIdLong()));
									
									List<CompletableFuture<Void>> futures = List.of(rmSonaFuture, rmOcsFuture
											, rmWaifuFuture, rmFavFuture, rmCollectFuture );
									
									CompletableFuture.allOf( futures.toArray( new CompletableFuture[0])).thenAccept( (v) -> 
									{
									}).exceptionally((ex) -> 
									{
										ex.printStackTrace(); 
										return null;
									}); 
								}
						} ); 
					}
				}
				
			});
	
	
			/*Get all guilds if bot was kicked from the server when offline it must detect this and delete any 
			 * tuples corresponding to those tuples 
			 * */ 
			
			CompletableFuture.runAsync( () -> 
			{
				CharacterSelection select=  new CharacterSelection(); 
				ShardManager shardManager = event.getJDA().getShardManager(); 
				// each server from the database 
				Map<Long, Long> dbServers = select.getAllServersDB(); 
				for(Map.Entry<Long, Long> server : dbServers.entrySet()) 
				{
					
					long idGuild = server.getValue();   
					
					// Now delete server gamedata that is not in any shards 
					if ( shardManager.getGuildById(idGuild) == null) 
					{
						
	
						// Excutute queries in parallel 
						CompletableFuture<Void> rmSonaFuture = CompletableFuture.runAsync( () 
								-> 	select.removeAllSonas(idGuild)); 
						CompletableFuture<Void> rmOcsFuture = CompletableFuture.runAsync(() 
								-> 	select.removeAllOcsInGuild(idGuild)) ; 
			    		CompletableFuture<Void> rmWaifuFuture = CompletableFuture.runAsync( () 
			    				-> select.removeAllWaifus(idGuild)  ); 
						CompletableFuture<Void> rmFavFuture = CompletableFuture.runAsync( () 
								-> select.removeFavListGuild(idGuild)); 
						CompletableFuture<Void> rmCollectFuture = CompletableFuture.runAsync(() 
								-> select.removeAllPlayersCollectInGuild(idGuild));
						CompletableFuture<Void> rmWhiteListFuture = CompletableFuture.runAsync( () 
								-> select.removeFromWhiteList(idGuild)); 
						
						List<CompletableFuture<Void>> futures = List.of(rmSonaFuture, rmOcsFuture
								, rmWaifuFuture, rmFavFuture, rmCollectFuture, rmWhiteListFuture );
						CompletableFuture.allOf( futures.toArray( new CompletableFuture[0])).thenAccept( (v) -> 
						{
						}).exceptionally((ex) -> 
						{
							ex.printStackTrace(); 
							return null;
						});
						
					} 
				}	
			});
			
			// Now get total number of users in each guild 
			
			
			CompletableFuture.runAsync(() -> 
			{
				List<Guild> guilds = event.getJDA().getShardManager().getGuilds(); 
				int memberCount = 0; 
				int size = guilds.size();
				for(int i =0; i < size; i++) 
				{
					memberCount += guilds.get(i).getMemberCount(); 
				}
				
				// have all members count 
				CharacterSelection select = new CharacterSelection(); // update member count to data base
				select.updateMemberCount(memberCount); 
				
			}); 
		
		} 

		
		// if not here remove them from database 
		
		// Now get total number of users in each guild. 
		
	
	}
	
	// Update number of server bot is in 
	public void onGuildJoin(GuildJoinEvent event) 
	{
		
		CompletableFuture.runAsync( () -> 
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
		
		CompletableFuture.runAsync(() -> 
		{
			int size  = event.getJDA().getShardManager().getGuilds().size(); 
			event.getJDA().getPresence().setActivity(Activity.listening("/help | " + size + " servers")); 
			Long idGuild = event.getGuild().getIdLong();
			// Now delete all sonas and waifus from the server 
			
				CharacterSelection select = new CharacterSelection();
				
				List<CompletableFuture<Void>> futures = List.of(
						CompletableFuture.runAsync( () ->	select.removeAllSonas(idGuild) ),
						CompletableFuture.runAsync( () ->	select.removeAllOcsInGuild(idGuild)) ,
						CompletableFuture.runAsync( () ->	select.removeAllWaifus(idGuild) ) ,
						CompletableFuture.runAsync( () ->	select.removeFavListGuild(idGuild)  ),
						CompletableFuture.runAsync( () ->	select.removeAllPlayersCollectInGuild(idGuild)  ), 
						CompletableFuture.runAsync( () -> 	select.removeFromWhiteList(idGuild) )
						); 
				
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept( v-> 
				{
				}).exceptionally( (ex) -> 
				{
					return null ;
				}); 
		}); 	
	}
	
	/* User leaves guild remove there sonas, ocs , waifus and favorite lists, playesInCollect, playersCollection 
	 * and wishlist from that server */ 
	@Override  
	public void onGuildMemberRemove(GuildMemberRemoveEvent event)
	{
		
		
		CompletableFuture.runAsync( () -> 
		{
			Long serverId = event.getGuild().getIdLong(); 
			Long userId = event.getUser().getIdLong(); 
			
				CharacterSelection select = new CharacterSelection();
				
				// parallel the queries 
				
				List<CompletableFuture<Void>> futures = List.of(
						CompletableFuture.runAsync( () ->	select.removeSona(userId, serverId)  ),
						CompletableFuture.runAsync( () ->	select.removeAllOcs(userId, serverId) ) ,
						CompletableFuture.runAsync( () ->	select.removeFavList(userId, serverId) ) ,
						CompletableFuture.runAsync( () ->	select.removeCollect(userId, serverId)  ),
						CompletableFuture.runAsync( () ->	select.removeWaifu(userId, serverId)  )
						); 
				
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept( v-> 
				{
				}).exceptionally( (ex) -> 
				{
					ex.printStackTrace(); 
					return null; 
				}); 				
			
		}).exceptionally( (ex) -> 
		{
			ex.printStackTrace(); 
			return null; 
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
				event.deferReply().queue( v -> 
				{
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
