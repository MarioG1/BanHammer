package name.richardson.james.bukkit.banhammer.record;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.avaje.ebean.validation.NotNull;
import org.apache.commons.lang.Validate;

import name.richardson.james.bukkit.banhammer.utilities.NameFetcher;
import name.richardson.james.bukkit.banhammer.utilities.UUIDFetcher;

@Entity
@Table(name = "banhammer_players")
public class CurrentPlayerRecord extends SimpleRecord implements PlayerRecord {

	@OneToMany(mappedBy = "player", targetEntity = CurrentBanRecord.class, cascade = {CascadeType.REMOVE})
	private List<CurrentBanRecord> bans;
	@OneToMany(mappedBy = "creator", targetEntity = CurrentBanRecord.class)
	private List<CurrentBanRecord> createdBans;
	@Id
	@GeneratedValue
	private long id;
	@NotNull
	private String lastKnownName;
	private UUID uuid;

	protected CurrentPlayerRecord() {}

	protected CurrentPlayerRecord(UUID uuid) {
		final Timestamp now = new Timestamp(System.currentTimeMillis());
		final String name = NameFetcher.getNameOf(uuid);
		this.setLastKnownName(name);
		this.setUuid(uuid);
		this.setCreatedAt(now);
	}

	protected CurrentPlayerRecord(String playerName) {
		Validate.notNull(playerName);
		final Timestamp now = new Timestamp(System.currentTimeMillis());
		UUID uuid = UUIDFetcher.getUUIDOf(playerName);
		this.setLastKnownName(playerName);
		this.setUuid(uuid);
		this.setCreatedAt(now);
	}

	@Override public BanRecord getActiveBan() {
		for (BanRecord record : this.getBans()) {
			if (record.getState() == BanRecord.State.NORMAL) return record;
		}
		return null;
	}

	@Override public List<BanRecord> getBans() {
		List<BanRecord> bans = new ArrayList<BanRecord>();
		if (this.bans != null) bans.addAll(bans);
		return bans;
	}

	@Override public List<BanRecord> getCreatedBans() {
		List<BanRecord> bans = new ArrayList<BanRecord>();
		if (this.createdBans != null) bans.addAll(createdBans);
		return bans;
	}

	@Override public long getId() {
		return id;
	}

	@Override public String getLastKnownName() {
		return lastKnownName;
	}

	@Override public UUID getUuid() {
		return uuid;
	}

	@Override public boolean isBanned() {
		for (BanRecord record : this.getBans()) {
			if (record.getState() == BanRecord.State.NORMAL) return true;
		}
		return false;
	}

	@Override public void setBans(final Set<BanRecord> bans) {
		this.bans.clear();
		for (BanRecord ban : bans) {
			if (ban instanceof CurrentBanRecord) this.bans.add((CurrentBanRecord) ban);
		}
	}

	@Override public void setCreatedBans(final Set<BanRecord> createdBans) {
		this.createdBans.clear();
		for (BanRecord ban : bans) {
			if (ban instanceof CurrentBanRecord) this.createdBans.add((CurrentBanRecord) ban);
		}
	}

	@Override public void setId(long id) {
		this.id = id;
	}

	@Override public void setLastKnownName(final String lastKnownName) {
		this.lastKnownName = lastKnownName;
	}

	@Override public void setUuid(final UUID uuid) {
		this.uuid = uuid;
	}

}