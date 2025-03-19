package eventHandlers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class RollClaimListener extends ListenerParent{
	private SlashCommandInteractionEvent ev = null; 
	private CharactersPack.Character character = null; 
	private Message messageEmbed = null; 
	private EmbedBuilder builder = null; 
    private ReentrantLock lock   = new ReentrantLock();
    public RollClaimListener(ExecutorService executor, ScheduledExecutorService sexecutor, Long messageId,
			Long playerId,Message messageEmbed, EmbedBuilder builder ,CharactersPack.Character character, SlashCommandInteractionEvent ev) {
		super(executor, sexecutor, messageId, playerId);
		// TODO Auto-generated constructor stub
		this.ev = ev; 
		this.character = character; 
		this.messageEmbed = messageEmbed;
		this.builder = builder; 
		this.sexecutor.schedule(() -> 
		{
			if (!this.pressed.get()) {
			ev.getHook().editMessageEmbedsById(messageEmbed.getIdLong(), builder.setFooter(
					"Claim has expired! " + character.getCreditStr() ,ev.getGuild().getIconUrl()).build()).queue(); 
			ev.getJDA().removeEventListener(this); 
			}
		}, 15, TimeUnit.SECONDS); 
	}
	
	
	// React with an emoji to claim character 
	public void onMessageReactionAdd(MessageReactionAddEvent event) 
	{
		// anyone can react
		if(event.getMessageIdLong() == messageId &&
				!this.pressed.get()) 
		{
			
			this.executor.submit(
					()->
					{
						try { 
							lock.lock(); 
						CharacterSelection  innerSelect = new CharacterSelection(); 
						// Insert the user who reacted into table 						
						// Check if max characters reached or has to wait till claim refreshes 
						CompletableFuture<Integer>claimLimitFuture = CompletableFuture.supplyAsync( () ->
								innerSelect.getClaimsAmount(event.getUser().getIdLong(), event.getGuild().getIdLong()));
						CompletableFuture<Boolean> collectLimitFuture =  CompletableFuture.supplyAsync( () ->
								innerSelect.checkCollectLimit(event.getUser().getIdLong(), event.getGuild().getIdLong()));
						
						CompletableFuture.allOf(claimLimitFuture,collectLimitFuture).thenRun( ()->
						{
							try 
							{ 
								if(claimLimitFuture.get() <= 0) 
								{
									String time = innerSelect.getCollectTime();
									ev.getHook().sendMessage(event.getUser().getAsMention() + " you do not have a claim! Wait after " + MarkdownUtil.bold(time) 
											+ " to recieve a claim!" ).queue(); 
								}
								else if(collectLimitFuture.get()) 
								{
									ev.getHook().sendMessage(event.getUser().getAsMention() +
											" you reached the max number of characters to collect! Release a character to open a slot!" ).queue(); 
								}
								else 
								{
									// valid claim 
									// Function to give user the character 
									// first to claim gets it
									this.pressed.set(true);
									EmbedBuilder tempBuilder = new EmbedBuilder(messageEmbed.getEmbeds().get(0)); 
									tempBuilder.setFooter("Claimed by " + event.getMember().getEffectiveName() + character.getCreditStr(), event.getMember().getEffectiveAvatarUrl());
									tempBuilder.setColor(event.getMember().getColor()); 
									innerSelect.claimCharacter(character.getId(), event.getUser().getIdLong(), event.getGuild().getIdLong());
									ev.getHook().editMessageEmbedsById(messageEmbed.getIdLong(), tempBuilder.build()).queue(); 
									ev.getHook().sendMessage(event.getUser().getAsMention() + " has claimed " +
									MarkdownUtil.bold(character.getName()) + "!").queue();  
									ev.getJDA().removeEventListener(this); 
								}
							} 
							catch(Exception ex) 
							{
								event.getChannel().sendMessage("Something went wrong!").queue(); 
								ex.printStackTrace(); 
							}
						});
						} finally 
						{
							lock.unlock(); 
						}
						
				}); 
		}
	}
}
