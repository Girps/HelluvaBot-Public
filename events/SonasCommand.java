package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.SETUPTYPE;
import CharactersPack.Character;
import CharactersPack.CharacterFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;


public class SonasCommand extends ListenerAdapter
{
	
	public static EventWaiter waiter; 
	
	public SonasCommand( EventWaiter argWaiter) 
	{
		this.waiter = argWaiter; 
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
					event.getHook().sendMessage("URL "  + "must end with " + ".png , .jpg or .gif make sure to use a valid imgur image link. If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
					return; 
				}
				
			// Check if name is available to avoid duplicates in the server 
				if(select.isAvailable( event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
				{
					event.getHook().sendMessage("<@" + event.getUser().getId() + ">" +" Character name " +  event.getOption("name").getAsString() +  " is unavailable! Make sure to give your sona a distinct name for this server! If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
					return; 
				}
				
				// Check if user has permisson to insert oc 
				if(select.serverWhiteList(event.getGuild().getIdLong()) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) && 
						!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) ) ) 
				{
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
					builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!"); 
					builder.setColor(Color.RED); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					event.getHook().sendMessage(event.getUser().getAsMention() + "! This server requires you to obtain the role " + MarkdownUtil.bold("Helluva Permission") + " or "+ MarkdownUtil.bold("Helluva Admin") + " in order to insert OCs/Sonas!" ).queue(); 
					return; 
				}
				
			// Now let user confirm inserting sona
				
				
				// Get character inserted 
				String CharacterName = event.getOption("name").getAsString(); 
				CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "Sona" ,event.getOption("url").getAsString(), SETUPTYPE.LIGHT); 
				Character temp = factory.getCharacter(); 
				EmbedBuilder builder = new EmbedBuilder(); 
				builder.setTitle(CharacterName); 
				builder.setFooter( event.getMember().getEffectiveName()  + "'s Sona ", event.getMember().getEffectiveAvatarUrl()); 
				builder.setColor(Color.WHITE);
				builder.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm inserting Sona into the bot! You have 30 seconds to react!"); 
				builder.setImage(temp.getDefaultImage());
				// Now return a the custom character and ask user to confirm this character is what they wanted
				event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) -> 
				{
					
						// React to emojis 
						this.waiter.waitForEvent(MessageReactionAddEvent.class, (eReact) ->
						{
							// Now check if its a player in same server and not a bot
						if(!eReact.getUser().isBot() && eReact.getUser().getIdLong() == event.getUser().getIdLong() && eReact.getMessageIdLong() == messageEmbed.getIdLong() ) 
						{
							
							return true; 
						}
						else 
						{
							return false; 
						}
							
						} ,(eSuccess) -> 
						{
							
							if(!event.getOptionsByName("name").isEmpty() &&
									select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + " Sona name selected not avaliable pick another name! Insert cancelled!").queue();
								 return; 
							}
							
							select.insertSona(event.getOption("name").getAsString(), event.getUser().getIdLong(), event.getOption("url").getAsString(),
									event.getGuild().getIdLong(), event.getOption("kdm").getAsString(), event.getOption("smashpass").getAsString(), event.getOption("simps").getAsString()
										, event.getOption("ships").getAsString(), event.getOption("kins").getAsString(), event.getOption("waifu").getAsString(), event.getOption("favorite").getAsString(), event.getOption("guess").getAsString(), event.getOption("collect").getAsString());
							
							event.getHook().sendMessage(eSuccess.getUser().getAsMention() + " your Sona " + MarkdownUtil.bold(CharacterName) + " has been successfully inserted!" ).queue(); 
						} 
						,30L
						, TimeUnit.SECONDS, () -> 
						{
							// On failure remove the oc 
							event.getHook().sendMessage(event.getUser().getAsMention() + " 30 seconds expired! OC/Sona was not added to the bot! If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
						} ) ;
						
				
				});
				
			} 
			catch(Exception e) 
			{
				event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "something went wrong unable to add your sona! Make sure to fill in each option!If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue();
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
				} // Check if Helluva Admin
				else if(Helper.checkAdminRole(event.getMember().getRoles()))
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
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
					builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
					builder.setColor(Color.RED); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can remove another users' sona!").queue();				
					
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
			case "update-sona":
				try 
				{
					CharacterSelection select =  new CharacterSelection(); 
					
					// Check if user has sona or a field has been picked or if name is picked make sure not a duplicated
					if(!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage( event.getUser().getAsMention() + " does not have a sona!").queue(); 
						return; 
					}
					else if(event.getOptions().isEmpty()) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " You must update at-least one field of your sona for this command!").queue();  
						return; 
					}
					else if(!event.getOptionsByName("name").isEmpty() &&
							select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " Sona name selected not avaliable pick another name!").queue();
						 return; 
					} // Check if they have permisson to update
					else if(select.serverWhiteList(event.getGuild().getIdLong()) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) && 
							!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) ) ) 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage(event.getUser().getAsMention() + "! This server requires you to obtain the role " + MarkdownUtil.bold("Helluva Permission") + " or "+ MarkdownUtil.bold("Helluva Admin") + " in order to update OCs/Sonas!" ).queue(); 
						return; 
					}
					
					// Now update a users sona using a hashmap to get fields of the current character 
					HashMap<String, String> Oldfields = select.getSonaFields(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
					
					// Now get new Fields 
					
					List<OptionMapping> newFields = event.getOptions(); 
					
					for(int i = 0; i < newFields.size(); ++i) 
					{
						Oldfields.put(newFields.get(i).getName(), newFields.get(i).getAsString());  
					}
					
					// First return the sona then ask confirmation to update the sona 
					// Get character inserted 
					String CharacterName = Oldfields.get("name"); 
					CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "Sona" ,Oldfields.get("url"), SETUPTYPE.LIGHT); 
					Character temp = factory.getCharacter(); 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(CharacterName); 
					builder.setFooter( event.getMember().getEffectiveName()  + "'s Sona ", event.getMember().getEffectiveAvatarUrl()); 
					builder.setColor(Color.WHITE);
					builder.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm updating Sona into the bot! You have 30 seconds to react!"); 
					builder.setImage(temp.getDefaultImage());
					// Now return a the custom character and ask user to confirm this character is what they wanted
					event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) -> 
					{
						
							// React to emojis 
							this.waiter.waitForEvent(MessageReactionAddEvent.class, (eReact) ->
							{
								// Now check if its a player in same server and not a bot
							if(!eReact.getUser().isBot() && eReact.getUser().getIdLong() == event.getUser().getIdLong() && eReact.getMessageIdLong() == messageEmbed.getIdLong() ) 
							{
								
								return true; 
							}
							else 
							{
								return false; 
							}
								
							} ,(eSuccess) -> 
							{
								if(!event.getOptionsByName("name").isEmpty() &&
										select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
								{
									event.getHook().sendMessage(event.getUser().getAsMention() + " Sona name selected not avaliable pick another name! Update cancelled!").queue();
									 return; 
								}
								select.updateSona(event.getUser().getIdLong(), event.getGuild().getIdLong(), Oldfields );
								event.getHook().sendMessage( event.getUser().getAsMention() + " your Sona updated!").queue();
							} 
							,30L
							, TimeUnit.SECONDS, () -> 
							{
								// On failure remove the oc 
								event.getHook().sendMessage(event.getUser().getAsMention() + " 30 seconds expired! OC/Sona was not updated to the bot! If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
							} ) ;
							
					
					});
					
					
					
					
				} 
				catch(Exception e)
				{
					e.printStackTrace(); 
					event.getHook().sendMessage("Something went wrong!").queue(); 
				}
				break; 
			default: 
				break;
		}
		
		
	}
		
		
}
