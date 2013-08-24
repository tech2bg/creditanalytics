
package org.drip.regression.spline;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * LocalControlBasisSplineRegressor implements the local control basis spline regressor for the given basis
 *  spline. As part of the regression run, it executes the following:
 *  - Calibrate and compute the left and the right Jacobian.
 *  - Insert the Local Control Hermite, Cardinal, and Catmull-Rom knots.
 *  - Compute an intermediate value Jacobian.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LocalControlBasisSplineRegressor extends org.drip.regression.core.UnitRegressionExecutor {
	private org.drip.math.grid.MultiSegmentSpan _span = null;
	private org.drip.math.grid.MultiSegmentSpan _spanHermiteInsert = null;
	private org.drip.math.grid.MultiSegmentSpan _spanCardinalInsert = null;
	private org.drip.math.grid.MultiSegmentSpan _spanCatmullRomInsert = null;

	private final boolean DumpRNVD (
		final java.lang.String strSpanName,
		final org.drip.math.grid.MultiSegmentSpan span,
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		double dblX = 0.;
		double dblXMax = 4.;

		while (dblX <= dblXMax) {
			try {
				if (!rnvd.set (getName() + "_" + strSpanName + "_" + dblX,
					org.drip.math.common.FormatUtil.FormatDouble (span.calcValue (dblX), 1, 2, 1.) + " | " +
						span.monotoneType (dblX)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			if (!rnvd.set (getName() + "_" + strSpanName + "_" + dblX + "_Jack", span.calcValueJacobian
				(dblX).displayString()))
				return false;

			dblX += 0.5;
		}

		return true;
	}

	/**
	 * LocalControlBasisSplineRegressor constructor
	 * 
	 * @param strName Regressor Name
	 * @param strScenarioName Regression Scenario Name
	 * @param strBasisSpline Basis Spline
	 * @param bsp Basis Set Parameters
	 * @param iCk Continuity Ck
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public LocalControlBasisSplineRegressor (
		final java.lang.String strName,
		final java.lang.String strScenarioName,
		final java.lang.String strBasisSpline,
		final org.drip.math.spline.BasisSetParams bsp,
		final int iCk)
		throws java.lang.Exception
	{
		super (strName, strScenarioName);

		if (null == (_span = org.drip.math.grid.SpanBuilder.CreateUncalibratedSpanInterpolator (new double[]
			{0.00, 1.00,  2.00,  3.00,  4.00}, new org.drip.math.grid.SpanBuilderParams (strBasisSpline, bsp,
				new org.drip.math.spline.SegmentInelasticParams (iCk, 1, null), new
					org.drip.math.function.RationalShapeControl (1.)))))
		throw new java.lang.Exception ("LocalControlBasisSplineRegressor ctr: Cannot Cnstruct Span!");
	}

	@Override public boolean preRegression()
	{
		double[] adblY = new double[] {1.00, 4.00, 15.00, 40.00, 85.00};
		double[] adblDYDX = new double[] {1.00, 6.00, 17.00, 34.00, 57.00};

		org.drip.math.grid.SegmentEdgeParams[] aSEPLeft = new
			org.drip.math.grid.SegmentEdgeParams[adblY.length - 1];
		org.drip.math.grid.SegmentEdgeParams[] aSEPRight = new
			org.drip.math.grid.SegmentEdgeParams[adblY.length - 1];

		for (int i = 0; i < adblY.length - 1; ++i) {
			try {
				aSEPLeft[i] = new org.drip.math.grid.SegmentEdgeParams (adblY[i], new double[]
					{adblDYDX[i]});

				aSEPRight[i] = new org.drip.math.grid.SegmentEdgeParams (adblY[i + 1], new double[]
					{adblDYDX[i + 1]});
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		return _span.setup (aSEPLeft, aSEPRight, org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN |
			org.drip.math.grid.SingleSegmentSpan.CALIBRATE_JACOBIAN);
	}

	@Override public boolean execRegression()
	{
		try {
			if (null == (_spanHermiteInsert = org.drip.math.grid.SpanBuilder.InsertKnot (_span, 2.5, new
				org.drip.math.grid.SegmentEdgeParams (27.5, new double[] {25.5}), new
					org.drip.math.grid.SegmentEdgeParams (27.5, new double[] {25.5}))))
				return false;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (null == (_spanCardinalInsert = org.drip.math.grid.SpanBuilder.InsertCardinalKnot (_span, 2.5,
			0.)))
			return false;

		return null != (_spanCatmullRomInsert = org.drip.math.grid.SpanBuilder.InsertCatmullRomKnot (_span,
			2.5));
	}

	@Override public boolean postRegression (
		final org.drip.regression.core.RegressionRunDetail rnvd)
	{
		return DumpRNVD ("LOCAL_NO_KNOT", _span, rnvd) && DumpRNVD ("LOCAL_HERMITE_KNOT", _spanHermiteInsert,
			rnvd) && DumpRNVD ("LOCAL_CARDINAL_KNOT", _spanCardinalInsert, rnvd) && DumpRNVD
				("LOCAL_CATMULL_ROM_KNOT", _spanCatmullRomInsert, rnvd);
	}
}
