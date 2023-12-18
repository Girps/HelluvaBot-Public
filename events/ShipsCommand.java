package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
		
		event.deferReply().queue();
		
		Character[] arr = null;
		if(event.getName().equals("ships")) 
		{
			// Get two characters 
			try 
			{
				// Get caller 			
				CharacterSelection select = new CharacterSelection(); 

				String userid = event.getUser().getId();
				
				// Get array of 2 characters 
				arr = select.getRandomCharacters(GAMETYPE.SHIPS, SETUPTYPE.LIGHT,  event.getGuild().getIdLong(),2);
				
				Character One = arr[0]; 
				Character Two = arr[1];
				
				// Build embed 
				EmbedBuilder builderOne = new EmbedBuilder(); 
				builderOne.setTitle(One.getName()); 
				builderOne.setColor(Color.MAGENTA); 
				builderOne.setThumbnail(One.getDefaultImage());
				
				// Build embed 
				EmbedBuilder builderTwo = new EmbedBuilder(); 
				builderTwo.setTitle(Two.getName()); 
				builderTwo.setColor(Color.MAGENTA); 
				builderTwo.setThumbnail(Two.getDefaultImage());
				
				
				event.getHook().sendMessageEmbeds(builderOne.build(),builderTwo.build()).queue();
				event.getHook().sendMessage("<@" + userid + ">" + " ships " + MarkdownUtil.bold(One.getName()) + " x " + MarkdownUtil.bold(Two.getName()) + "!").queue();
				
			} 
			catch(Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getHook().sendMessage(e.getMessage()).queue();
			}
		}
		
	}
}
