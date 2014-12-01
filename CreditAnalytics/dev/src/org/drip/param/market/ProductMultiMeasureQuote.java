
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
 * ProductMultiMeasureQuote holds the different types of quotes for a given component. It contains a single
 *  market field/quote pair, but multiple alternate named quotes (to accommodate quotes on different measures
 *   for the component). 
 *   
 * @author Lakshmi Krishnamurthy
 */

public class ProductMultiMeasureQuote extends org.drip.param.definition.ProductQuote {
	private java.lang.String _strMarketQuoteField = "";
	private org.drip.param.definition.Quote _mktQuote = null;

	org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.Quote> _mapQuotes = new
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.Quote>();

	/**
	 * Construct an empty instance of ProductMultiMeasureQuote
	 */

	public ProductMultiMeasureQuote()
	{
	}

	@Override public void addQuote (
		final java.lang.String strQuoteField,
		final org.drip.param.definition.Quote q,
		final boolean bIsMarketQuote)
	{
		_mapQuotes.put (strQuoteField, q);

		if (bIsMarketQuote) {
			_mktQuote = q;
			_strMarketQuoteField = strQuoteField;
		}
	}

	@Override public boolean setMarketQuote (
		final java.lang.String strMarketQuoteField,
		final org.drip.param.definition.Quote q)
	{
		if (null == strMarketQuoteField || strMarketQuoteField.isEmpty() || null == q) return false;

		_strMarketQuoteField = strMarketQuoteField;
		_mktQuote = q;
		return true;
	}

	@Override public boolean removeMarketQuote()
	{
		_mktQuote = null;
		_strMarketQuoteField = "";
		return true;
	}

	@Override public org.drip.param.definition.Quote quote (
		final java.lang.String strQuoteField)
	{
		if (null == strQuoteField || strQuoteField.isEmpty()) return null;

		return _mapQuotes.get (strQuoteField);
	}

	@Override public org.drip.param.definition.Quote marketQuote()
	{
		return _mktQuote;
	}

	@Override public java.lang.String marketQuoteField()
	{
		return _strMarketQuoteField;
	}

	@Override public boolean removeQuote (
		final java.lang.String strQuoteField)
	{
		if (null == strQuoteField || strQuoteField.isEmpty()) return false;

		_mapQuotes.remove (strQuoteField);

		if (!_strMarketQuoteField.equalsIgnoreCase (strQuoteField)) return true;

		removeMarketQuote();

		return true;
	}

	@Override public boolean containsQuote (
		final java.lang.String strQuoteField)
	{
		if (null == strQuoteField || strQuoteField.isEmpty()) return false;

		return _mapQuotes.containsKey (strQuoteField) || (null != _strMarketQuoteField &&
			_strMarketQuoteField.equalsIgnoreCase (strQuoteField));
	}
}
