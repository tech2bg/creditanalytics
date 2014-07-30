
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
 * FloatFloatQuoteSet extends the ProductQuoteSet by implementing the Calibration Parameters for the
 *  Float-Float Swap Component. Currently it exposes the PV, the Reference Basis, and the Derived Basis Quote
 *  Fields.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatQuoteSet extends org.drip.product.calib.ProductQuoteSet {

	/**
	 * FloatFloatQuoteSet Constructor
	 * 
	 * @param aLSS Array of Latent State Specification
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public FloatFloatQuoteSet (
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
		if (!containsPV()) throw new java.lang.Exception ("FloatFloatQuoteSet::pv => Does not contain PV");

		return _mapQuote.get ("PV");
	}

	/**
	 * Set the Derived Basis
	 * 
	 * @param dblDerivedBasis The Derived Basis
	 * 
	 * @return TRUE => The Derived Basis successfully set
	 */

	public boolean setDerivedBasis (
		final double dblDerivedBasis)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDerivedBasis)) return false;

		_mapQuote.put ("DerivedBasis", dblDerivedBasis);

		return true;
	}

	/**
	 * Indicate if the Derived Basis Field exists
	 * 
	 * @return TRUE => The Derived Basis Field Exists
	 */

	public boolean containsDerivedBasis()
	{
		return _mapQuote.containsKey ("DerivedBasis");
	}

	/**
	 * Retrieve the Derived Basis
	 * 
	 * @return The Derived Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Derived Basis Field does not exist
	 */

	public double derivedBasis()
		throws java.lang.Exception
	{
		if (!containsDerivedBasis())
			throw new java.lang.Exception
				("FloatFloatQuoteSet::derivedBasis => Does not contain the Derived Basis");

		return _mapQuote.get ("DerivedBasis");
	}

	/**
	 * Set the Reference Basis
	 * 
	 * @param dblReferenceBasis The Reference Basis
	 * 
	 * @return TRUE => The Reference Basis successfully set
	 */

	public boolean setReferenceBasis (
		final double dblReferenceBasis)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblReferenceBasis)) return false;

		_mapQuote.put ("ReferenceBasis", dblReferenceBasis);

		return true;
	}

	/**
	 * Indicate if the Reference Basis Field exists
	 * 
	 * @return TRUE => The Reference Basis Field Exists
	 */

	public boolean containsReferenceBasis()
	{
		return _mapQuote.containsKey ("ReferenceBasis");
	}

	/**
	 * Retrieve the Reference Basis
	 * 
	 * @return The Reference Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Reference Basis Field does not exist
	 */

	public double referenceBasis()
		throws java.lang.Exception
	{
		if (!containsReferenceBasis())
			throw new java.lang.Exception
				("FloatFloatQuoteSet::referenceBasis => Does not contain the Reference Basis");

		return _mapQuote.get ("ReferenceBasis");
	}
}
