package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import CharactersPack.Character;
import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class ListCommand extends ListenerAdapter{

	
	protected static Connection conn;
	
	public ListCommand( Connection arg_Conn ) 
	{
		
		conn = arg_Conn; 
	}

	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		event.deferReply().queue();
		
		switch (event.getName()) 
		{
		case "add-favorite" :
		{	
			// Now add characters into the list 
			Long userId = event.getUser().getIdLong(); 
			Long serverId = event.getGuild().getIdLong(); 
			String characterName = event.getOptions().get(0).getAsString(); 
			
			CharacterSelection select = new CharacterSelection(conn); 
				try {
					// check if list already exists
					if(!select.checkFavLimit(userId, serverId)) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you reached the 10 character limit!").queue(); 
						return;
					} 
					// Now insert the character 
					select.insertFavorite(characterName, userId, serverId);
					event.getHook().sendMessage(event.getUser().getAsMention() + " character " + MarkdownUtil.bold(characterName) + " succesfully entered!").queue();
				}
				catch (SQLException e) 
				{
						// TODO Auto-generated catch block
						e.printStackTrace();
						event.getHook().sendMessage("<@"+ userId + ">" + " falied to insert character" + MarkdownUtil.bold(MarkdownUtil.bold(characterName)) + "!" ).queue();; 
				} 
				break;
			
		}
		case "clear-favorites" : 
		{
			// delete list from the database
			CharacterSelection select = new CharacterSelection(conn);
			Long  userId = event.getUser().getIdLong(); 
			Long serverId = event.getGuild().getIdLong(); 
			try 
			{
				if(select.checkFavList(userId, serverId))
				{
					select.removeFavList(userId, serverId);
					event.getHook().sendMessage("<@" + userId + "> favorites list deleted!").queue(); 
				}
				else
				{
					event.getHook().sendMessage("<@" + userId + "> you do not have a favorites list!").queue(); 
					return; 
				}
			} 
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return; 
			}
		
			break; 
		}
		case "remove-favorite": 
		{
			String characterName = event.getOptions().get(0).getAsString(); 
			CharacterSelection select = new CharacterSelection(conn);
			try 
			{
				// Check if list exists 
				if(!select.checkFavList(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
				{
					event.getHook().sendMessage( event.getUser().getAsMention() + " you do not have a favorites list!").queue(); 
					return;
				} 
				
				select.removeFavCharacter(characterName, event.getUser().getIdLong(), event.getGuild().getIdLong());
				event.getHook().sendMessage( event.getUser().getAsMention() + " " + MarkdownUtil.bold(characterName) + " has been removed from your list!" ).queue(); 
			} 
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getHook().sendMessage("Something went wrong!").queue();
			}
			catch(Exception e) 
			{
				
				e.printStackTrace();
				event.getHook().sendMessage("Something went wrong!").queue();
			}
			break;
		}
		case "favorites" : 
		{
			// Get the list from the table 
			String title = null;
			CharacterSelection select = new CharacterSelection(conn);
			Long userId = event.getUser().getIdLong(); 
			Long serverId = event.getGuild().getIdLong(); 
			ArrayList<CharactersPack.Character> list = null; 
			try
			{
				if(event.getOptions().isEmpty())
				{ 
				// Check if list exists 
					if(!select.checkFavList(userId, serverId)) 
					{
						event.getHook().sendMessage("<@" + userId + "> you do not have a favorites list!").queue(); 
						return;
					} 
				
				
					title = select.getTitleList(userId, serverId); 
					list = select.getFavoritesList(userId,serverId);
					
					// Now send Embed 
					EmbedBuilder builder = new EmbedBuilder(); 
					
					builder.setAuthor(title, event.getMember().getEffectiveAvatarUrl(),event.getMember().getEffectiveAvatarUrl()); 
					builder.setThumbnail(list.get(0).getDefaultImage());   
					// build a string 
					String res = "";
					int count = 1;
					for(Character characters : list) 
					{
						res += count + "." + characters.getName() + "\n";
						count++; 
					}
					builder.setDescription(res); 
					builder.setColor(Color.ORANGE); 
					event.getHook().sendMessageEmbeds(builder.build()).queue();
				
				} 
				else	// Now  
				{
					Long targetId = event.getOptions().get(0).getAsUser().getIdLong(); 
					
					// Check if targeted user has a favorites list 
					if(!select.checkFavList(targetId , serverId)) 
					{
						event.getHook().sendMessage( "<@"+ userId +"> "+ "user" + " <@" + targetId + "> does not have a favorites list!").queue(); 
						return;
					} 
					
					title = select.getTitleList(targetId, serverId); 
					list = select.getFavoritesList(targetId,serverId);
					
					// Now send Embed 
					EmbedBuilder builder = new EmbedBuilder(); 
					
					builder.setAuthor(title, event.getOptions().get(0).getAsUser().getEffectiveAvatarUrl(),event.getOptions().get(0).getAsMember().getEffectiveAvatarUrl()); 
					builder.setThumbnail(list.get(0).getDefaultImage());   
					// build a string 
					String res = "";
					int count = 1;
					for(Character characters : list) 
					{
						res += count + "." + characters.getName() + "\n";
						count++; 
					}
					builder.setDescription(res); 
					builder.setColor(Color.ORANGE); 
					event.getHook().sendMessageEmbeds(builder.build()).queue();
				}
			} catch (SQLException e)
			{
				// TODO Auto-generated catch block
				event.getHook().sendMessage("something went wrong!").queue();
				e.printStackTrace();
				return; 
			}
			
		
			break; 
		}
		case "change-favorites-title" : 
		{
			
			String title = event.getOption("title").getAsString(); 
			
			CharacterSelection select = new CharacterSelection(conn);
		
			try 
			{
				if ( !select.checkFavList(event.getUser().getIdLong(), event.getGuild().getIdLong()) ) 
				{
					event.getHook().sendMessage(event.getUser().getAsMention() + " you do not have a favorites list to change a title for!").queue();
					return; 
				}
				select.changeFavTitle(title,event.getUser().getIdLong(), event.getGuild().getIdLong());
				event.getHook().sendMessage(event.getUser().getAsMention() + " favorites title changed to " + MarkdownUtil.bold(title) + "!").queue(); 
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				event.getHook().sendMessage("Something went wrong!").queue(); 
				e.printStackTrace();
				return; 
			}
			
			break; 
		}
		case "swap-favorite-rank" :
			{
			String characterOne = event.getOption("first-character").getAsString(); 
			String characterTwo = event.getOption("second-character").getAsString(); 
			
			CharacterSelection select = new CharacterSelection(conn); 
			
				try 
				{
					select.favSwapCharacter(characterOne, characterTwo, event.getUser().getIdLong(),event.getGuild().getIdLong());
					event.getHook().sendMessage( event.getUser().getAsMention() + " " + MarkdownUtil.bold(characterOne) 
					+ " has been swapped with " + MarkdownUtil.bold(characterTwo) + "!").queue(); 
				} 
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue(); 
				} 
			
			}	
			break; 
		}
	}
	
	
}
