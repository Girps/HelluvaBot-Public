package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.SETUPTYPE;
import eventHandlers.InsertSonaListener;
import eventHandlers.UpdateSona;
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
	 
	private ExecutorService executor = null; 
	private ScheduledExecutorService sexecutor = null; 
	public SonasCommand(  ExecutorService executor, ScheduledExecutorService sexecutor) 
	{
		this.executor = executor; 
		this.sexecutor = sexecutor; 
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		
		String cmd = event.getName(); 
		switch(cmd) 
		{
			case("sona"):	// Command to return sona from the database assigned to caller	
				
				
				this.executor.submit(() -> 
				{
					event.deferReply().queue();
					
					// check if user or other 
					
					try 
					{ 
						if (event.getOption("user") == null)  
						{
							CharacterSelection select = new CharacterSelection(); 
							// Now get the sona and display it 
							Character sona = select.getUserSona(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
							if(sona ==null) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + " does not have a sona!").queue(); 
							}
							else
							{ 
							EmbedBuilder build = new EmbedBuilder()
										.setAuthor(sona.getName())
										.setImage(sona.getDefaultImage()) 
										.setColor(Color.WHITE) 
										.setFooter( event.getMember().getEffectiveName() + "'s Sona", event.getMember().getEffectiveAvatarUrl()); 
							event.getHook().sendMessageEmbeds(build.build()).queue();
							} 
						}
						else 
						{
							Long targetId = event.getOption("user").getAsUser().getIdLong();
							Member target = event.getOption("user").getAsMember();
							CharacterSelection select = new CharacterSelection(); 	
								// Now get the sona and display it 
								Character sona = select.getUserSona(targetId, event.getGuild().getIdLong()); 
								if (sona == null) 
								{
									event.getHook().sendMessage(target.getAsMention() + " does not have a sona!").queue(); 
								}
								else { 
								EmbedBuilder build = new EmbedBuilder()
										.setAuthor(sona.getName())
										.setImage(sona.getDefaultImage())
										.setColor(Color.WHITE)
										.setFooter( target.getEffectiveName() + "'s Sona", target.getEffectiveAvatarUrl()); 
								event.getHook().sendMessageEmbeds(build.build()).queue(); 
								}
						}
					} 
					catch (Exception ex)
					{
						ex.printStackTrace(); 
						event.getHook().sendMessage(ex.toString()).queue(); 
					}
				}); 
				
				break; 
			case("insert-sona"):	// Command to insert a sona into the database 
				// Now get all the options and use it to insert into the database
				
				this.executor.submit(() -> 
				{
					try { 
					event.deferReply().queue(); 
					CharacterSelection select = new CharacterSelection(); 
					String ex  =  event.getOption("url").getAsString().substring( event.getOption("url").getAsString().length() - 4,  event.getOption("url").getAsString().length());
					// check max sonas
					if (select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " 
					+ "you can only have 1 sona! Remove your current sona and use this command again!").queue(); 
					}
					else if(!ex.contains(".png") && !ex.contains(".jpg") && !ex.contains(".gif")) // Check if is valid link 
					{
						event.getHook().sendMessage("URL "  +
					"must end with " + 
								".png , .jpg or .gif make sure to use a valid imgur image link. If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 

					} 	// Check if name is available to avoid duplicates in the server
					else if(select.isAvailable( event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage("<@" + event.getUser().getId() + ">" + 
					" Character name " +  event.getOption("name").getAsString() +  
					" is unavailable! Make sure to give your sona a distinct name for this server! If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 	
					} // check permission to insert sona is on
					else if (select.serverWhiteList(event.getGuild().getIdLong()) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) && 
							!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) )) 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage(event.getUser().getAsMention() + "! This server requires you to obtain the role " + MarkdownUtil.bold("Helluva Permission") + " or "+ MarkdownUtil.bold("Helluva Admin") + " in order to insert OCs/Sonas!" ).queue(); 
						return;
					}
					else 
					{
						// insert sona
						// Get character inserted 
						String CharacterName = event.getOption("name").getAsString();
						JSONObject obj = new JSONObject("{\"links\": [{\"url\": " + "\"" + event.getOption("url").getAsString() + "\"" + ", \"art_name\": \"\", \"author_link\": \"\", \"author_name\": \"\"}]}"); 
						ArrayList<JSONObject> arryJs = new ArrayList<JSONObject>(); 
						arryJs.add(obj.getJSONArray("links").getJSONObject(0)); 
						CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "Sona"
								,event.getOption("url").getAsString(), 0, arryJs,null, "",SETUPTYPE.LIGHT); 
						Character temp = factory.getCharacter(); 
						EmbedBuilder builder = new EmbedBuilder()
								.setTitle(CharacterName)
								.setFooter( event.getMember().getEffectiveName()  + "'s Sona ", event.getMember().getEffectiveAvatarUrl())
								.setColor(Color.WHITE)
								.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm inserting Sona into the bot! You have 30 seconds to react!")
								.setImage(temp.getDefaultImage());
						HashMap<String,String> characterData = new HashMap<String,String>(); 
						characterData.put("name",event.getOption("name").getAsString());
						characterData.put("url", event.getOption("url").getAsString() ); 
						characterData.put("kdm",event.getOption("kdm").getAsString());
						characterData.put("smashpass",event.getOption("smashpass").getAsString());
						characterData.put("simps",event.getOption("simps").getAsString());
						characterData.put("ships",event.getOption("ships").getAsString());
						characterData.put("kins",event.getOption("kins").getAsString());
						characterData.put("waifu",event.getOption("waifu").getAsString());
						characterData.put("favorite",event.getOption("favorite").getAsString());
						characterData.put("guess",event.getOption("guess").getAsString());
						characterData.put("collect",event.getOption("collect").getAsString());
						

						
						// Now return a the custom character and ask user to confirm this character is what they wanted
						event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) -> 
						{
							event.getJDA().addEventListener(new InsertSonaListener( executor, sexecutor, messageEmbed.getIdLong(),
									event.getUser().getIdLong(), characterData ,event)); 
						});
					}
					} 
					catch(Exception ex)
					{
						ex.printStackTrace(); 
						event.getHook().sendMessage(ex.getMessage()).queue(); 
					}
				});   
				break;
				
			case ("remove-sona"): 
				
				// submit
				this.executor.submit(() -> 
				{
					try 
					{ 
						event.deferReply().queue();
						if(event.getOption("user") == null) 
						{
								CharacterSelection select = new CharacterSelection(); 
								if (!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
								{
									event.getHook().sendMessage( "<@" + event.getUser().getId() + "> " + "does not have a sona!").queue(); 
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
						}
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
					}
					catch(Exception ex) 
					{
						event.getHook().sendMessage(ex.getMessage()).queue(); 
					}
				}); 
				break; 
			case "sona-available": 
			{
				this.executor.submit(() -> 
				{
					try 
					{ 
						if(event.getOption("user") == null) 
						{
							event.deferReply().queue();
							long userId = event.getUser().getIdLong(); 
							long serverId = event.getGuild().getIdLong(); 
							CharacterSelection select = new CharacterSelection(); 
							if (!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
							{
								event.getHook().sendMessage( "<@" + userId + "> " + "does not have a sona!").queue(); 
							}
							else
							{ 
								Character target = select.getUserSona(userId, serverId); 
								ArrayList<String> modes = select.CharacterGameModesSona(userId, serverId); 
								String result = "";
								for(String col: modes) 
								{
									result += col + "\n"; 
								}
								EmbedBuilder builder = new EmbedBuilder()
										.setTitle(target.getName())
										.setColor(Color.white)
										.setThumbnail(target.getDefaultImage())
										.setDescription(result)
										.setFooter(event.getMember().getEffectiveName() + "'s sona", event.getMember().getEffectiveAvatarUrl()); 
								event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							}
						}
						else 
						{
							event.deferReply().queue();
							long userId = event.getOption("user").getAsUser().getIdLong(); 
							long serverId =event.getGuild().getIdLong(); 
							CharacterSelection select = new CharacterSelection(); 
							if (!select.searchUserInSona(userId, serverId)) 
							{
								event.getHook().sendMessage( "<@" + event.getOption("user").getAsUser().getId() + "> " + "does not have a sona!").queue();  
							}
							else 
							{ 
								Character target = select.getUserSona(userId, serverId); 
								ArrayList<String> modes = select.CharacterGameModesSona(userId, serverId); 
								String result = "";
								for(String col: modes) 
								{
									result += col + "\n"; 
								}
								EmbedBuilder builder = new EmbedBuilder()
										.setTitle(target.getName())
										.setColor(Color.white)
										.setThumbnail(target.getDefaultImage())
										.setDescription(result)
										.setFooter(event.getOption("user").getAsMember().getEffectiveName() + "'s sona", 
										event.getOption("user").getAsMember().getEffectiveAvatarUrl()); 
								event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							} 
						}
					} 
					catch(Exception ex) 
					{
						event.getHook().sendMessage(ex.getMessage()).queue(); 
					}
				}); 
			}
			break;
			case "update-sona":
				
				this.executor.submit( () -> 
				{
					try { 
					event.deferReply().queue(); 
					CharacterSelection select =  new CharacterSelection(); 
					// Check if user has sona or a field has been picked or if name is picked make sure not a duplicated
					if(!select.searchUserInSona(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage( event.getUser().getAsMention() + " does not have a sona!").queue(); 
					}
					else if(event.getOptions().isEmpty()) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " You must update at-least one field of your sona for this command!").queue();  
					}
					else if(!event.getOptionsByName("name").isEmpty() &&
							select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " Sona name " + MarkdownUtil.bold(event.getOption("name").getAsString()) + 
								" selected not avaliable pick another name!").queue();
					} // Check if they have permisson to update
					else if(select.serverWhiteList(event.getGuild().getIdLong()) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) && 
							!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) ) ) 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage(event.getUser().getAsMention() + "! This server requires you to obtain the role " + MarkdownUtil.bold("Helluva Permission") + " or "+ MarkdownUtil.bold("Helluva Admin") 
						+ " in order to update OCs/Sonas!" ).queue(); 
					}
					else
					{ 
						// Now update a users sona using a hashmap to get fields of the current character 
						HashMap<String, String> Oldfields = select.getSonaFields(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
						// Now get new Fields 	
						List<OptionMapping> newFields = event.getOptions(); 
						String oldName = Oldfields.get("name"); 
						for(int i = 0; i < newFields.size(); ++i) 
						{
							if(newFields.get(i).getName().equals("url"))
							{
								Oldfields.put("url", "{\"links\": [{\"url\": " + "\"" + newFields.get(i).getAsString() + "\"" + ", \"art_name\": \"\", \"author_link\": \"\", \"author_name\": \"\"}]}" ); 
							}
							else { 
							Oldfields.put(newFields.get(i).getName(), newFields.get(i).getAsString());  
							}
						}
						
						ArrayList<JSONObject> arrObject = new ArrayList<JSONObject>(); 
						
						JSONObject obj = new JSONObject ( Oldfields.get("url") ); 
						JSONArray  jArry = obj.getJSONArray("links");  
						
						for(int i =0; i < jArry.length(); ++i) 
						{
							arrObject.add(jArry.getJSONObject(i)); 
						}
						
						// First return the sona then ask confirmation to update the sona 
						// Get character inserted 
						String CharacterName = Oldfields.get("name"); 
						CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "Sona" ,Oldfields.get("url"),
								0, arrObject, null,"", SETUPTYPE.LIGHT); 
						Character temp = factory.getCharacter(); 
						EmbedBuilder builder = new EmbedBuilder()
								.setTitle(CharacterName)
								.setFooter( event.getMember().getEffectiveName()  + "'s Sona ", event.getMember().getEffectiveAvatarUrl())
								.setColor(Color.WHITE)
								.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm updating Sona into the bot! You have 30 seconds to react!")
								.setImage(temp.getDefaultImage());
						
						// Now return a the custom character and ask user to confirm this character is what they wanted
						event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) -> 
						{
							event.getJDA().addEventListener(new UpdateSona( executor, sexecutor, 
									messageEmbed.getIdLong(), event.getUser().getIdLong(), oldName , Oldfields, event) ); 
						});
					}
					
					} 
					catch(Exception ex)
					{
						ex.printStackTrace(); 
						event.getHook().sendMessage("Something went wrong!").queue(); 
					}
				}); 
				break; 
			default: 
				break;
		}
		
		
	}
		
		
}
