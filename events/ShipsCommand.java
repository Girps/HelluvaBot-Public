package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;

import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import CharactersPack.Character;

public class ShipsCommand extends ListenerAdapter{
	
	private ExecutorService executor = null; 

	public ShipsCommand(ExecutorService executor )
	{
		this.executor = executor; 
	}
	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		if(event.getName().equals("ships")) 
		{
			this.executor.submit( () -> 
			{
				event.deferReply().queue(); 
				CharacterSelection select = new CharacterSelection(); 
				Character[] arr = null; 
				try 
				{
					arr = select.getRandomCharacters(GAMETYPE.SHIPS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 2); 
					 EmbedBuilder builderFirst = new EmbedBuilder()
							  .setTitle(arr[0].getName())
							  .setColor(Color.MAGENTA)
							  .setThumbnail(arr[0].getDefaultImage())
							  .setFooter(arr[0].getCreditStr());
					  
					  EmbedBuilder builderSecond = new EmbedBuilder()
							  .setTitle(arr[1].getName())
							  .setColor(Color.MAGENTA)
							  .setThumbnail(arr[1].getDefaultImage())
							  .setFooter(arr[1].getCreditStr());
					  
					  event.getHook().sendMessageEmbeds(builderFirst.build(),builderSecond.build()).queue( (message) -> 
					  {
						  message.reply( event.getUser().getAsMention() + " ships " + MarkdownUtil.bold( message.getEmbeds().get(0).getTitle()) +
								  " x " + MarkdownUtil.bold(message.getEmbeds().get(1).getTitle()) + "!" ).queue(); 
					  });
				}
				catch(Exception ex) 
				{
					event.getHook().sendMessage(ex.getMessage()).queue(); 
				}
			}); 	
		}
		
   }
}
