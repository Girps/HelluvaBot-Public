package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.SELECTIONTYPE;
import CharactersPack.Character;

import MiscClasses.KdmRound;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class KdmCommand extends ListenerAdapter
{
	private static String prefix = "$"; 
	private Connection conn; 
	private EventWaiter waiter; 
	public KdmCommand(String arg_Pre , Connection arg_Conn, EventWaiter arg_Waiter)
	{
		prefix = arg_Pre; 
		conn = arg_Conn; 
		waiter = arg_Waiter; 
	}
	
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{

		
		// Check length is one get 3 random characters and check options for show name 
		if(event.getName().equals("kdm") ) 
		{
			
			// Get info
			TextChannel  txtChan = event.getChannel().asTextChannel(); 
			CharacterSelection select = new CharacterSelection(conn); 
			String userId = event.getUser().getId(); 
			Long userLong = event.getUser().getIdLong(); 
			try 
			{
				Character[] chtrs = null; 
				

				if(event.getOption("first") == null && event.getOption("second") == null && event.getOption("third") == null) 
				{
					chtrs = select.getRandomCharacters(SELECTIONTYPE.ADULT,3);
				}
				else
				{
					  
					
					chtrs = new Character[3]; 
					
					// check each 
					if ( event.getOption("first") != null)
					{
						chtrs[0] = select.requestSingleCharacter(event.getOption("first").getAsString(), SELECTIONTYPE.ADULT); 
					}
					else 
					{
						chtrs[0] = select.getRandomCharacter(SELECTIONTYPE.ADULT); 
					}
					
					if(event.getOption("second") != null) 
					{
						chtrs[1] = select.requestSingleCharacter(event.getOption("second").getAsString(), SELECTIONTYPE.ADULT); 
					}
					else 
					{
						chtrs[1] = select.getRandomCharacter(SELECTIONTYPE.ADULT); 
					}
					
					if(event.getOption("third") != null) 
					{
						chtrs[2] = select.requestSingleCharacter(event.getOption("third").getAsString(), SELECTIONTYPE.ADULT); 
					}
					else 
					{
						chtrs[2] = select.getRandomCharacter(SELECTIONTYPE.ADULT);
					}
					
					// Check any invalid character 
					if(chtrs[0] == null || chtrs[1] == null|| chtrs[2] == null) 
					{
						event.deferReply().queue();
						event.getHook().sendMessage("character not in the game!").queue();
						return; 
					}
					
				}
				
				
				
				// set up emebeds and send each one
				EmbedBuilder builderOne = new EmbedBuilder(); 
				builderOne.setTitle(chtrs[0].getName()); 
				builderOne.setColor(Color.RED); 
				builderOne.setThumbnail(chtrs[0].getDefaultImage()); 
				builderOne.setDescription(MarkdownUtil.bold("Player : ") + "<@" + userId + ">"); 
				
				EmbedBuilder builderTwo = new EmbedBuilder(); 
				builderTwo.setTitle(chtrs[1].getName()); 
				builderTwo.setColor(Color.RED); 
				builderTwo.setThumbnail(chtrs[1].getDefaultImage()); 
				builderTwo.setDescription(MarkdownUtil.bold("Player : ") +"<@" + userId + ">"); 
				
				EmbedBuilder builderThree = new EmbedBuilder(); 
				builderThree.setTitle(chtrs[2].getName()); 
				builderThree.setColor(Color.RED); 
				builderThree.setThumbnail(chtrs[2].getDefaultImage()); 
				builderThree.setDescription(MarkdownUtil.bold("Player : ") +"<@" + userId + ">"); 
				
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(Button.danger("kill", "Kill")); 
				buttons.add(Button.primary("date", "Date"));
				buttons.add(Button.success("marry", "Marry")); 
				
			
				event.deferReply().queue();
				event.getHook().sendMessageEmbeds(builderOne.build()).setActionRow(buttons).queue( (e) -> 
				{	
					// Get the message ids 
					ArrayList<Long> messageIds = new ArrayList<Long>(); 
					
					// Create the kdm object push it to a hashmap  
					KdmRound currentRound = new KdmRound( userId , e.getIdLong()); 
					HashMap<Long, KdmRound> map = new HashMap<Long,KdmRound >(); 
					
					
					
					messageIds.add(e.getIdLong());  
					
					
					
					
					
					txtChan.sendMessageEmbeds(builderTwo.build()).setActionRow(buttons).queue( (msg2) -> 
					{
						messageIds.add(msg2.getIdLong());}
					);  
					
					txtChan.sendMessageEmbeds(builderThree.build()).setActionRow(buttons).queue( (msg2) -> 
					{ messageIds.add(msg2.getIdLong()); }
					
					); 
					
					MessageCreateBuilder msgBuilderTwo = new MessageCreateBuilder();
					msgBuilderTwo.setActionRow(buttons); 
					msgBuilderTwo.build(); 
					currentRound.setArrayList(messageIds);
					map.put(userLong, currentRound); 
				
					
					// Now use waiter
					this.waiter.waitForEvent(ButtonInteractionEvent.class, 
							(eBtn) ->
						{
							// Condition if valid, same author not a bot and one of the messages have been interacted with 
							if(!eBtn.getUser().isBot() && event.getUser().getId().equalsIgnoreCase(eBtn.getUser().getId()) &&
									(eBtn.getMessage().getIdLong() == messageIds.get(0) || eBtn.getMessage().getIdLong() == messageIds.get(1)
									|| eBtn.getMessage().getIdLong() == messageIds.get(2) ) ) 
							{
								eBtn.deferEdit().queue();  
								MessageEmbed original = eBtn.getMessage().getEmbeds().get(0);
									eBtn.getMessage().editMessageEmbeds(original).setActionRow(buttons.get(0).asDisabled(), 
											buttons.get(1).asDisabled(), buttons.get(2).asDisabled()).queue();     
									
									KdmRound temp = map.get(eBtn.getUser().getIdLong()); 
									temp.setState(eBtn.getButton().getLabel(), eBtn.getMessage().getEmbeds().get(0).getTitle(),  eBtn.getMessage().getIdLong(), e,eBtn);
									
											
									return temp.isOver(); 
								
								 
							} // Not in this game event 
							else 
							{
								eBtn.deferEdit().queue(); 
								return false;
							}
						}
						, (eBtn) -> 
						{ 
							KdmRound temp = map.get(eBtn.getUser().getIdLong()); 
							eBtn.getChannel().asTextChannel().sendMessage(temp.toString()).queue();  
							map.remove(temp.getGameId()); 
						}
						, 5L,TimeUnit.MINUTES, () -> 
						{
								// Now delete the messages part of this game! 
								txtChan.sendMessage("<@" + userId+"> " + "your KDM session expired!" ).queue(); 
								map.remove(e.getId()); 
								e.delete().queue();
								e.getChannel().retrieveMessageById(messageIds.get(1)).queue( (dMsg2) -> dMsg2.delete().queue());
								e.getChannel().retrieveMessageById(messageIds.get(2)).queue( (dMsg3) -> dMsg3.delete().queue()); 
								
						}); 
				});
				
				
			} 
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.deferReply().queue();
				event.getHook().sendMessage("KDM command failed!"); 
			}
		}
				
	}
	
}
