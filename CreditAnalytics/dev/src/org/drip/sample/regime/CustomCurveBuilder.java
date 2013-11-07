	
package org.drip.sample.regime;

/*
 * Java Imports
 */

import java.util.*;

/*
 * DRIP Math Imports
 */

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.spline.basis.*;
import org.drip.spline.params.*;
import org.drip.spline.regime.*;

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
 *  provides for elaborate curve builder control, both at the segment level and at the Regime level. In
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
	 * Sample API demonstrating the creation of the segment builder parameters based on Koch-Lyche-Kvasov tension spline.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SegmentCustomBuilderControl MakeKLKTensionSCBC (
		final double dblTension)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION, // Spline Type KLK Hyperbolic Basis Tension
			new ExponentialTensionSetParams (dblTension), // Segment Tension Parameter Value
			SegmentDesignInelasticControl.Create (2, 2), // Ck = 2; Curvature penalty (if necessary) order: 2
			new ResponseScalingShapeControl (
				true,
				new QuadraticRationalShapeControl (0.0))); // Univariate Rational Shape Controller
	}

	/**
	 * Sample API demonstrating the creation of the segment builder parameters based on polynomial spline.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	public static final SegmentCustomBuilderControl MakePolynomialSBP (
		final int iNumDegree)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, // Spline Type Polynomial
			new PolynomialFunctionSetParams (iNumDegree + 1), // Polynomial of degree (i.e, cubic would be 3+1; 4 basis functions - 1 "intercept")
			SegmentDesignInelasticControl.Create (2, 2), // Ck = 2; Curvature penalty (if necessary) order: 2
			new ResponseScalingShapeControl (
				true,
				new QuadraticRationalShapeControl (0.0))); // Univariate Rational Shape Controller
	}

	/**
	 * Sample API demonstrating the creation of the segment builder parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SegmentCustomBuilderControl MakeSCBC (
		final String strBasisSpline)
		throws Exception
	{
		if (strBasisSpline.equalsIgnoreCase (MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL)) // Polynomial Basis Spline
			return new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, // Spline Type Polynomial
				new PolynomialFunctionSetParams (4), // Polynomial of order 3 (i.e, cubic - 4 basis functions - 1 "intercept")
				SegmentDesignInelasticControl.Create (2, 2), // Ck = 2; Curvature penalty (if necessary) order: 2
				new ResponseScalingShapeControl (
					true,
					new QuadraticRationalShapeControl (0.0))); // Univariate Rational Shape Controller

		if (strBasisSpline.equalsIgnoreCase (MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION)) // Exponential Tension Basis Spline
			return new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION, // Spline Type Exponential Basis Tension
				new ExponentialTensionSetParams (1.), // Segment Tension Parameter Value = 1.
				SegmentDesignInelasticControl.Create (2, 2), // Ck = 2; Curvature penalty (if necessary) order: 2
				new ResponseScalingShapeControl (
					true,
					new QuadraticRationalShapeControl (0.0))); // Univariate Rational Shape Controller

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

	private static final SegmentResponseValueConstraint GenerateSegmentConstraint (
		final TreeMap<Double, Double> mapCF,
		final MultiSegmentSequence regimeDF)
		throws Exception
	{
		double dblValue = 0.;

		List<Double> lsTime = new ArrayList<Double>();

		List<Double> lsWeight = new ArrayList<Double>();

		for (Map.Entry<Double, Double> me : mapCF.entrySet()) {
			double dblTime = me.getKey();

			if (null != regimeDF && regimeDF.in (dblTime))
				dblValue += regimeDF.responseValue (dblTime) * me.getValue();
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

		return new SegmentResponseValueConstraint (adblNode, adblNodeWeight, -dblValue);
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

	private static final MultiSegmentSequence BuildSwapCurve (
		MultiSegmentSequence regime,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
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
			 * Convert the Cash flow into a DRIP segment constraint using the "prior" curve regime
			 */

			SegmentResponseValueConstraint srvc = GenerateSegmentConstraint (mapCF, regime);

			/*
			 * If it is the head segment, create a regime instance for the discount curve.
			 */

			if (null == regime) {
				/*
				 * Set the Segment Builder Parameters. This may be set on a segment-by-segment basis.
				 */

				SegmentCustomBuilderControl scbc = MakeSCBC (MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION);

				/*
				 * Start off with a single segment regime, with the corresponding Builder Parameters
				 */

				regime = MultiSegmentSequenceBuilder.CreateUncalibratedRegimeEstimator ("SWAP",
					new double[] {0., dblTenorInYears},
					new SegmentCustomBuilderControl[] {scbc});

				/*
				 * Set the regime up by carrying out a "Natural Boundary" Spline Calibration
				 */

				regime.setup (1.,
					new SegmentResponseValueConstraint[] {srvc},
					null,
					iCalibrationBoundaryCondition,
					iCalibrationDetail);
			} else {
				/*
				 * The Segment Builder Parameters shown here illustrate using an exponential/hyperbolic
				 *  spline with high tension (15.) to "stitch" the cash regime with the swaps regime.
				 *  
				 * Each of the respective regimes have their own tension settings, so the "high" tension
				 *  ensures that there is no propagation of derivatives and therefore high locality.
				 */

				SegmentCustomBuilderControl scbcLocal = null;

				if (bFirstNode) {
					bFirstNode = false;

					scbcLocal = MakeKLKTensionSCBC (1.);
				} else
					scbcLocal = MakeKLKTensionSCBC (1.);

				/*
				 * If not the head segment, just append the exclusive swap instrument segment to the tail of
				 * 	the current regime state, using the constraint generated from the swap cash flow.
				 */

				regime = org.drip.spline.regime.MultiSegmentSequenceModifier.AppendSegment (
					regime,
					dblTenorInYears,
					srvc,
					scbcLocal,
					iCalibrationBoundaryCondition,
					iCalibrationDetail);
			}
		}

		return regime;
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

	private static final MultiSegmentSequence BuildCashCurve (
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
		throws Exception
	{
		/*
		 * For the head segment, create a calibrated regime instance for the discount curve.
		 */

		MultiSegmentSequence regimeCash = MultiSegmentSequenceBuilder.CreateCalibratedRegimeEstimator (
			"CASH",
			new double[] {0., 0.002778}, // t0 and t1 for the segment
			new double[] {1., 0.999996}, // the corresponding discount factors
			new SegmentCustomBuilderControl[] {
				// MakeSCBC (MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION)
				MakeKLKTensionSCBC (1.)
			}, // Exponential Tension Basis Spline
			null,
			iCalibrationBoundaryCondition, iCalibrationDetail); // "Natural" Spline Boundary Condition + Calibrate the full regime

		/*
		 * Construct the discount curve by iterating through the cash instruments and their discount
		 * 	factors, and inserting them as "knots" onto the existing regime.
		 */

		for (Map.Entry<Double, Double> meCashDFQuote : CashDFQuotes().entrySet()) {
			double dblTenorInYears = meCashDFQuote.getKey(); // Instrument Tenor in Years

			double dblDF = meCashDFQuote.getValue(); // Discount Factor

			/*
			 * Insert the instrument/quote as a "knot" entity into the regime. Given the "natural" spline
			 */

			regimeCash = MultiSegmentSequenceModifier.InsertKnot (regimeCash, dblTenorInYears, dblDF, iCalibrationBoundaryCondition, iCalibrationDetail);
		}

		return regimeCash;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		MultiSegmentSequence regimeNaturalCash = BuildCashCurve (
			MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentSequence.CALIBRATE);

		MultiSegmentSequence regimeFinancialCash = BuildCashCurve (
			MultiSegmentSequence.BOUNDARY_CONDITION_FINANCIAL,
			MultiSegmentSequence.CALIBRATE);

		double dblXShift = 0.1 * (regimeNaturalCash.getRightPredictorOrdinateEdge() - regimeNaturalCash.getLeftPredictorOrdinateEdge());

		System.out.println ("\n\t\t\t----------------     <====>  ------------------");

		System.out.println ("\t\t\tNATURAL BOUNDARY     <====>  FINANCIAL BOUNDARY");

		System.out.println ("\t\t\t----------------     <====>  ------------------\n");

		/*
		 * Display the DF, the monotonicity, and the convexity for the cash instruments.
		 */

		for (double dblX = regimeNaturalCash.getLeftPredictorOrdinateEdge(); dblX <= regimeNaturalCash.getRightPredictorOrdinateEdge(); dblX = dblX + dblXShift)
			System.out.println ("Cash DF[" +
				FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "Y] => " +
				FormatUtil.FormatDouble (regimeNaturalCash.responseValue (dblX), 1, 6, 1.) + " | " +
				regimeNaturalCash.monotoneType (dblX) + "  <====>  " +
				FormatUtil.FormatDouble (regimeFinancialCash.responseValue (dblX), 1, 6, 1.) + " | " +
				regimeNaturalCash.monotoneType (dblX));

		System.out.println ("\n");

		MultiSegmentSequence regimeNaturalSwap = BuildSwapCurve (
			regimeNaturalCash,
			MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL,
			MultiSegmentSequence.CALIBRATE);

		MultiSegmentSequence regimeFinancialSwap = BuildSwapCurve (
			regimeNaturalCash,
			MultiSegmentSequence.BOUNDARY_CONDITION_FINANCIAL,
			MultiSegmentSequence.CALIBRATE);

		/*
		 * Display the DF, the monotonicity, and the convexity for the swaps.
		 */

		dblXShift = 0.05 * (regimeNaturalSwap.getRightPredictorOrdinateEdge() - regimeNaturalSwap.getLeftPredictorOrdinateEdge());

		for (double dblX = regimeNaturalSwap.getLeftPredictorOrdinateEdge(); dblX <= regimeNaturalSwap.getRightPredictorOrdinateEdge(); dblX = dblX + dblXShift)
			System.out.println ("Swap DF   [" +
				FormatUtil.FormatDouble (dblX, 2, 0, 1.) + "Y] => " +
				FormatUtil.FormatDouble (regimeNaturalSwap.responseValue (dblX), 1, 6, 1.) + " | " +
				regimeNaturalSwap.monotoneType (dblX) + "  <====>  " +
				FormatUtil.FormatDouble (regimeFinancialSwap.responseValue (dblX), 1, 6, 1.) + " | " +
				regimeFinancialSwap.monotoneType (dblX));
	}
}
