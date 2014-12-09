
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
 * CDSComponent implements the credit default swap product contract details. It exposes the following
 *  functionality:
 *  - Methods to extract effective date, maturity date, coupon, coupon day count, coupon frequency,
 *  	contingent credit, currency, basket notional, credit valuation parameters, and optionally the
 *  	outstanding notional schedule.
 *  - Methods to compute the Jacobians to/from quote-to-latent state/manifest measures
 *  - Serialization into and de-serialization out of byte arrays
 *  - CDS specific methods such as such loss metric/Jacobian estimation, quote flat spread calibration etc:
 *
 * @author Lakshmi Krishnamurthy
 *
 */

public class CDSComponent extends org.drip.product.definition.CreditDefaultSwap {
	private double _dblNotional = 100.;
	private java.lang.String _strCode = "";
	private java.lang.String _strName = "";
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private java.lang.String _strCouponCurrency = "";
	private double _dblCoupon = java.lang.Double.NaN;
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.CreditSetting _crValParams = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;
	private java.util.List<org.drip.analytics.cashflow.CompositePeriod> _lsCouponPeriod = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		return null;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> measures (
		final java.lang.String strMeasureSetPrefix,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == pricerParams || null == csqs) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == cc || null == dcFunding) return null;

		long lStart = System.nanoTime();

		double dblLossPV = 0.;
		double dblExpLoss = 0.;
		double dblAccrued01 = 0.;
		double dblDirtyDV01 = 0.;
		double dblLossNoRecPV = 0.;
		double dblExpLossNoRec = 0.;
		boolean bFirstPeriod = true;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblAccrualDays = java.lang.Double.NaN;

		double dblValueDate = valParams.valueDate();

		try {
			for (org.drip.analytics.cashflow.CompositePeriod period : _lsCouponPeriod) {
				double dblPayDate = period.payDate();

				if (dblPayDate < dblValueDate) continue;

				double dblEndDate = period.endDate();

				double dblStartDate = period.startDate();

				double dblPeriodNotional = notional (dblStartDate, dblValueDate);

				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (dblStartDate < dblValueDate) {
						dblAccrualDays = dblValueDate - dblStartDate;

						dblAccrued01 = period.accrualDCF (dblValueDate) * 0.01 * dblPeriodNotional;
					}
				}

				double dblSurvProb = pricerParams.survivalToPayDate() ? cc.survival (dblPayDate) :
					cc.survival (dblEndDate);

				dblDirtyDV01 += 0.01 * period.couponDCF() * dcFunding.df (dblPayDate) * dblSurvProb *
					dblPeriodNotional;

				for (org.drip.analytics.cashflow.LossQuadratureMetrics lp : period.lossMetrics (this,
					valParams, pricerParams, dblEndDate, csqs)) {
					if (null == lp) continue;

					double dblSubPeriodEnd = lp.end();

					double dblSubPeriodStart = lp.start();

					double dblSubPeriodDF = dcFunding.effectiveDF (dblSubPeriodStart +
						_crValParams._iDefPayLag, dblSubPeriodEnd + _crValParams._iDefPayLag);

					double dblSubPeriodNotional = notional (dblSubPeriodStart, dblSubPeriodEnd);

					double dblSubPeriodSurvival = cc.survival (dblSubPeriodStart) - cc.survival
						(dblSubPeriodEnd);

					double dblRec = _crValParams._bUseCurveRec ? cc.effectiveRecovery (dblSubPeriodStart,
						dblSubPeriodEnd) : _crValParams._dblRecovery;

					double dblSubPeriodExpLoss = (1. - dblRec) * 100. * dblSubPeriodSurvival *
						dblSubPeriodNotional;
					double dblSubPeriodExpLossNoRec = 100. * dblSubPeriodSurvival * dblSubPeriodNotional;
					dblLossPV += dblSubPeriodExpLoss * dblSubPeriodDF;
					dblLossNoRecPV += dblSubPeriodExpLossNoRec * dblSubPeriodDF;
					dblExpLoss += dblSubPeriodExpLoss;
					dblExpLossNoRec += dblSubPeriodExpLossNoRec;

					dblDirtyDV01 += 0.01 * lp.accrualDCF() * dblSubPeriodSurvival * dblSubPeriodDF *
						dblSubPeriodNotional;
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		try {
			dblCashPayDF = dcFunding.df (null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (dblValueDate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		dblLossPV /= dblCashPayDF;
		dblDirtyDV01 /= dblCashPayDF;
		dblLossNoRecPV /= dblCashPayDF;
		double dblNotlFactor = _dblNotional * 0.01;
		double dblCleanDV01 = dblDirtyDV01 - dblAccrued01;
		double dblCleanPV = dblCleanDV01 * 10000. * _dblCoupon - dblLossPV;
		double dblDirtyPV = dblDirtyDV01 * 10000. * _dblCoupon - dblLossPV;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put (strMeasureSetPrefix + "AccrualDays", dblAccrualDays);

		mapResult.put (strMeasureSetPrefix + "Accrued", dblAccrued01 * _dblCoupon * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "Accrued01", dblAccrued01 * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "CleanDV01", dblCleanDV01 * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "CleanPV", dblCleanPV * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "DV01", dblDirtyDV01 * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "DirtyDV01", dblDirtyDV01 * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "DirtyPV", dblDirtyPV * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "ExpLoss", dblExpLoss * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "ExpLossNoRec", dblExpLossNoRec * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "FairPremium", dblLossPV / dblCleanDV01);

		mapResult.put (strMeasureSetPrefix + "LossNoRecPV", dblLossNoRecPV * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "LossPV", dblLossPV * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "ParSpread", dblLossPV / dblCleanDV01);

		mapResult.put (strMeasureSetPrefix + "PremiumPV", dblDirtyDV01 * _dblCoupon * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "PV", dblDirtyPV * dblNotlFactor);

		mapResult.put (strMeasureSetPrefix + "Upfront", dblCleanPV * dblNotlFactor);

		try {
			double dblValueNotional = notional (dblValueDate);

			mapResult.put (strMeasureSetPrefix + "CleanPrice", 100. * (1. + (dblCleanPV / _dblNotional /
				dblValueNotional)));

			mapResult.put (strMeasureSetPrefix + "DirtyPrice", 100. * (1. + (dblDirtyPV / _dblNotional /
				dblValueNotional)));

			mapResult.put (strMeasureSetPrefix + "LossOnInstantaneousDefault", _dblNotional * (1. -
				cc.recovery (dblValueDate)));

			mapResult.put (strMeasureSetPrefix + "Price", 100. * (1. + (dblCleanPV / _dblNotional /
				dblValueNotional)));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put (strMeasureSetPrefix + "CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	private org.drip.quant.calculus.WengertJacobian calcPeriodOnDefaultPVDFMicroJack (
		final double dblFairPremium,
		final org.drip.analytics.cashflow.CompositePeriod period,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		int iNumParameters = 0;
		org.drip.quant.calculus.WengertJacobian wjPeriodOnDefaultPVDF = null;

		for (org.drip.analytics.cashflow.LossQuadratureMetrics lpcf : period.lossMetrics (this, valParams,
			pricerParams, period.endDate(), csqs)) {
			org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dcFunding.jackDDFDManifestMeasure (0.5
				* (lpcf.start() + lpcf.end()) + _crValParams._iDefPayLag, "Rate");

			try {
				if (null == wjPeriodOnDefaultPVDF)
					wjPeriodOnDefaultPVDF = new org.drip.quant.calculus.WengertJacobian (1, iNumParameters =
						wjPeriodPayDFDF.numParameters());

				double dblPeriodIncrementalCashFlow = notional (lpcf.start(), lpcf.end()) * (dblFairPremium *
					lpcf.accrualDCF() - 1. + lpcf.effectiveRecovery()) * (lpcf.startSurvival() -
						lpcf.endSurvival());

				for (int k = 0; k < iNumParameters; ++k) {
					if (!wjPeriodOnDefaultPVDF.accumulatePartialFirstDerivative (0, k,
						wjPeriodPayDFDF.firstDerivative (0, k) * dblPeriodIncrementalCashFlow))
						return null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return wjPeriodOnDefaultPVDF;
	}

	private PeriodLossMicroJack calcPeriodLossMicroJack (
		final org.drip.analytics.cashflow.CompositePeriod period,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		PeriodLossMicroJack plmj = null;

		for (org.drip.analytics.cashflow.LossQuadratureMetrics lpcf : period.lossMetrics (this, valParams,
			pricerParams, period.endDate(), csqs)) {
			double dblPeriodNotional = java.lang.Double.NaN;
			double dblPeriodIncrementalLoss = java.lang.Double.NaN;
			double dblPeriodIncrementalAccrual = java.lang.Double.NaN;
			double dblPeriodIncrementalSurvival = java.lang.Double.NaN;

			double dblPeriodEffectiveDate = 0.5 * (lpcf.start() + lpcf.end());

			org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dcFunding.jackDDFDManifestMeasure
				(dblPeriodEffectiveDate + _crValParams._iDefPayLag, "Rate");

			try {
				dblPeriodNotional = notional (lpcf.start(), lpcf.end());

				dblPeriodIncrementalSurvival = lpcf.startSurvival() - lpcf.endSurvival();

				dblPeriodIncrementalLoss = dblPeriodNotional * (1. - lpcf.effectiveRecovery()) *
					dblPeriodIncrementalSurvival;

				dblPeriodIncrementalAccrual = dblPeriodNotional * lpcf.accrualDCF() *
					dblPeriodIncrementalSurvival;

				if (null == plmj) plmj = new PeriodLossMicroJack (wjPeriodPayDFDF.numParameters());

				plmj._dblAccrOnDef01 += dblPeriodIncrementalAccrual * dcFunding.df (dblPeriodEffectiveDate +
					_crValParams._iDefPayLag);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			for (int k = 0; k < wjPeriodPayDFDF.numParameters(); ++k) {
				if (!plmj._wjLossPVMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodIncrementalLoss
					* wjPeriodPayDFDF.firstDerivative (0, k)))
					return null;

				if (!plmj._wjAccrOnDef01MicroJack.accumulatePartialFirstDerivative (0, k,
					dblPeriodIncrementalAccrual * wjPeriodPayDFDF.firstDerivative (0, k)))
					return null;
			}
		}

		return plmj;
	}

	private org.drip.analytics.cashflow.CompositePeriod calcCurrentPeriod (
		final double dblDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.analytics.cashflow.CompositePeriod cpFirst = _lsCouponPeriod.get (0);

		if (dblDate <= cpFirst.startDate()) return cpFirst;

		for (org.drip.analytics.cashflow.CompositePeriod period : _lsCouponPeriod) {
			try {
				if (period.contains (dblDate)) return period;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	/**
	 * CDSComponent constructor: Most generic CDS creation functionality
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param dblCoupon Coupon
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon DC
	 * @param strAccrualDC Accrual DC
	 * @param strFloatingRateIndex Floating Rate Index
	 * @param bConvCDS Is CDS Conventional
	 * @param dapEffective Effective DAP
	 * @param dapMaturity Maturity DAP
	 * @param dapPeriodStart Period Start DAP
	 * @param dapPeriodEnd Period End DAP
	 * @param dapAccrualStart Accrual Start DAP
	 * @param dapAccrualEnd Accrual End DAP
	 * @param dapPay Pay DAP
	 * @param dapReset Reset DAP
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Notional Amount
	 * @param strCouponCurrency Coupon Currency
	 * @param crValParams Credit Valuation Parameters
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception
	 */

	public CDSComponent (
		final double dblEffective,
		final double dblMaturity,
		final double dblCoupon,
		final int iFreq,
		final java.lang.String strCouponDC,
		final java.lang.String strAccrualDC,
		final java.lang.String strFloatingRateIndex,
		final boolean bConvCDS,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strCouponCurrency,
		final org.drip.product.params.CreditSetting crValParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == strCouponCurrency || (_strCouponCurrency = strCouponCurrency).isEmpty() || null ==
			crValParams || !org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblCoupon))
			throw new java.lang.Exception ("CDSComponent ctr: Invalid params!");

		_dblCoupon = dblCoupon;
		_crValParams = crValParams;
		java.lang.String strTenor = (12 / iFreq) + "M";

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.BulletSchedule();

		org.drip.param.period.UnitCouponAccrualSetting ucas = new
			org.drip.param.period.UnitCouponAccrualSetting (iFreq, strCouponDC, _bApplyCpnEOMAdj,
				strAccrualDC, _bApplyAccEOMAdj, _strCouponCurrency, true);

		org.drip.param.period.ComposableFixedUnitSetting cfus = new
			org.drip.param.period.ComposableFixedUnitSetting (strTenor,
				org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null, 0., 0.,
					_strCouponCurrency);

		org.drip.param.period.CompositePeriodSetting cps = new org.drip.param.period.CompositePeriodSetting
			(iFreq, strTenor, _strCouponCurrency, null,
				org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
					_dblNotional = dblNotional, null, _notlSchedule, null, null == _crValParams ? null :
						org.drip.state.identifier.CreditLabel.Standard (_crValParams._strCC));

		java.util.List<java.lang.Double> lsStreamEdgeDate =
			org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (new
				org.drip.analytics.date.JulianDate (_dblEffective = dblEffective), new
					org.drip.analytics.date.JulianDate (_dblMaturity = dblMaturity), strTenor, dapAccrualEnd,
						org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

			if (null == (_lsCouponPeriod =
				org.drip.analytics.support.CompositePeriodBuilder.FixedCompositeUnit (lsStreamEdgeDate, cps,
					ucas, cfus)) || 0 == _lsCouponPeriod.size())
				throw new java.lang.Exception ("CDSComponent ctr: Cannot make Coupon Period List!");
	}

	@Override public java.lang.String primaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	public boolean setName (
		final java.lang.String strName)
	{
		_strName = strName;
		return true;
	}

	@Override public java.lang.String name()
	{
		if (null != _strName && !_strName.isEmpty()) return _strName;

		return "CDS=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public java.util.List<java.lang.String> couponCurrency()
	{
		java.util.List<java.lang.String> lsCouponCurrency = new java.util.ArrayList<java.lang.String>();

		lsCouponCurrency.add (_strCouponCurrency);

		return lsCouponCurrency;
	}

	@Override public java.lang.String payCurrency()
	{
		return _strCouponCurrency;
	}

	@Override public java.lang.String principalCurrency()
	{
		return null;
	}

	@Override public double initialNotional()
	{
		return _dblNotional;
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("CDSComponent::notional => Bad date");

		return _notlSchedule.factor (dblDate);
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("CDSComponent::notional => Bad date");

		return _notlSchedule.factor (dblDate1, dblDate2);
	}

	@Override public double recovery (
		final double dblDate,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || null == cc)
			throw new java.lang.Exception ("CDSComponent::recovery => Bad inputs");

		return _crValParams._bUseCurveRec ? cc.recovery (dblDate) : _crValParams._dblRecovery;
	}

	@Override public double recovery (
		final double dblDateStart,
		final double dblDateEnd,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDateStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDateEnd) || null == cc)
			throw new java.lang.Exception ("CDSComponent::recovery: Bad inputs");

		return _crValParams._bUseCurveRec ? cc.effectiveRecovery (dblDateStart, dblDateEnd) :
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
			org.drip.analytics.cashflow.CompositePeriod period = calcCurrentPeriod (dblAccrualEndDate);

			return null == period ? null : period.couponMetrics (dblAccrualEndDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public int freq()
	{
		return couponPeriods().get (0).freq();
	}

	/**
	 * Reset the CDS's coupon
	 * 
	 * @param dblCoupon The new Coupon
	 * 
	 * @return The old Coupon
	 * 
	 * @throws java.lang.Exception Thrown if the coupon cannot be reset
	 */

	public double resetCoupon (
		final double dblCoupon)
		throws java.lang.Exception
	{
		double dblOldCoupon = _dblCoupon;

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblCoupon = dblCoupon))
			throw new java.lang.Exception ("CDSComponent::resetCoupon => Bad coupon Input!");

		return dblOldCoupon;
	}

	@Override public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return null == _crValParams || null == _crValParams._strCC || _crValParams._strCC.isEmpty() ? null :
			org.drip.state.identifier.CreditLabel.Standard (_crValParams._strCC);
	}

	@Override public java.util.List<org.drip.state.identifier.ForwardLabel> forwardLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return org.drip.state.identifier.FundingLabel.Standard (_strCouponCurrency);
	}

	@Override public java.util.List<org.drip.state.identifier.FXLabel> fxLabel()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effectiveDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturityDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.CompositePeriod> couponPeriods()
	{
		return _lsCouponPeriod;
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> lossFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == valParams || null == pricerParams) return null;

		java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLP = new
			java.util.ArrayList<org.drip.analytics.cashflow.LossQuadratureMetrics>();

		double dblValueDate = valParams.valueDate();

		for (org.drip.analytics.cashflow.CompositePeriod period : _lsCouponPeriod) {
			if (null == period || period.endDate() < dblValueDate) continue;

			java.util.List<org.drip.analytics.cashflow.LossQuadratureMetrics> sLPSub = period.lossMetrics
				(this, valParams, pricerParams, period.endDate(), csqs);

			if (null != sLPSub) sLP.addAll (sLPSub);
		}

		return sLP;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFairMeasures = measures ("",
			valParams, pricerParams, csqs, vcp);

		if (null == mapFairMeasures) return null;

		org.drip.quant.common.CollectionUtil.MergeWithMain (mapFairMeasures,
			org.drip.quant.common.CollectionUtil.PrefixKeys (mapFairMeasures, "Fair"));

		java.lang.String strName = name();

		org.drip.param.definition.ProductQuote cq = csqs.productQuote (strName);

		if ((null != pricerParams && null != pricerParams.calibParams()) || null == mapFairMeasures || null
			== cq)
			return mapFairMeasures;

		double dblCreditBasis = java.lang.Double.NaN;
		double dblMarketMeasure = java.lang.Double.NaN;
		org.drip.analytics.definition.CreditCurve ccMarket = null;

		if (cq.containsQuote ("Price"))
			mapFairMeasures.put ("MarketInputType=Price", dblMarketMeasure = cq.quote ("Price").value
				("mid"));
		else if (cq.containsQuote ("CleanPrice"))
			mapFairMeasures.put ("MarketInputType=CleanPrice", dblMarketMeasure = cq.quote
				("CleanPrice").value ("mid"));
		else if (cq.containsQuote ("Upfront"))
			mapFairMeasures.put ("MarketInputType=Upfront", dblMarketMeasure = cq.quote ("Upfront").value
				("mid"));
		else if (cq.containsQuote ("FairPremium"))
			mapFairMeasures.put ("MarketInputType=FairPremium", dblMarketMeasure = cq.quote
				("FairPremium").value ("mid"));
		else if (cq.containsQuote ("PV"))
			mapFairMeasures.put ("MarketInputType=PV", dblMarketMeasure = cq.quote ("PV").value ("mid"));
		else if (cq.containsQuote ("CleanPV"))
			mapFairMeasures.put ("MarketInputType=CleanPV", dblMarketMeasure = cq.quote ("CleanPV").value
				("mid"));

		try {
			SpreadCalibOP scop = new SpreadCalibrator (this,
				SpreadCalibrator.CALIBRATION_TYPE_NODE_PARALLEL_BUMP).calibrateHazardFromPrice (valParams,
					new org.drip.param.pricer.PricerParams (7,
						org.drip.param.definition.CalibrationParams.MakeStdCalibParams(), false,
							org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP, false), csqs,
								vcp, dblMarketMeasure);

			if (null != scop) {
				ccMarket = scop._ccCalib;
				dblCreditBasis = scop._dblCalibResult;
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (org.drip.quant.common.NumberUtil.IsValid (dblCreditBasis)) {
			mapFairMeasures.put ("MarketCreditBasis", dblCreditBasis);

			org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

			try {
				ccMarket = (org.drip.analytics.definition.CreditCurve) cc.customTweakManifestMeasure
					("FairPremium", new org.drip.param.definition.ResponseValueTweakParams
						(org.drip.param.definition.ResponseValueTweakParams.MANIFEST_MEASURE_FLAT_TWEAK,
							false, dblCreditBasis));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = mapFairMeasures;

		if (null != ccMarket) {
			org.drip.param.market.CurveSurfaceQuoteSet csqsMarket =
				org.drip.param.creator.MarketParamsBuilder.Create (csqs.fundingCurve (fundingLabel()),
					csqs.govvieCurve (org.drip.state.identifier.GovvieLabel.Standard (payCurrency())),
						ccMarket, strName, csqs.productQuote (strName), csqs.quoteMap(), csqs.fixings());

			if (null != csqsMarket) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMarketMeasures =
					measures ("", valParams, pricerParams, csqsMarket, vcp);

				if (null != mapMarketMeasures) {
					org.drip.quant.common.CollectionUtil.MergeWithMain (mapMarketMeasures,
						org.drip.quant.common.CollectionUtil.PrefixKeys (mapMarketMeasures, "Market"));

					org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, mapMarketMeasures);
				}
			}
		}

		return mapMeasures;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("AccrualDays");

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("Accrued01");

		setstrMeasureNames.add ("CleanDV01");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("DirtyDV01");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("ExpLoss");

		setstrMeasureNames.add ("ExpLossNoRec");

		setstrMeasureNames.add ("FairAccrualDays");

		setstrMeasureNames.add ("FairAccrued");

		setstrMeasureNames.add ("FairAccrued01");

		setstrMeasureNames.add ("FairCleanDV01");

		setstrMeasureNames.add ("FairCleanPV");

		setstrMeasureNames.add ("FairDirtyDV01");

		setstrMeasureNames.add ("FairDirtyPV");

		setstrMeasureNames.add ("FairDV01");

		setstrMeasureNames.add ("FairExpLoss");

		setstrMeasureNames.add ("FairExpLossNoRec");

		setstrMeasureNames.add ("FairFairPremium");

		setstrMeasureNames.add ("FairLossNoRecPV");

		setstrMeasureNames.add ("FairLossPV");

		setstrMeasureNames.add ("FairParSpread");

		setstrMeasureNames.add ("FairPremium");

		setstrMeasureNames.add ("FairPremiumPV");

		setstrMeasureNames.add ("FairPV");

		setstrMeasureNames.add ("FairUpfront");

		setstrMeasureNames.add ("LossNoRecPV");

		setstrMeasureNames.add ("LossPV");

		setstrMeasureNames.add ("MarketAccrualDays");

		setstrMeasureNames.add ("MarketAccrued");

		setstrMeasureNames.add ("MarketAccrued01");

		setstrMeasureNames.add ("MarketCleanDV01");

		setstrMeasureNames.add ("MarketCleanPV");

		setstrMeasureNames.add ("MarketDirtyDV01");

		setstrMeasureNames.add ("MarketDirtyPV");

		setstrMeasureNames.add ("MarketDV01");

		setstrMeasureNames.add ("MarketExpLoss");

		setstrMeasureNames.add ("MarketExpLossNoRec");

		setstrMeasureNames.add ("MarketFairPremium");

		setstrMeasureNames.add ("MarketLossNoRecPV");

		setstrMeasureNames.add ("MarketLossPV");

		setstrMeasureNames.add ("MarketParSpread");

		setstrMeasureNames.add ("MarketPremiumPV");

		setstrMeasureNames.add ("MarketPV");

		setstrMeasureNames.add ("MarketUpfront");

		setstrMeasureNames.add ("ParSpread");

		setstrMeasureNames.add ("PremiumPV");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
		valueFromQuotedSpread (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.pricer.PricerParams pricerParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final org.drip.param.valuation.ValuationCustomizationParams vcp,
			final double dblFixCoupon,
			final double dblQuotedSpread)
	{
		if (null == valParams || !org.drip.quant.common.NumberUtil.IsValid (dblFixCoupon) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblQuotedSpread))
			return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[] {this};
		org.drip.analytics.definition.CreditCurve cc = null;
		double[] adblRestorableCDSCoupon = new double[1];
		adblRestorableCDSCoupon[0] = _dblCoupon;
		_dblCoupon = dblFixCoupon;

		if (null != csqs) {
			org.drip.product.definition.CalibratableFixedIncomeComponent[] aMktComp = null;

			cc = csqs.creditCurve (creditLabel());

			if (null != cc && null != (aMktComp = cc.calibComp())) {
				int iNumComp = aMktComp.length;

				if (0 != iNumComp) {
					aComp = aMktComp;
					adblRestorableCDSCoupon = new double[iNumComp];

					for (int i = 0; i < iNumComp; ++i) {
						if (null != aComp[i] && aComp[i] instanceof
							org.drip.product.definition.CreditDefaultSwap) {
							try {
								org.drip.analytics.output.CompositePeriodCouponMetrics cpcm = couponMetrics
									(valParams.valueDate(), valParams, csqs);

								if (null == cpcm) return null;

								adblRestorableCDSCoupon[i] = cpcm.rate();

								((org.drip.product.definition.CreditDefaultSwap) aComp[i]).resetCoupon
									(dblFixCoupon);
							} catch (java.lang.Exception e) {
								e.printStackTrace();

								return null;
							}
						}
					}
				}
			}
		}

		int iNumCalibComp = aComp.length;
		double[] adblQS = new double[iNumCalibComp];
		org.drip.analytics.definition.CreditCurve ccQS = null;
		java.lang.String[] astrCalibMeasure = new java.lang.String[iNumCalibComp];

		for (int i = 0; i < iNumCalibComp; ++i) {
			adblQS[i] = dblQuotedSpread;
			astrCalibMeasure[i] = "FairPremium";
		}

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		try {
			if (null == (ccQS = org.drip.param.creator.CreditScenarioCurveBuilder.CreateCreditCurve
				(creditLabel().fullyQualifiedName(), new org.drip.analytics.date.JulianDate
					(valParams.valueDate()), aComp, dcFunding, adblQS, astrCalibMeasure, null != cc ?
						cc.recovery (valParams.valueDate()) : 0.4, false)))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapPV = value (valParams,
			pricerParams, org.drip.param.creator.MarketParamsBuilder.Credit (dcFunding, ccQS), vcp);

		for (int i = 0; i < iNumCalibComp; ++i) {
			try {
				((org.drip.product.definition.CreditDefaultSwap) aComp[i]).resetCoupon
					(adblRestorableCDSCoupon[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return mapPV;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == csqs) return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve (fundingLabel());

		if (null == cc || null == dc) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value (valParams,
			pricerParams, csqs, vcp);

		if (null == mapMeasures) return null;

		double dblPV = mapMeasures.get ("PV");

		double dblFairPremium = mapMeasures.get ("FairPremium");

		try {
			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = null;

			for (org.drip.analytics.cashflow.CompositePeriod p : _lsCouponPeriod) {
				double dblPeriodPayDate = p.payDate();

				if (dblPeriodPayDate < dblValueDate) continue;

				org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dc.jackDDFDManifestMeasure
					(dblPeriodPayDate, "Rate");

				org.drip.quant.calculus.WengertJacobian wjPeriodOnDefaultPVMicroJack =
					calcPeriodOnDefaultPVDFMicroJack (dblFairPremium, p, valParams, pricerParams, csqs);

				if (null == wjPeriodPayDFDF | null == wjPeriodOnDefaultPVMicroJack) continue;

				if (null == wjPVDFMicroJack)
					wjPVDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
						wjPeriodPayDFDF.numParameters());

				double dblPeriodCashFlow = dblFairPremium * notional (p.startDate(), p.endDate()) *
					p.couponDCF() * cc.survival (dblPeriodPayDate);

				for (int k = 0; k < wjPeriodPayDFDF.numParameters(); ++k) {
					if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodCashFlow *
						wjPeriodPayDFDF.firstDerivative (0, k) + wjPeriodOnDefaultPVMicroJack.firstDerivative
							(0, k)))
						return null;
				}
			}

			return adjustForCashSettle (valParams.cashPayDate(), dblPV, dc, wjPVDFMicroJack) ?
				wjPVDFMicroJack : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == strManifestMeasure || null == csqs) return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		org.drip.analytics.definition.CreditCurve cc = csqs.creditCurve (creditLabel());

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == cc || null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure) || "FairPremium".equalsIgnoreCase
			(strManifestMeasure) || "ParSpread".equalsIgnoreCase (strManifestMeasure)) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, vcp);

			if (null == mapMeasures) return null;

			double dblFairPremium = mapMeasures.get ("FairPremium");

			try {
				double dblDV01 = 0.;
				org.drip.quant.calculus.WengertJacobian wjFairPremiumDFMicroJack = null;

				for (org.drip.analytics.cashflow.CompositePeriod p : _lsCouponPeriod) {
					double dblPeriodPayDate = p.payDate();

					if (dblPeriodPayDate < dblValueDate) continue;

					double dblPeriodEndDate = p.endDate();

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF =
						dcFunding.jackDDFDManifestMeasure (dblPeriodEndDate, "Rate");

					PeriodLossMicroJack plmj = calcPeriodLossMicroJack (p, valParams, pricerParams, csqs);

					if (null == wjPeriodPayDFDF | null == plmj) continue;

					if (null == wjFairPremiumDFMicroJack)
						wjFairPremiumDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodPayDFDF.numParameters());

					double dblPeriodCoupon01 = notional (p.startDate(), dblPeriodEndDate) * p.couponDCF() *
						cc.survival (dblPeriodEndDate);

					dblDV01 += dblPeriodCoupon01 * dcFunding.df (dblPeriodPayDate) + plmj._dblAccrOnDef01;

					for (int k = 0; k < wjPeriodPayDFDF.numParameters(); ++k) {
						double dblPeriodNetLossJack = plmj._wjLossPVMicroJack.firstDerivative (0, k) -
							dblFairPremium * (plmj._wjAccrOnDef01MicroJack.firstDerivative (0, k) +
								dblPeriodCoupon01 * wjPeriodPayDFDF.firstDerivative (0, k));

						if (!wjFairPremiumDFMicroJack.accumulatePartialFirstDerivative (0, k,
							dblPeriodNetLossJack))
							return null;
					}
				}

				return wjFairPremiumDFMicroJack.scale (dblDV01) ? wjFairPremiumDFMicroJack : null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

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

	/**
	 * Calibrate the CDS's flat spread from the calculated up-front points
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param csqs ComponentMarketParams
	 * 
	 * @return Calibrated flat spread
	 * 
	 * @throws java.lang.Exception Thrown if cannot calibrate
	 */

	public double calibFlatSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
		throws java.lang.Exception
	{
		SpreadCalibOP scop = new SpreadCalibrator (this,
			SpreadCalibrator.CALIBRATION_TYPE_FLAT_CURVE_NODES).calibrateHazardFromPrice (valParams,
				pricerParams, csqs, vcp, measureValue (valParams, pricerParams, csqs, vcp, "Upfront"));

		if (null == scop)
			throw new java.lang.Exception ("CDSComponent::calibFlatSpread => Cannot calibrate flat spread!");

		return scop._dblCalibResult;
	}

	/**
	 *	CDS spread calibration output
	 *
	 * @author Lakshmi Krishnamurthy
	 */

	public class SpreadCalibOP {
		public double _dblCalibResult = java.lang.Double.NaN;
		public org.drip.analytics.definition.CreditCurve _ccCalib = null;

		public SpreadCalibOP (
			final double dblCalibResult,
			final org.drip.analytics.definition.CreditCurve ccCalib)
			throws java.lang.Exception
		{
			if (!org.drip.quant.common.NumberUtil.IsValid (_dblCalibResult = dblCalibResult) || null ==
				(_ccCalib = ccCalib))
				throw new java.lang.Exception ("CDSComponent::SpreadCalibOP ctr => Invalid inputs!");
		}
	}

	/**
	 *	Implementation of the CDS spread calibrator
	 *
	 * @author Lakshmi Krishnamurthy
	 */

	public class SpreadCalibrator {
		private org.drip.product.definition.CreditDefaultSwap _cds = null;

		/*
		 * Calibration Type
		 */

		public static final int CALIBRATION_TYPE_FLAT_INSTRUMENT_NODE = 1;
		public static final int CALIBRATION_TYPE_FLAT_CURVE_NODES = 2;
		public static final int CALIBRATION_TYPE_NODE_PARALLEL_BUMP = 4;

		private int _iCalibType = CALIBRATION_TYPE_FLAT_CURVE_NODES;

		/**
		 * Constructor: Construct the SpreadCalibrator from the CDS parent, and whether the calibration is
		 * 	off of a single node
		 * 
		 * @param cds CDS parent
		 * @param iCalibType Calibration type indicating whether the calibration is PARALLEL, FLAT SINGLE
		 * 		NODE, or FLAT TERM
		 * 
		 * @throws java.lang.Exception Thrown if inputs are invalid
		 */

		public SpreadCalibrator (
			final org.drip.product.definition.CreditDefaultSwap cds,
			final int iCalibType)
			throws java.lang.Exception
		{
			if (null == (_cds = cds) || (CALIBRATION_TYPE_FLAT_INSTRUMENT_NODE != (_iCalibType = iCalibType)
				&& CALIBRATION_TYPE_FLAT_CURVE_NODES != iCalibType && CALIBRATION_TYPE_NODE_PARALLEL_BUMP !=
					iCalibType))
				throw new java.lang.Exception ("CDSComponent::SpreadCalibrator ctr => Invalid inputs!");
		}

		/**
		 * Calibrate the hazard rate from calibration price
		 * 
		 * @param valParams ValuationParams
		 * @param pricerParams PricerParams
		 * @param csqs ComponentMarketParams
		 * @param dblPriceCalib Market price to be calibrated
		 * @param vcp Valuation Customization Parameters
		 * 
		 * @return Calibrated hazard
		 * 
		 * @throws java.lang.Exception Thrown if calibration failed
		 */

		public SpreadCalibOP calibrateHazardFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.pricer.PricerParams pricerParams,
			final org.drip.param.market.CurveSurfaceQuoteSet csqs,
			final org.drip.param.valuation.ValuationCustomizationParams vcp,
			final double dblPriceCalib)
		{
			if (null == valParams || null == pricerParams || null == csqs ||
				!org.drip.quant.common.NumberUtil.IsValid (dblPriceCalib))
				return null;

			final org.drip.analytics.definition.CreditCurve ccOld = csqs.creditCurve (creditLabel());

			org.drip.quant.function1D.AbstractUnivariate ofCDSPriceFromFlatSpread = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				@Override public double evaluate (
					final double dblFlatSpread)
					throws java.lang.Exception
				{
					if (CALIBRATION_TYPE_NODE_PARALLEL_BUMP != _iCalibType)
						csqs.setCreditCurve (ccOld.flatCurve (dblFlatSpread,
							CALIBRATION_TYPE_FLAT_CURVE_NODES == _iCalibType, java.lang.Double.NaN));
					else
						csqs.setCreditCurve ((org.drip.analytics.definition.CreditCurve)
							ccOld.customTweakManifestMeasure ("FairPremium", new
								org.drip.param.definition.ResponseValueTweakParams
									(org.drip.param.definition.ResponseValueTweakParams.MANIFEST_MEASURE_FLAT_TWEAK,
										false, dblFlatSpread)));

					return _cds.measureValue (valParams, pricerParams, csqs, vcp, "Upfront") - dblPriceCalib;
				}
			};

			try {
				org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
					org.drip.quant.solver1D.FixedPointFinderBrent (0., ofCDSPriceFromFlatSpread,
						true).findRoot();

				if (null == rfop || !rfop.containsRoot() && !csqs.setCreditCurve (ccOld))
					return new SpreadCalibOP (rfop.getRoot(), ccOld);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	class PeriodLossMicroJack {
		double _dblAccrOnDef01 = 0.;
		org.drip.quant.calculus.WengertJacobian _wjLossPVMicroJack = null;
		org.drip.quant.calculus.WengertJacobian _wjAccrOnDef01MicroJack = null;

		PeriodLossMicroJack (
			final int iNumParameters)
			throws java.lang.Exception
		{
			_wjLossPVMicroJack = new org.drip.quant.calculus.WengertJacobian (1, iNumParameters);

			_wjAccrOnDef01MicroJack = new org.drip.quant.calculus.WengertJacobian (1, iNumParameters);
		}
	}
}
