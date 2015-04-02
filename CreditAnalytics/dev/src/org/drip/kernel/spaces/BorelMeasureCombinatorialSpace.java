
package org.drip.kernel.spaces;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * BorelMeasureCombinatorialSpace exposes the normed Discrete Spaces containing the Combinatorial Elements
 * 	and their associated Probability Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BorelMeasureCombinatorialSpace extends org.drip.kernel.spaces.CombinatorialSpace {
	private int _iPNorm = -1;
	private java.util.Map<java.lang.Object, java.lang.Double> _mapSigmaMeasure = null;

	/**
	 * BorelMeasureCombinatorialSpace Constructor
	 * 
	 * @param iPNorm p-Norm
	 * @param mapSigmaMeasure The Map of the Discrete Probability Measures
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BorelMeasureCombinatorialSpace (
		final int iPNorm,
		final java.util.Map<java.lang.Object, java.lang.Double> mapSigmaMeasure)
		throws java.lang.Exception
	{
		super (mapSigmaMeasure.keySet());

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("BorelMeasureCombinatorialSpace ctr: Invalid Inputs");

		_mapSigmaMeasure = mapSigmaMeasure;
	}

	/**
	 * Retrieve the Borel Sigma Measure
	 * 
	 * @return The Borel Sigma Measure
	 */

	public java.util.Map<java.lang.Object, java.lang.Double> sigmaMeasure()
	{
		return _mapSigmaMeasure;
	}

	/**
	 * Retrieve the P-Norm of the Banach Space
	 * 
	 * @return The P-Norm of the Banach Space
	 */

	public int pnorm()
	{
		return _iPNorm;
	}

	/**
	 * Compute the ESS (i.e., the Essential Spectrum) of the Spanning Space
	 * 
	 * @return The ESS
	 */

	public double ess()
	{
		double dblESS = java.lang.Double.NaN;
		double dblESSNorm = java.lang.Double.NaN;

		for (java.util.Map.Entry<java.lang.Object, java.lang.Double> me : _mapSigmaMeasure.entrySet()) {
			double dblInstance = java.lang.Math.abs ((java.lang.Double) me.getKey());

			if (!org.drip.quant.common.NumberUtil.IsValid (dblESS))
				dblESSNorm = (dblESS = dblInstance) * me.getValue();
			else {
				double dblLocalESSNorm = dblInstance * me.getValue();

				if (dblLocalESSNorm > dblESSNorm) {
					dblESS = dblInstance;
					dblESSNorm = dblLocalESSNorm;
				}
			}
		}

		return dblESS;
	}

	/**
	 * Compute the P-Norm of the Spanning Space
	 * 
	 * @return The P-Norm
	 */

	public double norm()
	{
		if (0 == _iPNorm) return ess();

		double dblNorm = 0.;

		for (java.util.Map.Entry<java.lang.Object, java.lang.Double> me : _mapSigmaMeasure.entrySet())
			dblNorm += java.lang.Math.pow (java.lang.Math.abs ((java.lang.Double) me.getKey()), _iPNorm) *
				me.getValue();

		return java.lang.Math.pow (dblNorm, 1. / _iPNorm);
	}
}
