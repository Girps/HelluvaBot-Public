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

	private ExecutorService executor = null; 
	public KinsCommand(ExecutorService executor ) {
		// TODO Auto-generated constructor stub
		 this.executor = executor; 
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		// Check valid command 
		if(event.getName().equals("kins")) 
		{
			
		// submit task 
		 this.executor.submit( () -> 
		 {
			 
			 event.deferReply().queue(); 
			 CharacterSelection select = new CharacterSelection(); 
			 Character found = null; 
			 
			 	try
			 	{
			 		found = select.getRandomCharacters(GAMETYPE.KINS, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 1)[0];
			 		String name = found.getName(); 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setTitle(found.getName()); 
					builder.setThumbnail(found.getDefaultImage());
					builder.setColor(Color.CYAN);
					builder.setFooter(found.getCreditStr()); 
					event.getHook().sendMessageEmbeds(builder.build()).queue( (message) -> 
					{
						message.reply( event.getUser().getAsMention() + " kins for " +  MarkdownUtil.bold(name + "!")).queue();
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
