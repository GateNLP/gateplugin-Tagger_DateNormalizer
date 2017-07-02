/**
 * Copyright (C) 2004-2010, Mark A. Greenwood
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 3, June 2007
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 */
package mark.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It is often useful to be able to convert a String representation of a date
 * into an actual Date object. Unfortunately the standard methods for doing this
 * are very restrictive (often requiring the String representation to be in a
 * given format). This parser attempts to convert any date representation into a
 * Date object and also provides support for parsing dates which are not fully
 * specified (i.e. today) by normalising against a supplied Date object
 * (defaults to today).
 * @author Mark A. Greenwood
 */
public class DateParser implements Serializable
{

  private static final long serialVersionUID = -5538057276306768673L;

  /**
	 * Symbolises that none of the three date fields were inferred but were all
	 * present in the date being parsed.
	 */
	public static final int NONE = 0;

	/**
	 * Symbolises that the day was not in the date being parsed and had
	 * to be inferred.
	 */
	public static final int DAY = 1;

	/**
	 * Symbolises that the month was not in the date being parsed and had
	 * to be inferred.
	 */
	public static final int MONTH = 2;

	/**
	 * Symbolises that the year was not in the date being parsed and had
	 * to be inferred.
	 */
	public static final int YEAR = 4;

	/**
	 * Symbolises that all three fields had to be inferred.
	 */
	public static final int ALL = DAY | MONTH | YEAR;

