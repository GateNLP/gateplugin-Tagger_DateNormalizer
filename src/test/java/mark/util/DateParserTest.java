/**
 * This file is part of DateParser.
 * Copyright (C) 2004-2010, Mark A. Greenwood
 *
 * DateParser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DateParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DateParser. If not, see <http://www.gnu.org/licenses/>.
 **/

package mark.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Mark A. Greenwood
 */
public class DateParserTest
{
	private static DateParser parser = null;
	private static DateFormat df = null;

	@BeforeClass
	public static void setUp()
	{
		parser = new DateParser(Locale.UK);
		df = new SimpleDateFormat("dd/MM/yyyy");
	}

	@AfterClass
	public static void tearDown()
	{
		parser = null;
		df = null;
	}

	@Test
	public void testToday()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date d = parser.parse("today", pp);

		assertNotNull(d);

		String formatted = df.format(d);
		String today = df.format(new Date());

		assertEquals(today, formatted);
		assertEquals(pp.getIndex(), 5);
		assertEquals(pp.getFeatures().get("relative"), "present");
	}

	@Test
	public void testCapitalizedToday()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date d = parser.parse("Today", pp);

		assertNotNull(d);

		String formatted = df.format(d);
		String today = df.format(new Date());

		assertEquals(today, formatted);
		assertEquals(pp.getIndex(), 5);
		assertEquals(pp.getFeatures().get("relative"), "present");
	}

	@Test
	public void testTomorrow()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2000 - 1900, 11, 31);

		ParsePositionEx pp = new ParsePositionEx(0);

		Date tomorrow = parser.parse("tomorrow", pp, today);

		assertNotNull(tomorrow);

		String sTomorrow = df.format(tomorrow);

		assertEquals(sTomorrow, "01/01/2001");
		assertEquals(pp.getIndex(), 8);
		assertEquals(pp.getFeatures().get("relative"), "future");
	}

	@Test
	public void testYesterday()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 0, 1);

		ParsePositionEx pp = new ParsePositionEx(0);

		Date yesterday = parser.parse("yesterday", pp, today);

		assertNotNull(yesterday);

		String date = df.format(yesterday);

		assertEquals(date, "31/12/2000");
		assertEquals(pp.getIndex(), 9);
		assertEquals(pp.getFeatures().get("relative"), "past");
	}

	@Test
	public void testPreviousDay()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 0, 1);

		ParsePositionEx pp = new ParsePositionEx(0);

		Date yesterday = parser.parse("previous day", pp, today);

		assertNotNull(yesterday);

		String date = df.format(yesterday);

		assertEquals(date, "31/12/2000");
		assertEquals(pp.getIndex(), 12);
		assertEquals(pp.getFeatures().get("relative"), "past");
	}

	@Test
	public void testLastYear()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 7, 31);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date lastYear = parser.parse("last year", pp, today);

		assertNotNull(lastYear);

		String date = df.format(lastYear);

		assertEquals(pp.getIndex(), 9);
		assertEquals(date, "01/01/2000");
	}

	@Test
	public void testNextYear()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 7, 31);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date nextYear = parser.parse("next year", pp, today);

		assertNotNull(nextYear);

		String date = df.format(nextYear);

		assertEquals(pp.getIndex(), 9);
		assertEquals(date, "01/01/2002");
	}

	@Test
	public void testLastMonth()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 0, 7);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date lastMonth = parser.parse("last month", pp, today);

		assertNotNull(lastMonth);

		String date = df.format(lastMonth);

		assertEquals(pp.getIndex(), 10);
		assertEquals(date, "01/12/2000");
	}

	@Test
	public void testNextMonth()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 11, 7);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date nextMonth = parser.parse("next month", pp, today);

		assertNotNull(nextMonth);

		String date = df.format(nextMonth);

		assertEquals(pp.getIndex(), 10);
		assertEquals(date, "01/01/2002");
	}

	@Test
	public void testMonthsAgo()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 0, 1);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date before = parser.parse("3 months ago", pp, today);

		assertNotNull(before);

		String sTomorrow = df.format(before);

		assertEquals(pp.getIndex(), 12);
		assertEquals(sTomorrow, "01/10/2000");
	}

	@Test
	public void testFullDateSlash()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("31/8/79", pp);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 7);
		assertEquals(df.format(date), "31/08/1979");
	}

	@Test
	public void testFullDateDash()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("31-8-79", pp);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 7);
		assertEquals(df.format(date), "31/08/1979");
	}

	@Test
	public void testDayOfMonthYear()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("31st of August 1979", pp);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 19);
		assertEquals(df.format(date), "31/08/1979");
	}

	@Test
	public void testFullDateDot()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("31.08.79", pp);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 8);
		assertEquals(df.format(date), "31/08/1979");
	}

	@Test
	public void testInvlaidMonthAndDay()
	{
		Date date = parser.parse("27-34-55", new ParsePositionEx(0), null);

		assertNull(date);
	}

	@Test
	public void testMonthYear()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 2, 7);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("January 2005", pp, today);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 12);
		assertEquals(df.format(date), "01/01/2005");
	}

	@Test
	public void testBigEndian()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("2003Nov9", pp, null);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 8);
		assertEquals(df.format(date), "09/11/2003");
	}

	@Test
	public void testBigEndianWithWeekday()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("2003-Nov-9, Sunday", pp, null);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 18);
		assertEquals(df.format(date), "09/11/2003");
	}

	@Test
	public void testMiddleEndian()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("Nov/9/2003", pp, null);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 10);
		assertEquals(df.format(date), "09/11/2003");
	}

	@Test
	public void testMonthOnly()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 2, 7);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("August", pp, today);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 6);
		assertEquals(df.format(date), "01/08/2001");
	}

	@Test
	public void testWeekdayOnly()
	{
		@SuppressWarnings("deprecation") Date today = new Date(2001 - 1900, 2, 7);

		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("Sunday", pp, today);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 6);
		assertEquals(df.format(date), "11/03/2001");
	}
	
	@Test
	public void testJDBCTimestamp()
	{
		ParsePositionEx pp = new ParsePositionEx(0);
		Date date = parser.parse("2013-02-25 12:03:35.6", pp);

		assertNotNull(date);

		assertEquals(pp.getIndex(), 10);
		assertEquals(df.format(date), "25/02/2013");
	}
}
