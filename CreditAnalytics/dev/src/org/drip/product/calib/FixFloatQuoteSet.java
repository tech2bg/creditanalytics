
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
 * FixFloatQuoteSet extends the ProductQuoteSet by implementing the Calibration Parameters for the Fix-Float
 *  Swap Component. Currently it exposes the PV, the Reference Basis, and the Derived Basis Quote Fields.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatQuoteSet extends org.drip.product.calib.ProductQuoteSet {

	/**
	 * FixFloatQuoteSet Constructor
	 * 
	 * @param aLSS Array of Latent State Specification
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public FixFloatQuoteSet (
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
		if (!containsPV()) throw new java.lang.Exception ("FixFloatQuoteSet::pv => Does not contain PV");

		return _mapQuote.get ("PV");
	}

	/**
	 * Set the Derived Par Basis Spread
	 * 
	 * @param dblDerivedParBasisSpread The Derived Par Basis Spread
	 * 
	 * @return TRUE => The Derived Par Basis Spread successfully set
	 */

	public boolean setDerivedParBasisSpread (
		final double dblDerivedParBasisSpread)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDerivedParBasisSpread)) return false;

		_mapQuote.put ("DerivedParBasisSpread", dblDerivedParBasisSpread);

		return true;
	}

	/**
	 * Indicate if the Derived Par Basis Spread Field exists
	 * 
	 * @return TRUE => The Derived Par Basis Spread Field Exists
	 */

	public boolean containsDerivedParBasisSpread()
	{
		return _mapQuote.containsKey ("DerivedParBasisSpread");
	}

	/**
	 * Retrieve the Derived Par Basis Spread
	 * 
	 * @return The Derived Par Basis Spread
	 * 
	 * @throws java.lang.Exception Thrown if the Derived Par Basis Spread Field does not exist
	 */

	public double derivedParBasisSpread()
		throws java.lang.Exception
	{
		if (!containsDerivedParBasisSpread())
			throw new java.lang.Exception
				("FixFloatQuoteSet::derivedParBasisSpread => Does not contain the Derived Par Basis Spread");

		return _mapQuote.get ("DerivedParBasisSpread");
	}

	/**
	 * Set the Reference Par Basis Spread
	 * 
	 * @param dblReferenceParBasisSpread The Reference Par Basis Spread
	 * 
	 * @return TRUE => The Reference Par Basis Spread successfully set
	 */

	public boolean setReferenceParBasisSpread (
		final double dblReferenceParBasisSpread)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblReferenceParBasisSpread)) return false;

		_mapQuote.put ("ReferenceParBasisSpread", dblReferenceParBasisSpread);

		return true;
	}

	/**
	 * Indicate if the Reference Par Basis Spread Field exists
	 * 
	 * @return TRUE => The Reference Par Basis Spread Field Exists
	 */

	public boolean containsReferenceParBasisSpread()
	{
		return _mapQuote.containsKey ("ReferenceParBasisSpread");
	}

	/**
	 * Retrieve the Reference Par Basis Spread
	 * 
	 * @return The Reference Par Basis Spread
	 * 
	 * @throws java.lang.Exception Thrown if the Reference Par Basis Spread Field does not exist
	 */

	public double referenceParBasisSpread()
		throws java.lang.Exception
	{
		if (!containsReferenceParBasisSpread())
			throw new java.lang.Exception
				("FixFloatQuoteSet::referenceParBasisSpread => Does not contain the Reference Par Basis Spread");

		return _mapQuote.get ("ReferenceParBasisSpread");
	}

	/**
	 * Set the Swap Rate
	 * 
	 * @param dblSwapRate The Swap Rate
	 * 
	 * @return TRUE => The Swap Rate successfully set
	 */

	public boolean setSwapRate (
		final double dblSwapRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSwapRate)) return false;

		_mapQuote.put ("SwapRate", dblSwapRate);

		return true;
	}

	/**
	 * Indicate if the Swap Rate Field exists
	 * 
	 * @return TRUE => The Swap Rate Field Exists
	 */

	public boolean containsSwapRate()
	{
		return _mapQuote.containsKey ("SwapRate");
	}

	/**
	 * Retrieve the Swap Rate
	 * 
	 * @return The Swap Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Swap Rate Field does not exist
	 */

	public double swapRate()
		throws java.lang.Exception
	{
		if (!containsSwapRate())
			throw new java.lang.Exception ("FixFloatQuoteSet::swapRate => Does not contain the Swap Rate");

		return _mapQuote.get ("SwapRate");
	}
}
