
package org.drip.function.deterministic;

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
 * R1ToRd provides the evaluation of the R^1 -> R^d Objective Function and its derivatives for a specified
 *  variate. Default implementation of the derivatives are for non-analytical black box objective functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class R1ToRd {
	private static final int QUADRATURE_SAMPLING = 10000;

	protected org.drip.quant.calculus.DerivativeControl _dc = null;

	protected R1ToRd (
		final org.drip.quant.calculus.DerivativeControl dc)
	{
		if (null == (_dc = dc)) _dc = new org.drip.quant.calculus.DerivativeControl();
	}

	/**
	 * Evaluate for the given Input R^1 Variate
	 * 
	 * @param adblVariate Array of Input R^1 Variate
	 *  
	 * @return The Output R^d Array
	 */

	public abstract double[] evaluate (
		final double dblVariate);

	/**
	 * Calculate the Array of Differentials
	 * 
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Array of Differentials
	 */

	public org.drip.quant.calculus.Differential[] differential (
		final double dblVariate,
		final int iOrder)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate) || 0 >= iOrder) return null;

		int iOutputDimension = -1;
		double[] adblDerivative = null;
		double dblOrderedVariateInfinitesimal = 1.;
		double dblVariateInfinitesimal = java.lang.Double.NaN;

		try {
			dblVariateInfinitesimal = _dc.getVariateInfinitesimal (dblVariate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i <= iOrder; ++i) {
			if (0 != i) dblOrderedVariateInfinitesimal *= (2. * dblVariateInfinitesimal);

			double dblVariateIncremental = dblVariateInfinitesimal * (iOrder - 2. * i);

			double[] adblValue = evaluate (dblVariateIncremental);

			if (null == adblValue || 0 == (iOutputDimension = adblValue.length)) return null;

			if (null == adblDerivative) {
				adblDerivative = new double[iOutputDimension];

				for (int j = 0; j < iOutputDimension; ++j)
					adblDerivative[j] = 0.;
			}

			try {
				for (int j = 0; j < iOutputDimension; ++j)
					adblDerivative[j] += (i % 2 == 0 ? 1 : -1) * org.drip.quant.common.NumberUtil.NCK
						(iOrder, i) * adblValue[j];
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.quant.calculus.Differential[] aDiff = new
			org.drip.quant.calculus.Differential[iOutputDimension];

		try {
			for (int j = 0; j < iOutputDimension; ++j)
				aDiff[j] = new org.drip.quant.calculus.Differential (dblOrderedVariateInfinitesimal,
					adblDerivative[j]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return aDiff;
	}

	/**
	 * Calculate the Derivative Array as a double
	 * 
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative Array
	 */

	public double[] derivative (
		final double dblVariate,
		final int iOrder)
	{
		org.drip.quant.calculus.Differential[] aDiff = differential (dblVariate, iOrder);

		if (null == aDiff) return null;

		int iOutputDimension = aDiff.length;
		double[] adblDerivative = new double[iOutputDimension];

		if (0 == iOutputDimension) return null;

		for (int i = 0; i < iOutputDimension; ++i)
			adblDerivative[i] = aDiff[i].calcSlope (true);

		return adblDerivative;
	}

	/**
	 * Integrate over the given Input Range Using Uniform Monte-Carlo
	 * 
	 * @param dblLeftEdge Input Left Edge
	 * @param dblRightEdge Input Right Edge
	 *  
	 * @return The Array Containing the Result of the Integration over the specified Range
	 */

	public double[] integrate (
		final double dblLeftEdge,
		final double dblRightEdge)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblLeftEdge) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblRightEdge) || dblRightEdge <= dblLeftEdge)
			return null;

		int iOutputDimension = -1;
		double[] adblIntegrand = null;
		double dblVariateWidth = dblRightEdge - dblLeftEdge;

		for (int i = 0; i < QUADRATURE_SAMPLING; ++i) {
			double[] adblValue = evaluate (dblLeftEdge + java.lang.Math.random() * dblVariateWidth);

			if (null == adblValue || 0 == (iOutputDimension = adblValue.length)) return null;

			if (null == adblIntegrand) adblIntegrand = new double[iOutputDimension];

			for (int j = 0; j < iOutputDimension; ++j)
				adblIntegrand[j] += adblValue[j];
		}

		for (int i = 0; i < iOutputDimension; ++i)
			adblIntegrand[i] *= (dblVariateWidth / QUADRATURE_SAMPLING);

		return adblIntegrand;
	}
}
