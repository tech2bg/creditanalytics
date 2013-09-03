
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
	 * Creates an uncalibrated Span instance over the specified X input array points, using the specified
	 * 	basis splines.
	 * 
	 * @param adblX Input X array points
	 * @param aSBP Array of Segment Builder Parameters
	 * 
	 * @return Span instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateUncalibratedSpanInterpolator (
		final double[] adblX,
		final org.drip.math.grid.SegmentBuilderParams[] aSBP)
	{
		if (null == adblX || 0 == adblX.length || null == aSBP) return null;

		int iNumSegment = adblX.length - 1;
		org.drip.math.grid.Segment[] aCSS = new org.drip.math.grid.Segment[iNumSegment];

		if (1 > iNumSegment || iNumSegment != aSBP.length) return null;

		for (int i = 0; i < iNumSegment; ++i) {
			if (null == aSBP[i]) return null;

			java.lang.String strBasisSpline = aSBP[i].getBasisSpline();

			if (null == strBasisSpline || (!BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
				!BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline) &&
					!BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase (strBasisSpline) &&
						!BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase (strBasisSpline) &&
							!BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase (strBasisSpline)))
				return null;

			if (BASIS_SPLINE_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.PolynomialBasisSet
						((org.drip.math.spline.PolynomialBasisSetParams) aSBP[i].getBasisSetParams()),
							aSBP[i].getShapeController(), aSBP[i].getSegmentInelasticParams())))
					return null;
			} else if (BASIS_SPLINE_BERNSTEIN_POLYNOMIAL.equalsIgnoreCase (strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.BernsteinPolynomialBasisSet
						((org.drip.math.spline.PolynomialBasisSetParams) aSBP[i].getBasisSetParams()),
							aSBP[i].getShapeController(), aSBP[i].getSegmentInelasticParams())))
					return null;
			} else if (org.drip.math.grid.SpanBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.HyperbolicTensionBasisSet
						((org.drip.math.spline.ExponentialTensionBasisSetParams)
							aSBP[i].getBasisSetParams()), aSBP[i].getShapeController(),
								aSBP[i].getSegmentInelasticParams())))
					return null;
			} else if (org.drip.math.grid.SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.ExponentialTensionBasisSet
						((org.drip.math.spline.ExponentialTensionBasisSetParams)
							aSBP[i].getBasisSetParams()), aSBP[i].getShapeController(),
								aSBP[i].getSegmentInelasticParams())))
					return null;
			} else if (org.drip.math.grid.SpanBuilder.BASIS_SPLINE_KAKLIS_PANDELIS.equalsIgnoreCase
				(strBasisSpline)) {
				if (null == (aCSS[i] = org.drip.math.spline.SegmentBasisSetBuilder.CreateCk (adblX[i],
					adblX[i + 1], org.drip.math.spline.SegmentBasisSetBuilder.KaklisPandelisBasisSet
						((org.drip.math.spline.KaklisPandelisBasisSetParams) aSBP[i].getBasisSetParams()),
							aSBP[i].getShapeController(), aSBP[i].getSegmentInelasticParams())))
					return null;
			}
		}

		try {
			return new org.drip.math.grid.MultiSegmentCalibratableSpan (aCSS, aSBP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Uncalibrated Span from the following set of parameters:
	 * 
	 * @param adblX Array of X variates
	 * @param astrBasisSpline Array of the Segment Basis Splines
	 * @param aBSP Array of Basis Set Parameters
	 * @param aSegParams Array of the Segment Parameters
	 * @param aAUShapeControl Array of the Shape Controllers
	 * 
	 * @return The uncalibrated Span
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateUncalibratedSpanInterpolator (
		final double[] adblX,
		final java.lang.String[] astrBasisSpline,
		final org.drip.math.spline.BasisSetParams[] aBSP,
		final org.drip.math.spline.SegmentInelasticParams[] aSegParams,
		final org.drip.math.function.AbstractUnivariate[] aAUShapeControl)
	{
		if (null == adblX || null == astrBasisSpline || null == aBSP || null == aAUShapeControl || null ==
			aSegParams)
			return null;

		int iNumNode = adblX.length;
		org.drip.math.grid.Segment[] aCSS = new org.drip.math.grid.Segment[iNumNode - 1];
		org.drip.math.grid.SegmentBuilderParams[] aSBP = new
			org.drip.math.grid.SegmentBuilderParams[iNumNode - 1];

		if (2 > iNumNode || null == adblX || null == astrBasisSpline || astrBasisSpline.length != iNumNode ||
			aBSP.length != iNumNode || aAUShapeControl.length != iNumNode || aSegParams.length != iNumNode)
			return null;

		try {
			for (int i = 0; i < iNumNode - 1; ++i) {
				aSBP[i] = new org.drip.math.grid.SegmentBuilderParams (astrBasisSpline[i], aBSP[i],
					aSegParams[i], aAUShapeControl[i]);
			}

			return new org.drip.math.grid.MultiSegmentCalibratableSpan (aCSS, aSBP);
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
	 * @param aSBP Array of Segment Builder Parameters
	 * @param iSetupMode Setup Mode
	 * 
	 * @return Span instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateCalibratedSpanInterpolator (
		final double[] adblX,
		final double[] adblY,
		final java.lang.String strCalibrationMode,
		final org.drip.math.grid.SegmentBuilderParams[] aSBP,
		final int iSetupMode)
	{
		org.drip.math.grid.MultiSegmentSpan csi = CreateUncalibratedSpanInterpolator (adblX, aSBP);

		int iNumRightNode = adblY.length - 1;
		double[] adblYRight = new double[iNumRightNode];

		if (null == csi || null == adblY || 0 == iNumRightNode) return null;

		for (int i = 0; i < iNumRightNode; ++i)
			adblYRight[i] = adblY[i + 1];

		return csi.setup (adblY[0], adblYRight, strCalibrationMode, iSetupMode) ? csi : null;
	}

	/**
	 * Creates a calibrated Span instance over the specified X points and constraints, using the specified
	 * 	basis splines.
	 * 
	 * @param adblX Input X array points
	 * @param dblYLeading Left-most Y Point
	 * @param aSNWC Array of Segment Constraints - One per Segment
	 * @param strCalibrationMode Calibration Mode
	 * @param aSBP Array of Segment Builder Parameters
	 * @param iSetupMode Setup Mode
	 * 
	 * @return Span instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateCalibratedSpanInterpolator (
		final double[] adblX,
		final double dblYLeading,
		final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC,
		final java.lang.String strCalibrationMode,
		final org.drip.math.grid.SegmentBuilderParams[] aSBP,
		final int iSetupMode)
	{
		org.drip.math.grid.MultiSegmentSpan csi = CreateUncalibratedSpanInterpolator (adblX, aSBP);

		return csi.setup (dblYLeading, aSNWC, strCalibrationMode, iSetupMode) ? csi : null;
	}

	/**
	 * Creates a Calibrated Span instance from an array of X points and a flat Y point
	 * 
	 * @param adblX X Array
	 * @param dblY Flat Y Input
	 * @param strCalibrationMode Calibration Mode
	 * @param sbp Segment Builder Parameters
	 * @param iSetupMode Setup Mode
	 * 
	 * @return Span Instance
	 */

	public static final org.drip.math.grid.MultiSegmentSpan CreateCalibratedSpanInterpolator (
		final double[] adblX,
		final double dblY,
		final java.lang.String strCalibrationMode,
		final org.drip.math.grid.SegmentBuilderParams sbp,
		final int iSetupMode)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblY) || null == sbp) return null;

		int iNumNode = adblX.length;

		if (1 >= iNumNode) return null;

		double[] adblY = new double[iNumNode];
		org.drip.math.grid.SegmentBuilderParams[] aSBP = new
			org.drip.math.grid.SegmentBuilderParams[iNumNode - 1];

		for (int i = 0; i < iNumNode; ++i) {
			adblY[i] = dblY;

			if (0 != i) aSBP[i - 1] = sbp;
		}

		return CreateCalibratedSpanInterpolator (adblX, adblY, strCalibrationMode, aSBP, iSetupMode);
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
		org.drip.math.grid.SegmentBuilderParams[] aSBPOut = new
			org.drip.math.grid.SegmentBuilderParams[iNumCurrentSegment + 1];

		org.drip.math.grid.SegmentBuilderParams[] aSBPIn = span.getSegmentBuilderParams();

		if (dblX < aCSS[0].getLeft()) {
			adblX[iNewIndex] = dblX;
			adblY[iNewIndex] = dblY;
			aSBPOut[iNewIndex++] = aSBPIn[0];
		}

		for (int i = 0; i < iNumCurrentSegment; ++i) {
			aSBPOut[iNewIndex] = aSBPIn[i];

			adblX[iNewIndex] = aCSS[i].getLeft();

			try {
				adblY[iNewIndex++] = span.calcValue (aCSS[i].getLeft());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (dblX > aCSS[i].getLeft() && dblX < aCSS[i].getRight()) {
				adblX[iNewIndex] = dblX;
				adblY[iNewIndex] = dblY;
				aSBPOut[iNewIndex++] = aSBPIn[i];
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
			adblY[iNewIndex] = dblY;
			aSBPOut[aSBPOut.length - 1] = aSBPIn[aSBPIn.length - 1];
		}

		return CreateCalibratedSpanInterpolator (adblX, adblY,
			org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL, aSBPOut,
				org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN);
	}

	/**
	 * Append a Segment to the Right of the Specified Span using the Supplied Constraint
	 * 
	 * @param spanIn Input Span
	 * @param dblXAppendRight The Right Edge of the Segment to be appended
	 * @param snwc The Segment Constraint
	 * @param sbp Segment Builder Parameters
	 * 
	 * @return The Span with the Segment Appended
	 */

	public static final org.drip.math.grid.MultiSegmentSpan AppendSegment (
		final org.drip.math.grid.MultiSegmentSpan spanIn,
		final double dblXAppendRight,
		final org.drip.math.spline.SegmentNodeWeightConstraint snwc,
		final org.drip.math.grid.SegmentBuilderParams sbp)
	{
		if (null == spanIn || null == snwc || null == sbp || !org.drip.math.common.NumberUtil.IsValid
			(dblXAppendRight))
			return null;

		double dblSpanXRight = spanIn.getRightEdge();

		double[] adblConstraintOrdinate = snwc.ordinates();

		for (int i = 0; i < adblConstraintOrdinate.length; ++i) {
			if (adblConstraintOrdinate[i] <= dblSpanXRight) return null;
		}

		org.drip.math.grid.Segment[] aSegment = spanIn.getSegments();

		org.drip.math.grid.SegmentBuilderParams[] aSBPIn = spanIn.getSegmentBuilderParams();

		int iNumSegmentsIn = aSegment.length;
		double dblSpanYLeft = java.lang.Double.NaN;
		double[] adblXOut = new double[iNumSegmentsIn + 2];
		org.drip.math.grid.SegmentBuilderParams[] aSBPOut = new
			org.drip.math.grid.SegmentBuilderParams[iNumSegmentsIn + 1];
		org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWCOut = new
			org.drip.math.spline.SegmentNodeWeightConstraint[iNumSegmentsIn + 1];

		try {
			dblSpanYLeft = spanIn.calcValue (spanIn.getLeftEdge());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumSegmentsIn; ++i) {
			adblXOut[i] = aSegment[i].getLeft();

			double dblXRight = aSegment[i].getRight();

			aSBPOut[i] = aSBPIn[i];

			try {
				aSNWCOut[i] = new org.drip.math.spline.SegmentNodeWeightConstraint (new double[]
					{dblXRight}, new double[] {1.}, spanIn.calcValue (dblXRight));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		aSBPOut[iNumSegmentsIn] = sbp;
		aSNWCOut[iNumSegmentsIn] = snwc;
		adblXOut[iNumSegmentsIn + 1] = dblXAppendRight;

		adblXOut[iNumSegmentsIn] = aSegment[iNumSegmentsIn - 1].getRight();

		return CreateCalibratedSpanInterpolator (adblXOut, dblSpanYLeft, aSNWCOut,
			org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL, aSBPOut,
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
		org.drip.math.grid.SegmentBuilderParams[] aSBPOut = new
			org.drip.math.grid.SegmentBuilderParams[iNumInSegments + 1];

		if (dblX < aCSSIn[0].getLeft() || dblX > aCSSIn[iNumInSegments - 1].getRight()) return null;

		adblX[0] = aCSSIn[0].getLeft();

		org.drip.math.grid.SegmentBuilderParams[] aSBPIn = spanIn.getSegmentBuilderParams();

		for (int i = 0; i < iNumInSegments; ++i) {
			aSEPLeft[iOutSegmentIndex - 1] = spanIn.calcSEP (aCSSIn[i].getLeft());

			aSBPOut[iOutSegmentIndex - 1] = aSBPIn[i];

			if (dblX > aCSSIn[i].getLeft() && dblX < aCSSIn[i].getRight()) {
				aSEPRight[iOutSegmentIndex - 1] = sepLeftSegmentRightEdge;
				adblX[iOutSegmentIndex++] = dblX;
				aSBPOut[iOutSegmentIndex - 1] = aSBPIn[i];
				aSEPLeft[iOutSegmentIndex - 1] = sepRightSegmentLeftEdge;
			}

			aSEPRight[iOutSegmentIndex - 1] = spanIn.calcSEP (aCSSIn[i].getRight());

			adblX[iOutSegmentIndex++] = aCSSIn[i].getRight();
		}

		org.drip.math.grid.MultiSegmentSpan spanOut = CreateUncalibratedSpanInterpolator (adblX, aSBPOut);

		if (null == spanOut) return null;

		return spanOut.setup (aSEPLeft, aSEPRight, null, org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN)
			? spanOut : null;
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
