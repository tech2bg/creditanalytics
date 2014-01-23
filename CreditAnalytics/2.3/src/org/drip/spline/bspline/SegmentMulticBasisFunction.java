
package org.drip.spline.bspline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * SegmentMulticBasisFunction implements the local quadratic B Spline that envelopes the predictor ordinates,
 *  and the corresponding set of ordinates/basis functions. SegmentMulticBasisFunction uses the left/right
 *  SegmentBasisFunction instances to achieve its implementation goals.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SegmentMulticBasisFunction extends org.drip.spline.bspline.SegmentBasisFunction {
	private org.drip.spline.bspline.SegmentBasisFunction _oeLeft = null;
	private org.drip.spline.bspline.SegmentBasisFunction _oeRight = null;

	/**
	 * SegmentMulticBasisFunction constructor
	 * 
	 * @param oeLeft Left Ordered Envelope Spline Function
	 * @param oeRight Right Ordered Envelope Spline Function
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public SegmentMulticBasisFunction (
		final org.drip.spline.bspline.SegmentBasisFunction oeLeft,
		final org.drip.spline.bspline.SegmentBasisFunction oeRight)
		throws java.lang.Exception
	{
		super (oeLeft.bSplineOrder() + 1, oeLeft.leading(), oeRight.leading(), oeRight.trailing());

		if ((_oeLeft = oeLeft).bSplineOrder() != (_oeRight = oeRight).bSplineOrder())
			throw new java.lang.Exception ("SegmentMulticBasisFunction ctr: Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("SegmentMulticBasisFunction::evaluate => Invalid Inputs");

		if (dblPredictorOrdinate < leading() || dblPredictorOrdinate > trailing()) return 0.;

		return _oeLeft.normalizedCumulative (dblPredictorOrdinate) - _oeRight.normalizedCumulative
			(dblPredictorOrdinate);
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		return org.drip.quant.calculus.Integrator.Simpson (this, dblBegin, dblEnd);
	}

	@Override public double normalizer()
		throws java.lang.Exception
	{
		return integrate (leading(), trailing());
	}

	@Override public double normalizedCumulative (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception
				("SegmentMulticBasisFunction::normalizedCumulative => Invalid Inputs");

		if (dblPredictorOrdinate < leading()) return 0.;

		if (dblPredictorOrdinate > trailing()) return 1.;

		return integrate (leading(), dblPredictorOrdinate) / normalizer();
	}
}
