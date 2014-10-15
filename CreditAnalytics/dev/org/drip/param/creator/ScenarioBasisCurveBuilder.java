
package org.drip.param.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ScenarioBasisCurveBuilder implements the construction of the scenario basis curve using the input
 * 	instruments and their quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ScenarioBasisCurveBuilder {

	/**
	 * Create an Instance of the Custom Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * @param scbc The Segment Custom Builder Control
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve CustomSplineBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis,
		final org.drip.spline.params.SegmentCustomBuilderControl scbc)
	{
		if (null == strName || strName.isEmpty() || null == astrTenor || null == dtStart) return null;

		int iNumTenor = astrTenor.length;

		if (0 == iNumTenor) return null;

		double[] adblBasisResponseValue = new double[iNumTenor + 1];
		double[] adblBasisPredictorOrdinate = new double[iNumTenor + 1];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumTenor];

		for (int i = 0; i <= iNumTenor; ++i) {
			if (0 != i) {
				java.lang.String strTenor = astrTenor[i - 1];

				if (null == strTenor || strTenor.isEmpty()) return null;

				org.drip.analytics.date.JulianDate dtMaturity = dtStart.addTenor (strTenor);

				if (null == dtMaturity) return null;

				adblBasisPredictorOrdinate[i] = dtMaturity.julian();
			} else
				adblBasisPredictorOrdinate[i] = dtStart.julian();

			adblBasisResponseValue[i] = 0 == i ? adblBasis[0] : adblBasis[i - 1];

			if (0 != i) aSCBC[i - 1] = scbc;
		}

		try {
			return new org.drip.state.curve.BasisSplineBasisCurve (friReference, friDerived,
				bBasisOnReference, new org.drip.spline.grid.OverlappingStretchSpan
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator
						(strName, adblBasisPredictorOrdinate, adblBasisResponseValue, aSCBC, null,
							org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
								org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE)), collatParams);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the Cubic Polynomial Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve CubicPolynomialBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis)
	{
		try {
			return CustomSplineBasisCurve (strName, dtStart, friReference, friDerived, bBasisOnReference,
				collatParams, astrTenor, adblBasis, new org.drip.spline.params.SegmentCustomBuilderControl
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
						org.drip.spline.basis.PolynomialFunctionSetParams (4),
							org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the Quartic Polynomial Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve QuarticPolynomialBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis)
	{
		try {
			return CustomSplineBasisCurve (strName, dtStart, friReference, friDerived, bBasisOnReference,
				collatParams, astrTenor, adblBasis, new org.drip.spline.params.SegmentCustomBuilderControl
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
						org.drip.spline.basis.PolynomialFunctionSetParams (5),
							org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the Kaklis-Pandelis Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve KaklisPandelisBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis)
	{
		try {
			return CustomSplineBasisCurve (strName, dtStart, friReference, friDerived, bBasisOnReference,
				collatParams, astrTenor, adblBasis, new org.drip.spline.params.SegmentCustomBuilderControl
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KAKLIS_PANDELIS,
						new org.drip.spline.basis.KaklisPandelisSetParams (2),
							org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the KLK Hyperbolic Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * @param dblTension The Tension Parameter
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve KLKHyperbolicBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis,
		final double dblTension)
	{
		try {
			return CustomSplineBasisCurve (strName, dtStart, friReference, friDerived, bBasisOnReference,
				collatParams, astrTenor, adblBasis, new org.drip.spline.params.SegmentCustomBuilderControl
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
						new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
							org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the KLK Rational Linear Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * @param dblTension The Tension Parameter
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve KLKRationalLinearBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis,
		final double dblTension)
	{
		try {
			return CustomSplineBasisCurve (strName, dtStart, friReference, friDerived, bBasisOnReference,
				collatParams, astrTenor, adblBasis, new org.drip.spline.params.SegmentCustomBuilderControl
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION,
				new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
					org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of the KLK Rational Quadratic Splined Basis Curve
	 * 
	 * @param strName Curve Name
	 * @param dtStart The Tenor Start Date
	 * @param friReference Reference Leg FRI
	 * @param friDerived Derived Leg FRI
	 * @param bBasisOnReference TRUE => The Basis Quote is on the Reference Leg
	 * @param collatParams The Collateralization Parameters
	 * @param astrTenor Array of the Tenors
	 * @param adblBasis Array of the Basis Spreads
	 * @param dblTension The Tension Parameter
	 * 
	 * @return The Instance of the Basis Curve
	 */

	public static final org.drip.analytics.rates.BasisCurve KLKRationalQuadraticBasisCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final org.drip.state.identifier.ForwardLabel friReference,
		final org.drip.state.identifier.ForwardLabel friDerived,
		final boolean bBasisOnReference,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final java.lang.String[] astrTenor,
		final double[] adblBasis,
		final double dblTension)
	{
		try {
			return CustomSplineBasisCurve (strName, dtStart, friReference, friDerived, bBasisOnReference,
				collatParams, astrTenor, adblBasis, new org.drip.spline.params.SegmentCustomBuilderControl
					(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION,
				new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
					org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
