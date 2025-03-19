package events;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.SETUPTYPE;
import eventHandlers.WaifuSwapListener;
import CharactersPack.Character;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;


public class WaifuCommand extends ListenerAdapter {
	
	private ExecutorService executor = null; 
	private ScheduledExecutorService sexecutor = null; 
	public WaifuCommand( ExecutorService executor, ScheduledExecutorService sexecutor)
	{ 
		this.executor = executor; 
		this.sexecutor = sexecutor; 
	}
	
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{
		
		// Waifu command for caller 
		if(event.getName().equals("waifu") && event.getOption("user") == null) 
		{
			
			// submit to que
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				CharacterSelection select = new CharacterSelection();
				Long userID = event.getUser().getIdLong(); 	// get user id 
				Long serverID = event.getGuild().getIdLong(); // get guild id 
				Character chtr = null; 
				// Search db for waifu 
				if (select.searchUserInWaifu(userID, serverID))
				{
					chtr = select.getUserWaifu(userID, serverID); 
				}
				else 
				{
					// No waifu so create the character put it in the waifu table 
					chtr = null;
					try 
					{
						chtr = select.getRandomCharacters(GAMETYPE.WAIFU,SETUPTYPE.LIGHT,event.getGuild().getIdLong(),1)[0];
						// We got the character now add it to the waifus table 
						select.insertWaifu(userID, serverID, chtr); 
					} 
					catch (Exception ex)
					{
						event.getHook().sendMessage(ex.getMessage()).queue(); 

					} 
				}
				
				EmbedBuilder builder = new EmbedBuilder().setAuthor(event.getMember().getEffectiveName() + "'s waifu/husbando is ...", event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()) 
						.setTitle(chtr.getName())
						.setImage(chtr.getDefaultImage())
						.setColor(Color.RED)
						.addField(MarkdownUtil.bold("Next waifu selection in " ), MarkdownUtil.italics(chtr.getDate()), false)
						.setFooter(chtr.getCreditStr()); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				
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
			
			this.executor.submit(() -> 
			{
				try { 
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
				}
				else 
				{
					// No waifu so create the character put it in the waifu table of 
					chtr = select.getRandomCharacters(GAMETYPE.WAIFU,SETUPTYPE.LIGHT,  event.getGuild().getIdLong(),1)[0];
					name = chtr.getName(); 
					// We got the character now add it to the waifus table 
					select.insertWaifu(targetId, serverId, chtr); 
				}
				
				Member target = event.getOption("user").getAsMember(); 
				EmbedBuilder builder = new EmbedBuilder().setAuthor(target.getEffectiveName() + "'s waifu/husbando is ...", target.getEffectiveAvatarUrl(), target.getEffectiveAvatarUrl()) 
				.setTitle(chtr.getName())
				.setImage(chtr.getDefaultImage())
				.setColor(Color.RED)
				.addField(MarkdownUtil.bold("Next waifu selection in "), MarkdownUtil.italics(chtr.getDate() ) + chtr.getCreditStr(), false);
				event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				} 
				catch (Exception ex) 
				{
					event.getHook().sendMessage(ex.getMessage()).queue(); 
				}
			} ); 
			
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
			}, this.executor);
			
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
			}, this.executor);
			
			
			traderFuture.thenAcceptBoth( tradeeFuture, (traderChtr, tradeeChtr) ->
			{
				if(traderChtr == null || tradeeChtr ==null)
				{
					event.deferReply().queue();
					event.getHook().sendMessage("Invalid trade with a bot or player has not acquired their waifu today!").queue();
				}
				else
				{ 
					event.deferReply().queue();
					// now use event waiter 
					event.getHook().sendMessage( event.getUser().getAsMention() + " wants to trade their waifu " + MarkdownUtil.bold(traderChtr.getName())  + " for " + event.getOption("tradee").getAsUser().getAsMention() 
					+ "'s waifu " + MarkdownUtil.bold(tradeeChtr.getName() )+"! " + 
							 " React to this message with an emoji to accept the trade! ").queue( (msg) -> 
							 {
								 event.getJDA().addEventListener( new WaifuSwapListener(this.executor, this.sexecutor, msg.getIdLong(), 
										 event.getUser().getIdLong(), event.getOption("tradee").getAsUser().getIdLong(),tradeeChtr.getId(),traderChtr.getId(), event)  );
								 
							 });
				} 
			}).exceptionally( ex -> 
			{
				ex.printStackTrace(); 
				return null; 
			}); 
		}
	}
}
