/*******************************************************************************
 Copyright (c) 2014 James Richardson.

 BanHammerPlayerBannedEvent.java is part of BanHammer.

 BanHammer is free software: you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any
 later version.

 BanHammer is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 BanHammer. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.bukkit.banhammer.ban.event;

import java.util.Collection;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import name.richardson.james.bukkit.banhammer.model.BanRecord;

public final class BanHammerPlayerBannedEvent extends AbstractBanHammerPlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	public BanHammerPlayerBannedEvent(final Collection<BanRecord> records, final CommandSender commandSender, final boolean silent) {
		super(records, commandSender, silent);
	}

	@SuppressWarnings("UnusedDeclaration")
	public static HandlerList getHandlerList() {
		return BanHammerPlayerBannedEvent.handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return BanHammerPlayerBannedEvent.handlers;
	}

}