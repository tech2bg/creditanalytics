
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
 * MonicEnvelope implements the local monic B Spline that envelopes the predictor ordinates, and the
 *  corresponding set of ordinates/basis functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MonicEnvelope extends org.drip.spline.bspline.OrderedEnvelope {
	private org.drip.spline.bspline.TensionBasisHat _tbhLeft = null;
	private org.drip.spline.bspline.TensionBasisHat _tbhRight = null;

	/**
	 * MonicEnvelope constructor
	 * 
	 * @param dblLeadingPredictorOrdinate The Leading/First Predictor Ordinate
	 * @param dblFollowingPredictorOrdinate The Following/Next Predictor Ordinate
	 * @param dblTrailingPredictorOrdinate The Trailing/Final Predictor Ordinate
	 * @param tbhLeft Left Tension Basis Hat Function
	 * @param tbhRight Right Tension Basis Hat Function
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public MonicEnvelope (
		final double dblLeadingPredictorOrdinate,
		final double dblFollowingPredictorOrdinate,
		final double dblTrailingPredictorOrdinate,
		final org.drip.spline.bspline.TensionBasisHat tbhLeft,
		final org.drip.spline.bspline.TensionBasisHat tbhRight)
		throws java.lang.Exception
	{
		super (dblLeadingPredictorOrdinate, dblFollowingPredictorOrdinate, dblTrailingPredictorOrdinate);

		if (null == (_tbhLeft = tbhLeft) || null == (_tbhRight = tbhRight))
			throw new java.lang.Exception ("MonicEnvelope ctr: Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("MonicEnvelope::evaluate => Invalid Inputs");

		if (dblPredictorOrdinate < leading() || dblPredictorOrdinate > trailing()) return 0.;

		return dblPredictorOrdinate < following() ? _tbhLeft.evaluate
			(dblPredictorOrdinate) : _tbhRight.evaluate (dblPredictorOrdinate);
	}

	@Override public double calcDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("MonicEnvelope::calcDerivative => Invalid Inputs");

		if (dblPredictorOrdinate < leading() || dblPredictorOrdinate > trailing()) return 0.;

		return dblPredictorOrdinate < following() ? _tbhLeft.calcDerivative
			(dblPredictorOrdinate, iOrder) : _tbhRight.calcDerivative (dblPredictorOrdinate, iOrder);
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("MonicEnvelope::integrate => Invalid Inputs");

		if (dblBegin <= leading()) {
			if (dblEnd <= leading()) return 0.;

			if (dblEnd <= following()) return _tbhLeft.integrate (leading(), dblEnd);

			if (dblEnd <= trailing())
				return _tbhLeft.integrate (leading(), following()) + _tbhRight.integrate (following(),
					dblEnd);

			return _tbhLeft.integrate (leading(), following()) + _tbhRight.integrate (following(),
				trailing());
		}

		if (dblBegin <= following()) {
			if (dblEnd <= following()) return _tbhLeft.integrate (dblBegin, dblEnd);

			if (dblEnd <= trailing())
				return _tbhLeft.integrate (dblBegin, following()) + _tbhRight.integrate (following(),
					dblEnd);

			return _tbhLeft.integrate (dblBegin, following()) + _tbhRight.integrate (following(),
				trailing());
		}

		if (dblBegin <= trailing()) {
			if (dblEnd <= trailing()) return _tbhRight.integrate (following(), dblEnd);

			return _tbhRight.integrate (following(), trailing());
		}

		return 0.;
	}

	@Override public double normalizer()
		throws java.lang.Exception
	{
		return org.drip.quant.calculus.Integrator.Boole (_tbhLeft, leading(), following()) +
			org.drip.quant.calculus.Integrator.Boole (_tbhRight, following(), trailing());
	}

	@Override public double cumulativeIntegrand (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("MonicEnvelope::cumulativeIntegrand => Invalid Inputs");

		if (dblPredictorOrdinate < leading()) return 0.;

		if (dblPredictorOrdinate > trailing()) return 1.;

		if (dblPredictorOrdinate < following())
			return org.drip.quant.calculus.Integrator.Boole (_tbhLeft, leading(), dblPredictorOrdinate) /
				normalizer();

		return (org.drip.quant.calculus.Integrator.Boole (_tbhLeft, leading(), following()) +
			org.drip.quant.calculus.Integrator.Boole (_tbhRight, following(), dblPredictorOrdinate)) /
				normalizer();
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		double[] adblPredictorOrdinate = new double[] {1., 2., 3.};

		org.drip.spline.bspline.TensionBasisHat[] aTBH =
			org.drip.spline.bspline.BasisHatPairGenerator.GenerateHyperbolicTensionMonic
				(adblPredictorOrdinate, 1.);

		MonicEnvelope me = new MonicEnvelope (adblPredictorOrdinate[0], adblPredictorOrdinate[1],
			adblPredictorOrdinate[2], aTBH[0], aTBH[1]);

		System.out.println ("TBH[x = 1.0] : " + aTBH[0].evaluate (1.0) + " | " + aTBH[1].evaluate (1.0) + " | " + me.evaluate (1.0));

		System.out.println ("TBH[x = 1.5] : " + aTBH[0].evaluate (1.5) + " | " + aTBH[1].evaluate (1.5) + " | " + me.evaluate (1.5));

		System.out.println ("TBH[x = 2.0] : " + aTBH[0].evaluate (2.0) + " | " + aTBH[1].evaluate (2.0) + " | " + me.evaluate (2.0));

		System.out.println ("TBH[x = 2.5] : " + aTBH[0].evaluate (2.5) + " | " + aTBH[1].evaluate (2.5) + " | " + me.evaluate (2.5));

		System.out.println ("TBH[x = 3.0] : " + aTBH[0].evaluate (3.0) + " | " + aTBH[1].evaluate (3.0) + " | " + me.evaluate (3.0));

		System.out.println ("CumulativeIntegrand[x = 0.5] : " + me.cumulativeIntegrand (0.5));

		System.out.println ("CumulativeIntegrand[x = 1.0] : " + me.cumulativeIntegrand (1.0));

		System.out.println ("CumulativeIntegrand[x = 1.00001] : " + me.cumulativeIntegrand (1.00001));

		System.out.println ("CumulativeIntegrand[x = 1.5] : " + me.cumulativeIntegrand (1.5));

		System.out.println ("CumulativeIntegrand[x = 2.0] : " + me.cumulativeIntegrand (2.0));

		System.out.println ("CumulativeIntegrand[x = 2.5] : " + me.cumulativeIntegrand (2.5));

		System.out.println ("CumulativeIntegrand[x = 2.9999] : " + me.cumulativeIntegrand (2.9999));

		System.out.println ("CumulativeIntegrand[x = 3.0] : " + me.cumulativeIntegrand (3.0));

		int iOrder = 2;

		System.out.println ("Derivative[x = 0.5] : " + me.calcDerivative (0.5, iOrder));

		System.out.println ("Derivative[x = 1.0] : " + me.calcDerivative (1.0, iOrder));

		System.out.println ("Derivative[x = 1.00001] : " + me.calcDerivative (1.00001, iOrder));

		System.out.println ("Derivative[x = 1.5] : " + me.calcDerivative (1.5, iOrder));

		System.out.println ("Derivative[x = 2.0] : " + me.calcDerivative (2.0, iOrder));

		System.out.println ("Derivative[x = 2.5] : " + me.calcDerivative (2.5, iOrder));

		System.out.println ("Derivative[x = 2.9999] : " + me.calcDerivative (2.9999, iOrder));

		System.out.println ("Derivative[x = 3.0] : " + me.calcDerivative (3.0, iOrder));
	}
}
