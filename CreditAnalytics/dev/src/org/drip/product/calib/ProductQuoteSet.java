
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
 * ProductQuoteSet implements the Calibratable type-free Product Quote Shell. The derived calibration sets
 * 	provide custom accessors.
 * 
 * @author Lakshmi Krishnamurthy
 */

public abstract class ProductQuoteSet {
	private org.drip.state.representation.LatentStateSpecification[] _aLSS = null;

	protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mapQuote = new
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

	protected ProductQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
		throws java.lang.Exception
	{
		if (null == (_aLSS = aLSS) || 0 == _aLSS.length)
			throw new java.lang.Exception ("ProductQuoteSet ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Latent State Specification
	 * 
	 * @return The Array of Latent State Specification
	 */

	public org.drip.state.representation.LatentStateSpecification[] lss()
	{
		return _aLSS;
	}

	/**
	 * Indicates if the Specified External Latent State Specification is contained in the Array
	 * 
	 * @param strLatentState The Latent State
	 * @param strLatentStateQuantificationMetric The Latent State Quantification Metric
	 * @param label The Specific Latent State Label
	 * 
	 * @return TRUE => The Specified External Latent State Specification is contained in the Array
	 */

	public boolean contains (
		final java.lang.String strLatentState,
		final java.lang.String strLatentStateQuantificationMetric,
		final org.drip.state.identifier.LatentStateLabel label)
	{
		org.drip.state.representation.LatentStateSpecification lssExternal = null;

		try {
			lssExternal = new org.drip.state.representation.LatentStateSpecification (strLatentState,
				strLatentStateQuantificationMetric, label);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		for (org.drip.state.representation.LatentStateSpecification lss : _aLSS) {
			if (lss.match (lssExternal)) return true;
		}

		return false;
	}
}
