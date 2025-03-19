package events; 
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Color;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import CharactersPack.CharacterSelection;
import CharactersPack.GAMETYPE;
import CharactersPack.Character;
import CharactersPack.SETUPTYPE;
import eventHandlers.CollectTradeListener;
import eventHandlers.GiftCollectableListener;
import eventHandlers.RollClaimListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class CollectCommand extends ListenerAdapter{
	
	private  ExecutorService executor;
	private ScheduledExecutorService sexecutor; 
	public CollectCommand(ExecutorService executor, ScheduledExecutorService sexecutor) 
	{ 
		this.executor = executor; 
		this.sexecutor = sexecutor; 
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
					select.insertUserIntoCollect(event.getUser().getIdLong(), event.getGuild().getIdLong());
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
											.setColor(Color.YELLOW); 
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
			case "reset-collect" : // Can only be decided by Helluva Admin
				
				this.executor.submit( () -> 
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
						boolean flag = select.wishListLimit(event.getUser().getIdLong(), event.getGuild().getIdLong()); 		 
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
					if (event.getOption("receiver").getAsUser().isBot() 
							|| event.getOption("receiver").getAsUser().getIdLong() == event.getUser().getIdLong()) 
					{
						event.getHook().sendMessage(event.getUser().getAsMention() + " invalid gift to " + event.getOption("receiver").getAsUser().getAsMention() ).queue();
					} 
					else 
					{
						String giftCharacterName = event.getOption("gift").getAsString(); 
						long recieverId =  event.getOption("receiver").getAsUser().getIdLong(); 
						CharacterSelection select = new CharacterSelection(); 
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
					event.deferReply().queue(); 
					User gifter = event.getOption("user").getAsUser(); 
					User receiver = event.getOption("receiver").getAsUser();
					String giftCharacterName = event.getOption("receiver-character").getAsString(); 
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
						
						CharacterSelection select = new CharacterSelection(); 
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
				}); 
			}
			break; 
			
		}
	}
}
