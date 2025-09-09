package events; 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import java.awt.Color;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;
import eventHandlers.CollectTradeListener;
import eventHandlers.GiftCollectableListener;
import eventHandlers.ResetListener;
import eventHandlers.RollClaimListener;
import eventHandlers.UpdateDefaultImageListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class CollectCommand extends ListenerAdapter{
	
	private  ExecutorService executor;
	private ScheduledExecutorService sexecutor; 
	private volatile ConcurrentHashMap<String, Character> chtrMap = new ConcurrentHashMap<String, Character>(); 
	public CollectCommand(ExecutorService executor, ScheduledExecutorService sexecutor) 
	{ 
		this.executor = executor; 
		this.sexecutor = sexecutor;
		
		
		this.sexecutor.scheduleAtFixedRate(() ->
		{
			chtrMap.clear(); 
		}, 0, 30, TimeUnit.MINUTES); 
	}
	
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) 
	{	
	
		switch (event.getName())
		{
			
			// get roll time 
			case "next-claim":
				CompletableFuture.runAsync( () -> 
				{
					event.deferReply().queue();
					CharacterSelection select = new CharacterSelection(); 
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					
				}, this.executor).thenRun( () -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					CompletableFuture<ArrayList<String>> limitFuture =  CompletableFuture.supplyAsync( () -> select.getClaims(event.getUser().getIdLong(), event.getGuild().getIdLong()), this.executor);  	 
					CompletableFuture<String> limitTime = CompletableFuture.supplyAsync(() ->   select.getCollectTime() , this.executor); 
					CompletableFuture.allOf(limitFuture, limitTime).thenRun( () ->  
					{
						ArrayList<String> clms = limitFuture.join(); 
						String time = limitTime.join(); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " you have " + MarkdownUtil.bold( clms.get(0)) + " claims and "
								+ MarkdownUtil.bold(clms.get(1)) + " consumable claims. Next claims reset will be in "
										+ MarkdownUtil.bold(time) + ".").queue(); 
					});
				}).exceptionally((ex) -> 
				{
					event.getHook().sendMessage("Something went wrong!").queue(); 
					ex.printStackTrace(); 
					return null; 
				});  
				
			break; 
			
			case "next-roll":
				
				CompletableFuture.runAsync( () -> 
				{
					event.deferReply().queue();
					CharacterSelection select = new CharacterSelection(); 
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
				}, this.executor).thenRun( () -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					CompletableFuture<ArrayList<String>> rollFuture =  CompletableFuture.supplyAsync( () ->  select.getPlayerRolls(event.getUser().getIdLong(), event.getGuild().getIdLong()), this.executor);  	 
					CompletableFuture<String> limitTime = CompletableFuture.supplyAsync(() ->  select.getRollRestTime(), this.executor); 
					
					CompletableFuture.allOf(rollFuture,limitTime).thenRun( () -> 
					{
						ArrayList<String> rolls = rollFuture.join(); 
						String time = limitTime.join(); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " you have " + MarkdownUtil.bold(rolls.get(0)) 
								+ " regular rolls and have " + MarkdownUtil.bold(rolls.get(1)) + " consumable rolls." + 
								" The next rolls reset will be in " + MarkdownUtil.bold(time) + ".").queue();
					}); 
					
				}).exceptionally((ex) -> 
				{
					ex.printStackTrace(); 
					event.getHook().sendMessage("Something went wrong!"); 
					return null; 
				}); 
				break; 
				
			case "roll": // roll a random character claim with a reaction  
				// 
				this.executor.submit(() -> 
				{
					try
					{ 	
					event.deferReply().queue();
					CharacterSelection select = new CharacterSelection();
					Character temp = null; 
					// insert player into the game 
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					// if player has no turn
					// if player has no turn return 
					if ( select.getPlayerRollsLimit(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						// Get time till next turn reset and 
						String time = select.getRollRestTime(); 
						event.getHook().sendMessage( event.getUser().getAsMention() 
								+ " you ran out of rolls! Please wait till "
								+ MarkdownUtil.bold(time)
								+ " to roll again!").queue(); 
					}
					else 
					{
						// get character  
						temp = select.getRandomCharacters(GAMETYPE.COLLECT, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 1)[0];
						temp.getName();  
					
						// now check if character has been claimed , is in someones wishlist and decrement players roll
						final Character chtr = temp; 
						CompletableFuture<Long> claimFuture = CompletableFuture.supplyAsync(() -> select.hasBeenClaimed(chtr.getId(),event.getGuild().getIdLong()), this.executor); 
						CompletableFuture<Void>	decRollFuture = CompletableFuture.runAsync(() -> select.decPlayerRoll(event.getUser().getIdLong(), event.getGuild().getIdLong()), this.executor);
						CompletableFuture<ArrayList<String>> wishesFuture = CompletableFuture.supplyAsync(() ->  select.getUsersOfWish(chtr.getId(),event.getGuild().getIdLong()), this.executor); 
						CompletableFuture.allOf(claimFuture,decRollFuture,wishesFuture).thenRun( () -> 
						{
							try 
							{ 
								

								Long alreadyClaimed = claimFuture.get();
								if (alreadyClaimed != -1) 
								{
									// Get the person who has the character 
									//long userId = select.getCollectedCharPlayerId(character.getId() ,event.getGuild().getIdLong());
									event.getGuild().retrieveMemberById(alreadyClaimed).queue( (user) -> 
									{
										
										
										EmbedBuilder builder = new EmbedBuilder()
												.setTitle(chtr.getName())
												.setImage(chtr.getDefaultJSONObject().getString("url"))
												.setColor(user.getColor())
												.setFooter("Already claimed!" + chtr.getCreditStr() , event.getGuild().getIconUrl())
												.setDescription("Owned by " + user.getAsMention() + " !"); 
										event.getHook().sendMessageEmbeds(builder.build()).queue(); 	
									}); 
								}
								else 
								{
									EmbedBuilder builder = new EmbedBuilder()
											.setTitle(chtr.getName())
											.setDescription("React with an "
											+ MarkdownUtil.bold("emoji") + 
											" to claim! You have "  +  
											MarkdownUtil.bold("15 seconds") + 
											" to claim this character!")
											.setImage(chtr.getDefaultImage())
											.setFooter(event.getMember().getEffectiveName() 
													+ " rolled!" + chtr.getCreditStr(), event.getMember().getEffectiveAvatarUrl())
											.setColor(chtr.getColor()); 
									ArrayList<String> usersName = wishesFuture.join();
									String list = ""; 
									for(String names : usersName) 
									{
										list += names + " "; 
									}
									decRollFuture.join(); 
									
									// Now return a the custom character and ask user to confirm this character is what they wanted
									event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) -> 
									{
										event.getJDA().addEventListener( new  RollClaimListener( executor, sexecutor, messageEmbed.getIdLong(),
												event.getUser().getIdLong(),messageEmbed, builder , chtr, event)); 
									});
									
									// Send notifications 
									if(!list.isEmpty())
									{ 
										event.getHook().sendMessage(list + " character " + MarkdownUtil.bold(chtr.getName()) + " has been rolled!").queue(); 
									}
								
								}
							} 
							catch(Exception ex) 
							{
								ex.printStackTrace(); 
								event.getHook().sendMessage("Something went wrong!").queue(); 
							}
						})
						.exceptionally((ev) ->{ ev.printStackTrace();
						return null;}); 		
					}
					
					
					} 
					catch(Exception ex) 
					{
						ex.printStackTrace(); 
						event.getHook().sendMessage("Something went wrong! Roll refunded.").queue(); 
					}
				}); 
				
			
			break; 
			case "collection" : // shows collect list of calling user or option of another user 
				CompletableFuture.supplyAsync( () -> 
				{
					event.deferReply().queue();
					CharacterSelection select = new CharacterSelection(); 
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					if(event.getOptions().isEmpty())
					{
						ArrayList<String> list = select.getCollectionListNames(event.getUser().getIdLong(), event.getGuild().getIdLong());
						return list; 
					}
					else
					{
						long idtarget = event.getOption("user").getAsUser().getIdLong();  
						ArrayList<String> list = select.getCollectionListNames(idtarget, event.getGuild().getIdLong()); 
						return list; 
					}
				}, this.executor).thenAccept( (list) -> 
				{
					
					CharacterSelection select = new CharacterSelection(); 
					int size = list.size(); 
					if(event.getOptions().isEmpty() )
					{
						
						if (!list.isEmpty()) 
						{
							String names = "";
							EmbedBuilder builder = new EmbedBuilder();
							for(String temp : list) 
							{
								names += "- "  + temp + "\n"; 
							}
							try 
							{
								
								Character chtr = select.requestSingleCharacter(list.get(0), 
										event.getGuild().getIdLong(),
										GAMETYPE.COLLECT, SETUPTYPE.LIGHT); 
								builder.setAuthor(event.getMember().getEffectiveName() + 
										"'s Collection!", event.getMember().getEffectiveAvatarUrl(), 
										event.getMember().getEffectiveAvatarUrl()) 
								.setThumbnail(chtr.getDefaultImage())
								.setColor(Color.YELLOW)
								.setDescription(names)
								.setFooter(size + "/" + "30 characters" + chtr.getCreditStr()); 
							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								throw new RuntimeException(e.getMessage()); 
							} 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						}
						else 
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " you have not collected a character from the collect game"+ "!").queue(); 
						}
						
					}
					else 
					{
						
						if( !list.isEmpty())
						{
							String names = ""; 
							EmbedBuilder builder = new EmbedBuilder(); 
								
							for(String temp : list) 
							{
								names += "- "  + temp+ "\n"; 
							}
							
							builder.setAuthor(event.getOption("user").getAsMember().getEffectiveName() + "'s Collection!", event.getOption("user").getAsMember().getEffectiveAvatarUrl(),
									event.getOption("user").getAsMember().getEffectiveAvatarUrl());
							try 
							{

								builder.setAuthor(event.getOption("user").getAsMember().getEffectiveName() + "'s Collection!", event.getOption("user").getAsMember().getEffectiveAvatarUrl(),
										event.getOption("user").getAsMember().getEffectiveAvatarUrl())
								.setThumbnail(select.requestSingleCharacter(list.get(0), event.getGuild().getIdLong(), GAMETYPE.COLLECT, SETUPTYPE.LIGHT).getDefaultImage())
								.setColor(Color.YELLOW)
								.setDescription(names)
								.setFooter(size + "/" + "30 characters");
							} 
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								throw new RuntimeException(e.getMessage()); 
							} 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						}
						else 
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + 
									" user "
									+ event.getOption("user").getAsMember().getEffectiveName() + 
									" has not collected a character from the collect game"+ "!").queue(); 
						}
					}
					
				}).exceptionally((ex) -> 
				{
					ex.printStackTrace(); 
					event.getHook().sendMessage("Something went wrong!").queue(); 
					return null; 
				}); 
				break; 
			case "collect-trade" : // trade character in collection	
				
				// test this
				this.executor.submit(() -> 
				{
					try 
					{ 
						event.deferReply().queue(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						memebers.add(event.getOption("user").getAsMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						String traderCharacter = event.getOption("trader-character").getAsString(); 
						String tradeeCharacter = event.getOption("tradee-character").getAsString(); 
						long trader = event.getUser().getIdLong(); 
						long tradee = event.getOption("user").getAsUser().getIdLong(); 
						// Check if tradee is bot 
						if( event.getOption("user").getAsUser().isBot() 
								|| event.getOption("user").getAsUser().getIdLong() == event.getUser().getIdLong() ) 
						{
							event.getHook().
							sendMessage(event.getUser().getAsMention() + " invalid trade request with other user " + event.getOption("user").getAsUser().getAsMention()).queue(); 
						}
						else 
						{
							// valid trade set up an event waiter 
							event.getHook().sendMessage(event.getUser().getAsMention() +  " wants to trade their collectible " +  MarkdownUtil.bold(traderCharacter) 
							+ " with " + event.getOption("user").getAsUser().getAsMention() + "'s" + " collectible " + MarkdownUtil.bold( tradeeCharacter)  + 
							"! React to this message with an emoji to accept the trade!").queue
							( (eMessage) -> {
								event.getJDA().addEventListener(new CollectTradeListener(executor, sexecutor, eMessage.getIdLong(),
										trader, tradee, traderCharacter, tradeeCharacter, event)); 
							}); 
						}
					} 
					catch(Exception ex) 
					{
						event.getHook().sendMessage("Something went wrong!").queue(); 
					}
				}); 
				break; 
			case "reset" : // Can only be decided by Helluva Admin
				
				this.executor.submit( () -> 
				{
					event.deferReply().queue(); 
					if ( Helper.checkAdminRole(event.getMember().getRoles())) 
					{
						// Reset the game for the server 
						try 
						{					
							
							HashMap<String, Boolean> optionMap = new HashMap<String, Boolean>(); 
							
							List<OptionMapping> ops = event.getOptions();
							for(int i =0; i < ops.size(); ++i) 
							{
								optionMap.put(ops.get(i).getName(), ops.get(i).getAsBoolean()); 
							}
							
							EmbedBuilder builder = new EmbedBuilder();
							builder.setColor(Color.white);
							builder.setTitle("Reset options"); 
							builder.setThumbnail(event.getUser().getEffectiveAvatarUrl()); 
							builder.setDescription("React to this message to apply changes. You have 30 seconds to react!");  
							for(Map.Entry<String, Boolean> ent: optionMap.entrySet() ) 
							{
								builder.addField(ent.getKey(),  ( ent.getValue() ) ? ":white_check_mark:" : ":x:", false); 
							}
							
							event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) ->
							{
								Long messageId = messageEmbed.getIdLong();
								event.getJDA().addEventListener(new ResetListener( executor, sexecutor, optionMap , messageId, 
										event.getUser().getIdLong() ,  event)); 
								
							}); 
							
						}
						catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							event.getHook().sendMessage("Something went wrong!").queue(); 
						} 
						
					}
					else 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage( event.getUser().getAsMention() + " only " + MarkdownUtil.bold("Helluva Admins") + " can reset the collect game!").queue();				
					}
				}); 
				
				break; 
			case "set-default-collect" :  // sets default image of the collection list 
			{
				CompletableFuture.supplyAsync(() -> 
				{
					event.deferReply().queue(); 
					String targetCharacter = event.getOption("character").getAsString(); 
					long charId;
					
					CharacterSelection select = new CharacterSelection();
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					charId = select.getCharacterIdFromPlayersCollect(targetCharacter, event.getUser().getIdLong(),event.getGuild().getIdLong() ); 
					
					return charId; 
				}, this.executor).thenAccept((characterId) -> 
				{
					CharacterSelection select=  new CharacterSelection(); 
					String targetCharacter = event.getOption("character").getAsString(); 
					if( characterId != -1) 
					{ 
						select.setDefCollectCharacter(characterId,event.getUser().getIdLong(), event.getGuild().getIdLong()); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " collectible "+ MarkdownUtil.bold(targetCharacter) + " has been set as default image!").queue(); 
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " character "  + MarkdownUtil.bold(targetCharacter) + " is not on your collect list!" ).queue(); 
					}
				}).exceptionally((ex) -> 
				{
					ex.printStackTrace();
					event.getHook().sendMessage("Something went wrong!").queue();
					return null; 
				}); 				
			}	
				break; 
			case "wish-list": // list of characters players want to claim, or option wish list of another user
				{
					CompletableFuture.supplyAsync(() -> 
					{
						event.deferReply().queue(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						if (event.getOptions().isEmpty())
						{ 
							ArrayList<Character>list = select.getWishList(event.getUser().getIdLong(), event.getGuild().getIdLong());
							return list;
						}
						else 
						{
							ArrayList<Character>list = select.getWishList(event.getOption("user").getAsUser().getIdLong(), event.getGuild().getIdLong());
							return list; 
						}
						 
					}, this.executor).thenAccept(list -> 
					{
						CharacterSelection select = new CharacterSelection();
						if(event.getOptions().isEmpty()) 
						{
							if(!list.isEmpty()) 
							{
								String names = ""; 
								EmbedBuilder builder = new EmbedBuilder(); 
								
								for(Character temp : list) 
								{
									Long claimId = select.hasBeenClaimed(temp.getId(), event.getGuild().getIdLong()); 
									if( claimId == -1) 
									{  
										names += "- "  + temp.getName() + "\n"; 
									}
									else if(claimId == event.getUser().getIdLong()) 
									{
										names += "- " + temp.getName() + " :white_check_mark:" +"\n"; 
									}
									else 
									{
										names += "- " + temp.getName() + " :x:" +"\n"; 
									}
								}
								builder.setAuthor(event.getMember().getEffectiveName() + "'s wishlist!", event.getUser().getEffectiveAvatarUrl(),
										event.getUser().getEffectiveAvatarUrl())
								.setThumbnail(list.get(0).getDefaultImage())
								.setColor(Color.YELLOW)
								.setDescription(names)
								.setFooter("You will be @ when these characters appear on a roll!" + list.get(0).getCreditStr()); 
								event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							}
							else 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() +
										" you have not added a character to your wishlist"+ "!").queue(); 
							}
						}
						else 
						{
							if (!list.isEmpty()) 
							{
								String names = ""; 
								EmbedBuilder builder = new EmbedBuilder(); 
							
								for(Character temp : list) 
								{
									if(select.hasBeenClaimed(temp.getId(), event.getGuild().getIdLong()) == -1) 
									{  
										names += "- "  + temp.getName() + "\n"; 
									}
									else 
									{
										names += "- " + temp.getName() + " :x:" +"\n"; 
									}
								}
								builder.setAuthor(event.getOption("user").getAsMember().getEffectiveName() + "'s wishlist!", event.getOption("user").getAsMember().getEffectiveAvatarUrl(),
										event.getOption("user").getAsMember().getEffectiveAvatarUrl());
								builder.setThumbnail(list.get(0).getDefaultImage()); 
								builder.setColor(Color.YELLOW); 
								builder.setDescription(names); 
								builder.setFooter("You will be @ when these characters appear on a roll!"); 
								event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							}
							else 
							{
								event.getHook().sendMessage( event.getUser().getAsMention() + 
										" user " + event.getOption("user").getAsUser().getAsMention() +
										" has not added a character to their wishlist"+ "!").queue(); 
							}
						}
					}).exceptionally((ex) -> 
					{
						event.getHook().sendMessage("Something went wrong!").queue(); 
						ex.printStackTrace(); 
						return null; 
					}); 
				}
				break; 
			case "add-wish": 
				{	
					CompletableFuture.supplyAsync( () -> 
					{
						event.deferReply().queue(); 
						CharacterSelection select = new CharacterSelection();
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						boolean flag = select.wishlistLimit(event.getUser().getIdLong(), event.getGuild().getIdLong()); 		 
						return flag; 
					}, this.executor).thenAccept( (wish) -> 
					{
						CharacterSelection select = new CharacterSelection(); 
						if(wish) 
						{ 
							String characterName = event.getOption("character").getAsString(); 
							long charId = select.getCharacterId(characterName, event.getGuild().getIdLong());
							select.addToWishList(charId, event.getUser().getIdLong(), event.getGuild().getIdLong()); 
							event.getHook().sendMessage(event.getUser().getAsMention() + " character " + MarkdownUtil.bold(characterName) + " added to your wishlist you will be notified when this character appears on a roll!").queue(); 
						}
						else 
						{
							event.getHook().sendMessage(event.getUser().getAsMention() + " you reached the limit of 5 characters in your wishlist!").queue(); 
						}
					}).exceptionally( (ex) -> 
					{
						ex.printStackTrace(); 
						return null; 
					}); 
					
				}
				break; 
			case "clear-wishes" :
			{
				this.executor.submit(() -> 
				{
					try 
					{ 
						event.deferReply().queue(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						select.clearWishList(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " wishlist cleared!").queue();
					} 
					catch(Exception ex) 
					{
						event.getHook().sendMessage("Something went wrong!").queue(); 
						ex.printStackTrace(); 
					}
				}); 	
			}
			break ; 
			case "remove-wish": 	// remove character from your wish list
			{
				this.executor.submit(() -> 
				{
					try 
					{ 
						event.deferReply().queue(); 
						String targetCharacter = event.getOption("character").getAsString(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						select.removeWish(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong());
						event.getHook().sendMessage(event.getUser().getAsMention() + " has removed wish " + MarkdownUtil.bold(targetCharacter) + "!").queue();
					}
					catch(Exception ex)
					{
						event.getHook().sendMessage("Something went wrong!").queue();
						ex.printStackTrace(); 
					}
				}); 
				 
			}
				break; 
			case "release": // remove character from collection own collection
			{ 
				
				this.executor.submit(() -> 
				{
					event.deferReply().queue();
					String targetCharacter = event.getOption("character").getAsString(); 
					CharacterSelection select = new CharacterSelection();
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					if(select.searchCharacterCollectList(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong()) )
					{
						select.removeCollectCharacter(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong());
						event.getHook().sendMessage(event.getUser().getAsMention() + " has released " +  MarkdownUtil.bold(targetCharacter) + " back to the collect game!").queue();
					}
					else 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have " +  MarkdownUtil.bold(targetCharacter) + "!").queue();
					}
				}); 
			}
				break; 
			case "force-release" : 
			{
				
					this.executor.submit(() -> 
					{
						event.deferReply().queue(); 
						long targetUser = event.getOption("user").getAsUser().getIdLong(); 		
						String targetCharacter = event.getOption("character").getAsString(); 
						try 
						{
							if ( !Helper.checkAdminRole(event.getMember().getRoles())) 
							{
								
								EmbedBuilder builder = new EmbedBuilder(); 
								builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
								builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
								builder.setColor(Color.RED); 
								event.getHook().sendMessageEmbeds(builder.build()).queue(); 
								event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can release another players colectible!").queue();				
								return; 
							}
							
							CharacterSelection select = new CharacterSelection();

							if(select.searchCharacterCollectList(targetCharacter,targetUser, event.getGuild().getIdLong()) )
							{
								select.removeCollectCharacter(targetCharacter,event.getOption("user").getAsUser().getIdLong(), event.getGuild().getIdLong());
								event.getHook().sendMessage(event.getUser().getAsMention() + " has released " +  event.getOption("user").getAsUser().getAsMention() + "'s "
								+ targetCharacter + " back to the collect game!").queue();
							}
							else 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + " user " + event.getOption("user").getAsMentionable() + 
										" does not have character "+ targetCharacter + "!").queue();
							}
						}
						catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							event.getHook().sendMessage("Something went wrong!").queue(); 
						} 
					}); 
				
			}	
				break; 
			case "gift-collectable" :
			{
				
				this.executor.submit(() ->
				{
					event.deferReply().queue(); 
					CharacterSelection select = new CharacterSelection(); 
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					if (event.getOption("receiver").getAsUser().isBot() 
							|| event.getOption("receiver").getAsUser().getIdLong() == event.getUser().getIdLong()) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " invalid gift to " + event.getOption("receiver").getAsUser().getAsMention() ).queue();
					} 
					else 
					{
						String giftCharacterName = event.getOption("gift").getAsString(); 
						long recieverId =  event.getOption("receiver").getAsUser().getIdLong(); 
						CompletableFuture<Boolean> limitFuture = CompletableFuture.supplyAsync( () -> select.checkCollectLimit(recieverId, event.getGuild().getIdLong()), this.executor ); 
						CompletableFuture<Boolean> avaFuture = CompletableFuture.supplyAsync(() -> select.getCollectNamesOfUser(event.getUser().getIdLong(),
								event.getGuild().getIdLong()).contains(giftCharacterName) == false , this.executor); 
						
						limitFuture.thenCombine(avaFuture, (limit, available) -> 
						{
							if(limit) 
							{
								event.getHook().sendMessage(event.getOption("receiver").getAsUser().getAsMention() + " cannot receive a character they have hit the limit of "
										+ "30 collectables! They must release a character to open a slot!").queue(); 
								return false; 
							} 
							else if(available) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + " you don't have character " + MarkdownUtil.bold(giftCharacterName) + " to give!").queue(); 
								return false; 
							}
							return true; 
						}).thenAccept( trade  -> 
						{
							if(trade) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() +  " wants to give their collectible " +  MarkdownUtil.bold(giftCharacterName) 
								+ " to " + event.getOption("receiver").getAsUser().getAsMention()+  "! React to accept this gift!").queue
								( (eMessage) -> 
									{
										event.getJDA().addEventListener(new GiftCollectableListener(executor, sexecutor, eMessage.getIdLong(),
			event.getUser().getIdLong(), event.getOption("receiver").getAsUser().getIdLong(), giftCharacterName, event)); 
									}
								); 
							}
						});
					}
				}); 
			
			}
				break; 
			case  "force-gift":
			{
				this.executor.submit(() -> 
				{
					try { 
					event.deferReply().queue(); 
					User gifter = event.getOption("user").getAsUser(); 
					User receiver = event.getOption("receiver").getAsUser();
					String giftCharacterName = event.getOption("receiver-character").getAsString();
					ArrayList<Member> memebers = new ArrayList<Member>(); 
					memebers.add(event.getMember()); 
					memebers.add(event.getOption("user").getAsMember()); 
					memebers.add(event.getOption("receiver").getAsMember() ); 
					CharacterSelection select = new CharacterSelection(); 
					select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
					if( gifter.isBot() || receiver.isBot() ) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " the reciever or gifter cannot be a bot!").queue(); 
					}
					else if (gifter.getIdLong() == receiver.getIdLong()) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " the reciever or gifter cannot be the same!").queue(); 
					}
					else if (!Helper.checkAdminRole(event.getMember().getRoles())) 
					{
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
						builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						event.getHook().sendMessage( event.getUser().getAsMention() + " only " + MarkdownUtil.bold("Helluva Admins") + " can force gift to other players!").queue();	
					}
					else 
					{
						// Run async 
						
						// get the receiver characters 
						CompletableFuture<Boolean> limitFuture = CompletableFuture.supplyAsync( () -> select.checkCollectLimit(receiver.getIdLong(), 
								event.getGuild().getIdLong()), this.executor); 
						CompletableFuture<Boolean> avaFuture = CompletableFuture.supplyAsync(() -> select.getCollectNamesOfUser(gifter.getIdLong(),
								event.getGuild().getIdLong()).contains(giftCharacterName) == false, this.executor); 
						
						limitFuture.thenCombine(avaFuture, (limit, available) -> 
						{
							
							if( limit ) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() +
										" the user " + receiver.getAsMention() + " has reached the limited number of characters! They must release a spot before receiving!" ).queue(); 
								return false; 
							}
							else if(available) 
							{
								event.getHook().sendMessage(event.getUser().getAsMention() + 
										" the user " + gifter.getAsMention() + " does not have the character " +  MarkdownUtil.bold(giftCharacterName) + "!").queue(); 
								return false; 
							}
							return true; 
						}).thenAccept((trade) -> 
						{
							if (trade) 
							{
								// give character from one user to the other
								select.giveCharacter(gifter.getIdLong(), receiver.getIdLong(),event.getGuild().getIdLong(), giftCharacterName);  
								event.getHook().sendMessage(event.getUser().getAsMention() + " gifted " + gifter.getAsMention() + "'s character " 
								+ MarkdownUtil.bold(giftCharacterName) + " to " + receiver.getAsMention()).queue(); 
							}
						});  
					}
					}
					catch(Exception ex) 
					{
						ex.printStackTrace(); 
					event.getHook().sendMessage("Something went wrong").queue(); 
					}
				}); 
			}
			break; 
			case "search": 
				
				this.executor.submit( () -> 
				{
					try 
					{
						event.deferReply().queue(); 
						String characterName = event.getOption("character").getAsString(); 
						// searhc character states
						Long serverId = event.getGuild().getIdLong(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						Character chtr = select.requestSingleCharacter(characterName, serverId, GAMETYPE.COLLECT, SETUPTYPE.NEITHER);
						Long playerId = select.searchPlayerIdCollect(chtr.getId(), serverId); 
						ArrayList<String> occupation = select.getOccupation(chtr.getId()); 
						this.chtrMap.put(chtr.getName(), chtr); 
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setTitle(chtr.getName()); 
						builder.setImage(chtr.getDefaultImage());
						Integer defaultIndex = chtr.getDefaultNumber() + 1;
						Integer max = chtr.getJSONImages().size(); 
						// check if no credits available 
						if(chtr.getCreditStr().equalsIgnoreCase("")) 
						{
							builder.setFooter( "Art by N/A | link : N/A" + "\nImage:" + defaultIndex + "/" + max);
						} 
						else 
						{ 
							builder.setFooter(chtr.getCreditStr() + "\nImage:" + defaultIndex + "/" + max);
						} 
						// Instantiate list of buttons 
						List<Button> buttons = new ArrayList<Button>();
						buttons.add(Button.primary("leftSearch", "<")); 
						buttons.add(Button.danger("closeSearch", "Close")); 
						buttons.add(Button.primary("rightSearch", ">")); 
						// player has the character get their name 
						if(playerId != null) 
						{
							event.getGuild().retrieveMemberById(playerId).queue( (mem) -> 
							{
								// rarity 
								// perks 
								builder.setDescription("Owned by: " + mem.getAsMention()); 
								builder.addField("Rarity", chtr.getRarity(), true); 
								JSONObject perks = chtr.getPerks(); 
								Iterator<String> key = perks.keys();  
								while (key.hasNext()) 
								{
									String strKey = key.next(); 
									builder.addField(strKey, perks.get(strKey).toString() + "%", true);  
								}
								builder.addField("Occupation", (  
										!occupation.isEmpty() ? occupation.toString().replace("[", "").replace("]", "")  : "N/A") , true); 
								builder.setColor(mem.getColor()); 
								event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue();
							}, error ->
							{
								// member does not exist must clean it 
								// rarity 
								// perks 
								builder.setDescription("Owned by: " + playerId + " this player has left the server use /clean to remove non-existing members."); 
								builder.addField("Rarity", chtr.getRarity(), true); 
								JSONObject perks = chtr.getPerks(); 
								Iterator<String> key = perks.keys();  
								while (key.hasNext()) 
								{
									String strKey = key.next(); 
									builder.addField(strKey, perks.get(strKey).toString() + "%", true);  
								}
								builder.addField("Occupation", (  
										!occupation.isEmpty() ? occupation.toString().replace("[", "").replace("]", "")  : "N/A") , true); 
								builder.setColor(Color.WHITE); 
								event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue();
							}); 
						
						}
						else 
						{
							
							builder.setDescription("Still available to collect"); 
							builder.addField("Rarity", chtr.getRarity(), true); 
							JSONObject perks = chtr.getPerks(); 
							Iterator<String> key = perks.keys();  
							while (key.hasNext()) 
							{
								String strKey = key.next(); 
								builder.addField(strKey, perks.get(strKey).toString() + "%", true);  
							}
							builder.addField("Occupation", (  
									!occupation.isEmpty() ? occupation.toString().replace("[", "").replace("]", "")  : "N/A") , true);
							builder.setColor(chtr.getColor()); 
							event.getHook().sendMessageEmbeds(builder.build()).addActionRow(buttons).queue(); 
						}

					} 
					catch(Exception ex) 
					{
						ex.printStackTrace() ; 
						event.getHook().sendMessage("Somthing went wrong searching for" + event.getOption("character").getAsString()).queue(); 
					}
				}); 
				
				break; 
				
			case "stats":
			{
				this.executor.submit(() ->
				{
					try 
					{
						event.deferReply().queue();
						Long serverId = event.getGuild().getIdLong(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						Integer claimAmount = select.getTotalClaims(serverId);
						Integer characterAmount = select.getTotalCharacterCount(serverId);
						Float claimPercent =  ((float) ( (float)claimAmount ) / ((float)characterAmount) ) * 100f ; 
						Float availableAmount = ((float) ( 100f - claimPercent) )  ; 
						Integer diff = characterAmount - claimAmount; 
						// get rarity 
						HashMap<String, Float> rarityMap = select.getRarity();
						EmbedBuilder builder = new EmbedBuilder(); 
						builder.setTitle("Collect Statistics");
						builder.addField("Claimed:", claimAmount.toString() + "/" + characterAmount.toString(), true);  
						builder.addField("Avaliable:", (diff).toString()  , true);  
						builder.addBlankField(true); 
						builder.addField("Claimed Percentage:", String.format("%.2f%%",  claimPercent) , true);
						builder.addField("Available Percentage:", String.format("%.2f%%", availableAmount) , true);
						
						builder.setDescription("Chance of rolling character.\n"); 
						builder.appendDescription(MarkdownUtil.bold("Common") + ": " + String.format("%.2f%%", rarityMap.get("Common")) + "\n");
						builder.appendDescription(MarkdownUtil.bold("Uncommon") + ": " + String.format("%.2f%%", rarityMap.get("Uncommon")) + "\n");
						builder.appendDescription(MarkdownUtil.bold("Rare") + ": " + String.format("%.2f%%", rarityMap.get("Rare")) + "\n");
						builder.appendDescription(MarkdownUtil.bold("Ultra Rare") + ": " + String.format("%.2f%%", rarityMap.get("Ultra Rare")) + "\n");
						builder.setAuthor(event.getUser().getName(),event.getUser().getEffectiveAvatarUrl(),event.getUser().getEffectiveAvatarUrl());
						builder.setColor(Color.RED); 
						event.getHook().sendMessageEmbeds(builder.build()).queue(); 
						
					}
					catch(Exception ex) 
					{
						ex.printStackTrace(); 
						event.getHook().sendMessage("Something went wrong!").queue(); 
					}
				}); 
			}
			break; 
			case "set-default-image": 
			{
				this.executor.submit(() -> 
				{
					try 
					{
						event.deferReply().queue(); 
						CharacterSelection select = new CharacterSelection(); 
						ArrayList<Member> memebers = new ArrayList<Member>(); 
						memebers.add(event.getMember()); 
						select.addUsersToUnqueUsers(event.getGuild().getIdLong(), memebers ); // add users to the db
						 if ( Helper.checkAdminRole(event.getMember().getRoles()) ) 
						 {
							
							Long playerId = event.getUser().getIdLong(); 
							String indexStr = event.getOption("title").getAsString();
							String[] strArr = indexStr.split("\\|"); 
							Integer  index = Integer.valueOf(strArr[0]) - 1; 
							String arthor = strArr[2]; 
							String characterName = event.getOption("character").getAsString();
							EmbedBuilder builder = new EmbedBuilder();
							String image = strArr[3]; 
							builder.setFooter("Art by: " + arthor); 
							builder.setTitle(characterName); 
							builder.setImage(image); 
							builder.setColor(Color.WHITE); 
							builder.setDescription(" you have 30 seconds to react to "
									+ "confirm setting the default image for " + characterName + " in this server!"); 
							event.getHook().sendMessageEmbeds(builder.build()).queue( (messageEmbed) -> 
							{
								event.getJDA().addEventListener( new UpdateDefaultImageListener(executor, sexecutor, messageEmbed.getIdLong(),
										playerId, characterName, index, event) ); 
							}) ;
							 
						 }
						 else 
						 {
							 // not a helluva admin 
							EmbedBuilder builder = new EmbedBuilder(); 
							builder.setImage("https://i.imgur.com/gPWckoI.jpg"); 
							builder.setDescription("Make sure this role (same name no special permissons required) is created and Assigned to admins of this server in order to use this command!"); 
							builder.setColor(Color.RED); 
							event.getHook().sendMessageEmbeds(builder.build()).queue(); 
							event.getHook().sendMessage( event.getUser().getAsMention() + " only " + MarkdownUtil.bold("Helluva Admins") + " can reset the collect game!").queue();				
						 }
						
					} 
					catch(Exception ex) 
					{
						ex.printStackTrace(); 
						event.getHook().sendMessage("Something went wrong!").queue(); 
					}
				}); 
			}
				break; 
		}	
	}
	
	/* Deal with buttons clicked on the search embed message */ 
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) 
	{
		// now  get the character 
		String buttons = event.getButton().getId(); 
		if (buttons.equalsIgnoreCase("leftSearch") ||
				buttons.equalsIgnoreCase("rightSearch")) 
		{
			this.executor.submit( () -> 
			{
				try { 
				event.deferEdit().queue(); 
				MessageEmbed oldBuild = event.getMessage().getEmbeds().get(0); 
				EmbedBuilder newBuild = new EmbedBuilder(oldBuild);
				String characterName = oldBuild.getTitle(); 
				String[]  footerStr = oldBuild.getFooter().getText().split("\n"); 
				String[] numbers =  footerStr[1].split(":")[1].split("/"); 
				Integer index = (Integer.valueOf(numbers[0]) - 1 ); 
				
				ArrayList<JSONObject> arrList = new ArrayList<JSONObject>() ; 
				Integer size= 0; 
				// now get it from the map 
				
				if(!this.chtrMap.containsKey(characterName))
				{
					// add them 
					CharacterSelection select = new CharacterSelection(); 
					Character newChtr = select.requestSingleCharacter(characterName, event.getGuild().getIdLong() , GAMETYPE.COLLECT, SETUPTYPE.NEITHER); 
					this.chtrMap.put(newChtr.getName(), newChtr); 
				}
				
				
				if(this.chtrMap.containsKey(characterName)) 
				{
					// now get the character
					arrList = this.chtrMap.get(characterName).getJSONImages(); 
					size = arrList.size(); 
					// now change the index 
					if(buttons.equalsIgnoreCase("leftSearch")) 
					{
						index--;
						index = (index < 0 ) ?  ( arrList.size() - 1 ) : index;
					}
					else if (buttons.equalsIgnoreCase("rightSearch")) 
					{
						index++;
						index = (index >= arrList.size() ) ? 0 : index; 
					}
					
					// now  embed the message
					JSONObject Jobj = arrList.get(index) ; 
					
					String img = Jobj.getString("url") ;
					String author =  ( Jobj.getString("author_name").equalsIgnoreCase("") ) ? "N/A" : Jobj.getString("author_name") ;
					String author_link = ( Jobj.getString("author_link").equalsIgnoreCase("") ) ? "N/A" : Jobj.getString("author_link") ;
					String newFootStr = "Art by " + author + " | " + "link : " + author_link + "\nImage:" + (index + 1 ) + "/" + size; 
					newBuild.setImage(img);
					newBuild.setFooter(newFootStr);	
					event.getMessage().editMessageEmbeds(newBuild.build()).queue(); 
				}
				} 
				catch(Exception ex) 
				{
					ex.printStackTrace(); 
					event.getHook().sendMessage("Something went wrong!").queue(); 
				}
			}); 
		}
		else if (buttons.equalsIgnoreCase("closeSearch")) 
		{
			event.getMessage().delete().queue(); 
		}
	}
}
