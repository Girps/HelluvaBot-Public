package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import CharactersPack.Character;

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
				
				String userName = event.getUser().getId();  
		
				try 
				{		
					CharacterSelection select = new CharacterSelection(); 
					Character found = select.getRandomCharacters(GAMETYPE.SIMPS,SETUPTYPE.LIGHT, event.getGuild().getIdLong(),3)[0];
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(found.getName()); 
					builder.setThumbnail(found.getDefaultImage());
					builder.setColor(Color.PINK);
					event.deferReply().queue();
					event.getHook().sendMessageEmbeds(builder.build()).queue();
					event.getHook().sendMessage( "<@"+ userName + ">" + " simps for " + MarkdownUtil.bold(found.getName()) + "!").queue();
				} 
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.deferReply().queue();
					event.getHook().sendMessage(e.getMessage()).queue();
				} 
		}
	}
	
}
