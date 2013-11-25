
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
 * CubicRationalLeftRaw implements the raw left cubic rational hat basis function laid out in the basic
 *	framework outlined in Koch and Lyche (1989), Koch and Lyche (1993), and Kvasov (2000) Papers.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CubicRationalLeftRaw extends org.drip.spline.bspline.TensionBasisHat {

	/**
	 * Cubic Polynomial with No Shape Controller
	 */

	public static final java.lang.String SHAPE_CONTROL_NONE = "SHAPE_CONTROL_NONE";

	/**
	 * Cubic Polynomial with Rational Linear Shape Controller
	 */

	public static final java.lang.String SHAPE_CONTROL_RATIONAL_LINEAR =
		"SHAPE_CONTROL_RATIONAL_LINEAR";

	/**
	 * Cubic Polynomial with Rational Quadratic Shape Controller
	 */

	public static final java.lang.String SHAPE_CONTROL_RATIONAL_QUADRATIC =
		"SHAPE_CONTROL_RATIONAL_QUADRATIC";

	private java.lang.String _strShapeControlType = "";

	/**
	 * CubicRationalLeftRaw constructor
	 * 
	 * @param strShapeControlType Type of the Shape Controller to be used - NONE, LINEAR/QUADRATIC Rational
	 * @param dblTension Tension of the Tension Hat Function
	 * @param dblLeftPredictorOrdinate The Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate The Right Predictor Ordinate
	 * 
	 * @throws java.lang.Exception Thrown if the input is invalid
	 */

	public CubicRationalLeftRaw (
		final java.lang.String strShapeControlType,
		final double dblTension,
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate)
		throws java.lang.Exception
	{
		super (dblTension, dblLeftPredictorOrdinate, dblRightPredictorOrdinate);

		if (null == (_strShapeControlType = strShapeControlType) || (!SHAPE_CONTROL_NONE.equalsIgnoreCase
			(_strShapeControlType) && !SHAPE_CONTROL_RATIONAL_LINEAR.equalsIgnoreCase (_strShapeControlType)
				&& !SHAPE_CONTROL_RATIONAL_QUADRATIC.equalsIgnoreCase (_strShapeControlType)))
			throw new java.lang.Exception ("CubicRationalLeftRaw ctr: Invalid Inputs");
	}

	@Override public double evaluate (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!in (dblPredictorOrdinate)) return 0.;

		double dblCubicValue = (dblPredictorOrdinate - left()) * (dblPredictorOrdinate - left()) *
			(dblPredictorOrdinate - left());

		if (SHAPE_CONTROL_NONE.equalsIgnoreCase (_strShapeControlType)) return dblCubicValue;

		double dblWidth = right() - left();

		double dblScale = 1. / (dblWidth * (6. + 6. * tension() * dblWidth + 2. * tension() * dblWidth *
			dblWidth));

		if (SHAPE_CONTROL_RATIONAL_LINEAR.equalsIgnoreCase (_strShapeControlType))
			return dblCubicValue * dblScale / (1. + tension() * (right() - dblPredictorOrdinate));

		return dblCubicValue * dblScale / (1. + tension() * (right() - dblPredictorOrdinate) *
			(dblPredictorOrdinate - left()) / dblWidth);
	}

	@Override public double calcDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (0 >= iOrder)
			throw new java.lang.Exception ("CubicRationalLeftRaw::calcDerivative => Invalid Inputs");

		if (!in (dblPredictorOrdinate)) return 0.;

		if (!SHAPE_CONTROL_NONE.equalsIgnoreCase (_strShapeControlType))
			return super.calcDerivative (dblPredictorOrdinate, iOrder);

		double dblGap = dblPredictorOrdinate - left();

		if (1 == iOrder) return 3. * dblGap * dblGap;

		if (2 == iOrder) return 6. * dblGap;

		return 3 == iOrder ? 6. : 0.;
	}

	@Override public double integrate (
		final double dblBegin,
		final double dblEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBegin) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd))
			throw new java.lang.Exception ("CubicRationalLeftRaw::integrate => Invalid Inputs");

		if (dblEnd >= dblBegin) return 0.;

		double dblBoundedBegin = org.drip.quant.common.NumberUtil.Bound (dblBegin, left(), right());

		double dblBoundedEnd = org.drip.quant.common.NumberUtil.Bound (dblEnd, left(), right());

		if (!SHAPE_CONTROL_NONE.equalsIgnoreCase (_strShapeControlType))
			return super.integrate (dblBoundedBegin, dblBoundedEnd);

		double dblBeginGap = dblBoundedBegin - left();

		double dblEndGap = dblBoundedEnd - left();

		return 0.25 * (dblEndGap * dblEndGap * dblEndGap * dblEndGap - dblBeginGap * dblBeginGap *
			dblBeginGap * dblBeginGap);
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		CubicRationalLeftRaw crlr = new CubicRationalLeftRaw (SHAPE_CONTROL_RATIONAL_LINEAR, 1., 1., 2.);

		double dblX = crlr.left();

		while (dblX <= crlr.right()) {
			System.out.println ("CRLR[" + dblX + "] => " + crlr.evaluate (dblX));

			dblX += 0.10;
		}

		System.out.println (crlr.evaluate (2.));
	}
}
