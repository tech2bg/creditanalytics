
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ProductQuote abstract class holds the different types of quotes for a given product. It contains a single
 *  market field/quote pair, but multiple alternate named quotes (to accommodate quotes on different
 *  measures for the component). 
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ProductQuote {

	/**
	 * Add a regular or a market quote for the component
	 * 
	 * @param strQuoteField The quote field
	 * @param q Quote to be added
	 * @param bIsMarketQuote Whether the quote is a market quote
	 */

	public abstract void addQuote (
		final java.lang.String strQuoteField,
		final org.drip.param.definition.Quote q,
		final boolean bIsMarketQuote);

	/**
	 * Set the market quote for the component
	 * 
	 * @param strMarketQuoteField Market Quote field
	 * @param q Quote
	 * 
	 * @return True if successfully added
	 */

	public abstract boolean setMarketQuote (
		final java.lang.String strMarketQuoteField,
		final org.drip.param.definition.Quote q);

	/**
	 * Remove the market quote
	 */

	public abstract boolean removeMarketQuote();

	/**
	 * Get the Quote for the given Field
	 * 
	 * @param strQuoteField Field Name
	 * 
	 * @return Quote object
	 */

	public abstract org.drip.param.definition.Quote quote (
		final java.lang.String strQuoteField);

	/**
	 * Return the market quote object
	 * 
	 * @return Quote object
	 */

	public abstract org.drip.param.definition.Quote marketQuote();

	/**
	 * Retrieve the market quote field
	 * 
	 * @return Field name
	 */

	public abstract java.lang.String marketQuoteField();

	/**
	 * Remove the named Quote
	 * 
	 * @param strQuoteField Named Quote Field
	 * 
	 * @return Success (true) or failure (false)
	 */

	public abstract boolean removeQuote (
		final java.lang.String strQuoteField);

	/**
	 * Indicate if the named quote is available
	 * 
	 * @param strQuoteField The Quote Name
	 * 
	 * @return TRUE => Named Quote is present
	 */

	public abstract boolean containsQuote (
		final java.lang.String strQuoteField);
}
