
package org.drip.analytics.support;

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
 * OptionHelper contains the collection of the option valuation related utility functions used by the modules.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OptionHelper {

	static class CrossVolatilityQuantoProduct extends org.drip.quant.function.AbstractUnivariate {
		org.drip.quant.function.AbstractUnivariate _auFRIVolatility = null;
		org.drip.quant.function.AbstractUnivariate _auForwardToDomesticExchangeVolatility = null;
		org.drip.quant.function.AbstractUnivariate _auFRIForwardToDomesticExchangeCorrelation = null;

		CrossVolatilityQuantoProduct (
			final org.drip.quant.function.AbstractUnivariate auFRIVolatility,
			final org.drip.quant.function.AbstractUnivariate auForwardToDomesticExchangeVolatility,
			final org.drip.quant.function.AbstractUnivariate auFRIForwardToDomesticExchangeCorrelation)
		{
			super (null);

			_auFRIVolatility = auFRIVolatility;
			_auForwardToDomesticExchangeVolatility = auForwardToDomesticExchangeVolatility;
			_auFRIForwardToDomesticExchangeCorrelation = auFRIForwardToDomesticExchangeCorrelation;
		}

		@Override public double evaluate (
			final double dblVariate)
			throws java.lang.Exception
		{
			return _auFRIVolatility.evaluate (dblVariate) * _auForwardToDomesticExchangeVolatility.evaluate
				(dblVariate) * _auFRIForwardToDomesticExchangeCorrelation.evaluate (dblVariate);
		}
	}

	static class CrossVolatilityConvexityExponent extends org.drip.quant.function.AbstractUnivariate {
		double _dblForwardShiftedLogNormalScaler = java.lang.Double.NaN;
		double _dblDiscountShiftedLogNormalScaler = java.lang.Double.NaN;
		org.drip.quant.function.AbstractUnivariate _auForwardVolTS = null;
		org.drip.quant.function.AbstractUnivariate _auDiscountVolTS = null;
		org.drip.quant.function.AbstractUnivariate _auDiscountForwardCorrTS = null;

		CrossVolatilityConvexityExponent (
			final double dblDiscountShiftedLogNormalScaler,
			final double dblForwardShiftedLogNormalScaler,
			final org.drip.quant.function.AbstractUnivariate auDiscountVolTS,
			final org.drip.quant.function.AbstractUnivariate auForwardVolTS,
			final org.drip.quant.function.AbstractUnivariate auDiscountForwardCorrTS)
		{
			super (null);

			_auForwardVolTS = auForwardVolTS;
			_auDiscountVolTS = auDiscountVolTS;
			_auDiscountForwardCorrTS = auDiscountForwardCorrTS;
			_dblForwardShiftedLogNormalScaler = dblForwardShiftedLogNormalScaler;
			_dblDiscountShiftedLogNormalScaler = dblDiscountShiftedLogNormalScaler;
		}

		@Override public double evaluate (
			final double dblVariate)
			throws java.lang.Exception
		{
			double dblDiscountShiftedLogNormalScaler = java.lang.Double.isNaN
				(_dblDiscountShiftedLogNormalScaler) ? 1. : _dblDiscountShiftedLogNormalScaler;

			double dblForwardShiftedLogNormalScaler = java.lang.Double.isNaN
				(_dblForwardShiftedLogNormalScaler) ? 1. : _dblForwardShiftedLogNormalScaler;

			return _auDiscountForwardCorrTS.evaluate (dblVariate) * _auDiscountVolTS.evaluate (dblVariate) *
				_auForwardVolTS.evaluate (dblVariate) * dblDiscountShiftedLogNormalScaler *
					dblForwardShiftedLogNormalScaler - _auForwardVolTS.evaluate (dblVariate) *
						_auForwardVolTS.evaluate (dblVariate) * dblForwardShiftedLogNormalScaler *
							dblForwardShiftedLogNormalScaler;
		}
	}

	static class PeriodVariance extends org.drip.quant.function.AbstractUnivariate {
		org.drip.quant.function.AbstractUnivariate _auVolatility = null;

		PeriodVariance (
			final org.drip.quant.function.AbstractUnivariate auVolatility)
		{
			super (null);

			_auVolatility = auVolatility;
		}

		@Override public double evaluate (
			final double dblVariate)
			throws java.lang.Exception
		{
			return _auVolatility.evaluate (dblVariate) * _auVolatility.evaluate (dblVariate);
		}
	}

	/**
	 * Compute the Integrated Surface Variance given the corresponding volatility and the date spans
	 * 
	 * @param mktParams Market Parameters
	 * @param strVolSurface Name of the Volatility Surface
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Integrated Volatility Surface
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double IntegratedSurfaceVariance (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final java.lang.String strVolSurface,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || dblEndDate < dblStartDate)
			throw new java.lang.Exception ("OptionHelper::IntegratedSurfaceVariance => Invalid Inputs");

		if (null == csqs || null == strVolSurface || strVolSurface.isEmpty() || dblEndDate == dblStartDate)
			return 0.;

		org.drip.quant.function.AbstractUnivariate auVolSurface = csqs.customMetricVolSurface
			(org.drip.state.identifier.CustomMetricLabel.Standard (strVolSurface));

		return null != auVolSurface ? new PeriodVariance (auVolSurface).integrate (dblStartDate, dblEndDate)
			/ 365.25 : 0.;
	}

	/**
	 * Compute the Integrated Surface Variance given the corresponding volatility and the date spans
	 * 
	 * @param auVolSurface The Volatility Surface
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Integrated Volatility Surface
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double IntegratedSurfaceVariance (
		final org.drip.quant.function.AbstractUnivariate auVolSurface,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (null == auVolSurface || !org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || dblEndDate < dblStartDate)
			throw new java.lang.Exception ("OptionHelper::IntegratedSurfaceVariance => Invalid Inputs");

		return null != auVolSurface ? new PeriodVariance (auVolSurface).integrate (dblStartDate, dblEndDate)
			/ 365.25 : 0.;
	}

	/**
	 * Compute the Integrated Cross Volatility Quanto Product given the corresponding volatility and the
	 * 	correlation surfaces, and the date spans
	 * 
	 * @param auVolSurface1 Volatility Surface #1 Univariate Function
	 * @param auVolSurface2 Volatility Surface #2 Univariate Function
	 * @param auCorrSurface Correlation Surface Univariate Function
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Integrated Cross Volatility Quanto Product
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double IntegratedCrossVolQuanto (
		final org.drip.quant.function.AbstractUnivariate auVolSurface1,
		final org.drip.quant.function.AbstractUnivariate auVolSurface2,
		final org.drip.quant.function.AbstractUnivariate auCorrSurface,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || dblEndDate < dblStartDate)
			throw new java.lang.Exception ("OptionHelper::IntegratedCrossVolQuanto => Invalid Inputs");

		return null == auVolSurface1 || null == auVolSurface2 || null == auCorrSurface ? 0. : new
			CrossVolatilityQuantoProduct (auVolSurface1, auVolSurface2, auCorrSurface).integrate
				(dblStartDate, dblEndDate) / 365.25;
	}

	/**
	 * Compute the Integrated FRA Cross Volatility Convexity Exponent given the corresponding volatility and
	 * 	the correlation surfaces, and the date spans
	 * 
	 * @param auForwardVolTS Volatility Term Structure of the Funding Rate
	 * @param auFundingVolTS Volatility Term Structure of the Forward Rate
	 * @param auForwardFundingCorrTS Correlation Term Structure between the Forward and the Funding States
	 * @param dblForwardShiftedLogNormalScaler Scaling for the Forward Log Normal Volatility
	 * @param dblFundingShiftedLogNormalScaler Scaling for the Funding Log Normal Volatility
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Integrated FRA Cross Volatility Convexity Exponent
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double IntegratedFRACrossVolConvexityExponent (
		final org.drip.quant.function.AbstractUnivariate auForwardVolTS,
		final org.drip.quant.function.AbstractUnivariate auFundingVolTS,
		final org.drip.quant.function.AbstractUnivariate auForwardFundingCorrTS,
		final double dblForwardShiftedLogNormalScaler,
		final double dblFundingShiftedLogNormalScaler,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || dblEndDate < dblStartDate)
			throw new java.lang.Exception
				("OptionHelper::IntegratedFRACrossVolConvexityExponent => Invalid Inputs");

		return null == auFundingVolTS || null == auForwardVolTS || null == auForwardFundingCorrTS ? 0. :
			new CrossVolatilityConvexityExponent (dblFundingShiftedLogNormalScaler,
				dblForwardShiftedLogNormalScaler, auFundingVolTS, auForwardVolTS,
					auForwardFundingCorrTS).integrate (dblStartDate, dblEndDate) / 365.25;
	}

	/**
	 * Compute the Integrated Cross Volatility Quanto Product given the corresponding volatility and the
	 * 	correlation surfaces, and the date spans
	 * 
	 * @param csqs Market Parameters
	 * @param strVolSurface1 Name of the Volatility Surface #1
	 * @param strVolSurface2 Name of the Volatility Surface #2
	 * @param strCorrSurface Name of the Correlation Surface
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Integrated Cross Volatility Quanto Product
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double IntegratedCrossVolQuanto (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final java.lang.String strVolSurface1,
		final java.lang.String strVolSurface2,
		final java.lang.String strCorrSurface,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || dblEndDate < dblStartDate)
			throw new java.lang.Exception ("OptionHelper::IntegratedCrossVolQuanto => Invalid Inputs");

		if (null == csqs || null == strVolSurface1 || strVolSurface1.isEmpty() || null == strVolSurface2 ||
			strVolSurface2.isEmpty() || null == strCorrSurface || strCorrSurface.isEmpty() || dblEndDate ==
				dblStartDate)
			return 0.;

		return IntegratedCrossVolQuanto (csqs.customMetricVolSurface
			(org.drip.state.identifier.CustomMetricLabel.Standard (strVolSurface1)),
				csqs.customMetricVolSurface (org.drip.state.identifier.CustomMetricLabel.Standard
					(strVolSurface2)), csqs.customMetricVolSurface
						(org.drip.state.identifier.CustomMetricLabel.Standard (strCorrSurface)),
							dblStartDate, dblEndDate);
	}

	/**
	 * Compute the Multiplicative Cross Volatility Quanto Product given the corresponding volatility and the
	 * 	correlation surfaces, and the date spans
	 * 
	 * @param csqs Market Parameters
	 * @param strVolSurface1 Name of the Volatility Surface #1
	 * @param strVolSurface2 Name of the Volatility Surface #2
	 * @param strCorrSurface Name of the Correlation Surface
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Multiplicative Cross Volatility Quanto Product
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double MultiplicativeCrossVolQuanto (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final java.lang.String strVolSurface1,
		final java.lang.String strVolSurface2,
		final java.lang.String strCorrSurface,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		return java.lang.Math.exp (-1. * IntegratedCrossVolQuanto (csqs, strVolSurface1, strVolSurface2,
			strCorrSurface, dblStartDate, dblEndDate));
	}

	/**
	 * Compute the Integrated FRA Cross Volatility Convexity Adjuster given the corresponding volatility and
	 * 	the correlation surfaces, and the date spans
	 * 
	 * @param csqs Market Parameters
	 * @param forwardLabel Forward Latent State Label
	 * @param fundingLabel Funding Latent State Label
	 * @param dblForwardShiftedLogNormalScaler Scaling for the Forward Log Normal Volatility
	 * @param dblFundingShiftedLogNormalScaler Scaling for the Funding Log Normal Volatility
	 * @param dblStartDate Evolution Start Date
	 * @param dblEndDate Evolution End Date
	 * 
	 * @return The Integrated FRA Cross Volatility Convexity Adjuster
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public static final double IntegratedFRACrossVolConvexityAdjuster (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final double dblForwardShiftedLogNormalScaler,
		final double dblFundingShiftedLogNormalScaler,
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || dblEndDate < dblStartDate)
			throw new java.lang.Exception
				("OptionHelper::IntegratedFRACrossVolConvexityAdjuster => Invalid Inputs");

		return null == csqs || null == forwardLabel || null == fundingLabel || dblEndDate == dblStartDate ?
			0. : IntegratedFRACrossVolConvexityExponent (csqs.fundingCurveVolSurface (fundingLabel),
				csqs.forwardCurveVolSurface (forwardLabel), csqs.forwardFundingCorrSurface (forwardLabel,
					fundingLabel), dblFundingShiftedLogNormalScaler, dblForwardShiftedLogNormalScaler,
						dblStartDate, dblEndDate);
	}
}
