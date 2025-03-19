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

	
	private ExecutorService executor = null; 
	
	public SimpsCommand(ExecutorService ex ) 
	{
		executor = ex; 
	}
	
	 
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{

		
		// 	Check if bot
		if(event.getUser().isBot()) {return;}
		
		// Check valid command 
		if(event.getName().equals("simps")) 
		{
			executor.submit( ()-> 
			{
				event.deferReply().queue(); 
				CharacterSelection select = new CharacterSelection(); 
				Character CharacterFound = null; 
				try 
				{
					CharacterFound = select.getRandomCharacters(GAMETYPE.SIMPS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 1)[0]; 
				}
				catch(Exception e) 
				{
					event.getHook().sendMessage("Error with character " + CharacterFound.getName()).queue(); 
				}
				EmbedBuilder builder = new EmbedBuilder().
				setTitle(CharacterFound.getName()).
				setThumbnail(CharacterFound.getDefaultImage()).
				setColor(Color.PINK);
				event.getHook().sendMessageEmbeds(builder.build()).queue();
				event.getHook().sendMessage( event.getUser().getAsMention() + " simps for " + MarkdownUtil.bold(CharacterFound.getName()) + "!").queue();
			}); 
		}
	}
	
}
