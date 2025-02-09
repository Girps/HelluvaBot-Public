package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
	
	private EventWaiter waiter; 
	public WaifuCommand( EventWaiter arg_Waiter)
	{
		waiter = arg_Waiter; 
	}
	
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		// Is bot return
		if(event.getUser().isBot()) {return;}
		// Waifu command for caller 
		if(event.getName().equals("waifu") && event.getOption("user") == null) 
		{
			CompletableFuture.supplyAsync( () -> {
				event.deferReply().queue();
				CharacterSelection select = new CharacterSelection();
				Long userID = event.getUser().getIdLong(); 	// get user id 
				Long serverID = event.getGuild().getIdLong(); // get guild id 
				// Search db for waifu 
				if (select.searchUserInWaifu(userID, serverID))
				{
					Character chtr = select.getUserWaifu(userID, serverID); 
					return chtr; 
				}
				else 
				{
					// No waifu so create the character put it in the waifu table 
					Character chtr = null;
					try 
					{
						chtr = select.getRandomCharacters(GAMETYPE.WAIFU,SETUPTYPE.LIGHT,event.getGuild().getIdLong(),1)[0];
					} 
					catch (Exception e)
					{
						throw new CompletionException(e); 
					} 
					// We got the character now add it to the waifus table 
					select.insertWaifu(userID, serverID, chtr); 
					return chtr;
				}
			}).thenAccept( (character) -> 
			{
				EmbedBuilder builder = new EmbedBuilder().setAuthor(event.getMember().getEffectiveName() + "'s waifu/husbando is ...", event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()) 
				.setTitle(character.getName())
				.setImage(character.getDefaultImage())
				.setColor(Color.RED)
				.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(character.getDate()), false);
				event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				
			}).exceptionally( ex -> 
			{
				 
				event.getHook().sendMessage(ex.getMessage()).queue(); 
				return null; 
			}); 
		} // Check waifu of other player 
		else if(event.getName().equals("waifu") && event.getOption("user") != null) 
		{
			
			if(event.getOption("user").getAsUser().isBot())
			{
				event.deferReply().queue(); 
				event.getHook().sendMessage("Bots cannot have waifus!").queue(); 
				return;
			}
			
			CompletableFuture.supplyAsync( () -> {
				event.deferReply().queue();
				CharacterSelection select = new CharacterSelection();
				Long targetId = event.getOption("user").getAsUser().getIdLong();
				Long serverId =  event.getGuild().getIdLong(); 
				String name = ""; 
				Character chtr = null; 
				// Search db for waifu 
				if (select.searchUserInWaifu(targetId, serverId))
				{
					chtr = select.getUserWaifu(targetId, serverId); 
					return chtr; 
				}
				else 
				{
					// No waifu so create the character put it in the waifu table of target  
					try
					{ 
						chtr = select.getRandomCharacters(GAMETYPE.WAIFU,SETUPTYPE.LIGHT,  event.getGuild().getIdLong(),1)[0];
					}
					catch (Exception e) 
					{
						throw new CompletionException(e);
					}
					name = chtr.getName(); 
					// We got the character now add it to the waifus table 
					select.insertWaifu(targetId, serverId, chtr); 
					return chtr;
				}
			}).thenAccept( (character) -> 
			{
				Member target = event.getOption("user").getAsMember(); 
				EmbedBuilder builder = new EmbedBuilder().setAuthor(target.getEffectiveName() + "'s waifu/husbando is ...", target.getEffectiveAvatarUrl(), target.getEffectiveAvatarUrl()) 
				.setTitle(character.getName())
				.setImage(character.getDefaultImage())
				.setColor(Color.RED)
				.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(character.getDate()), false);
				event.getHook().sendMessageEmbeds(builder.build()).queue(); 
			}).exceptionally( ex -> 
			{
				event.getHook().sendMessage(ex.getMessage()).queue(); 
				return null; 
			}); 
		}
		
		// Event to trade waifus 
		if(event.getName().equals("waifu-trade") && event.getOption("tradee") != null) 
		{
			
			// get both waifus from each future 
			CompletableFuture<Character> traderFuture = CompletableFuture.supplyAsync( () -> 
			{
				Long trader = event.getUser().getIdLong();
				Long tradee = event.getOption("tradee").getAsUser().getIdLong(); 
				Long serverId =  event.getGuild().getIdLong();
				CharacterSelection select = new CharacterSelection();
								
				if (trader.equals(tradee) || event.getOption("tradee").getAsUser().isBot()) 
				{
					return null; 
				}
				else 
				{ 
					return select.getUserWaifu(trader, serverId);
				}
			});
			
			CompletableFuture<Character> tradeeFuture = CompletableFuture.supplyAsync( () -> 
			{
				Long tradee = event.getOption("tradee").getAsUser().getIdLong(); 
				Long trader = event.getUser().getIdLong();
				Long serverId =  event.getGuild().getIdLong();
				CharacterSelection select = new CharacterSelection(); 
				
				if(tradee.equals(trader) ||  event.getOption("tradee").getAsUser().isBot() ) 
				{
					return null; 
				} 
				else 
				{
					return select.getUserWaifu(tradee, serverId);
				}
			});
			
			traderFuture.thenAcceptBoth( tradeeFuture, (traderChtr, tradeeChtr) ->
			{
				event.deferReply().queue();
				// now use event waiter 
				event.getHook().sendMessage( event.getUser().getAsMention() + " wants to trade their waifu " + MarkdownUtil.bold(traderChtr.getName())  + " for " + event.getOption("tradee").getAsUser().getAsMention() 
				+ "'s waifu " + MarkdownUtil.bold(tradeeChtr.getName() )+"! " + 
						 " React to this message with an emoji to accept the trade! ").queue( (msg) -> 
						 {
							 this.waiter.waitForEvent(MessageReactionAddEvent.class,
									 (eReact) -> eReact.getMessageIdLong() == msg.getIdLong() && !eReact.getUser().isBot() && eReact.getUser().getIdLong() == event.getOption("tradee").getAsUser().getIdLong(), 
									 (eReact ) -> CompletableFuture.runAsync( () -> 
									 {
										 CharacterSelection select = new CharacterSelection();  
										 // Switch the character of each user 
										 try 
										 {	
											// swap waifus 
											select.waifuSwap(event.getUser().getIdLong(), event.getOption("tradee").getAsUser().getIdLong(), 
													traderChtr.getId(), tradeeChtr.getId(), event.getGuild().getIdLong()); 
											event.getHook().sendMessage("Trade successful!").queue();
											
										 }
										 catch (Exception e)
										 {
											e.printStackTrace();
											event.getHook().sendMessage("An error occured !").queue();
										 }
									 }),
									 30L,TimeUnit.SECONDS, 
									 () -> CompletableFuture.runAsync( () -> {
										 event.getHook().sendMessage("30 seconds passed trade expired!").queue();
										 })); 
						 } );
			}).exceptionally(ex -> 
			{
				event.getHook()
				.sendMessage("Trade unsuccessful one of the users has not acquired a waifu today! Or trade request was invalid!").queue();
				return null; 
			}); 
		}
	}
}
