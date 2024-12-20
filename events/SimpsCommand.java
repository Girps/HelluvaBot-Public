package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import CharactersPack.Character;
import java.util.concurrent.*;

public class SimpsCommand extends ListenerAdapter{

	

	
	public SimpsCommand( ) 
	{
		
	}
	
	 
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{

		
		// 	Check if bot
		if(event.getUser().isBot()) {return;}
		
		// Check valid command 
		if(event.getName().equals("simps")) 
		{
				event.deferReply().queue( (v) -> 
				{
					Instant start = Instant.now();  
					
					CompletableFuture.supplyAsync( () ->
						{
							CharacterSelection select = new CharacterSelection(); 
							Character found = null; 
							try 
							{
								found = select.getRandomCharacters(GAMETYPE.SIMPS,SETUPTYPE.LIGHT, event.getGuild().getIdLong(),3)[0];
							} catch (Exception e)
							{
								// TODO Auto-generated catch block
								throw new CompletionException(e); 
							}
							return found; 
						}).thenAccept( characterFound -> 
						{
							EmbedBuilder builder = new EmbedBuilder().
							setTitle(characterFound.getName()).
							setThumbnail(characterFound.getDefaultImage()).
							setColor(Color.PINK);
							event.getHook().sendMessageEmbeds(builder.build()).queue();
							event.getHook().sendMessage( event.getUser().getAsMention() + " simps for " + MarkdownUtil.bold(characterFound.getName()) + "!").queue();
						}).exceptionally(ex -> 
						{
							System.out.println(ex.getMessage()); 
							event.getHook().sendMessage(ex.getMessage()).queue(); 
							return null; 
						}).join(); 
					 
					 Instant end = Instant.now(); 
					 System.out.println(Duration.between(start, end)); 
				} );
		}
	}
	
}
