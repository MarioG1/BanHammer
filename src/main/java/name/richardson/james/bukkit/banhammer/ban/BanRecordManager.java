package name.richardson.james.bukkit.banhammer.ban;

import java.util.Arrays;
import java.util.List;

import com.avaje.ebean.EbeanServer;

public class BanRecordManager {

	private EbeanServer database;

	public BanRecordManager(EbeanServer database) {
		if (database == null) throw new IllegalArgumentException();
		this.database = database;
	}

	public void delete(BanRecord ban) {
		this.delete(Arrays.asList(ban));
	}

	public int delete(List<BanRecord> bans) {
		return this.database.delete(bans);
	}

	public boolean save(BanRecord record) {
		if (record.getPlayer().isBanned()) return false;
		this.database.save(record);
		return true;
	}

	public List<BanRecord> list() {
		return this.database.find(BanRecord.class).setUseCache(true).findList();
	}

	public List<BanRecord> list(int limit) {
		return this.database.find(BanRecord.class).setUseCache(true).setMaxRows(limit).findList();
	}

	public int count() {
		return this.database.find(BanRecord.class).setUseCache(true).setReadOnly(true).findRowCount();
	}
}
