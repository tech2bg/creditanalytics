
package org.drip.param.market;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * LatentStateFixingsContainer holds the explicit fixings for a specified Latent State Quantification along
 * 	the date ordinate.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateFixingsContainer {
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixings = new
			java.util.TreeMap<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

	/**
	 * A do nothing LatentStateFixingsContainer Instance Constructor
	 */

	public LatentStateFixingsContainer()
	{
	}

	/**
	 * Add the Fixing corresponding to the Date/Label Pair
	 * 
	 * @param dt The Fixing Date
	 * @param lsl The Fixing Label
	 * @param dblFixing The Fixing Amount
	 * 
	 * @return TRUE => Entry successfully added
	 */

	public boolean add (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl,
		final double dblFixing)
	{
		if (null == dt || null == lsl || !org.drip.quant.common.NumberUtil.IsValid (dblFixing)) return false;

		if (!_mmFixings.containsKey (dt))
			_mmFixings.put (dt, new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapLSLFixing = _mmFixings.get
			(dt);

		mapLSLFixing.put (lsl.fullyQualifiedName(), dblFixing);

		return true;
	}

	/**
	 * Add the Fixing corresponding to the Date/Label Pair
	 * 
	 * @param dblDate The Fixing Date
	 * @param lsl The Fixing Label
	 * @param dblFixing The Fixing Amount
	 * 
	 * @return TRUE => Entry successfully added
	 */

	public boolean add (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl,
		final double dblFixing)
	{
		try {
			return add (new org.drip.analytics.date.JulianDate (dblDate), lsl, dblFixing);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Remove the Fixing corresponding to the Date/Label Pair it if exists
	 * 
	 * @param dt The Fixing Date
	 * @param lsl The Fixing Label
	 * 
	 * @return TRUE => Entry successfully removed if it existed
	 */

	public boolean remove (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		if (null == dt || null == lsl || !_mmFixings.containsKey (dt)) return true;

		_mmFixings.get (dt).remove (lsl.fullyQualifiedName());

		return true;
	}


	/**
	 * Remove the Fixing corresponding to the Date/Label Pair it if exists
	 * 
	 * @param dblDate The Fixing Date
	 * @param lsl The Fixing Label
	 * 
	 * @return TRUE => Entry successfully removed if it existed
	 */

	public boolean remove (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		try {
			return remove (new org.drip.analytics.date.JulianDate (dblDate), lsl);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Retrieve the Fixing for the Specified Date/LSL Combination
	 * 
	 * @param dt Date
	 * @param lsl The Latent State Label
	 * 
	 * @return The Fixing for the Specified Date/LSL Combination
	 * 
	 * @throws java.lang.Exception Thrown if the Fixing cannot be found
	 */

	public double get (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl)
		throws java.lang.Exception
	{
		if (null == dt || null == lsl || !_mmFixings.containsKey (dt))
			throw new java.lang.Exception ("Cannot locate Fixing for the Date/Label Combination!");

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapLSLFixing = _mmFixings.get
			(dt);

		java.lang.String strLabel = lsl.fullyQualifiedName();

		if (!mapLSLFixing.containsKey (strLabel))
			throw new java.lang.Exception ("Cannot locate the LSL Entry for the Date!");

		return mapLSLFixing.get (strLabel);
	}

	/**
	 * Retrieve the Fixing for the Specified Date/LSL Combination
	 * 
	 * @param dblDate Date
	 * @param lsl The Latent State Label
	 * 
	 * @return The Fixing for the Specified Date/LSL Combination
	 * 
	 * @throws java.lang.Exception Thrown if the Fixing cannot be found
	 */

	public double get (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl)
		throws java.lang.Exception
	{
		return get (new org.drip.analytics.date.JulianDate (dblDate), lsl);
	}

	/**
	 * Indicates the Availability of the Fixing for the Specified LSL Label on the specified Date
	 * 
	 * @param dt The Date
	 * @param lsl The Label
	 * 
	 * @return TRUE => The Fixing for the Specified LSL Label on the specified Date 
	 */

	public boolean available (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		if (null == dt || null == lsl || !_mmFixings.containsKey (dt)) return false;

		return _mmFixings.get (dt).containsKey (lsl.fullyQualifiedName());
	}

	/**
	 * Indicates the Availability of the Fixing for the Specified LSL on the specified Date
	 * 
	 * @param dblDate The Date
	 * @param lsl The Label
	 * 
	 * @return TRUE => The Fixing for the Specified LSL on the specified Date 
	 */

	public boolean available (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		try {
			return available (new org.drip.analytics.date.JulianDate (dblDate), lsl);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
