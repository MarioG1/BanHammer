/*******************************************************************************
 * Copyright (c) 2011 James Richardson.
 * 
 * BanHammer.java is part of BanHammer.
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
package name.richardson.james.bukkit.banhammer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import name.richardson.james.bukkit.banhammer.ban.BanCommand;
import name.richardson.james.bukkit.banhammer.ban.CheckCommand;
import name.richardson.james.bukkit.banhammer.ban.HistoryCommand;
import name.richardson.james.bukkit.banhammer.ban.PardonCommand;
import name.richardson.james.bukkit.banhammer.ban.PlayerListener;
import name.richardson.james.bukkit.banhammer.ban.PurgeCommand;
import name.richardson.james.bukkit.banhammer.ban.RecentCommand;
import name.richardson.james.bukkit.banhammer.kick.KickCommand;
import name.richardson.james.bukkit.banhammer.management.ExportCommand;
import name.richardson.james.bukkit.banhammer.management.ImportCommand;
import name.richardson.james.bukkit.banhammer.management.ReloadCommand;
import name.richardson.james.bukkit.util.Logger;
import name.richardson.james.bukkit.util.Plugin;
import name.richardson.james.bukkit.util.command.CommandManager;

public class BanHammer extends Plugin {

  private long maximumTemporaryBan;
  private static ResourceBundle messages;
  private final static Locale locale = Locale.getDefault();
  private CommandManager cm;
  
  private PlayerListener playerListener;
  private PluginDescriptionFile desc;
  private PluginManager pm;
  private BanHammerConfiguration configuration;
  private DatabaseHandler database;
  
  private final HashSet<String> bannedPlayerNames = new HashSet<String>();


  /**
   * This returns a localised string from the loaded ResourceBundle.
   * 
   * @param key The key for the desired string.
   * @return The string for the given key.
   */
  public static String getMessage(String key) {
    return messages.getString(key);
  }
  
  @Override
  public List<Class<?>> getDatabaseClasses() {
    List<Class<?>> list = new ArrayList<Class<?>>();
    list.add(BanRecord.class);
    return list;
  }

  /**
   * This returns a handler to allow access to the BanHammer API.
   * 
   * @return A new BanHandler instance.
   */
  public BanHandler getHandler(Class<?> parentClass) {
    return new BanHandler(parentClass, this);
  }

  public long getMaximumTemporaryBan() {
    return maximumTemporaryBan;
  }

  @Override
  public void onDisable() {
    logger.info(String.format(messages.getString("plugin-disabled"), this.desc.getName()));
  }

  @Override
  public void onEnable() {
    this.desc = this.getDescription();
    this.pm = this.getServer().getPluginManager();

    try {
      this.logger.setPrefix("[BanHammer] ");
      this.setupLocalisation();
      this.loadConfiguration();
      this.setupDatabase();
      this.loadBans();
      this.setPermission();
      this.registerListeners();
      this.registerCommands();
    } catch (final IOException exception) {
      this.logger.severe("Unable to load configuration!");
      exception.printStackTrace();
    } catch (SQLException exception) {
      exception.printStackTrace();
    } finally {
      if (!this.getServer().getPluginManager().isPluginEnabled(this)) return;
    }

    logger.info(String.format(BanHammer.getMessage("plugin-enabled"), this.desc.getFullName()));
  }

  private void loadBans() {
    bannedPlayerNames.clear();
    for (Object record : this.database.list(BanRecord.class)) {
      BanRecord ban = (BanRecord) record;
      if (ban.isActive()) this.bannedPlayerNames.add(ban.getPlayer());
    }
    logger.info(String.format("%d banned names loaded.", bannedPlayerNames.size()));
  }

  public void setMaximumTemporaryBan(long maximumTemporaryBan) {
    this.maximumTemporaryBan = maximumTemporaryBan;
  }

  private void loadConfiguration() throws IOException {
    configuration = new BanHammerConfiguration(this);
    if (configuration.isDebugging()) {
      Logger.enableDebugging(this.getDescription().getName().toLowerCase());
    }
  }

  private void registerCommands() {
    final CommandManager cm = new CommandManager(this.getDescription());
    this.getCommand("bh").setExecutor(cm);
    this.getCommand("ban").setExecutor(new BanCommand(this));
    this.getCommand("kick").setExecutor(new KickCommand(this));
    this.getCommand("pardon").setExecutor(new PardonCommand(this));
    this.cm.registerCommand("check", new CheckCommand(this));
    this.cm.registerCommand("export", new ExportCommand(this));
    this.cm.registerCommand("history", new HistoryCommand(this));
    this.cm.registerCommand("import", new ImportCommand(this));
    this.cm.registerCommand("purge", new PurgeCommand(this));
    this.cm.registerCommand("recent", new RecentCommand(this));
    this.cm.registerCommand("reload", new ReloadCommand(this));
  }
  
  public DatabaseHandler getDatabaseHandler() {
    return this.database;
  }
  
  private void registerListeners() {
    this.playerListener = new PlayerListener();
    this.pm.registerEvent(Event.Type.PLAYER_LOGIN, this.playerListener, Event.Priority.Highest, this);
  }

  private void setupDatabase() throws SQLException {
    try {
      this.getDatabase().find(BanRecord.class).findRowCount();
    } catch (PersistenceException ex) {
      logger.warning(BanHammer.getMessage("no-database"));
      this.installDDL();
    }
    this.database = new DatabaseHandler(this.getDatabase());
  }

  private void setupLocalisation() {
    BanHammer.messages = ResourceBundle.getBundle("name.richardson.james.banhammer.localisation.Messages", locale);
  }

  public void reloadBannedPlayers() {
    this.loadBans();
  }
  
  public Set<String> getBannedPlayers() {
    return Collections.unmodifiableSet(this.bannedPlayerNames);
  }

}