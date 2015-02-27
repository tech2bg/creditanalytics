
package org.drip.function.deterministic;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * AbstractUnivariate provides the evaluation of the objective function and its derivatives for a specified
 * 	variate. Default implementation of the derivatives are for non-analytical black box objective functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class AbstractUnivariate {
	protected org.drip.quant.calculus.DerivativeControl _dc = null;

	/**
	 * Objective Function constructor
	 * 
	 * @param dc Derivative Control
	 */

	public AbstractUnivariate (
		final org.drip.quant.calculus.DerivativeControl dc)
	{
		if (null == (_dc = dc)) _dc = new org.drip.quant.calculus.DerivativeControl();
	}

	/**
	 * Evaluate for the given variate
	 * 
	 * @param dblVariate Variate
	 *  
	 * @return Returns the calculated value
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public abstract double evaluate (
		final double dblVariate)
		throws java.lang.Exception;

	/**
	 * Calculate the Differential
	 * 
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param dblOFBase Base Value for the Objective Function
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative
	 */

	public org.drip.quant.calculus.Differential differential (
		final double dblVariate,
		final double dblOFBase,
		final int iOrder)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblVariate) || 0 >= iOrder) return null;

		double dblDerivative = 0.;
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

			try {
				dblDerivative += (i % 2 == 0 ? 1 : -1) * org.drip.quant.common.NumberUtil.NCK (iOrder, i) *
					evaluate (dblVariate + dblVariateInfinitesimal * (iOrder - 2. * i));
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
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative
	 */

	public org.drip.quant.calculus.Differential differential (
		final double dblVariate,
		final int iOrder)
	{
		try {
			return differential (dblVariate, evaluate (dblVariate), iOrder);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the derivative as a double
	 * 
	 * @param dblVariate Variate at which the derivative is to be calculated
	 * @param iOrder Order of the derivative to be computed
	 * 
	 * @return The Derivative
	 */

	public double derivative (
		final double dblVariate,
		final int iOrder)
		throws java.lang.Exception
	{
		return differential (dblVariate, evaluate (dblVariate), iOrder).calcSlope (true);
	}

	/**
	 * Integrate over the given range
	 * 
	 * @param dblBegin Range Begin 
	 * @param dblEnd Range End 
	 *  
	 * @return The Integrated Value
	 * 
	 * @throws java.lang.Exception Thrown if evaluation cannot be done
	 */

	public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
	}
}
