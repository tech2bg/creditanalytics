
package org.drip.spaces.measure;

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
 * DiscreteCombinatorialBorelSigma exposes the normed Discrete Spaces containing the Combinatorial Elements
 * 	and their associated Probability Measure over a Borel Set satisfying the Sigma Algebra.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscreteCombinatorialBorelSigma extends org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector implements
	org.drip.spaces.metric.GeneralizedMetricSpace, org.drip.spaces.measure.BorelSigma {
	private int _iPNorm = -1;
	private java.util.Map<java.lang.Double, java.lang.Double> _mapSigmaMeasure = null;

	/**
	 * DiscreteCombinatorialBorelSigma Constructor
	 * 
	 * @param iPNorm p-Norm
	 * @param mapSigmaMeasure The Map of the Discrete Probability Measures
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public DiscreteCombinatorialBorelSigma (
		final int iPNorm,
		final java.util.Map<java.lang.Double, java.lang.Double> mapSigmaMeasure)
		throws java.lang.Exception
	{
		super (mapSigmaMeasure.keySet());

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("DiscreteCombinatorialBorelSigma ctr: Invalid Inputs");

		_mapSigmaMeasure = mapSigmaMeasure;
	}

	/**
	 * Retrieve the Borel Sigma Measure
	 * 
	 * @return The Borel Sigma Measure
	 */

	public java.util.Map<java.lang.Double, java.lang.Double> sigmaMeasure()
	{
		return _mapSigmaMeasure;
	}

	@Override public int pNorm()
	{
		return _iPNorm;
	}

	@Override public double populationESS()
		throws java.lang.Exception
	{
		double dblESS = java.lang.Double.NaN;
		double dblESSNorm = java.lang.Double.NaN;

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : _mapSigmaMeasure.entrySet()) {
			double dblInstance = java.lang.Math.abs (me.getKey());

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
	 * Compute the Sample ESS (i.e., the Essential Spectrum) of the Spanning Space
	 * 
	 * @param adblX The Sample
	 * 
	 * @return The Sample ESS
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double sampleESS (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblX))
			throw new java.lang.Exception ("DiscreteCombinatorialBorelSigma::sampleESS => Invalid Inputs");

		int iDimension = adblX.length;
		double dblESS = java.lang.Double.NaN;
		double dblESSMode = java.lang.Double.NaN;

		for (int i = 0; i < iDimension; ++i) {
			if (_mapSigmaMeasure.containsKey (adblX[i])) {
				double dblCurrentESSMode = _mapSigmaMeasure.get (adblX[i]);

				if (!org.drip.quant.common.NumberUtil.IsValid (dblESS)) {
					dblESS = adblX[i];
					dblESSMode = dblCurrentESSMode;
				} else {
					if (dblESSMode < dblCurrentESSMode) {
						dblESS = adblX[i];
						dblESSMode = dblCurrentESSMode;
					}
				}
			}
		}

		return dblESS;
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		if (0 == _iPNorm) return populationESS();

		double dblNorm = 0.;

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : _mapSigmaMeasure.entrySet())
			dblNorm += java.lang.Math.pow (java.lang.Math.abs (me.getKey()), _iPNorm) * me.getValue();

		return java.lang.Math.pow (dblNorm, 1. / _iPNorm);
	}

	public double sampleMetricNorm (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (0 == _iPNorm) return sampleESS (adblX);

		if (!org.drip.quant.common.NumberUtil.IsValid (adblX))
			throw new java.lang.Exception
				("DiscreteCombinatorialBorelSigma::sampleMetricNorm => Invalid Inputs");

		double dblNorm = 0.;
		int iDimension = adblX.length;

		for (int i = 0; i < iDimension; ++i) {
			if (_mapSigmaMeasure.containsKey (adblX[i]))
				dblNorm += java.lang.Math.pow (adblX[i], _iPNorm) * _mapSigmaMeasure.get (adblX[i]);
		}

		return java.lang.Math.pow (dblNorm, 1. / _iPNorm);
	}
}