	private static final Pattern[] p = new Pattern[] {

			Pattern.compile("(?:(\\p{Alpha}+)(?:,|\\s+the)?\\s+)?(\\d{1,2})\\.?\\s*(?:st|nd|rd|th)?\\s+(?:of\\s+)?(\\p{Alpha}+),?\\s+(\\d{4}|\\d{2})(?=\\b)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE),
			Pattern.compile("(?:(\\p{Alpha}+)(?:,|\\s+the)?\\s+)?(\\d{1,2})\\.?\\s*(?:st|nd|rd|th)?\\s+(?:of\\s+)?(\\p{Alpha}+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(?:(\\p{Alpha}+)(?:,|\\s+the)?\\s+)?(\\p{Alpha}+)\\.?\\s+(?:the\\s+)?(\\d{1,2})\\s*(?:st|nd|rd|th)?,?\\s+(\\d{4}|\\d{2})(?=\\b)", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE),
			Pattern.compile("(?:(\\p{Alpha}+)(?:,|\\s+the)?\\s+)?(\\p{Alpha}+)\\.?\\s+(?:the\\s+)?(\\d{1,2})(?:\\s*(st|nd|rd|th))?(?=\\b)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\d{1,2})[-/\\.](\\d{1,2})[-/\\.](\\d{4}|\\d{2})(?=$|[^0-9])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\d{4})[-/\\.]\\s*(\\d{1,2})[-/\\.]\\s*(\\d{1,2})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\d{1,2})[-/\\.](\\p{Alpha}+)[-/\\.](\\d{4}|\\d{2})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("last\\s+(\\p{Alpha}+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE), Pattern.compile("next\\s+(\\p{Alpha}+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\p{Alpha}+)\\s+'?(\\d{4}|\\d{2})(?=\\b)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\d{1,2})\\s+(\\p{Alpha}+)\\s+ago", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\d{4})[-\\.]?\\s*(\\p{Alpha}+)[-\\.]?\\s*(\\d{1,2})(?:,?\\s+(\\p{Alpha}+))?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\p{Alpha}+)[-/\\.](\\d{1,2})[-/\\.](\\d{4})(?=$|[^0-9])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
			Pattern.compile("(\\p{Alpha}+)(?=\\b)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)};

	private List<String> weekdays = null;
	private List<String> shortWeekdays = null;

	private List<String> months = null;
	private List<String> shortMonths = null;

	private List<String> eras = null;

	private boolean monthFirst = true;

	private int century = 0;
	private int yearIn = 0;

	private Locale locale;

	/**
	 * Construct a new date parser which takes the day/month ordering as well as
	 * month and day of week names from the current locale.
	 */
	public DateParser()
	{
		this(Locale.getDefault());
	}

	/**
	 * Construct a new date parser which takes the day/month ordering as well as
	 * month and day of week names from the specified locale.
	 * @param locale the locale used to configure the parser
	 */
	public DateParser(Locale locale)
	{
		this.locale = locale;

		if (this.locale == null) this.locale = Locale.getDefault();

		Calendar cal = Calendar.getInstance(this.locale);

		// Should this be done on this year or on the relative parse date
		yearIn = cal.get(Calendar.YEAR) % 100;
		century = cal.get(Calendar.YEAR) - yearIn;

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, this.locale);
		cal.set(2000, 2, 4);
		String td = df.format(cal.getTime());
		monthFirst = td.indexOf("3") < td.indexOf("4");

		DateFormatSymbols dfs = new DateFormatSymbols(this.locale);

		months = arrayToList(dfs.getMonths());
		shortMonths = arrayToList(dfs.getShortMonths());

		weekdays = arrayToList(dfs.getWeekdays());
		shortWeekdays = arrayToList(dfs.getShortWeekdays());

		// Is initialised even though it isn't used yet
		eras = arrayToList(dfs.getEras());
	}

	/**
	 * Parses the given text starting at the specified position and using the
	 * current date
	 * to infer missing fields.
	 * @param text the text to parse
	 * @param pos the position within the text to start parsing
	 * @return null if the text doesn't represent a date otherwise a fully
	 *         specified Date object
	 */
	public Date parse(String text, ParsePosition pos)
	{
		return parse(text, pos, new Date());
	}

	/**
	 * Parses the given text starting at the specified position using the
	 * provided date to infer missing fields. If the provided date is null then
	 * the current date will be used instead.
	 * @param text the text to parse
	 * @param pos the position within the text to start parsing
	 * @param date the date which should be used to infer missing fields
	 * @return null if the text doesn't represent a date otherwise a fully
	 *         specified Date object
	 */
	public Date parse(String text, ParsePosition pos, Date date)
	{
		if (date == null) date = new Date();

		Calendar parsed = internalParse(text, pos, date);

		if (parsed == null) return null;

		if (pos instanceof ParsePositionEx)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			String relative = "present";

			if (parsed.before(cal)) relative = "past";
			if (parsed.after(cal)) relative = "future";

			((ParsePositionEx) pos).getFeatures().put("relative", relative);
		}

		return parsed.getTime();
	}

	private Calendar internalParse(String text, ParsePosition pos, Date date)
	{
		Calendar cal = Calendar.getInstance(locale);
		cal.setLenient(false);
		cal.setTime(date);

		String lcase = text.toLowerCase();

		if (lcase.startsWith("today", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 5);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
			}
			return cal;
		}
		else if (lcase.startsWith("tomorrow", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 8);
			cal.add(Calendar.DATE, 1);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
			}
			return cal;
		}
		else if (lcase.startsWith("yesterday", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 9);
			cal.add(Calendar.DATE, -1);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
			}
			return cal;
		}
		else if (lcase.startsWith("previous day", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 12);
			cal.add(Calendar.DATE, -1);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
			}
			return cal;
		}
		else if (lcase.startsWith("last week", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 9);
			cal.add(Calendar.WEEK_OF_YEAR, -1);
			cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
			}
			return cal;
		}
		else if (lcase.startsWith("next week", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 9);
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
			}
			return cal;
		}
		else if (lcase.startsWith("last month", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 10);
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
			}
			return cal;
		}
		else if (lcase.startsWith("next month", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 10);
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
			}
			return cal;
		}
		else if (lcase.startsWith("last year", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 9);
			cal.add(Calendar.YEAR, -1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.MONTH, 0);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", YEAR);
			}
			return cal;
		}
		else if (lcase.startsWith("next year", pos.getIndex()))
		{
			pos.setIndex(pos.getIndex() + 9);
			cal.add(Calendar.YEAR, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.MONTH, 0);
			if (pos instanceof ParsePositionEx)
			{
				((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
				((ParsePositionEx) pos).getFeatures().put("accurate", YEAR);
			}
			return cal;
		}

		// 31st August 1979
		Matcher m = p[0].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				if (m.group(1) != null) parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(2));
				int month = parseMonth(m.group(3));
				int year = checkYear(Integer.parseInt(m.group(4)));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// 31st August
		m = p[1].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				if (m.group(1) != null) parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(2));
				int month = parseMonth(m.group(3));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(cal.get(Calendar.YEAR), month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", YEAR);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// August 31st 1979
		m = p[2].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				if (m.group(1) != null) parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(3));
				int month = parseMonth(m.group(2));
				int year = checkYear(Integer.parseInt(m.group(4)));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// August 31st
		m = p[3].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				if (m.group(1) != null) parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(3));
				int month = parseMonth(m.group(2));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(cal.get(Calendar.YEAR), month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", YEAR);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// 08/31/1979
		m = p[4].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int day = (monthFirst ? Integer.parseInt(m.group(2)) : Integer.parseInt(m.group(1)));
				int month = (monthFirst ? Integer.parseInt(m.group(1)) - 1 : Integer.parseInt(m.group(2)) - 1);
				int year = checkYear(Integer.parseInt(m.group(3)));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// 1979-08-31
		m = p[5].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int day = Integer.parseInt(m.group(3));
				int month = Integer.parseInt(m.group(2)) - 1;
				int year = Integer.parseInt(m.group(1));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// 31-Aug-1979
		m = p[6].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int day = Integer.parseInt(m.group(1));
				int month = parseMonth(m.group(2));
				int year = checkYear(Integer.parseInt(m.group(3)));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// last Tuesday
		m = p[7].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int weekday = parseWeekday(m.group(1));
				pos.setIndex(m.end());

				cal.set(Calendar.DAY_OF_WEEK, weekday);

				// TODO should we always roll back or not?
				// I guess this will depend on where in the week we are in
				// relation to the day mentioned
				cal.add(Calendar.WEEK_OF_MONTH, -1);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// next Wednesday
		m = p[8].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int weekday = parseWeekday(m.group(1));
				pos.setIndex(m.end());

				cal.set(Calendar.DAY_OF_WEEK, weekday);

				// TODO should we always roll forward or not?
				// I guess this will depend on where in the week we are in
				// relation to the day mentioned
				cal.add(Calendar.WEEK_OF_MONTH, 1);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{}
		}

		// July 2009
		// August '08
		m = p[9].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int month = parseMonth(m.group(1));
				int year = checkYear(Integer.parseInt(m.group(2)));

				checkMonthAndDay(month, 1);

				pos.setIndex(m.end());

				cal.set(year, month, 1);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", DAY);
					((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
				}

				return cal;
			}
			catch (Exception e)
			{}
		}

		m = p[10].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int goBack = Integer.parseInt(m.group(1)) * -1;

				String amount = m.group(2).toLowerCase();

				boolean found = false;

				if (amount.equals("days") || amount.equals("day"))
				{
					cal.add(Calendar.DATE, goBack);
					if (pos instanceof ParsePositionEx) ((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
					found = true;
				}
				else if (amount.equals("weeks") || amount.equals("week"))
				{
					cal.add(Calendar.WEEK_OF_YEAR, goBack);
					cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
					if (pos instanceof ParsePositionEx) ((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
					found = true;
				}
				else if (amount.equals("months") || amount.equals("month"))
				{
					cal.add(Calendar.MONTH, goBack);
					cal.set(Calendar.DAY_OF_MONTH, 1);
					if (pos instanceof ParsePositionEx) ((ParsePositionEx) pos).getFeatures().put("accurate", YEAR | MONTH);
					found = true;
				}
				else if (amount.equals("years") || amount.equals("year"))
				{
					cal.add(Calendar.YEAR, goBack);
					cal.set(Calendar.DAY_OF_YEAR, 1);
					if (pos instanceof ParsePositionEx) ((ParsePositionEx) pos).getFeatures().put("accurate", YEAR);
					found = true;
				}

				if (found)
				{
					if (pos instanceof ParsePositionEx) ((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
					pos.setIndex(m.end());
					return cal;
				}
			}
			catch (RuntimeException e)
			{
				//none of this can throw a checked exception so only look for runtime problems
			}
		}

		m = p[11].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int day = Integer.parseInt(m.group(3));
				int month = parseMonth(m.group(2));
				int year = checkYear(Integer.parseInt(m.group(1)));

				if (m.group(4) != null) parseWeekday(m.group(4));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{

			}

			try
			{
				int day = Integer.parseInt(m.group(3));
				int month = parseMonth(m.group(2));
				int year = checkYear(Integer.parseInt(m.group(1)));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end(3));

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{

			}
		}

		m = p[12].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int day = Integer.parseInt(m.group(2));
				int month = parseMonth(m.group(1));
				int year = checkYear(Integer.parseInt(m.group(3)));

				checkMonthAndDay(month, day);

				pos.setIndex(m.end());

				cal.set(year, month, day);
				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", NONE);
					((ParsePositionEx) pos).getFeatures().put("accurate", ALL);
				}
				return cal;
			}
			catch (Exception e)
			{

			}
		}

		//TODO think about mentions of just a month or day of week -- have to be careful with "may"
		m = p[13].matcher(text);

		if (m.find(pos.getIndex()) && checkPosition(m.start(), pos.getIndex()))
		{
			try
			{
				int weekday = parseWeekday(m.group(1));
				pos.setIndex(m.end());

				cal.set(Calendar.DAY_OF_WEEK, weekday);

				if (pos instanceof ParsePositionEx)
				{
					((ParsePositionEx) pos).getFeatures().put("inferred", ALL);
					((ParsePositionEx) pos).getFeatures().put("accurate", NONE);
				}

				return cal;
			}
			catch (Exception e)
			{

			}

			try
			{
				int month = parseMonth(m.group(1));
				if (!m.group(1).toLowerCase().equals(m.group(1)))
				{
					pos.setIndex(m.end());

					cal.set(cal.get(Calendar.YEAR), month, 1);

					if (pos instanceof ParsePositionEx)
					{
						((ParsePositionEx) pos).getFeatures().put("inferred", DAY | YEAR);
						((ParsePositionEx) pos).getFeatures().put("accurate", MONTH);
					}

					return cal;
				}
			}
			catch (Exception e)
			{

			}
		}

		return null;
	}

	private int checkYear(int year)
	{
		if (year < 100)
		{
			if (year > yearIn) year -= 100;

			year += century;
		}

		return year;
	}

	private boolean checkPosition(int found, int requested)
	{
		return requested == found;
	}

	private boolean checkMonthAndDay(int month, int day) throws Exception
	{
		if (month < 0 || month > 11) throw new Exception("month outside valid range");

		if (day < 1 || day > 31) throw new Exception("day of month outside valid range");

		return true;
	}

	private static List<String> arrayToList(String[] array)
	{
		List<String> data = new ArrayList<String>();

		for (int i = 0; i < array.length; ++i)
			data.add(array[i].toLowerCase());

		return data;
	}

	private int parseMonth(String month) throws Exception
	{
		String lcm = month.toLowerCase();

		if (months.contains(lcm)) return months.indexOf(lcm);
		if (shortMonths.contains(lcm)) return shortMonths.indexOf(lcm);

		throw new Exception("Invalid Month String: " + month);
	}

	private int parseWeekday(String weekday) throws Exception
	{
		String lcw = weekday.toLowerCase();

		if (weekdays.contains(lcw)) return weekdays.indexOf(lcw);
		if (shortWeekdays.contains(lcw)) return shortWeekdays.indexOf(lcw);

		throw new Exception("Invalid Weekday String: " + weekday);
	}

	private int parseEra(String era) throws Exception
	{
		String lce = era.toLowerCase();

		if (eras.contains(lce)) return eras.indexOf(lce);

		throw new Exception("Invalid Era String: " + era);
	}

	public static Locale getLocale(String name)
	{
		if (name == null || name.trim().equals("")) return null;

		String[] parts = name.split("_");

		Locale locale = null;

		if (parts.length == 1)
		{
			locale = new Locale(parts[0]);
		}
		else if (parts.length == 2)
		{
			locale = new Locale(parts[0], parts[1]);
		}
		else if (parts.length == 3)
		{
			locale = new Locale(parts[0], parts[1], parts[2]);
		}

		if (locale == null) return null;

		return (locale.toString().equals(name) ? locale : null);
	}

	/**
	 * Returns the set of words that can occur within dates. This includes the
	 * days of the week and months of the year as well as words such as today,
	 * tomorrow etc. This is provided in order to help users of this parser find
	 * the approximate location of dates within text in order to cut down on
	 * parsing time.
	 * @return the set of words which may occur within dates
	 */
	public Set<String> getWords()
	{
		Set<String> words = new HashSet<String>();

		words.addAll(weekdays);
		words.addAll(shortWeekdays);
		words.addAll(months);
		words.addAll(shortMonths);

		//THESE should be loaded from a resource bundle of some form
		words.add("last");
		words.add("next");
		words.add("previous");
		words.add("today");
		words.add("tomorrow");
		words.add("yesterday");

		return words;
	}
}
