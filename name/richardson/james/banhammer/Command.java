/*******************************************************************************
 * Copyright (c) 2011 James Richardson.
 * 
 * Command.java is part of BanHammer.
 * 
 * BanHammer is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 * 
 * BanHammer is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with BanHammer.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.banhammer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import name.richardson.james.banhammer.ban.BanRecord;
import name.richardson.james.banhammer.util.BanHammerTime;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public abstract class Command implements CommandExecutor {

  protected static final String CONSOLE_NAME = "Console";
  
  protected String description;
  protected String name;
  protected String permission;
  protected BanHammer plugin;
  protected String usage;

  public Command(BanHammer plugin) {
    this.plugin = plugin;
  }
  
  public abstract void execute(CommandSender sender, Map<String, String> arguments);

  @Override
  public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
    if (!this.authorisePlayer(sender)) {
      sender.sendMessage(ChatColor.RED + BanHammer.getMessage("no-permission"));
      return true;
    }
    
    try {
      LinkedList<String> arguments = new LinkedList<String>();
      arguments.addAll(Arrays.asList(args));
      final Map<String, String> parsedArguments = this.parseArguments(arguments);  
      this.execute(sender, parsedArguments);
    } catch (final IllegalArgumentException e) {
      if (e instanceof NumberFormatException) {
        sender.sendMessage(ChatColor.RED + BanHammer.getMessage("invalid-time-format"));
        sender.sendMessage(ChatColor.YELLOW + BanHammer.getMessage("invalid-time-format-hint"));
      } else {
        sender.sendMessage(ChatColor.RED + BanHammer.getMessage("not-enough-arguments"));
        sender.sendMessage(ChatColor.YELLOW + this.usage);
      } 
    } 
      
    return true;
  }

  /**
   * Check to see if a player has permission to use this command.
   * 
   * A console user is permitted to use all commands by default.
   * 
   * @param sender The player/console that is attempting to use the command
   * @return true if the player has permission; false otherwise.
   */
  protected boolean authorisePlayer(CommandSender sender) {
    if (sender instanceof ConsoleCommandSender) {
      return true;
    } else if (sender instanceof Player) {
      final Player player = (Player) sender;
      if (player.hasPermission(this.permission) || player.hasPermission("banhammer.*")) {
        return true;
      }
    } 
    return false;
  }

  
  /**
   * Combine an array of strings together into one string. 
   * 
   * Trailing separators are removed.
   * 
   * @param arguments A list of strings to append together
   * @param seperator A string that will appear between each appended string.
   * @return result The resulting string or "No reason specified" if an exception is raised.
   */
  protected String combineString(List<String> arguments, String seperator) {
    StringBuilder reason = new StringBuilder();
    try {
      for (String argument : arguments) {
        reason.append(argument);
        reason.append(seperator);
      } 
      reason.deleteCharAt(reason.length() - seperator.length());
      return reason.toString();
    } catch (StringIndexOutOfBoundsException e) {
      return BanHammer.getMessage("default-reason");
    }
  }
  
  /**
   * Attempt to match an Player by name. 
   * 
   * This method will attempt to match players that are currently
   * online. Failing that and if offline is true, it will return an
   * OfflinePlayer for the specified name. 
   * 
   * @param playerName The name to match.
   * @param offline If true and no match was found, return an OfflinePlayer for the same name.
   * @return result Player that matches that name.
   * @throws NoMatchingPlayerException - If the number of matches is not equal to 1.
   */
  protected Player getPlayer(String playerName) {
    List<Player> playerList = this.plugin.getServer().matchPlayer(playerName);
    if (playerList.size() == 1) {
      return playerList.get(0);
    }
    return null;
  }
  
  protected OfflinePlayer getOfflinePlayer(String playerName) {
    return this.plugin.getServer().getOfflinePlayer(playerName);
  }
  
  /**
   * Get the name of a CommandSender.
   * 
   * By default a CommandSender which is not a Player has no name. In this case
   * the method will return the value of consoleName.
   * 
   * @param sender The CommandSender that you wish to resolve the name of.
   * @return name Return the name of the Player or "Console" if no name available.
   */
  protected String getSenderName(CommandSender sender) {
    if (sender instanceof ConsoleCommandSender) {
      return Command.CONSOLE_NAME;
    } else {
      final Player player = (Player) sender;
      return player.getName();
    }
  }    

  protected void sendBanDetail(CommandSender sender, BanRecord ban) {
    Date createdDate = new Date(ban.getCreatedAt());
    DateFormat dateFormat = new SimpleDateFormat("MMM d");
    String createdAt = dateFormat.format(createdDate);
    sender.sendMessage(String.format(ChatColor.YELLOW + BanHammer.getMessage("ban-history-detail:"), ban.getCreatedBy(), createdAt));
    sender.sendMessage(String.format(ChatColor.YELLOW + BanHammer.getMessage("ban-history-reason:"), ban.getReason()));
    switch (ban.getType()) {
      case PERMENANT:
        sender.sendMessage(ChatColor.YELLOW + BanHammer.getMessage("ban-history-time-permanent:"));
        break;
      case TEMPORARY:
        Date expiryDate = new Date(ban.getExpiresAt());
        DateFormat expiryDateFormat = new SimpleDateFormat("MMM d H:mm a ");
        String expiryDateString = expiryDateFormat.format(expiryDate) + "(" + Calendar.getInstance().getTimeZone().getDisplayName() + ")";
        Long banTime = ban.getExpiresAt() - ban.getCreatedAt();
        sender.sendMessage(String.format(ChatColor.YELLOW + BanHammer.getMessage("ban-history-time-temporary:"), BanHammerTime.millisToLongDHMS(banTime)));
        sender.sendMessage(String.format(ChatColor.YELLOW + BanHammer.getMessage("ban-history-expires-on:"), expiryDateString));
        break;
    }
  }
  
  protected void registerPermission(final String name, final String description, final PermissionDefault defaultValue) {
    final Permission permission = new Permission(name, description, defaultValue);
    plugin.getServer().getPluginManager().addPermission(permission);
  }
  
  protected abstract Map<String, String> parseArguments(List<String> arguments);

}
