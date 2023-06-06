package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import CharactersPack.CharacterSelection;
import CharactersPack.SELECTIONTYPE;
import CharactersPack.Character;

public class SimpsCommand extends ListenerAdapter{

	protected static String prefix = "$"; 
	protected static Connection conn;
	
	public SimpsCommand(String arg_Pre, Connection arg_Conn) 
	{
		prefix = arg_Pre; 
		conn = arg_Conn; 
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
				CharacterSelection select = new CharacterSelection(conn); 
				try 
				{
					Character found = select.getRandomCharacter(SELECTIONTYPE.ADULT);
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(found.getName()); 
					builder.setThumbnail(found.getDefaultImage());
					builder.setColor(Color.red);
					event.deferReply().queue();
					event.getHook().sendMessageEmbeds(builder.build()).queue();
					event.getHook().sendMessage( "<@"+ userName + ">" + " simps for " + found.getName() + "!").queue();
				} 
				catch (SQLException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					event.deferReply().queue();
					event.getHook().sendMessage("simps command failed!").queue();
				} 
		}
	}
	
}
