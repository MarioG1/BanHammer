/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 *
 * AuditCommand.java is part of BanHammer.
 *
 * BanHammer is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * BanHammer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BanHammer. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.bukkit.banhammer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.argument.Argument;
import name.richardson.james.bukkit.utilities.command.argument.PlayerNamePositionalArgument;
import name.richardson.james.bukkit.utilities.formatters.ChoiceFormatter;

import name.richardson.james.bukkit.banhammer.ban.BanRecord;
import name.richardson.james.bukkit.banhammer.ban.BanRecordManager;
import name.richardson.james.bukkit.banhammer.ban.PlayerRecord;
import name.richardson.james.bukkit.banhammer.ban.PlayerRecordManager;
import name.richardson.james.bukkit.banhammer.utilities.formatters.BanCountChoiceFormatter;
import name.richardson.james.bukkit.banhammer.utilities.localisation.BanHammerLocalisation;

import static name.richardson.james.bukkit.banhammer.utilities.localisation.BanHammerLocalisation.*;
import static name.richardson.james.bukkit.utilities.localisation.PluginLocalisation.COMMAND_NO_PERMISSION;

public final class AuditCommand extends AbstractCommand {

	public static final String PERMISSION_ALL = "banhammer.audit";
	public static final String PERMISSION_SELF = "banhammer.audit.self";
	public static final String PERMISSION_OTHERS = "banhammer.audit.others";

	private final BanRecordManager banRecordManager;
	private final ChoiceFormatter choiceFormatter;
	private final Argument playerName;
	private final PlayerRecordManager playerRecordManager;

	public AuditCommand(PlayerRecordManager playerRecordManager, BanRecordManager banRecordManager) {
		super(AUDIT_COMMAND_NAME, AUDIT_COMMAND_DESC);
		this.playerRecordManager = playerRecordManager;
		this.banRecordManager = banRecordManager;
		this.playerName = PlayerNamePositionalArgument.getInstance(playerRecordManager, 0, false, PlayerRecordManager.PlayerStatus.CREATOR);
		addArgument(playerName);
		this.choiceFormatter = new BanCountChoiceFormatter();
		this.choiceFormatter.setMessage(getLocalisation().formatAsHeaderMessage(AUDIT_COMMAND_HEADER));
	}

	@Override
	protected void execute() {
		String playerName = (this.playerName.getString() == null) ? getContext().getCommandSender().getName() : this.playerName.getString();
		List<String> messages = new ArrayList<String>();
		if (!hasPermission(getContext().getCommandSender(), playerName)) {
			messages.add(getLocalisation().formatAsErrorMessage(COMMAND_NO_PERMISSION));
		} else {
			if (playerRecordManager.exists(playerName)) {
				PlayerRecord record = playerRecordManager.find(playerName);
				AuditSummary auditSummary = new AuditSummary(record.getCreatedBans(), banRecordManager.count());
				choiceFormatter.setArguments(auditSummary.getTotalBanCount(), playerName, auditSummary.getTotalBanCountPercentage());
				messages.add(choiceFormatter.getMessage());
				messages.addAll(auditSummary.getMessages());
			} else {
				messages.add(getLocalisation().formatAsInfoMessage(PLAYER_HAS_MADE_NO_BANS, playerName));
			}
		}
		getContext().getCommandSender().sendMessage(messages.toArray(new String[messages.size()]));
	}

	private boolean hasPermission(final CommandSender sender, String targetName) {
		final boolean isSenderCheckingSelf = targetName.equalsIgnoreCase(sender.getName());
		return sender.hasPermission(PERMISSION_SELF) && isSenderCheckingSelf || sender.hasPermission(PERMISSION_OTHERS) && !isSenderCheckingSelf;
	}

	@Override
	public boolean isAuthorised(Permissible permissible) {
		return permissible.hasPermission(PERMISSION_ALL) || permissible.hasPermission(PERMISSION_OTHERS) || permissible.hasPermission(PERMISSION_SELF);
	}

	@Override
	public boolean isAsynchronousCommand() {
		return false;
	}

	public final class AuditSummary {

		private final List<BanRecord> bans;
		private int expiredBans;
		private int normalBans;
		private int pardonedBans;
		private int permanentBans;
		private int temporaryBans;
		private int total;

		private AuditSummary(List<BanRecord> bans, int total) {
			this.bans = bans;
			this.total = total;
			this.update();
		}

		private void update() {
			for (BanRecord record : bans) {
				switch (record.getType()) {
					case PERMANENT:
						permanentBans++;
						break;
					case TEMPORARY:
						temporaryBans++;
						break;
				}
				switch (record.getState()) {
					case NORMAL:
						normalBans++;
						break;
					case PARDONED:
						pardonedBans++;
						break;
					case EXPIRED:
						expiredBans++;
						break;
				}
			}
		}

		public List<String> getMessages() {
			List<String> messages = new ArrayList<String>();
			messages.add(getLocalisation().formatAsHeaderMessage(BanHammerLocalisation.AUDIT_TYPE_SUMMARY));
			messages.add(getLocalisation().formatAsInfoMessage(BanHammerLocalisation.AUDIT_PERMANENT_BANS_PERCENTAGE, getPermanentBanCount(), getPardonedBanCountPercentage()));
			messages.add(getLocalisation().formatAsInfoMessage(BanHammerLocalisation.AUDIT_TEMPORARY_BANS_PERCENTAGE, getTemporaryBanCount(), getTemporaryBanCountPercentage()));
			messages.add(getLocalisation().formatAsHeaderMessage(BanHammerLocalisation.AUDIT_STATUS_SUMMARY));
			messages.add(getLocalisation().formatAsInfoMessage(BanHammerLocalisation.AUDIT_ACTIVE_BANS_PERCENTAGE, getNormalBanCount(), getNormalBanCountPercentage()));
			messages.add(getLocalisation().formatAsInfoMessage(BanHammerLocalisation.AUDIT_EXPIRED_BANS_PERCENTAGE, getExpiredBanCount(), getExpiredBanCountPercentage()));
			messages.add(getLocalisation().formatAsInfoMessage(BanHammerLocalisation.AUDIT_PARDONED_BANS_PERCENTAGE, getPardonedBanCount(), getPardonedBanCountPercentage()));
			return messages;
		}

		public float getTemporaryBanCountPercentage() {
			return (float) temporaryBans / this.bans.size();
		}

		public int getTemporaryBanCount() {
			return temporaryBans;
		}

		public int getPermanentBanCount() {
			return permanentBans;
		}

		public float getPardonedBanCountPercentage() {
			return (float) pardonedBans / this.bans.size();
		}

		public int getPardonedBanCount() {
			return pardonedBans;
		}

		public float getNormalBanCountPercentage() {
			return (float) normalBans / this.bans.size();
		}

		public int getNormalBanCount() {
			return normalBans;
		}

		public float getExpiredBanCountPercentage() {
			return (float) expiredBans / this.bans.size();
		}

		public int getExpiredBanCount() {
			return expiredBans;
		}

		public float getPermanentBanCountPercentage() {
			return (float) permanentBans / this.bans.size();
		}

		public int getTotalBanCount() {
			return this.bans.size();
		}

		public float getTotalBanCountPercentage() {
			return (float) this.bans.size() / total;
		}

	}
}
