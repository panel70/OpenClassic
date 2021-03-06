package ch.spacebase.openclassic.client.network.handler;

import ch.spacebase.openclassic.api.network.msg.PlayerOpMessage;
import ch.spacebase.openclassic.api.player.Player;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.game.network.ClassicSession;
import ch.spacebase.openclassic.game.network.MessageHandler;

public class PlayerOpMessageHandler extends MessageHandler<PlayerOpMessage> {

	@Override
	public void handle(ClassicSession session, Player player, PlayerOpMessage message) {
		((ClientPlayer) player).getHandle().userType = message.getOp();
	}

}
