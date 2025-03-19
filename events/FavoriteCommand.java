package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import CharactersPack.Character;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class FavoriteCommand extends ListenerAdapter{

	

	private ExecutorService executor = null; 
	public FavoriteCommand(ExecutorService executor ) 
	{
		this.executor = executor; 
	}

	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		switch (event.getName()) 
		{
		
		case "add-favorite" :
		{
			// task add character to favorites
			this.executor.submit( () -> 
			{
				String characterName = event.getOptions().get(0).getAsString();
				try 
				{ 
					event.deferReply().queue(); 
					Long userId = event.getUser().getIdLong(); 
					Long serverId = event.getGuild().getIdLong(); 
					CharacterSelection select = new CharacterSelection(); 
					// check if list already exists
					if(!select.checkFavLimit(userId, serverId)) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you reached the 10 character limit!").queue(); 
					} 
					// Now insert the character 
					select.insertFavorite(characterName, userId, serverId);
					event.getHook().sendMessage(event.getUser().getAsMention() + " character " + MarkdownUtil.bold(characterName) + " succesfully entered!").queue();
				} 
				catch(Exception ex) 
				{
					event.getHook().sendMessage( event.getUser().getAsMention() + " falied to insert character" + MarkdownUtil.bold(MarkdownUtil.bold(characterName)) + "!" ).queue(); 
				}
			});
			break;
		} 
		case "clear-favorites" : 
		{
			this.executor.submit( () -> 
			{
				event.deferReply().queue(); 
				// delete list from the database
				CharacterSelection select = new CharacterSelection();
				Long  userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				try 
				{
					// check if they have favorites list
					if(select.checkFavList(userId, serverId))
					{
						select.removeFavList(userId, serverId);
						event.getHook().sendMessage("<@" + userId + "> favorites list deleted!").queue(); 
					}
					else
					{
						event.getHook().sendMessage("<@" + userId + "> you do not have a favorites list!").queue();  
					}
				}
				catch(Exception ex) 
				{
					event.getHook().sendMessage("Something went wrong!").queue(); 

				}
			}); 
			
			break; 
		}
		
		case "remove-favorite": 
		{
			this.executor.submit(() -> 
			{
				try 
				{
					event.deferReply().queue(); 
					String characterName = event.getOptions().get(0).getAsString(); 
					CharacterSelection select = new CharacterSelection();
					select.removeFavCharacter(characterName, event.getUser().getIdLong(), event.getGuild().getIdLong());
					event.getHook().sendMessage( event.getUser().getAsMention() + " " + MarkdownUtil.bold(characterName) + " has been removed from your list!" ).queue(); 
				}
				catch(Exception ex)
				{
					event.getHook().sendMessage("Something went wrong!").queue();
				}
			}); 
			break;
		}
		
		case "favorites" : 
		{
			this.executor.submit(() -> 
			{
				try 
				{
					event.deferReply().queue(); 
					// Get the list from the table 
					String title = null;
					CharacterSelection select = new CharacterSelection();
					Long userId = event.getUser().getIdLong(); 
					Long serverId = event.getGuild().getIdLong(); 
					ArrayList<String> list = null; 
					
					if(event.getOptions().isEmpty())
					{ 
					// Check if list exists 
						if(!select.checkFavList(userId, serverId)) 
						{
							event.getHook().sendMessage("<@" + userId + "> you do not have a favorites list!").queue(); 
							return;
						} 
					
					
						title = select.getTitleList(userId, serverId); 
						list = select.getFavListNames(userId,serverId);
						
						// Now send Embed 
						EmbedBuilder builder = new EmbedBuilder(); 
						Character chtr = 
								select.requestSingleCharacter(list.get(0), event.getGuild().getIdLong(), GAMETYPE.FAVORITES, SETUPTYPE.LIGHT); 
						builder.setAuthor(title, event.getMember().getEffectiveAvatarUrl(),event.getMember().getEffectiveAvatarUrl()); 
						builder.setThumbnail(chtr.getDefaultImage());   
						// build a string 
						String res = "";
						int count = 1;
						for(String characters : list) 
						{
							res += count + "." + characters + "\n";
							count++; 
						}
						builder.setDescription(res); 
						builder.setColor(Color.ORANGE); 
						builder.setFooter(chtr.getCreditStr()); 
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
						list = select.getFavListNames(targetId,serverId);
						
						// Now send Embed 
						EmbedBuilder builder = new EmbedBuilder(); 
						Character chtr = 
								select.requestSingleCharacter(list.get(0), event.getGuild().getIdLong(), GAMETYPE.FAVORITES, SETUPTYPE.LIGHT); 
						builder.setAuthor(title, event.getOptions().get(0).getAsUser().getEffectiveAvatarUrl(),event.getOptions().get(0).getAsMember().getEffectiveAvatarUrl()); 
						builder.setThumbnail(chtr.getDefaultImage());   
						// build a string 
						String res = "";
						int count = 1;
						for(String characters : list) 
						{
							res += count + "." + characters + "\n";
							count++; 
						}
						builder.setDescription(res); 
						builder.setColor(Color.ORANGE); 
						event.getHook().sendMessageEmbeds(builder.build()).queue();
					}
					
				}
				catch(Exception ex) 
				{
					event.getHook().sendMessage("Something went wrong!").queue();
				}
			}); 
			break; 
		}
		
		case "change-favorites-title" : 
		{	
			this.executor.submit( () ->
			{ 
				event.deferReply().queue(); 
				String title = event.getOption("title").getAsString();
				CharacterSelection select = new CharacterSelection();
				try 
				{
					if ( !select.checkFavList(event.getUser().getIdLong(), event.getGuild().getIdLong()) ) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you do not have a favorites list to change a title for!").queue();
						return; 
					}
					select.changeFavTitle(title,event.getUser().getIdLong(), event.getGuild().getIdLong());
					event.getHook().sendMessage(event.getUser().getAsMention() + " favorites title changed to " + MarkdownUtil.bold(title) + "!").queue(); 
				} 
				catch (Exception e) 
				{
					event.getHook().sendMessage("Something went wrong!").queue();
				}
			}); 
			break; 
		}
		case "swap-favorite-rank" :
			{
				this.executor.submit(() -> 
				{
					event.deferReply().queue(); 
					String characterOne = event.getOption("first-character").getAsString(); 
					String characterTwo = event.getOption("second-character").getAsString();
					CharacterSelection select = new CharacterSelection(); 
					try 
					{
						select.favSwapCharacter(characterOne, characterTwo, event.getUser().getIdLong(),event.getGuild().getIdLong());
						event.getHook().sendMessage( event.getUser().getAsMention() + " " + MarkdownUtil.bold(characterOne) 
						+ " has been swapped with " + MarkdownUtil.bold(characterTwo) + "!").queue(); 
					} 
					catch (Exception ex)
					{
						event.getHook().sendMessage(ex.getMessage()).queue(); 
					} 					
				}); 
			}
			break; 
			
		}
	}
}
