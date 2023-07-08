package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import CharactersPack.Character;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;


public class WaifuCommand extends ListenerAdapter{
	
	private Connection conn; 
	private EventWaiter waiter; 
	public WaifuCommand( Connection arg_Conn, EventWaiter arg_Waiter)
	{
	
		conn = arg_Conn; 
		waiter = arg_Waiter; 
	}
	
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		
		Long userID = event.getUser().getIdLong(); 	// get user id 
		Long serverID = event.getGuild().getIdLong(); // get guild id 
		// Waifu command for caller 
		if(event.getName().equals("waifu") && event.getOption("user") == null) 
		{
			CharacterSelection select = new CharacterSelection(conn); 
			// Now search for waifu in the database
			try 
			{
				// Search db for waifu 
				if (select.searchUserInWaifu(userID, serverID))
				{
					// Get the waifu 
					Character chtr = select.getUserWaifu(userID, serverID); 
					EmbedBuilder build = new EmbedBuilder();
					build.setAuthor(event.getMember().getEffectiveName() + "'s waifu/husbando is ...", event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()); 
					build.setTitle(chtr.getName()); 
					build.setImage(chtr.getDefaultImage()); 
					build.setColor(Color.RED);
					build.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(chtr.getDate()), false);  // get the time from the data base also 
					event.deferReply().queue(); 
					event.getHook().sendMessageEmbeds(build.build()).queue(); 
					
				}
				else 
				{
					// No waifu so create the character put it in the waifu table 
					Character chtr = select.getRandomCharacters(GAMETYPE.WAIFU,SETUPTYPE.LIGHT,1)[0]; 
					// We got the character now add it to the waifus table 
					select.insertWaifu(userID, serverID, chtr); 
					
					EmbedBuilder build = new EmbedBuilder(); 
					build.setAuthor(event.getMember().getEffectiveName() + "'s waifu/husbando is ...", event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()); 
					build.setTitle(chtr.getName()); 
					build.setImage(chtr.getDefaultImage()); 
					build.setColor(Color.RED);
					build.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(chtr.getDate()), false);  // get the time from the data base also 
					event.deferReply().queue(); 
					event.getHook().sendMessageEmbeds(build.build()).queue(); 
				}
			}
			catch(SQLException e) 
			{
				e.printStackTrace();	
				event.deferReply();
				event.reply("An error occured!").queue(); 
			}
			
			
			
			
			
		} // Check waifu of other player 
		else if(event.getName().equals("waifu") && event.getOption("user") != null) 
		{
			// Is bot return
			if(event.getUser().isBot()) {return;}
			if(event.getOption("user").getAsUser().isBot())
			{
				event.deferReply().queue(); 
				event.getHook().sendMessage("Bots cannot have waifus!").queue(); 
				return;
			}
			
			
			Long targetId = event.getOption("user").getAsUser().getIdLong();  
			Member target = event.getOption("user").getAsMember(); 
			// Now check if user has a waifu if not give them a bot 
			CharacterSelection select = new CharacterSelection(conn); 
			// Now search for waifu in the database
			try 
			{
				// Search db for waifu 
				if (select.searchUserInWaifu(targetId, serverID))
				{
					// Get the waifu 
					Character chtr = select.getUserWaifu(targetId, serverID); 
					EmbedBuilder build = new EmbedBuilder(); 
					build.setAuthor(target.getEffectiveName() + "'s waifu/husbando is ...", target.getEffectiveAvatarUrl(), target.getEffectiveAvatarUrl()); 					build.setTitle(chtr.getName()); 
					build.setImage(chtr.getDefaultImage()); 
					build.setColor(Color.RED);
			
					build.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(chtr.getDate()), false);  // get the time from the data base also
					event.deferReply().queue();
					event.getHook().sendMessageEmbeds(build.build()).queue();
				}
				else 
				{
					// No waifu so create the character put it in the waifu table 
					Character chtr = select.getRandomCharacters(GAMETYPE.WAIFU,SETUPTYPE.LIGHT,1)[0]; 
					// We got the character now add it to the waifus table 
					select.insertWaifu(targetId, serverID, chtr); 
					
					EmbedBuilder build = new EmbedBuilder(); 
					build.setAuthor(target.getEffectiveName() + "'s waifu/husbando is ...", target.getEffectiveAvatarUrl(), target.getEffectiveAvatarUrl()); 
					build.setTitle(chtr.getName()); 
					build.setImage(chtr.getDefaultImage()); 
					build.setColor(Color.RED);
			
					build.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(chtr.getDate()), false);  // get the time from the data base also
					event.deferReply().queue();
					event.getHook().sendMessageEmbeds(build.build()).queue(); 
				}
			}
			catch(SQLException e) 
			{
				e.printStackTrace();
				event.deferReply();
				event.getHook().sendMessage("An error occured!").queue(); 
			}
			
		}
		
		// Event to trade waifus 
		if(event.getName().equals("waifutrade") && event.getOption("tradee") != null) 
		{

			CharacterSelection Outterselect = new CharacterSelection(conn); 
			TextChannel chan = event.getChannel().asTextChannel();
			// Get the user and the uers to trade with 
			User trader = event.getUser(); 
			User tradee = event.getOption("tradee").getAsUser(); 
			
			if(trader.getIdLong() == tradee.getIdLong() ) 
			{
				event.deferReply().queue(); 
				event.getHook().sendMessage("Can not trade with yourself!").queue(); 
				return; 
			}
			
			if(trader.isBot() || tradee.isBot() )
			{
				event.deferReply().queue(); 
				event.getHook().sendMessage("Can not trade with a bot!").queue(); 
				return; 
			}
			
			event.deferReply().queue(); 
			
			try {
				event.getHook().sendMessage("<@"+ trader.getId() + ">" + " wants to trade their waifu " + MarkdownUtil.bold(Outterselect.getUserWaifu(trader.getIdLong(), serverID).getName())  + " for " + "<@" + tradee.getId() + ">" 
				+ "'s waifu " + MarkdownUtil.bold(Outterselect.getUserWaifu(tradee.getIdLong(), serverID).getName() )+"! " + 
						 "React to this message with an emoji to accept the trade! ").queue( (msg) -> 
						 {
							 this.waiter.waitForEvent(MessageReactionAddEvent.class,
									 (eReact) -> eReact.getMessageIdLong() == msg.getIdLong() && !eReact.getUser().isBot() && eReact.getUser().getIdLong() == tradee.getIdLong(), 
									 (eReact ) ->
									 {
										 CharacterSelection select = new CharacterSelection(conn); 
										 Character one = null; 
										 Character two = null; 
										 // Switch the character of each user 
										 try 
										 {	
											 
											 // Now check if either has a character to trade with! 
											if(select.searchUserInWaifu(trader.getIdLong(), eReact.getGuild().getIdLong()) 
													&& select.searchUserInWaifu(tradee.getIdLong(), eReact.getGuild().getIdLong())) 
											{
											one = select.getUserWaifu(trader.getIdLong(), eReact.getGuild().getIdLong());
											two = select.getUserWaifu(tradee.getIdLong(), eReact.getGuild().getIdLong()); 
											
											// Now we got two characters now swap the character we have to update the database!
											
											// update trader 
											select.updateWaifuCharacter(trader.getIdLong(), eReact.getGuild().getIdLong(), two);
											select.updateWaifuCharacter(tradee.getIdLong(), eReact.getGuild().getIdLong(), one);
											chan.sendMessage("Trade successful!").queue();
											
											// Now we swapped 

											}
											else 
											{
												
												chan.sendMessage("Trade unsuccessful one of the users has not acquired a waifu today!").queue();
											}
										 }
										 catch (SQLException e)
										 {
											e.printStackTrace();
											chan.sendMessage("An error occured !").queue();
										 }
									 },
									 30L,TimeUnit.SECONDS, 
									 () -> chan.sendMessage("30 seconds passed trade expired!").queue()); 
						 } );
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
