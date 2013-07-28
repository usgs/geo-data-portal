package gov.usgs.cida.gdp.dataaccess.bean;

import gov.usgs.cida.gdp.dataaccess.bean.Time.TimeBreakdown;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache.CacheIdentifier;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author isuftin
 */
public class TimeTest {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.debug("Started testing class.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.debug("Ended testing class.");
	}

	@Test
	public void testCreateTime() {
		Time result = new Time();
		assertNotNull(result);
	}

	@Test
	public void testCreateTimeWithDateRangeStringList() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time result = null;
		try {
			result = new Time(input);
		} catch (ParseException ex) {
			Logger.getLogger(TimeTest.class.getName()).log(Level.SEVERE, null, ex);
			fail(ex.getMessage());
		}
		assertNotNull(result.getStarttime().toString());
		assertTrue(result.getStarttime().getMonth() == 7);
	}

	@Test
	public void testCreateTimeJSON() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time bean = null;
		try {
			bean = new Time(input);
		} catch (ParseException ex) {
			fail(ex.getMessage());
		}

		String result = bean.toJSON();
		assertNotNull(result);
		assertTrue(result.contains("\"time\":[\"2001-07-01T01:01:01Z\",\"2002-07-15T01:01:01Z\"]"));
	}

	@Test
	public void testCreateTimeXML() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time bean = null;
		try {
			bean = new Time(input);
		} catch (ParseException ex) {
			fail(ex.getMessage());
		}

		String result = bean.toXML();
		assertNotNull(result);
	}

	@Test
	public void testCreateTimeString() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time bean = null;
		try {
			bean = new Time(input);
		} catch (ParseException ex) {
			fail(ex.getMessage());
		}

		String result = bean.toString();
		assertNotNull(result);
		assertEquals(result, "2001-07-01T01:01:01Z|2002-07-15T01:01:01Z");
	}

	@Test
	public void testCreateTimeWithEmptyRangeStringList() {
		String[] input = new String[]{};
		Time result = null;
		try {
			result = new Time(input);
		} catch (ParseException ex) {
			Logger.getLogger(TimeTest.class.getName()).log(Level.SEVERE, null, ex);
			fail(ex.getMessage());
		}
		assertNotNull(result.getStarttime().toString());
		assertTrue(result.getStarttime().getHour() == 0);
	}

	@Test
	public void testTimeBreakDownCreateWithCalendar() {
		Calendar cal = new GregorianCalendar();
		TimeBreakdown tbd = new Time.TimeBreakdown(cal);
		assertNotNull(tbd);
	}

	@Test
	public void testTimeBreakDownWithParseException() {
		try {
			TimeBreakdown tbd = new Time.TimeBreakdown("unparseable string");
		} catch (Exception ex) {
			assertEquals(ex.getClass(), ParseException.class);
		}
	}

	@Test
	public void testSetGetStartTime() {
		Calendar cal = new GregorianCalendar();
		TimeBreakdown tbd = new Time.TimeBreakdown(cal);
		Time target = new Time();
		target.setStarttime(tbd);
		assertNotNull(target.getStarttime());
	}

	@Test
	public void testSetGetEndTime() {
		Calendar cal = new GregorianCalendar();
		TimeBreakdown tbd = new Time.TimeBreakdown(cal);
		Time target = new Time();
		target.setEndtime(tbd);
		assertNotNull(target.getEndtime());
	}

	@Test
	public void testTimeBreakdownSetGetMonth() {
		TimeBreakdown target = new TimeBreakdown();
		target.setMonth(1);
		assertEquals(target.getMonth(), 1);
	}

	@Test
	public void testTimeBreakdownSetGetDay() {
		TimeBreakdown target = new TimeBreakdown();
		target.setDay(1);
		assertEquals(target.getDay(), 1);
	}

	@Test
	public void testTimeBreakdownSetGetYear() {
		TimeBreakdown target = new TimeBreakdown();
		target.setYear(1);
		assertEquals(target.getYear(), 1);
	}

	@Test
	public void testTimeBreakdownSetGetTimeZone() {
		TimeBreakdown target = new TimeBreakdown();
		target.setTimezone(1);
		assertEquals(target.getTimezone(), 1);
	}

	@Test
	public void testTimeBreakdownSetGetHour() {
		TimeBreakdown target = new TimeBreakdown();
		target.setHour(1);
		assertEquals(target.getHour(), 1);
	}

	@Test
	public void testTimeBreakdownSetGetMinute() {
		TimeBreakdown target = new TimeBreakdown();
		target.setMinute(1);
		assertEquals(target.getMinute(), 1);
	}

	@Test
	public void testTimeBreakdownSetGetSecond() {
		TimeBreakdown target = new TimeBreakdown();
		target.setSecond(1);
		assertEquals(target.getSecond(), 1);
	}

	@Test
	public void testTimeWriteToCache() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time bean = null;
		try {
			bean = new Time(input);
		} catch (ParseException ex) {
			fail(ex.getMessage());
		}

		CacheIdentifier cacheId = null;
		try {
			cacheId = new ResponseCache.CacheIdentifier("http://not-a-r-eal-url.gov", ResponseCache.CacheIdentifier.CacheType.TIME_RANGE, "Dummy cache data");
			boolean didWrite = bean.writeToCache(cacheId);
			assertTrue(didWrite);
		} finally {
			if (cacheId != null) {
				try {
					FileUtils.deleteQuietly(cacheId.getFile());
				} catch (IOException ex) {
					// Not handled
				}
			}
		}
	}

	@Test
	public void testReadFromCache() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time inputBean = null;
		try {
			inputBean = new Time(input);
		} catch (ParseException ex) {
			fail(ex.getMessage());
		}

		CacheIdentifier cacheId = null;
		try {
			cacheId = new ResponseCache.CacheIdentifier("http://not-a-r-eal-url.gov", ResponseCache.CacheIdentifier.CacheType.TIME_RANGE, "Dummy cache data");
			boolean didWrite = inputBean.writeToCache(cacheId);
			assertTrue(didWrite);

			Time resultBean = Time.buildFromCache(cacheId);
			assertNotNull(resultBean);
		} finally {
			if (cacheId != null) {
				try {
					FileUtils.deleteQuietly(cacheId.getFile());
				} catch (IOException ex) {
					// Not handled
				}
			}
		}
	}

	@Test
	public void testReadFromCacheEquality() {
		String[] input = new String[]{"2001-07-01T01:01:01Z", "2002-07-15T01:01:01Z"};
		Time inputBean = null;
		try {
			inputBean = new Time(input);
		} catch (ParseException ex) {
			fail(ex.getMessage());
		}

		CacheIdentifier cacheId = null;
		try {
			cacheId = new ResponseCache.CacheIdentifier("http://not-a-r-eal-url.gov", ResponseCache.CacheIdentifier.CacheType.TIME_RANGE, "Dummy cache data");
			boolean didWrite = inputBean.writeToCache(cacheId);
			assertTrue(didWrite);

			Time resultBean = Time.buildFromCache(cacheId);
			assertNotNull(resultBean);
			
			assertEquals(inputBean.getTime()[0], resultBean.getTime()[0]);
			assertEquals(inputBean.getTime()[1], resultBean.getTime()[1]);
			assertEquals(inputBean.getStarttime().getDay(), resultBean.getStarttime().getDay());
			assertEquals(inputBean.getStarttime().getHour(), resultBean.getStarttime().getHour());
			assertEquals(inputBean.getStarttime().getMinute(), resultBean.getStarttime().getMinute());
			assertEquals(inputBean.getStarttime().getMonth(), resultBean.getStarttime().getMonth());
			assertEquals(inputBean.getStarttime().getSecond(), resultBean.getStarttime().getSecond());
			assertEquals(inputBean.getStarttime().getTimezone(), resultBean.getStarttime().getTimezone());
			assertEquals(inputBean.getStarttime().getYear(), resultBean.getStarttime().getYear());
			assertEquals(inputBean.getEndtime().getDay(), resultBean.getEndtime().getDay());
			assertEquals(inputBean.getEndtime().getHour(), resultBean.getEndtime().getHour());
			assertEquals(inputBean.getEndtime().getMinute(), resultBean.getEndtime().getMinute());
			assertEquals(inputBean.getEndtime().getMonth(), resultBean.getEndtime().getMonth());
			assertEquals(inputBean.getEndtime().getSecond(), resultBean.getEndtime().getSecond());
			assertEquals(inputBean.getEndtime().getTimezone(), resultBean.getEndtime().getTimezone());
			assertEquals(inputBean.getEndtime().getYear(), resultBean.getEndtime().getYear());
		} finally {
			if (cacheId != null) {
				try {
					FileUtils.deleteQuietly(cacheId.getFile());
				} catch (IOException ex) {
					// Not handled
				}
			}
		}
	}
}
