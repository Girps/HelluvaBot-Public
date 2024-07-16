package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import CharactersPack.Character;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class FavoriteCommand extends ListenerAdapter{

	

	
	public FavoriteCommand( ) 
	{
		
	}

	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		switch (event.getName()) 
		{
		case "add-favorite" :
		{	
			
			CompletableFuture.runAsync(() -> 
			{
				event.deferReply(); 
				// Now add characters into the list 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				String characterName = event.getOptions().get(0).getAsString(); 
				CharacterSelection select = new CharacterSelection(); 
					try 
					{
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
					catch (Exception e) 
					{
						event.getHook().sendMessage( event.getUser().getAsMention() + " falied to insert character" + MarkdownUtil.bold(MarkdownUtil.bold(characterName)) + "!" ).queue();; 
					} 
			}).exceptionally(ex -> 
			{
				event.getHook().sendMessage(ex.getMessage()).queue(); 
				ex.printStackTrace(); 
				return null; 
			}); 
			
				break;
		} 
		
		case "clear-favorites" : 
		{
			
			CompletableFuture.runAsync(() -> 
			{
				event.deferReply().queue(); 
				// delete list from the database
				CharacterSelection select = new CharacterSelection();
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
				catch (Exception e) {
					// TODO Auto-generated catch block
					throw new CompletionException(e); 
				}
			}).exceptionally( (ex) -> 
			{
				event.getHook().sendMessage(ex.getMessage()).queue(); 
				ex.printStackTrace(); 
				return null;
			}); 
			
		
			break; 
		}
		
		case "remove-favorite": 
		{
			
			CompletableFuture.runAsync( () -> 
			{
				event.deferReply().queue(); 
				String characterName = event.getOptions().get(0).getAsString(); 
				CharacterSelection select = new CharacterSelection();
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
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue();
				}
			}).exceptionally( (ex) -> {
				ex.printStackTrace(); 
				return null; 
			});
			
			break;
		}
		
		case "favorites" : 
		{
			
			CompletableFuture.runAsync( () -> {
				event.deferReply().queue(); 
			// Get the list from the table 
			String title = null;
			CharacterSelection select = new CharacterSelection();
			Long userId = event.getUser().getIdLong(); 
			Long serverId = event.getGuild().getIdLong(); 
			ArrayList<String> list = null; 
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
					list = select.getFavListNames(userId,serverId);
					
					// Now send Embed 
					EmbedBuilder builder = new EmbedBuilder(); 
					
					builder.setAuthor(title, event.getMember().getEffectiveAvatarUrl(),event.getMember().getEffectiveAvatarUrl()); 
					builder.setThumbnail(select.requestSingleCharacter(list.get(0), event.getGuild().getIdLong(), GAMETYPE.FAVORITES, SETUPTYPE.LIGHT).getDefaultImage());   
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
					
					builder.setAuthor(title, event.getOptions().get(0).getAsUser().getEffectiveAvatarUrl(),event.getOptions().get(0).getAsMember().getEffectiveAvatarUrl()); 
					builder.setThumbnail(select.requestSingleCharacter(list.get(0), event.getGuild().getIdLong(), GAMETYPE.FAVORITES, SETUPTYPE.LIGHT).getDefaultImage());   
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
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				throw new CompletionException(e); 
			}}).exceptionally( (ex) -> 
			{
				event.getHook().sendMessage("something went wrong!").queue();
				ex.printStackTrace();
				return null; 
			}); 
			break; 
		}
		
		case "change-favorites-title" : 
		{
			
			CompletableFuture.runAsync( () -> { 
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
				// TODO Auto-generated catch block
				throw new CompletionException(e);
			}
			}).exceptionally( (ex) -> 
			{
				event.getHook().sendMessage("Something went wrong!").queue(); 
				ex.printStackTrace();
				return null; 
			}); 
			break; 
		}
		case "swap-favorite-rank" :
			{
				CompletableFuture.runAsync( () -> {
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
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						throw new CompletionException(e);  
					} 
				}).exceptionally((ex) -> {
					event.getHook().sendMessage(ex.getMessage()).queue(); 
					return null; 
				});
			}
			break; 
			
		}
	}
}
