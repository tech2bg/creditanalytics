
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
 * MultiSidedQuote implements the Quote interface, which contains the stubs corresponding to a product
 *  quote. It contains the quote value, quote instant for the different quote sides (bid/ask/mid).
 *   
 * @author Lakshmi Krishnamurthy
 */

public class MultiSidedQuote extends org.drip.param.definition.Quote {
	class SidedQuote {
		double _dblSize = java.lang.Double.NaN;
		double _dblQuote = java.lang.Double.NaN;
		org.drip.analytics.date.DateTime _dt = null;

		SidedQuote (
			final double dblQuote,
			final double dblSize)
			throws java.lang.Exception
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (_dblQuote = dblQuote))
				throw new java.lang.Exception ("MultiSidedQuote::SidedQuote ctr: Invalid Inputs!");

			_dblSize = dblSize;

			_dt = new org.drip.analytics.date.DateTime();
		}

		double quote()
		{
			return _dblQuote;
		}

		double size()
		{
			return _dblSize;
		}

		org.drip.analytics.date.DateTime time()
		{
			return _dt;
		}

		boolean setQuote (
			final double dblQuote)
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (dblQuote)) return false;

			_dblQuote = dblQuote;
			return true;
		}

		boolean setSize (
			final double dblSize)
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (dblSize)) return false;

			_dblSize = dblSize;
			return true;
		}
	};

	private org.drip.analytics.support.CaseInsensitiveTreeMap<SidedQuote> _mapSidedQuote = new
		org.drip.analytics.support.CaseInsensitiveTreeMap<SidedQuote>();

	/**
	 * MultiSidedQuote Constructor: Constructs a Quote object from the quote value and the side string.
	 * 
	 * @param strSide bid/ask/mid
	 * @param dblQuote Quote Value
	 * 
	 * @throws java.lang.Exception Thrown on invalid inputs
	 */

	public MultiSidedQuote (
		final java.lang.String strSide,
		final double dblQuote)
		throws java.lang.Exception
	{
		if (null == strSide || strSide.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid (dblQuote))
			throw new java.lang.Exception ("MultiSidedQuote ctr: Invalid Side/Quote/Size!");

		_mapSidedQuote.put (strSide, new SidedQuote (dblQuote, java.lang.Double.NaN));
	}

	/**
	 * MultiSidedQuote Constructor: Constructs a Quote object from the quote size/value and the side string.
	 * 
	 * @param strSide bid/ask/mid
	 * @param dblQuote Quote Value
	 * @param dblSize Size
	 * 
	 * @throws java.lang.Exception Thrown on invalid inputs
	 */

	public MultiSidedQuote (
		final java.lang.String strSide,
		final double dblQuote,
		final double dblSize)
		throws java.lang.Exception
	{
		if (null == strSide || strSide.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid (dblQuote))
			throw new java.lang.Exception ("MultiSidedQuote ctr: Invalid Side/Quote/Size!");

		_mapSidedQuote.put (strSide, new SidedQuote (dblQuote, dblSize));
	}

	@Override public double value (
		final java.lang.String strSide)
	{
		if (null == strSide || strSide.isEmpty()) return java.lang.Double.NaN;

		return _mapSidedQuote.get (strSide).quote();
	}

	@Override public double size (
		final java.lang.String strSide)
	{
		if (null == strSide || strSide.isEmpty()) return java.lang.Double.NaN;

		return _mapSidedQuote.get (strSide).size();
	}

	@Override public org.drip.analytics.date.DateTime time (
		final java.lang.String strSide)
	{
		if (null == strSide || strSide.isEmpty()) return null;

		return _mapSidedQuote.get (strSide).time();
	}

	@Override public boolean setSide (
		final java.lang.String strSide,
		final double dblQuote,
		final double dblSize)
	{
		if (null != strSide && !strSide.isEmpty() && !org.drip.quant.common.NumberUtil.IsValid (dblQuote))
			return false;

		try {
			_mapSidedQuote.put (strSide, new SidedQuote (dblQuote, dblSize));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}
}
