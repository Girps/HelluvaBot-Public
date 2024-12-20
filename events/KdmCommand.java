package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import CharactersPack.Character;

import MiscClasses.KdmRound;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.concurrent.DelayedCompletableFuture;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class KdmCommand extends ListenerAdapter
{
	private EventWaiter waiter; 
	public KdmCommand( EventWaiter arg_Waiter)
	{	
		waiter = arg_Waiter; 
	}
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		
		// Check length is one get 3 random characters and check options for show name 
		if(event.getName().equals("kdm") ) 
		{
			
			CompletableFuture.supplyAsync( () -> 
			{
				event.deferReply().queue();
				// get the characters 
				// No entry get 3 random characters
				Character[] chtrs = new Character[3]; 
				CharacterSelection select =  new CharacterSelection(); 
				if(event.getOption("first") == null && event.getOption("second") == null && event.getOption("third") == null) 
				{
					try 
					{
						chtrs = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),3);
					}
					catch (Exception e)
					{
						throw new CompletionException(e); 
					}
				}
				else
				{
					int size = event.getOptions().size();
					List<OptionMapping> options = event.getOptions(); 
					// Request by name a character
					for(int i = 0; i < size; ++i) 
					{
						try
						{
							chtrs[i] = select.requestSingleCharacter(options.get(i).getAsString(),  event.getGuild().getIdLong(), GAMETYPE.KDM, SETUPTYPE.LIGHT);
						} 
						catch (Exception e) 
						{
							// TODO Auto-generated catch block
							throw new CompletionException(e);
						} 
					}
					// length difference computation 
					int delta = chtrs.length - options.size(); 
					switch(delta) 
					{
					case(1): 
						
						try 
						{
							chtrs[2] = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT,  event.getGuild().getIdLong(),1)[0];
						} catch (Exception e) 
						{
							throw new CompletionException(e);
						}    
						break; 
					case(2): 
						Character[] temp = null;
						try 
						{
							temp = select.getRandomCharacters(GAMETYPE.KDM, SETUPTYPE.LIGHT, event.getGuild().getIdLong(),2);
						} 
						catch (Exception e)
						{
							// TODO Auto-generated catch block
							throw new CompletionException(e); 
						} 
					chtrs[2] = temp[1]; 
					chtrs[1] =temp[0]; 
						break; 
					default: 
						break; 
					}
				}
				return chtrs; 
			}
			).thenAccept( (chtrsArray) ->
			{
				// have characters now send embed and use event waiter
				Color darkRed = new Color(190,0,0); 
				// set up emebeds and send each one
				EmbedBuilder builderOne = new EmbedBuilder(); 
				builderOne.setTitle(chtrsArray[0].getName()); 
				builderOne.setColor(darkRed); 
				builderOne.setThumbnail(chtrsArray[0].getDefaultImage()); 
				builderOne.setDescription(MarkdownUtil.bold("Player : ") + event.getUser().getAsMention()); 
				
				EmbedBuilder builderTwo = new EmbedBuilder(); 
				builderTwo.setTitle(chtrsArray[1].getName()); 
				builderTwo.setColor(darkRed); 
				builderTwo.setThumbnail(chtrsArray[1].getDefaultImage()); 
				builderTwo.setDescription(MarkdownUtil.bold("Player : ") + event.getUser().getAsMention()); 
				
				EmbedBuilder builderThree = new EmbedBuilder(); 
				builderThree.setTitle(chtrsArray[2].getName()); 
				builderThree.setColor(darkRed); 
				builderThree.setThumbnail(chtrsArray[2].getDefaultImage()); 
				builderThree.setDescription(MarkdownUtil.bold("Player : ") + event.getUser().getAsMention()); 
				
				List<Button> buttons = new ArrayList<Button>();
				buttons.add(Button.danger("kill", "Kill")); 
				buttons.add(Button.primary("date", "Date"));
				buttons.add(Button.success("marry", "Marry")); 
				
		
				
				List<CompletableFuture<Message>> futures = List.of(
						event.getHook().sendMessageEmbeds(builderOne.build()).setActionRow(buttons).submit(),
						event.getChannel().sendMessageEmbeds(builderTwo.build()).setActionRow(buttons).submit(),  
						event.getChannel().sendMessageEmbeds(builderThree.build()).setActionRow(buttons).submit()  
				);
				
				CompletableFuture.allOf( futures.toArray(new CompletableFuture[3]))
						.thenAccept( v -> 
						{
							List<Message> myMsgs = futures.stream().map( CompletableFuture::join).toList();
							// have the messages now create the kdm round
							KdmRound currentRound = new KdmRound( event.getUser().getId() , event.getIdLong(),builderOne.build().getTitle(), builderTwo.build().getTitle(), builderThree.build().getTitle()); 
							
							ArrayList<Long> messageIds = new ArrayList<Long>();  
							for (int i =0; i< myMsgs.size(); ++i) 
							{
								messageIds.add(myMsgs.get(i).getIdLong()); 
							}
							currentRound.setArrayList(messageIds);
							this.waiter.waitForEvent(ButtonInteractionEvent.class, 
									(eBtn) -> 
								{
									System.out.println( "Thread in waiter eBtn: "+ Thread.currentThread().getName()); 
									// Condition if valid, same author not a bot and one of the messages have been interacted with 
									if(!eBtn.getUser().isBot() && event.getChannel().getIdLong() == eBtn.getChannel().getIdLong() && event.getUser().getId().equalsIgnoreCase(eBtn.getUser().getId()) &&
											(eBtn.getMessage().getIdLong() == messageIds.get(0) || eBtn.getMessage().getIdLong() == messageIds.get(1)
											|| eBtn.getMessage().getIdLong() == messageIds.get(2) ) ) 
									{
										eBtn.deferEdit().queue(); 
										// Disable entire selection
										MessageEmbed original = eBtn.getMessage().getEmbeds().get(0);
											eBtn.getMessage().editMessageEmbeds(original).setActionRow(buttons.get(0).asDisabled(), 
													buttons.get(1).asDisabled(), buttons.get(2).asDisabled()).queue();     
											
											KdmRound temp = currentRound; 
											try 
											{
												temp.setState(eBtn.getButton().getLabel(), eBtn.getMessage().getEmbeds().get(0).getTitle(),  eBtn.getMessage().getIdLong(),eBtn);
											} 
											catch (InterruptedException e1) {
												// TODO Auto-generated catch block
												throw new CompletionException(e1); 
											}
											catch (ExecutionException e1) {
												// TODO Auto-generated catch block
												throw new CompletionException(e1); 
											}	
											return temp.isOver(); 
									} // Not in this game event 
									else 
									{
										return false;
									}
								}
								, (eBtn) -> CompletableFuture.runAsync( () ->  
								{ 
									System.out.println("Game done"); 
									KdmRound temp = currentRound; 
									eBtn.getHook().sendMessage( "<@"+ temp.getUser() + ">" 
									+ " would " +
											MarkdownUtil.italics("kill ") +
											MarkdownUtil.bold(temp.getKill()) +
											MarkdownUtil.italics(" date ") + 
											MarkdownUtil.bold(temp.getDate()) +
											" and "+  MarkdownUtil.italics(" marry ") +
											MarkdownUtil.bold(temp.getMarry())).queue();
								})
								, 1L,TimeUnit.MINUTES, () -> 
								{
										// Delete unfinished game 
										event.getChannel().retrieveMessageById(messageIds.get(0)).queue( (dMsg2) -> dMsg2.delete().queue());
										event.getChannel().retrieveMessageById(messageIds.get(1)).queue( (dMsg2) -> dMsg2.delete().queue());
										event.getChannel().retrieveMessageById(messageIds.get(2)).queue( (dMsg3) -> dMsg3.delete().queue()); 		
								}); 
						}); 
			}).
			exceptionally( (ex) ->
			{
				event.getHook().sendMessage(ex.getMessage()).queue(); 
				ex.printStackTrace(); 
				return null; 
			}
			); 
			System.out.println("Main thread after:" + Thread.currentThread().getName()); 
		}
				
	}
	
}
