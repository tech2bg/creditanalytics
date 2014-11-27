
package org.drip.analytics.support;

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
 * LossQuadratureGenerator generates the decomposed Integrand Quadrature for the Loss Steps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class LossQuadratureGenerator {

	/**
	 * Generate the Set of Loss Quadrature Metrics from the Day Step Loss Periods
	 * 
	 * @param comp Component for which the measures are to be generated
	 * @param valParams ValuationParams from which the periods are generated
	 * @param period The enveloping coupon period
	 * @param dblWorkoutDate Double JulianDate representing the absolute end of all the generated periods
	 * @param iPeriodUnit Day Step Size Unit of the generated Loss Quadrature Periods
	 * @param csqs The Market Parameters Curves/Quotes
	 *  
	 * @return List of the generated LossQuadratureMetrics
	 */

	public static final java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics>
		GenerateDayStepLossPeriods (
			final org.drip.product.definition.CreditComponent comp,
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.cashflow.CompositePeriod period,
			final double dblWorkoutDate,
			final int iPeriodUnit,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == comp || null == valParams || null == period || null == csqs || null == csqs.creditCurve
			(comp.creditLabel()[0]) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate) ||
				period.startDate() > dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (comp.payCurrency()[0]));

		if (null == dc) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (comp.creditLabel()[0]);

		double dblPeriodEndDate = period.endDate() < dblWorkoutDate ? period.endDate() : dblWorkoutDate;

		boolean bPeriodDone = false;

		double dblSubPeriodStart = period.startDate() < valParams.valueDate() ? valParams.valueDate() :
			period.startDate();

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		while (!bPeriodDone) {
			double dblSubPeriodEnd = dblSubPeriodStart + iPeriodUnit;

			if (dblSubPeriodEnd < valParams.valueDate()) return null;

			try {
				if (dblSubPeriodEnd >= dblPeriodEndDate) {
					bPeriodDone = true;

					dblSubPeriodEnd = period.endDate();
				}

				org.drip.analytics.cashflow.LossQuadratureMetrics lp =
					org.drip.analytics.cashflow.LossQuadratureMetrics.MakeDefaultPeriod (dblSubPeriodStart,
						dblSubPeriodEnd, period.accrualDCF (0.5 * (dblSubPeriodStart + dblSubPeriodEnd)),
							comp.notional (dblSubPeriodStart, dblSubPeriodEnd), comp.recovery
								(dblSubPeriodStart, dblSubPeriodEnd, cc), dc, cc,
									comp.creditValuationParams()._iDefPayLag);

				if (null != lp) sLP.add (lp);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblSubPeriodStart = dblSubPeriodEnd;
		}

		return sLP;
	}

	/**
	 * Generate the Set of Loss Quadrature Metrics from the Day Step Loss Periods
	 * 
	 * @param comp Component for which the measures are to be generated
	 * @param valParams ValuationParams from which the periods are generated
	 * @param period The enveloping coupon period
	 * @param dblWorkoutDate Double JulianDate representing the absolute end of all the generated periods
	 * @param iPeriodUnit Loss Grid Size Unit of the generated Loss Quadrature Periods
	 * @param csqs The Market Parameters Curves/Quotes
	 *  
	 * @return List of the generated LossQuadratureMetrics
	 */

	public static final java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics>
		GeneratePeriodUnitLossPeriods (
			final org.drip.product.definition.CreditComponent comp,
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.cashflow.CompositePeriod period,
			final double dblWorkoutDate,
			final int iPeriodUnit,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == comp || null == valParams || null == period || null == csqs || null == csqs.creditCurve
			(comp.creditLabel()[0]) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate) ||
				period.startDate() > dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (comp.payCurrency()[0]));

		if (null == dc) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (comp.creditLabel()[0]);

		double dblPeriodEndDate = period.endDate() < dblWorkoutDate ? period.endDate() : dblWorkoutDate;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		boolean bPeriodDone = false;

		if (period.endDate() < valParams.valueDate()) return null;

		double dblSubPeriodStart = period.startDate() < valParams.valueDate() ? valParams.valueDate() :
			period.startDate();

		int iDayStep = (int) ((period.endDate() - dblSubPeriodStart) / (iPeriodUnit));

		if (iDayStep < org.drip.param.pricer.PricerParams.PERIOD_DAY_STEPS_MINIMUM)
			iDayStep = org.drip.param.pricer.PricerParams.PERIOD_DAY_STEPS_MINIMUM;

		while (!bPeriodDone) {
			double dblSubPeriodEnd = dblSubPeriodStart + iDayStep;

			if (dblSubPeriodEnd < valParams.valueDate()) return null;

			try {
				if (dblSubPeriodEnd >= dblPeriodEndDate) {
					bPeriodDone = true;

					dblSubPeriodEnd = period.endDate();
				}

				org.drip.analytics.cashflow.LossQuadratureMetrics lp =
					org.drip.analytics.cashflow.LossQuadratureMetrics.MakeDefaultPeriod (dblSubPeriodStart,
						dblSubPeriodEnd, period.accrualDCF (0.5 * (dblSubPeriodStart + dblSubPeriodEnd)),
							comp.notional (dblSubPeriodStart, dblSubPeriodEnd), comp.recovery
								(dblSubPeriodStart, dblSubPeriodEnd, cc),  dc, cc,
									comp.creditValuationParams()._iDefPayLag);

				if (null != lp) sLP.add (lp);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblSubPeriodStart = dblSubPeriodEnd;
		}

		return sLP;
	}


	/**
	 * Generate the Set of Loss Quadrature Metrics from the Day Step Loss Periods
	 * 
	 * @param comp Component for which the measures are to be generated
	 * @param valParams ValuationParams from which the periods are generated
	 * @param period The enveloping coupon period
	 * @param dblWorkoutDate Double JulianDate representing the absolute end of all the generated periods
	 * @param csqs The Market Parameters Curves/Quotes
	 *  
	 * @return List of the generated LossQuadratureMetrics
	 */

	public static final java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics>
		GenerateWholeLossPeriods (
			final org.drip.product.definition.CreditComponent comp,
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.cashflow.CompositePeriod period,
			final double dblWorkoutDate,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == comp || null == valParams || null == period || null == csqs || null == csqs.creditCurve
			(comp.creditLabel()[0]) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate) ||
				period.startDate() > dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (comp.payCurrency()[0]));

		if (null == dc) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (comp.creditLabel()[0]);

		double dblPeriodEndDate = period.endDate() < dblWorkoutDate ? period.endDate() : dblWorkoutDate;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		try {
			double dblPeriodStartDate = period.startDate() < valParams.valueDate() ? valParams.valueDate() :
				period.startDate();

			org.drip.analytics.cashflow.LossQuadratureMetrics lp =
				org.drip.analytics.cashflow.LossQuadratureMetrics.MakeDefaultPeriod (dblPeriodStartDate,
					dblPeriodEndDate, period.accrualDCF (0.5 * (dblPeriodStartDate + dblPeriodEndDate)),
						comp.notional (dblPeriodStartDate, dblPeriodEndDate), comp.recovery
							(dblPeriodStartDate, dblPeriodEndDate, cc), dc, cc,
								comp.creditValuationParams()._iDefPayLag);

			if (null != lp) sLP.add (lp);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return sLP;
	}
}
