package games.stendhal.server.maps.semos.bank;

import games.stendhal.common.Direction;
import games.stendhal.common.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.MovementListener;
import games.stendhal.server.core.rule.defaultruleset.DefaultActionManager;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.PersonalChest;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.deathmatch.Spot;

import java.awt.geom.Rectangle2D;
import java.util.Set;

public class CustomerAdvisorNPC extends SpeakerNPCFactory {

	private final class VaultChatAction implements ChatAction {
		private final class VaultMovementListener implements
				MovementListener {
			public Rectangle2D getArea() {
				return new Rectangle2D.Double(0, 0, 100, 100);
			}

			public void onEntered(final ActiveEntity entity, final StendhalRPZone zone, final int newX,
					final int newY) {

			}

			public void onExited(final ActiveEntity entity, final StendhalRPZone zone, final int oldX,
					final int oldY) {
			    if(zone.getPlayers().size() == 1) {
				final Player postman = SingletonRepository.getRuleProcessor().getPlayer(
									"postman");
				Set<Item> itemsOnGround = zone.getItemsOnGround();
				for (Item item : itemsOnGround) {
					boolean equippedToBag = DefaultActionManager.getInstance().onEquip((RPEntity) entity, "bag", item);
					if (equippedToBag) {
					    // player may not have been online so use postman to send info message
					    if (postman != null) {
						postman.sendPrivateText("tell " + ((RPEntity) entity).getName() + " The " 
									+ Grammar.quantityplnoun(item.getQuantity(), item.getName()) 
									+ " which you left on the floor in the vault have been automatically "
									+ "returned to your bag.");
					    }
					} else {
					    boolean equippedToBank = DefaultActionManager.getInstance().onEquip((RPEntity) entity, "bank", item);
					    if (equippedToBank) {
						if (postman != null) {
						    postman.sendPrivateText("tell " + ((RPEntity) entity).getName() + " The "
									    + Grammar.quantityplnoun(item.getQuantity(), item.getName()) 
									    + " which you left on the floor in the vault have been automatically "
									    + "returned to your bank chest." );
						}	
					    } else {
						// the player lost their items
						if (postman != null) {
						    postman.sendPrivateText("tell " + ((RPEntity) entity).getName() + " The "
									    + Grammar.quantityplnoun(item.getQuantity(), item.getName()) 
									    + " which you left on the floor in the vault have been thrown into "
									    + "the void, because there was no space to fit them into either your "
									    + "bank chest or your bag." );
						} }
					}
				}
				SingletonRepository.getRPWorld().removeZone(zone);
			    }
			}

			public void onMoved(final ActiveEntity entity, final StendhalRPZone zone, final int oldX,
					final int oldY, final int newX, final int newY) {

			}
		}

		public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
			final StendhalRPZone vaultzone = (StendhalRPZone) SingletonRepository
					.getRPWorld().getRPZone("int_vault");
			final StendhalRPZone zone = StendhalRPZone.fillContent(player
					.getName()
					+ "_vault", vaultzone);
			
			zone.addMovementListener(new VaultMovementListener());
			PersonalChest chest = new PersonalChest();
			chest.setPosition(3, 2);
			Portal portal = new Teleporter(new Spot(player.getZone(), player.getX(), player.getY()));
			portal.setPosition(3, 6);
			zone.add(portal);
			zone.add(chest);
			SingletonRepository.getRPWorld().addRPZone(zone);
			player.teleport(zone, 3, 5, Direction.UP, player);
			
		}
	}

	@Override
	public void createDialog(final SpeakerNPC npc) {
		npc.addGreeting("Welcome to the bank of Semos! I am here to #help you manage your personal chest.");
		npc.addHelp("Follow the corridor to the right, and you will find the magic chests. You can store your belongings in any of them, and nobody else will be able to touch them! A number of spells have been cast on the chest areas to ensure #safety.");
		npc.addReply("safety", "When you are standing at a chest to organise your items, any other people or animals will not be able to come near you. A magical aura stops others from using scrolls to arrive near you, although unfortunately this also means you cannot use scrolls to exit the bank. You will need to walk out. Lastly let me tell you about safe #trading.");
		npc.addReply("trading", "There is a large table in the top right hand corner of this bank. It is designed so that trading can be done safely. Here is how to use it: Each take a chair and sit at opposite sides of the table. Once you have agreed to a trade, place up to 3 items at once on the 3 tiles directly adjacent to you on the table. Wait until the other person has done the same. Make sure you can see exactly what they have placed and how much of each item. Next is the #exchange.");
		npc.addReply("exchange", "When you are both ready, swap places. The narrow corridors are designed so that noone else can take the items you have placed. If someone gets in the way you can just go back and remove your items from the table until the area is clear again. If you don't understand anything, try asking another player for a demonstration. Oh, and by the way, we also have #security at the table.");
		npc.addReply("security", "Yes, there is a spell to make sure noone can return to this world next to the table. If they exit to the astral plane when standing by the table, and then attempt to return there, they are magically moved to a safer place. Good luck with your trading!");
		npc.addJob("I'm the Customer Advisor here at Semos Bank.");
		
		npc.addGoodbye("It was a pleasure to serve you.");
		npc.add(ConversationStates.ANY, "vault", new QuestCompletedCondition("armor_dagobert"), ConversationStates.IDLE, null, 
				new VaultChatAction());
		
		npc.add(ConversationStates.ANY, "vault", new QuestNotCompletedCondition("armor_dagobert"), ConversationStates.ATTENDING, "Perhaps you could do a #favour for me, and then I will tell you about the private banking vaults.", null);
		
		// remaining behaviour defined in games.stendhal.server.maps.quests.ArmorForDagobert	
	}

	
}
