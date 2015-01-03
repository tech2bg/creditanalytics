
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
 * ScenarioLocalVolatilityBuilder implements the construction of the Local Volatility surface using the input
 * 	option instruments, their Call Prices, and a wide variety of custom build schemes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ScenarioLocalVolatilityBuilder {

	/**
	 * Build an Instance of the Volatility Surface using custom wire span and surface splines
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param dblRiskFreeRate Risk Free Discounting Rate
	 * @param collatParams Collateral Parameters
	 * @param adblStrike Array of Strikes
	 * @param adblMaturity Array of Maturities
	 * @param aadblCallPrice Double Array of the Call Prices
	 * @param scbcWireSpan The Wire Span Segment Customizer
	 * @param scbcSurface The Surface Segment Customizer
	 * 
	 * @return Instance of the Market Node Surface
	 */

	public static final org.drip.analytics.definition.MarketSurface CustomSplineWireSurface (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double dblRiskFreeRate,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final double[] adblStrike,
		final double[] adblMaturity,
		final double[][] aadblCallPrice,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcWireSpan,
		final org.drip.spline.params.SegmentCustomBuilderControl scbcSurface)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblRiskFreeRate)) return null;

		org.drip.analytics.definition.MarketSurface msCallPrice =
			org.drip.param.creator.ScenarioMarketSurfaceBuilder.CustomSplineWireSurface (strName +
				"_CALL_PRICE_SURFACE", dtStart, strCurrency, collatParams, adblStrike, adblMaturity,
					aadblCallPrice, scbcWireSpan, scbcSurface);

		if (null == msCallPrice) return null;

		int iNumStrike = adblStrike.length;
		int iNumMaturity = adblMaturity.length;
		double[][] aadblLocalVolatility = new double[iNumStrike][iNumMaturity];
		org.drip.analytics.definition.TermStructure[] aTSMaturityAnchor = new
			org.drip.analytics.definition.TermStructure[iNumMaturity];

		for (int j = 0; j < iNumMaturity; ++j) {
			if (null == (aTSMaturityAnchor[j] = msCallPrice.maturityAnchorTermStructure (adblMaturity[j])))
				return null;
		}

		for (int i = 0; i < iNumStrike; ++i) {
			org.drip.analytics.definition.TermStructure tsStrikeAnchor =
				msCallPrice.strikeAnchorTermStructure (adblStrike[i]);

			if (null == tsStrikeAnchor) return null;

			for (int j = 0; j < iNumMaturity; ++j) {
				try {
					aadblLocalVolatility[i][j] = java.lang.Math.sqrt ((tsStrikeAnchor.nodeDerivative
						(adblMaturity[j], 1) + dblRiskFreeRate * adblStrike[i] *
							aTSMaturityAnchor[j].nodeDerivative (adblStrike[i], 1)) / (adblStrike[i] *
								adblStrike[i] * aTSMaturityAnchor[j].nodeDerivative (adblStrike[i], 2)));
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}
		}

		return org.drip.param.creator.ScenarioMarketSurfaceBuilder.CustomSplineWireSurface (strName, dtStart,
			strCurrency, collatParams, adblStrike, adblMaturity, aadblLocalVolatility, scbcWireSpan,
				scbcSurface);
	}

	/**
	 * Construct a Scenario Market Surface off of cubic polynomial wire spline and cubic polynomial surface
	 * 	Spline.
	 * 
	 * @param strName Name of the Volatility Surface
	 * @param dtStart Start/Epoch Julian Date
	 * @param strCurrency Currency
	 * @param dblRiskFreeRate Risk Free Discounting Rate
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
		final double dblRiskFreeRate,
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

		return CustomSplineWireSurface (strName, dtStart, strCurrency, dblRiskFreeRate, collatParams,
			adblStrike, adblMaturity, aadblNode, scbcWireSpan, scbcSurface);
	}
}
