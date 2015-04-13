
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
 * RdToR1 provides the evaluation of the R^d -> R^1 objective function and its derivatives for a specified
 * 	set of R^d variates. Default implementation of the derivatives are for non-analytical black box objective
 * 	functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class RdToR1 {
	private static final int EXTREMA_SAMPLING = 10000;
	private static final int QUADRATURE_SAMPLING = 10000;

	protected org.drip.quant.calculus.DerivativeControl _dc = null;

	/**
	 * Validate the Input Double Array
	 * 
	 * @param adblVariate The Input Double Array
	 * 
	 * @return The Input Double Array consists of valid Values
	 */

	public static final boolean ValidateInput (
		final double[] adblVariate)
	{
		if (null == adblVariate) return false;

		int iNumVariate = adblVariate.length;

		if (0 == iNumVariate) return false;

		for (int i = 0; i < iNumVariate; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (adblVariate[i])) return false;
		}

		return true;
	}

	protected RdToR1 (
		final org.drip.quant.calculus.DerivativeControl dc)
	{
		if (null == (_dc = dc)) _dc = new org.drip.quant.calculus.DerivativeControl();
	}

	/**
	 * Evaluate for the given input variate
	 * 
	 * @param adblVariate Array of Input Variates
	 *  
	 * @return The calculated value
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception;

	/**
	 * Calculate the Differential
	 * 
	 * @param adblVariate Variate Array at which the derivative is to be calculated
	 * @param dblOFBase Base Value for the Objective Function
	 * @param iVariateIndex Index of the Variate whose Derivative is to be computed
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative
	 */

	public org.drip.quant.calculus.Differential differential (
		final double[] adblVariate,
		final double dblOFBase,
		final int iVariateIndex,
		final int iOrder)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblVariate) || 0 >= iOrder) return null;

		double dblDerivative = 0.;
		int iNumVariate = adblVariate.length;
		double dblOrderedVariateInfinitesimal = 1.;
		double dblVariateInfinitesimal = java.lang.Double.NaN;

		if (iNumVariate <= iVariateIndex) return null;

		try {
			dblVariateInfinitesimal = _dc.getVariateInfinitesimal (adblVariate[iVariateIndex]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i <= iOrder; ++i) {
			if (0 != i) dblOrderedVariateInfinitesimal *= (2. * dblVariateInfinitesimal);

			double[] adblVariateIncremental = new double[iNumVariate];

			for (int j = 0; i < iNumVariate; ++j)
				adblVariateIncremental[j] = j == iVariateIndex ? adblVariate[j] + dblVariateInfinitesimal *
					(iOrder - 2. * i) : adblVariate[j];

			try {
				dblDerivative += (i % 2 == 0 ? 1 : -1) * org.drip.quant.common.NumberUtil.NCK (iOrder, i) *
					evaluate (adblVariateIncremental);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.quant.calculus.Differential (dblOrderedVariateInfinitesimal, dblDerivative);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the Differential
	 * 
	 * @param adblVariate Variate Array at which the derivative is to be calculated
	 * @param iVariateIndex Index of the Variate whose Derivative is to be computed
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative
	 */

	public org.drip.quant.calculus.Differential differential (
		final double[] adblVariate,
		final int iVariateIndex,
		final int iOrder)
	{
		try {
			return differential (adblVariate, evaluate (adblVariate), iVariateIndex, iOrder);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the derivative as a double
	 * 
	 * @param adblVariate Variate Array at which the derivative is to be calculated
	 * @param iVariateIndex Index of the Variate whose Derivative is to be computed
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative
	 */

	public double derivative (
		final double[] adblVariate,
		final int iVariateIndex,
		final int iOrder)
		throws java.lang.Exception
	{
		return differential (adblVariate, evaluate (adblVariate), iVariateIndex, iOrder).calcSlope (true);
	}

	/**
	 * Integrate over the given Input Range Using Uniform Monte-Carlo
	 * 
	 * @param adblLeftEdge Array of Input Left Edge
	 * @param adblRightEdge Array of Input Right Edge
	 *  
	 * @return The Result of the Integration over the specified Range
	 * 
	 * @throws java.lang.Exception Thrown if the Integration cannot be done
	 */

	public double integrate (
		final double[] adblLeftEdge,
		final double[] adblRightEdge)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblLeftEdge) ||
			!org.drip.quant.common.NumberUtil.IsValid (adblRightEdge))
			throw new java.lang.Exception ("RdToR1::integrate => Invalid Inputs");

		double dblIntegrand = 0.;
		int iNumVariate = adblLeftEdge.length;
		double[] adblVariate = new double[iNumVariate];
		double[] adblVariateWidth = new double[iNumVariate];

		if (adblRightEdge.length != iNumVariate)
			throw new java.lang.Exception ("RdToR1::integrate => Invalid Inputs");

		for (int j = 0; j < iNumVariate; ++j)
			adblVariateWidth[j] = adblRightEdge[j] - adblLeftEdge[j];

		for (int i = 0; i < QUADRATURE_SAMPLING; ++i) {
			for (int j = 0; j < iNumVariate; ++j)
				adblVariate[j] = adblLeftEdge[j] + java.lang.Math.random() * adblVariateWidth[j];

			dblIntegrand += evaluate (adblVariate);
		}

		for (int j = 0; j < iNumVariate; ++j)
			dblIntegrand = dblIntegrand * adblVariateWidth[j];

		return dblIntegrand / QUADRATURE_SAMPLING;
	}

	/**
	 * Compute the Maximum VOP within the Variate Array Range Using Uniform Monte-Carlo
	 * 
	 * @param adblVariateLeft The Range Left End Array
	 * @param adblVariateRight The Range Right End Array
	 * 
	 * @return The Maximum VOP
	 */

	public org.drip.function.deterministic.VariateOutputPair maxima (
		final double[] adblVariateLeft,
		final double[] adblVariateRight)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblVariateLeft) ||
			!org.drip.quant.common.NumberUtil.IsValid (adblVariateRight))
			return null;

		double dblValue = java.lang.Double.NaN;
		double dblMaxima = java.lang.Double.NaN;
		int iNumVariate = adblVariateLeft.length;
		double[] adblVariate = new double[iNumVariate];
		double[] adblVariateWidth = new double[iNumVariate];
		double[] adblMaximaVariate = new double[iNumVariate];

		if (adblVariateRight.length != iNumVariate) return null;

		for (int j = 0; j < iNumVariate; ++j)
			adblVariateWidth[j] = adblVariateRight[j] - adblVariateLeft[j];

		for (int i = 0; i < EXTREMA_SAMPLING; ++i) {
			for (int j = 0; j < iNumVariate; ++j)
				adblVariate[j] = adblVariateLeft[j] + java.lang.Math.random() * adblVariateWidth[j];

			try {
				dblValue = evaluate (adblVariate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblMaxima)) {
				dblMaxima = dblValue;

				for (int j = 0; j < iNumVariate; ++j)
					adblMaximaVariate[j] = adblVariate[j];
			} else {
				if (dblMaxima < dblValue) {
					dblMaxima = dblValue;

					for (int j = 0; j < iNumVariate; ++j)
						adblMaximaVariate[j] = adblVariate[j];
				}
			}
		}

		try {
			return new org.drip.function.deterministic.VariateOutputPair (adblMaximaVariate, dblMaxima);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Minimum VOP within the Variate Array Range Using Uniform Monte-Carlo
	 * 
	 * @param adblVariateLeft The Range Left End Array
	 * @param adblVariateRight The Range Right End Array
	 * 
	 * @return The Minimum VOP
	 */

	public org.drip.function.deterministic.VariateOutputPair minima (
		final double[] adblVariateLeft,
		final double[] adblVariateRight)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (adblVariateLeft) ||
			!org.drip.quant.common.NumberUtil.IsValid (adblVariateRight))
			return null;

		double dblValue = java.lang.Double.NaN;
		double dblMinima = java.lang.Double.NaN;
		int iNumVariate = adblVariateLeft.length;
		double[] adblVariate = new double[iNumVariate];
		double[] adblVariateWidth = new double[iNumVariate];
		double[] adblMinimaVariate = new double[iNumVariate];

		if (adblVariateRight.length != iNumVariate) return null;

		for (int j = 0; j < iNumVariate; ++j)
			adblVariateWidth[j] = adblVariateRight[j] - adblVariateLeft[j];

		for (int i = 0; i < EXTREMA_SAMPLING; ++i) {
			for (int j = 0; j < iNumVariate; ++j)
				adblVariate[j] = adblVariateLeft[j] + java.lang.Math.random() * adblVariateWidth[j];

			try {
				dblValue = evaluate (adblVariate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblMinima)) {
				dblMinima = dblValue;

				for (int j = 0; j < iNumVariate; ++j)
					adblMinimaVariate[j] = adblVariate[j];
			} else {
				if (dblMinima > dblValue) {
					dblMinima = dblValue;

					for (int j = 0; j < iNumVariate; ++j)
						adblMinimaVariate[j] = adblVariate[j];
				}
			}
		}

		try {
			return new org.drip.function.deterministic.VariateOutputPair (adblMinimaVariate, dblMinima);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
