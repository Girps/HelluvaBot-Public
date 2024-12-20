package events;

import java.awt.Color;
import events.Helper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.SETUPTYPE;
import CharactersPack.Character;
import CharactersPack.CharacterFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class OriginalCharacterCommand extends ListenerAdapter
{
	
	private static EventWaiter waiter; 
	
	public OriginalCharacterCommand(EventWaiter argWaiter)
	{ 
			waiter = argWaiter; 	
	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		switch(event.getName()) 
		{
			
		case "insert-oc": 
			{
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				CharacterSelection select = new CharacterSelection();
				CompletableFuture<Boolean> limitF = CompletableFuture.supplyAsync( () -> {return !select.checkOCLimit(event.getUser().getIdLong(),event.getGuild().getIdLong());}); 
				CompletableFuture<Boolean>  nameF= CompletableFuture.supplyAsync( () -> {return select.isAvailable(event.getOption("name").getAsString() , event.getGuild().getIdLong() );}); 
				CompletableFuture<Boolean>  whiteListF = CompletableFuture.supplyAsync( () -> 
				{
					return select.serverWhiteList(serverId) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) &&
						!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) );});
				
				List<CompletableFuture<Boolean>> futures = List.of(
						limitF, nameF, whiteListF
				);
				
				CompletableFuture.allOf( futures.toArray(new CompletableFuture[0])).thenAccept( v ->
				{
					event.deferReply().queue(); 
					System.out.println("all of: " + Thread.currentThread().getName()); 
						List<Boolean> results = futures.stream().
						map(CompletableFuture::join)
						.toList(); 
						
						// Check if allowed 
						if (results.get(0))
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " you already reached your character limit of 10! An Oc must be replace!").queue();
							throw new RuntimeException("reached limit of ocs"); 
						}
						else if(results.get(1)) 
						{
							event.getHook().sendMessage( event.getUser().getAsMention() + 
									" Character name " + event.getOption("name").getAsString()  + 
									" is unavailable! Make sure to give your OC a distinct name for this server!" ).queue(); 
								throw new RuntimeException(event.getUser().getAsMention() +
									" Character name " + event.getOption("name").getAsString()  +
									" is unavailable! Make sure to give your OC a distinct name for this server!"); 
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
							throw new RuntimeException(event.getUser().getAsMention() + 
									"! This server requires you to obtain the role "
									+ MarkdownUtil.bold("Helluva Permission") + 
									" or "+ MarkdownUtil.bold("Helluva Admin") + 
									" in order to insert OCs/Sonas!");  
						}
						// now build custom oc
				}).thenApply( (v) -> 
				{
					System.out.println("applyof: " + Thread.currentThread().getName());
					String CharacterName = event.getOption("name").getAsString(); 
					CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "OC" ,event.getOption("url").getAsString(), SETUPTYPE.LIGHT); 
					Character temp = factory.getCharacter(); 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(CharacterName)
					.setFooter( event.getMember().getEffectiveName()  + "'s OC ", event.getMember().getEffectiveAvatarUrl())
					.setColor(Color.WHITE)
					.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm inserting OC into the bot! You have 30 seconds to react!")
					.setImage(temp.getDefaultImage());
					// Now return a the custom character and ask user to confirm this character is what they wanted
					event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) ->
					{
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
								event.getHook().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name! Insert cancelled!").queue();
								 return; 
							}
							
							select.insertOrginalCharacter(event.getOption("name").getAsString() , eSuccess.getUserIdLong(), event.getOption("url").getAsString() ,
									serverId, event.getOption("kdm").getAsString() , event.getOption("smashpass").getAsString() , event.getOption("simps").getAsString() 
										, event.getOption("ships").getAsString() , event.getOption("kins").getAsString() , event.getOption("waifu").getAsString() ,event.getOption("favorite").getAsString() 
										, event.getOption("guess").getAsString() , event.getOption("collect").getAsString());
							
							event.getHook().sendMessage(eSuccess.getUser().getAsMention() + " your OC " + MarkdownUtil.bold(CharacterName) + " has been successfully inserted!" ).queue(); 
						} 
						,30L
						, TimeUnit.SECONDS, () -> 
						{
							// On failure do not add
							event.getHook().sendMessage(event.getUser().getAsMention() + " 30 seconds expired! OC/Sona was not added to the bot! If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
						} ) ;
						
					});
					return null; 
				}).exceptionally( (ex) -> 
				{
					ex.printStackTrace();  
					return null; 
				}) ; 
				break; 
			}
		case "remove-my-oc" : 
			{
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				CompletableFuture.runAsync( () -> 
				{
					event.deferReply().queue(); 
					System.out.println( "in remove async: "+ Thread.currentThread().getName()); 
					String characterName = event.getOption("customcharacter").getAsString();  // gets character name to remove 
					CharacterSelection select = new CharacterSelection();
					// Search for oc 
					if (!select.searchOC(characterName, event.getUser().getIdLong(),  serverId))
					{
						event.getHook().sendMessage( event.getUser().getAsMention() + " OC does not exist!").queue();
						throw new RuntimeException("Oc request does not exist"); 
					}
						// remove oc 
						select.removeCustomCharacter(characterName, event.getUser().getIdLong(), serverId);
						event.getHook().sendMessage( event.getUser().getAsMention() + " OC " + characterName + " succesfully removed!").queue();
				}).exceptionally( (ex) -> 
				{
					ex.printStackTrace(); 
					return null; 
				}); 
				break; 
			}
		case "remove-user-oc" : 
		{
			CompletableFuture.runAsync( () -> 
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
					throw new RuntimeException(); 
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
							throw new RuntimeException(); 
						}
						select.removeCustomCharacter(characterName, userId, serverId);
						event.getHook().sendMessage(event.getUser().getAsMention() + " succesfully removed OC " + characterName  +  " from" +  "<@" + userId+ ">" ).queue();
				}
			}).exceptionally((ex) ->
			{
				ex.printStackTrace(); 
				return null;}
			); 
			break; 
		} 
		case "remove-all-ocs":
		{
			
			CompletableFuture.runAsync( () -> 
			{
				event.deferReply().queue(); 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				if(event.getOptions().isEmpty()) { 
					try 
					{	
						CharacterSelection select = new CharacterSelection();
						if(!select.searchAllUserOcs(userId, serverId)) 
						{
							event.getHook().sendMessage("<@" + userId +">" + " you do not have any OCs!").queue(); 
							throw new RuntimeException("User does not have OCs"); 
						}
						
						select.removeAllOcs(userId,serverId);
						event.getHook().sendMessage("<@" + userId + "> all OCs removed!").queue();
					} 
					catch (Exception e)
					{
					// TODO Auto-generated catch block
						throw new CompletionException(e);
					}
				}
				else if(Helper.checkAdminRole(event.getMember().getRoles())) // Not empty
				{
					userId = event.getOption("user").getAsUser().getIdLong(); 
					try 
					{
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
					catch (Exception  e) 
					{
						// TODO Auto-generated catch block
						throw new CompletionException(e); 
					}
					
				}
				else 
				{
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
					builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
					builder.setColor(Color.RED); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
					event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can remove other users' OCs!").queue();				
					throw new RuntimeException("invalid permissions");
				}
				
			}).exceptionally( (ex) -> 
			{
				ex.printStackTrace(); 
				return null;
			}); 
			}
			break;  
		case "my-oc" : 
		{
			
			// Check if option is null if so return a list of all there characters on an embed
			CompletableFuture.supplyAsync( () -> 
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
				return null; 
			}).exceptionally( (ex) -> 
			{
				ex.printStackTrace(); 
				return null; 
			}); 
			break; 
		}
		case "search-oc": 
			
			CompletableFuture.runAsync( () ->
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
			}
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
			} ).exceptionally( (ex) -> 
			{
				ex.printStackTrace(); 
				return null; 
			}); 
		
			break; 
		case "set-default-oc":
			
			
			CompletableFuture.runAsync( () -> 
			{
				String name = event.getOption("customcharacter").getAsString(); 
				event.deferReply().queue(); 
					CharacterSelection select = new CharacterSelection();
					select.setDefOcCharacter(name, event.getUser().getIdLong(), event.getGuild().getIdLong());
					event.getHook().sendMessage(MarkdownUtil.bold(name) + " has been set as default image for your oc list!").queue();	
			}).exceptionally( (ex) -> 
			{
				ex.printStackTrace(); 
				event.getHook().sendMessage("Something went wrong!").queue();
				return null; 
			} ); 
			break; 
		case "oc-available" : 
		{
			CompletableFuture.runAsync( () -> 
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
				
			}).exceptionally( (ex) -> 
			{
				event.getHook().sendMessage("Something went wrong!").queue();
				ex.printStackTrace(); 
				return null; 
			}); 
			
		}
		break ;
		
		case "update-oc": 
			
				CharacterSelection select =  new CharacterSelection();  
				CompletableFuture<Boolean> searchF = CompletableFuture.supplyAsync( () -> {return !select.searchOC(
						event.getOptionsByName("customcharacter").get(0).getAsString(),event.getUser().getIdLong(), event.getGuild().getIdLong());}); 
				CompletableFuture<Boolean> avaF = CompletableFuture.supplyAsync( () -> {return  ( !event.getOptionsByName("name").isEmpty() ) &&
						select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong());}); 
				CompletableFuture<Boolean> whiteF = CompletableFuture.supplyAsync(() -> 
				{		return select.serverWhiteList(event.getGuild().getIdLong()) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) && 
						!Helper.checkOcSonaPrivellegeRole(event.getMember().getRoles()) ) ;}); 
				List<CompletableFuture<Boolean>> futures = List.of(searchF, avaF,whiteF); 
				
				CompletableFuture.allOf( futures.toArray(new CompletableFuture[0]))
				.thenAccept( v -> 
				{
					event.deferReply().queue(); 
					List<Boolean> results = futures.stream().
					map(CompletableFuture::join)
					.toList(); 
					
					//  now process it
					if (results.get(0)) 
					{
						event.getHook().sendMessage( event.getUser().getAsMention() + " does not have this oc!").queue(); 
						throw new RuntimeException("user does not have this oc"); 
					}
					else if (results.get(1))
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name!").queue();
						throw new RuntimeException("Oc chosen not available"); 
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
						throw new RuntimeException("Doesnt have permssion"); 
					}
					else if (event.getOptions().size() <= 1) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " You must update at-least one field of your sona for this command!").queue();  
						throw new RuntimeException("Not all fields selected"); 
					}
					
				}).thenApply((v) -> 
				{
					String targetName = event.getOption("customcharacter").getAsString(); 
					System.out.println(targetName); 
					// Now update a users sona using a hashmap to get fields of the current character 
					HashMap<String, String> Oldfields = select.getOCFields(targetName, event.getUser().getIdLong(), event.getGuild().getIdLong()); 
					List<OptionMapping> newFields = new ArrayList<OptionMapping>(); 
							event.getOptions(); 
					for(int i =0; i < event.getOptions().size(); ++i) 
					{
						newFields.add(event.getOptions().get(i)); 
					}
					newFields.remove(event.getOption("customcharacter")); 
					for(int i = 0; i < newFields.size(); ++i) 
					{
						Oldfields.put(newFields.get(i).getName(), newFields.get(i).getAsString());  
					}
					return Oldfields; 
				}).thenApply( (updatedFields) -> 
				{
					String targetName = event.getOption("customcharacter").getAsString(); 
					String CharacterName = updatedFields.get("name"); 
					CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "OC" ,updatedFields.get("url"), SETUPTYPE.LIGHT); 
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
								
							} ,(eSuccess) -> CompletableFuture.runAsync( ()  -> 
							{
								if(!event.getOptionsByName("name").isEmpty() &&
										select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
								{
									event.getHook().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name! Update cancelled!").queue();
									 return; 
								}
								select.updateOC(targetName,event.getUser().getIdLong(), event.getGuild().getIdLong(), updatedFields );
								event.getHook().sendMessage(event.getUser().getAsMention() + " your OC updated!").queue();
							}) 
							,30L
							, TimeUnit.SECONDS, () -> 
							{
								// On failure remove the oc 
								event.getHook().sendMessage(event.getUser().getAsMention() + " 30 seconds expired! OC/Sona was not updated to the bot! If you have trouble inserting your OC/Sona watch the following video. Tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ").queue(); 
							} ) ; 
					});
					return null; 
				})
				.exceptionally(ex -> {ex.printStackTrace(); return null;}); 
			break; 
			default : 
				break; 
				
		}

	}
	

}
