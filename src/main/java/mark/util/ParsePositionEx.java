/**
 * Copyright (C) 2004-2010, Mark A. Greenwood
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 3, June 2007
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 */

package mark.util;

import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends the standard ParsePosition class to allow arbitrary key/value pairs
 * to be associated with the current parse position. This is useful to allow the
 * reporting of parse related information over and above the value that has
 * actually been parsed and will be returned.
 * @author Mark A. Greenwood
 */
public class ParsePositionEx extends ParsePosition
{
	/**
	 * A map to hold the key/value pairs that provide the extra
	 * functionallity not found in ParsePosition
	 */
	private Map<String, Object> features = new HashMap<String, Object>();

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((features == null) ? 0 : features.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		ParsePositionEx other = (ParsePositionEx) obj;

		if (features == null)
		{
			if (other.features != null) return false;
		}
		else if (!features.equals(other.features)) { return false; }

		return true;
	}

	/**
	 * Create a new instance which starts parsing from 0
	 */
	public ParsePositionEx()
	{
		super(0);
	}

	/**
	 * Create a new instance with a given starting position
	 * @param index the index from which to start parsing
	 */
	public ParsePositionEx(int index)
	{
		super(index);
	}

	/**
	 * Create a new instance with a given starting position and an initial
	 * set of features.
	 * @param index the index from which to start parsing
	 * @param features the initial features to associate with the parse
	 */
	public ParsePositionEx(int index, Map<String, Object> features)
	{
		super(index);
		this.features.putAll(features);
	}

	/**
	 * Returns the map of features associated with this point in the parse
	 * @return The map of features associated with this point in the parse
	 */
	public Map<String, Object> getFeatures()
	{
		return features;
	}

	/**
	 * Resets this instance to have a zero index and no feature. Useful for
	 * allowing easy reuse of an instance when parsing multiple strings
	 * @param index the index at which to start parsing at next time
	 * @return the reset instance
	 */
	public ParsePositionEx reset(int index)
	{
		setIndex(index);
		features.clear();
		
		return this;
	}
	
	/**
	 * Resets this instance to have a zero index and no feature. Useful for
	 * allowing easy reuse of an instance when parsing multiple strings
	 * @return the reset instance
	 */
	public ParsePositionEx reset()
	{
		return reset(0);
	}
}
