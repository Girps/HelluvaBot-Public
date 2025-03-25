package eventHandlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class CollectTradeListener extends ListenerParent{
	private SlashCommandInteractionEvent ev; 
	private Long tradeeId = null; 
	String traderCharacter =null; 
	String tradeeCharacter = null; 
	public CollectTradeListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId,
			Long playerId, Long tradeeId, String traderCharacter, String tradeeCharacter, SlashCommandInteractionEvent ev) {
		super(executor, sexecutor, messageId, playerId);
		// TODO Auto-generated constructor stub
		this.tradeeId = tradeeId; 
		this.ev = ev; 
		this.tradeeCharacter = tradeeCharacter; 
		this.traderCharacter = traderCharacter; 
		
		ScheduledFuture<?> future = this.sexecutor.schedule(() -> 
		{
			if (!this.pressed.get()) 
			{
				ev.getHook().sendMessage("30 seconds passed trade expired!").queue();
				ev.getJDA().removeEventListener(this);
			}
		}, 30, TimeUnit.SECONDS); 
		
		
	}
	
	public void onMessageReactionAdd(MessageReactionAddEvent event) 
	{
		if (event.getUser().getIdLong() == this.tradeeId &&
				!this.pressed.get()) 
		{
			this.executor.submit(() -> 
			{
				try 
				{
					long traderCharacterId = 0; 
					long tradeeCharacterId = 0; 
					CharacterSelection select = new CharacterSelection();
					traderCharacterId = select.getCharacterIdFromPlayersCollect(traderCharacter, playerId,event.getGuild().getIdLong());
					tradeeCharacterId = select.getCharacterIdFromPlayersCollect (tradeeCharacter,tradeeId,event.getGuild().getIdLong()); 
					if(traderCharacterId == -1 ) 
					{
						ev.getHook().sendMessage((event.getUser().getAsMention() + 
								" Does not have this character to trade!")).queue(); 
					}
					else if(tradeeCharacterId == -1)
					{
						ev.getHook().sendMessage(ev.getOption("user").getAsUser().getAsMention() +
								" Does not have this character to trade!").queue();
					}
					else
					{ 
						select.swapUserCollectible(playerId, tradeeId, traderCharacterId, tradeeCharacterId, event.getGuild().getIdLong());	
						ev.getHook().sendMessage("Trade successful!").queue();
						this.pressed.set(true); 
						event.getJDA().removeEventListener(this); 
					} 
				}
				catch(Exception ex) 
				{
					ex.printStackTrace(); 
					ev.getHook().sendMessage("Something went wrong!").queue(); 
				}
			}); 
		}
	}
	
}
