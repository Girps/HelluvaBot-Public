package eventHandlers;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import CharactersPack.CharacterSelection; 
public class WaifuSwapListener extends ListenerParent{
	
	private Long traderChtrId; 
	private Long tradeeChtrId; 
	private Long tradeeId; 
	private SlashCommandInteractionEvent ev; 
	public WaifuSwapListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId, 
			Long playerId, Long tradeeId,Long tradeeChtrId,Long traderChtrId, SlashCommandInteractionEvent ev) 
	{
		super(executor, sexecutor,messageId, playerId);
		this.tradeeId = tradeeId; 
		this.tradeeChtrId = tradeeChtrId; 
		this.traderChtrId = traderChtrId; 
		this.ev = ev; 
		this.sexecutor.schedule(()-> 
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
    	if(event.getUserIdLong()== tradeeId && event.getMessageIdLong() == this.messageId && !this.pressed.get()) 
    	{
    		// now accept the trade
    		this.executor.submit(()->
    		{
    			try 
    			{
    				this.pressed.set(true); 
    				// start the trade
    				CharacterSelection select=  new CharacterSelection(); 
    				select.waifuSwap(playerId, tradeeId, traderChtrId, tradeeChtrId, event.getGuild().getIdLong()); 
					ev.getHook().sendMessage("Trade successful!").queue();
    			}
    			catch(Exception ex) 
    			{
    				ex.printStackTrace(); 
    				ev.getHook().sendMessage("Trade unsuccessful one of the users has not acquired a waifu today or trade request was invalid!").queue();
    			}
    			finally 
    			{
    				ev.getJDA().removeEventListener(this); 
    			}
    		});
    	}
    } 
	

}
