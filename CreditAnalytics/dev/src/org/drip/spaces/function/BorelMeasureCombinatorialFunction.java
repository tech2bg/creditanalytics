
package org.drip.spaces.function;

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
 * BorelMeasureCombinatorialFunction exposes the normed Discrete Functional Spaces containing the
 * 	Combinatorial Elements and their associated Probability Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BorelMeasureCombinatorialFunction {
	private org.drip.function.deterministic.AbstractMultivariate _amFunc = null;
	private org.drip.spaces.measure.DiscreteCombinatorialBorelSigma _bmcs = null;

	private double[] KeyToDoubleArray (
		final java.lang.Object objKey)
	{
		if (!(objKey instanceof java.lang.Double[])) return null;

		java.lang.Double[] aObjValue = (java.lang.Double[]) objKey;

		int iNumValue = aObjValue.length;
		double[] adblValue = new double[iNumValue];

		for (int i = 0; i < iNumValue; ++i)
			adblValue[i] = aObjValue[i];

		return adblValue;
	}

	/**
	 * BorelMeasureCombinatorialFunction Constructor
	 * 
	 * @param amFunc Multivariate Function
	 * @param bmcs Underlying Borel Measure Combinatorial Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BorelMeasureCombinatorialFunction (
		final org.drip.function.deterministic.AbstractMultivariate amFunc,
		final org.drip.spaces.measure.DiscreteCombinatorialBorelSigma bmcs)
		throws java.lang.Exception
	{
		if (null == (_bmcs = bmcs) || null == (_amFunc = amFunc))
			throw new java.lang.Exception ("BorelMeasureCombinatorialFunction Constructor: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Multivariate Function
	 * 
	 * @return The Underlying Multivariate Function
	 */

	public org.drip.function.deterministic.AbstractMultivariate multivariateFunction()
	{
		return _amFunc;
	}

	/**
	 * Retrieve the Underlying Borel-Algebra Combinatorial Metric Space
	 * 
	 * @return The Underlying Borel-Algebra Combinatorial Metric Space
	 */

	public org.drip.spaces.measure.DiscreteCombinatorialBorelSigma metricSpace()
	{
		return _bmcs;
	}

	/**
	 * Compute the ESS (i.e., the Essential Spectrum) of the Spanning Space
	 * 
	 * @return The ESS
	 * 
	 * @throws java.lang.Exception Thrown if the ESS cannot be calculated
	 */

	public double ess()
		throws java.lang.Exception
	{
		double dblESS = java.lang.Double.NaN;
		double dblESSNorm = java.lang.Double.NaN;

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : _bmcs.sigmaMeasure().entrySet()) {
			double dblInstanceFunctionValue = java.lang.Math.abs (_amFunc.evaluate (KeyToDoubleArray
				(me.getKey())));

			if (!org.drip.quant.common.NumberUtil.IsValid (dblESS))
				dblESSNorm = (dblESS = dblInstanceFunctionValue) * me.getValue();
			else {
				double dblLocalESSNorm = dblInstanceFunctionValue * me.getValue();

				if (dblLocalESSNorm > dblESSNorm) {
					dblESS = dblInstanceFunctionValue;
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
	 * 
	 * @throws java.lang.Exception Thrown if the ESS cannot be calculated
	 */

	public double norm()
		throws java.lang.Exception
	{
		int iPNorm = _bmcs.pNorm();

		if (0 == iPNorm) return ess();

		double dblNorm = 0.;

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : _bmcs.sigmaMeasure().entrySet())
			dblNorm += java.lang.Math.pow (java.lang.Math.abs (_amFunc.evaluate (KeyToDoubleArray
				(me.getKey()))), iPNorm) * me.getValue();

		return java.lang.Math.pow (dblNorm, 1. / iPNorm);
	}
}
