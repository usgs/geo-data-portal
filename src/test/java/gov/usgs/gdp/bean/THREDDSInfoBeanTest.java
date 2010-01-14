package gov.usgs.gdp.bean;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class THREDDSInfoBeanTest {
	THREDDSInfoBean tIBean = null;
	
	@Before
	public void setUp() throws Exception {
		this.tIBean = new THREDDSInfoBean();
		this.tIBean.setOpenDapGridTimes(new ArrayList<String>());
		for (int yearCount = 1;yearCount < 12;yearCount++) {
			for (int monthCount = 1; monthCount < 13;monthCount++) {
				String year = Integer.toString(yearCount + 1900);
				String month = "";
				if (monthCount < 10) {
					month = "0" + Integer.toString(monthCount);
				} else {
					month = Integer.toString(monthCount);
				}
				String instanceDate = year + "-" + month + "-15 00:00:00Z";
				this.tIBean.getOpenDapGridTimes().add(instanceDate);
			}
		}
	}

	@Test
	public final void testGetFromYearFunction() {
		int year = this.tIBean.getFromYear();
		assertFalse(year < 0);
	}
	
	@Test
	public final void testGetToYearFunction() {
		int year = this.tIBean.getToYear();
		assertFalse(year < 0);
		assertEquals(1911, year);
	}
	
	@Test
	public final void testGetFromMonthFunction() {
		int month = this.tIBean.getFromMonth();
		assertFalse(month < 0);
	}
	
	@Test
	public final void testGetToMonthFunction() {
		int month = this.tIBean.getToMonth();
		assertFalse(month < 0);
		assertEquals(11, month);
	}
	
	@Test
	public final void testGetToDayFunction() {
		int day = this.tIBean.getToDay();
		assertFalse(day < 0);
		assertEquals(15, day);
	}
	
	@Test
	public final void testGetFromDayFunction() {
		int day = this.tIBean.getFromDay();
		assertFalse(day < 0);
		assertEquals(15, day);
	}
	
}
