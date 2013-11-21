
package org.drip.spline.bspline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * ExponentialTensionLeftHat implements the left exponential hat basis function laid out in the basic
 *	framework outlined in Koch and Lyche (1989), Koch and Lyche (1993), and Kvasov (2000) Papers.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ExponentialTensionLeftHat extends org.drip.spline.bspline.TensionBasisHat {

	/**
	 * ExponentialTensionLeftHat constructor
	 * 
	 * @param dblTension Tension of the Tension Hat Function
	 * @param dblLeftPredictorOrdinate The Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate The Right Predictor Ordinate
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public ExponentialTensionLeftHat (
		final double dblTension,
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate)
		throws java.lang.Exception
	{
		super (dblTension, dblLeftPredictorOrdinate, dblRightPredictorOrdinate);
	}

	@Override public double evaluate (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!in (dblPredictorOrdinate)) return 0.;

		return java.lang.Math.sinh (tension() * (dblPredictorOrdinate - left())) * normalizer();
	}

	@Override public double calcDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (0 > iOrder)
			throw new java.lang.Exception ("ExponentialTensionLeftHat::calcDerivative => Invalid Inputs");

		if (!in (dblPredictorOrdinate)) return 0.;

		return java.lang.Math.pow (tension(), iOrder) * (0 == iOrder % 2 ? java.lang.Math.sinh (tension() *
			(dblPredictorOrdinate - left())) : java.lang.Math.cosh (tension() * (dblPredictorOrdinate -
				left()))) / normalizer();
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("ExponentialTensionLeftHat::integrate => Invalid Inputs");

		return (java.lang.Math.cosh (tension() * (dblEnd - left())) - java.lang.Math.cosh (tension() *
			(dblBegin - left()))) * normalizer() / tension();
	}
}
