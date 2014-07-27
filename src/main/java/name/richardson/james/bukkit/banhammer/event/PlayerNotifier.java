package name.richardson.james.bukkit.banhammer.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import name.richardson.james.bukkit.utilities.listener.AbstractListener;
import name.richardson.james.bukkit.utilities.logging.PluginLoggerFactory;

import name.richardson.james.bukkit.banhammer.excluded.BanHammer;
import name.richardson.james.bukkit.banhammer.record.BanRecord;
import name.richardson.james.bukkit.banhammer.record.BanRecordFormatter;
import name.richardson.james.bukkit.banhammer.record.SimpleBanRecordFormatter;

import static name.richardson.james.bukkit.banhammer.utilities.localisation.BanHammerMessages.NOTIFY_PLAYER_BANNED;
import static name.richardson.james.bukkit.banhammer.utilities.localisation.BanHammerMessages.NOTIFY_PLAYER_PARDONED;

public class PlayerNotifier extends AbstractListener {

	private final Logger logger = PluginLoggerFactory.getLogger(PlayerNotifier.class);
	private final Server server;

	public PlayerNotifier(final Plugin plugin, final PluginManager pluginManager, final Server server) {
		super(plugin, pluginManager);
		this.server = server;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBanned(final BanHammerPlayerBannedEvent event) {
		if (event.isSilent()) return;
		for (BanRecord record : event.getRecords()) {
			BanRecordFormatter formatter = new SimpleBanRecordFormatter(record);
			String message = NOTIFY_PLAYER_BANNED.asInfoMessage(record.getPlayer().getLastKnownName(), record.getCreator().getLastKnownName());
			server.broadcast(message, BanHammer.NOTIFY_PERMISSION_NAME);
			server.broadcast(formatter.getReason(), BanHammer.NOTIFY_PERMISSION_NAME);
			server.broadcast(formatter.getLength(), BanHammer.NOTIFY_PERMISSION_NAME);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPardoned(final BanHammerPlayerPardonedEvent event) {
		logger.log(Level.FINER, "Received " + event.getEventName());
		if (event.isSilent()) return;
		for (BanRecord record : event.getRecords()) {
			String message = NOTIFY_PLAYER_PARDONED.asInfoMessage(record.getPlayer().getLastKnownName(), event.getCommandSender());
			server.broadcast(message, BanHammer.NOTIFY_PERMISSION_NAME);
		}
	}

}
