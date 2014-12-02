
package org.drip.product.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * FixedIncomeComponent abstract class extends the MarketParamRef and provides the following methods:
 *  - Get the product's initial notional, notional, and coupon.
 *  - Get the Effective date, Maturity date, First Coupon Date.
 *  - List the coupon periods.
 *  - Set the market curves - discount, TSY, forward, and Credit curves.
 *  - Retrieve the product's settlement parameters.
 *  - Value the product's using standard/custom market parameters.
 *  - Retrieve the product's named measures and named measure values.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class FixedIncomeComponent implements org.drip.product.definition.MarketParamRef {
	protected double measure (
		final java.lang.String strMeasure,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalc)
		throws java.lang.Exception
	{
		if (null == strMeasure || strMeasure.isEmpty() || null == mapCalc || null == mapCalc.entrySet())
			throw new java.lang.Exception ("FixedIncomeComponent::measure => Invalid Inputs");

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCalc.entrySet()) {
			if (null != me.getKey() && me.getKey().equalsIgnoreCase (strMeasure)) return me.getValue();
		}

		throw new java.lang.Exception ("FixedIncomeComponent::measure => Invalid Measure: " + strMeasure);
	}

	protected boolean adjustPVDFMicroJackForCashSettle (
		final double dblSettleDate,
		final double dblPV,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack)
	{
		org.drip.quant.calculus.WengertJacobian wjCashSettleDFDF = dc.jackDDFDManifestMeasure (dblSettleDate,
			"Rate");

		if (null == wjCashSettleDFDF) return false;

		double dblDFCashSettle = java.lang.Double.NaN;

		int iNumParameters = wjCashSettleDFDF.numParameters();

		try {
			dblDFCashSettle = dc.df (dblSettleDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (!wjPVDFMicroJack.scale (1. / dblDFCashSettle)) return false;

		double dblSettleJackAdjust = -1. * dblPV / dblDFCashSettle / dblDFCashSettle;

		for (int k = 0; k < iNumParameters; ++k) {
			if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, dblSettleJackAdjust *
				wjCashSettleDFDF.firstDerivative (0, k)))
				return false;
		}

		return true;
	}

	/**
	 * Get the Initial Notional for the Product
	 * 
	 * @return Initial Notional
	 * 
	 * @throws java.lang.Exception Thrown if Initial Notional cannot be computed
	 */

	public abstract double initialNotional()
		throws java.lang.Exception;

	/**
	 * Get the Notional for the Product at the given date
	 * 
	 * @param dblDate Double date input
	 * 
	 * @return Product Notional
	 * 
	 * @throws java.lang.Exception Thrown if Notional cannot be computed
	 */

	public abstract double notional (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Get the time-weighted Notional for the Product between 2 dates
	 * 
	 * @param dblDate1 Double date first
	 * @param dblDate2 Double date second
	 * 
	 * @return The Product Notional
	 * 
	 * @throws java.lang.Exception Thrown if Notional cannot be computed
	 */

	public abstract double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception;

	/**
	 * Get the Product's coupon at the specified accrual date
	 * 
	 * @param dblAccrualEndDate Accrual End Date
	 * @param valParams The Valuation Parameters
	 * @param csqs Component Market Parameters
	 * 
	 * @return The Product's coupon Nominal/Adjusted Coupon Measures
	 */

	public abstract org.drip.analytics.output.CompositePeriodCouponMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs);

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public abstract int freq();

	/**
	 * Get the Effective Date
	 * 
	 * @return Effective Date
	 */

	public abstract org.drip.analytics.date.JulianDate effective();

	/**
	 * Get the Maturity Date
	 * 
	 * @return Maturity Date
	 */

	public abstract org.drip.analytics.date.JulianDate maturity();

	/**
	 * Get the First Coupon Date
	 * 
	 * @return First Coupon Date
	 */

	public abstract org.drip.analytics.date.JulianDate firstCouponDate();

	/**
	 * Get the Product's Cash Flow Periods
	 * 
	 * @return List of the Product's Cash Flow Periods
	 */

	public abstract java.util.List<org.drip.analytics.cashflow.CompositePeriod> cashFlowPeriod();

	/**
	 * Get the Product's cash settlement parameters
	 * 
	 * @return Cash settlement Parameters
	 */

	public abstract org.drip.param.valuation.CashSettleParams cashSettleParams();

	/**
	 * Generate a full list of the Product measures for the full input set of market parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param csqs Market Parameters
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Map of measure name and value
	 */

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams);

	/**
	 * Retrieve the ordered set of the measure names whose values will be calculated
	 * 
	 * @return Set of Measure Names
	 */

	public abstract java.util.Set<java.lang.String> measureNames();

	/**
	 * Retrieve the Instrument's Imputed Tenor
	 * 
	 * @return The Instrument's Imputed Tenor
	 */

	public java.lang.String tenor()
	{
		double dblNumDays = maturity().julian() - effective().julian();

		if (365. > dblNumDays) {
			int iNumMonth = (int) (0.5 + (dblNumDays / 30.));

			return 12 == iNumMonth ? "1Y" : iNumMonth + "M";
		}

		 return ((int) (0.5 + (dblNumDays / 365.))) + "Y";
	}

	/**
	 * Calculate the value of the given Product's measure
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param csqs ComponentMarketParams
	 * @param strMeasure Measure String
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Double measure value
	 * 
	 * @throws java.lang.Exception Thrown if the measure cannot be calculated
	 */

	public double measureValue (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final java.lang.String strMeasure)
		throws java.lang.Exception
	{
		return measure (strMeasure, value (valParams, pricerParams, csqs, quotingParams));
	}

	/**
	 * Generate a full list of the Product's measures for the set of scenario market parameters present in
	 * 	the org.drip.param.definition.MarketParams
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mpc org.drip.param.definition.MarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return ComponentOutput object
	 */

	public org.drip.analytics.output.ComponentMeasures measures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ScenarioMarketParams mpc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mpc || null == mpc.scenMarketParams (this, "Base")) return null;

		org.drip.analytics.output.ComponentMeasures compOp = new
			org.drip.analytics.output.ComponentMeasures();

		long lStart = System.nanoTime();

		if (!compOp.setBaseMeasures (value (valParams, pricerParams, mpc.scenMarketParams (this, "Base"),
			quotingParams)))
			return null;

		if (null != mpc.scenMarketParams (this, "FlatCreditBumpUp")) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCreditBumpUp = value
				(valParams, pricerParams, mpc.scenMarketParams (this, "FlatCreditBumpUp"), quotingParams);

			if (null != mapCreditBumpUp && null != mapCreditBumpUp.entrySet()) {
				compOp.setFlatCreditDeltaMeasures (new
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

				for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCreditBumpUp.entrySet())
				{
					if (null == me || null == me.getKey()) continue;

					compOp.flatCreditDeltaMeasures().put (me.getKey(), me.getValue() -
						compOp.baseMeasures().get (me.getKey()));
				}

				if (null != mpc.scenMarketParams (this, "FlatCreditBumpDn")) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCreditBumpDn =
						value (valParams, pricerParams, mpc.scenMarketParams (this, "FlatCreditBumpDn"),
							quotingParams);

					if (null != mapCreditBumpUp && null != mapCreditBumpDn.entrySet()) {
						compOp.setFlatCreditGammaMeasures (new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapCreditBumpDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							compOp.flatCreditGammaMeasures().put (me.getKey(), me.getValue() +
								mapCreditBumpUp.get (me.getKey()) - 2. * compOp.baseMeasures().get
									(me.getKey()));
						}
					}
				}
			}
		}

		if (null != mpc.scenMarketParams (this, "RRBumpUp")) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRRBumpUp = value (valParams,
				pricerParams, mpc.scenMarketParams (this, "RRBumpUp"), quotingParams);

			compOp.setFlatRRDeltaMeasures (new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

			if (null != mapRRBumpUp && null != mapRRBumpUp.entrySet()) {
				for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapRRBumpUp.entrySet()) {
					if (null == me || null == me.getKey()) continue;

					compOp.flatRRDeltaMeasures().put (me.getKey(), me.getValue() - compOp.baseMeasures().get
						(me.getKey()));
				}

				if (null != mpc.scenMarketParams (this, "RRBumpDn")) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRRBumpDn = value
						(valParams, pricerParams, mpc.scenMarketParams (this, "RRBumpDn"), quotingParams);

					if (null != mapRRBumpDn && null != mapRRBumpDn.entrySet()) {
						compOp.setFlatRRGammaMeasures (new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapRRBumpDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							compOp.flatRRGammaMeasures().put (me.getKey(), me.getValue() + mapRRBumpUp.get
								(me.getKey()) - 2. * compOp.baseMeasures().get (me.getKey()));
						}
					}
				}
			}
		}

		if (null != mpc.scenMarketParams (this, "IRCreditBumpUp")) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRBumpUp = value (valParams,
				pricerParams, mpc.scenMarketParams (this, "IRCreditBumpUp"), quotingParams);

			if (null != mapIRBumpUp && null != mapIRBumpUp.entrySet()) {
				compOp.setFlatIRDeltaMeasures (new
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

				for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapIRBumpUp.entrySet()) {
					if (null == me || null == me.getKey()) continue;

					compOp.flatIRDeltaMeasures().put (me.getKey(), me.getValue() - compOp.baseMeasures().get
						(me.getKey()));
				}

				if (null != mpc.scenMarketParams (this, "IRCreditBumpDn")) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIRBumpDn = value
						(valParams, pricerParams, mpc.scenMarketParams (this, "IRCreditBumpDn"),
							quotingParams);

					if (null != mapIRBumpDn && null != mapIRBumpDn.entrySet()) {
						compOp.setFlatIRGammaMeasures (new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>());

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapIRBumpDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							compOp.flatIRGammaMeasures().put (me.getKey(), me.getValue() + mapIRBumpUp.get
								(me.getKey()) - 2. * compOp.baseMeasures().get (me.getKey()));
						}
					}
				}
			}
		}

		if (null != mpc.creditTenorMarketParams (this, true)) {
			compOp.setTenorCreditDeltaMeasures (new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>());

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
				mapCCTenorUpCSQS = mpc.creditTenorMarketParams (this, true);

			if (null != mapCCTenorUpCSQS && null != mapCCTenorUpCSQS.entrySet()) {
				for (java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet>
					meTenorUpMP : mapCCTenorUpCSQS.entrySet()) {
					if (null == meTenorUpMP || null == meTenorUpMP.getValue()) continue;

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorUp = value
						(valParams, pricerParams, meTenorUpMP.getValue(), quotingParams);

					if (null == mapCCTenorUp || null == mapCCTenorUp.entrySet()) continue;

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcUp = new
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

					for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
						mapCCTenorUp.entrySet()) {
						if (null == me || null == me.getKey()) continue;

						mapCalcUp.put (me.getKey(), me.getValue() - compOp.baseMeasures().get (me.getKey()));
					}

					compOp.tenorCreditDeltaMeasures().put (meTenorUpMP.getKey(), mapCalcUp);
				}

				if (null != mpc.creditTenorMarketParams (this, false)) {
					compOp.setTenorCreditGammaMeasures (new
						org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>());

					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
						mapCCTenorDnCSQS = mpc.creditTenorMarketParams (this, false);

					if (null != mapCCTenorDnCSQS && null != mapCCTenorDnCSQS.entrySet()) {
						for (java.util.Map.Entry<java.lang.String,
							org.drip.param.market.CurveSurfaceQuoteSet> meTenorDnMP :
								mapCCTenorDnCSQS.entrySet()) {
							if (null == meTenorDnMP || null == meTenorDnMP.getValue()) continue;

							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorDn =
								value (valParams, pricerParams, meTenorDnMP.getValue(), quotingParams);

							if (null == mapCCTenorDn || null == mapCCTenorDn.entrySet()) continue;

							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcDn = new
									org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

							for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
								mapCCTenorDn.entrySet()) {
								if (null == me || null == me.getKey()) continue;

								mapCalcDn.put (me.getKey(), me.getValue() - compOp.baseMeasures().get
									(me.getKey()) + compOp.tenorCreditDeltaMeasures().get
										(meTenorDnMP.getKey()).get (me.getKey()));
							}

							compOp.tenorCreditGammaMeasures().put (meTenorDnMP.getKey(), mapCalcDn);
						}
					}
				}
			}
		}

		if (null != mpc.irTenorMarketParams (this, true)) {
			compOp.setTenorIRDeltaMeasures (new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>());

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
				mapIRTenorUpCSQS = mpc.irTenorMarketParams (this, true);

			if (null != mapIRTenorUpCSQS && null != mapIRTenorUpCSQS.entrySet()) {
				for (java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet>
					meTenorUpMP : mapIRTenorUpCSQS.entrySet()) {
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorUp = value
						(valParams, pricerParams, meTenorUpMP.getValue(), quotingParams);

					if (null == mapCCTenorUp || null == mapCCTenorUp.entrySet()) continue;

					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcUp = new
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

					for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
						mapCCTenorUp.entrySet()) {
						if (null == me || null == me.getKey()) continue;

						mapCalcUp.put (me.getKey(), me.getValue() - compOp.baseMeasures().get (me.getKey()));
					}

					compOp.tenorIRDeltaMeasures().put (meTenorUpMP.getKey(), mapCalcUp);
				}
			}

			if (null != mpc.irTenorMarketParams (this, false)) {
				compOp.setTenorIRGammaMeasures (new
					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>());

				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
					mapIRTenorDnCSQS = mpc.irTenorMarketParams (this, false);

				if (null != mapIRTenorDnCSQS & null != mapIRTenorDnCSQS.entrySet()) {
					for (java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet>
						meTenorDnMP : mapIRTenorDnCSQS.entrySet()) {
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCCTenorDn = value
							(valParams, pricerParams, meTenorDnMP.getValue(), quotingParams);

						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalcDn = new
							org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

						if (null == mapCalcDn || null == mapCalcDn.entrySet()) continue;

						for (java.util.Map.Entry<java.lang.String, java.lang.Double> me :
							mapCCTenorDn.entrySet()) {
							if (null == me || null == me.getKey()) continue;

							mapCalcDn.put (me.getKey(), me.getValue() - compOp.baseMeasures().get
								(me.getKey()) + compOp.tenorIRDeltaMeasures().get (meTenorDnMP.getKey()).get
									(me.getKey()));
						}

						compOp.tenorIRGammaMeasures().put (meTenorDnMP.getKey(), mapCalcDn);
					}
				}
			}
		}

		compOp.setCalcTime ((System.nanoTime() - lStart) * 1.e-09);

		return compOp;
	}

	/**
	 * Generate a full list of custom measures for the set of scenario market parameters present in
	 * 	the org.drip.param.definition.MarketParams
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mpc org.drip.param.definition.MarketParams
	 * @param strCustomScenName Custom Scenario Name
	 * @param quotingParams Quoting Parameters
	 * @param mapBaseOP Base OP from used to calculate the desired delta measure. If null, the base OP will
	 * 	be generated.
	 * 
	 * @return Custom Scenarios Measures output set
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> customScenarioMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ScenarioMarketParams mpc,
		final java.lang.String strCustomScenName,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBaseOP)
	{
		if (null == strCustomScenName || strCustomScenName.isEmpty() || null == valParams || null == mpc ||
			null == mpc.scenMarketParams (this, strCustomScenName))
			return null;

		if (null == mapBaseOP) {
			org.drip.param.market.CurveSurfaceQuoteSet csqsBase = mpc.scenMarketParams (this, "Base");

			if (null == csqsBase) return null;

			if (null == (mapBaseOP = value (valParams, pricerParams, csqsBase, quotingParams))) return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCustomOP = value (valParams,
			pricerParams, mpc.scenMarketParams (this, strCustomScenName), quotingParams);

		if (null == mapCustomOP) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCustomOPDelta = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		if (null != mapCustomOP && null != mapCustomOP.entrySet()) {
			for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCustomOP.entrySet()) {
				if (null == me || null == me.getKey()) continue;

				mapCustomOPDelta.put (me.getKey(), me.getValue() - mapBaseOP.get (me.getKey()));
			}
		}

		return mapCustomOPDelta;
	}
}
