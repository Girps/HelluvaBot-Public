package events;

import CharactersPack.Character;
import CharactersPack.CharacterSelection;
import CharactersPack.SELECTIONTYPE;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;



public class WikiCommand extends ListenerAdapter{

	private static String prefix = "$"; 
	private Connection conn; 
	
	public WikiCommand(String arg, Connection arg_Conn)
	{
		prefix = arg; 
		conn = arg_Conn; 
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
	{
		// Check if called by bot 
		if(event.getUser().isBot()) 
		{
			return; 
		}
		
		
		// Check valid command
		if(event.getName().equals("wiki")) 
		{
			// Get character name  
			String characterName = event.getOption("character").getAsString(); 
			Statement get; 
			OffsetDateTime time = event.getTimeCreated(); 
			Date date = Date.from(time.toInstant());
			// Now check in database for the character 
			try 
			{
					CharacterSelection select = new CharacterSelection(conn); 
				
				
					// We have the fields now create our character object 
					Character charactFound = select.requestSingleCharacter(characterName, SELECTIONTYPE.ADULT);
					
					// We have the character now build an embed for the character 
					EmbedBuilder builder = new EmbedBuilder(); 
					builder.setAuthor(charactFound.getName(), charactFound.getUrl());
					builder.setColor(Color.red); 
					builder.setDescription(charactFound.getBasic()); 
					builder.addField("Quote", MarkdownUtil.quote(charactFound.getQuote()),false);
					builder.setThumbnail(charactFound.getDefaultImage()); 
					builder.setFooter("Time: " + date.toString(), event.getGuild().getIconUrl());  
					// Now send the embed to the server
					// Now send the embed to the server
					event.deferReply().queue(); 
					event.getHook().sendMessageEmbeds(builder.build()).queue();
				
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.reply(characterName + " not found " + "<:smolas_crying:1111057782473506848>").queue();
			} 
		}
		else if(event.getName().equals("wikifull"))	// wiki full pagnation implementation 
		{
			// Get character name  
						String characterName = event.getOption("character").getAsString();
						 
						// Now check in database for the character 
						try 
						{
								CharacterSelection select = new CharacterSelection(conn); 
								
								
								// We have the fields now create our character object 
								Character charactFound = select.requestSingleCharacter(characterName, SELECTIONTYPE.ALL);
								
								// We have the character now build an embed for the character 
								EmbedBuilder builder = new EmbedBuilder(); 
								builder.setColor(Color.red); 
								builder.setAuthor(charactFound.getName(), charactFound.getUrl());
								builder.setImage(charactFound.getDefaultImage());  
								builder.setDescription(charactFound.getBasic()); 
								builder.addField("Quote",  MarkdownUtil.quote(charactFound.getQuote()),false);
								builder.setFooter("Image: " + "1/" + (charactFound.getImageList().size() + "\nWiki: 1/14") , event.getGuild().getIconUrl()); 
								
								// Instantiate list of buttons 
								List<Button> buttons = new ArrayList<Button>();
								buttons.add(Button.secondary("leftwiki", "<<")); 
								buttons.add(Button.primary("left", "<"));
								buttons.add(Button.danger("close", "Close")); 
								buttons.add(Button.primary("right", ">")); 
								buttons.add(Button.secondary("rightwiki", ">>")); 
								

								// Now send the embed to the server
								event.deferReply().queue(); 
								event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue();
								
							
						}
						catch (SQLException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							event.reply(characterName + " not found " + "<:smolas_crying:1111057782473506848>").queue();
						} 
			
		}
		
	}
	
	/* Deal with buttons clicked on this event */ 
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) 
	{
		// Implement pagination on embeds called by wikifull command 
		if( event.getMember().getUser().isBot()) 
		{
				// if called by bot return
			return; 
		}
		
		// Now get title which should hold characters name use it
	
		MessageEmbed old = event.getMessage().getEmbeds().get(0); 
		EmbedBuilder oldBuild = new EmbedBuilder(old); 
		String characterName = old.getAuthor().getName(); // get the name 
		
		// Get page location in the footer 
		String footerData = event.getMessage().getEmbeds().get(0).getFooter().getText();  
		
		// Get number of image 
		String imageNumberStr = footerData.split("\n")[0]; 
		imageNumberStr = footerData.split("/")[0]; 
		imageNumberStr = imageNumberStr.replace("Image: ",""); 
		int pageNumber = Integer.valueOf(imageNumberStr) - 1 ; 
		
		// Get number of wiki 
		String wikiNumberStr = footerData.split("\n")[1]; 
		wikiNumberStr = wikiNumberStr.split("/")[0]; 
		wikiNumberStr = wikiNumberStr.replace("Wiki: ", ""); 
		int wikiNumber = Integer.valueOf(wikiNumberStr); 
		
		// Now query the character from Database and instantitate  it
		String[] fields = null; 
		// Select character with name ignore case 
		String query = "SELECT * FROM characters " + 
				"WHERE LOWER(name) = "  + "LOWER(" + "\"" + characterName  + "\""+ ")" + 
				"LIMIT 1";
		
		Statement get = null;
		try
		{
			get = conn.createStatement();
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResultSet data = null;
		// Now execute 
		try 
		{
			data = get.executeQuery(query);
			
			data.next(); 
			// Get the row data  
			int column  = data.getMetaData().getColumnCount(); 
			 fields  = new String[column+1];
			do 
			{
				for(int i =1; i < column; ++i) 
				{
					fields[i] = data.getString(i); 
				}
			}
			while(data.next()); 
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		// Create the character 
		Character charcTarget = new Character(Integer.valueOf(fields[1]),fields[2]); 
		// Size of the array  
		int arraySize = charcTarget.getImageList().size() - 1; 
		
		// Get the button name called 
		String dir_Str = event.getButton().getId(); 
		
		// Check which buttons were clicked 
		if(dir_Str.equalsIgnoreCase("left")) 
		{
			pageNumber--; 
			// Check if negative or above the bounds
			if(pageNumber < 0) 
			{
				// reset it to arraySize 
				pageNumber = arraySize; 
				
			}
		} // Right decrement number 
		else if(dir_Str.equalsIgnoreCase("right"))
		{
			pageNumber++; 
			 if(pageNumber > arraySize) // if above the size set it 0
			{
				pageNumber = 0; 
			}
		}
		else if(dir_Str.equalsIgnoreCase("close")) // Delete the message 
		{
			event.deferEdit().queue(); 
			event.getMessage().delete().queue(); 
		}
		else if(dir_Str.equalsIgnoreCase("leftwiki")) 
		{
			// Decremenit it 
			wikiNumber--; 
			if(wikiNumber < 1) 
			{
				wikiNumber = 14; 
			}
			
		}
		else // deals with right wiki 
		{
			wikiNumber++; 
			if(wikiNumber > 14) 
			{
				wikiNumber = 1; 
			}
		}

		
		
		// We have a proper index now edit the embed
		tweakEmbedWiki(oldBuild, charcTarget ,wikiNumber); 
		
		oldBuild.setImage(charcTarget.getImageList().get(pageNumber)); 
		oldBuild.setFooter("Image: " + (pageNumber + 1) + "/" + (charcTarget.getImageList().size()) + 
				"\nWiki: " + wikiNumber + "/14",event.getGuild().getIconUrl()); 
		event.deferEdit().queue();
		event.getMessage().editMessageEmbeds(oldBuild.build()).queue( );
	}
	
	// Helper function to help set up embed of the message 
	void tweakEmbedWiki(EmbedBuilder builder, Character charactFound, int wikiNumber) 
	{
		builder.clearFields(); 
		switch(wikiNumber)
		{
		case(1):
			builder.setDescription(charactFound.getBasic()); 
			builder.addField("Quote", MarkdownUtil.quote(charactFound.getQuote()),false);
			break;
		case(2):
			builder.addField(MarkdownUtil.underline("Nicknames"),charactFound.getNickNames(),true);  
			break; 
		case(3):
			builder.addField(MarkdownUtil.underline("Likes"),charactFound.getLikes(),true);
			break; 
		case(4):
			builder.addField(MarkdownUtil.underline("Dislikes"),charactFound.getDisLikes(),true); 
			break; 
		case(5):
			builder.addField(MarkdownUtil.underline("Species"), charactFound.getSpecies(), true);
			break;
		case(6):
			builder.addField(MarkdownUtil.underline("Abilities"), charactFound.getAbilities(),true);
			break; 
		case(7):
			builder.addField(MarkdownUtil.underline("Status"),charactFound.getStatus(),true); 
			break; 
		case(8):
			builder.addField(MarkdownUtil.underline("Occupation"), charactFound.getOccup(),true);
			break; 
		case(9):
			builder.addField(MarkdownUtil.underline("Family"), charactFound.getFamily(),true);
			break; 
		case(10):
			builder.addField(MarkdownUtil.underline("Friends"), charactFound.getFriends(),true);
			break; 
		case(11):
			builder.addField(MarkdownUtil.underline("Romantic Interests"), charactFound.getRomance(),true);
			break; 
		case(12):
			builder.addField(MarkdownUtil.underline("Enemies"),charactFound.getEnemies(),true); 
			break; 	
		case(13):
			builder.addField(MarkdownUtil.underline("Others"),charactFound.getOthers(),true); 
		break;
		case(14):
			builder.addField(MarkdownUtil.underline("Voice actor"), charactFound.getVoiceActors(),true);
			break; 
		}
	}
	
}
