
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
 * CompositePeriodQuoteSet implements the composite period's calibration quote set functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CompositePeriodQuoteSet extends org.drip.product.calib.ProductQuoteSet {

	/**
	 * CompositePeriodQuoteSet constructor
	 * 
	 * @param aLSS Array of Latent State Specification
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public CompositePeriodQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
		throws java.lang.Exception
	{
		super (aLSS);
	}

	/**
	 * Set the Base Rate
	 * 
	 * @param dblBaseRate The Base Rate
	 * 
	 * @return TRUE => Base Rate successfully set
	 */

	public boolean setBaseRate (
		final double dblBaseRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBaseRate)) return false;

		_mapQuote.put ("BaseRate", dblBaseRate);

		return true;
	}

	/**
	 * Indicate if the Base Rate Field exists
	 * 
	 * @return TRUE => Base Rate Field Exists
	 */

	public boolean containsBaseRate()
	{
		return _mapQuote.containsKey ("BaseRate");
	}

	/**
	 * Get the Period Base Coupon Rate
	 * 
	 * @return The Period Base Coupon Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Base Rate Field does not exist
	 */

	public double baseRate()
		throws java.lang.Exception
	{
		if (!containsBaseRate())
			throw new java.lang.Exception ("CompositePeriodQuoteSet::baseRate => Does not contain BaseRate");

		return _mapQuote.get ("BaseRate");
	}

	/**
	 * Set the Basis
	 * 
	 * @param dblBasis The Basis
	 * 
	 * @return TRUE => Basis successfully set
	 */

	public boolean setBasis (
		final double dblBasis)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBasis)) return false;

		_mapQuote.put ("Basis", dblBasis);

		return true;
	}

	/**
	 * Indicate if the Basis Field exists
	 * 
	 * @return TRUE => Basis Field Exists
	 */

	public boolean containsBasis()
	{
		return _mapQuote.containsKey ("Basis");
	}

	/**
	 * Get the Period Coupon Basis
	 * 
	 * @return The Period Coupon Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Basis Field does not exist
	 */

	public double basis()
		throws java.lang.Exception
	{
		if (!containsBasis())
			throw new java.lang.Exception ("CompositePeriodQuoteSet::basis => Does not contain Basis");

		return _mapQuote.get ("Basis");
	}
}
