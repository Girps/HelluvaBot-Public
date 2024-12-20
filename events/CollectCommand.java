package events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class CollectCommand extends ListenerAdapter{
	
	private static EventWaiter waiter; 
	
	public CollectCommand(EventWaiter argWaiter) 
	{ 
		waiter = argWaiter; 
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
					select.insertUserIntoCollect(event.getUser().getIdLong(), event.getGuild().getIdLong());
					System.out.println(Thread.currentThread().getName()); 
				}).thenRun( () -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					CompletableFuture<Boolean> limitFuture =  CompletableFuture.supplyAsync( () -> !select.getClaimLimit(event.getUser().getIdLong(), event.getGuild().getIdLong()));  	 
					CompletableFuture<String> limitTime = CompletableFuture.supplyAsync(() ->   select.getCollectTime()); 
					CompletableFuture.allOf(limitFuture, limitTime).thenRun( () ->  
					{
						Boolean flag= limitFuture.join(); 
						String time = limitTime.join(); 
						event.getHook().sendMessage( event.getUser().getAsMention() + " next claim reset time is " + MarkdownUtil.bold(time + "!")  +   ( (flag ) ? (" You currently have 1 claim!") : (" You currently have no claims!") )   ).queue(); 
						System.out.println(Thread.currentThread().getName()); 
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
					select.insertUserIntoCollect(event.getUser().getIdLong(), event.getGuild().getIdLong());
				}).thenRun( () -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					CompletableFuture<Integer> rollFuture =  CompletableFuture.supplyAsync( () ->  select.getPlayerRolls(event.getUser().getIdLong(), event.getGuild().getIdLong()));  	 
					CompletableFuture<String> limitTime = CompletableFuture.supplyAsync(() ->  select.getRollRestTime()); 
					
					CompletableFuture.allOf(rollFuture,limitTime).thenRun( () -> 
					{
						int rolls = rollFuture.join(); 
						String time = limitTime.join(); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " next roll time is " + MarkdownUtil.bold(time + "!") + " You have " + rolls + " rolls left!" ).queue(); 
					}); 
					
				}).exceptionally((ex) -> 
				{
					ex.printStackTrace(); 
					event.getHook().sendMessage("Something went wrong!"); 
					return null; 
				}); 
				
				break; 
				
			case "roll": // roll a random character claim with a reaction  
				  
				event.deferReply().queue( (ev) -> { 
				Instant now = Instant.now(); 
				CompletableFuture.runAsync(() -> 
				{
					
					CharacterSelection select = new CharacterSelection(); 
					select.insertUserIntoCollect(event.getUser().getIdLong(), event.getGuild().getIdLong());
					
				}).thenRun(() ->
				{
					CharacterSelection select = new CharacterSelection(); 
					// if player has no turn return 
					if ( select.getPlayerRollsLimit(event.getUser().getIdLong(), event.getGuild().getIdLong())) 
					{
						// Get time till next turn reset and 
						String time = select.getRollRestTime(); 
						event.getHook().sendMessage( event.getUser().getAsMention() 
								+ " you ran out of rolls! Please wait till "
								+ MarkdownUtil.bold(time)
								+ " to roll again!").queue(); 
						throw new RuntimeException(" "); 
					}
					
				}).thenApply((v) ->
				{
					// now Decrement players roll and Get list of users that thave character in their wish list 
					CharacterSelection select = new CharacterSelection();
					Character temp = null;
					try 
					{
						temp = select.getRandomCharacters(GAMETYPE.COLLECT, SETUPTYPE.LIGHT, event.getGuild().getIdLong(), 1)[0];
						temp.getName();  
					} catch (Exception e) {
						// TODO Auto-generated catch block
						event.getHook().sendMessage( e.getMessage() + " roll refunded in the mean time wait till the character is fixed.").queue(); 
						throw new RuntimeException(e.getMessage()); 
					}
					return temp; 
				}).thenAccept( (character) -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					// now parallelize alreadyClaimed, getCharacterbyId, decPlayerRoll, getUsersOfWish
					CompletableFuture<Long> claimFuture = CompletableFuture.supplyAsync(() -> select.hasBeenClaimed(character.getId(),event.getGuild().getIdLong())); 
					CompletableFuture<Void>	decRollFuture = CompletableFuture.runAsync(() -> select.decPlayerRoll(event.getUser().getIdLong(), event.getGuild().getIdLong()));
					CompletableFuture<ArrayList<String>> wishesFuture = CompletableFuture.supplyAsync(() ->  select.getUsersOfWish(character.getId(),event.getGuild().getIdLong())); 
					
					CompletableFuture.allOf(claimFuture,decRollFuture,wishesFuture).thenRun( () -> 
					{
						Long alreadyClaimed = claimFuture.join(); 
						if (alreadyClaimed != -1) 
						{
							// Get the person who has the character 
							//long userId = select.getCollectedCharPlayerId(character.getId() ,event.getGuild().getIdLong());
							event.getGuild().retrieveMemberById(alreadyClaimed).queue( (user) -> 
							{
								EmbedBuilder builder = new EmbedBuilder()
										.setTitle(character.getName())
										.setImage(character.getDefaultImage())
										.setColor(user.getColor())
										.setFooter("Already claimed!", event.getGuild().getIconUrl())
										.setDescription("Owned by " + user.getAsMention() + " !"); 
								event.getHook().sendMessageEmbeds(builder.build()).queue(); 	
							}); 
						}
						else 
						{
							EmbedBuilder builder = new EmbedBuilder()
									.setTitle(character.getName())
									.setDescription("React with an "
									+ MarkdownUtil.bold("emoji") + 
									" to claim! You have "  +  
									MarkdownUtil.bold("15 seconds") + 
									" to claim this character!")
									.setImage(character.getDefaultImage())
									.setFooter(event.getMember().getEffectiveName() 
											+ " rolled ", event.getMember().getEffectiveAvatarUrl())
									.setColor(Color.YELLOW); 
							
							// now set up event waiter for claims
							decRollFuture.join(); 
							ArrayList<String> usersName = wishesFuture.join();
							String list = ""; 
							for(String names : usersName) 
							{
								list += names + " "; 
							}
							
							// now set up the event waiter
							event.getHook().sendMessageEmbeds(builder.build() )
							.queue( (Emessage) -> 
								{
									this.waiter.waitForEvent(
											MessageReactionAddEvent.class, 
											(eReact) -> 
											{ 
												if(  !eReact.getUser().isBot() &&     eReact.getMessageIdLong() == Emessage.getIdLong()) 
												{
														CharacterSelection  innerSelect = new CharacterSelection(); 
														// Insert the user who reacted into table 
														innerSelect.insertUserIntoCollect(eReact.getUser().getIdLong(), event.getGuild().getIdLong()); 
														// Check if max has already been collect or has to wait till claim refreshes 
														CompletableFuture<Boolean>claimLimitFuture = CompletableFuture.supplyAsync( () ->
																innerSelect.getClaimLimit(eReact.getUser().getIdLong(), event.getGuild().getIdLong()));
														CompletableFuture<Boolean> collectLimitFuture =  CompletableFuture.supplyAsync( () ->
																innerSelect.checkCollectLimit(eReact.getUser().getIdLong(), event.getGuild().getIdLong()));
														// may need already claim? 
														
														CompletableFuture<Boolean> combinedFuture = claimLimitFuture.thenCombine( collectLimitFuture, (claim,collect) -> 
														{
															// If valid claim 
															if (!collect && !claim && alreadyClaimed == -1) 
															{
																return true; 
															} 	// Already claimed pass hour interval 
															else if (claim)	// check if they already claimed the pass hour interval 
															{
																String time = "";
																try 
																{
																	time = innerSelect.getCollectTime();
																	event.getHook().sendMessage(eReact.getUser().getAsMention() + " you already claimed! Wait after " + MarkdownUtil.bold(time) + " before you can claim again!" ).queue(); 
																}
																catch (Exception e)
																{
																	// TODO Auto-generated catch block
																	e.printStackTrace();
																} 
																return false; 
															}
															else if (collect) 
															{
																event.getHook().sendMessage(eReact.getUser().getAsMention() +
																		" you reached the max number of characters to collect! Release a character to open a slot!" ).queue(); 
																return false; 
															}
															return false; 
														}); 
														try 
														{
															return combinedFuture.get();
														} 
														catch (Exception e) 
														{
															throw new RuntimeException(e.getMessage()); 
														}
												}
													return false;
											},	// success 
											(eSuccess) -> CompletableFuture.runAsync( () ->  	
											{
												// Function to give user the character 
												try 
												{
													// claimCharacter method may need to be rewritten 
													CharacterSelection  innerSelect = new CharacterSelection(); 
													EmbedBuilder tempBuilder = new EmbedBuilder(builder); 
													tempBuilder.setFooter("Claimed by " + eSuccess.getMember().getEffectiveName(), eSuccess.getMember().getEffectiveAvatarUrl());
													tempBuilder.setColor(eSuccess.getMember().getColor()); 
													innerSelect.claimCharacter(character.getId(), eSuccess.getUser().getIdLong(), eSuccess.getGuild().getIdLong());
													event.getHook().editMessageEmbedsById(Emessage.getIdLong(), tempBuilder.build()).queue(); 
													event.getHook().sendMessage(eSuccess.getUser().getAsMention() + " has claimed " + MarkdownUtil.bold(character.getName()) + "!").queue(); 
												}
												catch (Exception e1)
												{
													// TODO Auto-generated catch block
													e1.printStackTrace();
													event.getHook().sendMessage("something went wrong!").queue(); 
													throw new RuntimeException(e1.getMessage()); 
												} 
											}) 
											,
												// Waited 15 seconds call this function 
												15L,TimeUnit.SECONDS, 
												() -> event.getHook().editMessageEmbedsById(Emessage.getIdLong(), builder.setFooter(
												"Claim has expired!",event.getGuild().getIconUrl()).build()).queue()
											);
								}
							); 
							// Send message notify
							if(!list.isEmpty())
							{ 
								event.getHook().sendMessage(list + " character " + MarkdownUtil.bold(character.getName()) + " has been rolled!").queue(); 
							}
						}
					});   
					
				}).exceptionally( (ex) -> 
				{
					ex.printStackTrace(); 
					return null; 
				}); 
				
				Instant end = Instant.now(); 
				
				System.out.println(Duration.between(now, end));
				}); 
			
			break; 
			case "collection" : // shows collect list of calling user or option of another user 
				CompletableFuture.supplyAsync( () -> 
				{
					event.deferReply().queue();
					CharacterSelection select = new CharacterSelection(); 
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
				}).thenAccept( (list) -> 
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
								builder.setAuthor(event.getMember().getEffectiveName() + 
										"'s Collection!", event.getMember().getEffectiveAvatarUrl(), 
										event.getMember().getEffectiveAvatarUrl()) 
								.setThumbnail(select.requestSingleCharacter(list.get(0), 
										event.getGuild().getIdLong(),
										GAMETYPE.COLLECT, SETUPTYPE.LIGHT).getDefaultImage())
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
			
				CompletableFuture.runAsync( () ->
				{
					event.deferReply().queue();
					String traderCharacter = event.getOption("trader-character").getAsString(); 
					long trader = event.getUser().getIdLong(); 
					long tradee = event.getOption("user").getAsUser().getIdLong(); 
					String tradeeCharacter = event.getOption("tradee-character").getAsString(); 
					
					// Check if tradee is bot 
					if(event.getUser().isBot() || event.getOption("user").getAsUser().isBot() || event.getOption("user").getAsUser().getIdLong() == event.getUser().getIdLong() ) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " invalid trade request with other user " + event.getOption("user").getAsUser().getAsMention()).queue(); 
					}
					else
					{ 
						event.getHook().sendMessage(event.getUser().getAsMention() +  " wants to trade their collectible " +  MarkdownUtil.bold(traderCharacter) 
						+ " with " + event.getOption("user").getAsUser().getAsMention() + "'s" + " collectible " + MarkdownUtil.bold( tradeeCharacter)  + "! React to this message with an emoji to accept the trade!").queue
						( (eMessage) -> 
							{
								this.waiter.waitForEvent(
										MessageReactionAddEvent.class, 
										(eReact) ->  
											{ 
												// Make sure not a bot and same user and message and conduct the trade 
												if(!eReact.getUser().isBot() && eReact.getUser().getIdLong() == tradee 
														&& eReact.getMessageIdLong() == eMessage.getIdLong() ) 
												{
													// now trade 
													return true; 
												}
												else 
												{
													return false;
												}
											},	// success 
											(eSuccess) -> 	
											{
												long traderCharacterId = 0; 
												long tradeeCharacterId = 0; 
												try 
												{
												
													CharacterSelection select = new CharacterSelection();
													traderCharacterId = select.getCharacterIdFromPlayersCollect(traderCharacter, trader,event.getGuild().getIdLong());
													tradeeCharacterId = select.getCharacterIdFromPlayersCollect (tradeeCharacter,tradee,event.getGuild().getIdLong()); 
													if(traderCharacterId == -1 ) 
													{
														event.getHook().sendMessage((event.getUser().getAsMention() + " Does not have this character to trade!")).queue(); 
														throw new RuntimeException(""); 
													}
													else if(tradeeCharacterId == -1)
													{
														event.getHook().sendMessage(event.getOption("user").getAsUser().getAsMention() + " Does not have this character to trade!").queue();
														throw new RuntimeException(""); 
													}
													select.swapUserCollectible(event.getUser().getIdLong(), tradee, traderCharacterId, tradeeCharacterId, eSuccess.getGuild().getIdLong());
												}
												catch (Exception e)
												{
													e.printStackTrace();
													event.getHook().sendMessage(e.getMessage()).queue(); 
													return; 
												} 
												event.getHook().sendMessage("Trade successful!").queue(); 
											} 
											,
											// Waited 30 seconds call this function
											30L,TimeUnit.SECONDS, 
											() -> event.getHook().sendMessage("30 seconds passed trade expired!").queue()
										);});
					}
				}).exceptionally((ex) -> 
				{
					ex.printStackTrace(); 
					return null;
				}); 
				break; 
			case "reset-collect" : // Can only be decided by Helluva Admin
				
				CompletableFuture.runAsync( () -> 
				{
					event.deferReply().queue(); 
					if ( Helper.checkAdminRole(event.getMember().getRoles())) 
					{
						// Reset the game for the server 
						try 
						{					
							CharacterSelection select = new CharacterSelection();
							select.removeAllPlayersCollectInGuild(event.getGuild().getIdLong());
							event.getHook().sendMessage( "Helluva Admin " + event.getUser().getAsMention() + " has reset the collect game!").queue();
							
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
						event.getHook().sendMessage("<@"+ event.getUser().getId() + ">"+ " only " + MarkdownUtil.bold("Helluva Admins") + " can reset the collect game!").queue();				
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
					charId = select.getCharacterIdFromPlayersCollect(targetCharacter, event.getUser().getIdLong(),event.getGuild().getIdLong() ); 
					
					return charId; 
				}).thenAccept((characterId) -> 
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
						 
					}).thenAccept(list -> 
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
								.setFooter("You will be @ when these characters appear on a roll!"); 
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
						boolean flag = select.wishListLimit(event.getUser().getIdLong(), event.getGuild().getIdLong()); 		 
						return flag; 
					}).thenAccept( (wish) -> 
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
					} ).exceptionally( (ex) -> 
					{
						ex.printStackTrace(); 
						return null; 
					}); 
					
				}
				break; 
			case "clear-wishes" :
			{
					event.deferReply().queue( (v) -> 
					{
						CharacterSelection select = new CharacterSelection(); 
						select.clearWishList(event.getUser().getIdLong(), event.getGuild().getIdLong()); 
						event.getHook().sendMessage(event.getUser().getAsMention() + " wishlist cleared!").queue();
					});	
			}
			break ; 
			case "remove-wish": 	// remove character from your wish list
			{
				String targetCharacter = event.getOption("character").getAsString(); 
				
				event.deferReply().queue( (v) -> 
				{
					CharacterSelection select = new CharacterSelection(); 
					select.removeWish(targetCharacter,event.getUser().getIdLong(), event.getGuild().getIdLong());
					event.getHook().sendMessage(event.getUser().getAsMention() + " has removed wish " + MarkdownUtil.bold(targetCharacter) + "!").queue();
				});  
			}
				break; 
			case "release": // remove character from collection own collection
			{ 
				event.deferReply().queue( (v) -> 
				{
					
					String targetCharacter = event.getOption("character").getAsString(); 
					CharacterSelection select = new CharacterSelection();
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
					event.deferReply().queue( (v) -> 
					{
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
				
				
				event.deferReply().queue(v -> 
				{
					if (event.getOption("receiver").getAsUser().isBot() || event.getOption("receiver").getAsUser().getIdLong() == event.getUser().getIdLong()) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " invalid gift to " + event.getOption("receiver").getAsUser().getAsMention() ).queue();
					} 
					else 
					{
						String giftCharacterName = event.getOption("gift").getAsString(); 
						long recieverId =  event.getOption("receiver").getAsUser().getIdLong(); 
						CharacterSelection select = new CharacterSelection(); 
						CompletableFuture<Boolean> limitFuture = CompletableFuture.supplyAsync( () -> select.checkCollectLimit(recieverId, event.getGuild().getIdLong()) ); 
						CompletableFuture<Boolean> avaFuture = CompletableFuture.supplyAsync(() -> select.getCollectNamesOfUser(event.getUser().getIdLong(),
								event.getGuild().getIdLong()).contains(giftCharacterName) == false); 
						
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
										this.waiter.waitForEvent(
												MessageReactionAddEvent.class, 
												(eReact) ->  
													{ 
														// Make sure not a bot and same user and message and conduct gift 
														if(!eReact.getUser().isBot() && eReact.getUser().getIdLong() == recieverId 
																&& eReact.getMessageIdLong() == eMessage.getIdLong() ) 
														{
															// now trade 
															return true; 
														}
														else 
														{
															return false;
														}
													},	// success 
													(eSuccess) -> 	
													{
														// now give 
														select.giveCharacter(event.getUser().getIdLong(), recieverId,event.getGuild().getIdLong(), giftCharacterName); 
														event.getHook().sendMessage("Gift successful!").queue(); 
													} 
													,
													// Waited 10 seconds call this function
													30L,TimeUnit.SECONDS, 
													() -> event.getHook().sendMessage("30 seconds passed gift expired!").queue()
												);
									}
								); 
							}
						}).exceptionally((ex) -> 
						{
							ex.printStackTrace(); 
							return null;
						}); 	
					}
				});  
			}
				break; 
			case  "force-gift":
			{
				event.deferReply().queue( (v) -> 
				{
					User gifter = event.getOption("user").getAsUser(); 
					User receiver = event.getOption("receiver").getAsUser();
					String giftCharacterName = event.getOption("receiver-character").getAsString(); 
					if( gifter.isBot() || receiver.isBot() ) 
					{
						v.sendMessage(event.getUser().getAsMention() + " the reciever or gifter cannot be a bot!").queue(); 
					}
					else if (gifter.getIdLong() == receiver.getIdLong()) 
					{
						v.sendMessage(event.getUser().getAsMention() + " the reciever or gifter cannot be the same!").queue(); 
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
						CompletableFuture.runAsync( () -> 
						{
							CharacterSelection select = new CharacterSelection(); 
							// get the receiver characters 
							CompletableFuture<Boolean> limitFuture = CompletableFuture.supplyAsync( () -> select.checkCollectLimit(receiver.getIdLong(), event.getGuild().getIdLong()) ); 
							CompletableFuture<Boolean> avaFuture = CompletableFuture.supplyAsync(() -> select.getCollectNamesOfUser(gifter.getIdLong(),
									event.getGuild().getIdLong()).contains(giftCharacterName) == false); 
							
							limitFuture.thenCombine(avaFuture, (limit, available) -> 
							{
								
								if( limit ) 
								{
									v.sendMessage(event.getUser().getAsMention() + " the user " + receiver.getAsMention() + " has reached the limited number of characters! They must release a spot before receiving!" ).queue(); 
									return false; 
								}
								else if(available) 
								{
									v.sendMessage(event.getUser().getAsMention() + " the user " + gifter.getAsMention() + " does not have the character " +  MarkdownUtil.bold(giftCharacterName) + "!").queue(); 
									return false; 
								}
								return true; 
							}).thenAccept((trade) -> 
							{
								if (trade) 
								{
									// give character from one user to the other
									select.giveCharacter(gifter.getIdLong(), receiver.getIdLong(),event.getGuild().getIdLong(), giftCharacterName);  
									v.sendMessage(event.getUser().getAsMention() + " gifted " + gifter.getAsMention() + "'s character " 
									+ MarkdownUtil.bold(giftCharacterName) + " to " + receiver.getAsMention()).queue(); 
								}
							});  
						}
						).exceptionally((ex) -> 
						{
							System.out.println("Some error"); 
							ex.printStackTrace(); 
							event.reply("An error occured!").queue(); 
							return null; 
						});  
					}
				} ); 
			}
			break; 
			
		}
	}
}
