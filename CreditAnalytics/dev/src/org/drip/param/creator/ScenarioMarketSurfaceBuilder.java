
package org.drip.param.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ScenarioMarketSurfaceBuilder implements the construction of the scenario market Node surface using the
 * 	input option instruments, their quotes, and a wide variety of custom builds
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ScenarioMarketSurfaceBuilder {

	/**
	 * Build an Instance of the Market Node Surface using custom wire span and surface splines
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblMaturity Array of Maturities
	 * @param aadblNode Double Array of the Surface Nodes
	 * @param scbcWireSpan The Wire Span Segment Customizer
	 * @param scbcSurface The Surface Segment Customizer
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface CustomSplineWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final double[] adblMaturity,
		final double[][] aadblNode,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcSurface)
	{
		if (null == dtStart || null == strName || strName.isEmpty() || null == strCurrency ||
			strCurrency.isEmpty() || null == adblStrike || null == adblMaturity || null == aadblNode || null
				== scbcWireSpan || null == scbcSurface)
			return null;

		int iNumStrike = adblStrike.length;
		int iNumOuterNode = aadblNode.length;
		int iNumMaturity = adblMaturity.length;

		if (0 == iNumStrike || 0 == iNumMaturity || iNumStrike != iNumOuterNode) return null;

		for (int i = 0; i < iNumStrike; ++i) {
			double[] adblInner = aadblNode[i];

			if (null == adblInner || iNumMaturity != adblInner.length) return null;
		}

		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCWireSpan = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumStrike - 1];

		for (int i = 0; i < iNumStrike - 1; ++i)
			aSCBCWireSpan[i] = scbcWireSpan;

		java.util.TreeMap<java.lang.Double, org.drip.spline.grid.Span> mapWireSpan = new
			java.util.TreeMap<java.lang.Double, org.drip.spline.grid.Span>();

		for (int i = 0; i < iNumStrike; ++i) {
			org.drip.spline.stretch.MultiSegmentSequence mssWire =
				org.drip.spline.stretch.MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator
					("Stretch@" + strName + "@" + org.drip.quant.common.StringUtil.GUID(), adblMaturity,
						aadblNode[i], aSCBCWireSpan, null,
							org.drip.spline.stretch.BoundarySettings.NaturalStandard(),
								org.drip.spline.stretch.MultiSegmentSequence.CALIBRATE);

			if (null == mssWire) return null;

			try {
				mapWireSpan.put (adblStrike[i], new org.drip.spline.grid.OverlappingStretchSpan (mssWire));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.state.curve.BasisSplineMarketSurface (dtStart.julian(),
				org.drip.state.identifier.CustomMetricLabel.Standard (strName), strCurrency, new
					org.drip.spline.multidimensional.WireSurfaceStretch ("WireSurfaceStretch@" + strName +
						"@" + org.drip.quant.common.StringUtil.GUID(), scbcSurface, mapWireSpan),
							collatParams);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a Scenario Market Surface off of cubic polynomial wire spline and cubic polynomial surface
	 * 	Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface CubicPolynomialWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];
		org.drip.spline.params.SegmentCustomBuilderControl scbcSurface = null;
		org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan = null;

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		try {
			scbcWireSpan = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (4),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

			scbcSurface = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (4),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of quartic polynomial wire spline and quartic polynomial
	 * 	surface Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface QuarticPolynomialWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];
		org.drip.spline.params.SegmentCustomBuilderControl scbcSurface = null;
		org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan = null;

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		try {
			scbcWireSpan = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (5),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

			scbcSurface = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.spline.basis.PolynomialFunctionSetParams (5),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of Kaklis-Pandelis wire spline and Kaklis-Pandelis surface
	 * 	Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface KaklisPandelisWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];
		org.drip.spline.params.SegmentCustomBuilderControl scbcSurface = null;
		org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan = null;

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		try {
			scbcWireSpan = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KAKLIS_PANDELIS, new
					org.drip.spline.basis.KaklisPandelisSetParams (2),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

			scbcSurface = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KAKLIS_PANDELIS, new
					org.drip.spline.basis.KaklisPandelisSetParams (2),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of KLK Hyperbolic wire spline and KLK Hyperbolic surface
	 * 	Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * @param dblTension The Tension Parameter
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface KLKHyperbolicWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode,
		final double dblTension)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];
		org.drip.spline.params.SegmentCustomBuilderControl scbcSurface = null;
		org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan = null;

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		try {
			scbcWireSpan = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION, new
					org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

			scbcSurface = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION, new
					org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of KLK Rational Linear wire spline and KLK Rational Linear
	 * 	Surface Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * @param dblTension The Tension Parameter
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface KLKRationalLinearWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode,
		final double dblTension)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];
		org.drip.spline.params.SegmentCustomBuilderControl scbcSurface = null;
		org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan = null;

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		try {
			scbcWireSpan = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION,
					new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

			scbcSurface = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_LINEAR_TENSION,
					new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of KLK Rational Quadratic wire spline and KLK Rational
	 * 	Quadratic Surface Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * @param dblTension The Tension Parameter
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface KLKRationalQuadraticWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode,
		final double dblTension)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];
		org.drip.spline.params.SegmentCustomBuilderControl scbcSurface = null;
		org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan = null;

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		try {
			scbcWireSpan = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION,
					new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);

			scbcSurface = new org.drip.spline.params.SegmentCustomBuilderControl
				(org.drip.spline.stretch.MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_RATIONAL_QUADRATIC_TENSION,
					new org.drip.spline.basis.ExponentialTensionSetParams (dblTension),
						org.drip.spline.params.SegmentInelasticDesignControl.Create (2, 2), null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of Custom wire spline and Custom Surface Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblTenor Array of Maturity Tenors
	 * @param aadblNode Double Array of the Surface Nodes
	 * @param scbcWireSpan The Wire Span Segment Customizer
	 * @param scbcSurface The Surface Segment Customizer
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface CustomWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final double[][] aadblNode,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcSurface)
	{
		if (null == astrTenor) return null;

		int iNumTenor = astrTenor.length;
		double[] adblMaturity = new double[iNumTenor];

		if (0 == iNumTenor) return null;

		for (int i = 0; i < iNumTenor; ++i)
			adblMaturity[i] = dtStart.addTenor (astrTenor[i]).julian();

		return CustomSplineWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike,
			adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}

	/**
	 * Create a Price/Volatility Market Surface Based off of a Single Run using the Heston 1993 Model
	 * 
	 * @param strName Surface Name
	 * @param dtStart Epoch/Start Date
	 * @param strCurrency Currency
	 * @param dblRiskFreeRate Risk-Free Rate
	 * @param dblUnderlier The Underlier
	 * @param bIsForward TRUE => The Underlier represents the Forward, FALSE => it represents Spot
	 * @param dblInitialVolatility Initial Volatility
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param astrTenor Array of Maturity Tenors
	 * @param fphp The Heston Stochastic Volatility Generation Parameters
	 * @param bPriceSurface TRUE => Generate the Price Surface; FALSE => Generate the Vol Surface
	 * @param scbcWireSpan The Wire Span Segment Customizer
	 * @param scbcSurface The Surface Segment Customizer
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface HestonRunMarketSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double dblRiskFreeRate,
		final double dblUnderlier,
		final boolean bIsForward,
		final double dblInitialVolatility,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final java.lang.String[] astrTenor,
		final org.drip.param.pricer.HestonOptionPricerParams fphp,
		final boolean bPriceSurface,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcSurface)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblUnderlier) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblInitialVolatility) || null == adblStrike ||
					null == astrTenor || null == fphp)
			return null;

		int iStrike = 0;
		int iNumTenor = astrTenor.length;
		int iNumStrike = adblStrike.length;
		double[][] aadblImpliedNode = new double[iNumStrike][iNumTenor];
		org.drip.pricer.option.HestonStochasticVolatilityAlgorithm hsva = null;

		try {
			hsva = new org.drip.pricer.option.HestonStochasticVolatilityAlgorithm (fphp);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (0 == iNumTenor || 0 == iNumStrike) return null;

		for (double dblStrike : adblStrike) {
			int iTenor = 0;

			for (java.lang.String strTenor : astrTenor) {
				try {
					double dblTimeToExpiry = org.drip.analytics.support.AnalyticsHelper.TenorToYearFraction
						(strTenor);

					if (!hsva.compute (dblStrike, dblTimeToExpiry, dblRiskFreeRate, dblUnderlier, bIsForward,
						dblInitialVolatility, false))
						return null;

					aadblImpliedNode[iStrike][iTenor++] = bPriceSurface ? hsva.callPrice() : new
						org.drip.pricer.option.BlackScholesAlgorithm().implyBlackScholesVolatility
							(dblStrike, dblTimeToExpiry, dblRiskFreeRate, dblUnderlier, false,
								hsva.callPrice());
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			++iStrike;
		}

		return CustomWireSurface (strName, dtStart, strCurrency, collatParams, adblStrike, astrTenor,
			aadblImpliedNode, scbcWireSpan, scbcSurface);
	}
}
