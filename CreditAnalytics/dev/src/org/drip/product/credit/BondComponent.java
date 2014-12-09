
package org.drip.product.credit;

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
 * BondComponent is the base class that extends CreditComponent abstract class and implements the functionality behind
 * 		bonds of all kinds. Bond static data is captured in a set of 11 container classes – BondTSYParams,
 * 		BondCouponParams, BondNotionalParams, BondFloaterParams, BondCurrencyParams, BondIdentifierParams,
 * 		BondIRValuationParams, CompCRValParams, BondCFTerminationEvent, BondFixedPeriodGenerationParams, and
 * 		one EmbeddedOptionSchedule object instance each for the call and the put objects. Each of these
 * 		parameter set can be set separately.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondComponent extends org.drip.product.definition.Bond implements
	org.drip.product.definition.BondProduct {
	private static final boolean s_bSuppressErrors = true;
	private static final boolean s_bYieldDFOffofCouponAccrualDCF = true;

	/*
	 * EOS Control
	 */

	private static final int LEFT_EOS_SNIP = 1;

	/*
	 * Width for calculating local forward rate width
	 */

	private static final int LOCAL_FORWARD_RATE_WIDTH = 1;

	/*
	 * Recovery Period discretization Mode
	 */

	private static final int s_iDiscretizationScheme =
		org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP;

	/*
	 * Discount Curve to derive the zero curve off of
	 */

	private static final int ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE = 1;
	private static final int ZERO_OFF_OF_TREASURIES_DISCOUNT_CURVE = 2;

	private org.drip.product.params.BondStream _stream = null;
	private org.drip.product.params.TsyBmkSet _tsyBmkSet = null;
	private org.drip.product.params.CouponSetting _cpnParams = null;
	private org.drip.product.params.NotionalSetting _notlParams = null;
	private org.drip.product.params.FloaterSetting _fltParams = null;
	private org.drip.product.params.CurrencySet _ccyParams = null;
	private org.drip.product.params.IdentifierSet _idParams = null;
	private org.drip.product.params.QuoteConvention _mktConv = null;
	private org.drip.product.params.RatesSetting _irValParams = null;
	private org.drip.product.params.CreditSetting _crValParams = null;
	private org.drip.param.market.LatentStateFixingsContainer _lsfc = null;
	private org.drip.product.params.TerminationSetting _terminationSetting = null;

	/*
	 * Bond EOS Params
	 */

	protected org.drip.product.params.EmbeddedOptionSchedule _eosPut = null;
	protected org.drip.product.params.EmbeddedOptionSchedule _eosCall = null;

	private double treasuryBenchmarkYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate))
			throw new java.lang.Exception ("Bond::treasuryBenchmarkYield => Invalid Inputs");

		java.lang.String strTsyBmk = null;
		org.drip.param.definition.ProductQuote cqTsyBmkYield = null;

		if (null != _tsyBmkSet) strTsyBmk = _tsyBmkSet.primaryBenchmark();

		double dblValDate = valParams.valueDate();

		if (null == strTsyBmk || strTsyBmk.isEmpty())
			strTsyBmk = org.drip.analytics.support.AnalyticsHelper.BaseTsyBmk (dblValDate, dblWorkoutDate);

		if (null != csqs.quoteMap() && null != strTsyBmk && !strTsyBmk.isEmpty())
			cqTsyBmkYield = csqs.quoteMap().get (strTsyBmk);

		if (null != cqTsyBmkYield) {
			 org.drip.param.definition.Quote q = cqTsyBmkYield.quote ("Yield");

			 if (null != q) return q.value ("mid");
		}

		org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()));

		return null == dcGovvie ? java.lang.Double.NaN : dcGovvie.libor (dblValDate, dblWorkoutDate);
	}

	private org.drip.param.valuation.WorkoutInfo exerciseCallYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblPrice) || null
			== _eosCall)
			return null;

		double dblValDate = valParams.valueDate();

		double[] adblEOSDate = _eosCall.dates();

		double[] adblEOSFactor = _eosCall.factors();

		int iNoticePeriod = _eosCall.exerciseNoticePeriod();

		double dblMaturity = maturityDate().julian();

		int iExercise = -1;
		double dblExerciseYield = java.lang.Double.NaN;
		int iNumEOSDate = null == adblEOSDate ? 0 : adblEOSDate.length;

		try {
			dblExerciseYield = yieldFromPrice (valParams, csqs, vcp, dblMaturity, 1., dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumEOSDate; ++i) {
			if (dblValDate > adblEOSDate[i] + LEFT_EOS_SNIP || adblEOSDate[i] - dblValDate < iNoticePeriod)
				continue;

			try {
				double dblYield = yieldFromPrice (valParams, csqs, vcp, adblEOSDate[i], adblEOSFactor[i],
					dblPrice);

				if (dblYield < dblExerciseYield) {
					iExercise = i;
					dblExerciseYield = dblYield;
				}
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		try {
			if (-1 == iExercise)
				return new org.drip.param.valuation.WorkoutInfo (dblMaturity, dblExerciseYield, 1.,
					org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);

			return new org.drip.param.valuation.WorkoutInfo (adblEOSDate[iExercise], dblExerciseYield,
				adblEOSFactor[iExercise], org.drip.param.valuation.WorkoutInfo.WO_TYPE_CALL);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.param.valuation.WorkoutInfo exercisePutYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblPrice) || null
			== _eosPut)
			return null;

		double dblValDate = valParams.valueDate();

		double[] adblEOSDate = _eosPut.dates();

		double[] adblEOSFactor = _eosPut.factors();

		int iNoticePeriod = _eosCall.exerciseNoticePeriod();

		double dblMaturity = maturityDate().julian();

		int iExercise = -1;
		double dblExerciseYield = java.lang.Double.NaN;
		int iNumEOSDate = null == adblEOSDate ? 0 : adblEOSDate.length;

		try {
			dblExerciseYield = yieldFromPrice (valParams, csqs, vcp, dblMaturity, 1., dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumEOSDate; ++i) {
			if (dblValDate > adblEOSDate[i] + LEFT_EOS_SNIP || adblEOSDate[i] - dblValDate < iNoticePeriod)
				continue;

			try {
				double dblYield = yieldFromPrice (valParams, csqs, vcp, adblEOSDate[i], adblEOSFactor[i],
					dblPrice);

				if (dblYield > dblExerciseYield) {
					iExercise = i;
					dblExerciseYield = dblYield;
				}
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		try {
			if (-1 == iExercise)
				return new org.drip.param.valuation.WorkoutInfo (dblMaturity, dblExerciseYield, 1.,
					org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);

			return new org.drip.param.valuation.WorkoutInfo (adblEOSDate[iExercise], dblExerciseYield,
				adblEOSFactor[iExercise], org.drip.param.valuation.WorkoutInfo.WO_TYPE_PUT);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.param.valuation.WorkoutInfo exerciseYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			return null;

		double dblMaturity = maturityDate().julian();

		try {
			if (null == _eosCall && null == _eosPut)
				return new org.drip.param.valuation.WorkoutInfo (dblMaturity, yieldFromPrice (valParams,
					csqs, vcp, dblMaturity, 1., dblPrice), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);

			if (null == _eosCall && null != _eosPut)
				return exercisePutYieldFromPrice (valParams, csqs, vcp, dblPrice);

			if (null != _eosCall && null == _eosPut)
				return exerciseCallYieldFromPrice (valParams, csqs, vcp, dblPrice);

			org.drip.param.valuation.WorkoutInfo wiPut = exercisePutYieldFromPrice (valParams, csqs, vcp,
				dblPrice);

			org.drip.param.valuation.WorkoutInfo wiCall = exerciseCallYieldFromPrice (valParams, csqs, vcp,
				dblPrice);

			if (null == wiPut || null == wiCall) return null;

			return wiPut.date() < wiCall.date() ? wiPut : wiCall;
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private double indexRate (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.analytics.cashflow.CompositeFloatingPeriod period)
		throws java.lang.Exception
	{
		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve (fundingLabel());

		int iFreq = freq();

		if (null != period) {
			org.drip.analytics.cashflow.ComposableUnitPeriod cupFirst = period.periods().get (0);

			if (!(cupFirst instanceof org.drip.analytics.cashflow.ComposableUnitFloatingPeriod))
				throw new java.lang.Exception ("BondComponent::indexRate => Not a floater");

			double dblFixingDate = ((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)
				cupFirst).referenceIndexPeriod().fixingDate();

			if (!csqs.available (dblFixingDate, _fltParams._fri)) {
				org.drip.analytics.rates.ForwardRateEstimator fc = null;

				double dblPayDate = period.payDate();

				double dblStartDate = period.startDate();

				if (null != _fltParams && null != _fltParams._fri) {
					if (null == (fc = csqs.forwardCurve (_fltParams._fri)) || !_fltParams._fri.match
						(fc.index()))
						fc = dc.forwardRateEstimator (dblPayDate, _fltParams._fri);
				}

				if (null != fc) return fc.forward (dblPayDate);

				return dblStartDate < dblValueDate && 0 != iFreq ? dc.libor (dblValueDate, (12 / iFreq) +
					"M") : dc.libor (dblStartDate, period.endDate());
			}

			return csqs.fixing (dblFixingDate, _fltParams._fri);
		}

		return dc.libor (dblValueDate, 0 != iFreq ? dblValueDate + 365.25 / iFreq : dblValueDate +
			LOCAL_FORWARD_RATE_WIDTH);
	}

	private org.drip.analytics.output.BondWorkoutMeasures workoutMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate,
		final double dblWorkoutFactor)
	{
		if (null == valParams || null == csqs) return null;

		double dblValueDate = valParams.valueDate();

		if (!org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) || dblValueDate >= dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

		double dblAccrued01 = 0.;
		double dblRecoveryPV = 0.;
		boolean bPeriodZero = true;
		double dblExpectedRecovery = 0.;
		double dblCreditRiskyDirtyDV01 = 0.;
		boolean bTerminateCouponFlow = false;
		double dblCreditRiskyPrincipalPV = 0.;
		double dblCreditRisklessDirtyDV01 = 0.;
		double dblCreditRiskyDirtyCouponPV = 0.;
		double dblCreditRisklessPrincipalPV = 0.;
		double dblCreditRisklessDirtyCouponPV = 0.;
		double dblFirstCoupon = java.lang.Double.NaN;
		double dblCreditRiskyDirtyIndexCouponPV = 0.;
		double dblFirstIndexRate = java.lang.Double.NaN;
		double dblCreditRisklessDirtyIndexCouponPV = 0.;
		double dblCreditRiskyParPV = java.lang.Double.NaN;
		double dblCreditRisklessParPV = java.lang.Double.NaN;

		try {
			for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
				double dblPeriodPayDate = period.payDate();

				if (dblPeriodPayDate < dblValueDate) continue;

				double dblPeriodEndDate = period.endDate();

				double dblPeriodStartDate = period.startDate();

				if (dblWorkoutDate <= dblPeriodEndDate) {
					bTerminateCouponFlow = true;
					dblPeriodEndDate = dblWorkoutDate;
				}

				org.drip.analytics.output.CompositePeriodCouponMetrics cpcm = couponMetrics (dblValueDate,
					valParams, csqs);

				if (null == cpcm) return null;

				double dblPeriodCoupon = cpcm.rate();

				double dblPeriodBaseRate = period.periods().get (0).baseRate (csqs);

				double dblPeriodAnnuity = dcFunding.df (dblPeriodPayDate) * cpcm.cumulative();

				if (bPeriodZero) {
					bPeriodZero = false;
					dblFirstCoupon = dblPeriodCoupon;

					if (dblPeriodStartDate < dblValueDate)
						dblAccrued01 = 0.0001 * period.accrualDCF (dblValueDate) * notional
							(dblPeriodStartDate, dblValueDate);

					if (null != _fltParams) dblFirstIndexRate = dblPeriodBaseRate;
				}

				double dblPeriodCreditRisklessDirtyDV01 = 0.0001 * period.accrualDCF (dblPeriodEndDate) *
					dblPeriodAnnuity * notional (dblPeriodStartDate, dblPeriodEndDate);

				double dblPeriodCreditRiskessPrincipalPV = (notional (dblPeriodStartDate) - notional
					(dblPeriodEndDate)) * dblPeriodAnnuity;

				double dblPeriodCreditRiskyDirtyDV01 = dblPeriodCreditRisklessDirtyDV01;
				double dblPeriodCreditRiskyPrincipalPV = dblPeriodCreditRiskessPrincipalPV;

				if (null != cc && null != pricerParams) {
					double dblSurvProb = cc.survival (pricerParams.survivalToPayDate() ? dblPeriodPayDate :
						dblPeriodEndDate);

					dblPeriodCreditRiskyDirtyDV01 *= dblSurvProb;
					dblPeriodCreditRiskyPrincipalPV *= dblSurvProb;

					for (org.drip.analytics.cashflow.LossQuadratureMetrics lqm : period.lossMetrics (this,
						valParams, pricerParams, dblWorkoutDate, csqs)) {
						if (null == lqm) continue;

						double dblSubPeriodEnd = lqm.end();

						double dblSubPeriodStart = lqm.start();

						double dblSubPeriodDF = dcFunding.effectiveDF (dblSubPeriodStart +
							_crValParams._iDefPayLag, dblSubPeriodEnd + _crValParams._iDefPayLag);

						double dblSubPeriodNotional = notional (dblSubPeriodStart, dblSubPeriodEnd);

						double dblSubPeriodSurvival = cc.survival (dblSubPeriodStart) - cc.survival
							(dblSubPeriodEnd);

						if (_crValParams._bAccrOnDefault)
							dblPeriodCreditRiskyDirtyDV01 += 0.0001 * lqm.accrualDCF() * dblSubPeriodSurvival
								* dblSubPeriodDF * dblSubPeriodNotional;

						double dblRecovery = _crValParams._bUseCurveRec ? cc.effectiveRecovery
							(dblSubPeriodStart, dblSubPeriodEnd) : _crValParams._dblRecovery;

						double dblSubPeriodExpRecovery = dblRecovery * dblSubPeriodSurvival *
							dblSubPeriodNotional;
						dblRecoveryPV += dblSubPeriodExpRecovery * dblSubPeriodDF;
						dblExpectedRecovery += dblSubPeriodExpRecovery;
					}
				}

				dblCreditRiskyDirtyDV01 += dblPeriodCreditRiskyDirtyDV01;
				dblCreditRiskyPrincipalPV += dblPeriodCreditRiskyPrincipalPV;
				dblCreditRisklessDirtyDV01 += dblPeriodCreditRisklessDirtyDV01;
				dblCreditRisklessPrincipalPV += dblPeriodCreditRiskessPrincipalPV;
				dblCreditRiskyDirtyCouponPV += 10000. * dblPeriodCoupon * dblPeriodCreditRiskyDirtyDV01;
				dblCreditRisklessDirtyCouponPV += 10000. * dblPeriodCoupon *
					dblPeriodCreditRisklessDirtyDV01;
				dblCreditRiskyDirtyIndexCouponPV += 10000. * dblPeriodBaseRate *
					dblPeriodCreditRiskyDirtyDV01;
				dblCreditRisklessDirtyIndexCouponPV += 10000. * dblPeriodBaseRate *
					dblPeriodCreditRisklessDirtyDV01;

				if (bTerminateCouponFlow) break;
			}
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			return null;
		}

		double dblCashPayDate = java.lang.Double.NaN;

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		try {
			double dblCashPayDF = dcFunding.df (dblCashPayDate);

			double dblMaturity = maturityDate().julian();

			dblCreditRisklessParPV = dcFunding.df (dblMaturity) * notional (dblMaturity) * dblWorkoutFactor;

			if (null != cc && null != pricerParams)
				dblCreditRiskyParPV = dblCreditRisklessParPV * cc.survival (dblMaturity);

			org.drip.analytics.output.BondCouponMeasures bcmCreditRisklessDirty = new
				org.drip.analytics.output.BondCouponMeasures (dblCreditRisklessDirtyDV01,
					dblCreditRisklessDirtyIndexCouponPV, dblCreditRisklessDirtyCouponPV,
						dblCreditRisklessDirtyCouponPV + dblCreditRisklessPrincipalPV +
							dblCreditRisklessParPV);

			double dblDefaultExposure = java.lang.Double.NaN;
			double dblDefaultExposureNoRec = java.lang.Double.NaN;
			double dblLossOnInstantaneousDefault = java.lang.Double.NaN;
			org.drip.analytics.output.BondCouponMeasures bcmCreditRiskyDirty = null;

			if (null != cc && null != pricerParams) {
				double dblInitialNotional = notional (dblValueDate);

				double dblInitialRecovery = cc.recovery (dblValueDate);

				bcmCreditRiskyDirty = new org.drip.analytics.output.BondCouponMeasures
					(dblCreditRiskyDirtyDV01, dblCreditRiskyDirtyIndexCouponPV, dblCreditRiskyDirtyCouponPV,
						dblCreditRiskyDirtyCouponPV + dblCreditRiskyPrincipalPV + dblCreditRiskyParPV);

				dblDefaultExposure = (dblDefaultExposureNoRec = dblInitialNotional) * dblInitialRecovery;
				dblLossOnInstantaneousDefault = dblInitialNotional * (1. - dblInitialRecovery);
			}

			return new org.drip.analytics.output.BondWorkoutMeasures (bcmCreditRiskyDirty,
				bcmCreditRisklessDirty, dblCreditRiskyParPV, dblCreditRisklessParPV,
					dblCreditRiskyPrincipalPV, dblCreditRisklessPrincipalPV, dblRecoveryPV,
						dblExpectedRecovery, dblDefaultExposure, dblDefaultExposureNoRec,
							dblLossOnInstantaneousDefault, dblAccrued01, dblFirstCoupon, dblFirstIndexRate,
								dblCashPayDF);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> rvMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.param.valuation.WorkoutInfo wi,
		final double dblPrice,
		final java.lang.String strPrefix)
	{
		if (null == strPrefix) return null;

		org.drip.analytics.output.BondRVMeasures bmRV = standardMeasures (valParams, pricerParams, csqs, vcp,
			wi, dblPrice);

		return null == bmRV ? null : bmRV.toMap (strPrefix);
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> fairMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		double dblMaturity = maturityDate().julian();

		org.drip.analytics.output.BondWorkoutMeasures bwmFair = workoutMeasures (valParams, pricerParams,
			csqs, dblMaturity, 1.);

		if (null == bwmFair) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = bwmFair.toMap ("");

		double dblPrice = (null == bwmFair.creditRiskyCleanbcm() || !org.drip.quant.common.NumberUtil.IsValid
			(bwmFair.creditRiskyCleanbcm().pv())) ? bwmFair.creditRisklessCleanbcm().pv() :
				bwmFair.creditRiskyCleanbcm().pv();

		try {
			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, rvMeasures (valParams,
				pricerParams, csqs, vcp, new org.drip.param.valuation.WorkoutInfo (dblMaturity,
					yieldFromPrice (valParams, csqs, vcp, dblPrice / notional (valParams.valueDate())), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY), dblPrice, ""));

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures,
				org.drip.quant.common.CollectionUtil.PrefixKeys (mapMeasures, "Fair"));

			return mapMeasures;
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> marketMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.param.valuation.WorkoutInfo wiMarket)
	{
		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = rvMeasures
				(valParams, pricerParams, csqs, vcp, wiMarket, priceFromYield (valParams, csqs, vcp,
					wiMarket.date(), wiMarket.factor(), wiMarket.yield()), "");

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures,
				org.drip.quant.common.CollectionUtil.PrefixKeys (mapMeasures, "Market"));

			return mapMeasures;
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.cashflow.CompositePeriod currentPeriod (
		final double dblDate)
	{
		try {
			return _stream.period (_stream.periodIndex (dblDate));
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		double dblExerciseFactor = 1.;
		double dblCleanPrice = java.lang.Double.NaN;

		double dblExerciseDate = maturityDate().julian();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalibMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		org.drip.param.definition.CalibrationParams calibParams = null == pricerParams ? null :
			pricerParams.calibParams();

		if (null == calibParams) return null;

		org.drip.param.valuation.WorkoutInfo wi = calibParams.workout();

		if (null != wi) {
			dblExerciseDate = wi.date();

			dblExerciseFactor = wi.factor();
		}

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

		try {
			if (null == cc)
				dblCleanPrice = priceFromBumpedDC (valParams, csqs, dblExerciseDate, dblExerciseFactor, 0.);
			else
				dblCleanPrice = priceFromBumpedCC (valParams, csqs, dblExerciseDate, dblExerciseFactor, 0.,
					false);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			return null;
		}

		if (!org.drip.quant.common.NumberUtil.IsValid (dblCleanPrice)) return null;

		java.lang.String strCalibMeasure = calibParams.measure();

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"CleanPrice", "FairCleanPrice", "FairPrice", "Price"}, false)) {
			mapCalibMeasures.put (strCalibMeasure, dblCleanPrice);

			return mapCalibMeasures;
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"DirtyPrice", "FairDirtyPrice"}, false)) {
			try {
				mapCalibMeasures.put (strCalibMeasure, dblCleanPrice + accrued (valParams.valueDate(),
					csqs));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"Yield", "FairYield"}, false)) {
			try {
				mapCalibMeasures.put (strCalibMeasure, yieldFromPrice (valParams, csqs, vcp, dblExerciseDate,
					dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"TSYSpread", "FairTSYSpread"}, false)) {
			try {
				mapCalibMeasures.put (strCalibMeasure, tsySpreadFromPrice (valParams, csqs, vcp,
					dblExerciseDate, dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"OAS", "OASpread", "OptionAdjustedSpread"}, false)) {
			try {
				mapCalibMeasures.put (strCalibMeasure, oasFromPrice (valParams, csqs, vcp, dblExerciseDate,
					dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"BondBasis", "YieldBasis", "YieldSpread"}, false)) {
			try {
				mapCalibMeasures.put (strCalibMeasure, bondBasisFromPrice (valParams, csqs, vcp,
					dblExerciseDate, dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"CreditBasis"}, false)) {
			try {
				if (null == cc) return null;

				mapCalibMeasures.put (strCalibMeasure, creditBasisFromPrice (valParams, csqs, vcp,
					dblExerciseDate, dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (strCalibMeasure, new java.lang.String[]
			{"PECS", "ParEquivalentCDSSpread"}, false)) {
			try {
				if (null == cc) return null;

				mapCalibMeasures.put (strCalibMeasure, pecsFromPrice (valParams, csqs, vcp, dblExerciseDate,
					dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Constructor: Construct an empty bond object
	 */

	public BondComponent()
	{
	}

	@Override public double[] secTreasurySpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote> mapTSYQuote
			= csqs.quoteMap();

		if (null == mapTSYQuote || 0 == mapTSYQuote.size()) return null;

		java.lang.String[] astrTreasuryBenchmark = null == _tsyBmkSet ? null :
			_tsyBmkSet.secondaryBenchmarks();

		int iNumTreasuryBenchmark = null == astrTreasuryBenchmark ? 0 : astrTreasuryBenchmark.length;
		double[] adblSecTSYSpread = new double[iNumTreasuryBenchmark];

		if (0 == iNumTreasuryBenchmark) return null;

		for (int i = 0; i < iNumTreasuryBenchmark; ++i) {
			org.drip.param.definition.ProductQuote pqTSYBenchmark = null == astrTreasuryBenchmark[i] || null
				== astrTreasuryBenchmark[i] ? null : mapTSYQuote.get (astrTreasuryBenchmark[i]);

			org.drip.param.definition.Quote q = null == pqTSYBenchmark ? null : pqTSYBenchmark.quote
				("Yield");

			adblSecTSYSpread[i] = null == q ? java.lang.Double.NaN : q.value ("mid");
		}

		return adblSecTSYSpread;
	}

	@Override public double effectiveTreasuryBenchmarkYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("Bond::effectiveTreasuryBenchmarkYield => Bad val/mkt Params");

		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("Bond::effectiveTreasuryBenchmarkYield => Invalid Work-out!");

		java.lang.String strTreasuryBenchmark = null != _tsyBmkSet ? _tsyBmkSet.primaryBenchmark() : null;

		double dblValueDate = valParams.valueDate();

		double dblWorkoutDate = wi.date();

		if (null == strTreasuryBenchmark || strTreasuryBenchmark.isEmpty())
			strTreasuryBenchmark = org.drip.analytics.support.AnalyticsHelper.BaseTsyBmk (dblValueDate,
				dblWorkoutDate);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote> mapTSYQuote
			= csqs.quoteMap();

		org.drip.param.definition.ProductQuote pqTSYBenchmark = null != mapTSYQuote && 0 !=
			mapTSYQuote.size() && null != strTreasuryBenchmark && !strTreasuryBenchmark.isEmpty() ?
				mapTSYQuote.get (strTreasuryBenchmark) : null;

		org.drip.param.definition.Quote q = null != pqTSYBenchmark ? pqTSYBenchmark.quote ("Yield") : null;

		if (null != q) return q.value ("mid");

		org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()));

		return null == dcGovvie ? java.lang.Double.NaN : dcGovvie.libor (dblValueDate, dblWorkoutDate);
	}

	@Override public boolean setTreasuryBenchmark (
		final org.drip.product.params.TsyBmkSet tsyBmkSet)
	{
		return null != (_tsyBmkSet = tsyBmkSet);
	}

	@Override public org.drip.product.params.TsyBmkSet treasuryBenchmark()
	{
		return _tsyBmkSet;
	}

	@Override public boolean setIdentifierSet (
		final org.drip.product.params.IdentifierSet idParams)
	{
		return null != (_idParams = idParams);
	}

	@Override public org.drip.product.params.IdentifierSet identifierSet()
	{
		return _idParams;
	}

	@Override public boolean setCouponSetting (
		final org.drip.product.params.CouponSetting cpnParams)
	{
		return null != (_cpnParams = cpnParams);
	}

	@Override public org.drip.product.params.CouponSetting couponSetting()
	{
		return _cpnParams;
	}

	@Override public boolean setCurrencySet (
		final org.drip.product.params.CurrencySet ccyParams)
	{
		return null == (_ccyParams = ccyParams);
	}

	@Override public org.drip.product.params.CurrencySet currencyParams()
	{
		return _ccyParams;
	}

	@Override public boolean setFloaterSetting (
		final org.drip.product.params.FloaterSetting fltParams)
	{
		return null == (_fltParams = fltParams);
	}

	@Override public org.drip.product.params.FloaterSetting floaterSetting()
	{
		return _fltParams;
	}

	@Override public boolean setFixings (
		final org.drip.param.market.LatentStateFixingsContainer lsfc)
	{
		_lsfc = lsfc;
		return true;
	}

	@Override public org.drip.param.market.LatentStateFixingsContainer fixings()
	{
		return _lsfc;
	}

	@Override public boolean setMarketConvention (
		final org.drip.product.params.QuoteConvention mktConv)
	{
		return null == (_mktConv = mktConv);
	}

	@Override public org.drip.product.params.QuoteConvention marketConvention()
	{
		return _mktConv;
	}

	@Override public boolean setRatesSetting (
		final org.drip.product.params.RatesSetting irValParams)
	{
		return null == (_irValParams = irValParams);
	}

	@Override public org.drip.product.params.RatesSetting ratesSetting()
	{
		return _irValParams;
	}

	@Override public boolean setCreditSetting (
		final org.drip.product.params.CreditSetting crValParams)
	{
		return null == (_crValParams = crValParams);
	}

	@Override public org.drip.product.params.CreditSetting creditSetting()
	{
		return _crValParams;
	}

	@Override public boolean setTerminationSetting (
		final org.drip.product.params.TerminationSetting terminationSetting)
	{
		return null == (_terminationSetting = terminationSetting);
	}

	@Override public org.drip.product.params.TerminationSetting terminationSetting()
	{
		return _terminationSetting;
	}

	@Override public boolean setStream (
		final org.drip.product.params.BondStream stream)
	{
		return null != (_stream = stream);
	}

	@Override public org.drip.product.params.BondStream stream()
	{
		return _stream;
	}

	@Override public boolean setNotionalSetting (
		final org.drip.product.params.NotionalSetting notlParams)
	{
		return null == (_notlParams = notlParams);
	}

	@Override public org.drip.product.params.NotionalSetting notionalSetting()
	{
		return _notlParams;
	}

	@Override public java.lang.String primaryCode()
	{
		return null == _idParams ? null : "BOND." + _idParams._strID;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		// _strCode = strCode;
	}

	@Override public java.lang.String[] secondaryCode()
	{
		return new java.lang.String[] {_idParams._strID};
	}

	@Override public java.lang.String isin()
	{
		return null == _idParams ? null : _idParams._strISIN;
	}

	@Override public java.lang.String cusip()
	{
		return null == _idParams ? null : _idParams._strCUSIP;
	}

	@Override public java.lang.String name()
	{
		return null == _idParams ? null : _idParams._strID;
	}

	@Override public java.util.List<java.lang.String> couponCurrency()
	{
		java.util.List<java.lang.String> lsCouponCurrency = new java.util.ArrayList<java.lang.String>();

		for (java.lang.String strCouponCurrency : _ccyParams.couponCurrency())
			lsCouponCurrency.add (strCouponCurrency);

		return lsCouponCurrency;
	}

	@Override public java.lang.String payCurrency()
	{
		return _ccyParams.couponCurrency()[0];
	}

	@Override public java.lang.String principalCurrency()
	{
		return _ccyParams.principalCurrency()[0];
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlParams || null == _notlParams._fsPrincipalOutstanding ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("Bond::notional => Bad state/inputs");

		return _notlParams._fsPrincipalOutstanding.factor (dblDate);
	}

	@Override public double notional (
		final double dblStartDate,
		final double dblEndDate)
		throws java.lang.Exception
	{
		if (null == _notlParams || null == _notlParams._fsPrincipalOutstanding ||
			!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblEndDate))
			throw new java.lang.Exception ("Bond::notional => Bad state/inputs");

		return _notlParams._fsPrincipalOutstanding.factor (dblStartDate, dblEndDate);
	}

	@Override public double initialNotional()
		throws java.lang.Exception
	{
		if (null == _notlParams) throw new java.lang.Exception ("Bond::initialNotional => Bad state/inputs");

		return _notlParams._dblNotional;
	}

	@Override public double recovery (
		final double dblDate,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || null == cc)
			throw new java.lang.Exception ("Bond::recovery: Bad state/inputs");

		return _crValParams._bUseCurveRec ? cc.recovery (dblDate) : _crValParams._dblRecovery;
	}

	@Override public double recovery (
		final double dblStartDate,
		final double dblEndDate,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEndDate) || null == cc)
			throw new java.lang.Exception ("Bond::recovery: Bad state/inputs");

		return _crValParams._bUseCurveRec ? cc.effectiveRecovery (dblStartDate, dblEndDate) :
			_crValParams._dblRecovery;
	}

	@Override public org.drip.product.params.CreditSetting creditValuationParams()
	{
		return _crValParams;
	}

	@Override public org.drip.analytics.output.CompositePeriodCouponMetrics couponMetrics (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		try {
			org.drip.analytics.cashflow.CompositePeriod period = currentPeriod (dblAccrualEndDate);

			org.drip.analytics.output.UnitPeriodMetrics upm = new org.drip.analytics.output.UnitPeriodMetrics
				(period.startDate(), period.endDate(), period.couponDCF(), period.couponMetrics
					(valParams.valueDate(), csqs).rate(), new
						org.drip.analytics.output.ConvexityAdjustment());

			java.util.List<org.drip.analytics.output.UnitPeriodMetrics> lsUPM = new
				java.util.ArrayList<org.drip.analytics.output.UnitPeriodMetrics>();

			lsUPM.add (upm);

			return org.drip.analytics.output.CompositePeriodCouponMetrics.Create (lsUPM);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public int freq()
	{
		return couponPeriods().get (0).freq();
	}

	@Override public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return null == _crValParams || null == _crValParams._strCC || _crValParams._strCC.isEmpty() ? null :
			org.drip.state.identifier.CreditLabel.Standard (_crValParams._strCC);
	}

	@Override public java.util.List<org.drip.state.identifier.ForwardLabel> forwardLabel()
	{
		if (null == _fltParams) return null;

		java.util.List<org.drip.state.identifier.ForwardLabel> lsFRI = new
			java.util.ArrayList<org.drip.state.identifier.ForwardLabel>();

		lsFRI.add (_fltParams._fri);

		return lsFRI;
	}

	@Override public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return org.drip.state.identifier.FundingLabel.Standard (_irValParams._strCouponDiscountCurve);
	}

	@Override public java.util.List<org.drip.state.identifier.FXLabel> fxLabel()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effectiveDate()
	{
		return _stream.effective();
	}

	@Override public org.drip.analytics.date.JulianDate maturityDate()
	{
		return _stream.maturity();
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (couponPeriods().get (0).endDate());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.CompositePeriod> couponPeriods()
	{
		return null == _stream ? null : _stream.periods();
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _mktConv._settleParams;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> lossFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == valParams || null == pricerParams || null == csqs) return null;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			if (null == period) continue;

			java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLPSub = period.lossMetrics
				(this, valParams, pricerParams, period.endDate(), csqs);

			if (null != sLPSub) sLP.addAll (sLPSub);
		}

		return sLP;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> lossFlowFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
	{
		if (null == valParams || null == pricerParams || null == csqs ||
			!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			return null;

		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi) return null;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		double dblValueDate = valParams.valueDate();

		double dblWorkoutDate = wi.date();

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			if (null == period) continue;

			double dblPeriodEndDate = period.endDate();

			if (null == period || dblPeriodEndDate < dblValueDate) continue;

			if (period.startDate() > dblWorkoutDate) break;

			java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLPSub = period.lossMetrics
				(this, valParams, pricerParams, dblPeriodEndDate, csqs);

			if (null != sLPSub) sLP.addAll (sLPSub);
		}

		return sLP;
	}

	@Override public boolean isFloater()
	{
		return null == _fltParams ? false : true;
	}

	@Override public java.lang.String rateIndex()
	{
		return null == _fltParams ? "" : _fltParams._fri.fullyQualifiedName();
	}

	@Override public double currentCoupon()
	{
		return null == _fltParams ? java.lang.Double.NaN : _fltParams._dblCurrentCoupon;
	}

	@Override public double floatSpread()
	{
		return null == _fltParams ? java.lang.Double.NaN : _fltParams._dblFloatSpread;
	}

	@Override public java.lang.String ticker()
	{
		return null == _idParams ? null : _idParams._strTicker;
	}

	@Override public void setEmbeddedCallSchedule (
		final org.drip.product.params.EmbeddedOptionSchedule eos)
	{
		if (null == eos || eos.isPut()) return;

		_eosCall = new org.drip.product.params.EmbeddedOptionSchedule (eos);
	}

	@Override public void setEmbeddedPutSchedule (
		final org.drip.product.params.EmbeddedOptionSchedule eos)
	{
		if (null == eos || !eos.isPut()) return;

		_eosPut = new org.drip.product.params.EmbeddedOptionSchedule (eos);
	}

	@Override public boolean callable()
	{
		return null != _eosCall;
	}

	@Override public boolean putable()
	{
		return null != _eosPut;
	}

	@Override public boolean sinkable()
	{
		return null == _notlParams ? false : true;
	}

	@Override public boolean variableCoupon()
	{
		return null == _cpnParams || null == _cpnParams._strCouponType || !"variable".equalsIgnoreCase
			(_cpnParams._strCouponType) ? false : true;
	}

	@Override public boolean exercised()
	{
		return null == _terminationSetting ? false : _terminationSetting.exercised();
	}

	@Override public boolean defaulted()
	{
		return null == _terminationSetting ? false : _terminationSetting.defaulted();
	}

	@Override public boolean perpetual()
	{
		return null == _terminationSetting ? false : _terminationSetting.perpetual();
	}

	@Override public boolean tradeable (
		final org.drip.param.valuation.ValuationParams valParams)
		throws java.lang.Exception
	{
		if (null == valParams) throw new java.lang.Exception ("BondComponent::tradeable => invalid Inputs");

		return !_terminationSetting.exercised() && !_terminationSetting.defaulted() && valParams.valueDate()
			< maturityDate().julian();
	}

	@Override public org.drip.product.params.EmbeddedOptionSchedule callSchedule()
	{
		return _eosCall;
	}

	@Override public org.drip.product.params.EmbeddedOptionSchedule putSchedule()
	{
		return _eosPut;
	}

	@Override public java.lang.String couponType()
	{
		return null == _cpnParams ? "" : _cpnParams._strCouponType;
	}

	@Override public java.lang.String couponDC()
	{
		return null == _stream ? "" : _stream.couponDC();
	}

	@Override public java.lang.String accrualDC()
	{
		return null == _stream ? "" : _stream.accrualDC();
	}

	@Override public java.lang.String maturityType()
	{
		return null == _stream ? "" : maturityType();
	}

	@Override public org.drip.analytics.date.JulianDate finalMaturity()
	{
		try {
			return null == _stream ? null : new org.drip.analytics.date.JulianDate (_stream.finalMaturity());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String calculationType()
	{
		return null == _mktConv ? "" : _mktConv._strCalculationType;
	}

	@Override public double redemptionValue()
	{
		return null == _mktConv ? java.lang.Double.NaN : _mktConv._dblRedemptionValue;
	}

	@Override public java.lang.String currency()
	{
		if (null == _ccyParams) return "";

		java.lang.String[] astrCouponCurrency = _ccyParams.couponCurrency();

		return null == astrCouponCurrency || 0 == astrCouponCurrency.length ? "" : astrCouponCurrency[0];
	}

	@Override public java.lang.String redemptionCurrency()
	{
		if (null == _ccyParams) return "";

		java.lang.String[] astrPrincipalCurrency = _ccyParams.principalCurrency();

		return null == astrPrincipalCurrency || 0 == astrPrincipalCurrency.length ? "" :
			astrPrincipalCurrency[0];
	}

	@Override public boolean inFirstCouponPeriod (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("BondComponent::inFirstCouponPeriod => Input date is NaN");

		return _stream.firstPeriod().contains (dblDate);
	}

	@Override public boolean inLastCouponPeriod (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("BondComponent::inLastCouponPeriod => Input date is NaN");

		return _stream.lastPeriod().contains (dblDate);
	}

	@Override public java.lang.String floatCouponConvention()
	{
		return null == _fltParams ? "" : _fltParams._strFloatDayCount;
	}

	@Override public org.drip.analytics.date.JulianDate periodFixingDate (
		final double dblValueDate)
	{
		if (null == _fltParams || !org.drip.quant.common.NumberUtil.IsValid (dblValueDate) || dblValueDate >=
			maturityDate().julian())
			return null;

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			if (period.payDate() < dblValueDate) continue;

			try {
				return new org.drip.analytics.date.JulianDate
					(((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod) (period.periods().get
						(0))).referenceIndexPeriod().fixingDate());
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate previousCouponDate (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		try {
			int iIndex = _stream.periodIndex (dt.julian());

			if (0 == iIndex) return null;
			
			org.drip.analytics.cashflow.CompositePeriod period = _stream.period (iIndex - 1);

			if (null == period) return null;

			return new org.drip.analytics.date.JulianDate (period.payDate());
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override public double previousCouponRate (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == dt || null == csqs)
			throw new java.lang.Exception ("BondComponent::previousCouponRate => Invalid Inputs");

		int iIndex = _stream.periodIndex (dt.julian());

		org.drip.analytics.cashflow.CompositePeriod period = _stream.period (iIndex - 1);

		if (null == period)
			throw new java.lang.Exception
				("BondComponent::previousCouponRate => Cannot find previous period!");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (period.endDate(), new
			org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

		if (null == pcm)
			throw new java.lang.Exception
				("BondComponent::previousCouponRate => Invalid previous period metrics!");

		return pcm.rate();
	}

	@Override public org.drip.analytics.date.JulianDate currentCouponDate (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		try {
			int iIndex = _stream.periodIndex (dt.julian());
			
			org.drip.analytics.cashflow.CompositePeriod period = _stream.period (iIndex);

			return null == period ? null : new org.drip.analytics.date.JulianDate (period.payDate());
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate nextCouponDate (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		try {
			int iIndex = _stream.periodIndex (dt.julian());
			
			org.drip.analytics.cashflow.CompositePeriod period = _stream.period (iIndex + 1);

			return null == period ? null : new org.drip.analytics.date.JulianDate (period.payDate());
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override public org.drip.analytics.output.ExerciseInfo nextValidExerciseDateOfType (
		final org.drip.analytics.date.JulianDate dt,
		final boolean bGetPut)
	{
		if (null == dt || (bGetPut && null == _eosPut) || (!bGetPut && null == _eosCall)) return null;

		org.drip.product.params.EmbeddedOptionSchedule eos = bGetPut ? _eosPut : _eosCall;

		double[] adblEOSExerciseDates = eos.dates();

		if (null == eos || null == adblEOSExerciseDates) return null;

		int iNumExerciseDates = adblEOSExerciseDates.length;

		if (0 == iNumExerciseDates) return null;

		for (int i = 0; i < iNumExerciseDates; ++i) {
			if (dt.julian() > adblEOSExerciseDates[i] + LEFT_EOS_SNIP || adblEOSExerciseDates[i] -
				dt.julian() < eos.exerciseNoticePeriod())
				continue;

			try {
				return new org.drip.analytics.output.ExerciseInfo (adblEOSExerciseDates[i], eos.factor (i),
					bGetPut ? org.drip.param.valuation.WorkoutInfo.WO_TYPE_PUT :
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_CALL);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	@Override public org.drip.analytics.output.ExerciseInfo nextValidExerciseInfo (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		org.drip.analytics.output.ExerciseInfo neiNextCall = nextValidExerciseDateOfType (dt, false);

		org.drip.analytics.output.ExerciseInfo neiNextPut = nextValidExerciseDateOfType (dt, true);

		if (null == neiNextCall && null == neiNextPut) {
			try {
				return new org.drip.analytics.output.ExerciseInfo (maturityDate().julian(), 1.,
					org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				return null;
			}
		}

		if (null != neiNextCall && null == neiNextPut) return neiNextCall;

		if (null == neiNextCall && null != neiNextPut) return neiNextPut;

		return neiNextCall.date() < neiNextPut.date() ? neiNextCall : neiNextPut;
	}

	@Override public double currentCouponRate (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == dt || null == csqs)
			throw new java.lang.Exception ("BondComponent::currentCouponRate => Null val/mkt params!");

		if (!org.drip.quant.common.NumberUtil.IsValid (_fltParams._dblCurrentCoupon))
			return _fltParams._dblCurrentCoupon;

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dt.julian(), new
			org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::currentCouponRate => Null PCM!");

		return pcm.rate();
	}

	@Override public double nextCouponRate (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == dt || null == csqs)
			throw new java.lang.Exception ("BondComponent::nextCouponRate => Null val/mkt params!");

		if (null == _fltParams) {
			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dt.julian(), new
				org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

			if (null == pcm)
				throw new java.lang.Exception ("BondComponent::nextCouponRate => Null PCM!");

			return pcm.rate();
		}

		int iIndex = _stream.periodIndex (dt.julian());

		org.drip.analytics.cashflow.CompositePeriod period = _stream.period (iIndex + 1);

		if (null == period)
			throw new java.lang.Exception ("BondComponent::nextCouponRate => Cannot find next period!");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (period.endDate(), new
			org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::nextCouponRate => Null PCM!");

		return pcm.rate();
	}

	@Override public double accrued (
		final double dblDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || null == csqs)
			throw new java.lang.Exception ("BondComponent::accrued => Invalid inputs");

		org.drip.analytics.date.JulianDate dt = new org.drip.analytics.date.JulianDate (dblDate);

		if (dblDate >= maturityDate().julian())
			throw new java.lang.Exception ("BondComponent::accrued => Val date " + dt +
				" greater than maturity " + maturityDate());

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			double dblEndDate = period.endDate();

			double dblStartDate = period.startDate();

			if (dblEndDate < dblDate) continue;

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblEndDate, new
				org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::accrued => No PCM");

			double dblCoupon = pcm.rate();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblCoupon)) return java.lang.Double.NaN;

			if (dblStartDate < dblDate && dblEndDate >= dblDate)
				return period.accrualDCF (dblDate) * dblCoupon * notional (dblStartDate);

			return 0.;
		}

		return 0.;
	}

	@Override public double priceFromBumpedZC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final int iZeroCurveBaseDC,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZCBump)
		throws java.lang.Exception
	{
		if (null == valParams)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => Invalid Inputs");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP || null == csqs ||
			!org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblZCBump))
			throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => Invalid Inputs");

		double dblPVFromZC = 0.;
		boolean bTerminateCouponFlow = false;
		org.drip.analytics.rates.ZeroCurve zc = null;
		double dblCashPayDate = java.lang.Double.NaN;
		double dblScalingNotional = java.lang.Double.NaN;
		org.drip.analytics.rates.DiscountCurve dcBase = null;

		if (ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE == iZeroCurveBaseDC)
			dcBase = csqs.fundingCurve (fundingLabel());
		else if (ZERO_OFF_OF_TREASURIES_DISCOUNT_CURVE == iZeroCurveBaseDC)
			dcBase = csqs.govvieCurve (org.drip.state.identifier.GovvieLabel.Standard (payCurrency()));

		if (null == dcBase)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => Invalid discount curve");

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		if (null != _notlParams && _notlParams._bPriceOffOriginalNotional) dblScalingNotional = 1.;

		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCompositePeriod = couponPeriods();

		try {
			zc = org.drip.state.creator.ZeroCurveBuilder.CreateZeroCurve (freq(), couponDC(), currency(),
				_stream.couponEOMAdjustment(), lsCompositePeriod, dblWorkoutDate, dblCashPayDate, dcBase,
					null == vcp ? (null == _mktConv ? null : _mktConv._quotingParams) : vcp, dblZCBump);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		if (null == zc)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => Cannot create shifted ZC");

		for (org.drip.analytics.cashflow.CompositePeriod period : lsCompositePeriod) {
			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			double dblPeriodStartDate = period.startDate();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblScalingNotional))
				dblScalingNotional = notional (dblPeriodStartDate);

			double dblAccrualEndDate = period.endDate();

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblValueDate,
				valParams, csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => No PCM");

			double dblZCDF = zc.df (dblPeriodPayDate);

			double dblCouponNotional = notional (dblPeriodStartDate);

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblPeriodStartDate, dblNotionalEndDate);

			dblPVFromZC += period.accrualDCF (dblAccrualEndDate) * dblZCDF * pcm.rate() * dblCouponNotional;

			dblPVFromZC += (notional (dblPeriodStartDate) - notional (dblNotionalEndDate)) * dblZCDF;

			if (bTerminateCouponFlow) break;
		}

		return ((dblPVFromZC + dblWorkoutFactor * zc.df (dblWorkoutDate) * notional (dblWorkoutDate)) /
			zc.df (dblCashPayDate) - accrued (dblValueDate, csqs)) / dblScalingNotional;
	}

	@Override public double priceFromBumpedDC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDCBump)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate)
			|| !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblDCBump))
			throw new java.lang.Exception ("BondComponent::priceFromBumpedDC => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedDC => No funding curve");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedDC => Val date " +
				org.drip.analytics.date.JulianDate.fromJulian (dblValueDate) + " greater than Work-out " +
					org.drip.analytics.date.JulianDate.fromJulian (dblWorkoutDate));

		double dblPVFromDC = 0.;
		boolean bTerminateCouponFlow = false;
		double dblCashPayDate = java.lang.Double.NaN;
		double dblScalingNotional = java.lang.Double.NaN;

		if (null != _notlParams && _notlParams._bPriceOffOriginalNotional) dblScalingNotional = 1.;

		if (0. != dblDCBump)
			dcFunding = (org.drip.analytics.rates.DiscountCurve) dcFunding.parallelShiftManifestMeasure
				("Rate", dblDCBump);

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedDC => Cannot shift funding curve");

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			double dblPeriodStartDate = period.startDate();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblScalingNotional))
				dblScalingNotional = notional (dblPeriodStartDate);

			double dblAccrualEndDate = period.endDate();

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblAccrualEndDate,
				valParams, csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromBumpedDC => No PCM");

			double dblPeriodAnnuity = dcFunding.df (dblPeriodPayDate) * pcm.cumulative();

			double dblCouponNotional = notional (dblPeriodStartDate);

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblPeriodStartDate, dblNotionalEndDate);

			dblPVFromDC += period.accrualDCF (dblAccrualEndDate) * dblPeriodAnnuity * pcm.rate() *
				dblCouponNotional;

			dblPVFromDC += (notional (dblPeriodStartDate) - notional (dblNotionalEndDate)) *
				dblPeriodAnnuity;

			if (bTerminateCouponFlow) break;
		}

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		return ((dblPVFromDC + dblWorkoutFactor * dcFunding.df (dblWorkoutDate) * notional (dblWorkoutDate))
			/ dcFunding.df (dblCashPayDate) - accrued (dblValueDate, csqs)) / dblScalingNotional;
	}

	@Override public double priceFromBumpedCC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis,
		final boolean bFlat)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate)
			|| !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblCreditBasis))
			throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => Invalid inputs");

		org.drip.analytics.definition.CreditCurve ccIn = csqs.creditCurve (creditLabel());

		if (null == ccIn)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => No funding curve");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => Val date " +
				org.drip.analytics.date.JulianDate.fromJulian (dblValueDate) + " greater than Work-out " +
					org.drip.analytics.date.JulianDate.fromJulian (dblWorkoutDate));

		org.drip.analytics.definition.CreditCurve cc = bFlat ? ccIn.flatCurve (dblCreditBasis, true, null !=
			_crValParams && !_crValParams._bUseCurveRec ? _crValParams._dblRecovery : java.lang.Double.NaN) :
				(org.drip.analytics.definition.CreditCurve) ccIn.parallelShiftManifestMeasure ("FairPremium",
					dblCreditBasis);

		if (null == cc)
			throw new java.lang.Exception
				("BondComponent::priceFromBumpedCC => Cannot create adjusted Curve");

		double dblPVFromCC = 0.;
		double dblScalingNotional = 1.;
		double dblCashPayDate = java.lang.Double.NaN;

		org.drip.param.pricer.PricerParams pricerParams = new org.drip.param.pricer.PricerParams (7, null,
			false, s_iDiscretizationScheme, false);

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate >= dblWorkoutDate) dblPeriodEndDate = dblWorkoutDate;

			double dblPeriodStartDate = period.startDate();

			if (dblPeriodStartDate < dblValueDate) dblPeriodStartDate = dblValueDate;

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblPeriodEndDate,
				valParams, csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => No PCM");

			double dblPeriodCoupon = pcm.rate();

			double dblPeriodEndSurv = cc.survival (dblPeriodEndDate);

			double dblCouponNotional = notional (dblPeriodStartDate);

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblPeriodEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblPeriodStartDate, dblPeriodEndDate);

			double dblPeriodAnnuity = dcFunding.df (dblPeriodPayDate) * pcm.cumulative();

			dblPVFromCC += period.accrualDCF (dblPeriodEndDate) * dblPeriodAnnuity * dblPeriodEndSurv *
				dblPeriodCoupon * dblCouponNotional;

			dblPVFromCC += (notional (dblPeriodStartDate) - notional (dblPeriodEndDate)) * dblPeriodAnnuity *
				dblPeriodEndSurv;

			for (org.drip.analytics.cashflow.LossQuadratureMetrics lp : period.lossMetrics (this, valParams,
				pricerParams, dblPeriodEndDate, csqs)) {
				if (null == lp) continue;

				double dblSubPeriodEndDate = lp.end();

				double dblSubPeriodStartDate = lp.start();

				double dblSubPeriodDF = dcFunding.effectiveDF (dblSubPeriodStartDate +
					_crValParams._iDefPayLag, dblSubPeriodEndDate + _crValParams._iDefPayLag);

				double dblSubPeriodNotional = notional (dblSubPeriodStartDate, dblSubPeriodEndDate);

				double dblSubPeriodSurvival = cc.survival (dblSubPeriodStartDate) - cc.survival
					(dblSubPeriodEndDate);

				if (_crValParams._bAccrOnDefault)
					dblPVFromCC += 0.0001 * lp.accrualDCF() * dblSubPeriodSurvival * dblSubPeriodDF *
						dblSubPeriodNotional * dblPeriodCoupon;

				dblPVFromCC += (_crValParams._bUseCurveRec ? cc.effectiveRecovery (dblSubPeriodStartDate,
					dblSubPeriodEndDate) : _crValParams._dblRecovery) * dblSubPeriodSurvival *
						dblSubPeriodNotional * dblSubPeriodDF;
			}
		}

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		if (!_notlParams._bPriceOffOriginalNotional) dblScalingNotional = notional (dblWorkoutDate);

		return ((dblPVFromCC + dblWorkoutFactor * dcFunding.df (dblWorkoutDate) * cc.survival
			(dblWorkoutDate) * notional (dblWorkoutDate)) / dcFunding.df (dblCashPayDate) - accrued
				(dblValueDate, csqs)) / dblScalingNotional;
	}

	@Override public double aswFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double aswFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return aswFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblBondBasis);
	}

	@Override public double aswFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromBondBasisToOptimalExercise => " +
				"Cannot calc ASW from Bond Basis to Optimal Exercise for bonds w emb option");

		return aswFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblBondBasis);
	}

	@Override public double aswFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double aswFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return aswFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblCreditBasis);
	}

	@Override public double aswFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromCreditBasisToOptimalExercise => " +
				"Cannot calc ASW from Credit Basis to Optimal Exercise for bonds w emb option");

		return aswFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblCreditBasis);
	}

	@Override public double aswFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromDiscountMargin
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double aswFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return aswFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1., dblDiscountMargin);
	}

	@Override public double aswFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromDiscountMarginToOptimalExercise => " +
				"Cannot calc ASW from Discount Margin to optimal exercise for bonds w emb option");

		return aswFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1., dblDiscountMargin);
	}

	@Override public double aswFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double aswFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return aswFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double aswFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromGSpreadToOptimalExercise => " +
				"Cannot calc ASW from G Spread to optimal exercise for bonds w emb option");

		return aswFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double aswFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double aswFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return aswFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double aswFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromISpreadToOptimalExercise => " +
				"Cannot calc ASW from I Spread to optimal exercise for bonds w emb option");

		return aswFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double aswFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS (valParams,
			csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double aswFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return aswFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double aswFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromOASToOptimalExercise => " +
				"Cannot calc ASW from OAS to optimal exercise for bonds w emb option");

		return aswFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double aswFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double aswFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return aswFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double aswFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromPECSToOptimalExercise => " +
				"Cannot calc ASW from PECS to optimal exercise for bonds w emb option");

		return aswFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double aswFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::aswFromPrice => Invalid Inputs");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::aswFromPrice => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::aswFromPrice => Invalid Inputs");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblValueDate, valParams,
			csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::aswFromPrice => No PCM");

		return pcm.rate() - dcFunding.estimateManifestMeasure ("Rate", dblWorkoutDate) + 0.0001 *
			(dblWorkoutFactor - dblPrice) / dcFunding.liborDV01 (dblWorkoutDate);
	}

	@Override public double aswFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double aswFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::aswFromPriceToOptimalExercise => Can't determine Work-out");

		return aswFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double aswFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double aswFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return aswFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double aswFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromTSYSpreadToOptimalExercise => " +
				"Cannot calc ASW from TSY Spread to optimal exercise for bonds w emb option");

		return aswFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double aswFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double aswFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return aswFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double aswFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromYieldToOptimalExercise => " +
				"Cannot calc ASW from Yield to optimal exercise for bonds w emb option");

		return aswFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double aswFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double aswFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return aswFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double aswFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromYieldSpreadToOptimalExercise => " +
				"Cannot calc ASW from Yield Spread to optimal exercise for bonds w emb option");

		return aswFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double aswFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double aswFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return aswFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double aswFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::aswFromZSpreadToOptimalExercise => " +
				"Cannot calc ASW from Yield Spread to optimal exercise for bonds w emb option");

		return aswFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double bondBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double bondBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return bondBasisFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double bondBasisFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromASWToOptimalExercise => " +
				"Cannot calc Bond Basis from ASW to optimal exercise for bonds w emb option");

		return bondBasisFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double bondBasisFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double bondBasisFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return bondBasisFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double bondBasisFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromCreditBasisToOptimalExercise => " +
				"Cannot calc Bond Basis from Credit Basis to optimal exercise for bonds w emb option");

		return bondBasisFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double bondBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double bondBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return bondBasisFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double bondBasisFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromDiscountMarginToOptimalExercise " +
				"=> Cant calc Bond Basis from Discount Margin to optimal exercise for bonds w emb option");

		return bondBasisFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double bondBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double bondBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return bondBasisFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double bondBasisFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromGSpreadToOptimalExercise => " +
				"Cant calc Bond Basis from G Spread to optimal exercise for bonds w emb option");

		return bondBasisFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double bondBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double bondBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return bondBasisFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double bondBasisFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromISpreadToOptimalExercise => " +
				"Cant calc Bond Basis from I Spread to optimal exercise for bonds w emb option");

		return bondBasisFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double bondBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double bondBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return bondBasisFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double bondBasisFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromOASToOptimalExercise => " +
				"Cant calc Bond Basis from OAS to optimal exercise for bonds w emb option");

		return bondBasisFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double bondBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double bondBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return bondBasisFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double bondBasisFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromPECSToOptimalExercise => " +
				"Cant calc Bond Basis from PECS to optimal exercise for bonds w emb option");

		return bondBasisFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double bondBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double bondBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return bondBasisFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double bondBasisFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);
		
		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::bondBasisFromPriceToOptimalExercise => cant calc Work-out info");

		return bondBasisFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double bondBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double bondBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return bondBasisFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double bondBasisFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromTSYSpreadToOptimalExercise => " +
				"Cant calc Bond Basis from TSY Spread to optimal exercise for bonds w emb option");

		return bondBasisFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double bondBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::bondBasisFromYield => Invalid inputs");

		return dblYield - yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.));
	}

	@Override public double bondBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double bondBasisFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromYieldToOptimalExercise => " +
				"Cant calc Bond Basis from Yield to optimal exercise for bonds w emb option");

		return bondBasisFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double bondBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double bondBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return bondBasisFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblYieldSpread);
	}

	@Override public double bondBasisFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromYieldSpreadToOptimalExercise " +
				"=> Cant calc Bond Basis from Yield Spread to optimal exercise for bonds w emb option");

		return bondBasisFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double bondBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double bondBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return bondBasisFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double bondBasisFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::bondBasisFromZSpreadToOptimalExercise => " +
				"Cant calc Bond Basis from Z Spread to optimal exercise for bonds w emb option");

		return bondBasisFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double convexityFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double convexityFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return convexityFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double convexityFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromASWToOptimalExercise => " +
				"Cant calc Convexity from ASW to optimal exercise for bonds w emb option");

		return convexityFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double convexityFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double convexityFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return convexityFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double convexityFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromBondBasisToOptimalExercise => " +
				"Cant calc Convexity from Bond Basis to optimal exercise for bonds w emb option");

		return convexityFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double convexityFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double convexityFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return convexityFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double convexityFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromCreditBasisToOptimalExercise => " +
				"Cant calc Convexity from Credit Basis to optimal exercise for bonds w emb option");

		return convexityFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double convexityFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double convexityFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return convexityFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double convexityFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromDiscountMarginToOptimalExercise " +
				"=> Cant calc Convexity from Discount Margin to optimal exercise for bonds w emb option");

		return convexityFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double convexityFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double convexityFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return convexityFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double convexityFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromGSpreadToOptimalExercise => " +
				"Cant calc Convexity from G Spread to optimal exercise for bonds w emb option");

		return convexityFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double convexityFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double convexityFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return convexityFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double convexityFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromISpreadToOptimalExercise => " +
				"Cant calc Convexity from I Spread to optimal exercise for bonds w emb option");

		return convexityFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double convexityFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double convexityFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return convexityFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double convexityFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromOASToOptimalExercise => " +
				"Cant calc Convexity from OAS to optimal exercise for bonds w emb option");

		return convexityFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double convexityFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double convexityFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return convexityFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double convexityFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromPECSToOptimalExercise => " +
				"Cant calc Convexity from PECS to optimal exercise for bonds w emb option");

		return convexityFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double convexityFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || !org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::convexityFromPrice => Input inputs");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::convexityFromPrice => Input inputs");

		double dblYield = yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice);

		return (priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield - 0.0001) +
			priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield + 0.0001) - 2. *
				dblPrice) / (dblPrice + accrued (dblValueDate, csqs));
	}

	@Override public double convexityFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double convexityFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromPriceToOptimalExercise => " +
				"Cant calc Convexity from Price to optimal exercise for bonds w emb option");

		return convexityFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double convexityFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double convexityFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return convexityFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double convexityFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromTSYSpreadToOptimalExercise => " +
				"Cant calc Convexity from TSY Sprd to optimal exercise for bonds w emb option");

		return convexityFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double convexityFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double convexityFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return convexityFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double convexityFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromYieldToOptimalExercise => " +
				"Cant calc Convexity from Yield to optimal exercise for bonds w emb option");

		return convexityFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double convexityFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double convexityFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return convexityFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double convexityFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromYieldSpreadToOptimalExercise => " +
				"Cant calc Convexity from Yld Sprd to optimal exercise for bonds w emb option");

		return convexityFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double convexityFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double convexityFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return convexityFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double convexityFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::convexityFromZSpreadToOptimalExercise => " +
				"Cant calc Convexity from Z Spread to optimal exercise for bonds w emb option");

		return convexityFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double creditBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double creditBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return creditBasisFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double creditBasisFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromASWToOptimalExercise => " +
				"Cannot calc Credit Basis from ASW to optimal exercise for bonds w emb option");

		return creditBasisFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double creditBasisFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double creditBasisFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return creditBasisFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double creditBasisFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromBondBasisToOptimalExercise " +
				"=> Cant calc Credit Basis from Bond Basis to optimal exercise for bonds w emb option");

		return creditBasisFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double creditBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double creditBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return creditBasisFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double creditBasisFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::creditBasisFromDiscountMarginToOptimalExercise => " +
					"Cant calc Credit Basis from Discnt Margin to optimal exercise for bonds w emb option");

		return creditBasisFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double creditBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double creditBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return creditBasisFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double creditBasisFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromGSpreadToOptimalExercise => " +
				"Cant calc Credit Basis from G Spread to optimal exercise for bonds w emb option");

		return creditBasisFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double creditBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double creditBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return creditBasisFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double creditBasisFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromISpreadToOptimalExercise => " +
				"Cant calc Credit Basis from I Spread to optimal exercise for bonds w emb option");

		return creditBasisFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double creditBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double creditBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return creditBasisFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double creditBasisFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromOASToOptimalExercise => " +
				"Cant calc Credit Basis from OAS to optimal exercise for bonds w emb option");

		return creditBasisFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double creditBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double creditBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return creditBasisFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double creditBasisFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromPECSToOptimalExercise => " +
				"Cant calc Credit Basis from PECS to optimal exercise for bonds w emb option");

		return creditBasisFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double creditBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateCreditBasisFromPrice (valParams, csqs, dblWorkoutDate,
			dblWorkoutFactor, dblPrice, false);
	}

	@Override public double creditBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double creditBasisFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::creditBasisFromPriceToOptimalExercise => cant calc Work-out");

		return creditBasisFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double creditBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double creditBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return creditBasisFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double creditBasisFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromTSYSpreadToOptimalExercise => " +
				"Cant calc Credit Basis from TSY Spread to optimal exercise for bonds w emb option");

		return creditBasisFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double creditBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double creditBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return creditBasisFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double creditBasisFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromYieldToOptimalExercise => " +
				"Cant calc Credit Basis from Yield to optimal exercise for bonds w emb option");

		return creditBasisFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double creditBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double creditBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return creditBasisFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double creditBasisFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws	java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromYieldSpreadToOptimalExercise " +
				"=> Cant calc Credit Basis from Yield Spread to optimal exercise for bonds w emb option");

		return creditBasisFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double creditBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double creditBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return creditBasisFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double creditBasisFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::creditBasisFromZSpreadToOptimalExercise => " +
				"Cant calc Credit Basis from Z Spread to optimal exercise for bonds w emb option");

		return creditBasisFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double discountMarginFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double discountMarginFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return discountMarginFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double discountMarginFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromASWToOptimalExercise => " +
				"Cant calc Discount Margin from ASW to optimal exercise for bonds w emb option");

		return discountMarginFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double discountMarginFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double discountMarginFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return discountMarginFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double discountMarginFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromBondBasisToOptimalExercise " +
				"=> Cant calc Discount Margin from Bond Basis to optimal exercise for bonds w emb option");

		return discountMarginFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double discountMarginFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double discountMarginFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return discountMarginFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double discountMarginFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::discountMarginFromCreditBasisToOptimalExercise => " +
					"Cant calc Discount Margin from Crdit Basis to optimal exercise for bonds w emb option");

		return discountMarginFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double discountMarginFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromGSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double discountMarginFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return discountMarginFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double discountMarginFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromGSpreadToOptimalExercise =>" +
				" => Cant calc Discount Margin from G Spread to optimal exercise for bonds w emb option");

		return discountMarginFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double discountMarginFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double discountMarginFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return discountMarginFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double discountMarginFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromISpreadToOptimalExercise " +
				"=> Cant calc Discount Margin from I Spread to optimal exercise for bonds w emb option");

		return discountMarginFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double discountMarginFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double discountMarginFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return discountMarginFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double discountMarginFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromOASToOptimalExercise => " +
				"Cant calc Discount Margin from OAS to optimal exercise for bonds w emb option");

		return discountMarginFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double discountMarginFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double discountMarginFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return discountMarginFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double discountMarginFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromPECSToOptimalExercise => " +
				"Cant calc Discount Margin from PECS to optimal exercise for bonds w emb option");

		return discountMarginFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double discountMarginFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double discountMarginFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return discountMarginFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPrice);
	}

	@Override public double discountMarginFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::discountMarginFromPriceToOptimalExercise => Can't do Work-out");

		return discountMarginFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double discountMarginFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double discountMarginFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return discountMarginFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double discountMarginFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromTSYSpreadToOptimalExercise " +
				"=> Cant calc Discount Margin from TSY Spread to optimal exercise for bonds w emb option");

		return discountMarginFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double discountMarginFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::discountMarginFromYield => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::discountMarginFromYield => Invalid inputs");

		double dblValueDate = valParams.valueDate();

		int iFreq = freq();

		return null == _fltParams ? dblYield - dcFunding.libor (dblValueDate, ((int) (12. / (0 == iFreq ? 2 :
			iFreq))) + "M") : dblYield - indexRate (dblValueDate, csqs,
				(org.drip.analytics.cashflow.CompositeFloatingPeriod) currentPeriod (dblValueDate));
	}

	@Override public double discountMarginFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double discountMarginFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromYieldToOptimalExercise =>" +
				" Cant calc Discount Margin from Yield to optimal exercise for bonds w emb option");

		return discountMarginFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double discountMarginFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double discountMarginFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return discountMarginFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double discountMarginFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::discountMarginFromYieldSpreadToOptimalExercise => " +
					"Cant calc Discount Margin from Yield Sprd to optimal exercise for bonds w emb option");

		return discountMarginFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double discountMarginFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromZSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double discountMarginFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return discountMarginFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double discountMarginFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::discountMarginFromZSpreadToOptimalExercise =>" +
				" Cant calc Discount Margin from Z Spread to optimal exercise for bonds w emb option");

		return discountMarginFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double durationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double durationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return durationFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double durationFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromASWToOptimalExercise => " +
				"Cant calc Duration from ASW to optimal exercise for bonds w emb option");

		return durationFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double durationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double durationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return durationFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double durationFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromBondBasisToOptimalExercise => " +
				"Cant calc Duration from Bond Basis to optimal exercise for bonds w emb option");

		return durationFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double durationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double durationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return durationFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double durationFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromCreditBasisToOptimalExercise => " +
				"Cant calc Duration from Credit Basis to optimal exercise for bonds w emb option");

		return durationFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double durationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double durationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return durationFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double durationFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromDiscountMarginToOptimalExercise " +
				"=> Cant calc Duration from Discount Margin to optimal exercise for bonds w emb option");

		return durationFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double durationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double durationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return durationFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double durationFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromGSpreadToOptimalExercise => " +
				"Cant calc Duration from G Spread to optimal exercise for bonds w emb option");

		return durationFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double durationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double durationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return durationFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double durationFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromISpreadToOptimalExercise => " +
				"Cant calc Duration from I Spread to optimal exercise for bonds w emb option");

		return durationFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double durationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double durationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return durationFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double durationFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromOASToOptimalExercise => " +
				"Cant calc Duration from OAS to optimal exercise for bonds w emb option");

		return durationFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double durationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double durationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return durationFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double durationFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromPECSToOptimalExercise => " +
				"Cant calc Duration from PECS to optimal exercise for bonds w emb option");

		return durationFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double durationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice);
	}

	@Override public double durationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double durationFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromPriceToOptimalExercise => " +
				"Cant calc Duration from Price to optimal exercise for bonds w emb option");

		return durationFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double durationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double durationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return durationFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double durationFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromTSYSpreadToOptimalExercise => " +
				"Cant calc Duration from TSY Sprd to optimal exercise for bonds w emb option");

		return durationFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double durationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double durationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return durationFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double durationFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromYieldToOptimalExercise => " +
				"Cant calc Duration from Yield to optimal exercise for bonds w emb option");

		return durationFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double durationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double durationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return durationFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double durationFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromYieldSpreadToOptimalExercise => " +
				"Cant calc Duration from Yield Spread to optimal exercise for bonds w emb option");

		return durationFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double durationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return durationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double durationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return durationFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double durationFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::durationFromZSpreadToOptimalExercise => " +
				"Cant calc Duration from Z Spread to optimal exercise for bonds w emb option");

		return durationFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double gSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double gSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return gSpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double gSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromASWToOptimalExercise => " +
				"Cant calc G Spread from ASW to optimal exercise for bonds w emb option");

		return gSpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double gSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double gSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return gSpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double gSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromBondBasisToOptimalExercise => " +
				"Cant calc G Spread from Bond Basis to optimal exercise for bonds w emb option");

		return gSpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double gSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double gSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return gSpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double gSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromCreditBasisToOptimalExercise => " +
				"Cant calc G Spread from Credit Basis to optimal exercise for bonds w emb option");

		return gSpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double gSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double gSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return gSpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double gSSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSSpreadFromDiscountMarginToOptimalExercise =>" +
				" Cant calc G Spread from Discount Margin to optimal exercise for bonds w emb option");

		return gSpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double gSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double gSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return gSpreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double gSpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromISpreadToOptimalExercise => " +
				"Cant calc G Spread from I Spread to optimal exercise for bonds w emb option");

		return gSpreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double gSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double gSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return gSpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double gSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromOASToOptimalExercise => " +
				"Cant calc G Spread from OAS to optimal exercise for bonds w emb option");

		return gSpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double gSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double gSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return gSpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double gSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromPECSToOptimalExercise => " +
				"Cant calc G Spread from PECS to optimal exercise for bonds w emb option");

		return gSpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double gSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double gSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return gSpreadFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double gSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::gSpreadFromPriceToOptimalExercise => Can't do Work-out");

		return gSpreadFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double gSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double gSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return gSpreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double gSpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc G Spread from TSY Spread to optimal exercise for bonds w emb option");

		return gSpreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double gSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblYield) || valParams.valueDate() >=
					dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::gSpreadFromYield => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcTSY = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()));

		if (null == dcTSY)
			throw new java.lang.Exception ("BondComponent::gSpreadFromYield => Invalid inputs");

		return dblYield - dcTSY.estimateManifestMeasure ("Yield", dblWorkoutDate);
	}

	@Override public double gSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double gSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromYieldToOptimalExercise => " +
				"Cant calc G Spread from Yield to optimal exercise for bonds w emb option");

		return gSpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double gSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double gSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return gSpreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double gSpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc G Spread from Yield Spread to optimal exercise for bonds w emb option");

		return gSpreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double gSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double gSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return gSpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double gSpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::gSpreadFromZSpreadToOptimalExercise => " +
				"Cant calc G Spread from Z Spread to optimal exercise for bonds w emb option");

		return gSpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double iSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double iSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return iSpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double iSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromASWToOptimalExercise => " +
				"Cant calc I Spread from ASW to optimal exercise for bonds w emb option");

		return iSpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double iSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double iSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return iSpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double iSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromBondBasisToOptimalExercise => " +
				"Cant calc I Spread from Bond Basis to optimal exercise for bonds w emb option");

		return iSpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double iSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double iSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return iSpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double iSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromCreditBasisToOptimalExercise => " +
				"Cant calc I Spread from Credit Basis to optimal exercise for bonds w emb option");

		return iSpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double iSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double iSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return iSpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double iSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromDiscountMarginToOptimalExercise =>" +
				" Cant calc I Spread from Discount Margin to optimal exercise for bonds w emb option");

		return iSpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double iSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double iSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return iSpreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double iSpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromGSpreadToOptimalExercise => " +
				"Cant calc I Spread from G Spread to optimal exercise for bonds w emb option");

		return iSpreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double iSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double iSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return iSpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double iSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromOASToOptimalExercise => " +
				"Cant calc I Spread from OAS to optimal exercise for bonds w emb option");

		return iSpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double iSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double iSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return iSpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double iSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromPECSToOptimalExercise => " +
				"Cant calc I Spread from PECS to optimal exercise for bonds w emb option");

		return iSpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double iSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double iSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return iSpreadFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double iSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::iSpreadFromPriceToOptimalExercise => Can't do Work-out");

		return iSpreadFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double iSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double iSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return iSpreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double iSpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc I Spread from TSY Spread to optimal exercise for bonds w emb option");

		return iSpreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double iSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::iSpreadFromYield => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::iSpreadFromYield => Invalid inputs");

		return dblYield - dcFunding.estimateManifestMeasure ("Rate", dblWorkoutDate);
	}

	@Override public double iSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double iSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromYieldToOptimalExercise => " +
				"Cant calc I Spread from Yield to optimal exercise for bonds w emb option");

		return iSpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double iSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double iSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return iSpreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double iSpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc I Spread from Yield Spread to optimal exercise for bonds w emb option");

		return iSpreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double iSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double iSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return iSpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double iSpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::iSpreadFromZSpreadToOptimalExercise => " +
				"Cant calc I Spread from Z Spread to optimal exercise for bonds w emb option");

		return iSpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double macaulayDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromASW (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double macaulayDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return macaulayDurationFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double macaulayDurationFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromASWToOptimalExercise => " +
					"Cant calc Macaulay Duration from ASW to optimal exercise for bonds w emb option");

		return macaulayDurationFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double macaulayDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double macaulayDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return macaulayDurationFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double macaulayDurationFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromBondBasisToOptimalExercise => " +
					"Cant calc Macaulay Duration from Bnd Basis to optimal exercise for bonds w emb option");

		return macaulayDurationFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double macaulayDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double macaulayDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return macaulayDurationFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double macaulayDurationFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromCreditBasisToOptimalExercise => " +
					"Cant calc Macaulay Duration from Crd Basis to optimal exercise for bonds w emb option");

		return macaulayDurationFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double macaulayDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double macaulayDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return macaulayDurationFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(),
			1., dblDiscountMargin);
	}

	@Override public double macaulayDurationFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromDiscountMarginToOptimalExercise => " +
					"Cant calc Macaulay Duration from Disc Marg to optimal exercise for bonds w emb option");

		return macaulayDurationFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(),
			1., dblDiscountMargin);
	}

	@Override public double macaulayDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromGSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double macaulayDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double macaulayDurationFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromGSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from G Spread to optimal exercise for bonds w emb option");

		return macaulayDurationFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double macaulayDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double macaulayDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double macaulayDurationFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromISpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from I Spread to optimal exercise for bonds w emb option");

		return macaulayDurationFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double macaulayDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromOAS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double macaulayDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return macaulayDurationFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double mnacaulayDurationFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::mnacaulayDurationFromOASToOptimalExercise => " +
					"Cant calc Macaulay Duration from OAS to optimal exercise for bonds w emb option");

		return macaulayDurationFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double macaulayDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromPECS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double macaulayDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return macaulayDurationFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPECS);
	}

	@Override public double macaulayDurationFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromPECSToOptimalExercise => " +
					"Cant calc Macaulay Duration from PECS to optimal exercise for bonds w emb option");

		return macaulayDurationFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPECS);
	}

	@Override public double macaulayDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double macaulayDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return macaulayDurationFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPrice);
	}

	@Override public double macaulayDurationFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromPriceToOptimalExercise => Cant determine Work-out");

		return macaulayDurationFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double macaulayDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double macaulayDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double macaulayDurationFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromTSYSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from TSY Sprd to optimal exercise for bonds w emb option");

		return macaulayDurationFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double macaulayDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor))
			throw new java.lang.Exception ("BondComponent::macaulayDurationFromYield => Invalid inputs");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::macaulayDurationFromYield => Invalid inputs");

		double dblYieldPV = 0.;
		double dblCFPeriod = 0.;
		double dblDuration = 0.;
		boolean bFirstPeriod = true;
		boolean bTerminateCouponFlow = false;
		org.drip.analytics.daycount.ActActDCParams aap = null;
		org.drip.analytics.cashflow.CompositePeriod periodRef = null;

		int iFrequency = freq();

		java.lang.String strDC = couponDC();

		boolean bApplyCpnEOMAdj = _stream.couponEOMAdjustment();

		java.lang.String strCalendar = currency();

		if (null == strCalendar || strCalendar.isEmpty()) strCalendar = redemptionCurrency();

		if (null != vcp) {
			strDC = vcp.yieldDayCount();

			iFrequency = vcp.yieldFreq();

			bApplyCpnEOMAdj = vcp.applyYieldEOMAdj();

			strCalendar = vcp.yieldCalendar();
		} else if (null != _mktConv && null != _mktConv._quotingParams) {
			strDC = _mktConv._quotingParams.yieldDayCount();

			iFrequency = _mktConv._quotingParams.yieldFreq();

			bApplyCpnEOMAdj = _mktConv._quotingParams.applyYieldEOMAdj();

			strCalendar = _mktConv._quotingParams.yieldCalendar();
		}

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			if (bFirstPeriod) {
				bFirstPeriod = false;

				dblCFPeriod = period.couponDCF() - period.accrualDCF (dblValueDate);
			} else
				dblCFPeriod += period.couponDCF();

			periodRef = period;

			double dblPeriodEndDate = period.endDate();

			double dblPeriodStartDate = period.startDate();

			if (dblPeriodEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblPeriodEndDate = dblWorkoutDate;
			}

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblValueDate,
				valParams, csqs);

			if (null == pcm)
				throw new java.lang.Exception ("BondComponent::calcMacaulayDurationFromYield => No PCM");

			aap = new org.drip.analytics.daycount.ActActDCParams (iFrequency, dblPeriodStartDate,
				dblPeriodEndDate);

			if (null != vcp) {
				if (null == (aap = vcp.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams (vcp.yieldFreq(),
						dblPeriodStartDate, dblPeriodEndDate);
			} else if (null != _mktConv && null != _mktConv._quotingParams) {
				if (null == (aap = _mktConv._quotingParams.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams
						(_mktConv._quotingParams.yieldFreq(), dblPeriodStartDate, dblPeriodEndDate);
			}

			double dblYearFract = org.drip.analytics.daycount.Convention.YearFraction (dblValueDate,
				dblPeriodPayDate, strDC, bApplyCpnEOMAdj, aap, strCalendar);

			double dblYieldAnnuity = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency,
				dblYield, s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod : dblYearFract) * pcm.cumulative();

			double dblCouponNotional = notional (dblPeriodStartDate);

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblPeriodEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblPeriodStartDate, dblPeriodEndDate);

			double dblCouponPV = period.accrualDCF (dblPeriodEndDate) * pcm.rate() * dblYieldAnnuity *
				dblCouponNotional;

			double dblPeriodNotionalPV = (notional (dblPeriodStartDate) - notional (dblPeriodEndDate)) *
				dblYieldAnnuity;

			dblYieldPV += (dblCouponPV + dblPeriodNotionalPV);
			dblDuration += dblCFPeriod * (dblCouponPV + dblPeriodNotionalPV);

			if (bTerminateCouponFlow) break;
		}

		if (null != periodRef)
			aap = new org.drip.analytics.daycount.ActActDCParams (iFrequency, periodRef.startDate(),
				periodRef.endDate());

		if (null != vcp) {
			if (null != periodRef)
				aap = new org.drip.analytics.daycount.ActActDCParams (vcp.yieldFreq(),
					periodRef.startDate(), periodRef.endDate());
		} else if (null != _mktConv && null != _mktConv._quotingParams) {
			if (null != periodRef)
				aap = new org.drip.analytics.daycount.ActActDCParams (_mktConv._quotingParams.yieldFreq(),
					periodRef.startDate(), periodRef.endDate());
		}

		double dblRedemptionPV = dblWorkoutFactor * org.drip.analytics.support.AnalyticsHelper.Yield2DF
			(iFrequency, dblYield, s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod :
				org.drip.analytics.daycount.Convention.YearFraction (dblValueDate, dblWorkoutDate, strDC,
					bApplyCpnEOMAdj, aap, strCalendar)) * notional (dblWorkoutDate);

		return (dblDuration + dblCFPeriod * dblRedemptionPV) / (dblYieldPV + dblRedemptionPV);
	}

	@Override public double macaulayDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYield);
	}

	@Override public double macaulayDurationFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::macaulayDurationFromYieldToOptimalExercise =>" +
				" Cant calc Macaulay Duration from Yield to optimal exercise for bonds w emb option");

		return macaulayDurationFromYield (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYield);
	}

	@Override public double macaulayDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double macaulayDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double macaulayDurationFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromYieldSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from Yld Sprd to optimal exercise for bonds w emb option");

		return macaulayDurationFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double macaulayDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromZSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double macaulayDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return macaulayDurationFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double macaulayDurationFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::macaulayDurationFromZSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from Z Spread to optimal exercise for bonds w emb option");

		return macaulayDurationFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double modifiedDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromASW (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double modifiedDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return modifiedDurationFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double modifiedDurationFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromASWToOptimalExercise => " +
					"Cant calc Modified Duration from ASW to optimal exercise for bonds w emb option");

		return modifiedDurationFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double modifiedDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double modifiedDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return modifiedDurationFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double modifiedDurationFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromBondBasisToOptimalExercise => " +
					"Cant calc Modified Duration from Bnd Basis to optimal exercise for bonds w emb option");

		return modifiedDurationFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double modifiedDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double modifiedDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return modifiedDurationFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double modifiedDurationFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromCreditBasisToOptimalExercise => " +
					"Cant calc Modified Duration from Crd Basis to optimal exercise for bonds w emb option");

		return modifiedDurationFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double modifiedDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double modifiedDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return modifiedDurationFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(),
			1., dblDiscountMargin);
	}

	@Override public double modifiedDurationFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromDiscountMarginToOptimalExercise => " +
					"Cant calc Modified Duration from Disc Marg to optimal exercise for bonds w emb option");

		return modifiedDurationFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(),
			1., dblDiscountMargin);
	}

	@Override public double modifiedDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromGSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double modifiedDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double modifiedDurationFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromGSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from G Spread to optimal exercise for bonds w emb option");

		return modifiedDurationFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double modifiedDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double modifiedDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double modifiedDurationFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromISpreadToOptimalExercise => " +
					"Cant calc Modified Duration from I Spread to optimal exercise for bonds w emb option");

		return modifiedDurationFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double modifiedDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromOAS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double modifiedDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return modifiedDurationFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double modifiedDurationFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromOASToOptimalExercise => " +
					"Cant calc Modified Duration from OAS to optimal exercise for bonds w emb option");

		return modifiedDurationFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double modifiedDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double modifiedDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return modifiedDurationFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPECS);
	}

	@Override public double modifiedDurationFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromPECSToOptimalExercise => " +
					"Cant calc Modified Duration from PECS to optimal exercise for bonds w emb option");

		return modifiedDurationFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPECS);
	}

	@Override public double modifiedDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP ||
			!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::modifiedDurationFromPrice => Input inputs");

		return (dblPrice - priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice) + 0.0001)) /
				(dblPrice + accrued (valParams.valueDate(), csqs));
	}

	@Override public double modifiedDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblPrice);
	}

	@Override public double modifiedDurationFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromPriceToOptimalExercise => Cant determine Work-out");

		return modifiedDurationFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double modifiedDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double modifiedDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double modifiedDurationFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromTSYSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from TSY Sprd to optimal exercise for bonds w emb option");

		return modifiedDurationFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double modifiedDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double modifiedDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return modifiedDurationFromYield (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYield);
	}

	@Override public double modifiedDurationFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::modifiedDurationFromYieldToOptimalExercise =>" +
				" Cant calc Modified Duration from Yield to optimal exercise for bonds w emb option");

		return modifiedDurationFromYield (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYield);
	}

	@Override public double modifiedDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double modifiedDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double modifiedDurationFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromYieldSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from Yld Sprd to optimal exercise for bonds w emb option");

		return modifiedDurationFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double modifiedDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromZSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double modifiedDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return modifiedDurationFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double modifiedDurationFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::modifiedDurationFromZSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from Z Spread to optimal exercise for bonds w emb option");

		return modifiedDurationFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double oasFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW (valParams,
			csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double oasFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return oasFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double oasFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromASWToOptimalExercise => " +
				"Cant calc OAS from ASW to optimal exercise for bonds w emb option");

		return oasFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double oasFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double oasFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return oasFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblBondBasis);
	}

	@Override public double oasFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromBondBasisToOptimalExercise => " +
				"Cant calc OAS from Bnd Basis to optimal exercise for bonds w emb option");

		return oasFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblBondBasis);
	}

	@Override public double oasFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double oasFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return oasFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double oasFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromCreditBasisToOptimalExercise => " +
				"Cant calc OAS from Credit Basis to optimal exercise for bonds w emb option");

		return oasFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double oasFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromDiscountMargin
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double oasFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return oasFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double oasFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromDiscountMarginToOptimalExercise => " +
				"Cant calc OAS from Discount Margin to optimal exercise for bonds w emb option");

		return oasFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double oasFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double oasFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return oasFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double oasFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromGSpreadToOptimalExercise => " +
				"Cant calc OAS from G Spread to optimal exercise for bonds w emb option");

		return oasFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double oasFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double oasFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return oasFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double oasFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromISpreadToOptimalExercise => " +
				"Cant calc OAS from I Spread to optimal exercise for bonds w emb option");

		return oasFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double oasFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double oasFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return oasFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double oasFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromPECSToOptimalExercise => " +
				"Cant calc OAS from PECS to optimal exercise for bonds w emb option");

		return oasFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double oasFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP ||
			!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::oasFromPrice => Input inputs");

		return new BondCalibrator (this).calibrateZSpreadFromPrice (valParams, csqs,
			ZERO_OFF_OF_TREASURIES_DISCOUNT_CURVE, dblWorkoutDate, dblWorkoutFactor, dblPrice);
	}

	@Override public double oasFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double oasFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::oasFromPriceToOptimalExercise - cant calc Work-out");

		return oasFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double oasFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double oasFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return oasFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double oasFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromTSYSpreadToOptimalExercise => " +
				"Cant calc OAS from TSY Sprd to optimal exercise for bonds w emb option");

		return oasFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double oasFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double oasFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return oasFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double oasFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromYieldToOptimalExercise => " +
				"Cant calc OAS from Yield to optimal exercise for bonds w emb option");

		return oasFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double oasFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double oasFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return oasFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double oasFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromYieldSpreadToOptimalExercise => " +
				"Cant calc OAS from Yield Sprd to optimal exercise for bonds w emb option");

		return oasFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double oasFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double oasFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return oasFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double oasFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::oasFromZSpreadToOptimalExercise => " +
				"Cant calc OAS from Z Spread to optimal exercise for bonds w emb option");

		return oasFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double pecsFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double pecsFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return pecsFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double pecsFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromASWToOptimalExercise => " +
				"Cant calc PECS from ASW to optimal exercise for bonds w emb option");

		return pecsFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double pecsFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double pecsFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return pecsFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblBondBasis);
	}

	@Override public double pecsFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromBondBasisToOptimalExercise => " +
				"Cant calc PECS from Bond Basis to optimal exercise for bonds w emb option");

		return pecsFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1., dblBondBasis);
	}

	@Override public double pecsFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double pecsFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return pecsFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double pecsFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromCreditBasisToOptimalExercise => " +
				"Cant calc PECS from Credit Basis to optimal exercise for bonds w emb option");

		return pecsFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double pecsFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromDiscountMargin
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double pecsFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return pecsFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double pecsFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromDiscountMarginToOptimalExercise => " +
				"Cant calc PECS from Discount Margin to optimal exercise for bonds w emb option");

		return pecsFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double pecsFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double pecsFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return pecsFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double pecsFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromGSpreadToOptimalExercise => " +
				"Cant calc PECS from G Spread to optimal exercise for bonds w emb option");

		return pecsFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double pecsFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double pecsFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return pecsFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double pecsFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromISpreadToOptimalExercise => " +
				"Cant calc PECS from I Spread to optimal exercise for bonds w emb option");

		return pecsFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double pecsFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double pecsFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return pecsFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double pecsFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromOASToOptimalExercise => " +
				"Cant calc PECS from OAS to optimal exercise for bonds w emb option");

		return pecsFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double pecsFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateCreditBasisFromPrice (valParams, csqs, dblWorkoutDate,
			dblWorkoutFactor, dblPrice, true);
	}

	@Override public double pecsFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double pecsFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::pecsFromPriceToOptimalExercise => Cant determine Work-out");

		return pecsFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double pecsFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double pecsFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return pecsFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double pecsFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromTSYSpreadToOptimalExercise => " +
				"Cant calc PECS from TSY Spread to optimal exercise for bonds w emb option");

		return pecsFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double pecsFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double pecsFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return pecsFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double pecsFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromYieldToOptimalExercise => " +
				"Cant calc PECS from Yield to optimal exercise for bonds w emb option");

		return pecsFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double pecsFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double pecsFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return pecsFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double pecsFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromYieldSpreadToOptimalExercise => " +
				"Cant calc PECS from Yield Spread to optimal exercise for bonds w emb option");

		return pecsFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double pecsFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double pecsFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return pecsFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double pecsFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::pecsFromZSpreadToOptimalExercise => " +
				"Cant calc PECS from Z Spread to optimal exercise for bonds w emb option");

		return pecsFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double priceFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblASW))
			throw new java.lang.Exception ("BondComponent::priceFromASW => Invalid Inputs");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::priceFromASW => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::priceFromASW => Invalid Inputs");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblValueDate, valParams,
			csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromASW => No PCM");

		return dblWorkoutFactor - 100. * dcFunding.liborDV01 (dblWorkoutDate) * (dblASW +
			dcFunding.estimateManifestMeasure ("Rate", dblWorkoutDate) - pcm.rate());
	}

	@Override public double priceFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return priceFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double priceFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromASWToOptimalExercise => " +
				"Cant calc Price from ASW to optimal exercise for bonds w emb option");

		return priceFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double priceFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double priceFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return priceFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double priceFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromBondBasisToOptimalExercise => " +
				"Cant calc Price from Bond Basis to optimal exercise for bonds w emb option");

		return priceFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double priceFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return priceFromBumpedCC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis, false);
	}

	@Override public double priceFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return priceFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double priceFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromCreditBasisToOptimalExercise => " +
				"Cant calc Price from Credit Basis to optimal exercise for bonds w emb option");

		return priceFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double priceFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double priceFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		double dblDiscountMargin)
		throws java.lang.Exception
	{
		return priceFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double priceFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromDiscountMarginToOptimalExercise => " +
				"Cant calc Price from Discount Margin to optimal exercise for bonds w emb option");

		return priceFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double priceFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double priceFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return priceFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double priceFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromGSpreadToOptimalExercise => " +
				"Cant calc Price from G Spread to optimal exercise for bonds w emb option");

		return priceFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double priceFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double priceFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return priceFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double priceFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromISpreadToOptimalExercise => " +
				"Cant calc Price from I Spread to optimal exercise for bonds w emb option");

		return priceFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double priceFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, dblOAS);
	}

	@Override public double priceFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return priceFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double priceFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromOASToOptimalExercise => " +
				"Cant calc Price from OAS to optimal exercise for bonds w emb option");

		return priceFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double priceFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return priceFromBumpedCC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, dblPECS, true);
	}

	@Override public double priceFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return priceFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double priceFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromPECSToOptimalExercise => " +
				"Cant calc Price from PECS to optimal exercise for bonds w emb option");

		return priceFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double priceFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double priceFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return priceFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double priceFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromTSYSpreadToOptimalExercise => " +
				"Cant calc Price from TSY Spread to optimal exercise for bonds w emb option");

		return priceFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double priceFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor))
			throw new java.lang.Exception ("BondComponent::priceFromYield => Invalid inputs");

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::priceFromYield => Invalid inputs");

		double dblYieldPV = 0.;
		double dblCFPeriod = 0.;
		boolean bFirstPeriod = true;
		boolean bTerminateCouponFlow = false;
		double dblCashPayDate = java.lang.Double.NaN;
		double dblScalingNotional = java.lang.Double.NaN;
		org.drip.analytics.daycount.ActActDCParams aap = null;

		if (null != _notlParams && _notlParams._bPriceOffOriginalNotional) dblScalingNotional = 1.;

		int iFrequency = freq();

		java.lang.String strDC = couponDC();

		boolean bApplyCpnEOMAdj = _stream.couponEOMAdjustment();

		java.lang.String strCalendar = currency();

		if (null == strCalendar || strCalendar.isEmpty()) strCalendar = redemptionCurrency();

		if (null != vcp) {
			strDC = vcp.yieldDayCount();

			iFrequency = vcp.yieldFreq();

			bApplyCpnEOMAdj = vcp.applyYieldEOMAdj();

			strCalendar = vcp.yieldCalendar();
		} else if (null != _mktConv && null != _mktConv._quotingParams) {
			strDC = _mktConv._quotingParams.yieldDayCount();

			iFrequency = _mktConv._quotingParams.yieldFreq();

			bApplyCpnEOMAdj = _mktConv._quotingParams.applyYieldEOMAdj();

			strCalendar = _mktConv._quotingParams.yieldCalendar();
		}

		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods()) {
			double dblPayDate = period.payDate();

			if (dblPayDate < dblValueDate) continue;

			if (bFirstPeriod) {
				bFirstPeriod = false;

				dblCFPeriod = period.couponDCF() - period.accrualDCF (dblValueDate);
			} else
				dblCFPeriod += period.couponDCF();

			double dblEndDate = period.endDate();

			double dblStartDate = period.startDate();

			if (dblEndDate >= dblWorkoutDate) {
				dblEndDate = dblWorkoutDate;
				bTerminateCouponFlow = true;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblScalingNotional))
				dblScalingNotional = notional (dblStartDate);

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = couponMetrics (dblValueDate,
				valParams, csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromYield => No PCM");

			double dblPeriodCoupon = pcm.rate();

			aap = new org.drip.analytics.daycount.ActActDCParams (iFrequency, dblStartDate, dblEndDate);

			if (null != vcp) {
				if (null == (aap = vcp.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams (vcp.yieldFreq(), dblStartDate,
						dblEndDate);
			} else if (null != _mktConv && null != _mktConv._quotingParams) {
				if (null == (aap = _mktConv._quotingParams.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams
						(_mktConv._quotingParams.yieldFreq(), dblStartDate, dblEndDate);
			}

			double dblYieldAnnuity = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency,
				dblYield, s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod :
					org.drip.analytics.daycount.Convention.YearFraction (dblValueDate, dblPayDate, strDC,
						bApplyCpnEOMAdj, aap, strCalendar)) * pcm.cumulative();

			double dblCouponNotional = notional (dblStartDate);

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblStartDate, dblEndDate);

			dblYieldPV += (period.accrualDCF (dblEndDate) * dblPeriodCoupon * dblCouponNotional + notional
				(dblStartDate) - notional (dblEndDate)) * dblYieldAnnuity;

			if (bTerminateCouponFlow) break;
		}

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		return (((dblYieldPV + dblWorkoutFactor * org.drip.analytics.support.AnalyticsHelper.Yield2DF
			(iFrequency, dblYield, s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod :
				org.drip.analytics.daycount.Convention.YearFraction (dblValueDate, dblWorkoutDate, strDC,
					bApplyCpnEOMAdj, aap, strCalendar)) * notional (dblWorkoutDate)) /
						org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency, dblYield,
							org.drip.analytics.daycount.Convention.YearFraction (dblValueDate,
								dblCashPayDate, strDC, bApplyCpnEOMAdj, aap, strCalendar))) - accrued
									(dblValueDate, csqs)) / dblScalingNotional;
	}

	@Override public double priceFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double priceFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromYieldToOptimalExercise => " +
				"Cannot calc exercise px from yld for bonds w emb option");

		return priceFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double priceFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double priceFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return priceFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double priceFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromYieldSpreadToOptimalExercise => " +
				"Cant calc Price from Yield Spread to optimal exercise for bonds w emb option");

		return priceFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double priceFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return priceFromBumpedZC (valParams, csqs, vcp, ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE,
			dblWorkoutDate, dblWorkoutFactor, dblZSpread);
	}

	@Override public double priceFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return priceFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double priceFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::priceFromZSpreadToOptimalExercise => " +
				"Cant calc Price from Z Spread to optimal exercise for bonds w emb option");

		return priceFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double tsySpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return tsySpreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double tsySpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return tsySpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double tsySpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromASWToOptimalExercise => " +
				"Cant calc TSY Spread from ASW to optimal exercise for bonds w emb option");

		return tsySpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double tsySpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double tsySpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return tsySpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double tsySpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromBondBasisToOptimalExercise => " +
				"Cant calc TSY Spread from Bond Basis to optimal exercise for bonds w emb option");

		return tsySpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double tsySpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double tsySpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return tsySpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double tsySpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromCreditBasisToOptimalExercise => " +
				"Cant calc TSY Spread from Credit Basis to optimal exercise for bonds w emb option");

		return tsySpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double tsySpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double tsySpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return tsySpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double tsySpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromDiscountMarginToOptimalExercise " +
				"=> Cant calc TSY Spread from Discount Margin to optimal exercise for bonds w emb option");

		return tsySpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double tsySpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double tsySpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return tsySpreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double tsySpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromGSpreadToOptimalExercise => " +
				"Cant calc TSY Spread from G Spread to optimal exercise for bonds w emb option");

		return tsySpreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double tsySpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double tsySpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return tsySpreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double tsySpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromISpreadToOptimalExercise => " +
				"Cant calc TSY Spread from I Spread to optimal exercise for bonds w emb option");

		return tsySpreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double tsySpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double tsySpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return tsySpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double tsySpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromOASToOptimalExercise => " +
				"Cant calc TSY Spread from OAS to optimal exercise for bonds w emb option");

		return tsySpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double tsySpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double tsySpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return tsySpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double tsySpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromPECSToOptimalExercise => " +
				"Cant calc TSY Spread from PECS to optimal exercise for bonds w emb option");

		return tsySpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double tsySpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double tsySpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return tsySpreadFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double tsySpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::tsySpreadFromPriceToOptimalExercise => Cant determine Work-out");

		return tsySpreadFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double tsySpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		double dblBmkYield = treasuryBenchmarkYield (valParams, csqs, dblWorkoutDate);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblBmkYield))
			throw new java.lang.Exception
				("BondComponent::tsySpreadFromYield => Cannot calculate TSY Bmk Yield");

		return dblYield - dblBmkYield;
	}

	@Override public double tsySpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double tsySpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromYieldToOptimalExercise => " +
				"Cant calc TSY Spread from Yield to optimal exercise for bonds w emb option");

		return tsySpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double tsySpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double tsySpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return tsySpreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double tsySpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc TSY Spread from Yield Spread to optimal exercise for bonds w emb option");

		return tsySpreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double tsySpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double tsySpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return tsySpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double tsySpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::tsySpreadFromZSpreadToOptimalExercise => " +
				"Cant calc TSY Spread from Z Spread to optimal exercise for bonds w emb option");

		return tsySpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double yieldFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double yieldFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return yieldFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double yieldFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromASWToOptimalExercise => " +
				"Cant calc Yield from ASW to optimal exercise for bonds w emb option");

		return yieldFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double yieldFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBondBasis) || valParams.valueDate() >=
			dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::yieldFromBondBasis => Invalid Inputs");

		return yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBumpedDC
			(valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.)) + dblBondBasis;
	}

	@Override public double yieldFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return yieldFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double yieldFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromBondBasisToOptimalExercise => " +
				"Cant calc Yield from Bond Basis to optimal exercise for bonds w emb option");

		return yieldFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double yieldFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double yieldFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return yieldFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double yieldFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromCreditBasisToOptimalExercise => " +
				"Cant calc Yield from Credit Basis to optimal exercise for bonds w emb option");

		return yieldFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double yieldFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutDate)
			|| !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblDiscountMargin))
			throw new java.lang.Exception ("BondComponent::yieldFromDiscountMargin => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::yieldFromDiscountMargin => Invalid inputs");

		double dblValueDate = valParams.valueDate();

		int iFreq = freq();

		return null == _fltParams ? dblDiscountMargin + dcFunding.libor (dblValueDate, ((int) (12. / (0 ==
			iFreq ? 2 : iFreq))) + "M") : dblDiscountMargin - indexRate (dblValueDate, csqs,
				(org.drip.analytics.cashflow.CompositeFloatingPeriod) currentPeriod (dblValueDate));
	}

	@Override public double yieldFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return yieldFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1., dblDiscountMargin);
	}

	@Override public double yieldFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromDiscountMarginToOptimalExercise => " +
				"Cant calc Yield from Discount Margin to optimal exercise for bonds w emb option");

		return yieldFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double yieldFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblGSpread) || valParams.valueDate() >= dblWorkoutDate
			+ LEFT_EOS_SNIP || null == csqs)
			throw new java.lang.Exception ("BondComponent::yieldFromGSpread => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()));

		if (null == dcGovvie)
			throw new java.lang.Exception ("BondComponent::yieldFromGSpread => Invalid Inputs");

		return dcGovvie.estimateManifestMeasure ("Yield", dblWorkoutDate) + dblGSpread;
	}

	@Override public double yieldFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return yieldFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double yieldFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromGSpreadToOptimalExercise => " +
				"Cant calc Yield from G Spread to optimal exercise for bonds w emb option");

		return yieldFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double yieldFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblISpread) || valParams.valueDate() >= dblWorkoutDate
			+ LEFT_EOS_SNIP || null == csqs)
			throw new java.lang.Exception ("BondComponent::yieldFromISpread => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dc = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()));

		if (null == dc) throw new java.lang.Exception ("BondComponent::yieldFromISpread => Invalid Inputs");

		return dc.estimateManifestMeasure ("Rate", dblWorkoutDate) + dblISpread;
	}

	@Override public double yieldFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return yieldFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double yieldFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromISpreadToOptimalExercise => " +
				"Cant calc Yield from I Spread to optimal exercise for bonds w emb option");

		return yieldFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double yieldFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return yieldFromOAS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS (valParams,
			csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double yieldFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return yieldFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double yieldFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromOASToOptimalExercise => " +
				"Cant calc Yield from OAS to optimal exercise for bonds w emb option");

		return yieldFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double yieldFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double yieldFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return yieldFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double yieldFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromPECSToOptimalExercise => " +
				"Cant calc Yield from PECS to optimal exercise for bonds w emb option");

		return yieldFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double yieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateYieldFromPrice (valParams, csqs, dblWorkoutDate,
			dblWorkoutFactor, dblPrice);
	}

	@Override public double yieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yieldFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yieldFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::yieldFromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return wi.yield();
	}

	@Override public double yieldFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblTSYSpread) || valParams.valueDate() >=
			dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcYieldFromTSYSpread => Invalid Inputs");

		return treasuryBenchmarkYield (valParams, csqs, dblWorkoutDate) + dblTSYSpread;
	}

	@Override public double yieldFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yieldFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yieldFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromTSYSpreadToOptimalExercise => " +
				"Cant calc Yield from TSY Spread to optimal exercise for bonds w emb option");

		return yieldFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yieldFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYieldSpread) || valParams.valueDate() >=
			dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::yieldFromYieldSpread => Invalid Inputs");

		return yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBumpedDC
			(valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.)) + dblYieldSpread;
	}

	@Override public double yieldFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return yieldFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double yieldFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromYieldSpreadToOptimalExercise => " +
				"Cant calc Yield from Yield Spread to optimal exercise for bonds w emb option");

		return yieldFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double yieldFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblZSpread) || valParams.valueDate() >= dblWorkoutDate
			+ LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::yieldFromZSpread => Invalid Inputs");

		return yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromBumpedZC
			(valParams, csqs, vcp, ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE, dblWorkoutDate,
				dblWorkoutFactor, dblZSpread));
	}

	@Override public double yieldFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return yieldFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double yieldFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldFromZSpreadToOptimalExercise => " +
				"Cant calc Yield from Z Spread to optimal exercise for bonds w emb option");

		return yieldFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double yield01FromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double yield01FromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return yield01FromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double yield01FromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromASWToOptimalExercise => " +
				"Cant calc Yield from ASW to optimal exercise for bonds w emb option");

		return yield01FromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double yield01FromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromBondBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double yield01FromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return yieldFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double yield01FromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromBondBasisToOptimalExercise => " +
				"Cant calc Yield01 from Bond Basis to optimal exercise for bonds w emb option");

		return yield01FromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double yield01FromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromCreditBasis
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double yield01FromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return yield01FromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double yield01FromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromCreditBasisToOptimalExercise => " +
				"Cant calc Yield01 from Credit Basis to optimal exercise for bonds w emb option");

		return yield01FromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double yield01FromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double yield01FromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return yield01FromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double yield01FromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromDiscountMarginToOptimalExercise =>" +
				" Cant calc Yield01 from Discount Margin to optimal exercise for bonds w emb option");

		return yield01FromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double yield01FromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double yield01FromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return yield01FromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double yield01FromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromGSpreadToOptimalExercise => " +
				"Cant calc Yield01 from G Spread to optimal exercise for bonds w emb option");

		return yield01FromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double yield01FromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return yield01FromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double yield01FromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return yield01FromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double yield01FromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromISpreadToOptimalExercise => " +
				"Cant calc Yield01 from I Spread to optimal exercise for bonds w emb option");

		return yield01FromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double yield01FromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double yield01FromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return yield01FromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double yield01FromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromOASToOptimalExercise => " +
				"Cant calc Yield01 from OAS to optimal exercise for bonds w emb option");

		return yield01FromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double yield01FromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double yield01FromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return yield01FromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double yield01FromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromPECSToOptimalExercise => " +
				"Cant calc Yield01 from PECS to optimal exercise for bonds w emb option");

		return yield01FromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double yield01FromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double yield01FromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yield01FromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yield01FromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::yield01FromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return yield01FromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double yield01FromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double yield01FromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yield01FromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yield01FromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromTSYSpreadToOptimalExercise => " +
				"Cant calc Yield01 from TSY Spread to optimal exercise for bonds w emb option");

		return yield01FromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yield01FromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::yield01FromYield => Invalid Inputs");

		return priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield) -
			priceFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield + 0.0001);
	}

	@Override public double yield01FromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double yield01FromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromYieldToOptimalExercise => " +
				"Cant calc Yield01 from Yield to optimal exercise for bonds w emb option");

		return yield01FromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double yield01FromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double yield01FromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return yield01FromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double yield01FromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromYieldSpreadToOptimalExercise => " +
				"Cant calc Yield01 from Yield Spread to optimal exercise for bonds w emb option");

		return yield01FromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblYieldSpread);
	}

	@Override public double yield01FromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double yield01FromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return yield01FromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double yield01FromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yield01FromZSpreadToOptimalExercise => " +
				"Cant calc Yield01 from Z Spread to optimal exercise for bonds w emb option");

		return yield01FromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblZSpread);
	}

	@Override public double yieldSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return yieldSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double yieldSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return yieldSpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double yieldSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromASWToOptimalExercise => " +
				"Cant calc Yield Spread from ASW to optimal exercise for bonds w emb option");

		return yieldSpreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double yieldSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return yieldSpreadFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double yieldSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return yieldSpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double yieldSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromBondBasisToOptimalExercise => "
				+ "Cant calc Yield Spread from Bond Basis to optimal exercise for bonds w emb option");

		return yieldSpreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double yieldSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return yieldSpreadFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double yieldSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return yieldSpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double yieldSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromCreditBasisToOptimalExercise " +
				"=> Cant calc Yield Spread from Credit Basis to optimal exercise for bonds w emb option");

		return yieldSpreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double yieldSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return yieldSpreadFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double yieldSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return yieldSpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double yieldSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::yieldSpreadFromDiscountMarginToOptimalExercise => " +
					"Cant calc Yield Spread from Disc Margin to optimal exercise for bonds w emb option");

		return yieldSpreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double yieldSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double yieldSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double yieldSpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromGSpreadToOptimalExercise => " +
				"Cant calc Yield Spread from G Spread to optimal exercise for bonds w emb option");

		return yieldSpreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblGSpread);
	}

	@Override public double yieldSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double yieldSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double yieldSpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromISpreadToOptimalExercise => " +
				"Cant calc Yield Spread from I Spread to optimal exercise for bonds w emb option");

		return yieldSpreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblISpread);
	}

	@Override public double yieldSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return yieldSpreadFromOAS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double yieldSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return yieldSpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double yieldSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromOASToOptimalExercise => " +
				"Cant calc Yield Spread from OAS to optimal exercise for bonds w emb option");

		return yieldSpreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double yieldSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return yieldSpreadFromPECS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double yieldSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return yieldSpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double yieldSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromPECSToOptimalExercise => " +
				"Cant calc Yield Spread from PECS to optimal exercise for bonds w emb option");

		return yieldSpreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double yieldSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yieldSpreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromPrice
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double yieldSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return yieldSpreadFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double yieldSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return yieldSpreadFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double yieldSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			yieldFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double yieldSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double yieldSpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc Yield Spread from TSY Spread to optimal exercise for bonds w emb option");

		return yieldSpreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblTSYSpread);
	}

	@Override public double yieldSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromYield => Invalid Inputs");

		return dblYield - yieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.));
	}

	@Override public double yieldSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return yieldSpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double yieldSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromYieldToOptimalExercise => " +
				"Cant calc Yield Spread from Yield to optimal exercise for bonds w emb option");

		return yieldSpreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double yieldSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, yieldFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double yieldSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return yieldSpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double yieldSpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::yieldSpreadFromZSpreadToOptimalExercise => " +
				"Cant calc Yield Spread from Z Spread to optimal exercise for bonds w emb option");

		return yieldSpreadFromZSpread (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblZSpread);
	}

	@Override public double zspreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return zspreadFromASW (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromASW
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double zspreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return zspreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double zspreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromASWToOptimalExercise => " +
				"Cant calc Z Spread from ASW to optimal exercise for bonds w emb option");

		return zspreadFromASW (valParams, csqs, vcp, maturityDate().julian(), 1., dblASW);
	}

	@Override public double zspreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return zspreadFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double zspreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return zspreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double zspreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromBondBasisToOptimalExercise => " +
				"Cant calc Z Spread from Bond Basis to optimal exercise for bonds w emb option");

		return zspreadFromBondBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblBondBasis);
	}

	@Override public double zspreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return zspreadFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double zspreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return zspreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double zspreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromCreditBasisToOptimalExercise => " +
				"Cant calc Z Spread from Credit Basis to optimal exercise for bonds w emb option");

		return zspreadFromCreditBasis (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblCreditBasis);
	}

	@Override public double zspreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return zspreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double zspreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return zspreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double zspreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::zspreadFromDiscountMarginToOptimalExercise => " +
					"Cant calc Z Spread from Discount Margin to optimal exercise for bonds w emb option");

		return zspreadFromDiscountMargin (valParams, csqs, vcp, maturityDate().julian(), 1.,
			dblDiscountMargin);
	}

	@Override public double zspreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return zspreadFromGSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double zspreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return zspreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double zspreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromGSpreadToOptimalExercise => " +
				"Cant calc Z Spread from G Spread to optimal exercise for bonds w emb option");

		return zspreadFromGSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblGSpread);
	}

	@Override public double zspreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return zspreadFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double zspreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return zspreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double zspreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromISpreadToOptimalExercise => " +
				"Cant calc Z Spread from I Spread to optimal exercise for bonds w emb option");

		return zspreadFromISpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblISpread);
	}

	@Override public double zspreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return zspreadFromOAS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double zspreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return zspreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double zspreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromOASToOptimalExercise => " +
				"Cant calc Z Spread from OAS to optimal exercise for bonds w emb option");

		return zspreadFromOAS (valParams, csqs, vcp, maturityDate().julian(), 1., dblOAS);
	}

	@Override public double zspreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return zspreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double zspreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return zspreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double zspreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromPECSToOptimalExercise => " +
				"Cant calc Z Spread from PECS to optimal exercise for bonds w emb option");

		return zspreadFromPECS (valParams, csqs, vcp, maturityDate().julian(), 1., dblPECS);
	}

	@Override public double zspreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateZSpreadFromPrice (valParams, csqs,
			ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE, dblWorkoutDate, dblWorkoutFactor, dblPrice);
	}

	@Override public double zspreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return zspreadFromPrice (valParams, csqs, vcp, maturityDate().julian(), 1., dblPrice);
	}

	@Override public double zspreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::zspreadFromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return zspreadFromPrice (valParams, csqs, vcp, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double zspreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return zspreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double zspreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return zspreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double zspreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc Z Spread from TSY Spread to optimal exercise for bonds w emb option");

		return zspreadFromTSYSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblTSYSpread);
	}

	@Override public double zspreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return zspreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double zspreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return zspreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double zspreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromYieldToOptimalExercise => " +
				"Cant calc Z Spread from Yield to optimal exercise for bonds w emb option");

		return zspreadFromYield (valParams, csqs, vcp, maturityDate().julian(), 1., dblYield);
	}

	@Override public double zspreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return zspreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, priceFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double zspreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return zspreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblYieldSpread);
	}

	@Override public double zspreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::zspreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc Z Spread from Yield Spread to optimal exercise for bonds w emb option");

		return zspreadFromYieldSpread (valParams, csqs, vcp, maturityDate().julian(), 1., dblYieldSpread);
	}

	@Override public org.drip.analytics.output.BondRVMeasures standardMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.param.valuation.WorkoutInfo wi,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || null == wi || !org.drip.quant.common.NumberUtil.IsValid
			(dblPrice))
			return null;

		double dblWorkoutDate = wi.date();

		double dblWorkoutYield = wi.yield();

		double dblWorkoutFactor = wi.factor();

		if (valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP) return null;

		double dblASW = java.lang.Double.NaN;
		double dblPECS = java.lang.Double.NaN;
		double dblGSpread = java.lang.Double.NaN;
		double dblISpread = java.lang.Double.NaN;
		double dblYield01 = java.lang.Double.NaN;
		double dblZSpread = java.lang.Double.NaN;
		double dblOASpread = java.lang.Double.NaN;
		double dblBondBasis = java.lang.Double.NaN;
		double dblConvexity = java.lang.Double.NaN;
		double dblTSYSpread = java.lang.Double.NaN;
		double dblCreditBasis = java.lang.Double.NaN;
		double dblDiscountMargin = java.lang.Double.NaN;
		double dblMacaulayDuration = java.lang.Double.NaN;
		double dblModifiedDuration = java.lang.Double.NaN;

		try {
			dblDiscountMargin = discountMarginFromYield (valParams, csqs, vcp, dblWorkoutDate,
				dblWorkoutFactor, dblWorkoutYield);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		if (null == _fltParams) {
			try {
				dblZSpread = zspreadFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
					dblPrice);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}

			try {
				dblOASpread = oasFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
					dblPrice);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		try {
			dblISpread = iSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblWorkoutYield);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblGSpread = gSpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblWorkoutYield);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblTSYSpread = tsySpreadFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblWorkoutYield);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblMacaulayDuration = macaulayDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate,
				dblWorkoutFactor, dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblModifiedDuration = modifiedDurationFromPrice (valParams, csqs, vcp, dblWorkoutDate,
				dblWorkoutFactor, dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblASW = aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblConvexity = convexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblCreditBasis = creditBasisFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblPECS = pecsFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblBondBasis = bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblWorkoutYield);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblYield01 = yield01FromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblWorkoutYield);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			return new org.drip.analytics.output.BondRVMeasures (dblPrice, dblBondBasis, dblZSpread,
				dblGSpread, dblISpread, dblOASpread, dblTSYSpread, dblDiscountMargin, dblASW, dblCreditBasis,
					dblPECS, dblYield01, dblModifiedDuration, dblMacaulayDuration, dblConvexity, wi);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == csqs) return null;

		if (null != pricerParams) {
			org.drip.param.definition.CalibrationParams calibParams = pricerParams.calibParams();

			if (null != calibParams) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalibMeasures =
					calibMeasures (valParams, pricerParams, csqs, vcp);

				if (null != mapCalibMeasures && mapCalibMeasures.containsKey (calibParams.measure()))
					return mapCalibMeasures;
			}
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = fairMeasures
			(valParams, pricerParams, csqs, vcp);

		if (null == mapMeasures) return null;

		java.lang.String strName = name();

		org.drip.param.definition.ProductQuote pq = csqs.productQuote (strName);

		if (null == pq) return mapMeasures;

		double dblMaturity = maturityDate().julian();

		if (null == _fltParams) {
			double dblParSpread = (mapMeasures.get ("FairDirtyPV") - mapMeasures.get ("FairParPV") -
				mapMeasures.get ("FairPrincipalPV")) / mapMeasures.get ("FairDirtyDV01");

			mapMeasures.put ("ParSpread", dblParSpread);

			mapMeasures.put ("FairParSpread", dblParSpread);
		} else {
			double dblCleanIndexCouponPV = mapMeasures.containsKey ("FairRiskyCleanIndexCouponPV") ?
				mapMeasures.get ("FairRiskyCleanIndexCouponPV") : mapMeasures.get
					("FairRisklessCleanIndexCouponPV");

			double dblZeroDiscountMargin = (mapMeasures.get ("FairCleanPV") - mapMeasures.get ("FairParPV") -
				dblCleanIndexCouponPV - mapMeasures.get ("FairPrincipalPV")) / mapMeasures.get
					("FairCleanDV01");

			mapMeasures.put ("ZeroDiscountMargin", dblZeroDiscountMargin);

			mapMeasures.put ("FairZeroDiscountMargin", dblZeroDiscountMargin);
		}

		org.drip.param.valuation.WorkoutInfo wiMarket = null;

		if (pq.containsQuote ("Price")) {
			double dblMarketPrice = pq.quote ("Price").value ("mid");

			mapMeasures.put ("MarketInputType=CleanPrice", dblMarketPrice);

			wiMarket = exerciseYieldFromPrice (valParams, csqs, vcp, dblMarketPrice);
		} else if (pq.containsQuote ("CleanPrice")) {
			double dblCleanMarketPrice = pq.quote ("CleanPrice").value ("mid");

			mapMeasures.put ("MarketInputType=CleanPrice", dblCleanMarketPrice);

			wiMarket = exerciseYieldFromPrice (valParams, csqs, vcp, dblCleanMarketPrice);
		} else if (pq.containsQuote ("QuotedMargin")) {
			double dblQuotedMargin = pq.quote ("QuotedMargin").value ("mid");

			mapMeasures.put ("MarketInputType=QuotedMargin", dblQuotedMargin);

			try {
				wiMarket = exerciseYieldFromPrice (valParams, csqs, vcp, priceFromDiscountMargin (valParams,
					csqs, vcp, dblQuotedMargin));
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		} else if (pq.containsQuote ("DirtyPrice")) {
			try {
				double dblDirtyMarketPrice = pq.quote ("DirtyPrice").value ("mid");

				mapMeasures.put ("MarketInputType=DirtyPrice", dblDirtyMarketPrice);

				wiMarket = exerciseYieldFromPrice (valParams, csqs, vcp, dblDirtyMarketPrice - accrued
					(valParams.valueDate(), csqs));
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (pq.containsQuote ("TSYSpread")) {
			try {
				double dblTSYSpread = pq.quote ("TSYSpread").value ("mid");

				mapMeasures.put ("MarketInputType=TSYSpread", dblTSYSpread);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (dblMaturity, treasuryBenchmarkYield
					(valParams, csqs, dblMaturity) + dblTSYSpread, 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (pq.containsQuote ("Yield")) {
			try {
				double dblYield = pq.quote ("Yield").value ("mid");

				mapMeasures.put ("MarketInputType=Yield", dblYield);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (dblMaturity, dblYield, 1.,
					org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (pq.containsQuote ("ZSpread")) {
			try {
				double dblZSpread = pq.quote ("ZSpread").value ("mid");

				mapMeasures.put ("MarketInputType=ZSpread", dblZSpread);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (dblMaturity, yieldFromZSpread
					(valParams, csqs, vcp, dblZSpread), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (pq.containsQuote ("ISpread")) {
			try {
				double dblISpread = pq.quote ("ISpread").value ("mid");

				mapMeasures.put ("MarketInputType=ISpread", dblISpread);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (dblMaturity, yieldFromISpread
					(valParams, csqs, vcp, dblISpread), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (pq.containsQuote ("CreditBasis")) {
			try {
				double dblCreditBasis = pq.quote ("CreditBasis").value ("mid");

				mapMeasures.put ("MarketInputType=CreditBasis", dblCreditBasis);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (dblMaturity, yieldFromCreditBasis
					(valParams, csqs, vcp, dblCreditBasis), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (pq.containsQuote ("PECS")) {
			try {
				double dblCreditBasis = pq.quote ("PECS").value ("mid");

				mapMeasures.put ("MarketInputType=PECS", dblCreditBasis);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (dblMaturity, yieldFromPECS (valParams,
					csqs, vcp, dblCreditBasis), 1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		}

		if (null != wiMarket) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapWorkoutMeasures =
				marketMeasures (valParams, pricerParams, csqs, vcp, wiMarket);

			if (null == _fltParams) {
				double dblParSpread = (mapWorkoutMeasures.get ("Price") - mapMeasures.get ("FairParPV") -
					mapMeasures.get ("FairPrincipalPV")) / mapMeasures.get ("FairCleanDV01");

				mapMeasures.put ("ParSpread", dblParSpread);

				mapMeasures.put ("MarketParSpread", dblParSpread);
			} else {
				double dblCleanIndexCouponPV = mapMeasures.containsKey ("FairRiskyCleanIndexCouponPV") ?
					mapMeasures.get ("FairRiskyCleanIndexCouponPV") : mapMeasures.get
						("FairRisklessCleanIndexCouponPV");

				double dblZeroDiscountMargin = (mapMeasures.get ("Price") - mapMeasures.get ("FairParPV") -
					dblCleanIndexCouponPV - mapMeasures.get ("FairPrincipalPV")) / mapMeasures.get
						("FairCleanDV01");

				mapMeasures.put ("ZeroDiscountMargin", dblZeroDiscountMargin);

				mapMeasures.put ("MarketZeroDiscountMargin", dblZeroDiscountMargin);
			}

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, mapWorkoutMeasures);

			org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

			if (null != mapMeasures.get ("FairYield")) {
				org.drip.param.market.CurveSurfaceQuoteSet csqsMarket =
					org.drip.param.creator.MarketParamsBuilder.Create
						((org.drip.analytics.rates.DiscountCurve) csqs.fundingCurve
							(fundingLabel()).parallelShiftQuantificationMetric (wiMarket.yield() -
								mapMeasures.get ("FairYield")), csqs.govvieCurve
									(org.drip.state.identifier.GovvieLabel.Standard (payCurrency())), cc,
										strName, csqs.productQuote (strName), csqs.quoteMap(),
											csqs.fixings());

				if (null != csqsMarket) {
					org.drip.analytics.output.BondWorkoutMeasures bwmMarket = workoutMeasures (valParams,
						pricerParams, csqsMarket, wiMarket.date(), wiMarket.factor());

					if (null != bwmMarket) {
						org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMarketMeasures
							= bwmMarket.toMap ("");

						org.drip.quant.common.CollectionUtil.MergeWithMain (mapMarketMeasures,
							org.drip.quant.common.CollectionUtil.PrefixKeys (mapMarketMeasures, "Market"));

						org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, mapMarketMeasures);
					}
				}
			}
		}

		return mapMeasures;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("Accrued01");

		setstrMeasureNames.add ("AssetSwapSpread");

		setstrMeasureNames.add ("ASW");

		setstrMeasureNames.add ("BondBasis");

		setstrMeasureNames.add ("CleanCouponPV");

		setstrMeasureNames.add ("CleanDV01");

		setstrMeasureNames.add ("CleanIndexCouponPV");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("Convexity");

		setstrMeasureNames.add ("CreditRisklessParPV");

		setstrMeasureNames.add ("CreditRisklessPrincipalPV");

		setstrMeasureNames.add ("CreditRiskyParPV");

		setstrMeasureNames.add ("CreditRiskyPrincipalPV");

		setstrMeasureNames.add ("CreditBasis");

		setstrMeasureNames.add ("DiscountMargin");

		setstrMeasureNames.add ("DefaultExposure");

		setstrMeasureNames.add ("DefaultExposureNoRec");

		setstrMeasureNames.add ("DirtyCouponPV");

		setstrMeasureNames.add ("DirtyDV01");

		setstrMeasureNames.add ("DirtyIndexCouponPV");

		setstrMeasureNames.add ("DirtyPrice");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("Duration");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("ExpectedRecovery");

		setstrMeasureNames.add ("FairAccrued");

		setstrMeasureNames.add ("FairAccrued01");

		setstrMeasureNames.add ("FairAssetSwapSpread");

		setstrMeasureNames.add ("FairASW");

		setstrMeasureNames.add ("FairBondBasis");

		setstrMeasureNames.add ("FairCleanCouponPV");

		setstrMeasureNames.add ("FairCleanDV01");

		setstrMeasureNames.add ("FairCleanIndexCouponPV");

		setstrMeasureNames.add ("FairCleanPrice");

		setstrMeasureNames.add ("FairCleanPV");

		setstrMeasureNames.add ("FairConvexity");

		setstrMeasureNames.add ("FairCreditBasis");

		setstrMeasureNames.add ("FairCreditRisklessParPV");

		setstrMeasureNames.add ("FairCreditRisklessPrincipalPV");

		setstrMeasureNames.add ("FairCreditRiskyParPV");

		setstrMeasureNames.add ("FairCreditRiskyPrincipalPV");

		setstrMeasureNames.add ("FairDefaultExposure");

		setstrMeasureNames.add ("FairDefaultExposureNoRec");

		setstrMeasureNames.add ("FairDirtyCouponPV");

		setstrMeasureNames.add ("FairDirtyDV01");

		setstrMeasureNames.add ("FairDirtyIndexCouponPV");

		setstrMeasureNames.add ("FairDirtyPrice");

		setstrMeasureNames.add ("FairDirtyPV");

		setstrMeasureNames.add ("FairDiscountMargin");

		setstrMeasureNames.add ("FairDuration");

		setstrMeasureNames.add ("FairDV01");

		setstrMeasureNames.add ("FairExpectedRecovery");

		setstrMeasureNames.add ("FairFirstIndexRate");

		setstrMeasureNames.add ("FairGSpread");

		setstrMeasureNames.add ("FairISpread");

		setstrMeasureNames.add ("FairLossOnInstantaneousDefault");

		setstrMeasureNames.add ("FairMacaulayDuration");

		setstrMeasureNames.add ("FairModifiedDuration");

		setstrMeasureNames.add ("FairOAS");

		setstrMeasureNames.add ("FairOASpread");

		setstrMeasureNames.add ("FairOptionAdjustedSpread");

		setstrMeasureNames.add ("FairParPV");

		setstrMeasureNames.add ("FairParSpread");

		setstrMeasureNames.add ("FairPECS");

		setstrMeasureNames.add ("FairPrice");

		setstrMeasureNames.add ("FairPrincipalPV");

		setstrMeasureNames.add ("FairPV");

		setstrMeasureNames.add ("FairRecoveryPV");

		setstrMeasureNames.add ("FairRisklessCleanCouponPV");

		setstrMeasureNames.add ("FairRisklessCleanDV01");

		setstrMeasureNames.add ("FairRisklessCleanIndexCouponPV");

		setstrMeasureNames.add ("FairRisklessCleanPV");

		setstrMeasureNames.add ("FairRisklessDirtyCouponPV");

		setstrMeasureNames.add ("FairRisklessDirtyDV01");

		setstrMeasureNames.add ("FairRisklessDirtyIndexCouponPV");

		setstrMeasureNames.add ("FairRisklessDirtyPV");

		setstrMeasureNames.add ("FairRiskyCleanCouponPV");

		setstrMeasureNames.add ("FairRiskyCleanDV01");

		setstrMeasureNames.add ("FairRiskyCleanIndexCouponPV");

		setstrMeasureNames.add ("FairRiskyCleanPV");

		setstrMeasureNames.add ("FairRiskyDirtyCouponPV");

		setstrMeasureNames.add ("FairRiskyDirtyDV01");

		setstrMeasureNames.add ("FairRiskyDirtyIndexCouponPV");

		setstrMeasureNames.add ("FairRiskyDirtyPV");

		setstrMeasureNames.add ("FairTSYSpread");

		setstrMeasureNames.add ("FairWorkoutDate");

		setstrMeasureNames.add ("FairWorkoutFactor");

		setstrMeasureNames.add ("FairWorkoutType");

		setstrMeasureNames.add ("FairWorkoutYield");

		setstrMeasureNames.add ("FairYield");

		setstrMeasureNames.add ("FairYield01");

		setstrMeasureNames.add ("FairYieldBasis");

		setstrMeasureNames.add ("FairYieldSpread");

		setstrMeasureNames.add ("FairZeroDiscountMargin");

		setstrMeasureNames.add ("FairZSpread");

		setstrMeasureNames.add ("FirstCouponRate");

		setstrMeasureNames.add ("FirstIndexRate");

		setstrMeasureNames.add ("GSpread");

		setstrMeasureNames.add ("ISpread");

		setstrMeasureNames.add ("LossOnInstantaneousDefault");

		setstrMeasureNames.add ("MacaulayDuration");

		setstrMeasureNames.add ("MarketAccrued");

		setstrMeasureNames.add ("MarketAccrued01");

		setstrMeasureNames.add ("MarketCleanCouponPV");

		setstrMeasureNames.add ("MarketCleanDV01");

		setstrMeasureNames.add ("MarketCleanIndexCouponPV");

		setstrMeasureNames.add ("MarketCleanPrice");

		setstrMeasureNames.add ("MarketCleanPV");

		setstrMeasureNames.add ("MarketCreditRisklessParPV");

		setstrMeasureNames.add ("MarketCreditRisklessPrincipalPV");

		setstrMeasureNames.add ("MarketCreditRiskyParPV");

		setstrMeasureNames.add ("MarketCreditRiskyPrincipalPV");

		setstrMeasureNames.add ("MarketDefaultExposure");

		setstrMeasureNames.add ("MarketDefaultExposureNoRec");

		setstrMeasureNames.add ("MarketDirtyCouponPV");

		setstrMeasureNames.add ("MarketDirtyDV01");

		setstrMeasureNames.add ("MarketDirtyIndexCouponPV");

		setstrMeasureNames.add ("MarketDirtyPrice");

		setstrMeasureNames.add ("MarketDirtyPV");

		setstrMeasureNames.add ("MarketDV01");

		setstrMeasureNames.add ("MarketExpectedRecovery");

		setstrMeasureNames.add ("MarketFirstCouponRate");

		setstrMeasureNames.add ("MarketFirstIndexRate");

		setstrMeasureNames.add ("MarketInputType=CleanPrice");

		setstrMeasureNames.add ("MarketInputType=CreditBasis");

		setstrMeasureNames.add ("MarketInputType=DirtyPrice");

		setstrMeasureNames.add ("MarketInputType=GSpread");

		setstrMeasureNames.add ("MarketInputType=ISpread");

		setstrMeasureNames.add ("MarketInputType=PECS");

		setstrMeasureNames.add ("MarketInputType=QuotedMargin");

		setstrMeasureNames.add ("MarketInputType=TSYSpread");

		setstrMeasureNames.add ("MarketInputType=Yield");

		setstrMeasureNames.add ("MarketInputType=ZSpread");

		setstrMeasureNames.add ("MarketLossOnInstantaneousDefault");

		setstrMeasureNames.add ("MarketParPV");

		setstrMeasureNames.add ("MarketPrincipalPV");

		setstrMeasureNames.add ("MarketPV");

		setstrMeasureNames.add ("MarketRecoveryPV");

		setstrMeasureNames.add ("MarketRisklessDirtyCouponPV");

		setstrMeasureNames.add ("MarketRisklessDirtyDV01");

		setstrMeasureNames.add ("MarketRisklessDirtyIndexCouponPV");

		setstrMeasureNames.add ("MarketRisklessDirtyPV");

		setstrMeasureNames.add ("MarketRiskyDirtyCouponPV");

		setstrMeasureNames.add ("MarketRiskyDirtyDV01");

		setstrMeasureNames.add ("MarketRiskyDirtyIndexCouponPV");

		setstrMeasureNames.add ("MarketRiskyDirtyPV");

		setstrMeasureNames.add ("ModifiedDuration");

		setstrMeasureNames.add ("OAS");

		setstrMeasureNames.add ("OASpread");

		setstrMeasureNames.add ("OptionAdjustedSpread");

		setstrMeasureNames.add ("ParEquivalentCDSSpread");

		setstrMeasureNames.add ("ParPV");

		setstrMeasureNames.add ("ParSpread");

		setstrMeasureNames.add ("PECS");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PrincipalPV");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("RecoveryPV");

		setstrMeasureNames.add ("RisklessCleanCouponPV");

		setstrMeasureNames.add ("RisklessCleanDV01");

		setstrMeasureNames.add ("RisklessCleanIndexCouponPV");

		setstrMeasureNames.add ("RisklessCleanPV");

		setstrMeasureNames.add ("RisklessDirtyCouponPV");

		setstrMeasureNames.add ("RisklessDirtyDV01");

		setstrMeasureNames.add ("RisklessDirtyIndexCouponPV");

		setstrMeasureNames.add ("RisklessDirtyPV");

		setstrMeasureNames.add ("RiskyCleanCouponPV");

		setstrMeasureNames.add ("RiskyCleanDV01");

		setstrMeasureNames.add ("RiskyCleanIndexCouponPV");

		setstrMeasureNames.add ("RiskyCleanPV");

		setstrMeasureNames.add ("RiskyDirtyCouponPV");

		setstrMeasureNames.add ("RiskyDirtyDV01");

		setstrMeasureNames.add ("RiskyDirtyIndexCouponPV");

		setstrMeasureNames.add ("RiskyDirtyPV");

		setstrMeasureNames.add ("TSYSpread");

		setstrMeasureNames.add ("WorkoutDate");

		setstrMeasureNames.add ("WorkoutFactor");

		setstrMeasureNames.add ("WorkoutType");

		setstrMeasureNames.add ("WorkoutYield");

		setstrMeasureNames.add ("Yield");

		setstrMeasureNames.add ("Yield01");

		setstrMeasureNames.add ("YieldBasis");

		setstrMeasureNames.add ("YieldSpread");

		setstrMeasureNames.add ("ZeroDiscountMargin");

		setstrMeasureNames.add ("ZSpread");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		return null;
	}

	@Override public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		return null;
	}

	/**
	 * The BondCalibrator implements a calibrator that calibrates the yield, the credit basis, or the Z
	 * 		Spread for the bond given the price input. Calibration happens via either Newton-Raphson method,
	 * 		or via bracketing/root searching.
	 * 
	 * @author Lakshmi Krishnamurthy
	 *
	 */

	public class BondCalibrator {
		private BondComponent _bond = null;

		/**
		 * Constructor: Construct the calibrator from the parent bond.
		 * 
		 * @param bond Parent
		 * 
		 * @throws java.lang.Exception Thrown if the inputs are invalid
		 */

		public BondCalibrator (
			final BondComponent bond)
			throws java.lang.Exception
		{
			if (null == (_bond = bond))
				throw new java.lang.Exception ("BondComponent::BondCalibrator ctr => Invalid Inputs");
		}

		/**
		 * Calibrate the bond yield from the market price using the root bracketing technique.
		 * 
		 * @param valParams Valuation Parameters
		 * @param csqs Bond Market Parameters
		 * @param dblWorkoutDate JulianDate Work-out
		 * @param dblWorkoutFactor Work-out factor
		 * @param dblPrice Price to be calibrated to
		 * 
		 * @return The calibrated Yield
		 * 
		 * @throws java.lang.Exception Thrown if the yield cannot be calibrated
		 */

		public double calibrateYieldFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final double dblWorkoutDate,
			final double dblWorkoutFactor,
			final double dblPrice)
			throws java.lang.Exception
		{
			org.drip.quant.function1D.AbstractUnivariate ofYieldToPrice = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				@Override public double evaluate (
					final double dblYield)
					throws java.lang.Exception
				{
					return _bond.priceFromYield (valParams, csqs, null, dblWorkoutDate, dblWorkoutFactor,
						dblYield) - dblPrice;
				}
			};

			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderNewton (0., ofYieldToPrice, true).findRoot();

			if (null == rfop || !rfop.containsRoot()) {
				rfop = new org.drip.quant.solver1D.FixedPointFinderZheng (0., ofYieldToPrice,
					true).findRoot();

				if (null == rfop || !rfop.containsRoot())
					throw new java.lang.Exception
						("BondComponent::BondCalibrator::calibrateYieldFromPrice => Cannot get root!");
			}

			return rfop.getRoot();
		}

		/**
		 * Calibrate the bond Z Spread from the market price using the root bracketing technique.
		 * 
		 * @param valParams Valuation Parameters
		 * @param csqs Bond Market Parameters
		 * @param iZeroCurveBaseDC The Discount Curve to derive the zero curve off of
		 * @param dblWorkoutDate JulianDate Work-out
		 * @param dblWorkoutFactor Work-out factor
		 * @param dblPrice Price to be calibrated to
		 * 
		 * @return The calibrated Z Spread
		 * 
		 * @throws java.lang.Exception Thrown if the Z Spread cannot be calibrated
		 */

		public double calibrateZSpreadFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final int iZeroCurveBaseDC,
			final double dblWorkoutDate,
			final double dblWorkoutFactor,
			final double dblPrice)
			throws java.lang.Exception
		{
			if (null != _fltParams)
				throw new java.lang.Exception
					("BondComponent::BondCalibrator::calibZSpreadFromPrice => Z Spread Calculation turned off for floaters!");

			org.drip.quant.function1D.AbstractUnivariate ofZSpreadToPrice = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				@Override public double evaluate (
					final double dblZSpread)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedZC (valParams, csqs, null, iZeroCurveBaseDC, dblWorkoutDate,
						dblWorkoutFactor, dblZSpread) - dblPrice;
				}
			};

			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., ofZSpreadToPrice, true).findRoot();

			if (null == rfop || !rfop.containsRoot())
				throw new java.lang.Exception
					("BondComponent::BondCalibrator::calibrateZSpreadFromPrice => Cannot get root!");

			return rfop.getRoot();
		}

		/**
		 * Calibrate the bond Z Spread from the market price. Calibration is done by bumping the discount
		 * 		curve.
		 * 
		 * @param valParams Valuation Parameters
		 * @param csqs Bond Market Parameters
		 * @param dblWorkoutDate JulianDate Work-out
		 * @param dblWorkoutFactor Work-out factor
		 * @param dblPriceCalib Price to be calibrated to
		 * 
		 * @return The calibrated Z Spread
		 * 
		 * @throws java.lang.Exception Thrown if the yield cannot be calibrated
		 */

		public double calibDiscCurveSpreadFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final double dblWorkoutDate,
			final double dblWorkoutFactor,
			final double dblPriceCalib)
			throws java.lang.Exception
		{
			org.drip.quant.function1D.AbstractUnivariate ofZSpreadToPrice = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				@Override public double evaluate (
					final double dblZSpread)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor,
						dblZSpread) - dblPriceCalib;
				}
			};

			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., ofZSpreadToPrice, true).findRoot();

			if (null == rfop || !rfop.containsRoot())
				throw new java.lang.Exception
					("BondComponent::BondCalibrator::calibDiscCurveSpreadFromPrice => Cannot get root!");

			return rfop.getRoot();
		}

		/**
		 * Calibrate the bond Z Spread from the market price. Calibration is done by bumping the Zero Curve.
		 * 
		 * @param valParams Valuation Parameters
		 * @param csqs Bond Market Parameters
		 * @param vcp Quoting Parameters
		 * @param dblWorkoutDate JulianDate Work-out
		 * @param dblWorkoutFactor Work-out factor
		 * @param dblPriceCalib Price to be calibrated to
		 * 
		 * @return The calibrated Z Spread
		 * 
		 * @throws java.lang.Exception Thrown if the yield cannot be calibrated
		 */

		public double calibZeroCurveSpreadFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final org.drip.param.valuation.ValuationCustomizationParams vcp,
			final double dblWorkoutDate,
			final double dblWorkoutFactor,
			final double dblPriceCalib)
			throws java.lang.Exception
		{
			if (null != _fltParams)
				throw new java.lang.Exception
					("BondComponent::BondCalibrator::calibZeroCurveSpreadFromPrice => Z Spread Calculation turned off for floaters!");

			org.drip.quant.function1D.AbstractUnivariate ofZSpreadToPrice = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				@Override public double evaluate (
					final double dblZSpread)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor,
						dblZSpread) - dblPriceCalib;
				}
			};

			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., ofZSpreadToPrice, true).findRoot();

			if (null == rfop || !rfop.containsRoot())
				throw new java.lang.Exception
					("BondComponent.calibZeroCurveSpreadFromPrice => Cannot get root!");

			return rfop.getRoot();
		}

		/**
		 * Calibrate the bond Credit Basis from the market price
		 * 
		 * @param valParams Valuation Parameters
		 * @param csqs Bond Market Parameters
		 * @param dblWorkoutDate JulianDate Work-out
		 * @param dblWorkoutFactor Work-out factor
		 * @param dblPriceCalib Price to be calibrated to
		 * 
		 * @return The calibrated Credit Basis
		 * 
		 * @throws java.lang.Exception Thrown if the Credit Basis cannot be calibrated
		 */

		public double calibrateCreditBasisFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final double dblWorkoutDate,
			final double dblWorkoutFactor,
			final double dblPriceCalib,
			final boolean bFlatCalib)
			throws java.lang.Exception
		{
			org.drip.quant.function1D.AbstractUnivariate ofCreditBasisToPrice = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				@Override public double evaluate (
					final double dblCreditBasis)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedCC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor,
						dblCreditBasis, bFlatCalib) - dblPriceCalib;
				}
			};

			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., ofCreditBasisToPrice, true).findRoot();

			if (null == rfop || !rfop.containsRoot())
				throw new java.lang.Exception
					("BondComponent.calibrateCreditBasisFromPrice => Cannot get root!");

			return rfop.getRoot();
		}
	}

	@Override public void showPeriods()
		throws java.lang.Exception
	{
		for (org.drip.analytics.cashflow.CompositePeriod period : couponPeriods())
			System.out.println ("\t" + org.drip.analytics.date.JulianDate.fromJulian (period.startDate()) +
				"->" + org.drip.analytics.date.JulianDate.fromJulian (period.endDate()) + "    " +
					period.accrualDCF (period.endDate()));
	}
}
