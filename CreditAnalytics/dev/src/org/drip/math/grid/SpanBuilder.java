
package org.drip.math.grid;

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
 * SpanBuilder exports Span creation/calibration methods to generate customized basis splines, with
 *  customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SpanBuilder {

	/**
	 * Polynomial Spline
	 */

	public static final java.lang.String BASIS_SPLINE_POLYNOMIAL = "Polynomial";

	/**
	 * Bernstein Polynomial Spline
	 */

	public static final java.lang.String BASIS_SPLINE_BERNSTEIN_POLYNOMIAL = "BernsteinPolynomial";

	/**
	 * Hyperbolic Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_HYPERBOLIC_TENSION = "HyperbolicTension";

	/**
	 * Exponential Tension Spline
	 */

	public static final java.lang.String BASIS_SPLINE_EXPONENTIAL_TENSION = "ExponentialTension";

	/**
	 * Kaklis Pandelis Spline
	 */

	public static final java.lang.String BASIS_SPLINE_KAKLIS_PANDELIS = "KaklisPandelis";

	/**
	 * Creates an un-calibrated Span instance over the specified X input array points, using the specified
	 * 	basis splines.
	 * 
	 * @param adblX Input X array points
	 * @param segControlParams Segment Control Parameters
	 * 
	 * @return Span instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateUncalibratedSpanInterpolator (
		final double[] adblX,
		final org.drip.math.grid.SpanBuilderParams segControlParams)
	{
		if (null == adblX || 0 == adblX.length || null == segControlParams) return null;

		int iNumSegment = adblX.length - 1;
		org.drip.math.grid.Segment[] aCSS = new org.drip.math.grid.Segment[iNumSegment];

		java.lang.String strBasisSpline = segControlParams.getBasisSpline();

		if (1 >= iNumSegment || null == strBasisSpline || (!BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase
			(strBasisSpline) && !BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
				!BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase (strBasisSpline) &&
					!BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase (strBasisSpline) &&
						!BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase (strBasisSpline)))
			return null;

		for (int i = 0; i < iNumSegment; ++i) {
			if (BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.PolynomialBasisSet
						((org.drip.math.spline.PolynomialBasisSetParams)
							segControlParams.getBasisSetParams()), segControlParams.getShapeController(),
								segControlParams.getSegmentInelasticParams())))
					return null;
			} else if (BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.BernsteinPolynomialBasisSet
						((org.drip.math.spline.PolynomialBasisSetParams)
							segControlParams.getBasisSetParams()), segControlParams.getShapeController(),
								segControlParams.getSegmentInelasticParams())))
					return null;
			} else if (org.drip.math.grid.SpanBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.HyperbolicTensionBasisSet
						((org.drip.math.spline.ExponentialTensionBasisSetParams)
							segControlParams.getBasisSetParams()), segControlParams.getShapeController(),
								segControlParams.getSegmentInelasticParams())))
					return null;
			} else if (org.drip.math.grid.SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.ExponentialTensionBasisSet
						((org.drip.math.spline.ExponentialTensionBasisSetParams)
							segControlParams.getBasisSetParams()), segControlParams.getShapeController(),
								segControlParams.getSegmentInelasticParams())))
					return null;
			} else if (org.drip.math.grid.SpanBuilder.BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.KaklisPandelisBasisSet
						((org.drip.math.spline.KaklisPandelisBasisSetParams)
							segControlParams.getBasisSetParams()), segControlParams.getShapeController(),
								segControlParams.getSegmentInelasticParams())))
					return null;
			}
		}

		try {
			return new org.drip.math.grid.MultiSegmentCalibratableSpan (aCSS, segControlParams);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a calibrated Span instance over the specified X and Y input array points, using the specified
	 * 	basis splines.
	 * 
	 * @param adblX Input X array points
	 * @param adblY Input Y array points
	 * @param strCalibrationMode Calibration Mode
	 * @param segControlParams Segment Control Parameters
	 * @param iSetupMode Setup Mode
	 * 
	 * @return Span instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateCalibratedSpanInterpolator (
		final double[] adblX,
		final double[] adblY,
		final java.lang.String strCalibrationMode,
		final org.drip.math.grid.SpanBuilderParams segControlParams,
		final int iSetupMode)
	{
		org.drip.math.grid.MultiSegmentSpan csi = CreateUncalibratedSpanInterpolator (adblX, segControlParams);

		return null == csi? null : csi.setup (adblY, strCalibrationMode, iSetupMode) ? csi : null;
	}

	/**
	 * Creates a Calibrated Span instance from an array of X points and a flat Y point
	 * 
	 * @param adblX X Array
	 * @param dblY Flat Y Input
	 * @param strCalibrationMode Calibration Mode
	 * @param segControlParams Segment Control Parameters
	 * @param iSetupMode Setup Mode
	 * 
	 * @return Span Instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateCalibratedSpanInterpolator (
		final double[] adblX,
		final double dblY,
		final java.lang.String strCalibrationMode,
		final org.drip.math.grid.SpanBuilderParams segControlParams,
		final int iSetupMode)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblY)) return null;

		int iNumNode = adblX.length;

		if (0 == iNumNode) return null;

		double[] adblY = new double[iNumNode];

		for (int i = 0; i < iNumNode; ++i)
			adblY[i] = dblY;

		return CreateCalibratedSpanInterpolator (adblX, adblY, strCalibrationMode, segControlParams,
			iSetupMode);
	}

	/**
	 * Insert a Knot into the specified Span
	 * 
	 * @param span Input Span
	 * @param dblX Knot X
	 * @param dblY Knot Y
	 * 
	 * @return The Span with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentSpan InsertKnot (
		final org.drip.math.grid.MultiSegmentSpan span,
		final double dblX,
		final double dblY)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX) || !org.drip.math.common.NumberUtil.IsValid
			(dblY) || null == span || span.isKnot (dblX))
			return null;

		org.drip.math.grid.Segment[] aCSS = span.getSegments();

		int iNewIndex = 0;
		int iNumCurrentSegment = aCSS.length;
		double[] adblX = new double[iNumCurrentSegment + 2];
		double[] adblY = new double[iNumCurrentSegment + 2];

		if (dblX < aCSS[0].getLeft()) {
			adblX[iNewIndex] = dblX;
			adblY[iNewIndex++] = dblY;
		}

		for (int i = 0; i < iNumCurrentSegment; ++i) {
			adblX[iNewIndex] = aCSS[i].getLeft();

			try {
				adblY[iNewIndex++] = span.calcValue (aCSS[i].getLeft());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (dblX > aCSS[i].getLeft() && dblX < aCSS[i].getRight()) {
				adblX[iNewIndex] = dblX;
				adblY[iNewIndex++] = dblY;
			}
		}

		adblX[iNewIndex] = aCSS[iNumCurrentSegment - 1].getRight();

		try {
			adblY[iNewIndex++] = span.calcValue (aCSS[iNumCurrentSegment - 1].getRight());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (dblX > aCSS[iNumCurrentSegment - 1].getRight()) {
			adblX[iNewIndex] = dblX;
			adblY[iNewIndex++] = dblY;
		}

		return CreateCalibratedSpanInterpolator (adblX, adblY,
			org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL, span.getSpanBuilderParams(),
				org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN);
	}

	/**
	 * Insert a Knot into the specified Span
	 * 
	 * @param SpanIn Input Span
	 * @param dblX Knot X
	 * @param sepLeftSegmentRightEdge SEP for the right edge of the left segment
	 * @param sepRightSegmentLeftEdge SEP for the left edge of the right segment
	 * 
	 * @return The Span with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentSpan InsertKnot (
		final org.drip.math.grid.MultiSegmentSpan spanIn,
		final double dblX,
		final org.drip.math.grid.SegmentEdgeParams sepLeftSegmentRightEdge,
		final org.drip.math.grid.SegmentEdgeParams sepRightSegmentLeftEdge)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX) || null == spanIn || spanIn.isKnot (dblX) || null
			== sepLeftSegmentRightEdge || null == sepRightSegmentLeftEdge)
			return null;

		org.drip.math.grid.Segment[] aCSSIn = spanIn.getSegments();

		int iOutSegmentIndex = 1;
		int iNumInSegments = aCSSIn.length;
		double[] adblX = new double[iNumInSegments + 2];
		org.drip.math.grid.SegmentEdgeParams[] aSEPLeft = new
			org.drip.math.grid.SegmentEdgeParams[iNumInSegments + 1];
		org.drip.math.grid.SegmentEdgeParams[] aSEPRight = new
			org.drip.math.grid.SegmentEdgeParams[iNumInSegments + 1];

		if (dblX < aCSSIn[0].getLeft() || dblX > aCSSIn[iNumInSegments - 1].getRight()) return null;

		adblX[0] = aCSSIn[0].getLeft();

		for (int i = 0; i < iNumInSegments; ++i) {
			aSEPLeft[iOutSegmentIndex - 1] = spanIn.calcSEP (aCSSIn[i].getLeft());

			if (dblX > aCSSIn[i].getLeft() && dblX < aCSSIn[i].getRight()) {
				aSEPRight[iOutSegmentIndex - 1] = sepLeftSegmentRightEdge;
				adblX[iOutSegmentIndex++] = dblX;
				aSEPLeft[iOutSegmentIndex - 1] = sepRightSegmentLeftEdge;
			}

			aSEPRight[iOutSegmentIndex - 1] = spanIn.calcSEP (aCSSIn[i].getRight());

			adblX[iOutSegmentIndex++] = aCSSIn[i].getRight();
		}

		org.drip.math.grid.MultiSegmentSpan spanOut = CreateUncalibratedSpanInterpolator (adblX,
			spanIn.getSpanBuilderParams());

		if (null == spanOut) return null;

		return spanOut.setup (aSEPLeft, aSEPRight, org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN) ?
			spanOut : null;
	}

	/**
	 * Insert a Cardinal Knot into the specified Span at the specified location
	 * 
	 * @param SpanIn Input Span
	 * @param dblX Knot X
	 * @param dblCardinalTension Cardinal Tension Parameter
	 * 
	 * @return The Span with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentSpan InsertCardinalKnot (
		final org.drip.math.grid.MultiSegmentSpan spanIn,
		final double dblX,
		final double dblCardinalTension)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX) || !org.drip.math.common.NumberUtil.IsValid
			(dblCardinalTension) || null == spanIn || spanIn.isKnot (dblX))
			return null;

		org.drip.math.grid.Segment[] aCSSIn = spanIn.getSegments();

		int iSegmentIndex = 0;
		int iNumInSegments = aCSSIn.length;

		if (dblX < aCSSIn[0].getLeft() || dblX > aCSSIn[iNumInSegments - 1].getRight()) return null;

		for (; iSegmentIndex < iNumInSegments; ++iSegmentIndex) {
			if (dblX > aCSSIn[iSegmentIndex].getLeft() && dblX < aCSSIn[iSegmentIndex].getRight()) break;
		}

		org.drip.math.grid.SegmentEdgeParams sepCardinalInterp =
			org.drip.math.grid.SegmentEdgeParams.CardinalEdgeAggregate
				(spanIn.calcSEP (aCSSIn[iSegmentIndex].getLeft()),
					spanIn.calcSEP (aCSSIn[iSegmentIndex].getRight()), dblCardinalTension);

		return null == sepCardinalInterp ? null : InsertKnot (spanIn, dblX, sepCardinalInterp,
			sepCardinalInterp);
	}

	/**
	 * Insert a Catmull-Rom Knot into the specified Span at the specified location
	 * 
	 * @param SpanIn Input Span
	 * @param dblX Knot X
	 * 
	 * @return The Span with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentSpan InsertCatmullRomKnot (
		final org.drip.math.grid.MultiSegmentSpan spanIn,
		final double dblX)
	{
		return InsertCardinalKnot (spanIn, dblX, 0.);
	}
}
