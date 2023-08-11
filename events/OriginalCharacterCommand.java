package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;



import CharactersPack.CharacterSelection;
import CharactersPack.Character;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class OriginalCharacterCommand extends ListenerAdapter
{
	
	
	public OriginalCharacterCommand()
	{

	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		
		
		Long userId = event.getUser().getIdLong(); 
		Long serverId = event.getGuild().getIdLong(); 
		event.deferReply().queue();
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
					if(select.isAvailable(event.getOption("name").getAsString() , userId, serverId)) 
					{
						event.getHook().sendMessage("<@" + userId + ">" +" Character name " + event.getOption("name").getAsString()  +  " is unavailable! Make sure to give your OC a distinct name for this server!" ).queue(); 
						return;
					}
					
					
					select.insertOrginalCharacter(event.getOption("name").getAsString() , userId, event.getOption("url").getAsString() ,
						serverId, event.getOption("kdm").getAsString() , event.getOption("smashpass").getAsString() , event.getOption("simps").getAsString() 
							, event.getOption("ships").getAsString() , event.getOption("kins").getAsString() , event.getOption("waifu").getAsString() ,event.getOption("favorite").getAsString() 
							, event.getOption("guess").getAsString() , event.getOption("collect").getAsString());
				
					event.getHook().sendMessage("<@"+ userId +"> OC successfully inserted!" ).queue(); 
				}
				catch (Exception e) 
				{
					event.getHook().sendMessage("<@" + userId+ "> " + "unable to add original character make sure each option is filled correctly!" ).queue(); 
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
						event.getHook().sendMessage("<@" + userId+ ">" + " OC succesfully removed!").queue();
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
			if(!Helper.checkRoles(event.getMember().getRoles())) 
			{
				event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only Helluva Admins can use that command!").queue();				
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
			else if(Helper.checkRoles(event.getMember().getRoles())) // Not empty
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
				event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only Helluva Admins can use that command!").queue();			}
			}
			break;  
		case "my-oc" : 
		{
		
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
		}
		
	}
	

}
