package events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import org.json.JSONArray;
import org.json.JSONObject;
import CharactersPack.CharacterSelection;
import CharactersPack.SETUPTYPE;
import eventHandlers.InsertOCListener;
import eventHandlers.UpdateOCListener;
import CharactersPack.Character;
import CharactersPack.CharacterFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class OriginalCharacterCommand extends ListenerAdapter
{
	
	private static ExecutorService executor; 
	ScheduledExecutorService sexecutor; 
	public OriginalCharacterCommand( ExecutorService executor,ScheduledExecutorService sexecutor)
	{ 
			this.executor = executor; 
			this.sexecutor = sexecutor; 
	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		switch(event.getName()) 
		{
			
		case "insert-oc": 
			{
				this.executor.submit(()-> 
				{
					try
					{ 
						event.deferReply().queue(); 
						Long userId = event.getUser().getIdLong(); 
						Long serverId = event.getGuild().getIdLong(); 
						CharacterSelection select = new CharacterSelection();
						List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
						tasks.add( ()-> {return !select.checkOCLimit(event.getUser().getIdLong(),event.getGuild().getIdLong());}); 
						tasks.add(()-> {return select.isAvailable(event.getOption("name").getAsString() , event.getGuild().getIdLong() );});
						tasks.add(()->{
							return select.serverWhiteList(serverId) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) &&
									!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) );}); 
						List<Boolean> results = this.executor.invokeAll(tasks).stream().map( future -> 
						{
							try 
							{
								return future.get();
							} catch (InterruptedException | ExecutionException ex)
							{
								// TODO Auto-generated catch block
								throw new RuntimeException(ex); 
							}
						}).toList();
						
						
						// Check if allowed 
						if (results.get(0))
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " you already reached your character limit of 10! An Oc must be replace!").queue();
						}
						else if(results.get(1)) 
						{
							event.getHook().sendMessage( event.getUser().getAsMention() + 
									" Character name " + event.getOption("name").getAsString()  + 
									" is unavailable! Make sure to give your OC a distinct name for this server!" ).queue(); 
						}
						else if (results.get(2)) 
						{
							EmbedBuilder builder = new EmbedBuilder(); 
							builder.setImage("https://i.imgur.com/lekCghO.jpg"); 
							builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!"); 
							builder.setColor(Color.RED); 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							event.getHook().sendMessage(event.getUser().getAsMention() + "! This server requires you to obtain the role "
									+ MarkdownUtil.bold("Helluva Permission") 
							+ " or "+ MarkdownUtil.bold("Helluva Admin") 
							+ " in order to insert OCs/Sonas!" ).queue(); 
						}
						else 
						{
							// passed conditions
							String CharacterName = event.getOption("name").getAsString(); 
							JSONObject obj = new JSONObject("{\"links\": [{\"url\": " + "\"" + event.getOption("url").getAsString() + "\"" + ", \"art_name\": \"\", \"author_link\": \"\", \"author_name\": \"\"}]}"); 
							ArrayList<JSONObject> arryJs = new ArrayList<JSONObject>(); 
							arryJs.add(obj.getJSONArray("links").getJSONObject(0)); 
							CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "OC" ,event.getOption("url").getAsString(), 0, arryJs, SETUPTYPE.LIGHT); 
							Character temp = factory.getCharacter(); 
							EmbedBuilder builder = new EmbedBuilder(); 
							builder.setTitle(CharacterName)
							.setFooter( event.getMember().getEffectiveName()  + "'s OC ", event.getMember().getEffectiveAvatarUrl())
							.setColor(Color.WHITE)
							.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm inserting OC into the bot! You have 30 seconds to react!")
							.setImage(temp.getDefaultImage());
							HashMap<String,String> characterData = new HashMap<String,String>(); 
							characterData.put("name",event.getOption("name").getAsString());
							characterData.put("url",event.getOption("url").getAsString());
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
								event.getJDA().addEventListener(new InsertOCListener(executor, sexecutor, messageEmbed.getIdLong(),
										event.getUser().getIdLong(), characterData, event) ); 
							});
						}
					} 
					catch(Exception ex) 
					{
						event.getHook().sendMessage("Somethng went wrong!").queue(); 
						ex.printStackTrace(); 
					}
				});
			}
			break;
		case "remove-my-oc" : 
			{
				this.executor.submit(() -> 
				{
					try 
					{ 
						event.deferReply().queue(); 
						Long userId = event.getUser().getIdLong(); 
						Long serverId = event.getGuild().getIdLong(); 
						String characterName = event.getOption("customcharacter").getAsString();  // gets character name to remove 
						CharacterSelection select = new CharacterSelection();
						// Search for oc 
						if (!select.searchOC(characterName, event.getUser().getIdLong(),  serverId))
						{
							event.getHook().sendMessage( event.getUser().getAsMention() + " OC does not exist!").queue();
						}
						else 
						{
							select.removeCustomCharacter(characterName, event.getUser().getIdLong(), serverId);
							event.getHook().sendMessage( event.getUser().getAsMention() + " OC " + characterName + " succesfully removed!").queue();
						}
					} 
					catch(Exception ex)
					{
						event.getHook().sendMessage("Something went wrong!");
						ex.printStackTrace(); 
					}
				}); 
				break; 
			}
		case "remove-user-oc" : 
		{
			this.executor.submit( () -> 
			{
				try 
				{
					event.deferReply().queue(); 
					Long serverId = event.getGuild().getIdLong(); 
					CharacterSelection select = new CharacterSelection();
					if(!Helper.checkAdminRole(event.getMember().getRoles())) 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can remove other users' OCs!").queue();				
					}
					else 
					{
						String characterName = ""; 
						// remove oc 
						Long userId = event.getOption("user").getAsUser().getIdLong(); 
						characterName = event.getOption("customcharacter").getAsString(); 
						// Search for oc 
						if (!select.searchOC(characterName, userId,  serverId))
						{
							event.getHook().sendMessage( event.getUser().getAsMention() + " the user <@" + userId + ">" + " does not have OC " + characterName  +"!").queue();
						}
						else 
						{ 
							select.removeCustomCharacter(characterName, userId, serverId);
							event.getHook().sendMessage(event.getUser().getAsMention() + " succesfully removed OC " + characterName  +  " from" +  "<@" + userId+ ">" ).queue();
						}
					}
				}
				catch(Exception ex)
				{
					event.getHook().sendMessage("Something went wrong!").queue(); 
					ex.printStackTrace(); 
				}
				
			}); 
			break; 
		} 
		case "remove-all-ocs":
		{
			this.executor.submit(()-> 
			{
				try { 
				event.deferReply().queue(); 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				if(event.getOptions().isEmpty()) { 
						
					CharacterSelection select = new CharacterSelection();
					if(!select.searchAllUserOcs(userId, serverId)) 
					{
						event.getHook().sendMessage("<@" + userId +">" + " you do not have any OCs!").queue(); 
					}
					else 
					{
						select.removeAllOcs(userId,serverId);
						event.getHook().sendMessage("<@" + userId + "> all OCs removed!").queue();
					} 
				}
				else if(Helper.checkAdminRole(event.getMember().getRoles())) // Not empty
				{
					userId = event.getOption("user").getAsUser().getIdLong(); 
					CharacterSelection select = new CharacterSelection();
					// Check that user has ocs 
					if(!select.searchAllUserOcs(userId, serverId)) 
					{
						event.getHook().sendMessage("<@" + userId +">" + " does not have any OCs!").queue(); 
						throw new RuntimeException("User does not have OCs"); 
					}
					select.removeAllOcs(userId, serverId); // remove targeted users' ocs 
					event.getHook().sendMessage(event.getUser().getAsMention() + " has cleared " + event.getOption("user").getAsUser().getAsMention() + "'s OC list!").queue(); 
				
				}
				else 
				{
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
					builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
					builder.setColor(Color.RED); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can remove other users' OCs!").queue();				
				}
				} catch(Exception ex) 
				{
					event.getHook().sendMessage("Something went wrong!").queue(); 
					ex.printStackTrace(); 
				}
			});  
			}
			break;  
		case "my-oc" : 
		{
			// Check if option is null if so return a list of all there characters on an embed
			this.executor.submit(()-> 
			{
				event.deferReply().queue(); 
				try 
				{	
					Long userId = event.getUser().getIdLong(); 
					Long serverId = event.getGuild().getIdLong(); 
					CharacterSelection select = new CharacterSelection();
					// print all ocs 
					if(event.getOptions().isEmpty()) 
					{	
						ArrayList<String> chars = null; 
						chars = select.getUsersOCName(userId, serverId);
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setAuthor( event.getMember().getEffectiveName() + "'s OC list ", event.getMember().getEffectiveAvatarUrl(),event.getMember().getEffectiveAvatarUrl()); 
						builder.setColor(Color.WHITE);
						String result = ""; 
						// Now print them in a list 
						for(String temps : chars)
						{
							result += "- " + temps + "\n";  
						}
						builder.setDescription(result); 
						builder.setThumbnail(select.getOC(chars.get(0), userId, serverId).getDefaultImage()); 
						
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					}
					else // print single name
					{
						String CharacterName = event.getOption("customcharacter").getAsString();
						Character temp = select.getOC(CharacterName, userId, serverId); 
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setTitle(CharacterName); 
						builder.setFooter( event.getMember().getEffectiveName()  + "'s OC ", event.getMember().getEffectiveAvatarUrl()); 
						builder.setColor(Color.WHITE);
						builder.setImage(temp.getDefaultImage());
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					}
				}
				catch(Exception e) 
				{
					event.getHook().sendMessage(event.getUser().getAsMention() + " you do not have any OCs in this server! ").queue();
					throw new CompletionException(e); 
				}
			}); 
			break; 
		}
		case "search-oc": 
			
			this.executor.submit(()->
			{
				event.deferReply().queue(); 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				userId = event.getOption("user").getAsUser().getIdLong(); 
				CharacterSelection select = new CharacterSelection();
			try 
			{ 
					if(event.getOptions().size() == 2)
					{
						if(!select.searchAllUserOcs(userId, serverId)) 
						{
							event.getHook().sendMessage("<@" + userId +">" + " does not have any OCs!").queue(); 
							return;
						}
					
					// Name of character 
					String CharacterName = event.getOption("customcharacter").getAsString(); 
					Character temp = select.getOC(CharacterName, userId, serverId); 
					
					if(temp == null) 
					{
						event.getHook().sendMessage("<@" + userId +">" + " does not have " + CharacterName + "!" ).queue(); 
						throw new RuntimeException("Doesnt have that specific character"); 
					}
					
					EmbedBuilder builder = new EmbedBuilder(); 
						builder.setTitle(CharacterName); 
						builder.setFooter( event.getOption("user").getAsMember().getEffectiveName()  + "'s OC ", event.getOption("user").getAsMember().getEffectiveAvatarUrl()); 
						builder.setColor(Color.WHITE);
						builder.setImage(temp.getDefaultImage());
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					}
					else if (event.getOptions().size() == 1)
					{
						if(!select.searchAllUserOcs(userId, serverId)) 
						{
							event.getHook().sendMessage("<@" + userId +">" + " does not have any OCs!").queue(); 
							throw new RuntimeException("No ocs"); 
						}
						
						
						ArrayList<String> chars = null; 
						chars = select.getUsersOCName(userId, serverId);
						
						
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setAuthor( event.getOption("user").getAsMember().getEffectiveName() + "'s OC list ", event.getOption("user").getAsMember().getEffectiveAvatarUrl(),event.getOption("user").getAsMember().getEffectiveAvatarUrl());  
						builder.setColor(Color.WHITE);
						String result = ""; 
						// Now print them in a list 
						for(String temps : chars)
						{
							result += "- " + temps + "\n";  
						}
						builder.setDescription(result); 
						builder.setThumbnail(select.getOC(chars.get(0), userId, serverId).getDefaultImage()); 
						
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					}
			} catch(Exception ex) 
			{
				event.getHook().sendMessage("Something went wrong!"); 
				ex.printStackTrace(); 
			}
			}); 
			break; 
		case "set-default-oc":
			
			this.executor.submit(()-> 
			{
				try 
				{
					String name = event.getOption("customcharacter").getAsString(); 
					event.deferReply().queue(); 
					CharacterSelection select = new CharacterSelection();
					select.setDefOcCharacter(name, event.getUser().getIdLong(), event.getGuild().getIdLong());
					event.getHook().sendMessage(MarkdownUtil.bold(name) + " has been set as default image for your oc list!").queue();
				}
				catch(Exception ex) 
				{
					event.getHook().sendMessage("Something went wrong!").queue();
					ex.printStackTrace(); 
				}
				
			}); 
			break; 
		case "oc-available" : 
		{
			this.executor.submit(() -> 
			{
				try 
				{ 
					event.deferReply().queue(); 
					Long userId = event.getUser().getIdLong(); 
					Long serverId = event.getGuild().getIdLong(); 
					String charName = event.getOption("customcharacter").getAsString(); 
					CharacterSelection select = new CharacterSelection(); 
					
					if (!select.searchOC(charName,event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						event.getHook().sendMessage( "<@" + userId + "> " + "does not this oc!").queue(); 
						return; 
					}
					
					Character target = select.getOC(charName, userId, serverId);  
					ArrayList <String> modes = select.CharacterGameModesOc(userId, serverId, target.getId()); 
										
					String result = "";
					for(String col: modes) 
					{
						result += col + "\n"; 
					}
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle(charName);
					builder.setColor(Color.white); 
					builder.setThumbnail(target.getDefaultImage()); 
					builder.setDescription(result);  
					builder.setFooter(event.getMember().getEffectiveName() + "'s oc", event.getMember().getEffectiveAvatarUrl()); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					} 
				catch(Exception ex) 
				{
					event.getHook().sendMessage("Something went wrong!").queue();
					ex.printStackTrace(); 
				}
			}); 
		}
		break ;
		
		case "update-oc": 
			
		{ 
				// task update the oc
				this.executor.submit(()-> 
				{
					try 
					{ 
						event.deferReply().queue();
						CharacterSelection select =  new CharacterSelection();  
						List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>(); 
						tasks.add( ()-> {return !select.searchOC(
								event.getOptionsByName("customcharacter").get(0).getAsString(),event.getUser().getIdLong(), event.getGuild().getIdLong());});
						tasks.add(() -> {return  ( !event.getOptionsByName("name").isEmpty() ) &&
								select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong());});
						tasks.add(()-> {		return select.serverWhiteList(event.getGuild().getIdLong()) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) && 
								!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) ) ;}); 
						
						List<Boolean> results = this.executor.invokeAll( tasks).stream().map( future -> {try {
							return future.get();
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							throw new RuntimeException(e); 
						}
						
						}).toList(); 
						
						//  now process it
						if (results.get(0)) 
						{
							event.getHook().sendMessage( event.getUser().getAsMention() + " does not have this oc!").queue(); 
						}
						else if (results.get(1))
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name!").queue();
						}
						else if (results.get(2)) 
						{
							EmbedBuilder builder = new EmbedBuilder()
									.setImage("https://i.imgur.com/lekCghO.jpg")
							.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to users of this server in order to use this command!")
							.setColor(Color.RED); 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							event.getHook().sendMessage(event.getUser().getAsMention() +
									"! This server requires you to obtain the role " + 
									MarkdownUtil.bold("Helluva Permission") + 
									" or "+ MarkdownUtil.bold("Helluva Admin") + " in order to update OCs/Sonas!" ).queue(); 
						}
						else if (event.getOptions().size() <= 1) 
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " You must update at-least one field of your sona for this command!").queue();  
						}
						else // pass conditions now update the characters 
						{
							// get the character data
							String targetName = event.getOption("customcharacter").getAsString(); 
							// Now update a users sona using a hashmap to get fields of the current character 
							HashMap<String, String> Oldfields = select.getOCFields(targetName, event.getUser().getIdLong(), event.getGuild().getIdLong()); 
							List<OptionMapping> newFields = new ArrayList<OptionMapping>(); 
									event.getOptions(); 
							for(int i =0; i < event.getOptions().size(); ++i) 
							{
								newFields.add(event.getOptions().get(i)); 
							}
							newFields.remove(event.getOption("customcharacter")); 
							String oldName = Oldfields.get("name"); 
							for(int i = 0; i < newFields.size(); ++i) 
							{
								if(newFields.get(i).getName().equals("url"))
								{
									Oldfields.put("url", "{\"links\": [{\"url\": " + "\"" + newFields.get(i).getAsString() + "\"" + ", \"art_name\": \"\", \"author_link\": \"\", \"author_name\": \"\"}]}" ); 
								}
								else
								{ 
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
							
							String CharacterName = Oldfields.get("name"); 
							CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "OC" ,Oldfields.get("url"), 0, arrObject , SETUPTYPE.LIGHT); 
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
								event.getJDA().addEventListener(new UpdateOCListener( executor, sexecutor,  messageEmbed.getIdLong
										(), event.getUser().getIdLong(), Oldfields, targetName, event)); 
							} ); 
						}
					}
					catch(Exception ex) 
					{
						event.getHook().sendMessage("Something went wrong!").queue(); 
						ex.printStackTrace(); 
					}
					
				}); 	
		}
			break; 
			default : 
				break; 
				
		}

	}
	

}
