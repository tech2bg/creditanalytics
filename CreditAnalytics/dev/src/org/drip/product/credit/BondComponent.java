
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
	private static final boolean s_bBlog = false;
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

	private org.drip.product.params.TsyBmkSet _tsyBmkSet = null;
	private org.drip.product.params.CouponSetting _cpnParams = null;
	private org.drip.product.params.NotionalSetting _notlParams = null;
	private org.drip.product.params.FloaterSetting _fltParams = null;
	private org.drip.product.params.CurrencySet _ccyParams = null;
	private org.drip.product.params.IdentifierSet _idParams = null;
	private org.drip.product.params.QuoteConvention _mktConv = null;
	private org.drip.product.params.RatesSetting _irValParams = null;
	private org.drip.product.params.CreditSetting _crValParams = null;
	private org.drip.product.params.TerminationSetting _cfteParams = null;
	private org.drip.product.params.PeriodSet _periodParams = null;
	private org.drip.param.market.LatentStateFixingsContainer _lsfc = null;

	/*
	 * Bond EOS Params
	 */

	protected org.drip.product.params.EmbeddedOptionSchedule _eosPut = null;
	protected org.drip.product.params.EmbeddedOptionSchedule _eosCall = null;

	private double getTsyBmkYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblWorkoutDate))
			throw new java.lang.Exception ("Bond.getTsyBmkYield: Bad val/mkt Params");

		java.lang.String strTsyBmk = null;
		org.drip.param.definition.ProductQuote cqTsyBmkYield = null;

		if (null != _tsyBmkSet) strTsyBmk = _tsyBmkSet.getPrimaryBmk();

		if (null == strTsyBmk || strTsyBmk.isEmpty())
			strTsyBmk = org.drip.analytics.support.AnalyticsHelper.BaseTsyBmk (valParams.valueDate(),
				dblWorkoutDate);

		if (null != csqs.quoteMap() && null != strTsyBmk && !strTsyBmk.isEmpty())
			cqTsyBmkYield = csqs.quoteMap().get (strTsyBmk);

		if (null != cqTsyBmkYield && null != cqTsyBmkYield.quote ("Yield"))
			return cqTsyBmkYield.quote ("Yield").getQuote ("mid");

		org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

		return null == dcGovvie ? java.lang.Double.NaN : dcGovvie.libor (valParams.valueDate(),
			dblWorkoutDate);
	}

	private org.drip.param.valuation.WorkoutInfo calcExerciseCallYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblPrice) || null == _eosCall)
			return null;

		int iExercise = -1;
		double dblExerciseYield = java.lang.Double.NaN;

		try {
			dblExerciseYield = calcYieldFromPrice (valParams, csqs, quotingParams,
				_periodParams.maturity(), 1., dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			return null;
		}

		double[] adblEOSDates = _eosCall.getDates();

		double[] adblEOSFactors = _eosCall.getFactors();

		for (int i = 0; i < adblEOSDates.length; ++i) {
			if (valParams.valueDate() > adblEOSDates[i] + LEFT_EOS_SNIP || adblEOSDates[i] -
				valParams.valueDate() < _eosCall.getExerciseNoticePeriod())
				continue;

			try {
				double dblYield = calcYieldFromPrice (valParams, csqs, quotingParams, adblEOSDates[i],
					adblEOSFactors[i], dblPrice);

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
				return new org.drip.param.valuation.WorkoutInfo (_periodParams.maturity(),
					dblExerciseYield, 1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);

			return new org.drip.param.valuation.WorkoutInfo (adblEOSDates[iExercise], dblExerciseYield,
				adblEOSFactors[iExercise], org.drip.param.valuation.WorkoutInfo.WO_TYPE_CALL);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.param.valuation.WorkoutInfo calcExercisePutYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblPrice) || null == _eosPut)
			return null;

		int iExercise = -1;
		double dblExerciseYield = java.lang.Double.NaN;

		try {
			dblExerciseYield = calcYieldFromPrice (valParams, csqs, quotingParams,
				_periodParams.maturity(), 1., dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			return null;
		}

		double[] adblEOSDates = _eosPut.getDates();

		double[] adblEOSFactors = _eosPut.getFactors();

		for (int i = 0; i < adblEOSDates.length; ++i) {
			if (valParams.valueDate() > adblEOSDates[i] + LEFT_EOS_SNIP || adblEOSDates[i] -
				valParams.valueDate() < _eosPut.getExerciseNoticePeriod())
				continue;

			try {
				double dblYield = calcYieldFromPrice (valParams, csqs, quotingParams, adblEOSDates[i],
					adblEOSFactors[i], dblPrice);

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
				return new org.drip.param.valuation.WorkoutInfo (_periodParams.maturity(),
					dblExerciseYield, 1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);

			return new org.drip.param.valuation.WorkoutInfo (adblEOSDates[iExercise], dblExerciseYield,
				adblEOSFactors[iExercise], org.drip.param.valuation.WorkoutInfo.WO_TYPE_PUT);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.param.valuation.WorkoutInfo exerciseYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblPrice)) return null;

		try {
			if (null == _eosCall && null == _eosPut)
				return new org.drip.param.valuation.WorkoutInfo (_periodParams.maturity(),
					calcYieldFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
						dblPrice), 1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);

			if (null == _eosCall && null != _eosPut)
				return calcExercisePutYieldFromPrice (valParams, csqs, quotingParams, dblPrice);

			if (null != _eosCall && null == _eosPut)
				return calcExerciseCallYieldFromPrice (valParams, csqs, quotingParams, dblPrice);

			org.drip.param.valuation.WorkoutInfo wiPut = calcExercisePutYieldFromPrice (valParams, csqs,
				quotingParams, dblPrice);

			org.drip.param.valuation.WorkoutInfo wiCall = calcExerciseCallYieldFromPrice (valParams,
				csqs, quotingParams, dblPrice);

			return wiPut.date() < wiCall.date() ? wiPut : wiCall;
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private double getIndexRate (
		final double dblValue,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.analytics.cashflow.CompositeFloatingPeriod period)
		throws java.lang.Exception
	{
		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve (fundingLabel()[0]);

		if (null != period) {
			if (!csqs.available (((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)
				period.periods().get (0)).referenceIndexPeriod().fixingDate(), _fltParams._fri)) {
				if (s_bBlog)
					System.out.println ("IRS reset for index " + _fltParams._fri.fullyQualifiedName() +
						" and reset date " + org.drip.analytics.date.JulianDate.fromJulian
							(((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod) period.periods().get
								(0)).referenceIndexPeriod().fixingDate()) +
									" not found; defaulting to implied");

				org.drip.analytics.rates.ForwardRateEstimator fc = null;

				if (null != _fltParams && null != _fltParams._fri) {
					fc = csqs.forwardCurve (_fltParams._fri);

					if (null == fc || !_fltParams._fri.match (fc.index()))
						fc = dc.forwardRateEstimator (period.payDate(), _fltParams._fri);
				}

				if (null != fc) return fc.forward (period.payDate());

				if (period.startDate() < dblValue && 0 != _periodParams.freq())
					return dc.libor (dblValue, (12 / _periodParams.freq()) + "M");

				return dc.libor (period.startDate(), period.endDate());
			}

			return csqs.getFixing (((org.drip.analytics.cashflow.ComposableUnitFloatingPeriod)
				period.periods().get (0)).referenceIndexPeriod().fixingDate(), _fltParams._fri);
		}

		double dblRateRefEndDate = dblValue + LOCAL_FORWARD_RATE_WIDTH;

		if (0 != _periodParams.freq()) dblRateRefEndDate = dblValue + 365.25 / _periodParams.freq();

		double dblIndexRate = dc.libor (dblValue, dblRateRefEndDate);

		if (s_bBlog) System.out.println ("All else fails! " + dblIndexRate);

		return dblIndexRate;
	}

	private org.drip.analytics.output.BondWorkoutMeasures calcBondWorkoutMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate,
		final double dblWorkoutFactor)
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblWorkoutDate) ||
			java.lang.Double.isNaN (dblWorkoutFactor) || valParams.valueDate() >= dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding) return null;

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
			for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
				if (null == period || period.payDate() < valParams.valueDate()) continue;

				double dblPeriodStartDate = period.startDate() > valParams.valueDate() ? period.startDate() :
					valParams.valueDate();

				double dblPeriodEndDate = period.endDate();

				if (dblWorkoutDate <= dblPeriodEndDate) {
					bTerminateCouponFlow = true;
					dblPeriodEndDate = dblWorkoutDate;
				}

				double dblPeriodDF = dcFunding.df (period.payDate());

				org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (valParams.valueDate(),
					valParams, csqs);

				if (null == pcm) return null;

				double dblPeriodCoupon = pcm.rate();

				double dblPeriodIndexRate = period.periods().get (0).baseRate (csqs);

				if (bPeriodZero) {
					bPeriodZero = false;
					dblFirstCoupon = dblPeriodCoupon;

					if (period.startDate() < valParams.valueDate())
						dblAccrued01 = 0.0001 * period.accrualDCF (valParams.valueDate()) * notional
							(period.startDate(), valParams.valueDate());

					if (null != _fltParams) dblFirstIndexRate = dblPeriodIndexRate;
				}

				double dblPeriodCreditRisklessDirtyDV01 = 0.0001 * period.accrualDCF (dblPeriodEndDate) *
					dblPeriodDF * notional (dblPeriodStartDate, dblPeriodEndDate);

				double dblPeriodCreditRiskessPrincipalPV = (notional (dblPeriodStartDate) - notional
					(dblPeriodEndDate)) * dblPeriodDF;

				double dblPeriodCreditRiskyDirtyDV01 = dblPeriodCreditRisklessDirtyDV01;
				double dblPeriodCreditRiskyPrincipalPV = dblPeriodCreditRiskessPrincipalPV;

				org.drip.state.identifier.CreditLabel[] aLSLCreditCurve = creditLabel();

				if (null != aLSLCreditCurve && 0 < aLSLCreditCurve.length && null != csqs.creditCurve
					(aLSLCreditCurve[0]) && null != pricerParams) {
					double dblSurvProb = java.lang.Double.NaN;

					if (dblPeriodEndDate < period.endDate())
						dblSurvProb = csqs.creditCurve (aLSLCreditCurve[0]).survival (dblPeriodEndDate);
					else {
						if (pricerParams.survivalToPayDate())
							dblSurvProb = csqs.creditCurve (aLSLCreditCurve[0]).survival (period.payDate());
						else
							dblSurvProb = csqs.creditCurve (aLSLCreditCurve[0]).survival (dblPeriodEndDate);
					}

					dblPeriodCreditRiskyDirtyDV01 *= dblSurvProb;
					dblPeriodCreditRiskyPrincipalPV *= dblSurvProb;

					for (org.drip.analytics.cashflow.LossQuadratureMetrics lp : period.lossMetrics (this,
						valParams, pricerParams, dblWorkoutDate, csqs)) {
						if (null == lp) continue;

						double dblSubPeriodEnd = lp.end();

						double dblSubPeriodStart = lp.start();

						double dblSubPeriodDF = dcFunding.effectiveDF (dblSubPeriodStart +
							_crValParams._iDefPayLag, dblSubPeriodEnd + _crValParams._iDefPayLag);

						double dblSubPeriodNotional = notional (dblSubPeriodStart, dblSubPeriodEnd);

						double dblSubPeriodSurvival = csqs.creditCurve (aLSLCreditCurve[0]).survival
							(dblSubPeriodStart) - csqs.creditCurve (aLSLCreditCurve[0]).survival
								(dblSubPeriodEnd);

						if (_crValParams._bAccrOnDefault)
							dblPeriodCreditRiskyDirtyDV01 += 0.0001 * lp.accrualDCF() * dblSubPeriodSurvival
								* dblSubPeriodDF * dblSubPeriodNotional;

						double dblRecovery = _crValParams._bUseCurveRec ? csqs.creditCurve
							(aLSLCreditCurve[0]).effectiveRecovery (dblSubPeriodStart, dblSubPeriodEnd) :
								_crValParams._dblRecovery;

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
				dblCreditRiskyDirtyIndexCouponPV += 10000. * dblPeriodIndexRate *
					dblPeriodCreditRiskyDirtyDV01;
				dblCreditRisklessDirtyIndexCouponPV += 10000. * dblPeriodIndexRate *
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

			dblCreditRisklessParPV = dcFunding.df (_periodParams.maturity()) * notional
				(_periodParams.maturity()) * dblWorkoutFactor;

			org.drip.state.identifier.CreditLabel[] aLSLCreditCurve = creditLabel();

			if (null != aLSLCreditCurve && 0 < aLSLCreditCurve.length && null != csqs.creditCurve
				(aLSLCreditCurve[0]) && null != pricerParams)
				dblCreditRiskyParPV = dblCreditRisklessParPV * csqs.creditCurve (aLSLCreditCurve[0]).survival
					(_periodParams.maturity());

			org.drip.analytics.output.BondCouponMeasures bcmCreditRisklessDirty = new
				org.drip.analytics.output.BondCouponMeasures (dblCreditRisklessDirtyDV01,
					dblCreditRisklessDirtyIndexCouponPV, dblCreditRisklessDirtyCouponPV,
						dblCreditRisklessDirtyCouponPV + dblCreditRisklessPrincipalPV +
							dblCreditRisklessParPV);

			double dblDefaultExposure = java.lang.Double.NaN;
			double dblDefaultExposureNoRec = java.lang.Double.NaN;
			double dblLossOnInstantaneousDefault = java.lang.Double.NaN;
			org.drip.analytics.output.BondCouponMeasures bcmCreditRiskyDirty = null;

			if (null != aLSLCreditCurve && 0 != aLSLCreditCurve.length && null != csqs.creditCurve
				(aLSLCreditCurve[0]) && null != pricerParams) {
				bcmCreditRiskyDirty = new org.drip.analytics.output.BondCouponMeasures
					(dblCreditRiskyDirtyDV01, dblCreditRiskyDirtyIndexCouponPV, dblCreditRiskyDirtyCouponPV,
						dblCreditRiskyDirtyCouponPV + dblCreditRiskyPrincipalPV + dblCreditRiskyParPV);

				dblDefaultExposure = (dblDefaultExposureNoRec = notional (valParams.valueDate())) *
					csqs.creditCurve (aLSLCreditCurve[0]).recovery (valParams.valueDate());

				dblLossOnInstantaneousDefault = notional (valParams.valueDate()) * (1. - csqs.creditCurve
					(aLSLCreditCurve[0]).recovery (valParams.valueDate()));
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

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> standardRVMeasureMap (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.param.valuation.WorkoutInfo wi,
		final double dblPrice,
		final java.lang.String strPrefix)
	{
		if (null == strPrefix) return null;

		org.drip.analytics.output.BondRVMeasures bmRV = standardMeasures (valParams, pricerParams, csqs,
			quotingParams, wi, dblPrice);

		if (null == bmRV) return null;

		return bmRV.toMap (strPrefix);
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calcFairMeasureSet (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		org.drip.analytics.output.BondWorkoutMeasures bwmFair = calcBondWorkoutMeasures (valParams,
			pricerParams, csqs, maturity().julian(), 1.);

		if (null == bwmFair) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = bwmFair.toMap ("");

		double dblPrice = (null == bwmFair._bcmCreditRiskyClean || java.lang.Double.isNaN
			(bwmFair._bcmCreditRiskyClean._dblPV)) ? bwmFair._bcmCreditRisklessClean._dblPV :
				bwmFair._bcmCreditRiskyClean._dblPV;

		try {
			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, standardRVMeasureMap (valParams,
				pricerParams, csqs, quotingParams, new org.drip.param.valuation.WorkoutInfo
					(maturity().julian(), calcYieldFromPrice (valParams, csqs, quotingParams,
						dblPrice / notional (valParams.valueDate())), 1.,
							org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY), dblPrice, ""));

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures,
				org.drip.quant.common.CollectionUtil.PrefixKeys (mapMeasures, "Fair"));

			return mapMeasures;
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calcMarketMeasureSet (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.param.valuation.WorkoutInfo wiMarket)
	{
		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures =
				standardRVMeasureMap (valParams, pricerParams, csqs, quotingParams, wiMarket,
					calcPriceFromYield (valParams, csqs, quotingParams, wiMarket.date(),
						wiMarket.factor(), wiMarket.yield()), "");

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures,
				org.drip.quant.common.CollectionUtil.PrefixKeys (mapMeasures, "Market"));

			return mapMeasures;
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.cashflow.CompositePeriod calcCurrentPeriod (
		final double dblDate)
	{
		if (java.lang.Double.isNaN (dblDate)) return null;

		try {
			int iIndex = _periodParams.periodIndex (dblDate);

			return _periodParams.period (iIndex);
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		double dblExerciseFactor = 1.;
		double dblCleanPrice = java.lang.Double.NaN;

		double dblExerciseDate = maturity().julian();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalibMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		if (null != pricerParams.calibParams().workout()) {
			dblExerciseDate = pricerParams.calibParams().workout().date();

			dblExerciseFactor = pricerParams.calibParams().workout().factor();
		}

		org.drip.state.identifier.CreditLabel[] aLSLCreditCurve = creditLabel();

		org.drip.analytics.definition.CreditCurve cc = null == aLSLCreditCurve || 0 == aLSLCreditCurve.length
			? null : csqs.creditCurve (aLSLCreditCurve[0]);

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

		if (java.lang.Double.isNaN (dblCleanPrice)) return null;

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"CleanPrice", "FairCleanPrice", "FairPrice", "Price"}, false)) {
			mapCalibMeasures.put (pricerParams.calibParams().measure(), dblCleanPrice);

			return mapCalibMeasures;
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"DirtyPrice", "FairDirtyPrice"}, false)) {
			try {
				mapCalibMeasures.put (pricerParams.calibParams().measure(), dblCleanPrice + accrued
					(valParams.valueDate(), csqs));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"Yield", "FairYield"}, false)) {
			try {
				mapCalibMeasures.put (pricerParams.calibParams().measure(), calcYieldFromPrice (valParams,
					csqs, quotingParams, dblExerciseDate, dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"TSYSpread", "FairTSYSpread"}, false)) {
			try {
				mapCalibMeasures.put (pricerParams.calibParams().measure(), calcTSYSpreadFromPrice
					(valParams, csqs, quotingParams, dblExerciseDate, dblExerciseFactor,
						dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"OAS", "OASpread", "OptionAdjustedSpread"}, false)) {
			try {
				mapCalibMeasures.put (pricerParams.calibParams().measure(), calcOASFromPrice (valParams,
					csqs, quotingParams, dblExerciseDate, dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"BondBasis", "YieldBasis", "YieldSpread"}, false)) {
			try {
				mapCalibMeasures.put (pricerParams.calibParams().measure(), bondBasisFromPrice (valParams,
					csqs, quotingParams, dblExerciseDate, dblExerciseFactor, dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"CreditBasis"}, false)) {
			try {
				if (null == cc) return null;

				mapCalibMeasures.put (pricerParams.calibParams().measure(), calcCreditBasisFromPrice
					(valParams, csqs, quotingParams, dblExerciseDate, dblExerciseFactor,
						dblCleanPrice));

				return mapCalibMeasures;
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		if (org.drip.quant.common.StringUtil.MatchInStringArray (pricerParams.calibParams().measure(), new
			java.lang.String[] {"PECS", "ParEquivalentCDSSpread"}, false)) {
			try {
				if (null == cc) return null;

				mapCalibMeasures.put (pricerParams.calibParams().measure(), calcPECSFromPrice (valParams,
					csqs, quotingParams, dblExerciseDate, dblExerciseFactor, dblCleanPrice));

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
		if (null == valParams || null == csqs || null == csqs.quoteMap() || null == _tsyBmkSet ||
			null == _tsyBmkSet.getSecBmk())
			return null;

		double[] adblSecTSYSpread = new double[_tsyBmkSet.getSecBmk().length];

		for (int i = 0; i < _tsyBmkSet.getSecBmk().length; ++i) {
			adblSecTSYSpread[i] = java.lang.Double.NaN;
			org.drip.param.definition.ProductQuote cqTsyBmkYield = null;

			java.lang.String strTsyBmk = _tsyBmkSet.getSecBmk()[i];

			if (null != strTsyBmk && !strTsyBmk.isEmpty())
				cqTsyBmkYield = csqs.quoteMap().get (strTsyBmk);

			if (null != cqTsyBmkYield && null != cqTsyBmkYield.quote ("Yield"))
				adblSecTSYSpread[i] = cqTsyBmkYield.quote ("Yield").getQuote ("mid");
			else {
				org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
					(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

				if (null != dcGovvie) {
					try {
						adblSecTSYSpread[i] = dcGovvie.libor (valParams.valueDate(),
							_periodParams.maturity());
					} catch (java.lang.Exception e) {
						if (!s_bSuppressErrors) e.printStackTrace();
					}
				}
			}
		}

		return adblSecTSYSpread;
	}

	@Override public double effectiveTreasuryBenchmarkYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblPrice))
			throw new java.lang.Exception ("Bond.effectiveTreasuryBenchmarkYield: Bad val/mkt Params");

		java.lang.String strTsyBmk = null;
		org.drip.param.definition.ProductQuote cqTsyBmkYield = null;

		if (null != _tsyBmkSet) strTsyBmk = _tsyBmkSet.getPrimaryBmk();

		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("Bond.effectiveTreasuryBenchmarkYield: Cant do TSY wkout for px!");

		if (null == strTsyBmk || strTsyBmk.isEmpty())
			strTsyBmk = org.drip.analytics.support.AnalyticsHelper.BaseTsyBmk (valParams.valueDate(),
				wi.date());

		if (null != csqs.quoteMap() && null != strTsyBmk && !strTsyBmk.isEmpty())
			cqTsyBmkYield = csqs.quoteMap().get (strTsyBmk);

		if (null != cqTsyBmkYield && null != cqTsyBmkYield.quote ("Yield"))
			return cqTsyBmkYield.quote ("Yield").getQuote ("mid");

		org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

		return null == dcGovvie ? java.lang.Double.NaN : dcGovvie.libor (valParams.valueDate(), wi.date());
	}

	@Override public boolean setTreasuryBenchmark (
		final org.drip.product.params.TsyBmkSet tsyBmkSet)
	{
		if (null == (_tsyBmkSet = tsyBmkSet)) return false;

		return true;
	}

	@Override public org.drip.product.params.TsyBmkSet getTreasuryBenchmark()
	{
		return _tsyBmkSet;
	}

	@Override public boolean setIdentifierSet (
		final org.drip.product.params.IdentifierSet idParams)
	{
		if (null == (_idParams = idParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.IdentifierSet getIdentifierSet()
	{
		return _idParams;
	}

	@Override public boolean setCouponSetting (
		final org.drip.product.params.CouponSetting cpnParams)
	{
		if (null == (_cpnParams = cpnParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.CouponSetting getCouponSetting()
	{
		return _cpnParams;
	}

	@Override public boolean setCurrencySet (
		final org.drip.product.params.CurrencySet ccyParams)
	{
		if (null == (_ccyParams = ccyParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.CurrencySet getCurrencyParams()
	{
		return _ccyParams;
	}

	@Override public boolean setFloaterSetting (
		final org.drip.product.params.FloaterSetting fltParams)
	{
		if (null == (_fltParams = fltParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.FloaterSetting getFloaterSetting()
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
		if (null == (_mktConv = mktConv)) return false;

		return true;
	}

	@Override public org.drip.product.params.QuoteConvention getMarketConvention()
	{
		return _mktConv;
	}

	@Override public boolean setRatesSetting (
		final org.drip.product.params.RatesSetting irValParams)
	{
		if (null == (_irValParams = irValParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.RatesSetting setRatesSetting()
	{
		return _irValParams;
	}

	@Override public boolean setCreditSetting (
		final org.drip.product.params.CreditSetting crValParams)
	{
		if (null == (_crValParams = crValParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.CreditSetting getCreditSetting()
	{
		return _crValParams;
	}

	@Override public boolean setTerminationSetting (
		final org.drip.product.params.TerminationSetting cfteParams)
	{
		if (null == (_cfteParams = cfteParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.TerminationSetting getTerminationSetting()
	{
		return _cfteParams;
	}

	@Override public boolean setPeriodSet (
		final org.drip.product.params.PeriodSet periodParams)
	{
		if (null == (_periodParams = periodParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.PeriodSet getPeriodSet()
	{
		return _periodParams;
	}

	@Override public boolean setNotionalSetting (
		final org.drip.product.params.NotionalSetting notlParams)
	{
		if (null == (_notlParams = notlParams)) return false;

		return true;
	}

	@Override public org.drip.product.params.NotionalSetting getNotionalSetting()
	{
		return _notlParams;
	}

	@Override public java.lang.String primaryCode()
	{
		if (null == _idParams) return null;

		return "BOND." + _idParams._strID;
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
		if (null == _idParams) return null;

		return _idParams._strISIN;
	}

	@Override public java.lang.String cusip()
	{
		if (null == _idParams) return null;

		return _idParams._strCUSIP;
	}

	@Override public java.lang.String name()
	{
		if (null == _idParams) return null;

		return _idParams._strID;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		java.lang.String[] astrCouponCurrency = _ccyParams.couponCurrency();

		java.lang.String[] astrPrincipalCurrency = _ccyParams.principalCurrency();

		int iNumCouponCurrency = null == astrCouponCurrency ? 0 : astrCouponCurrency.length;
		int iNumPrincipalCurrency = null == astrPrincipalCurrency ? 0 : astrPrincipalCurrency.length;

		for (int i = 0; i < iNumCouponCurrency; ++i) {
			java.lang.String strCouponCurrency = astrCouponCurrency[i];

			if (null != strCouponCurrency && !strCouponCurrency.isEmpty()) setCcy.add (strCouponCurrency);
		}

		for (int i = 0; i < iNumPrincipalCurrency; ++i) {
			java.lang.String strPrincipalCurrency = astrPrincipalCurrency[i];

			if (null != strPrincipalCurrency && !strPrincipalCurrency.isEmpty())
				setCcy.add (strPrincipalCurrency);
		}

		return setCcy;
	}

	@Override public java.lang.String[] payCurrency()
	{
		return _ccyParams.couponCurrency();
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return _ccyParams.principalCurrency();
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlParams || null == _notlParams._fsPrincipalOutstanding || java.lang.Double.isNaN
			(dblDate))
			throw new java.lang.Exception ("Bond::notional => Bad state/inputs");

		return _notlParams._fsPrincipalOutstanding.factor (dblDate);
	}

	@Override public double notional (
		final double dblDateStart,
		final double dblDateEnd)
		throws java.lang.Exception
	{
		if (null == _notlParams || null == _notlParams._fsPrincipalOutstanding || java.lang.Double.isNaN
			(dblDateStart) || java.lang.Double.isNaN (dblDateEnd))
			throw new java.lang.Exception ("Bond::notional => Bad state/inputs");

		return _notlParams._fsPrincipalOutstanding.factor (dblDateStart, dblDateEnd);
	}

	@Override public double initialNotional()
		throws java.lang.Exception
	{
		if (null == _notlParams) throw new java.lang.Exception ("Bond::initialNotional => Bad state/inputs");

		return _notlParams._dblNotional;
	}

	@Override public double getRecovery (
		final double dblDate,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (java.lang.Double.isNaN (dblDate) || null == cc)
			throw new java.lang.Exception ("Bond.getRecovery: Bad state/inputs");

		return _crValParams._bUseCurveRec ? cc.recovery (dblDate) : _crValParams._dblRecovery;
	}

	@Override public double getRecovery (
		final double dblDateStart,
		final double dblDateEnd,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (java.lang.Double.isNaN (dblDateStart) || java.lang.Double.isNaN (dblDateEnd) || null == cc)
			throw new java.lang.Exception ("Bond.getRecovery: Bad state/inputs");

		return _crValParams._bUseCurveRec ? cc.effectiveRecovery (dblDateStart, dblDateEnd) :
			_crValParams._dblRecovery;
	}

	@Override public org.drip.product.params.CreditSetting getCRValParams()
	{
		return _crValParams;
	}

	@Override public org.drip.analytics.output.CompositePeriodCouponMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		try {
			org.drip.analytics.cashflow.CompositePeriod period = calcCurrentPeriod (dblAccrualEndDate);

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
		return cashFlowPeriod().get (0).freq();
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		if (null == _crValParams || null == _crValParams._strCC || _crValParams._strCC.isEmpty())
			return null;

		return new org.drip.state.identifier.CreditLabel[] {org.drip.state.identifier.CreditLabel.Standard
			(_crValParams._strCC)};
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		if (null == _fltParams) return null;

		return new org.drip.state.identifier.ForwardLabel[] {_fltParams._fri};
	}

	@Override public org.drip.state.identifier.FundingLabel[] fundingLabel()
	{
		return new org.drip.state.identifier.FundingLabel[] {org.drip.state.identifier.FundingLabel.Standard
			(_irValParams._strCouponDiscountCurve)};
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_periodParams.effective());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_periodParams.maturity());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_periodParams.periods().get (0).endDate());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.CompositePeriod> cashFlowPeriod()
	{
		if (null == _periodParams) return null;

		return _periodParams.periods();
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _mktConv._settleParams;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> getLossFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == valParams || null == pricerParams || null == csqs) return null;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
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
		if (null == valParams || null == pricerParams || null == csqs || java.lang.Double.isNaN
			(dblPrice))
			return null;

		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs, vcp, dblPrice);

		if (null == wi) return null;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (null == period || period.endDate() < valParams.valueDate()) continue;

			if (period.startDate() > wi.date()) break;

			java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLPSub = period.lossMetrics
				(this, valParams, pricerParams, period.endDate(), csqs);

			if (null != sLPSub) sLP.addAll (sLPSub);
		}

		return sLP;
	}

	@Override public boolean isFloater()
	{
		if (null == _fltParams) return false;

		return true;
	}

	@Override public java.lang.String rateIndex()
	{
		if (null == _fltParams) return "";

		return _fltParams._fri.fullyQualifiedName();
	}

	@Override public double currentCoupon()
	{
		if (null == _fltParams) return java.lang.Double.NaN;

		return _fltParams._dblCurrentCoupon;
	}

	@Override public double floatSpread()
	{
		if (null == _fltParams) return java.lang.Double.NaN;

		return _fltParams._dblFloatSpread;
	}

	@Override public java.lang.String ticker()
	{
		if (null == _idParams) return null;

		return _idParams._strTicker;
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
		if (null == _notlParams) return false;

		return true;
	}

	@Override public boolean variableCoupon()
	{
		if (null == _cpnParams || null == _cpnParams._strCouponType || !"variable".equalsIgnoreCase
			(_cpnParams._strCouponType))
			return false;

		return true;
	}

	@Override public boolean exercised()
	{
		if (null == _cfteParams) return false;

		return _cfteParams._bHasBeenExercised;
	}

	@Override public boolean defaulted()
	{
		if (null == _cfteParams) return false;

		return _cfteParams._bIsDefaulted;
	}

	@Override public boolean perpetual()
	{
		if (null == _cfteParams) return false;

		return _cfteParams._bIsPerpetual;
	}

	@Override public boolean tradeable (
		final org.drip.param.valuation.ValuationParams valParams)
		throws java.lang.Exception
	{
		if (null == valParams)
			throw new java.lang.Exception
				("BondComponent::isTradable => Null valParams in BondComponent::tradeable!");

		return !_cfteParams._bHasBeenExercised && !_cfteParams._bIsDefaulted && valParams.valueDate() <
			_periodParams.maturity();
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
		if (null == _cpnParams) return "";

		return _cpnParams._strCouponType;
	}

	@Override public java.lang.String couponDC()
	{
		if (null == _periodParams) return "";

		return _periodParams.couponDC();
	}

	@Override public java.lang.String accrualDC()
	{
		if (null == _periodParams) return "";

		return _periodParams.accrualDC();
	}

	@Override public java.lang.String maturityType()
	{
		if (null == _periodParams) return "";

		return _periodParams.maturityType();
	}

	@Override public org.drip.analytics.date.JulianDate finalMaturity()
	{
		if (null == _periodParams) return null;

		try {
			return new org.drip.analytics.date.JulianDate (_periodParams.finalMaturity());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String calculationType()
	{
		if (null == _mktConv) return "";

		return _mktConv._strCalculationType;
	}

	@Override public double redemptionValue()
	{
		if (null == _mktConv) return java.lang.Double.NaN;

		return _mktConv._dblRedemptionValue;
	}

	@Override public java.lang.String couponCurrency()
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
		if (java.lang.Double.isNaN (dblDate))
			throw new java.lang.Exception ("BondComponent::inFirstCouponPeriod => Input date is NaN");

		return _periodParams.firstPeriod().contains (dblDate);
	}

	@Override public boolean inLastCouponPeriod (
		final double dblDate)
		throws java.lang.Exception
	{
		if (java.lang.Double.isNaN (dblDate))
			throw new java.lang.Exception ("BondComponent::inLastCouponPeriod => Input date is NaN");

		return _periodParams.lastPeriod().contains (dblDate);
	}

	@Override public java.lang.String floatCouponConvention()
	{
		if (null == _fltParams) return "";

		return _fltParams._strFloatDayCount;
	}

	@Override public org.drip.analytics.date.JulianDate periodFixingDate (
		final double dblValue)
	{
		if (null == _fltParams || java.lang.Double.isNaN (dblValue) || dblValue >=
			_periodParams.maturity())
			return null;

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < dblValue) continue;

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
			int iIndex = _periodParams.periodIndex (dt.julian());

			if (0 == iIndex) return null;
			
			org.drip.analytics.cashflow.CompositePeriod period = _periodParams.period (iIndex - 1);

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
			throw new java.lang.Exception ("BondComponent::previousCouponRate => Null val/mkt params!");

		int iIndex = _periodParams.periodIndex (dt.julian());

		org.drip.analytics.cashflow.CompositePeriod period = _periodParams.period (iIndex - 1);

		if (null == period)
			throw new java.lang.Exception
				("BondComponent::previousCouponRate => Cannot find previous period!");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (period.endDate(), new
			org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

		if (null == pcm)
			throw new java.lang.Exception
				("BondComponent::previousCouponRate => Cannot find previous period!");

		return pcm.rate();
	}

	@Override public org.drip.analytics.date.JulianDate currentCouponDate (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		try {
			int iIndex = _periodParams.periodIndex (dt.julian());
			
			org.drip.analytics.cashflow.CompositePeriod period = _periodParams.period (iIndex);

			if (null == period) return null;

			return new org.drip.analytics.date.JulianDate (period.payDate());
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate nextCouponDate (
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		try {
			int iIndex = _periodParams.periodIndex (dt.julian());
			
			org.drip.analytics.cashflow.CompositePeriod period = _periodParams.period (iIndex + 1);

			if (null == period) return null;

			return new org.drip.analytics.date.JulianDate (period.payDate());
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	@Override public org.drip.analytics.output.ExerciseInfo nextValidExerciseDateOfType (
		final org.drip.analytics.date.JulianDate dt,
		final boolean bGetPut)
	{
		if (null == dt || (bGetPut && null == _eosPut) || (!bGetPut && null == _eosCall)) return null;

		double[] adblEOSExerciseDates = null;
		org.drip.product.params.EmbeddedOptionSchedule eos = null;

		if (bGetPut)
			adblEOSExerciseDates = (eos = _eosPut).getDates();
		else
			adblEOSExerciseDates = (eos = _eosCall).getDates();

		if (null == eos || null == adblEOSExerciseDates || 0 == adblEOSExerciseDates.length) return null;

		for (int i = 0; i < adblEOSExerciseDates.length; ++i) {
			if (dt.julian() > adblEOSExerciseDates[i] + LEFT_EOS_SNIP || adblEOSExerciseDates[i] -
				dt.julian() < eos.getExerciseNoticePeriod())
				continue;

			try {
				return new org.drip.analytics.output.ExerciseInfo (adblEOSExerciseDates[i], eos.getFactor
					(i), bGetPut ? org.drip.param.valuation.WorkoutInfo.WO_TYPE_PUT :
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
				return new org.drip.analytics.output.ExerciseInfo (maturity().julian(), 1.,
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

		if (!java.lang.Double.isNaN (_fltParams._dblCurrentCoupon)) return _fltParams._dblCurrentCoupon;

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (dt.julian(), new
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
			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (dt.julian(), new
				org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

			if (null == pcm)
				throw new java.lang.Exception ("BondComponent::nextCouponRate => Null PCM!");

			return pcm.rate();
		}

		int iIndex = _periodParams.periodIndex (dt.julian());

		org.drip.analytics.cashflow.CompositePeriod period = _periodParams.period (iIndex + 1);

		if (null == period)
			throw new java.lang.Exception ("BondComponent::nextCouponRate => Cannot find next period!");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (period.endDate(), new
			org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::nextCouponRate => Null PCM!");

		return pcm.rate();
	}

	@Override public double accrued (
		final double dblDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (java.lang.Double.isNaN (dblDate) || null == csqs)
			throw new java.lang.Exception ("BondComponent::accrued => Invalid inputs");

		org.drip.analytics.date.JulianDate dt = new org.drip.analytics.date.JulianDate (dblDate);

		if (dblDate >= _periodParams.maturity())
			throw new java.lang.Exception ("BondComponent::accrued => Val date " + dt +
				" greater than maturity " + org.drip.analytics.date.JulianDate.fromJulian
					(_periodParams.maturity()));

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < dblDate) continue;

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (period.endDate(), new
				org.drip.param.valuation.ValuationParams (dt, dt, ""), csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::accrued => No PCM");

			double dblCoupon = pcm.rate();

			if (java.lang.Double.isNaN (dblCoupon)) return java.lang.Double.NaN;

			if (period.startDate() < dblDate && period.endDate() >= dblDate) {
				double dblAccrued = period.accrualDCF (dblDate) * dblCoupon * notional (period.startDate());

				if (s_bBlog) {
					System.out.println ("Accrued DCF: " + (int) (period.accrualDCF (dblDate) * 366. + 0.5));

					System.out.println ("Accrued: " + dblAccrued);
				}

				return dblAccrued;
			}

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
		if (null == valParams || valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP|| null == csqs ||
			java.lang.Double.isNaN (dblWorkoutDate) || java.lang.Double.isNaN (dblWorkoutFactor) ||
				java.lang.Double.isNaN (dblZCBump))
			throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => Invalid Inputs");

		double dblPVFromZC = 0.;
		boolean bTerminateCouponFlow = false;
		double dblCashPayDate = java.lang.Double.NaN;
		double dblScalingNotional = java.lang.Double.NaN;
		org.drip.analytics.rates.ZeroCurve zc = null;
		org.drip.analytics.rates.DiscountCurve dcBase = null;

		if (ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE == iZeroCurveBaseDC)
			dcBase = csqs.fundingCurve (fundingLabel()[0]);
		else if (ZERO_OFF_OF_TREASURIES_DISCOUNT_CURVE == iZeroCurveBaseDC)
			dcBase = csqs.govvieCurve (org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

		if (null == dcBase)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => Invalid discount curve");

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		if (null != _notlParams && _notlParams._bPriceOffOriginalNotional) dblScalingNotional = 1.;

		try {
			zc = org.drip.state.creator.ZeroCurveBuilder.CreateZeroCurve (_periodParams.freq(),
				_periodParams.couponDC(), couponCurrency(), _periodParams.couponEOMAdjustment(),
					_periodParams.periods(), dblWorkoutDate, dblCashPayDate, dcBase, null == vcp ? (null ==
						_mktConv ? null : _mktConv._quotingParams) : vcp, dblZCBump);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		if (null == zc)
			throw new java.lang.Exception ("Cannot create shifted ZC in BondComponent::priceFromBumpedZC");

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < valParams.valueDate()) continue;

			if (java.lang.Double.isNaN (dblScalingNotional))
				dblScalingNotional = notional (period.startDate());

			double dblAccrualEndDate = period.endDate();

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (valParams.valueDate(),
				valParams, csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromBumpedZC => No PCM");

			double dblZCDF = zc.df (period.payDate());

			double dblCouponNotional = notional (period.startDate());

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (period.startDate(), dblNotionalEndDate);

			dblPVFromZC += period.accrualDCF (dblAccrualEndDate) * dblZCDF * pcm.rate() * dblCouponNotional;

			dblPVFromZC += (notional (period.startDate()) - notional (dblNotionalEndDate)) * dblZCDF;

			if (bTerminateCouponFlow) break;
		}

		return ((dblPVFromZC + dblWorkoutFactor * zc.df (dblWorkoutDate) * notional (dblWorkoutDate)) /
			zc.df (dblCashPayDate) - accrued (valParams.valueDate(), csqs)) / dblScalingNotional;
	}

	@Override public double priceFromBumpedDC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDCBump)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblWorkoutDate) ||
			java.lang.Double.isNaN (dblWorkoutFactor) || java.lang.Double.isNaN (dblDCBump))
			throw new java.lang.Exception ("BondComponent::priceFromBumpedDC => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception
				("BondComponent::calcPriceFromBumpedDC => Cannot locate funding curve");

		if (valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("Val date " + org.drip.analytics.date.JulianDate.fromJulian
				(valParams.valueDate()) + " greater than Work-out " +
					org.drip.analytics.date.JulianDate.fromJulian (dblWorkoutDate));

		double dblPVFromDC = 0.;
		boolean bTerminateCouponFlow = false;
		double dblScalingNotional = java.lang.Double.NaN;

		if (null != _notlParams && _notlParams._bPriceOffOriginalNotional) dblScalingNotional = 1.;

		if (0. != dblDCBump)
			dcFunding = (org.drip.analytics.rates.DiscountCurve) dcFunding.parallelShiftManifestMeasure
				("Rate", dblDCBump);

		if (null == dcFunding)
			throw new java.lang.Exception
				("Cannot create shifted DC in BondComponent::calcPriceFromBumpedDC");

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < valParams.valueDate()) continue;

			if (java.lang.Double.isNaN (dblScalingNotional)) dblScalingNotional = notional
				(period.startDate());

			double dblAccrualEndDate = period.endDate();

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (period.endDate(), valParams,
				csqs);

			if (null == pcm)
				throw new java.lang.Exception ("BondComponent::calcPriceFromBumpedDC => No PCM");

			double dblDF = dcFunding.df (period.payDate());

			double dblCouponNotional = notional (period.startDate());

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (period.startDate(), dblNotionalEndDate);

			dblPVFromDC += period.accrualDCF (dblAccrualEndDate) * dblDF * pcm.rate() * dblCouponNotional;

			dblPVFromDC += (notional (period.startDate()) - notional (dblNotionalEndDate)) * dblDF;

			if (bTerminateCouponFlow) break;
		}

		double dblCashPayDate = java.lang.Double.NaN;

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		return ((dblPVFromDC + dblWorkoutFactor * dcFunding.df (dblWorkoutDate) * notional (dblWorkoutDate))
			/ dcFunding.df (dblCashPayDate) - accrued (valParams.valueDate(), csqs)) / dblScalingNotional;
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
		if (null == valParams || null == csqs || java.lang.Double.isNaN (dblWorkoutDate) ||
			java.lang.Double.isNaN (dblWorkoutFactor) || java.lang.Double.isNaN (dblCreditBasis))
			throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => Invalid inputs");

		org.drip.state.identifier.CreditLabel[] aLSLCreditCurve = creditLabel();

		org.drip.analytics.definition.CreditCurve ccIn = null == aLSLCreditCurve || 0 ==
			aLSLCreditCurve.length ? null : csqs.creditCurve (aLSLCreditCurve[0]);

		if (null == ccIn)
			throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception
				("BondComponent::priceFromBumpedCC => Cannot locate funding curve");

		if (valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("Val date " + org.drip.analytics.date.JulianDate.fromJulian
				(valParams.valueDate()) + " greater than Work-out " +
					org.drip.analytics.date.JulianDate.fromJulian (dblWorkoutDate));

		org.drip.analytics.definition.CreditCurve cc = null;

		if (bFlat) {
			double dblRecoveryCalib = java.lang.Double.NaN;

			if (null != _crValParams && !_crValParams._bUseCurveRec)
				dblRecoveryCalib = _crValParams._dblRecovery;

			cc = ccIn.flatCurve (dblCreditBasis, true, dblRecoveryCalib);
		} else
			cc = (org.drip.analytics.definition.CreditCurve) ccIn.parallelShiftManifestMeasure
				("FairPremium", dblCreditBasis);

		if (null == cc)
			throw new java.lang.Exception
				("BondComponent::priceFromBumpedCC => Cannot create adjusted Curve");

		double dblPVFromCC = 0.;

		org.drip.param.pricer.PricerParams pricerParams = new org.drip.param.pricer.PricerParams (7, null,
			false, s_iDiscretizationScheme, false);

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < valParams.valueDate()) continue;

			double dblAccrualEndDate = period.endDate();

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			double dblPeriodStart = period.startDate();

			if (dblPeriodStart < valParams.valueDate()) dblPeriodStart = valParams.valueDate();

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (period.endDate(), valParams,
				csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::priceFromBumpedCC => No PCM");

			double dblPeriodCoupon = pcm.rate();

			double dblPeriodEndSurv = cc.survival (period.endDate());

			double dblCouponNotional = notional (period.startDate());

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (period.startDate(), dblNotionalEndDate);

			dblPVFromCC += period.accrualDCF (dblAccrualEndDate) * dcFunding.df (period.payDate()) *
				dblPeriodEndSurv * dblPeriodCoupon * dblCouponNotional;

			dblPVFromCC += (notional (period.startDate()) - notional (period.endDate())) * dcFunding.df
				(period.payDate()) * dblPeriodEndSurv;

			if (s_bBlog)
				System.out.println (org.drip.analytics.date.JulianDate.fromJulian (dblPeriodStart) + "=>" +
					org.drip.analytics.date.JulianDate.fromJulian (period.endDate()) + ": " +
						org.drip.quant.common.FormatUtil.FormatDouble (dblPVFromCC, 1, 3, 100.));

			for (org.drip.analytics.cashflow.LossQuadratureMetrics lp : period.lossMetrics (this, valParams,
				pricerParams, period.endDate(), csqs)) {
				if (null == lp) continue;

				double dblSubPeriodEnd = lp.end();

				double dblSubPeriodStart = lp.start();

				double dblSubPeriodDF = dcFunding.effectiveDF (dblSubPeriodStart + _crValParams._iDefPayLag,
					dblSubPeriodEnd + _crValParams._iDefPayLag);

				double dblSubPeriodNotional = notional (dblSubPeriodStart, dblSubPeriodEnd);

				double dblSubPeriodSurvival = cc.survival (dblSubPeriodStart) - cc.survival
					(dblSubPeriodEnd);

				if (_crValParams._bAccrOnDefault)
					dblPVFromCC += 0.0001 * lp.accrualDCF() * dblSubPeriodSurvival * dblSubPeriodDF *
						dblSubPeriodNotional * dblPeriodCoupon;

				double dblRec = _crValParams._bUseCurveRec ? cc.effectiveRecovery (dblSubPeriodStart,
					dblSubPeriodEnd) : _crValParams._dblRecovery;

				dblPVFromCC += dblRec * dblSubPeriodSurvival * dblSubPeriodNotional * dblSubPeriodDF;

				if (s_bBlog)
					System.out.println ("\t" + org.drip.analytics.date.JulianDate.fromJulian (lp.start()) +
						"=>" + org.drip.analytics.date.JulianDate.fromJulian (lp.end()) + ": " +
							org.drip.quant.common.FormatUtil.FormatDouble (dblPVFromCC, 1, 3, 100.));
			}
		}

		double dblCashPayDate = java.lang.Double.NaN;

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		double dblScalingNotional = 1.;

		if (!_notlParams._bPriceOffOriginalNotional) dblScalingNotional = notional (dblWorkoutDate);

		return ((dblPVFromCC + dblWorkoutFactor * dcFunding.df (dblWorkoutDate) * cc.survival
			(dblWorkoutDate) * notional (dblWorkoutDate)) / dcFunding.df (dblCashPayDate) - accrued
				(valParams.valueDate(), csqs)) / dblScalingNotional;
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double aswFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return aswFromBondBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblBondBasis);
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

		return aswFromBondBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblBondBasis);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double aswFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return aswFromCreditBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblCreditBasis);
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

		return aswFromCreditBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblCreditBasis);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double aswFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return aswFromDiscountMargin (valParams, csqs, vcp, _periodParams.maturity(), 1., dblDiscountMargin);
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

		return aswFromDiscountMargin (valParams, csqs, vcp, _periodParams.maturity(), 1., dblDiscountMargin);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromGSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double aswFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return aswFromGSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblGSpread);
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

		return aswFromGSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblGSpread);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromISpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double aswFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return aswFromISpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblISpread);
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

		return aswFromISpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblISpread);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromOAS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double aswFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return aswFromOAS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblOAS);
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

		return aswFromOAS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblOAS);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromPECS
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double aswFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return aswFromPECS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblPECS);
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

		return aswFromPECS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblPECS);
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
				!org.drip.quant.common.NumberUtil.IsValid (dblPrice) || valParams.valueDate() >=
					dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::aswFromPrice => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::aswFromPrice => Invalid Inputs");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (valParams.valueDate(),
			valParams, csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::aswFromPrice => No PCM");

		return pcm.rate() - dcFunding.estimateManifestMeasure ("Rate", dblWorkoutDate) + 0.01 *
			(dblWorkoutFactor - dblPrice) / dcFunding.liborDV01 (dblWorkoutDate);
	}

	@Override public double aswFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return aswFromPrice (valParams, csqs, vcp, _periodParams.maturity(), 1., dblPrice);
	}

	@Override public double aswFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::aswFromPriceToOptimalExercise => Can't determine Work-out");

		return aswFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromTSYSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double aswFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return aswFromTSYSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblTSYSpread);
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

		return aswFromTSYSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblTSYSpread);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromYield
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYield));
	}

	@Override public double aswFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return aswFromYield (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYield);
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

		return aswFromYield (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYield);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromYieldSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double aswFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return aswFromYieldSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYieldSpread);
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

		return aswFromYieldSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYieldSpread);
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
		return aswFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, calcPriceFromZSpread
			(valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double aswFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return aswFromZSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblZSpread);
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

		return aswFromZSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblZSpread);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromASW (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double bondBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return bondBasisFromASW (valParams, csqs, vcp, _periodParams.maturity(), 1., dblASW);
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

		return bondBasisFromASW (valParams, csqs, vcp, _periodParams.maturity(), 1., dblASW);
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
			calcYieldFromCreditBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double bondBasisFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return bondBasisFromCreditBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblCreditBasis);
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

		return bondBasisFromCreditBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblCreditBasis);
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
			calcYieldFromDiscountMargin (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double bondBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return bondBasisFromDiscountMargin (valParams, csqs, vcp, _periodParams.maturity(), 1.,
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

		return bondBasisFromDiscountMargin (valParams, csqs, vcp, _periodParams.maturity(), 1.,
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromGSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblGSpread));
	}

	@Override public double bondBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return bondBasisFromGSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblGSpread);
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

		return bondBasisFromGSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblGSpread);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromISpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblISpread));
	}

	@Override public double bondBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblISpread)
		throws java.lang.Exception
	{
		return bondBasisFromISpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblISpread);
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

		return bondBasisFromISpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblISpread);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromOAS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblOAS));
	}

	@Override public double bondBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblOAS)
		throws java.lang.Exception
	{
		return bondBasisFromOAS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblOAS);
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

		return bondBasisFromOAS (valParams, csqs, vcp, _periodParams.maturity(), 1.,
			dblOAS);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPECS (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPECS));
	}

	@Override public double bondBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPECS)
		throws java.lang.Exception
	{
		return bondBasisFromPECS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblPECS);
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

		return bondBasisFromPECS (valParams, csqs, vcp, _periodParams.maturity(), 1., dblPECS);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblPrice));
	}

	@Override public double bondBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblPrice)
		throws java.lang.Exception
	{
		return bondBasisFromPrice (valParams, csqs, vcp, _periodParams.maturity(), 1., dblPrice);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromTSYSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double bondBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return bondBasisFromTSYSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblTSYSpread);
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

		return bondBasisFromTSYSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblTSYSpread);
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

		return dblYield - calcYieldFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.));
	}

	@Override public double bondBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYield)
		throws java.lang.Exception
	{
		return bondBasisFromYield (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYield);
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

		return bondBasisFromYield (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYield);
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
			calcYieldFromYieldSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double bondBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return bondBasisFromYieldSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYieldSpread);
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

		return bondBasisFromYieldSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblYieldSpread);
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
		return bondBasisFromYield (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromZSpread (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double bondBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return bondBasisFromZSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblZSpread);
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

		return bondBasisFromZSpread (valParams, csqs, vcp, _periodParams.maturity(), 1., dblZSpread);
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
		return calcConvexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblASW));
	}

	@Override public double convexityFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblASW)
		throws java.lang.Exception
	{
		return convexityFromASW (valParams, csqs, vcp, _periodParams.maturity(), 1., dblASW);
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

		return convexityFromASW (valParams, csqs, vcp, _periodParams.maturity(), 1., dblASW);
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
		return calcConvexityFromPrice (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromBondBasis (valParams, csqs, vcp, dblWorkoutDate, dblWorkoutFactor, dblBondBasis));
	}

	@Override public double convexityFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return convexityFromBondBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblBondBasis);
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

		return convexityFromBondBasis (valParams, csqs, vcp, _periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcConvexityFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcConvexityFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcConvexityFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcConvexityFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromCreditBasisToOptimalExercise => "
				+ "Cant calc Convexity from Credit Basis to optimal exercise for bonds w emb option");

		return calcConvexityFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcConvexityFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double calcConvexityFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcConvexityFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcConvexityFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromDiscountMarginToOptimalExercise "
				+ "=> Cant calc Convexity from Discount Margin to optimal exercise for bonds w emb option");

		return calcDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcConvexityFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcConvexityFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcConvexityFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromGSpreadToOptimalExercise => " +
				"Cant calc Convexity from G Spread to optimal exercise for bonds w emb option");

		return calcConvexityFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcConvexityFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcConvexityFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcConvexityFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcConvexityFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromISpreadToOptimalExercise => " +
				"Cant calc Convexity from I Spread to optimal exercise for bonds w emb option");

		return calcConvexityFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcConvexityFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcConvexityFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcConvexityFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcConvexityFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromOASToOptimalExercise => " +
				"Cant calc Convexity from OAS to optimal exercise for bonds w emb option");

		return calcConvexityFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcConvexityFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcConvexityFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcConvexityFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcConvexityFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromPECSToOptimalExercise => " +
				"Cant calc Convexity from PECS to optimal exercise for bonds w emb option");

		return calcConvexityFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcConvexityFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP ||
			!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::calcConvexityFromPrice => Input inputs");

		double dblPriceForYieldMinus1bp = calcPriceFromYield (valParams, csqs, quotingParams,
			dblWorkoutDate, dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams,
				dblWorkoutDate, dblWorkoutFactor, dblPrice) - 0.0001);

		double dblPriceForYieldPlus1bp = calcPriceFromYield (valParams, csqs, quotingParams,
			dblWorkoutDate, dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams,
				dblWorkoutDate, dblWorkoutFactor, dblPrice) + 0.0001);

		double dblDirtyPrice = dblPrice + accrued (valParams.valueDate(), csqs);

		return (dblPriceForYieldMinus1bp + dblPriceForYieldPlus1bp - 2. * dblPrice) / dblDirtyPrice;
	}

	@Override public double calcConvexityFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcConvexityFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromPriceToOptimalExercise => " +
				"Cant calc Convexity from Price to optimal exercise for bonds w emb option");

		return calcConvexityFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcConvexityFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcConvexityFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblTSYSpread);
	}

	@Override public double calcConvexityFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromTSYSpreadToOptimalExercise => " +
				"Cant calc Convexity from TSY Sprd to optimal exercise for bonds w emb option");

		return calcConvexityFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblTSYSpread);
	}

	@Override public double calcConvexityFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYield));
	}

	@Override public double calcConvexityFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcConvexityFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcConvexityFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromYieldToOptimalExercise => " +
				"Cant calc Convexity from Yield to optimal exercise for bonds w emb option");

		return calcConvexityFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcConvexityFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcConvexityFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcConvexityFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromYieldSpreadToOptimalExercise => "
				+ "Cant calc Convexity from Yld Sprd to optimal exercise for bonds w emb option");

		return calcConvexityFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcConvexityFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcConvexityFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcConvexityFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcConvexityFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcConvexityFromZSpreadToOptimalExercise => " +
				"Cant calc Convexity from Z Spread to optimal exercise for bonds w emb option");

		return calcConvexityFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcCreditBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblASW));
	}

	@Override public double calcCreditBasisFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcCreditBasisFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcCreditBasisFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromASWToOptimalExercise => " +
				"Cannot calc Credit Basis from ASW to optimal exercise for bonds w emb option");

		return calcCreditBasisFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcCreditBasisFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblBondBasis));
	}

	@Override public double calcCreditBasisFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcCreditBasisFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcCreditBasisFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromBondBasisToOptimalExercise " +
				"=> Cant calc Credit Basis from Bond Basis to optimal exercise for bonds w emb option");

		return calcCreditBasisFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcCreditBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromDiscountMargin (valParams, csqs, quotingParams,
				dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcCreditBasisFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcCreditBasisFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcCreditBasisFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcCreditBasisFromDiscountMarginToOptimalExercise => " +
					"Cant calc Credit Basis from Discnt Margin to optimal exercise for bonds w emb option");

		return calcCreditBasisFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcCreditBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblGSpread));
	}

	@Override public double calcCreditBasisFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblGSpread);
	}

	@Override public double calcCreditBasisFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromGSpreadToOptimalExercise => " +
				"Cant calc Credit Basis from G Spread to optimal exercise for bonds w emb option");

		return calcCreditBasisFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblGSpread);
	}

	@Override public double calcCreditBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblISpread));
	}

	@Override public double calcCreditBasisFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblISpread);
	}

	@Override public double calcCreditBasisFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromISpreadToOptimalExercise => " +
				"Cant calc Credit Basis from I Spread to optimal exercise for bonds w emb option");

		return calcCreditBasisFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblISpread);
	}

	@Override public double calcCreditBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblOAS));
	}

	@Override public double calcCreditBasisFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcCreditBasisFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcCreditBasisFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromOASToOptimalExercise => " +
				"Cant calc Credit Basis from OAS to optimal exercise for bonds w emb option");

		return calcCreditBasisFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcCreditBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromPECS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPECS));
	}

	@Override public double calcCreditBasisFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcCreditBasisFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromPECSToOptimalExercise => " +
				"Cant calc Credit Basis from PECS to optimal exercise for bonds w emb option");

		return calcCreditBasisFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcCreditBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateCreditBasisFromPrice (valParams, csqs, dblWorkoutDate,
			dblWorkoutFactor, dblPrice, false);
	}

	@Override public double calcCreditBasisFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcCreditBasisFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcCreditBasisFromPriceToOptimalExercise => cant calc Work-out");

		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
			dblPrice);
	}

	@Override public double calcCreditBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double calcCreditBasisFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblTSYSpread);
	}

	@Override public double calcCreditBasisFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromTSYSpreadToOptimalExercise => "
				+ "Cant calc Credit Basis from TSY Spread to optimal exercise for bonds w emb option");

		return calcCreditBasisFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblTSYSpread);
	}

	@Override public double calcCreditBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYield));
	}

	@Override public double calcCreditBasisFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcCreditBasisFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcCreditBasisFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromYieldToOptimalExercise => " +
				"Cant calc Credit Basis from Yield to optimal exercise for bonds w emb option");

		return calcCreditBasisFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcCreditBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double calcCreditBasisFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcCreditBasisFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws	java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromYieldSpreadToOptimalExercise "
				+ "=> Cant calc Credit Basis from Yield Spread to optimal exercise for bonds w emb option");

		return calcCreditBasisFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcCreditBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblZSpread));
	}

	@Override public double calcCreditBasisFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcCreditBasisFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblZSpread);
	}

	@Override public double calcCreditBasisFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcCreditBasisFromZSpreadToOptimalExercise => " +
				"Cant calc Credit Basis from Z Spread to optimal exercise for bonds w emb option");

		return calcCreditBasisFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblZSpread);
	}

	@Override public double calcDiscountMarginFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromASW (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblASW));
	}

	@Override public double calcDiscountMarginFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromASW (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblASW);
	}

	@Override public double calcDiscountMarginFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromASWToOptimalExercise => " +
				"Cant calc Discount Margin from ASW to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromASW (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblASW);
	}

	@Override public double calcDiscountMarginFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblBondBasis));
	}

	@Override public double calcDiscountMarginFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromBondBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcDiscountMarginFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromBondBasisToOptimalExercise "
				+ "=> Cant calc Discount Margin from Bond Basis to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromBondBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcDiscountMarginFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double calcDiscountMarginFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcDiscountMarginFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcDiscountMarginFromCreditBasisToOptimalExercise => " +
					"Cant calc Discount Margin from Crdit Basis to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcDiscountMarginFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblGSpread));
	}

	@Override public double calcDiscountMarginFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromGSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblGSpread);
	}

	@Override public double calcDiscountMarginFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromGSpreadToOptimalExercise =>"
				+ " => Cant calc Discount Margin from G Spread to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromGSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblGSpread);
	}

	@Override public double calcDiscountMarginFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblISpread));
	}

	@Override public double calcDiscountMarginFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromISpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblISpread);
	}

	@Override public double calcDiscountMarginFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromISpreadToOptimalExercise " +
				"=> Cant calc Discount Margin from I Spread to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromISpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblISpread);
	}

	@Override public double calcDiscountMarginFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblOAS));
	}

	@Override public double calcDiscountMarginFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblOAS);
	}

	@Override public double calcDiscountMarginFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromOASToOptimalExercise => " +
				"Cant calc Discount Margin from OAS to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblOAS);
	}

	@Override public double calcDiscountMarginFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPECS));
	}

	@Override public double calcDiscountMarginFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPECS);
	}

	@Override public double calcDiscountMarginFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromPECSToOptimalExercise => " +
				"Cant calc Discount Margin from PECS to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPECS);
	}

	@Override public double calcDiscountMarginFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPrice));
	}

	@Override public double calcDiscountMarginFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPrice);
	}

	@Override public double calcDiscountMarginFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcDiscountMarginFromPriceToOptimalExercise => Can't do Work-out");

		return calcDiscountMarginFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
			dblPrice);
	}

	@Override public double calcDiscountMarginFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double calcDiscountMarginFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromTSYSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblTSYSpread);
	}

	@Override public double calcDiscountMarginFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromTSYSpreadToOptimalExercise "
				+ "=> Cant calc Discount Margin from TSY Spread to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromTSYSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblTSYSpread);
	}

	@Override public double calcDiscountMarginFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromYield => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromYield => Invalid inputs");

		return null == _fltParams ? dblYield - dcFunding.libor (valParams.valueDate(), ((int) (12. / (0 ==
			_periodParams.freq() ? 2 : _periodParams.freq()))) + "M") : dblYield - getIndexRate
				(valParams.valueDate(), csqs, (org.drip.analytics.cashflow.CompositeFloatingPeriod)
					calcCurrentPeriod (valParams.valueDate()));
	}

	@Override public double calcDiscountMarginFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYield);
	}

	@Override public double calcDiscountMarginFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromYieldToOptimalExercise =>" +
				" Cant calc Discount Margin from Yield to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYield);
	}

	@Override public double calcDiscountMarginFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double calcDiscountMarginFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcDiscountMarginFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcDiscountMarginFromYieldSpreadToOptimalExercise => " +
					"Cant calc Discount Margin from Yield Sprd to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcDiscountMarginFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblZSpread));
	}

	@Override public double calcDiscountMarginFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcDiscountMarginFromZSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblZSpread);
	}

	@Override public double calcDiscountMarginFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDiscountMarginFromZSpreadToOptimalExercise =>"
				+ " Cant calc Discount Margin from Z Spread to optimal exercise for bonds w emb option");

		return calcDiscountMarginFromZSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblZSpread);
	}

	@Override public double calcDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcDurationFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcDurationFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromASWToOptimalExercise => " +
				"Cant calc Duration from ASW to optimal exercise for bonds w emb option");

		return calcDurationFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcDurationFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcDurationFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromBondBasisToOptimalExercise => " +
				"Cant calc Duration from Bond Basis to optimal exercise for bonds w emb option");

		return calcDurationFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcDurationFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcDurationFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromCreditBasisToOptimalExercise => "
				+ "Cant calc Duration from Credit Basis to optimal exercise for bonds w emb option");

		return calcDurationFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcDurationFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromDiscountMarginToOptimalExercise "
				+ "=> Cant calc Duration from Discount Margin to optimal exercise for bonds w emb option");

		return calcDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcDurationFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcDurationFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromGSpreadToOptimalExercise => " +
				"Cant calc Duration from G Spread to optimal exercise for bonds w emb option");

		return calcDurationFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcDurationFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcDurationFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromISpreadToOptimalExercise => " +
				"Cant calc Duration from I Spread to optimal exercise for bonds w emb option");

		return calcDurationFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcDurationFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcDurationFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromOASToOptimalExercise => " +
				"Cant calc Duration from OAS to optimal exercise for bonds w emb option");

		return calcDurationFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcDurationFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcDurationFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromPECSToOptimalExercise => " +
				"Cant calc Duration from PECS to optimal exercise for bonds w emb option");

		return calcDurationFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, dblPrice);
	}

	@Override public double calcDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcDurationFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromPriceToOptimalExercise => " +
				"Cant calc Duration from Price to optimal exercise for bonds w emb option");

		return calcDurationFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcDurationFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblTSYSpread);
	}

	@Override public double calcDurationFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromTSYSpreadToOptimalExercise => " +
				"Cant calc Duration from TSY Sprd to optimal exercise for bonds w emb option");

		return calcDurationFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblTSYSpread);
	}

	@Override public double calcDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYield));
	}

	@Override public double calcDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcDurationFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcDurationFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromYieldToOptimalExercise => " +
				"Cant calc Duration from Yield to optimal exercise for bonds w emb option");

		return calcDurationFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcDurationFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcDurationFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromYieldSpreadToOptimalExercise => "
				+ "Cant calc Duration from Yield Spread to optimal exercise for bonds w emb option");

		return calcDurationFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcDurationFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcDurationFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcDurationFromZSpreadToOptimalExercise => " +
				"Cant calc Duration from Z Spread to optimal exercise for bonds w emb option");

		return calcDurationFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcGSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcGSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcGSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcGSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromASWToOptimalExercise => " +
				"Cant calc G Spread from ASW to optimal exercise for bonds w emb option");

		return calcGSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcGSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcGSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcGSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcGSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromBondBasisToOptimalExercise => " +
				"Cant calc G Spread from Bond Basis to optimal exercise for bonds w emb option");

		return calcGSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcGSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcGSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcGSpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcGSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromCreditBasisToOptimalExercise => " +
				"Cant calc G Spread from Credit Basis to optimal exercise for bonds w emb option");

		return calcGSpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcGSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcGSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcGSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcGSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromDiscountMarginToOptimalExercise =>"
				+ " Cant calc G Spread from Discount Margin to optimal exercise for bonds w emb option");

		return calcGSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcGSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcGSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcGSpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromISpreadToOptimalExercise => " +
				"Cant calc G Spread from I Spread to optimal exercise for bonds w emb option");

		return calcGSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcGSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcGSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcGSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcGSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromOASToOptimalExercise => " +
				"Cant calc G Spread from OAS to optimal exercise for bonds w emb option");

		return calcGSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcGSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcGSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcGSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcGSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromPECSToOptimalExercise => " +
				"Cant calc G Spread from PECS to optimal exercise for bonds w emb option");

		return calcGSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcGSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPrice));
	}

	@Override public double calcGSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcGSpreadFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcGSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcGSpreadFromPriceToOptimalExercise => Can't do Work-out");

		return calcGSpreadFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double calcGSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcGSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcGSpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc G Spread from TSY Spread to optimal exercise for bonds w emb option");

		return calcGSpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcGSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblYield) || valParams.valueDate() >=
					dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromYield => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcTSY = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

		if (null == dcTSY)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromYield => Invalid inputs");

		return dblYield - dcTSY.estimateManifestMeasure ("Yield", dblWorkoutDate);
	}

	@Override public double calcGSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcGSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromYieldToOptimalExercise => " +
				"Cant calc G Spread from Yield to optimal exercise for bonds w emb option");

		return calcGSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcGSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcGSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcGSpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc G Spread from Yield Spread to optimal exercise for bonds w emb option");

		return calcGSpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcGSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcGSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcGSpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcGSpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcGSpreadFromZSpreadToOptimalExercise => " +
				"Cant calc G Spread from Z Spread to optimal exercise for bonds w emb option");

		return calcGSpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcISpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcISpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcISpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcISpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromASWToOptimalExercise => " +
				"Cant calc I Spread from ASW to optimal exercise for bonds w emb option");

		return calcISpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcISpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcISpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcISpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcISpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromBondBasisToOptimalExercise => " +
				"Cant calc I Spread from Bond Basis to optimal exercise for bonds w emb option");

		return calcISpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcISpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcISpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcISpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcISpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromCreditBasisToOptimalExercise => " +
				"Cant calc I Spread from Credit Basis to optimal exercise for bonds w emb option");

		return calcISpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcISpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcISpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcISpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcISpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromDiscountMarginToOptimalExercise =>"
				+ " Cant calc I Spread from Discount Margin to optimal exercise for bonds w emb option");

		return calcISpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcISpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcISpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcISpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromGSpreadToOptimalExercise => " +
				"Cant calc I Spread from G Spread to optimal exercise for bonds w emb option");

		return calcISpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcISpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcISpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcISpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcISpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromOASToOptimalExercise => " +
				"Cant calc I Spread from OAS to optimal exercise for bonds w emb option");

		return calcISpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcISpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcISpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcISpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcISpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromPECSToOptimalExercise => " +
				"Cant calc I Spread from PECS to optimal exercise for bonds w emb option");

		return calcISpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcISpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPrice));
	}

	@Override public double calcISpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcISpreadFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcISpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcISpreadFromPriceToOptimalExercise => Can't do Work-out");

		return calcISpreadFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double calcISpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcISpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcISpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc I Spread from TSY Spread to optimal exercise for bonds w emb option");

		return calcISpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcISpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::calcISpreadFromYield => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromYield => Invalid inputs");

		return dblYield - dcFunding.estimateManifestMeasure ("Rate", dblWorkoutDate);
	}

	@Override public double calcISpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcISpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromYieldToOptimalExercise => " +
				"Cant calc I Spread from Yield to optimal exercise for bonds w emb option");

		return calcISpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcISpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcISpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcISpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc I Spread from Yield Spread to optimal exercise for bonds w emb option");

		return calcISpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcISpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcISpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcISpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcISpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcISpreadFromZSpreadToOptimalExercise => " +
				"Cant calc I Spread from Z Spread to optimal exercise for bonds w emb option");

		return calcISpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcMacaulayDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromASW (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblASW));
	}

	@Override public double calcMacaulayDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromASW (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblASW);
	}

	@Override public double calcMacaulayDurationFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromASWToOptimalExercise => " +
					"Cant calc Macaulay Duration from ASW to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromASW (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblASW);
	}

	@Override public double calcMacaulayDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblBondBasis));
	}

	@Override public double calcMacaulayDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromBondBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcMacaulayDurationFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromBondBasisToOptimalExercise => " +
					"Cant calc Macaulay Duration from Bnd Basis to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromBondBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcMacaulayDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double calcMacaulayDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcMacaulayDurationFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromCreditBasisToOptimalExercise => " +
					"Cant calc Macaulay Duration from Crd Basis to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcMacaulayDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromDiscountMargin (valParams, csqs, quotingParams,
				dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcMacaulayDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcMacaulayDurationFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromDiscountMarginToOptimalExercise => " +
					"Cant calc Macaulay Duration from Disc Marg to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcMacaulayDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblGSpread));
	}

	@Override public double calcMacaulayDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromGSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblGSpread);
	}

	@Override public double calcMacaulayDurationFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromGSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from G Spread to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromGSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblGSpread);
	}

	@Override public double calcMacaulayDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblISpread));
	}

	@Override public double calcMacaulayDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromISpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblISpread);
	}

	@Override public double calcMacaulayDurationFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromISpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from I Spread to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromISpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblISpread);
	}

	@Override public double calcMacaulayDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblOAS));
	}

	@Override public double calcMacaulayDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblOAS);
	}

	@Override public double calcMacaulayDurationFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromOASToOptimalExercise => " +
					"Cant calc Macaulay Duration from OAS to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblOAS);
	}

	@Override public double calcMacaulayDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPECS));
	}

	@Override public double calcMacaulayDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPECS);
	}

	@Override public double calcMacaulayDurationFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromPECSToOptimalExercise => " +
					"Cant calc Macaulay Duration from PECS to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPECS);
	}

	@Override public double calcMacaulayDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPrice));
	}

	@Override public double calcMacaulayDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromPrice (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblPrice);
	}

	@Override public double calcMacaulayDurationFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromPriceToOptimalExercise => Cant determine Work-out");

		return calcMacaulayDurationFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
			dblPrice);
	}

	@Override public double calcMacaulayDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double calcMacaulayDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromTSYSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblTSYSpread);
	}

	@Override public double calcMacaulayDurationFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromTSYSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from TSY Sprd to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromTSYSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblTSYSpread);
	}

	@Override public double calcMacaulayDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcMacaulayDurationFromYield => Invalid inputs");

		double dblYieldPV = 0.;
		double dblCFPeriod = 0.;
		double dblDuration = 0.;
		boolean bFirstPeriod = true;
		boolean bTerminateCouponFlow = false;
		org.drip.analytics.cashflow.CompositePeriod periodRef = null;

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < valParams.valueDate()) continue;

			if (bFirstPeriod) {
				bFirstPeriod = false;

				dblCFPeriod = period.couponDCF() - period.accrualDCF (valParams.valueDate());
			} else
				dblCFPeriod += period.couponDCF();

			periodRef = period;

			double dblAccrualEndDate = period.endDate();

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (valParams.valueDate(),
				valParams, csqs);

			if (null == pcm)
				throw new java.lang.Exception ("BondComponent::calcMacaulayDurationFromYield => No PCM");

			int iFrequency = _periodParams.freq();
			java.lang.String strDC = _periodParams.couponDC();
			boolean bApplyCpnEOMAdj = _periodParams.couponEOMAdjustment();

			java.lang.String strCalendar = couponCurrency();

			if (null == strCalendar || strCalendar.isEmpty()) strCalendar = redemptionCurrency();

			org.drip.analytics.daycount.ActActDCParams aap = new org.drip.analytics.daycount.ActActDCParams
				(iFrequency, period.startDate(), period.endDate());

			if (null != quotingParams) {
				strDC = quotingParams.yieldDayCount();

				iFrequency = quotingParams.yieldFreq();

				bApplyCpnEOMAdj = quotingParams.applyYieldEOMAdj();

				strCalendar = quotingParams.yieldCalendar();

				if (null == (aap = quotingParams.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams (quotingParams.yieldFreq(),
						period.startDate(), period.endDate());
			} else if (null != _mktConv && null != _mktConv._quotingParams) {
				strDC = _mktConv._quotingParams.yieldDayCount();

				iFrequency = _mktConv._quotingParams.yieldFreq();

				bApplyCpnEOMAdj = _mktConv._quotingParams.applyYieldEOMAdj();

				strCalendar = _mktConv._quotingParams.yieldCalendar();

				if (null == (aap = _mktConv._quotingParams.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams
						(_mktConv._quotingParams.yieldFreq(), period.startDate(), period.endDate());
			}

			double dblYearFract = org.drip.analytics.daycount.Convention.YearFraction (valParams.valueDate(),
				period.payDate(), strDC, bApplyCpnEOMAdj, aap, strCalendar);

			double dblYieldDF = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency, dblYield,
				s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod : dblYearFract);

			double dblCouponNotional = notional (period.startDate());

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (period.startDate(), dblNotionalEndDate);

			double dblCouponPV = period.accrualDCF (dblAccrualEndDate) * pcm.rate() * dblYieldDF *
				dblCouponNotional;

			double dblPeriodNotionalPV = (notional (period.startDate()) - notional (dblNotionalEndDate)) *
				dblYieldDF;

			dblYieldPV += (dblCouponPV + dblPeriodNotionalPV);
			dblDuration += dblCFPeriod * (dblCouponPV + dblPeriodNotionalPV);

			if (bTerminateCouponFlow) break;
		}

		int iFrequency = _periodParams.freq();
		java.lang.String strDC = _periodParams.couponDC();
		org.drip.analytics.daycount.ActActDCParams aap = null;
		boolean bApplyCpnEOMAdj = _periodParams.couponEOMAdjustment();

		java.lang.String strCalendar = couponCurrency();

		if (null == strCalendar || strCalendar.isEmpty()) strCalendar = redemptionCurrency();

		if (null != periodRef)
			aap = new org.drip.analytics.daycount.ActActDCParams (iFrequency, periodRef.startDate(),
				periodRef.endDate());

		if (null != quotingParams) {
			strDC = quotingParams.yieldDayCount();

			iFrequency = quotingParams.yieldFreq();

			bApplyCpnEOMAdj = quotingParams.applyYieldEOMAdj();

			strCalendar = quotingParams.yieldCalendar();

			if (null != periodRef)
				aap = new org.drip.analytics.daycount.ActActDCParams (quotingParams.yieldFreq(),
					periodRef.startDate(), periodRef.endDate());
		} else if (null != _mktConv && null != _mktConv._quotingParams) {
			strDC = _mktConv._quotingParams.yieldDayCount();

			iFrequency = _mktConv._quotingParams.yieldFreq();

			bApplyCpnEOMAdj = _mktConv._quotingParams.applyYieldEOMAdj();

			strCalendar = _mktConv._quotingParams.yieldCalendar();

			if (null != periodRef)
				aap = new org.drip.analytics.daycount.ActActDCParams (_mktConv._quotingParams.yieldFreq(),
					periodRef.startDate(), periodRef.endDate());
		}

		double dblYearFractWorkout = org.drip.analytics.daycount.Convention.YearFraction
			(valParams.valueDate(), dblWorkoutDate, strDC, bApplyCpnEOMAdj, aap, strCalendar);

		double dblDFWorkout = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency, dblYield,
			s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod : dblYearFractWorkout);

		double dblRedemptionPV = dblWorkoutFactor * dblDFWorkout * notional (dblWorkoutDate);

		return (dblDuration + dblCFPeriod * dblRedemptionPV) / (dblYieldPV + dblRedemptionPV);
	}

	@Override public double calcMacaulayDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYield);
	}

	@Override public double calcMacaulayDurationFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcMacaulayDurationFromYieldToOptimalExercise =>"
				+ " Cant calc Macaulay Duration from Yield to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYield);
	}

	@Override public double calcMacaulayDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double calcMacaulayDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcMacaulayDurationFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromYieldSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from Yld Sprd to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcMacaulayDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblZSpread));
	}

	@Override public double calcMacaulayDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcMacaulayDurationFromZSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblZSpread);
	}

	@Override public double calcMacaulayDurationFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcMacaulayDurationFromZSpreadToOptimalExercise => " +
					"Cant calc Macaulay Duration from Z Spread to optimal exercise for bonds w emb option");

		return calcMacaulayDurationFromZSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblZSpread);
	}

	@Override public double calcModifiedDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblASW));
	}

	@Override public double calcModifiedDurationFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromASW (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblASW);
	}

	@Override public double calcModifiedDurationFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromASWToOptimalExercise => " +
					"Cant calc Modified Duration from ASW to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromASW (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblASW);
	}

	@Override public double calcModifiedDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblBondBasis));
	}

	@Override public double calcModifiedDurationFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromBondBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcModifiedDurationFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromBondBasisToOptimalExercise => " +
					"Cant calc Modified Duration from Bnd Basis to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromBondBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblBondBasis);
	}

	@Override public double calcModifiedDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double calcModifiedDurationFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcModifiedDurationFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromCreditBasisToOptimalExercise => " +
					"Cant calc Modified Duration from Crd Basis to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcModifiedDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromDiscountMargin (valParams, csqs, quotingParams,
				dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcModifiedDurationFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcModifiedDurationFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromDiscountMarginToOptimalExercise => " +
					"Cant calc Modified Duration from Disc Marg to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcModifiedDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblGSpread));
	}

	@Override public double calcModifiedDurationFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromGSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblGSpread);
	}

	@Override public double calcModifiedDurationFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromGSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from G Spread to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromGSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblGSpread);
	}

	@Override public double calcModifiedDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblISpread));
	}

	@Override public double calcModifiedDurationFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromISpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblISpread);
	}

	@Override public double calcModifiedDurationFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromISpreadToOptimalExercise => " +
					"Cant calc Modified Duration from I Spread to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromISpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblISpread);
	}

	@Override public double calcModifiedDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblOAS));
	}

	@Override public double calcModifiedDurationFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblOAS);
	}

	@Override public double calcModifiedDurationFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromOASToOptimalExercise => " +
					"Cant calc Modified Duration from OAS to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblOAS);
	}

	@Override public double calcModifiedDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPECS));
	}

	@Override public double calcModifiedDurationFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPECS);
	}

	@Override public double calcModifiedDurationFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromPECSToOptimalExercise => " +
					"Cant calc Modified Duration from PECS to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPECS);
	}

	@Override public double calcModifiedDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP ||
			!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::calcModifiedDurationFromPrice => Input inputs");

		return (dblPrice - calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPrice) + 0.0001)) / (dblPrice + accrued (valParams.valueDate(), csqs));
	}

	@Override public double calcModifiedDurationFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblPrice);
	}

	@Override public double calcModifiedDurationFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromPriceToOptimalExercise => Cant determine Work-out");

		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
			dblPrice);
	}

	@Override public double calcModifiedDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double calcModifiedDurationFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromTSYSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblTSYSpread);
	}

	@Override public double calcModifiedDurationFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromTSYSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from TSY Sprd to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromTSYSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblTSYSpread);
	}

	@Override public double calcModifiedDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYield));
	}

	@Override public double calcModifiedDurationFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromYield (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYield);
	}

	@Override public double calcModifiedDurationFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcModifiedDurationFromYieldToOptimalExercise =>"
				+ " Cant calc Modified Duration from Yield to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromYield (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYield);
	}

	@Override public double calcModifiedDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYieldSpread));
	}

	@Override public double calcModifiedDurationFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcModifiedDurationFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromYieldSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from Yld Sprd to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromYieldSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblYieldSpread);
	}

	@Override public double calcModifiedDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcPriceFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblZSpread));
	}

	@Override public double calcModifiedDurationFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcModifiedDurationFromZSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblZSpread);
	}

	@Override public double calcModifiedDurationFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcModifiedDurationFromZSpreadToOptimalExercise => " +
					"Cant calc Modified Duration from Z Spread to optimal exercise for bonds w emb option");

		return calcModifiedDurationFromZSpread (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblZSpread);
	}

	@Override public double calcOASFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcOASFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcOASFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1., dblASW);
	}

	@Override public double calcOASFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromASWToOptimalExercise => " +
				"Cant calc OAS from ASW to optimal exercise for bonds w emb option");

		return calcOASFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1., dblASW);
	}

	@Override public double calcOASFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcOASFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcOASFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcOASFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromBondBasisToOptimalExercise => " +
				"Cant calc OAS from Bnd Basis to optimal exercise for bonds w emb option");

		return calcOASFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcOASFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcOASFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcOASFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcOASFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromCreditBasisToOptimalExercise => " +
				"Cant calc OAS from Credit Basis to optimal exercise for bonds w emb option");

		return calcOASFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcOASFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcOASFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcOASFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcOASFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromDiscountMarginToOptimalExercise => " +
				"Cant calc OAS from Discount Margin to optimal exercise for bonds w emb option");

		return calcOASFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcOASFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcOASFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcOASFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcOASFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromGSpreadToOptimalExercise => " +
				"Cant calc OAS from G Spread to optimal exercise for bonds w emb option");

		return calcOASFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcOASFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcOASFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcOASFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcOASFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromISpreadToOptimalExercise => " +
				"Cant calc OAS from I Spread to optimal exercise for bonds w emb option");

		return calcOASFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcOASFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcOASFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcOASFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcOASFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromPECSToOptimalExercise => " +
				"Cant calc OAS from PECS to optimal exercise for bonds w emb option");

		return calcOASFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcOASFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null == valParams || valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP ||
			!org.drip.quant.common.NumberUtil.IsValid (dblPrice))
			throw new java.lang.Exception ("BondComponent::calcOASFromPrice => Input inputs");

		return new BondCalibrator (this).calibrateZSpreadFromPrice (valParams, csqs,
			ZERO_OFF_OF_TREASURIES_DISCOUNT_CURVE, dblWorkoutDate, dblWorkoutFactor, dblPrice);
	}

	@Override public double calcOASFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcOASFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcOASFromPriceToOptimalExercise - cant calc Work-out");

		return calcOASFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double calcOASFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcOASFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcOASFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcOASFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromTSYSpreadToOptimalExercise => " +
				"Cant calc OAS from TSY Sprd to optimal exercise for bonds w emb option");

		return calcOASFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcOASFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYield));
	}

	@Override public double calcOASFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcOASFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcOASFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromYieldToOptimalExercise => " +
				"Cant calc OAS from Yield to optimal exercise for bonds w emb option");

		return calcOASFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcOASFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcOASFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcOASFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcOASFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromYieldSpreadToOptimalExercise => " +
				"Cant calc OAS from Yield Sprd to optimal exercise for bonds w emb option");

		return calcOASFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcOASFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcOASFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcOASFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcOASFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcOASFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcOASFromZSpreadToOptimalExercise => " +
				"Cant calc OAS from Z Spread to optimal exercise for bonds w emb option");

		return calcOASFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcPECSFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcPECSFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcPECSFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1., dblASW);
	}

	@Override public double calcPECSFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromASWToOptimalExercise => " +
				"Cant calc PECS from ASW to optimal exercise for bonds w emb option");

		return calcPECSFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1., dblASW);
	}

	@Override public double calcPECSFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcPECSFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcPECSFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcPECSFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromBondBasisToOptimalExercise => " +
				"Cant calc PECS from Bond Basis to optimal exercise for bonds w emb option");

		return calcPECSFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcPECSFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcPECSFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcPECSFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcPECSFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromCreditBasisToOptimalExercise => " +
				"Cant calc PECS from Credit Basis to optimal exercise for bonds w emb option");

		return calcPECSFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcPECSFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcPECSFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcPECSFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcPECSFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromDiscountMarginToOptimalExercise => " +
				"Cant calc PECS from Discount Margin to optimal exercise for bonds w emb option");

		return calcPECSFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcPECSFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcPECSFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcPECSFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcPECSFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromGSpreadToOptimalExercise => " +
				"Cant calc PECS from G Spread to optimal exercise for bonds w emb option");

		return calcPECSFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcPECSFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcPECSFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcPECSFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcPECSFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromISpreadToOptimalExercise => " +
				"Cant calc PECS from I Spread to optimal exercise for bonds w emb option");

		return calcPECSFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcPECSFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcPECSFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcPECSFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1., dblOAS);
	}

	@Override public double calcPECSFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromOASToOptimalExercise => " +
				"Cant calc PECS from OAS to optimal exercise for bonds w emb option");

		return calcPECSFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1., dblOAS);
	}

	@Override public double calcPECSFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateCreditBasisFromPrice (valParams, csqs, dblWorkoutDate,
			dblWorkoutFactor, dblPrice, true);
	}

	@Override public double calcPECSFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcPECSFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcPECSFromPriceToOptimalExercise => Cant determine Work-out");

		return calcPECSFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double calcPECSFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcPECSFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcPECSFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcPECSFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromTSYSpreadToOptimalExercise => " +
				"Cant calc PECS from TSY Spread to optimal exercise for bonds w emb option");

		return calcPECSFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcPECSFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYield));
	}

	@Override public double calcPECSFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcPECSFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcPECSFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromYieldToOptimalExercise => " +
				"Cant calc PECS from Yield to optimal exercise for bonds w emb option");

		return calcPECSFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcPECSFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcPECSFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcPECSFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcPECSFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromYieldSpreadToOptimalExercise => " +
				"Cant calc PECS from Yield Spread to optimal exercise for bonds w emb option");

		return calcPECSFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcPECSFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcPECSFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcPECSFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcPECSFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcPECSFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPECSFromZSpreadToOptimalExercise => " +
				"Cant calc PECS from Z Spread to optimal exercise for bonds w emb option");

		return calcPECSFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcPriceFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblASW) || valParams.valueDate() >=
					dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcPriceFromASW => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::calcPriceFromASW => Invalid Inputs");

		org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (valParams.valueDate(),
			valParams, csqs);

		if (null == pcm) throw new java.lang.Exception ("BondComponent::calcPriceFromASW => No PCM");

		return dblWorkoutFactor - 100. * dcFunding.liborDV01 (dblWorkoutDate) * (dblASW +
			dcFunding.estimateManifestMeasure ("Rate", dblWorkoutDate) - pcm.rate());
	}

	@Override public double calcPriceFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcPriceFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcPriceFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromASWToOptimalExercise => " +
				"Cant calc Price from ASW to optimal exercise for bonds w emb option");

		return calcPriceFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcPriceFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcPriceFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcPriceFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcPriceFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromBondBasisToOptimalExercise => " +
				"Cant calc Price from Bond Basis to optimal exercise for bonds w emb option");

		return calcPriceFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcPriceFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return priceFromBumpedCC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, dblCreditBasis, false);
	}

	@Override public double calcPriceFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcPriceFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcPriceFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromCreditBasisToOptimalExercise => " +
				"Cant calc Price from Credit Basis to optimal exercise for bonds w emb option");

		return calcPriceFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcPriceFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcPriceFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcPriceFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcPriceFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromDiscountMarginToOptimalExercise => "
				+ "Cant calc Price from Discount Margin to optimal exercise for bonds w emb option");

		return calcPriceFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcPriceFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcPriceFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcPriceFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcPriceFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromGSpreadToOptimalExercise => " +
				"Cant calc Price from G Spread to optimal exercise for bonds w emb option");

		return calcPriceFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcPriceFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcPriceFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcPriceFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcPriceFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromISpreadToOptimalExercise => " +
				"Cant calc Price from I Spread to optimal exercise for bonds w emb option");

		return calcPriceFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcPriceFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, dblOAS);
	}

	@Override public double calcPriceFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcPriceFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcPriceFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromOASToOptimalExercise => " +
				"Cant calc Price from OAS to optimal exercise for bonds w emb option");

		return calcPriceFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcPriceFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return priceFromBumpedCC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, dblPECS, true);
	}

	@Override public double calcPriceFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcPriceFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcPriceFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromPECSToOptimalExercise => " +
				"Cant calc Price from PECS to optimal exercise for bonds w emb option");

		return calcPriceFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcPriceFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcPriceFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcPriceFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcPriceFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromTSYSpreadToOptimalExercise => " +
				"Cant calc Price from TSY Spread to optimal exercise for bonds w emb option");

		return calcPriceFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblTSYSpread);
	}

	@Override public double calcPriceFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				valParams.valueDate() >= dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcPriceFromYield => Invalid inputs");

		double dblYieldPV = 0.;
		double dblCFPeriod = 0.;
		boolean bFirstPeriod = true;
		boolean bTerminateCouponFlow = false;
		double dblScalingNotional = java.lang.Double.NaN;
		org.drip.analytics.cashflow.CompositePeriod periodRef = null;

		if (null != _notlParams && _notlParams._bPriceOffOriginalNotional) dblScalingNotional = 1.;

		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods()) {
			if (period.payDate() < valParams.valueDate()) continue;

			if (bFirstPeriod) {
				bFirstPeriod = false;

				dblCFPeriod = period.couponDCF() - period.accrualDCF (valParams.valueDate());
			} else
				dblCFPeriod += period.couponDCF();

			periodRef = period;

			double dblAccrualEndDate = period.endDate();

			if (s_bBlog)
				System.out.println ("Unadjusted Accrual End: " + new org.drip.analytics.date.JulianDate
					(dblAccrualEndDate));

			double dblNotionalEndDate = period.endDate();

			if (dblAccrualEndDate >= dblWorkoutDate) {
				bTerminateCouponFlow = true;
				dblAccrualEndDate = dblWorkoutDate;
				dblNotionalEndDate = dblWorkoutDate;
			}

			if (s_bBlog)
				System.out.println ("Adjusted Accrual End: " + new org.drip.analytics.date.JulianDate
					(dblAccrualEndDate));

			if (java.lang.Double.isNaN (dblScalingNotional))
				dblScalingNotional = notional (period.startDate());

			org.drip.analytics.output.CompositePeriodCouponMetrics pcm = coupon (valParams.valueDate(),
				valParams, csqs);

			if (null == pcm) throw new java.lang.Exception ("BondComponent::calcPriceFromYield => No PCM");

			double dblPeriodCoupon = pcm.rate();

			int iFrequency = _periodParams.freq();
			java.lang.String strDC = _periodParams.couponDC();
			boolean bApplyCpnEOMAdj = _periodParams.couponEOMAdjustment();

			java.lang.String strCalendar = couponCurrency();

			if (null == strCalendar || strCalendar.isEmpty()) strCalendar = redemptionCurrency();

			org.drip.analytics.daycount.ActActDCParams aap = new org.drip.analytics.daycount.ActActDCParams
				(iFrequency, period.startDate(), period.endDate());

			if (null != quotingParams) {
				strDC = quotingParams.yieldDayCount();

				iFrequency = quotingParams.yieldFreq();

				bApplyCpnEOMAdj = quotingParams.applyYieldEOMAdj();

				strCalendar = quotingParams.yieldCalendar();

				if (null == (aap = quotingParams.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams (quotingParams.yieldFreq(),
						period.startDate(), period.endDate());
			} else if (null != _mktConv && null != _mktConv._quotingParams) {
				strDC = _mktConv._quotingParams.yieldDayCount();

				iFrequency = _mktConv._quotingParams.yieldFreq();

				bApplyCpnEOMAdj = _mktConv._quotingParams.applyYieldEOMAdj();

				strCalendar = _mktConv._quotingParams.yieldCalendar();

				if (null == (aap = _mktConv._quotingParams.yieldAAP()))
					aap = new org.drip.analytics.daycount.ActActDCParams
						(_mktConv._quotingParams.yieldFreq(), period.startDate(), period.endDate());
			}

			double dblYearFract = org.drip.analytics.daycount.Convention.YearFraction (valParams.valueDate(),
				period.payDate(), strDC, bApplyCpnEOMAdj, aap, strCalendar);

			double dblYieldDF = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency, dblYield,
				s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod : dblYearFract);

			double dblCouponNotional = notional (period.startDate());

			if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_AT_END ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (dblNotionalEndDate);
			else if (org.drip.product.params.NotionalSetting.PERIOD_AMORT_EFFECTIVE ==
				_notlParams._iPeriodAmortizationMode)
				dblCouponNotional = notional (period.startDate(), dblNotionalEndDate);

			double dblCouponPV = period.accrualDCF (dblAccrualEndDate) * dblPeriodCoupon * dblYieldDF *
				dblCouponNotional;

			dblYieldPV += dblCouponPV;

			if (s_bBlog) {
				System.out.println ("Coupon Notional: " + dblCouponNotional);

				System.out.println ("Period Coupon: " + dblPeriodCoupon);

				System.out.println ("\n" + org.drip.analytics.date.JulianDate.fromJulian (dblAccrualEndDate)
					+ "; DCF=" + org.drip.quant.common.FormatUtil.FormatDouble (period.accrualDCF
						(dblAccrualEndDate), 1, 3, 100.) + "; Eff Notl=" +
							org.drip.quant.common.FormatUtil.FormatDouble (notional (period.startDate(),
								dblNotionalEndDate), 1, 3, 100.) + "; PV: " +
									org.drip.quant.common.FormatUtil.FormatDouble (dblYieldPV, 1, 3, 100.));

				System.out.println ("Incremental Cpn PV: " + org.drip.quant.common.FormatUtil.FormatDouble
					(dblCouponPV, 1, 3, 100.));
			}

			dblYieldPV += (notional (period.startDate()) - notional (dblNotionalEndDate)) * dblYieldDF;

			if (s_bBlog) {
				System.out.println (org.drip.analytics.date.JulianDate.fromJulian (period.startDate()) + "->"
					+ org.drip.analytics.date.JulianDate.fromJulian (dblNotionalEndDate) + "; Notl:" +
						org.drip.quant.common.FormatUtil.FormatDouble (notional (period.startDate()), 1, 3,
							100.) + "->" + org.drip.quant.common.FormatUtil.FormatDouble (notional
								(period.endDate()), 1, 3, 100.) + "; Coupon=" +
									org.drip.quant.common.FormatUtil.FormatDouble (dblPeriodCoupon, 1, 3,
										100.));

				System.out.println ("Incremental Notl PV: " + org.drip.quant.common.FormatUtil.FormatDouble
					((notional (period.startDate()) - notional (dblNotionalEndDate)) * dblYieldDF, 1, 3,
						100.));

				System.out.println ("YF: " + org.drip.quant.common.FormatUtil.FormatDouble (dblYearFract, 1,
					3, 100.) + "; DF: " + dblYieldDF + "; PV: " +
						org.drip.quant.common.FormatUtil.FormatDouble (dblYieldPV, 1, 3, 100.));
			}

			if (bTerminateCouponFlow) break;
		}

		double dblCashPayDate = java.lang.Double.NaN;

		try {
			dblCashPayDate = _mktConv.getSettleDate (valParams);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();

			dblCashPayDate = valParams.cashPayDate();
		}

		int iFrequency = _periodParams.freq();
		java.lang.String strDC = _periodParams.couponDC();
		org.drip.analytics.daycount.ActActDCParams aap = null;
		boolean bApplyCpnEOMAdj = _periodParams.couponEOMAdjustment();

		java.lang.String strCalendar = couponCurrency();

		if (null == strCalendar || strCalendar.isEmpty()) strCalendar = redemptionCurrency();

		if (null != periodRef)
			aap = new org.drip.analytics.daycount.ActActDCParams (iFrequency, periodRef.startDate(),
				periodRef.endDate());

		if (null != quotingParams) {
			strDC = quotingParams.yieldDayCount();

			iFrequency = quotingParams.yieldFreq();

			bApplyCpnEOMAdj = quotingParams.applyYieldEOMAdj();

			strCalendar = quotingParams.yieldCalendar();

			if (null != periodRef)
				aap = new org.drip.analytics.daycount.ActActDCParams (quotingParams.yieldFreq(),
					periodRef.startDate(), periodRef.endDate());
		} else if (null != _mktConv && null != _mktConv._quotingParams) {
			strDC = _mktConv._quotingParams.yieldDayCount();

			iFrequency = _mktConv._quotingParams.yieldFreq();

			bApplyCpnEOMAdj = _mktConv._quotingParams.applyYieldEOMAdj();

			strCalendar = _mktConv._quotingParams.yieldCalendar();

			if (null != periodRef)
				aap = new org.drip.analytics.daycount.ActActDCParams (_mktConv._quotingParams.yieldFreq(),
					periodRef.startDate(), periodRef.endDate());
		}

		double dblYearFractCashPay = org.drip.analytics.daycount.Convention.YearFraction
			(valParams.valueDate(), dblCashPayDate, strDC, bApplyCpnEOMAdj, aap, strCalendar);

		double dblDFCashPay = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency, dblYield,
			dblYearFractCashPay);

		if (s_bBlog)
			System.out.println ("CP Date: " + new org.drip.analytics.date.JulianDate (dblCashPayDate) +
				"; DF: " + dblDFCashPay);

		double dblAccrued = accrued (valParams.valueDate(), csqs);

		double dblYearFractWorkout = org.drip.analytics.daycount.Convention.YearFraction
			(valParams.valueDate(), dblWorkoutDate, strDC, bApplyCpnEOMAdj, aap, strCalendar);

		double dblDFWorkout = org.drip.analytics.support.AnalyticsHelper.Yield2DF (iFrequency, dblYield,
			s_bYieldDFOffofCouponAccrualDCF ? dblCFPeriod : dblYearFractWorkout);

		if (s_bBlog) System.out.println ("DF Workout: " + dblDFWorkout);

		double dblPV = (((dblYieldPV + dblWorkoutFactor * dblDFWorkout * notional (dblWorkoutDate)) /
			dblDFCashPay) - dblAccrued);

		if (s_bBlog)
			System.out.println ("Accrued: " + dblAccrued + "; Clean PV: " +
				org.drip.quant.common.FormatUtil.FormatDouble (dblPV, 1, 3, 100.) + "; PV Scale: " +
					notional (valParams.valueDate()));

		return dblPV / dblScalingNotional;
	}

	@Override public double calcPriceFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcPriceFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromYieldToOptimalExercise => " +
				"Cannot calc exercise px from yld for bonds w emb option");

		return calcPriceFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcPriceFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcPriceFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcPriceFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcPriceFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromYieldSpreadToOptimalExercise => " +
				"Cant calc Price from Yield Spread to optimal exercise for bonds w emb option");

		return calcPriceFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcPriceFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return priceFromBumpedZC (valParams, csqs, quotingParams,
			ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE, dblWorkoutDate, dblWorkoutFactor, dblZSpread);
	}

	@Override public double calcPriceFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcPriceFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcPriceFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcPriceFromZSpreadToOptimalExercise => " +
				"Cant calc Price from Z Spread to optimal exercise for bonds w emb option");

		return calcPriceFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcTSYSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcTSYSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcTSYSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromASWToOptimalExercise => " +
				"Cant calc TSY Spread from ASW to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcTSYSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcTSYSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcTSYSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromBondBasisToOptimalExercise => " +
				"Cant calc TSY Spread from Bond Basis to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcTSYSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcTSYSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcTSYSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromCreditBasisToOptimalExercise => "
				+ "Cant calc TSY Spread from Credit Basis to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcTSYSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblDiscountMargin));
	}

	@Override public double calcTSYSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcTSYSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromDiscountMarginToOptimalExercise "
				+ "=> Cant calc TSY Spread from Discount Margin to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcTSYSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcTSYSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcTSYSpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromGSpreadToOptimalExercise => " +
				"Cant calc TSY Spread from G Spread to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcTSYSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcTSYSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcTSYSpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromISpreadToOptimalExercise => " +
				"Cant calc TSY Spread from I Spread to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcTSYSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcTSYSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcTSYSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromOASToOptimalExercise => " +
				"Cant calc TSY Spread from OAS to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcTSYSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcTSYSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcTSYSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromPECSToOptimalExercise => " +
				"Cant calc TSY Spread from PECS to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcTSYSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPrice));
	}

	@Override public double calcTSYSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcTSYSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception
				("BondComponent::calcTSYSpreadFromPriceToOptimalExercise => Cant determine Work-out");

		return calcTSYSpreadFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
			dblPrice);
	}

	@Override public double calcTSYSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		double dblBmkYield = getTsyBmkYield (valParams, csqs, dblWorkoutDate);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblBmkYield))
			throw new java.lang.Exception
				("BondComponent::calcTSYSpreadFromYield => Cannot calculate TSY Bmk Yield");

		return dblYield - dblBmkYield;
	}

	@Override public double calcTSYSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcTSYSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromYieldToOptimalExercise => " +
				"Cant calc TSY Spread from Yield to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcTSYSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcTSYSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcTSYSpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromYieldSpreadToOptimalExercise => "
				+ "Cant calc TSY Spread from Yield Spread to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcTSYSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcTSYSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcTSYSpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcTSYSpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcTSYSpreadFromZSpreadToOptimalExercise => " +
				"Cant calc TSY Spread from Z Spread to optimal exercise for bonds w emb option");

		return calcTSYSpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcYieldFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcYieldFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcYieldFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcYieldFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromASWToOptimalExercise => " +
				"Cant calc Yield from ASW to optimal exercise for bonds w emb option");

		return calcYieldFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcYieldFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblBondBasis) || valParams.valueDate() >=
			dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcYieldFromBondBasis => Invalid Inputs");

		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.)) + dblBondBasis;
	}

	@Override public double calcYieldFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcYieldFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcYieldFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromBondBasisToOptimalExercise => " +
				"Cant calc Yield from Bond Basis to optimal exercise for bonds w emb option");

		return calcYieldFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcYieldFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcYieldFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcYieldFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcYieldFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromCreditBasisToOptimalExercise => " +
				"Cant calc Yield from Credit Basis to optimal exercise for bonds w emb option");

		return calcYieldFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcYieldFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null == valParams || null == csqs || !org.drip.quant.common.NumberUtil.IsValid
			(dblWorkoutDate) || !org.drip.quant.common.NumberUtil.IsValid (dblWorkoutFactor) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblDiscountMargin))
			throw new java.lang.Exception ("BondComponent::calcYieldFromDiscountMargin => Invalid inputs");

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel()[0]);

		if (null == dcFunding)
			throw new java.lang.Exception ("BondComponent::calcYieldFromDiscountMargin => Invalid inputs");

		double dblValueDate = valParams.valueDate();

		return null == _fltParams ? dblDiscountMargin + dcFunding.libor (dblValueDate, ((int) (12. / (0 ==
			_periodParams.freq() ? 2 : _periodParams.freq()))) + "M") : dblDiscountMargin - getIndexRate
				(dblValueDate, csqs, (org.drip.analytics.cashflow.CompositeFloatingPeriod) calcCurrentPeriod
					(dblValueDate));
	}

	@Override public double calcYieldFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcYieldFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcYieldFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromDiscountMarginToOptimalExercise => "
				+ "Cant calc Yield from Discount Margin to optimal exercise for bonds w emb option");

		return calcYieldFromDiscountMargin (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblDiscountMargin);
	}

	@Override public double calcYieldFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblGSpread) || valParams.valueDate() >= dblWorkoutDate
			+ LEFT_EOS_SNIP || null == csqs)
			throw new java.lang.Exception ("BondComponent::calcYieldFromGSpread => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dcGovvie = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

		if (null == dcGovvie)
			throw new java.lang.Exception ("BondComponent::calcYieldFromGSpread => Invalid Inputs");

		return dcGovvie.estimateManifestMeasure ("Yield", dblWorkoutDate) + dblGSpread;
	}

	@Override public double calcYieldFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcYieldFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcYieldFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromGSpreadToOptimalExercise => " +
				"Cant calc Yield from G Spread to optimal exercise for bonds w emb option");

		return calcYieldFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcYieldFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblISpread) || valParams.valueDate() >= dblWorkoutDate
			+ LEFT_EOS_SNIP || null == csqs)
			throw new java.lang.Exception ("BondComponent::calcYieldFromISpread => Invalid Inputs");

		org.drip.analytics.rates.DiscountCurve dc = csqs.govvieCurve
			(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0]));

		if (null == dc)
			throw new java.lang.Exception ("BondComponent::calcYieldFromISpread => Invalid Inputs");

		return dc.estimateManifestMeasure ("Rate", dblWorkoutDate) + dblISpread;
	}

	@Override public double calcYieldFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcYieldFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcYieldFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromISpreadToOptimalExercise => " +
				"Cant calc Yield from I Spread to optimal exercise for bonds w emb option");

		return calcYieldFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcYieldFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcYieldFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcYieldFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcYieldFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromOASToOptimalExercise => " +
				"Cant calc Yield from OAS to optimal exercise for bonds w emb option");

		return calcYieldFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcYieldFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcYieldFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcYieldFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcYieldFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromPECSToOptimalExercise => " +
				"Cant calc Yield from PECS to optimal exercise for bonds w emb option");

		return calcYieldFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateYieldFromPrice (valParams, csqs, dblWorkoutDate,
			dblWorkoutFactor, dblPrice);
	}

	@Override public double calcYieldFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYieldFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYieldFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::calcYieldFromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return wi.yield();
	}

	@Override public double calcYieldFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblTSYSpread) || valParams.valueDate() >=
			dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcYieldFromTSYSpread => Invalid Inputs");

		return getTsyBmkYield (valParams, csqs, dblWorkoutDate) + dblTSYSpread;
	}

	@Override public double calcYieldFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYieldFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYieldFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromTSYSpreadToOptimalExercise => " +
				"Cant calc Yield from TSY Spread to optimal exercise for bonds w emb option");

		return calcYieldFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYieldFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYieldSpread) || valParams.valueDate() >=
			dblWorkoutDate + LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcYieldFromYieldSpread => Invalid Inputs");

		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.)) + dblYieldSpread;
	}

	@Override public double calcYieldFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcYieldFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcYieldFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromYieldSpreadToOptimalExercise => " +
				"Cant calc Yield from Yield Spread to optimal exercise for bonds w emb option");

		return calcYieldFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcYieldFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblZSpread) || valParams.valueDate() >= dblWorkoutDate
			+ LEFT_EOS_SNIP)
			throw new java.lang.Exception ("BondComponent::calcYieldFromZSpread => Invalid Inputs");

		return calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			priceFromBumpedZC (valParams, csqs, quotingParams, ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE,
				dblWorkoutDate, dblWorkoutFactor, dblZSpread));
	}

	@Override public double calcYieldFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcYieldFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcYieldFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldFromZSpreadToOptimalExercise => " +
				"Cant calc Yield from Z Spread to optimal exercise for bonds w emb option");

		return calcYieldFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcYield01FromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcYield01FromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcYield01FromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcYield01FromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromASWToOptimalExercise => " +
				"Cant calc Yield from ASW to optimal exercise for bonds w emb option");

		return calcYield01FromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcYield01FromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcYield01FromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcYieldFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcYield01FromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromBondBasisToOptimalExercise => " +
				"Cant calc Yield01 from Bond Basis to optimal exercise for bonds w emb option");

		return calcYield01FromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcYield01FromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcYield01FromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcYield01FromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblCreditBasis);
	}

	@Override public double calcYield01FromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromCreditBasisToOptimalExercise => " +
				"Cant calc Yield01 from Credit Basis to optimal exercise for bonds w emb option");

		return calcYield01FromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcYield01FromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcYield01FromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcYield01FromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcYield01FromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromDiscountMarginToOptimalExercise =>"
				+ " Cant calc Yield01 from Discount Margin to optimal exercise for bonds w emb option");

		return calcYield01FromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcYield01FromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcYield01FromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcYield01FromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcYield01FromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromGSpreadToOptimalExercise => " +
				"Cant calc Yield01 from G Spread to optimal exercise for bonds w emb option");

		return calcYield01FromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcYield01FromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcYield01FromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcYield01FromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcYield01FromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromISpreadToOptimalExercise => " +
				"Cant calc Yield01 from I Spread to optimal exercise for bonds w emb option");

		return calcYield01FromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcYield01FromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcYield01FromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcYield01FromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcYield01FromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromOASToOptimalExercise => " +
				"Cant calc Yield01 from OAS to optimal exercise for bonds w emb option");

		return calcYield01FromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcYield01FromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcYield01FromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcYield01FromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcYield01FromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromPECSToOptimalExercise => " +
				"Cant calc Yield01 from PECS to optimal exercise for bonds w emb option");

		return calcYield01FromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcYield01FromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPrice));
	}

	@Override public double calcYield01FromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYield01FromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYield01FromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::calcYield01FromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return calcYield01FromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double calcYield01FromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcYield01FromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYield01FromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYield01FromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromTSYSpreadToOptimalExercise => " +
				"Cant calc Yield01 from TSY Spread to optimal exercise for bonds w emb option");

		return calcYield01FromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYield01FromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::calcYield01FromYield => Invalid Inputs");

		return calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			dblYield) - calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblYield + 0.0001);
	}

	@Override public double calcYield01FromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcYield01FromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromYieldToOptimalExercise => " +
				"Cant calc Yield01 from Yield to optimal exercise for bonds w emb option");

		return calcYield01FromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcYield01FromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcYield01FromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcYield01FromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcYield01FromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromYieldSpreadToOptimalExercise => " +
				"Cant calc Yield01 from Yield Spread to optimal exercise for bonds w emb option");

		return calcYield01FromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcYield01FromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcYield01FromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblZSpread));
	}

	@Override public double calcYield01FromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcYield01FromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcYield01FromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYield01FromZSpreadToOptimalExercise => " +
				"Cant calc Yield01 from Z Spread to optimal exercise for bonds w emb option");

		return calcYield01FromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblZSpread);
	}

	@Override public double calcYieldSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromASW (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblASW));
	}

	@Override public double calcYieldSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcYieldSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromASWToOptimalExercise => " +
				"Cant calc Yield Spread from ASW to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcYieldSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblBondBasis));
	}

	@Override public double calcYieldSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcYieldSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromBondBasisToOptimalExercise => "
				+ "Cant calc Yield Spread from Bond Basis to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcYieldSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblCreditBasis));
	}

	@Override public double calcYieldSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcYieldSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromCreditBasisToOptimalExercise "
				+ "=> Cant calc Yield Spread from Credit Basis to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromCreditBasis (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblCreditBasis);
	}

	@Override public double calcYieldSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromDiscountMargin (valParams, csqs, quotingParams,
				dblWorkoutDate, dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcYieldSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcYieldSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcYieldSpreadFromDiscountMarginToOptimalExercise => " +
					"Cant calc Yield Spread from Disc Margin to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcYieldSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblGSpread));
	}

	@Override public double calcYieldSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblGSpread);
	}

	@Override public double calcYieldSpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromGSpreadToOptimalExercise => " +
				"Cant calc Yield Spread from G Spread to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblGSpread);
	}

	@Override public double calcYieldSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromISpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblISpread));
	}

	@Override public double calcYieldSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblISpread);
	}

	@Override public double calcYieldSpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromISpreadToOptimalExercise => " +
				"Cant calc Yield Spread from I Spread to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblISpread);
	}

	@Override public double calcYieldSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromOAS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblOAS));
	}

	@Override public double calcYieldSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcYieldSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromOASToOptimalExercise => " +
				"Cant calc Yield Spread from OAS to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcYieldSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPECS (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPECS));
	}

	@Override public double calcYieldSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcYieldSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromPECSToOptimalExercise => " +
				"Cant calc Yield Spread from PECS to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcYieldSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblPrice));
	}

	@Override public double calcYieldSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcYieldSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
			dblPrice);
	}

	@Override public double calcYieldSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblTSYSpread));
	}

	@Override public double calcYieldSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPrice);
	}

	@Override public double calcYieldSpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromTSYSpreadToOptimalExercise => "
				+ "Cant calc Yield Spread from TSY Spread to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblPrice);
	}

	@Override public double calcYieldSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblYield))
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromYield => Invalid Inputs");

		return dblYield - calcYieldFromPrice (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor, 0.));
	}

	@Override public double calcYieldSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcYieldSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromYieldToOptimalExercise => " +
				"Cant calc Yield Spread from Yield to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcYieldSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromYield (valParams, csqs, quotingParams, dblWorkoutDate,
			dblWorkoutFactor, calcYieldFromZSpread (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblZSpread));
	}

	@Override public double calcYieldSpreadFromZSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		return calcYieldSpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblZSpread);
	}

	@Override public double calcYieldSpreadFromZSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblZSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcYieldSpreadFromZSpreadToOptimalExercise => " +
				"Cant calc Yield Spread from Z Spread to optimal exercise for bonds w emb option");

		return calcYieldSpreadFromZSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblZSpread);
	}

	@Override public double calcZSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromASW (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblASW));
	}

	@Override public double calcZSpreadFromASW (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		return calcZSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcZSpreadFromASWToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblASW)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromASWToOptimalExercise => " +
				"Cant calc Z Spread from ASW to optimal exercise for bonds w emb option");

		return calcZSpreadFromASW (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblASW);
	}

	@Override public double calcZSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromBondBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblBondBasis));
	}

	@Override public double calcZSpreadFromBondBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		return calcZSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblBondBasis);
	}

	@Override public double calcZSpreadFromBondBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblBondBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromBondBasisToOptimalExercise => "
				+ "Cant calc Z Spread from Bond Basis to optimal exercise for bonds w emb option");

		return calcZSpreadFromBondBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblBondBasis);
	}

	@Override public double calcZSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromCreditBasis (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblCreditBasis));
	}

	@Override public double calcZSpreadFromCreditBasis (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		return calcZSpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcZSpreadFromCreditBasisToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblCreditBasis)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromCreditBasisToOptimalExercise => " +
				"Cant calc Z Spread from Credit Basis to optimal exercise for bonds w emb option");

		return calcZSpreadFromCreditBasis (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblCreditBasis);
	}

	@Override public double calcZSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromDiscountMargin (valParams, csqs, quotingParams, dblWorkoutDate,
				dblWorkoutFactor, dblDiscountMargin));
	}

	@Override public double calcZSpreadFromDiscountMargin (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		return calcZSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcZSpreadFromDiscountMarginToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblDiscountMargin)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception
				("BondComponent::calcZSpreadFromDiscountMarginToOptimalExercise => " +
					"Cant calc Z Spread from Discount Margin to optimal exercise for bonds w emb option");

		return calcZSpreadFromDiscountMargin (valParams, csqs, quotingParams,
			_periodParams.maturity(), 1., dblDiscountMargin);
	}

	@Override public double calcZSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromGSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblGSpread));
	}

	@Override public double calcZSpreadFromGSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcZSpreadFromGSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblGSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromGSpreadToOptimalExercise => " +
				"Cant calc Z Spread from G Spread to optimal exercise for bonds w emb option");

		return calcZSpreadFromGSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblGSpread);
	}

	@Override public double calcZSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromISpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblISpread));
	}

	@Override public double calcZSpreadFromISpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcZSpreadFromISpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblISpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromISpreadToOptimalExercise => " +
				"Cant calc Z Spread from I Spread to optimal exercise for bonds w emb option");

		return calcZSpreadFromISpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblISpread);
	}

	@Override public double calcZSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromOAS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblOAS));
	}

	@Override public double calcZSpreadFromOAS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		return calcZSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcZSpreadFromOASToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblOAS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromOASToOptimalExercise => " +
				"Cant calc Z Spread from OAS to optimal exercise for bonds w emb option");

		return calcZSpreadFromOAS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblOAS);
	}

	@Override public double calcZSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromPECS (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblPECS));
	}

	@Override public double calcZSpreadFromPECS (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		return calcZSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcZSpreadFromPECSToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPECS)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromPECSToOptimalExercise => " +
				"Cant calc Z Spread from PECS to optimal exercise for bonds w emb option");

		return calcZSpreadFromPECS (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPECS);
	}

	@Override public double calcZSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblPrice)
		throws java.lang.Exception
	{
		return new BondCalibrator (this).calibrateZSpreadFromPrice (valParams, csqs,
			ZERO_OFF_OF_RATES_INSTRUMENTS_DISCOUNT_CURVE, dblWorkoutDate, dblWorkoutFactor, dblPrice);
	}

	@Override public double calcZSpreadFromPrice (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcZSpreadFromPriceToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		org.drip.param.valuation.WorkoutInfo wi = exerciseYieldFromPrice (valParams, csqs,
			quotingParams, dblPrice);

		if (null == wi)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromPriceToOptimalExercise => " +
				"Cant calc Workout from Price to optimal exercise for bonds w emb option");

		return calcZSpreadFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
	}

	@Override public double calcZSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblTSYSpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromTSYSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblTSYSpread));
	}

	@Override public double calcZSpreadFromTSYSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		return calcZSpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcZSpreadFromTSYSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblPrice)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromTSYSpreadToOptimalExercise => " +
				"Cant calc Z Spread from TSY Spread to optimal exercise for bonds w emb option");

		return calcZSpreadFromTSYSpread (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblPrice);
	}

	@Override public double calcZSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYield (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYield));
	}

	@Override public double calcZSpreadFromYield (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYield)
		throws java.lang.Exception
	{
		return calcZSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYield);
	}

	@Override public double calcZSpreadFromYieldToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromYieldToOptimalExercise => " +
				"Cant calc Z Spread from Yield to optimal exercise for bonds w emb option");

		return calcZSpreadFromYield (valParams, csqs, quotingParams, _periodParams.maturity(), 1.,
			dblYieldSpread);
	}

	@Override public double calcZSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblWorkoutDate,
		final double dblWorkoutFactor,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromPrice (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
			calcPriceFromYieldSpread (valParams, csqs, quotingParams, dblWorkoutDate, dblWorkoutFactor,
				dblYieldSpread));
	}

	@Override public double calcZSpreadFromYieldSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		return calcZSpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public double calcZSpreadFromYieldSpreadToOptimalExercise (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final double dblYieldSpread)
		throws java.lang.Exception
	{
		if (null != _eosCall || null != _eosPut)
			throw new java.lang.Exception ("BondComponent::calcZSpreadFromYieldSpreadToOptimalExercise => " +
				"Cant calc Z Spread from Yield Spread to optimal exercise for bonds w emb option");

		return calcZSpreadFromYieldSpread (valParams, csqs, quotingParams, _periodParams.maturity(),
			1., dblYieldSpread);
	}

	@Override public org.drip.analytics.output.BondRVMeasures standardMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.param.valuation.WorkoutInfo wi,
		final double dblPrice)
	{
		if (null == valParams || null == csqs || null == wi || java.lang.Double.isNaN (dblPrice) ||
			valParams.valueDate() >= wi.date() + LEFT_EOS_SNIP)
			return null;

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
			dblDiscountMargin = calcDiscountMarginFromYield (valParams, csqs, quotingParams,
				wi.date(), wi.factor(), wi.yield());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		if (null == _fltParams) {
			try {
				dblZSpread = calcZSpreadFromPrice (valParams, csqs, quotingParams, wi.date(),
					wi.factor(), dblPrice);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}

			try {
				dblOASpread = calcOASFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
					dblPrice);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		}

		try {
			dblISpread = calcISpreadFromYield (valParams, csqs, quotingParams, wi.date(), wi.factor(),
				wi.yield());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblGSpread = calcGSpreadFromYield (valParams, csqs, quotingParams, wi.date(), wi.factor(),
				wi.yield());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblTSYSpread = calcTSYSpreadFromYield (valParams, csqs, quotingParams, wi.date(),
				wi.factor(), wi.yield());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblMacaulayDuration = calcMacaulayDurationFromPrice (valParams, csqs, quotingParams,
				wi.date(), wi.factor(), dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblModifiedDuration = calcModifiedDurationFromPrice (valParams, csqs, quotingParams,
				wi.date(), wi.factor(), dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblASW = aswFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(), dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblConvexity = calcConvexityFromPrice (valParams, csqs, quotingParams, wi.date(),
				wi.factor(), dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblCreditBasis = calcCreditBasisFromPrice (valParams, csqs, quotingParams, wi.date(),
				wi.factor(), dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblPECS = calcPECSFromPrice (valParams, csqs, quotingParams, wi.date(), wi.factor(),
				dblPrice);
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblBondBasis = bondBasisFromYield (valParams, csqs, quotingParams, wi.date(),
				wi.factor(), wi.yield());
		} catch (java.lang.Exception e) {
			if (!s_bSuppressErrors) e.printStackTrace();
		}

		try {
			dblYield01 = calcYield01FromYield (valParams, csqs, quotingParams, wi.date(), wi.factor(),
				wi.yield());
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
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == csqs) return null;

		if (null != pricerParams && null != pricerParams.calibParams()) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalibMeasures =
				calibMeasures (valParams, pricerParams, csqs, quotingParams);

			if (null != mapCalibMeasures && mapCalibMeasures.containsKey
				(pricerParams.calibParams().measure()))
				return mapCalibMeasures;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = calcFairMeasureSet
			(valParams, pricerParams, csqs, quotingParams);

		if (null == mapMeasures || null == csqs.productQuote (name())) return mapMeasures;

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

		if (null != csqs.productQuote (name()).quote ("Price")) {
			double dblMarketPrice = csqs.productQuote (name()).quote ("Price").getQuote ("mid");

			mapMeasures.put ("MarketInputType=CleanPrice", dblMarketPrice);

			wiMarket = exerciseYieldFromPrice (valParams, csqs, quotingParams, dblMarketPrice);
		} else if (null != csqs.productQuote (name()).quote ("CleanPrice")) {
			double dblCleanMarketPrice = csqs.productQuote (name()).quote ("CleanPrice").getQuote
				("mid");

			mapMeasures.put ("MarketInputType=CleanPrice", dblCleanMarketPrice);

			wiMarket = exerciseYieldFromPrice (valParams, csqs, quotingParams, dblCleanMarketPrice);
		} else if (null != csqs.productQuote (name()).quote ("QuotedMargin")) {
			double dblQuotedMargin = csqs.productQuote (name()).quote ("QuotedMargin").getQuote ("mid");

			mapMeasures.put ("MarketInputType=QuotedMargin", dblQuotedMargin);

			try {
				wiMarket = exerciseYieldFromPrice (valParams, csqs, quotingParams,
					calcPriceFromDiscountMargin (valParams, csqs, quotingParams, dblQuotedMargin));
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();
			}
		} else if (null != csqs.productQuote (name()).quote ("DirtyPrice")) {
			try {
				double dblDirtyMarketPrice = csqs.productQuote (name()).quote ("DirtyPrice").getQuote
					("mid");

				mapMeasures.put ("MarketInputType=DirtyPrice", dblDirtyMarketPrice);

				wiMarket = exerciseYieldFromPrice (valParams, csqs, quotingParams, dblDirtyMarketPrice -
					accrued (valParams.valueDate(), csqs));
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (null != csqs.productQuote (name()).quote ("TSYSpread")) {
			try {
				double dblTSYSpread = csqs.productQuote (name()).quote ("TSYSpread").getQuote ("mid");

				mapMeasures.put ("MarketInputType=TSYSpread", dblTSYSpread);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (maturity().julian(),
					getTsyBmkYield (valParams, csqs, maturity().julian()) + dblTSYSpread, 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (null != csqs.productQuote (name()).quote ("Yield")) {
			try {
				double dblYield = csqs.productQuote (name()).quote ("Yield").getQuote ("mid");

				mapMeasures.put ("MarketInputType=Yield", dblYield);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (maturity().julian(), dblYield,
					1., org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (null != csqs.productQuote (name()).quote ("ZSpread")) {
			try {
				double dblZSpread = csqs.productQuote (name()).quote ("ZSpread").getQuote ("mid");

				mapMeasures.put ("MarketInputType=ZSpread", dblZSpread);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (maturity().julian(),
					calcYieldFromZSpread (valParams, csqs, quotingParams, dblZSpread), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (null != csqs.productQuote (name()).quote ("ISpread")) {
			try {
				double dblISpread = csqs.productQuote (name()).quote ("ISpread").getQuote ("mid");

				mapMeasures.put ("MarketInputType=ISpread", dblISpread);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (maturity().julian(),
					calcYieldFromISpread (valParams, csqs, quotingParams, dblISpread), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (null != csqs.productQuote (name()).quote ("CreditBasis")) {
			try {
				double dblCreditBasis = csqs.productQuote (name()).quote ("CreditBasis").getQuote
					("mid");

				mapMeasures.put ("MarketInputType=CreditBasis", dblCreditBasis);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (maturity().julian(),
					calcYieldFromCreditBasis (valParams, csqs, quotingParams, dblCreditBasis), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		} else if (null != csqs.productQuote (name()).quote ("PECS")) {
			try {
				double dblCreditBasis = csqs.productQuote (name()).quote ("PECS").getQuote ("mid");

				mapMeasures.put ("MarketInputType=PECS", dblCreditBasis);

				wiMarket = new org.drip.param.valuation.WorkoutInfo (maturity().julian(),
					calcYieldFromPECS (valParams, csqs, quotingParams, dblCreditBasis), 1.,
						org.drip.param.valuation.WorkoutInfo.WO_TYPE_MATURITY);
			} catch (java.lang.Exception e) {
				if (!s_bSuppressErrors) e.printStackTrace();

				wiMarket = null;
			}
		}

		if (null != wiMarket) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapWorkoutMeasures =
				calcMarketMeasureSet (valParams, pricerParams, csqs, quotingParams, wiMarket);

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

			org.drip.state.identifier.CreditLabel[] aLSLCreditCurve = creditLabel();

			org.drip.analytics.definition.CreditCurve cc = null == aLSLCreditCurve || 0 ==
				aLSLCreditCurve.length ? null : csqs.creditCurve (aLSLCreditCurve[0]);

			if (null != mapMeasures.get ("FairYield") && !java.lang.Double.isNaN (wiMarket.yield())) {
				org.drip.param.market.CurveSurfaceQuoteSet csqsMarket =
					org.drip.param.creator.MarketParamsBuilder.Create
						((org.drip.analytics.rates.DiscountCurve) csqs.fundingCurve
							(fundingLabel()[0]).parallelShiftQuantificationMetric (wiMarket.yield() -
								mapMeasures.get ("FairYield")), csqs.govvieCurve
									(org.drip.state.identifier.GovvieLabel.Standard (payCurrency()[0])), cc,
										name(), csqs.productQuote (name()), csqs.quoteMap(), csqs.fixings());

				if (null != csqsMarket) {
					org.drip.analytics.output.BondWorkoutMeasures bwmMarket = calcBondWorkoutMeasures
						(valParams, pricerParams, csqsMarket, wiMarket.date(), wiMarket.factor());

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
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
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
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
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
				throw new java.lang.Exception
					("BondComponent::BondCalibrator ctr => No NULL bond into BondCalibrator constructor");
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
				public double evaluate (
					final double dblYield)
					throws java.lang.Exception
				{
					return _bond.calcPriceFromYield (valParams, csqs, null, dblWorkoutDate,
						dblWorkoutFactor, dblYield) - dblPrice;
				}

				@Override public double integrate (
					final double dblBegin,
					final double dblEnd)
					throws java.lang.Exception
				{
					return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
				}
			};

			org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
				org.drip.quant.solver1D.FixedPointFinderNewton (0., ofYieldToPrice, true).findRoot();

			if (null == rfop || !rfop.containsRoot()) {
				rfop = new org.drip.quant.solver1D.FixedPointFinderBrent (0., ofYieldToPrice, true).findRoot();

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
				public double evaluate (
					final double dblZSpread)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedZC (valParams, csqs, null, iZeroCurveBaseDC, dblWorkoutDate,
						dblWorkoutFactor, dblZSpread) - dblPrice;
				}

				@Override public double integrate (
					final double dblBegin,
					final double dblEnd)
					throws java.lang.Exception
				{
					return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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
				public double evaluate (
					final double dblZSpread)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor,
						dblZSpread) - dblPriceCalib;
				}

				@Override public double integrate (
					final double dblBegin,
					final double dblEnd)
					throws java.lang.Exception
				{
					return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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
		 * @param quotingParams Quoting Parameters
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
			final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
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
				public double evaluate (
					final double dblZSpread)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedDC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor,
						dblZSpread) - dblPriceCalib;
				}

				@Override public double integrate (
					final double dblBegin,
					final double dblEnd)
					throws java.lang.Exception
				{
					return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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
				public double evaluate (
					final double dblCreditBasis)
					throws java.lang.Exception
				{
					return _bond.priceFromBumpedCC (valParams, csqs, dblWorkoutDate, dblWorkoutFactor,
						dblCreditBasis, bFlatCalib) - dblPriceCalib;
				}

				@Override public double integrate (
					final double dblBegin,
					final double dblEnd)
					throws java.lang.Exception
				{
					return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
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
		for (org.drip.analytics.cashflow.CompositePeriod period : _periodParams.periods())
			System.out.println ("\t" + org.drip.analytics.date.JulianDate.fromJulian (period.startDate()) +
				"->" + org.drip.analytics.date.JulianDate.fromJulian (period.endDate()) + "    " +
					period.accrualDCF (period.endDate()));
	}
}
