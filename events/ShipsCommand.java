package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;

import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import CharactersPack.Character;

public class ShipsCommand extends ListenerAdapter{
	

	public ShipsCommand( )
	{
	}
	
	@Override
	public void  onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		// Check by bot
		if(event.getUser().isBot()) {return; }
		
		if(event.getName().equals("ships")) 
		{
		
			
			event.deferReply().queue( (v) -> 
			{
				 CompletableFuture.supplyAsync( () -> 
				  {
					  // get characters 
					  CharacterSelection select = new CharacterSelection(); 
					  Character[] arr = null; 
					  // get array of two characters 
					  try {
						arr = select.getRandomCharacters(GAMETYPE.SHIPS, SETUPTYPE.LIGHT,  event.getGuild().getIdLong(),2);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						throw new CompletionException(e); 
					}
					  return arr; 
				  }
				  ).thenAccept((characters) -> 
				  {
					  EmbedBuilder builderFirst = new EmbedBuilder()
							  .setTitle(characters[0].getName())
							  .setColor(Color.MAGENTA)
							  .setThumbnail(characters[0].getDefaultImage());
					  
					  EmbedBuilder builderSecond = new EmbedBuilder()
							  .setTitle(characters[1].getName())
							  .setColor(Color.MAGENTA)
							  .setThumbnail(characters[1].getDefaultImage());
					  
					  event.getHook().sendMessageEmbeds(builderFirst.build(),builderSecond.build()).queue( (message) -> 
					  {
						  message.reply( event.getUser().getAsMention() + " ships " + MarkdownUtil.bold( message.getEmbeds().get(0).getTitle()) + " x " + MarkdownUtil.bold(message.getEmbeds().get(1).getTitle()) + "!" ).queue(); 
					  }); 
					  
				  }).exceptionally(ex -> 
					{
						System.out.println(ex.getMessage()); 
						event.getHook().sendMessage(ex.getMessage()).queue(); 
						return null; 
					});
				
			});
		}
   }
}
