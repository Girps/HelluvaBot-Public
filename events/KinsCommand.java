package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;

import CharactersPack.Character;
import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;

import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;


public class KinsCommand extends SimpsCommand{

	public KinsCommand( Connection arg_Conn) {
		super(arg_Conn);
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
				String userName = event.getUser().getId();  
				CharacterSelection select = new CharacterSelection(conn); 
				try 
				{
					Character found = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT,1)[0];
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(found.getName()); 
					builder.setThumbnail(found.getDefaultImage());
					builder.setColor(Color.red); 
					event.deferReply().queue();
					event.getHook().sendMessageEmbeds(builder.build()).queue();
					event.getHook().sendMessage( "<@"+ userName + ">" + " kins " + MarkdownUtil.bold(found.getName()) + "!").queue();
				} 
				catch (SQLException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.deferReply().queue(); 
					event.getHook().sendMessage("Simps command failed!").queue();
				} 
		}
	}
}
