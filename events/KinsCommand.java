package events;

import java.awt.Color;
import CharactersPack.Character;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import java.util.concurrent.*;

public class KinsCommand extends ListenerAdapter{

	
	public KinsCommand( ) {
		// TODO Auto-generated constructor stub
		 
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		
		
		// 	Check if bot
		if(event.getUser().isBot()) {return;}
		
		// Check valid command 
		if(event.getName().equals("kins")) 
		{
			
			// Aync get the character and send it to the channel
		  event.deferReply().queue( v -> 
		  {
			  
			  CompletableFuture.supplyAsync( () -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					Character found = null;
					try
					{
						found = select.getRandomCharacters(GAMETYPE.KINS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 1)[0];
					}
					catch (Exception e) 
					{
						throw new CompletionException(e); 
					}  
					return found; 
				}).thenAccept( (characterFound) -> 
				{
					String name = characterFound.getName(); 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(characterFound.getName()); 
					builder.setThumbnail(characterFound.getDefaultImage());
					builder.setColor(Color.CYAN);
					event.getHook().sendMessageEmbeds(builder.build()).queue( (message) -> 
					{
						message.reply( event.getUser().getAsMention() + " kins for " +  MarkdownUtil.bold(name + "!")).queue();
					}); 
				}).exceptionally(ex -> 
				{
					event.getHook().sendMessage(ex.getMessage()).queue(); 
					return null; 
				}); 
			  
		  }); 
		 
		}
	}
}
