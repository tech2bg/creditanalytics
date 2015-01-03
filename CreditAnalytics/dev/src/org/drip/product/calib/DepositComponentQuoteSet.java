
package org.drip.product.calib;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * DepositComponentQuoteSet extends the ProductQuoteSet by implementing the Calibration Parameters for the
 * 	Deposit Component. Currently it exposes the PV and the Rate Quote Fields.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DepositComponentQuoteSet extends org.drip.product.calib.ProductQuoteSet {

	/**
	 * DepositComponentQuoteSet Constructor
	 * 
	 * @param aLSS Array of Latent State Specification
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public DepositComponentQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
		throws java.lang.Exception
	{
		super (aLSS);
	}

	/**
	 * Set the PV
	 * 
	 * @param dblPV The PV
	 * 
	 * @return TRUE => PV successfully set
	 */

	public boolean setPV (
		final double dblPV)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPV)) return false;

		_mapQuote.put ("PV", dblPV);

		return true;
	}

	/**
	 * Indicate if the PV Field exists
	 * 
	 * @return TRUE => PV Field Exists
	 */

	public boolean containsPV()
	{
		return _mapQuote.containsKey ("PV");
	}

	/**
	 * Retrieve the PV
	 * 
	 * @return The PV
	 * 
	 * @throws java.lang.Exception Thrown if the PV Field does not exist
	 */

	public double pv()
		throws java.lang.Exception
	{
		if (!containsPV())
			throw new java.lang.Exception ("DepositComponentQuoteSet::pv => Does not contain PV");

		return _mapQuote.get ("PV");
	}

	/**
	 * Set the Forward Rate
	 * 
	 * @param dblForwardRate The Forward Rate
	 * 
	 * @return TRUE => The Forward Rate successfully set
	 */

	public boolean setForwardRate (
		final double dblForwardRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblForwardRate)) return false;

		_mapQuote.put ("ForwardRate", dblForwardRate);

		return true;
	}

	/**
	 * Indicate if the Forward Rate Field exists
	 * 
	 * @return TRUE => Forward Rate Field Exists
	 */

	public boolean containsForwardRate()
	{
		return _mapQuote.containsKey ("ForwardRate");
	}

	/**
	 * Retrieve the Forward Rate
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate Field does not exist
	 */

	public double forwardRate()
		throws java.lang.Exception
	{
		if (!containsForwardRate())
			throw new java.lang.Exception
				("DepositComponentQuoteSet::forwardRate => Does not contain Forward Rate");

		return _mapQuote.get ("ForwardRate");
	}

	/**
	 * Set the Rate
	 * 
	 * @param dblRate The Rate
	 * 
	 * @return TRUE => The Rate successfully set
	 */

	public boolean setRate (
		final double dblRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblRate)) return false;

		_mapQuote.put ("Rate", dblRate);

		return true;
	}

	/**
	 * Indicate if the Rate Field exists
	 * 
	 * @return TRUE => Rate Field Exists
	 */

	public boolean containsRate()
	{
		return _mapQuote.containsKey ("Rate");
	}

	/**
	 * Retrieve the Rate
	 * 
	 * @return The Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Rate Field does not exist
	 */

	public double rate()
		throws java.lang.Exception
	{
		if (!containsRate())
			throw new java.lang.Exception ("DepositComponentQuoteSet::rate => Does not contain rate");

		return _mapQuote.get ("Rate");
	}
}
