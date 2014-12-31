
package org.drip.product.fra;

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
 * FRAStandardComponent contains the implementation of the Standard Multi-Curve FRA Component.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAStandardComponent extends org.drip.product.rates.SingleStreamComponent {
	private double _dblStrike = java.lang.Double.NaN;

	/**
	 * FRAStandardComponent constructor
	 * 
	 * @param strName Futures Component Name
	 * @param stream Futures Stream
	 * @param dblStrike Futures Strike
	 * @param csp Cash Settle Parameters Instance
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public FRAStandardComponent (
		final java.lang.String strName,
		final org.drip.product.rates.Stream stream,
		final double dblStrike,
		final org.drip.param.valuation.CashSettleParams csp)
		throws java.lang.Exception
	{
		super (strName, stream, csp);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStrike = dblStrike))
			throw new java.lang.Exception ("FRAStandardComponent ctr => Invalid Inputs!");
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.state.identifier.FundingLabel fundingLabel =
			org.drip.state.identifier.FundingLabel.Standard (payCurrency());

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		double dblParForward = java.lang.Double.NaN;

		double dblValueDate = valParams.valueDate();

		double dblEffectiveDate = effectiveDate().julian();

		if (dblValueDate > dblEffectiveDate) return null;

		org.drip.analytics.date.JulianDate dtMaturity = maturityDate();

		double dblMaturityDate = dtMaturity.julian();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel().get ("DERIVED");

		org.drip.analytics.rates.ForwardRateEstimator fc = csqs.forwardCurve (forwardLabel);

		if (null == fc || !forwardLabel.match (fc.index())) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		org.drip.param.valuation.CashSettleParams settleParams = cashSettleParams();

		try {
			double dblCashSettle = null == settleParams ? valParams.cashPayDate() :
				settleParams.cashSettleDate (dblValueDate);

			dblParForward = csqs.available (dtMaturity, forwardLabel) ? csqs.fixing (dtMaturity,
				forwardLabel) : fc.forward (dtMaturity);

			double dblMultiplicativeQuantoAdjustment = java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.forwardCurveVolSurface (forwardLabel), csqs.fundingCurveVolSurface (fundingLabel),
						csqs.forwardFundingCorrSurface (forwardLabel, fundingLabel), dblValueDate,
							dblEffectiveDate));

			double dblDCF = org.drip.analytics.daycount.Convention.YearFraction (dblEffectiveDate,
				dblMaturityDate, stream().couponDC(), false, null, stream().calendar());

			double dblQuantoAdjustedParForward = dblParForward * dblMultiplicativeQuantoAdjustment;

			double dblDV01 = dblDCF * dcFunding.df (dblMaturityDate) / dcFunding.df (dblCashSettle) *
				notional (dblValueDate);

			double dblPV = dblDV01 * (dblQuantoAdjustedParForward - _dblStrike);

			double dblDCParForward = dcFunding.libor (dblEffectiveDate, dblMaturityDate);

			mapResult.put ("additivequantoadjustment", dblQuantoAdjustedParForward - dblParForward);

			mapResult.put ("discountcurveadditivebasis", dblQuantoAdjustedParForward - dblDCParForward);

			mapResult.put ("discountcurvemultiplicativebasis", dblQuantoAdjustedParForward /
				dblDCParForward);

			mapResult.put ("discountcurveparforward", dblDCParForward);

			mapResult.put ("dv01", dblDV01);

			mapResult.put ("forward", dblParForward);

			mapResult.put ("forwardrate", dblParForward);

			mapResult.put ("mercuriorfactor", (dblDCF * dblDCParForward + 1.) / (dblDCF *
				dblQuantoAdjustedParForward + 1.));

			mapResult.put ("multiplicativequantoadjustment", dblMultiplicativeQuantoAdjustment);

			mapResult.put ("parforward", dblParForward);

			mapResult.put ("parforwardrate", dblParForward);

			mapResult.put ("price", dblPV);

			mapResult.put ("pv", dblPV);

			mapResult.put ("quantoadjustedparforward", dblQuantoAdjustedParForward);

			mapResult.put ("upfront", dblPV);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("AdditiveQuantoAdjustment");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("DiscountCurveAdditiveBasis");

		setstrMeasureNames.add ("DiscountCurveMultiplicativeBasis");

		setstrMeasureNames.add ("DiscountCurveParForward");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("Forward");

		setstrMeasureNames.add ("ForwardRate");

		setstrMeasureNames.add ("MercurioRFactor");

		setstrMeasureNames.add ("MultiplicativeQuantoAdjustment");

		setstrMeasureNames.add ("ParForward");

		setstrMeasureNames.add ("ParForwardRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("QuantoAdjustedParForward");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		try {
			return new org.drip.product.calib.FRAComponentQuoteSet (aLSS);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FRAComponentQuoteSet))
			return null;

		if (valParams.valueDate() > effectiveDate().julian()) return null;

		org.drip.product.calib.FRAComponentQuoteSet fcqs = (org.drip.product.calib.FRAComponentQuoteSet) pqs;

		if (!fcqs.containsFRARate() && !fcqs.containsParForwardRate()) return null;

		double dblForwardRate = java.lang.Double.NaN;

		try {
			if (fcqs.containsParForwardRate())
				dblForwardRate = fcqs.parForwardRate();
			else if (fcqs.containsFRARate())
				dblForwardRate = fcqs.fraRate();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		double dblMaturity = maturityDate().julian();

		if (!prwc.addPredictorResponseWeight (dblMaturity, 1.)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("Rate", dblMaturity, 1.)) return null;

		if (!prwc.updateValue (dblForwardRate)) return null;

		if (!prwc.updateDValueDManifestMeasure ("Rate", 1.)) return null;

		return prwc;
	}

	/**
	 * Retrieve the FRA Strike
	 * 
	 * @return The FRA Strike
	 */

	public double strike()
	{
		return _dblStrike;
	}
}
