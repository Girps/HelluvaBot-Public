package events;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import CharactersPack.CharacterSelection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class EconomyCommand extends ListenerAdapter{
	
	private  ExecutorService executor;
	private ScheduledExecutorService sexecutor; 
	private volatile ConcurrentHashMap<Pair<Long, Long>, Instant > map = new ConcurrentHashMap<Pair<Long, Long>, Instant>(); 
	private final String souls = "<:Soul:1349948800139001981>"; 
	public EconomyCommand(ExecutorService executor, ScheduledExecutorService sexecutor) 
	{ 
		this.executor = executor; 
		this.sexecutor = sexecutor; 
		
		// use scheduler to clear hashmap every few minutes. 
		this.sexecutor.scheduleAtFixedRate( () -> 
		{
			Map<Pair<Long,Long>, Instant> mapIt = map; 
			mapIt.forEach( (key, value) -> 
			{
				// if off duration get rid of it
				if(Instant.now().getEpochSecond() -  value.getEpochSecond() > 900) 
				{
					map.remove(key); 
				}
			}); 
		}, 0, 15, TimeUnit.MINUTES); 
		
	}
	
	public EmbedBuilder doJob(Long userId, Long serverId) 
	{
		CharacterSelection select = new CharacterSelection(); 
		ArrayList<String> job = select.getJob(); // gets the fields 
		String occupation = job.get(1);
		// with job id do another query to get the characters invoved 
		// now use the fields compute the amount should earn and send it to cash field 
		Random rand = new Random(); 
		// now get any characters for a bonus 
		HashMap<String, JSONObject> chtrs = select.getJobCharacters(userId,serverId, occupation); 
		
		Map<String, JSONObject> map = chtrs;
		String expression = "";
		int pay = rand.nextInt(Integer.valueOf(job.get(3)), Integer.valueOf( job.get(4)) + 1);
		int bonusesCal = 0; 
		if(map.size() > 0 ) 
		{
			
			for (Map.Entry<String, JSONObject> en : chtrs.entrySet()) 
			{
				double bonus = (en.getValue().getDouble("bonus") / 100 ); // bonus percentage 
				int bonusRes = (int)( pay * bonus); // get bonus 
				bonusesCal += bonusRes; 
				expression += " + " + en.getKey() + " bonus " + bonusRes;  
			}
		}
		
		int earned = pay + bonusesCal; 
		String result = "Earned: " + earned + " = " + " Paid " + pay  + expression; 
		// now add money to user's cash 
		select.sendCash(userId, serverId, earned);
		// return builder 
		EmbedBuilder builder = new EmbedBuilder(); 
		builder.setColor(Color.RED);
		builder.setTitle("Job: " + job.get(1)); 
		builder.setDescription(job.get(2) + MarkdownUtil.bold("\nEarned " + souls + earned + " souls")); 
		builder.setFooter(result); // shows the expression
		return builder; 
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{	
		
		switch (event.getName())
		{
		case "work": 
		{
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				// initally add user in the map  
				if (!map.containsKey(Pair.of(serverId, userId))) 
				{
					// add it 
					Instant start = Instant.now().minus(Duration.ofSeconds(1000)); 
					map.put(Pair.of(serverId, userId), start); 
				}
				
				// if within duration do the job otherwise deny
				if(Instant.now().getEpochSecond() - map.get(Pair.of(serverId, userId)).getEpochSecond()  > 900)  
				{
					// do the job 
					Instant start = Instant.now(); 
					map.put(Pair.of(serverId, userId), start); 
					// method to send get job id, characters involved , and send embed with money modified by characters in collect 
					EmbedBuilder builder = doJob(userId, event.getGuild().getIdLong());
					builder.setAuthor(event.getUser().getName(), event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()); 
					event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				}
				else // not done 
				{
					Long time =   map.get(Pair.of(serverId, userId)).plus(Duration.ofSeconds(900)).getEpochSecond() - Instant.now().getEpochSecond(); 
					int minutes = (int) (time / 60);
					int seconds = (int)(time % 60); 
					event.getHook().sendMessage( event.getUser().getAsMention() + " you have to wait " + minutes + 
							" minutes and " + seconds + " seconds till you can do your next job!").queue(); 
				}
			}); 
		}
			break;
		case "balance":
		{
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				
				CharacterSelection select = new CharacterSelection(); 
				ArrayList<String> fields = select.getBalance(userId, serverId);
				String cashStr = MarkdownUtil.bold("Cash: ") + souls + " " + fields.get(0);
				String bankChecking = MarkdownUtil.bold("Bank: ") +  fields.get(1);
				String totalStr = MarkdownUtil.bold("Total: ") + ( Integer.valueOf(fields.get(0)) 
						+ Integer.valueOf(fields.get(1))); 
				EmbedBuilder builder = new EmbedBuilder(); 
				builder.setColor(event.getMember().getColor()); 
				builder.setAuthor(event.getUser().getName()); 
				builder.setDescription( totalStr + "\n"+ bankChecking + "\n" + cashStr); 
				builder.setAuthor(event.getUser().getName(), 
						event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()); 
				event.getHook().sendMessageEmbeds(builder.build()).queue(); 
			}); 
		}
			break; 
		case "deposit": 
		{
			this.executor.submit(() -> 
			{
				event.deferReply().queue();
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				String amount = event.getOption("amount").getAsString(); 
				
				boolean isInt = true; 
				try 
				{ 
					int num = Integer.parseInt(amount);
					CharacterSelection select = new CharacterSelection(); 
					if ( num > 0 && select.checkDeposit(userId, serverId, num) ) 
					{
						// has enough , now deposit it 
						select.deposit(userId, serverId, num); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " has deposited " + amount + " " + souls).queue(); 
					} 
					else 
					{
						// not enough
						event.getHook().sendMessage(event.getUser().getAsMention() + " does not have " + amount + " " + souls + "to deposit").queue(); 
					}
					
				} 
				catch(Exception e) 
				{
					isInt = false; 
				}
				
				if(!isInt) 
				{
					event.getHook().sendMessage("Entered invalid input!").queue(); 
				}
			
			} ); 
		}
			break; 
		case "withdraw": 
		{
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				String amount = event.getOption("amount").getAsString(); 
				CharacterSelection select = new CharacterSelection(); 
				int num = Integer.parseInt(amount); 
				// check a valid amount 
				if(num > 0 && select.checkWithDraw(userId, serverId, num) ) 
				{
					// has valid amount now withdraw 
					select.withDraw(userId,serverId, num); 
					event.getHook().sendMessage(event.getUser().getAsMention() + " has withdrawn " + amount + " " + souls).queue(); 
				}
				else 
				{
					// invalid withdraw amount
					event.getHook().sendMessage(event.getUser().getAsMention() + " does not have " + amount + " " +  souls 
							+ " to withdraw!" ).queue(); 
				}
			}); 
		}
		break; 
		case "buy": 
		{
			this.executor.submit(() -> 
			{
				event.deferReply().queue();
				Long userId = event.getUser().getIdLong(); 
				Long serverId = event.getGuild().getIdLong(); 
				String choice = event.getOption("item").getAsString(); 
				CharacterSelection select = new CharacterSelection(); 
				// get price of the item 
				
				switch(choice) 
				{
					case "consumable rolls": 
					{
							choice = choice.replace(" ", "_"); 
						 int price = select.getItemPrice(choice);
						// check if enough for claim
						if(select.checkItemPrice(userId, serverId, choice)) 
						{
							// yes remove roll amount CASH
							// perform increment roll
							select.performItemTransaction(userId,serverId, choice);
							event.getHook().sendMessage(event.getUser().getAsMention() + " you purchased a consumable roll!").queue(); 
						}
						else 
						{
							
							event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have enough " 
									+ souls + " to purchase a " + choice.replace("_"," ") + "." + " The " + choice.replace("_"," ") + " costs " + price + " " +souls+ "!").queue(); 
						}
					}
						break;
					case "consumable claims": 
					{
						choice = choice.replace(" ", "_"); 
						 int price = select.getItemPrice(choice);
						// check if enough for claim
						if(select.checkItemPrice(userId, serverId, choice)) 
						{
							// perform increment consumeable claim
							select.performItemTransaction(userId,serverId, choice);
							event.getHook().sendMessage(event.getUser().getAsMention() + " you purchased a consumable claim!").queue(); 
						}
						else 
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have enough " 
									+ souls + " to purchase a " + choice.replace("_"," ") + "." + " The " + choice.replace("_"," ") + " costs " + price + " " +souls+ "!").queue(); 
						}
						
					}
					break; 
					case "kill your waifu": 
					{
						int price = select.getItemPrice(choice);
						// check if enough for waifu 
						if(select.checkItemPrice(userId, serverId, choice)) 
						{
							// perform transaction 
							select.performServiceTransaction(userId, serverId, choice); 
							select.removeWaifu(userId, serverId); 
							event.getHook().sendMessage(event.getUser().getAsMention() + " your waifu has been killed!").queue(); 
						}
						else 
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have enough " 
									+ souls + " to purchase a " + choice + "." + " The " + choice + " costs " + price + " " +souls+ "!").queue(); 
						}
						// yes remove waifu amount CASH
						// perform kill waifu
					}
					break; 
					default: 
						event.getHook().sendMessage(event.getUser().getAsMention() + 
								" you entered an invalid command!").queue(); 
						break; 
				}
			}); 
		}
			break; 
		case "prices":
		{
			this.executor.submit(() -> 
			{
				event.deferReply().queue(); 
				CharacterSelection select = new CharacterSelection(); 
				HashMap<String, Pair<String, Integer>> map = select.getItems(); 
				String result = "";
				
				
				for(Map.Entry<String, Pair<String,Integer>> ent : map.entrySet()) 
				{
					result += MarkdownUtil.bold(ent.getKey()) + " : " + ent.getValue().getLeft() + " Price - " + souls 
							+ " " + MarkdownUtil.bold(ent.getValue().getRight().toString()) + "\n"; 
				}
				
				// just return all items  
				EmbedBuilder builder = new EmbedBuilder(); 
				builder.setTitle("Items");
				builder.setDescription(result); 
				builder.setAuthor(event.getUser().getName(), 
						event.getMember().getEffectiveAvatarUrl(), event.getMember().getEffectiveAvatarUrl()); 
				builder.setColor(Color.RED); 
				event.getHook().sendMessageEmbeds(builder.build()).queue(); 
				
			}); 
		} 
		}
	} 
	
	
} 
