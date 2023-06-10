package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


import CharactersPack.CharacterSelection;
import CharactersPack.Character;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;


public class SonasCommand extends ListenerAdapter
{
	
	private static Connection conn; 
	
	public SonasCommand(Connection connArg) 
	{
		conn = connArg; 
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		
		String cmd = event.getName(); 
		event.deferReply().queue(); 
		CharacterSelection select = new CharacterSelection(conn); 
		switch(cmd) 
		{
			case("sona"):	// Command to return sona from the database assigned to caller 
				
					// Check if option is empty 
					if(event.getOption("user") == null) 
					{
						// get callers sona 
						
						try 
						{
							if (!select.searchUserInSona(event.getUser().getId(), event.getGuild().getId())) 
							{
								event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "does not have a sona!").queue(); 
								return; 
							}
							// Now get the sona and display it 
							Character sona = select.getUserSona(event.getUser().getId(), event.getGuild().getId()); 
							EmbedBuilder build = new EmbedBuilder(); 
							build.setAuthor(sona.getName()); 
							build.setImage(sona.getDefaultImage()); 
							build.setColor(Color.red); 
							event.getHook().sendMessageEmbeds(build.build()).queue();
						}
						catch (SQLException e) 
						{
							// TODO Auto-generated catch block
							event.getHook().sendMessage("something went wrong!").queue(); 
							e.printStackTrace();
						} 
					}
					else // searching another user sona  
					{
						String targetId = event.getOption("user").getAsUser().getId();
					
						try 
						{
							if (!select.searchUserInSona(event.getUser().getId(), event.getGuild().getId())) 
							{
								event.getHook().sendMessage( "<@" + targetId  + "> " + "does not have a sona!").queue(); 
								return; 
							}
							// Now get the sona and display it 
							Character sona = select.getUserSona(targetId, event.getGuild().getId()); 
							EmbedBuilder build = new EmbedBuilder(); 
							build.setAuthor(sona.getName()); 
							build.setImage(sona.getDefaultImage()); 
							build.setColor(Color.red); 
							event.getHook().sendMessageEmbeds(build.build()).queue();
						}
						catch (SQLException e) 
						{
							// TODO Auto-generated catch block
							event.getHook().sendMessage("something went wrong!").queue(); 
							e.printStackTrace();
						} 
					}
				break; 
			case("insertsona"):	// Command to insert a sona into the database 
				
				List<OptionMapping> list = event.getOptions();  
				// Now get all the options and use it to insert into the database 
				
				
			
			try 
			{
				
				if (select.searchUserInSona(event.getUser().getId(), event.getGuild().getId())) 
				{
					event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "already inserted a sona!").queue(); 
					return; 
				}
				
				
				String ex  = list.get(1).getAsString().substring(list.get(1).getAsString().length() - 4, list.get(1).getAsString().length()); 
				if(!ex.contains(".png") && !ex.contains(".jpg") && !ex.contains(".gif")) 
				{
					event.getHook().sendMessage("URL "  + "must end with " + ".png , .jpg or .gif make sure to use a valid imgur image link" ).queue(); 
					return; 
				}
				
				
				boolean res = select.insertSona(list.get(0).getAsString(), event.getUser().getId(), list.get(1).getAsString(),
					event.getGuild().getId(), list.get(2).getAsString(), list.get(3).getAsString(), list.get(4).getAsString()
						, list.get(5).getAsString(), list.get(6).getAsString(), list.get(7).getAsString());
				
				if(res) 
				{
					event.getHook().sendMessage("Sona created succesfully!").queue();
				}
				else 
				{
					event.getHook().sendMessage("Unsuccesfully in creating sona!").queue();
				}
				
			} 
			catch (SQLException e)
			{
				event.getHook().sendMessage("something went wrong!").queue();
				e.printStackTrace();
			} 
				
				break;
				
			case ("removesona"): 
				
				if(event.getOption("user") == null) 
				{
					
					try 
					{
						if (!select.searchUserInSona(event.getUser().getId(), event.getGuild().getId())) 
						{
							event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "does not have a sona!").queue(); 
							return; 
						}
						else 
						{
							// Delete the sona 
							if ( select.removeSona(event.getUser().getId(), event.getGuild().getId())) 
							{
								event.getHook().sendMessage("Sona removed succesfully!").queue();
							}
							else 
							{
								event.getHook().sendMessage("Sona was not removed succesfully!").queue();
							}
						}
					} catch (SQLException e)
					{
						// TODO Auto-generated catch block
						event.getHook().sendMessage("something went wrong!").queue();
						e.printStackTrace();
					}
				}
				else if(Helper.checkRoles(event.getMember().getRoles()))
				{
					// Delete the sona 
					try 
					{
						if (!select.searchUserInSona(event.getOption("user").getAsUser().getId(), event.getGuild().getId())) 
						{
							event.getHook().sendMessage( "<@" +   event.getOption("user").getAsUser().getId() + "> " + "does not have a sona!").queue(); 
							return; 
						}
						
						if ( select.removeSona(event.getOption("user").getAsMember().getId(), event.getGuild().getId())) 
						{
							event.getHook().sendMessage("Sona removed succesfully!").queue();
						}
						else 
						{
							event.getHook().sendMessage("Sona was not removed succesfully!").queue();
						}
					} catch (SQLException e)
					{
						// TODO Auto-generated catch block
						event.getHook().sendMessage("something went wrong!").queue();
						e.printStackTrace();
					}
				}
				else 
				{
					event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only admins can use that command!").queue();
				}
			default: 
				break; 
		}
		
		
	}
		
		/* Delete all waifus and sonas from the server the bot left from */
		@Override
		public void onGuildLeave(GuildLeaveEvent event)
		{
			String id = event.getGuild().getId(); 
			
			// Now delete all sonas and waifus from the server 
			CharacterSelection select = new CharacterSelection(conn); 
			try 
			{	
				select.removeAllSonas(id);
				select.removeAllWaifus(id); 
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
		// User leaves the guild remove their sonas and waifus from the tables 
		@Override  
		public void onGuildMemberRemove(GuildMemberRemoveEvent event)
		{
			System.out.println("Member leave event"); 
			
			
			
			String serverId = event.getGuild().getId(); 
			String userId = event.getUser().getId(); 
			
			CharacterSelection select = new CharacterSelection(conn); 
			try 
			{
				select.removeSona(userId, serverId); 
				select.removeWaifu(userId, serverId); 
			}
			catch(SQLException e) 
			{
				e.printStackTrace(); 
			}
		}
}
