package name.richardson.james.bukkit.banhammer.ban;

import java.sql.Timestamp;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class BanRecordTest extends TestCase {

	private OldBanRecord record;

	@Test
	public void testToString()
	throws Exception {
		Assert.assertTrue("to String is not overriden.", record.toString().contains(OldBanRecord.class.getSimpleName()));
	}

	@Test
	public void testBanHasExpired() {
		record.setState(OldBanRecord.State.NORMAL);
		record.setExpiresAt(new Timestamp(System.currentTimeMillis() - 10000));
		Assert.assertEquals("Ban should have expired by now.", OldBanRecord.State.EXPIRED, record.getState());
		record.setExpiresAt(new Timestamp(System.currentTimeMillis() + 10000));
		record.setState(OldBanRecord.State.NORMAL);
		Assert.assertEquals("Ban should not have expired.", OldBanRecord.State.NORMAL, record.getState());
		System.out.print(record.toString());
	}

	@Test
	public void testGetTemporaryType()
	throws Exception {
		record.setExpiresAt(new Timestamp(System.currentTimeMillis()));
		Assert.assertEquals("When the expiry time greater than 0, the ban should be temporary.", OldBanRecord.Type.TEMPORARY, record.getType());
	}

	@Test
	public void testGetPermanentType()
	throws Exception {
		Assert.assertEquals("When the expiry time is null, the ban should be permanent.", OldBanRecord.Type.PERMANENT, record.getType());
	}

	@Test
	public void testSetState()
	throws Exception {
		OldBanRecord.State state = OldBanRecord.State.PARDONED;
		record.setState(state);
		Assert.assertEquals("State is inconsistent!", state, record.getState());
	}

	@Test
	public void testSetReason()
	throws Exception {
		String reason = "This is a reason";
		record.setReason(reason);
		Assert.assertEquals("Reason is inconsistent!", reason, record.getReason());
	}

	@Test
	public void testSetPlayer()
	throws Exception {
		PlayerRecord playerRecord = new OldPlayerRecord();
		record.setPlayer(playerRecord);
		Assert.assertEquals("Player is inconsistent!", playerRecord, record.getPlayer());
	}

	@Test
	public void testSetId()
	throws Exception {
		int id = 1;
		record.setId(id);
		Assert.assertEquals("Id is inconsistent!", id, record.getId());
	}

	@Test
	public void testSetExpiresAt()
	throws Exception {
		Timestamp time = new Timestamp(System.currentTimeMillis());
		record.setExpiresAt(time);
		Assert.assertEquals("Timestamp is inconsistent!", time, record.getExpiresAt());
	}

	@Test
	public void testSetCreator()
	throws Exception {
		PlayerRecord playerRecord = new OldPlayerRecord();
		record.setCreator(playerRecord);
		Assert.assertEquals("Creator is inconsistent!", playerRecord, record.getCreator());
	}

	@Test
	public void testSetCreatedAt()
	throws Exception {
		Timestamp time = new Timestamp(System.currentTimeMillis());
		record.setCreatedAt(time);
		Assert.assertEquals("Timestamp is inconsistent!", time, record.getCreatedAt());
	}

	@Before
	public void setUp()
	throws Exception {
		record = new OldBanRecord();
	}

}
