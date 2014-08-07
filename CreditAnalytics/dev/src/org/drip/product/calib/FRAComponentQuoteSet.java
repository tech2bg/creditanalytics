
package org.drip.product.calib;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * FRAComponentQuoteSet extends the ProductQuoteSet by implementing the Calibration Parameters for the FRA
 * 	Component. Currently it only exposes the FRA Rate Quote Field.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAComponentQuoteSet extends org.drip.product.calib.ProductQuoteSet {

	/**
	 * FRAComponentQuoteSet Constructor
	 * 
	 * @param aLSS Array of Latent State Specification
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public FRAComponentQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
		throws java.lang.Exception
	{
		super (aLSS);
	}

	/**
	 * Set the FRA Rate
	 * 
	 * @param dblFRARate The FRA Rate
	 * 
	 * @return TRUE => The FRA Rate successfully set
	 */

	public boolean setFRARate (
		final double dblFRARate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblFRARate)) return false;

		_mapQuote.put ("FRARate", dblFRARate);

		return true;
	}

	/**
	 * Indicate if the FRA Rate Field exists
	 * 
	 * @return TRUE => FRA Rate Field Exists
	 */

	public boolean containsFRARate()
	{
		return _mapQuote.containsKey ("FRARate");
	}

	/**
	 * Retrieve the FRA Rate
	 * 
	 * @return The FRA Rate
	 * 
	 * @throws java.lang.Exception Thrown if the FRA Rate Field does not exist
	 */

	public double fraRate()
		throws java.lang.Exception
	{
		if (!containsFRARate())
			throw new java.lang.Exception ("FRAComponentQuoteSet::coupon => Does not contain FRA Rate");

		return _mapQuote.get ("FRARate");
	}

	/**
	 * Set the Par Forward Rate
	 * 
	 * @param dblParForwardRate The Par Forward Rate
	 * 
	 * @return TRUE => The Par Forward Rate successfully set
	 */

	public boolean setParForwardRate (
		final double dblParForwardRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblParForwardRate)) return false;

		_mapQuote.put ("ParForwardRate", dblParForwardRate);

		return true;
	}

	/**
	 * Indicate if the Par Forward Rate Field exists
	 * 
	 * @return TRUE => Par Forward Rate Field Exists
	 */

	public boolean containsParForwardRate()
	{
		return _mapQuote.containsKey ("ParForwardRate");
	}

	/**
	 * Retrieve the Par Forward Rate
	 * 
	 * @return The Par Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Par Forward Rate Field does not exist
	 */

	public double parForwardRate()
		throws java.lang.Exception
	{
		if (!containsParForwardRate())
			throw new java.lang.Exception
				("FRAComponentQuoteSet::parForwardRate => Does not contain the Par Forward Rate");

		return _mapQuote.get ("ParForwardRate");
	}
}
