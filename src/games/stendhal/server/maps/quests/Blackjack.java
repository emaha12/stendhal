package games.stendhal.server.maps.quests;

import games.stendhal.common.Direction;
import games.stendhal.common.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatAction;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Blackjack extends AbstractQuest {
	// spades ♠
	 private static final String SPADES = "\u2660";

	// hearts ♥
	private static final String HEARTS = "\u2665";

	// diamonds ♦
	private static final String DIAMONDS = "\u2666";

	// clubs ♣
	private static final String CLUBS = "\u2663";

	
	
	// 1 min at 300 ms/turn
	private static final int ONE_MINUTE = 180;

	private static final int MIN_STAKE = 10;

	private static final int MAX_STAKE = 200;

	private int stake;

	private boolean bankStands;

	private boolean playerStands;

	private Map<String, Integer> cardValues;

	private Stack<String> deck;

	private final List<String> playerCards = new LinkedList<String>();

	private final List<String> bankCards = new LinkedList<String>();

	private StackableItem playerCardsItem;

	private StackableItem bankCardsItem;

	private SpeakerNPC ramon;

	private final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(
			"-1_athor_ship_w2");

	private void startNewGame(final Player player) {
		cleanUpTable();
		playerCards.clear();
		bankCards.clear();
		playerCardsItem = (StackableItem) SingletonRepository.getEntityManager().getItem("cards");
		zone.add(playerCardsItem);
		playerCardsItem.setPosition(25, 38);
		bankCardsItem = (StackableItem) SingletonRepository.getEntityManager().getItem("cards");
		bankCardsItem.setPosition(27, 38);
		zone.add(bankCardsItem);

		playerStands = false;
		bankStands = false;
		// Before each game, we put all cards back on the deck and
		// shuffle it.
		// We could change that later so that the player can
		// try to remember what's still on the deck
		deck = new Stack<String>();
		for (final String card : cardValues.keySet()) {
			deck.add(card);
		}
		Collections.shuffle(deck);

		dealCards(player, 2);
	}

	private void cleanUpTable() {
		if (playerCardsItem != null) {
			zone.remove(playerCardsItem);
			playerCardsItem = null;
		}
		if (bankCardsItem != null) {
			zone.remove(bankCardsItem);
			bankCardsItem = null;
		}
	}

	private int countAces(final List<String> cards) {
		int count = 0;
		for (final String card : cards) {
			if (card.startsWith("A")) {
				count++;
			}
		}
		return count;
	}

	private int sumValues(final List<String> cards) {
		int sum = 0;
		for (final String card : cards) {
			sum += cardValues.get(card).intValue();
		}
		int numberOfAces = countAces(cards);
		while ((sum > 21) && (numberOfAces > 0)) {
			sum -= 10;
			numberOfAces--;
		}
		return sum;
	}

	private boolean isBlackjack(final List<String> cards) {
		return (sumValues(cards) == 21) && (cards.size() == 2);
	}

	/**
	 * Deals <i>number</i> cards to the player, if the player is not standing,
	 * and to the bank, if the bank is not standing.
	 * @param player 
	 *
	 * @param number
	 *            The number of cards that each player should draw.
	 */
	private void dealCards(final Player player, final int number) {
		String message = "\n";
		int playerSum = sumValues(playerCards);
		int bankSum = sumValues(bankCards);
		for (int i = 0; i < number; i++) {
			if (!playerStands) {
				final String playerCard = deck.pop();
				playerCards.add(playerCard);
				message += "You got a " + playerCard + ".\n";
			}

			if (playerStands && (playerSum < bankSum)) {
				message += "The bank stands.\n";
				bankStands = true;
			}
			if (!bankStands) {
				final String bankCard = deck.pop();
				bankCards.add(bankCard);
				message += "The bank got a " + bankCard + ".\n";
			}
			playerSum = sumValues(playerCards);
			bankSum = sumValues(bankCards);
		}
		playerCardsItem.setQuantity(playerSum);
		playerCardsItem.setDescription("You see the player's cards: "
				+ Grammar.enumerateCollection(playerCards));
		playerCardsItem.notifyWorldAboutChanges();
		bankCardsItem.setQuantity(bankSum);
		bankCardsItem.setDescription("You see the bank's cards: "
				+ Grammar.enumerateCollection(bankCards));
		bankCardsItem.notifyWorldAboutChanges();
		if (!playerStands) {
			message += "You have " + playerSum + ".\n";
			if (playerSum == 21) {
				playerStands = true;
			}
		}
		if (!bankStands) {
			message += "The bank has " + bankSum + ".\n";
			if ((bankSum >= 17) && (bankSum <= 21) && (bankSum >= playerSum)) {
				bankStands = true;
				message += "The bank stands.\n";
			}
		}
		final String message2 = analyze(player);
		if (message2 != null) {
			message += message2;
		}
		ramon.say(message);
	}

	/**
	 * @param player 
	 * @return The text that the dealer should say, or null if he shouldn't say
	 *         anything.
	 */
	private String analyze(final Player player) {
		final int playerSum = sumValues(playerCards);
		final int bankSum = sumValues(bankCards);
		String message = null;
		if (isBlackjack(bankCards) && isBlackjack(playerCards)) {
			message = "You have a blackjack, but the bank has one too. It's a push. ";
			message += payOff(player, 1);
		} else if (isBlackjack(bankCards)) {
			message = "The bank has a blackjack. Better luck next time!";
		} else if (isBlackjack(playerCards)) {
			message = "You have a blackjack! Congratulations! ";
			message += payOff(player, 3);
		} else if (playerSum > 21) {
			if (bankSum > 21) {
				message = "Both have busted! This is a draw. ";
				message += payOff(player, 1);
			} else {
				message = "You have busted! Better luck next time!";
			}
		} else if (bankSum > 21) {
			message = "The bank has busted! Congratulations! ";
			message += payOff(player, 2);
		} else {
			if (!playerStands) {
				message = "Do you want another card?";
				ramon.setCurrentState(ConversationStates.QUESTION_1);
			} else if (!bankStands) {
				letBankDrawAfterPause(ramon.getAttending().getName());
			} else if (bankSum > playerSum) {
				message = "The bank has won. Better luck next time!";
			} else if (bankSum == playerSum) {
				message = "This is a draw. ";
				message += payOff(player, 1);
			} else {
				message = "You have won. Congratulations! ";
				message += payOff(player, 2);
			}
		}
		return message;
	}

	private void letBankDrawAfterPause(final String playerName) {
		SingletonRepository.getTurnNotifier().notifyInSeconds(1, new TurnListener() {
			private final String name = playerName;

			public void onTurnReached(final int currentTurn) {
				if (name.equals(ramon.getAttending().getName())) {
					dealCards(ramon.getAttending(), 1);
				}

			}

		});
	}

	/**
	 * Gives the player <i>factor</i> times his stake.
	 *
	 * @param player
	 *            The player.
	 * @param factor
	 *            The multiplier. 1 for draw, 2 for win, 3 for win with
	 *            blackjack.
	 * @return A message that the NPC should say to inform the player.
	 */
	private String payOff(final Player player, final int factor) {
		final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		money.setQuantity(factor * stake);
		player.equip(money, true);
		if (factor == 1) {
			return "You get your stake back.";
		} else {
			return "Here's your stake, plus " + (factor - 1) * stake
					+ " pieces of gold.";
		}
	}

	@Override
	// @SuppressWarnings("unchecked")
	public void addToWorld() {

		ramon = new SpeakerNPC("Ramon") {
			@Override
			protected void createPath() {
				// Ramon doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {

				addGreeting("Welcome to the #blackjack table! You can #play here to kill time until the ferry arrives.");
				addJob("I was a card dealer in the Semos tavern, but I lost my gambling license. But my brother Ricardo is still working in the tavern.");
				addReply(
						"blackjack",
						"Blackjack is a simple card game. You can read the rules at the blackboard at the wall.");
				// TODO add help description string
				addHelp("...");
				addGoodbye("Goodbye!");
			}

			@Override
			protected void onGoodbye(final Player player) {
				// remove the cards when the player stops playing.
				cleanUpTable();
			}
		};

		ramon.setEntityClass("naughtyteen2npc");
		ramon.setPosition(26, 36);
		ramon.setDirection(Direction.DOWN);
		ramon.initHP(100);
		zone.add(ramon);

		cardValues = new HashMap<String, Integer>();
		final String[] colors = { CLUBS, 
				DIAMONDS, 
				HEARTS,
				SPADES }; 
		final String[] pictures = { "J", "Q", "K" };
		for (final String color : colors) {
			for (int i = 2; i <= 10; i++) {
				cardValues.put(i + color, Integer.valueOf(i));
			}
			for (final String picture : pictures) {
				cardValues.put(picture + color, Integer.valueOf(10));
			}
			// ace values can change to 1 during the game
			cardValues.put("A" + color, Integer.valueOf(11));
		}

		// increase the timeout, as otherwise the player often
		// would use their stake because of reacting too slow.
		
		ramon.setPlayerChatTimeout(ONE_MINUTE); 

		ramon.add(ConversationStates.ATTENDING, "play", null,
				ConversationStates.ATTENDING,
				"In order to play, you have to at least #'stake " + MIN_STAKE
						+ "' and at most ##stake " + MAX_STAKE
						+ "' pieces of gold. So, how much will you risk?", null);

		ramon.add(ConversationStates.ATTENDING, "stake", null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
				        if (sentence.hasError()) {
				        	npc.say(sentence.getErrorString() + " Just tell me how much you want to risk, for example #'stake 50'.");
				        } else {
				        	final Behaviour behaviour = new Behaviour();

							behaviour.parseRequest(sentence);

							stake = behaviour.getAmount();

							if (stake < MIN_STAKE) {
								npc.say("You must stake at least " + MIN_STAKE
										+ " pieces of gold.");
							} else if (stake > MAX_STAKE) {
								npc.say("You can't stake more than "
										+ MAX_STAKE + " pieces of gold.");
							} else {
								if (player.drop("money", stake)) {
									startNewGame(player);
								} else {
									npc.say("Hey! You don't have enough money!");
								}
							}
						}
					}
				});
		ramon.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						dealCards(player, 1);
					}
				});

		// The player says he doesn't want to have another card.
		// Let the dealer give cards to the bank.
		ramon.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						playerStands = true;
						if (bankStands) {
							// Both stand. Let the dealer tell the final resul
							final String message = analyze(player);
							if (message != null) {
								ramon.say(message);
							}
						} else {
							letBankDrawAfterPause(player.getName());
						}
					}
				});
	}
	@Override
	public String getSlotName() {
		return "blackjack";
	}
}
