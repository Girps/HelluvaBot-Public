package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import CharactersPack.CharacterSelection;
import CharactersPack.Character;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class SonasCommand extends ListenerAdapter
{
	
	public SonasCommand( ) 
	{
		
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		
		String cmd = event.getName(); 
		event.deferReply().queue(); 
		switch(cmd) 
		{
			case("sona"):	// Command to return sona from the database assigned to caller
				
					
					// Check if option is empty 
					if(event.getOption("user") == null) 
					{
						// get callers sona 
						
						try 
						{
							CharacterSelection select = new CharacterSelection(); 
							if (!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "does not have a sona!").queue(); 
								return; 
							}
							// Now get the sona and display it 
							Character sona = select.getUserSona(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
							EmbedBuilder build = new EmbedBuilder(); 
							build.setAuthor(sona.getName()); 
							build.setImage(sona.getDefaultImage()); 
							build.setColor(Color.WHITE); 
							build.setFooter( event.getMember().getEffectiveName() + "'s Sona", event.getMember().getEffectiveAvatarUrl()); 
							event.getHook().sendMessageEmbeds(build.build()).queue();
						}
						catch (Exception e) 
						{
							// TODO Auto-generated catch block
							event.getHook().sendMessage("something went wrong!").queue(); 
							e.printStackTrace();
						} 
					}
					else // searching another user sona  
					{
						Long targetId = event.getOption("user").getAsUser().getIdLong();
						Member target = event.getOption("user").getAsMember();
						try 
						{
							CharacterSelection select = new CharacterSelection(); 
							if (!select.searchUserInSona(targetId, event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage( "<@" + targetId  + "> " + "does not have a sona!").queue(); 
								return; 
							}
							// Now get the sona and display it 
							Character sona = select.getUserSona(targetId, event.getGuild().getIdLong()); 
							EmbedBuilder build = new EmbedBuilder(); 
							build.setAuthor(sona.getName()); 
							build.setImage(sona.getDefaultImage()); 
							build.setColor(Color.WHITE); 
							build.setFooter( target.getEffectiveName() + "'s Sona", target.getEffectiveAvatarUrl()); 
							event.getHook().sendMessageEmbeds(build.build()).queue();
						}
						catch (Exception e) 
						{
							// TODO Auto-generated catch block
							event.getHook().sendMessage("something went wrong!").queue(); 
							e.printStackTrace();
						} 
					}
				break; 
			case("insert-sona"):	// Command to insert a sona into the database 
				
			
				// Now get all the options and use it to insert into the database 
				
			try 
			{
				CharacterSelection select = new CharacterSelection(); 
				if (select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
				{
					event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "you can only have 1 sona! Remove your current sona and use this command again!").queue(); 
					return; 
				}
				
				
				String ex  =  event.getOption("url").getAsString().substring( event.getOption("url").getAsString().length() - 4,  event.getOption("url").getAsString().length());
				if(!ex.contains(".png") && !ex.contains(".jpg") && !ex.contains(".gif")) 
				{
					event.getHook().sendMessage("URL "  + "must end with " + ".png , .jpg or .gif make sure to use a valid imgur image link" ).queue(); 
					return; 
				}
				
			// Check if name is available to avoid duplicates in the server 
				if(select.isAvailable( event.getOption("name").getAsString(),event.getUser().getIdLong(), event.getGuild().getIdLong())) 
				{
					event.getHook().sendMessage("<@" + event.getUser().getId() + ">" +" Character name " +  event.getOption("name").getAsString() +  " is unavailable! Make sure to give your sona a distinct name for this server!" ).queue(); 
					return; 
				}
				
				
			 select.insertSona(event.getOption("name").getAsString(), event.getUser().getIdLong(), event.getOption("url").getAsString(),
					event.getGuild().getIdLong(), event.getOption("kdm").getAsString(), event.getOption("smashpass").getAsString(), event.getOption("simps").getAsString()
						, event.getOption("ships").getAsString(), event.getOption("kins").getAsString(), event.getOption("waifu").getAsString(), event.getOption("favorite").getAsString(), event.getOption("guess").getAsString(), event.getOption("collect").getAsString());
				
			 event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "succesfully added your sona!").queue();
				
			} 
			catch(Exception e) 
			{
				event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "something went wrong unable to add your sona! Make sure to fill in each option!").queue();
				e.printStackTrace();
			}
				
				break;
				
			case ("remove-sona"): 
				
 				if(event.getOption("user") == null) 
				{
					
					try 
					{
						CharacterSelection select = new CharacterSelection(); 
						if (!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
						{
							event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "does not have a sona!").queue(); 
							return; 
						}
						else 
						{
							// Delete the sona 
							if ( select.removeSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage("Sona removed succesfully!").queue();
							}
							else 
							{
								event.getHook().sendMessage("Sona was not removed succesfully!").queue();
							}
						}
					} catch (Exception e)
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
						CharacterSelection select = new CharacterSelection(); 
						if (!select.searchUserInSona(event.getOption("user").getAsUser().getIdLong(), event.getGuild().getIdLong())) 
						{
							event.getHook().sendMessage( "<@" +   event.getOption("user").getAsUser().getId() + "> " + "does not have a sona!").queue(); 
							return; 
						}
						
						if ( select.removeSona(event.getOption("user").getAsMember().getIdLong(), event.getGuild().getIdLong())) 
						{
							event.getHook().sendMessage("Sona removed succesfully!").queue();
						}
						else 
						{
							event.getHook().sendMessage("Sona was not removed succesfully!").queue();
						}
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						event.getHook().sendMessage("something went wrong!").queue();
						e.printStackTrace();
					}
				}
				else 
				{
					event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only Helluva Admins can use that command!").queue();
				} 
				break; 
			case "sona-available": 
			{
				if(event.getOption("user") == null) 
				{
					try 
					{
						
						long userId = event.getUser().getIdLong(); 
						long serverId = event.getGuild().getIdLong(); 
						CharacterSelection select = new CharacterSelection(); 
						
						if (!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
						{
							event.getHook().sendMessage( "<@" + userId + "> " + "does not have a sona!").queue(); 
							return; 
						}
						
						Character target = select.getUserSona(userId, serverId); 
						ArrayList<String> modes = select.CharacterGameModesSona(userId, serverId); 
						String result = "";
						for(String col: modes) 
						{
							result += col + "\n"; 
						}
						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle(target.getName());
						builder.setColor(Color.white); 
						builder.setThumbnail(target.getDefaultImage()); 
						builder.setDescription(result);  
						builder.setFooter(event.getMember().getEffectiveName() + "'s sona", event.getMember().getEffectiveAvatarUrl()); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					}
					catch(Exception  e)
					{
						event.getHook().sendMessage("Something went wrong!").queue(); 
					}
				}
				else 
				{
					try 
					{
						long userId = event.getOption("user").getAsUser().getIdLong(); 
						long serverId =event.getGuild().getIdLong(); 
					 
						CharacterSelection select = new CharacterSelection(); 
						
						if (!select.searchUserInSona(userId, serverId)) 
						{
							event.getHook().sendMessage( "<@" + event.getOption("user").getAsUser().getId() + "> " + "does not have a sona!").queue(); 
							return; 
						}
						
						
						Character target = select.getUserSona(userId, serverId); 
						ArrayList<String> modes = select.CharacterGameModesSona(userId, serverId); 
						String result = "";
						for(String col: modes) 
						{
							result += col + "\n"; 
						}
						EmbedBuilder builder = new EmbedBuilder();
						builder.setTitle(target.getName());
						builder.setColor(Color.white); 
						builder.setThumbnail(target.getDefaultImage()); 
						builder.setDescription(result);  
						builder.setFooter(event.getOption("user").getAsMember().getEffectiveName() + "'s sona", 
								event.getOption("user").getAsMember().getEffectiveAvatarUrl()); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					}
					catch(Exception  e)
					{
						event.getHook().sendMessage("Something went wrong!").queue(); 
					}
				}
				
			}
			break;
			default: 
				break;
		}
		
		
	}
		
		
}
