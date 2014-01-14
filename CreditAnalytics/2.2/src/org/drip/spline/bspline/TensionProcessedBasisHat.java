
package org.drip.spline.bspline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * TensionProcessedBasisHat implements the processed hat basis function of the form laid out in the basic
 *	framework outlined in Koch and Lyche (1989), Koch and Lyche (1993), and Kvasov (2000) Papers.
 *
 * @author Lakshmi Krishnamurthy
 */

public class TensionProcessedBasisHat extends org.drip.spline.bspline.TensionBasisHat {
	private int _iDerivOrder = -1;
	private org.drip.spline.bspline.TensionBasisHat _tbhRaw = null;

	/**
	 * TensionProcessedBasisHat constructor
	 * 
	 * @param tbhRaw The Raw TBH
	 * @param iDerivOrder Derivative Order off of the Raw TBH
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public TensionProcessedBasisHat (
		final org.drip.spline.bspline.TensionBasisHat tbhRaw,
		final int iDerivOrder)
		throws java.lang.Exception
	{
		super (tbhRaw.left(), tbhRaw.right(), tbhRaw.tension());

		if (null == (_tbhRaw = tbhRaw) || 0 >= (_iDerivOrder = iDerivOrder))
			throw new java.lang.Exception ("TensionProcessedBasisHat ctr: Invalid Input");
	}

	@Override public double evaluate (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!in (dblPredictorOrdinate)) return 0.;

		return _tbhRaw.calcDerivative (dblPredictorOrdinate, _iDerivOrder);
	}

	@Override public double calcDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (0 > iOrder)
			throw new java.lang.Exception ("TensionProcessedBasisHat::calcDerivative => Invalid Inputs");

		if (!in (dblPredictorOrdinate)) return 0.;

		return _tbhRaw.calcDerivative (dblPredictorOrdinate, iOrder + _iDerivOrder);
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("TensionProcessedBasisHat::integrate => Invalid Inputs");

		double dblBoundedBegin = org.drip.quant.common.NumberUtil.Bound (dblBegin, left(), right());

		double dblBoundedEnd = org.drip.quant.common.NumberUtil.Bound (dblEnd, left(), right());

		if (dblBoundedBegin >= dblBoundedEnd) return 0.;

		if (1 == _iDerivOrder) return _tbhRaw.evaluate (dblBoundedEnd) - _tbhRaw.evaluate (dblBoundedBegin);

		return _tbhRaw.calcDerivative (dblBoundedEnd, _iDerivOrder - 1) - _tbhRaw.calcDerivative
			(dblBoundedBegin, _iDerivOrder - 1);
	}

	@Override public double normalizer()
		throws java.lang.Exception
	{
		if (1 == _iDerivOrder) return _tbhRaw.evaluate (right()) - _tbhRaw.evaluate (left());

		return _tbhRaw.calcDerivative (right(), _iDerivOrder - 1) - _tbhRaw.calcDerivative (left(),
			_iDerivOrder - 1);
	}
}
