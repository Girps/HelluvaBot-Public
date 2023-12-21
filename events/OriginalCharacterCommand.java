package events;

import java.awt.Color;
import events.Helper;
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
		
		
		Long userId = event.getUser().getIdLong(); 
		Long serverId = event.getGuild().getIdLong(); 
		
		switch(event.getName()) 
		{
			
		case "insert-oc": 
			{
				
		
				try {

					CharacterSelection select = new CharacterSelection();
					// First check if player hasn't reached the 10 character limit 
					if(!select.checkOCLimit(userId, serverId)) 
					{
						event.getHook().sendMessage("<@"+ userId +"> you already reached your character limit of 10! An Oc must be replace!").queue();
						return; 
					}
			
					// First check if oc name is already taken 
					if(select.isAvailable(event.getOption("name").getAsString() , serverId)) 
					{
						event.getHook().sendMessage("<@" + userId + ">" +" Character name " + event.getOption("name").getAsString()  +  " is unavailable! Make sure to give your OC a distinct name for this server!" ).queue(); 
						return;
					}
					
					// Check if user has permisson to insert oc 
					if(select.serverWhiteList(serverId) &&  ( !Helper.checkAdminRole(event.getMember().getRoles()) &&
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
					
					
					
					
					
					
					// Get character inserted 
					String CharacterName = event.getOption("name").getAsString(); 
					CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "OC" ,event.getOption("url").getAsString(), SETUPTYPE.LIGHT); 
					Character temp = factory.getCharacter(); 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(CharacterName); 
					builder.setFooter( event.getMember().getEffectiveName()  + "'s OC ", event.getMember().getEffectiveAvatarUrl()); 
					builder.setColor(Color.WHITE);
					builder.setDescription( event.getUser().getAsMention() + "! "+ "react to this image to confirm inserting OC into the bot! You have 30 seconds to react!"); 
					builder.setImage(temp.getDefaultImage());
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
					
				}
				catch (Exception e) 
				{
					event.getHook().sendMessage(event.getUser().getAsMention() + " unable to add original character make sure each option is filled correctly! ERROR can also be caused by invalid URL watch the following tutorial to insert ocs and sona https://www.youtube.com/watch?v=iHQl8KG_ZAQ " ).queue(); 
					e.printStackTrace(); 
				}
				break; 
			}
		case "remove-my-oc" : 
			{
				String characterName = event.getOption("customcharacter").getAsString();  // gets character name to remove 
				
				try 
				{
					CharacterSelection select = new CharacterSelection();
					// Search for oc 
					if (!select.searchOC(characterName, userId,  serverId))
					{
						event.getHook().sendMessage("<@" + userId + ">" + " OC does not exist!").queue();
						return; 
					}
						// remove oc 
						select.removeCustomCharacter(characterName, userId, serverId);
						event.getHook().sendMessage("<@" + userId+ ">" + " OC " + characterName + " succesfully removed!").queue();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					event.getHook().sendMessage("Something went wrong!").queue(); 
					e.printStackTrace();
				} 
				

				break; 
			}
			
		case "remove-user-oc" : 
		{
			
			CharacterSelection select = new CharacterSelection();
			if(!Helper.checkAdminRole(event.getMember().getRoles())) 
			{
				EmbedBuilder builder = new EmbedBuilder(); 
				builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
				builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
				builder.setColor(Color.RED); 
				event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can remove other users' OCs!").queue();				
				return; 
			}
			else 
			{
				
				String characterName = ""; 
				// remove oc 
				try
				{
					userId = event.getOption("user").getAsUser().getIdLong(); 
					 characterName = event.getOption("customcharacter").getAsString(); 
					// Search for oc 
					if (!select.searchOC(characterName, userId,  serverId))
					{
						event.getHook().sendMessage( event.getUser().getAsMention() + "<@" + userId + ">" + " does not have OC " + characterName  +"!").queue();
						return; 
					}
					select.removeCustomCharacter(characterName, userId, serverId);
					event.getHook().sendMessage(event.getUser().getAsMention() + " succesfully removed OC " + characterName  +  " from" +  "<@" + userId+ ">" ).queue();
				} 
				catch(Exception e) 
				{
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong! Make sure to fill in fields!").queue(); 
					return; 
				}
			}
			
			
			break; 
		}
		case "remove-all-ocs":
		{
			
			if(event.getOptions().isEmpty()) { 
				try 
				{	
					CharacterSelection select = new CharacterSelection();
					if(!select.searchAllUserOcs(userId, serverId)) 
					{
						event.getHook().sendMessage("<@" + userId +">" + " you do not have any OCs!").queue(); 
						return; 
					}
					
					select.removeAllOcs(userId,serverId);
					event.getHook().sendMessage("<@" + userId + "> all OCs removed!").queue();
				} 
				catch (Exception e)
				{
				// TODO Auto-generated catch block
					event.getHook().sendMessage("Something went wrong!").queue();
					e.printStackTrace();
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
						return; 
					}
					select.removeAllOcs(userId, serverId); // remove targeted users' ocs 
					event.getHook().sendMessage(event.getUser().getAsMention() + " has cleared " + event.getOption("user").getAsUser().getAsMention() + "'s OC list!").queue(); 
				} 
				catch (Exception  e) {
					// TODO Auto-generated catch block
					event.getHook().sendMessage("Something went wrong!").queue();
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
				event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can remove other users' OCs!").queue();				
		
			}
			}
			break;  
		case "my-oc" : 
		{
			System.out.println("my-oc called!"); 
			// Check if option is null if so return a list of all there characters on an embed
			try 
			{
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
			}
			
			break; 
		}
		case "search-oc": 
			
			try 
			{
				userId = event.getOption("user").getAsUser().getIdLong(); 
				CharacterSelection select = new CharacterSelection();
				
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
					return; 
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
						return;
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
			catch (Exception e) {
				// TODO Auto-generated catch block
				event.getHook().sendMessage("Something went wrong!").queue(); 
				e.printStackTrace();
			}
			
			break; 
		case "set-default-oc":
			String name = event.getOption("customcharacter").getAsString(); 
			
			try 
			{
				CharacterSelection select = new CharacterSelection();
				select.setDefOcCharacter(name, event.getUser().getIdLong(), event.getGuild().getIdLong());
				event.getHook().sendMessage(MarkdownUtil.bold(name) + " has been set as default image for your oc list!").queue();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getHook().sendMessage("Something went wrong!").queue();
			}
			
			
			break; 
		case "oc-available" : 
		{
			try 
			{
				
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
			catch(Exception e) 
			{
				event.getHook().sendMessage("Something went wrong!").queue(); 
			}
		}
		break ;
		
		case "update-oc": 
			try 
			{
				CharacterSelection select =  new CharacterSelection();  
				// Check if user has sona or a field has been picked or if name is picked make sure not a duplicated
				if(!select.searchOC(event.getOptionsByName("customcharacter").get(0).getAsString(),event.getUser().getIdLong(), event.getGuild().getIdLong())) 
				{
					event.getHook().sendMessage( event.getUser().getAsMention() + " does not have this oc!").queue(); 
					return; 
				}
				else if(event.getOptions().size() <= 1) 
				{
					event.getHook().sendMessage(event.getUser().getAsMention() + " You must update at-least one field of your sona for this command!").queue();  
					return; 
				}
				else if( ( !event.getOptionsByName("name").isEmpty() ) &&
						select.isAvailable(event.getOption("name").getAsString(), event.getGuild().getIdLong())) 
				{
					event.getHook().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name!").queue();
					 return; 
				}
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
				
				String targetName = event.getOption("customcharacter").getAsString(); 
				
				// Now update a users sona using a hashmap to get fields of the current character 
				HashMap<String, String> Oldfields = select.getOCFields(targetName, event.getUser().getIdLong(), event.getGuild().getIdLong()); 
				
				// Now get new Fields 
				
				List<OptionMapping> newFields = event.getOptions(); 
				newFields.remove(event.getOption("customcharacter")); 
				
				for(int i = 0; i < newFields.size(); ++i) 
				{
					Oldfields.put(newFields.get(i).getName(), newFields.get(i).getAsString());  
				}
				
				// Let user confirm updating oc 
				String CharacterName = Oldfields.get("name"); 
				CharacterFactory factory = new CharacterFactory(-1L, CharacterName, "OC" ,Oldfields.get("url"), SETUPTYPE.LIGHT); 
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
								event.getHook().sendMessage(event.getUser().getAsMention() + " OC name selected not avaliable pick another name! Update cancelled!").queue();
								 return; 
							}
							select.updateOC(targetName,event.getUser().getIdLong(), event.getGuild().getIdLong(), Oldfields );
							event.getHook().sendMessage("your OC updated!").queue();
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
			default : 
				break; 
		}

	}
	

}
