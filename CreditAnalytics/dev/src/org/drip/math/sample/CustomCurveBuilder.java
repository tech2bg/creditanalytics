	
package org.drip.math.sample;

/*
 * Java Imports
 */

import java.util.*;

/*
 * DRIP Math Imports
 */

import org.drip.math.common.FormatUtil;
import org.drip.math.function.RationalShapeControl;
import org.drip.math.grid.*;
import org.drip.math.spline.*;

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
 * CustomCurveBuilder contains samples that demo how to build a discount curve from purely the cash flows. It
 *  provides for elaborate curve builder control, both at the segment level and at the span level. In
 *  particular, it shows the following:
 * 	- Construct a discount curve from the discount factors available purely from the cash and the euro-dollar
 *  	instruments.
 * 	- Construct a discount curve from the cash flows available from the swap instruments.
 * 
 * In addition, the sample demonstrates the following ways of controlling curve construction:
 * 	- Control over the type of segment basis spline
 * 	- Control over the polynomial basis spline order, Ck, and tension parameters
 * 	- Provision of custom shape controllers (in this case rational shape controller)
 * 	- Calculation of segment monotonicity and convexity
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomCurveBuilder {

	/**
	 * Sample API demonstrating the creation of the segment builder parameters based on exponential tension spline.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SegmentBuilderParams MakeExponentialTensionSBP (
		final double dblTension)
		throws Exception
	{
		return new SegmentBuilderParams (
			SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION, // Spline Type Exponential Basis Tension
			new ExponentialTensionBasisSetParams (dblTension), // Segment Tension Parameter Value
			new SegmentInelasticParams (2, 2, null), // Ck = 2; Roughness penalty (if necessary) order: 2
			new RationalShapeControl (0.0)); // Univariate Rational Shape Controller
	}

	/**
	 * Sample API demonstrating the creation of the segment builder parameters based on polynomial spline.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static final SegmentBuilderParams MakePolynomialSBP (
		final int iNumDegree)
		throws Exception
	{
		return new SegmentBuilderParams (
			SpanBuilder.BASIS_SPLINE_POLYNOMIAL, // Spline Type Polynomial
			new PolynomialBasisSetParams (iNumDegree + 1), // Polynomial of degree (i.e, cubic would be 3+1; 4 basis functions - 1 "intercept")
			new SegmentInelasticParams (2, 2, null), // Ck = 2; Roughness penalty (if necessary) order: 2
			new RationalShapeControl (0.0)); // Univariate Rational Shape Controller
	}

	/**
	 * Sample API demonstrating the creation of the segment builder parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SegmentBuilderParams MakeSBP (
		final String strBasisSpline)
		throws Exception
	{
		if (strBasisSpline.equalsIgnoreCase (SpanBuilder.BASIS_SPLINE_POLYNOMIAL)) // Polynomial Basis Spline
			return new SegmentBuilderParams (
				SpanBuilder.BASIS_SPLINE_POLYNOMIAL, // Spline Type Polynomial
				new PolynomialBasisSetParams (4), // Polynomial of order 3 (i.e, cubic - 4 basis functions - 1 "intercept")
				new SegmentInelasticParams (2, 2, null), // Ck = 2; Roughness penalty (if necessary) order: 2
				new RationalShapeControl (0.0)); // Univariate Rational Shape Controller

		if (strBasisSpline.equalsIgnoreCase (SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION)) // Exponential Tension Basis Spline
			return new SegmentBuilderParams (
				SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION, // Spline Type Exponential Basis Tension
				new ExponentialTensionBasisSetParams (1.), // Segment Tension Parameter Value = 1.
				new SegmentInelasticParams (2, 2, null), // Ck = 2; Roughness penalty (if necessary) order: 2
				new RationalShapeControl (0.0)); // Univariate Rational Shape Controller

		return null;
	}

	/**
	 * Generates the sample Swap Cash Flows to a given maturity, for the frequency/coupon.
	 * 	Cash Flow is in the form of <Date, Cash Amount> Map.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final TreeMap<Double, Double> SwapCashFlow (
		final double dblCoupon,
		final int iFreq,
		final double dblTenorInYears)
	{
		TreeMap<Double, Double> mapCF = new TreeMap<Double, Double>();

		for (double dblCFDate = 1. / iFreq; dblCFDate < dblTenorInYears; dblCFDate += 1. / iFreq)
			mapCF.put (dblCFDate, dblCoupon / iFreq);

		mapCF.put (0., -1.);

		mapCF.put (1. * dblTenorInYears, 1. + dblCoupon / iFreq);

		return mapCF;
	}

	/**
	 * Generates the DRIP linear constraint corresponding to an exclusive swap segment. This constraint is
	 * 	used to calibrate the discount curve in this segment.
	 *  
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SegmentNodeWeightConstraint GenerateSegmentConstraint (
		final TreeMap<Double, Double> mapCF,
		final MultiSegmentSpan spanDF)
		throws Exception
	{
		double dblValue = 0.;

		List<Double> lsTime = new ArrayList<Double>();

		List<Double> lsWeight = new ArrayList<Double>();

		for (Map.Entry<Double, Double> me : mapCF.entrySet()) {
			double dblTime = me.getKey();

			if (null != spanDF && spanDF.isInRange (dblTime))
				dblValue += spanDF.calcValue (dblTime) * me.getValue();
			else {
				lsTime.add (me.getKey());

				lsWeight.add (me.getValue());
			}
		}

		int iSize = lsTime.size();

		double[] adblNode = new double[iSize];
		double[] adblNodeWeight = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblNode[i] = lsTime.get (i);

			adblNodeWeight[i] = lsWeight.get (i);
		}

		return new SegmentNodeWeightConstraint (adblNode, adblNodeWeight, -dblValue);
	}

	/**
	 * The set of Par Swap Quotes.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final Map<Double, Double> SwapQuotes()
	{
		Map<Double, Double> mapSwapQuotes = new TreeMap<Double, Double>();

		mapSwapQuotes.put (4., 0.0166);

		mapSwapQuotes.put (5., 0.0206);

		mapSwapQuotes.put (6., 0.0241);

		mapSwapQuotes.put (7., 0.0269);

		mapSwapQuotes.put (8., 0.0292);

		mapSwapQuotes.put (9., 0.0311);

		mapSwapQuotes.put (10., 0.0326);

		mapSwapQuotes.put (11., 0.0340);

		mapSwapQuotes.put (12., 0.0351);

		mapSwapQuotes.put (15., 0.0375);

		mapSwapQuotes.put (20., 0.0393);

		mapSwapQuotes.put (25., 0.0402);

		mapSwapQuotes.put (30., 0.0407);

		mapSwapQuotes.put (40., 0.0409);

		mapSwapQuotes.put (50., 0.0409);

		return mapSwapQuotes;
	}

	/**
	 * Sample Function illustrating the construction of the discount curve off of swap cash flows and
	 *  detailed segment level controls for the swap instruments.Further, the Segment Builder Parameters
	 *  for the cash/swap bridging regime shown here illustrate using an exponential/hyperbolic spline with
	 *  very high tension (100000.) to "stitch" the cash regime with the swaps regime.
	 * 
	 * Each of the respective regimes have their own tension settings, so the "high" tension
	 *  ensures that there is no propagation of derivatives and therefore high locality.
	 *  
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void BuildSwapCurve (
		MultiSegmentSpan span)
		throws Exception
	{
		boolean bFirstNode = true;

		/*
		 * Iterate through the swap instruments and their quotes.
		 */

		for (Map.Entry<Double, Double> meSwapQuote : SwapQuotes().entrySet()) {
			double dblTenorInYears = meSwapQuote.getKey(); // Swap Maturity in Years

			double dblQuote = meSwapQuote.getValue(); // Par Swap Quote

			/*
			 * Generate the Cash flow for the swap Instrument
			 */

			TreeMap<Double, Double> mapCF = SwapCashFlow (dblQuote, 2, dblTenorInYears);

			/*
			 * Convert the Cash flow into a DRIP segment constraint using the "prior" curve span
			 */

			SegmentNodeWeightConstraint snwc = GenerateSegmentConstraint (mapCF, span);

			/*
			 * If it is the head segment, create a span instance for the discount curve.
			 */

			if (null == span) {
				/*
				 * Set the Segment Builder Parameters. This may be set on a segment-by-segment basis.
				 */

				SegmentBuilderParams sbp = MakeSBP (SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION);

				/*
				 * Start off with a single segment span, with the corresponding Builder Parameters
				 */

				span = SpanBuilder.CreateUncalibratedSpanInterpolator (
					new double[] {0., dblTenorInYears},
					new SegmentBuilderParams[] {sbp});

				/*
				 * Set the span up by carrying out a "Natural Boundary" Spline Calibration
				 */

				span.setup (1.,
					new SegmentNodeWeightConstraint[] {snwc},
					MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL,
					SingleSegmentSpan.CALIBRATE_SPAN);
			} else {
				/*
				 * The Segment Builder Parameters shown here illustrate using an exponential/hyperbolic
				 *  spline with very high tension (100000.) to "stitch" the cash regime with the swaps
				 *  regime.
				 *  
				 * Each of the respective regimes have their own tension settings, so the "high" tension
				 *  ensures that there is no propagation of derivatives and therefore high locality.
				 */

				SegmentBuilderParams sbpLocal = null;

				if (bFirstNode) {
					bFirstNode = false;

					sbpLocal = MakeExponentialTensionSBP (100000.);
				} else
					sbpLocal = MakeExponentialTensionSBP (1.);

				/*
				 * If not the head segment, just append the exclusive swap instrument segment to the tail of
				 * 	the current span state, using the constraint generated from the swap cash flow.
				 */

				span = org.drip.math.grid.SpanBuilder.AppendSegment (span, dblTenorInYears, snwc, sbpLocal);
			}
		}

		/*
		 * Display the DF, the monotonicity, and the convexity.
		 */

		for (double dblX = 0.; dblX <= 50.; dblX += 1.)
			System.out.println ("Discount Factor[" +
				FormatUtil.FormatDouble (dblX, 2, 0, 1.) + "Y] = " +
				FormatUtil.FormatDouble (span.calcValue (dblX), 1, 6, 1.) + " | " +
				span.monotoneType (dblX));
	}

	/**
	 * The set of Cash Discount Factors.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final Map<Double, Double> CashDFQuotes()
	{
		Map<Double, Double> mapDFCashQuotes = new TreeMap<Double, Double>();

		mapDFCashQuotes.put (0.005556, 0.999991);

		mapDFCashQuotes.put (0.019444, 0.999967);

		mapDFCashQuotes.put (0.038889, 0.999931);

		mapDFCashQuotes.put (0.083333, 0.999836);

		mapDFCashQuotes.put (0.166667, 0.999622);

		mapDFCashQuotes.put (0.250000, 0.999360);

		mapDFCashQuotes.put (0.500000, 0.998686);

		mapDFCashQuotes.put (0.750000, 0.997888);

		mapDFCashQuotes.put (1.000000, 0.996866);

		mapDFCashQuotes.put (1.250000, 0.995522);

		mapDFCashQuotes.put (1.500000, 0.993609);

		mapDFCashQuotes.put (1.750000, 0.991033);

		mapDFCashQuotes.put (2.000000, 0.987724);

		mapDFCashQuotes.put (2.250000, 0.983789);

		return mapDFCashQuotes;
	}

	/**
	 * Sample Function illustrating the construction of the discount curve off of discount factors and
	 *  detailed segment level controls for the cash instruments.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final MultiSegmentSpan BuildCashCurve()
		throws Exception
	{
		/*
		 * For the head segment, create a calibrated span instance for the discount curve.
		 */

		MultiSegmentSpan span = SpanBuilder.CreateCalibratedSpanInterpolator (
			new double[] {0., 0.002778}, // t0 and t1 for the segment
			new double[] {1., 0.999996}, // the corresponding discount factors
			MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL, // "Natural" Spline Boundary Condition
			new SegmentBuilderParams[] {MakeSBP (SpanBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION)}, // Exponential Tension Basis Spline
			SingleSegmentSpan.CALIBRATE_SPAN); // Calibrate the full Span

		/*
		 * Construct the discount curve by iterating through the cash instruments and their discount
		 * 	factors, and inserting them as "knots" onto the existing span.
		 */

		for (Map.Entry<Double, Double> meCashDFQuote : CashDFQuotes().entrySet()) {
			double dblTenorInYears = meCashDFQuote.getKey(); // Instrument Tenor in Years

			double dblDF = meCashDFQuote.getValue(); // Discount Factor

			System.out.println ("Adding Cash Node [" +
				FormatUtil.FormatDouble (dblTenorInYears, 1, 6, 1.) + ", " +
				FormatUtil.FormatDouble (dblDF, 1, 6, 1.) + "]");

			/*
			 * Insert the instrument/quote as a "knot" entity into the span. Given the "natural" spline
			 */

			span = SpanBuilder.InsertKnot (span, dblTenorInYears, dblDF);
		}

		double dblX = 0.;
		double dblXShift = 0.1 * 2.25;

		/*
		 * Display the DF, the monotonicity, and the convexity.
		 */

		while (dblX <= 2.25) {
			System.out.println ("\tDF[" +
				FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] => " +
				FormatUtil.FormatDouble (span.calcValue (dblX), 1, 6, 1.) + " | " +
				span.monotoneType (dblX));

			dblX += dblXShift;
		}

		return span;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		MultiSegmentSpan spanCash = BuildCashCurve();

		BuildSwapCurve (spanCash);
	}
}
