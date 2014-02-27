
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
	private java.lang.String _strIR = "";
	private java.lang.String _strCode = "";
	private java.lang.String _strName = "";
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private double _dblCoupon = java.lang.Double.NaN;
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.CreditSetting _crValParams = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;
	private java.util.List<org.drip.analytics.period.CashflowPeriod> _lsCouponPeriod = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		return null;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calcMeasureSet (
		final java.lang.String strMeasureSetPrefix,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == pricerParams || null == mktParams) return null;

		org.drip.analytics.definition.CreditCurve cc = mktParams.getCreditCurve();

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == cc || null == dc) return null;

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

		try {
			for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
				if (period.getPayDate() < valParams.valueDate()) continue;

				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (period.getStartDate() < valParams.valueDate()) {
						dblAccrualDays = valParams.valueDate() - period.getAccrualStartDate();

						dblAccrued01 = period.getAccrualDCF (valParams.valueDate()) * 0.01 * getNotional
							(period.getAccrualStartDate(), valParams.valueDate());
					}
				}

				double dblSurvProb = pricerParams._bSurvToPayDate ? cc.getSurvival (period.getPayDate()) :
					cc.getSurvival (period.getEndDate());

				dblDirtyDV01 += 0.01 * period.getCouponDCF() * dc.df (period.getPayDate()) * dblSurvProb *
					getNotional (period.getAccrualStartDate(), period.getEndDate());

				java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> sLPSub =
					org.drip.analytics.support.AnalyticsHelper.GenerateLossPeriods (this, valParams,
						pricerParams, period, period.getEndDate(), mktParams);

				if (null == sLPSub || 0 == sLPSub.size()) continue;

				for (org.drip.analytics.period.LossPeriodCurveFactors lp : sLPSub) {
					if (null == lp) continue;

					double dblSubPeriodEnd = lp.getEndDate();

					double dblSubPeriodStart = lp.getStartDate();

					double dblSubPeriodDF = dc.effectiveDF (dblSubPeriodStart + _crValParams._iDefPayLag,
						dblSubPeriodEnd + _crValParams._iDefPayLag);

					double dblSubPeriodNotional = getNotional (dblSubPeriodStart, dblSubPeriodEnd);

					double dblSubPeriodSurvival = cc.getSurvival (dblSubPeriodStart) - cc.getSurvival
						(dblSubPeriodEnd);

					double dblRec = _crValParams._bUseCurveRec ? cc.getEffectiveRecovery (dblSubPeriodStart,
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
			dblCashPayDF = dc.df (null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate()));
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
			mapResult.put (strMeasureSetPrefix + "CleanPrice", 100. * (1. + (dblCleanPV / _dblNotional /
				getNotional (valParams.valueDate()))));

			mapResult.put (strMeasureSetPrefix + "DirtyPrice", 100. * (1. + (dblDirtyPV / _dblNotional /
				getNotional (valParams.valueDate()))));

			mapResult.put (strMeasureSetPrefix + "LossOnInstantaneousDefault", _dblNotional * (1. -
				cc.getRecovery (valParams.valueDate())));

			mapResult.put (strMeasureSetPrefix + "Price", 100. * (1. + (dblCleanPV / _dblNotional /
				getNotional (valParams.valueDate()))));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put (strMeasureSetPrefix + "CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	private org.drip.quant.calculus.WengertJacobian calcPeriodOnDefaultPVDFMicroJack (
		final double dblFairPremium,
		final org.drip.analytics.period.Period period,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams)
	{
		java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> sLPSub =
			org.drip.analytics.support.AnalyticsHelper.GenerateLossPeriods (this, valParams,
				pricerParams, period, period.getEndDate(), mktParams);

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == sLPSub || 0 == sLPSub.size() || null == dc) return null;

		int iNumParameters = 0;
		org.drip.quant.calculus.WengertJacobian wjPeriodOnDefaultPVDF = null;

		for (org.drip.analytics.period.LossPeriodCurveFactors lpcf : sLPSub) {
			org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dc.jackDDFDManifestMeasure (0.5 *
				(lpcf.getStartDate() + lpcf.getEndDate()) + _crValParams._iDefPayLag, "Rate");

			try {
				if (null == wjPeriodOnDefaultPVDF)
					wjPeriodOnDefaultPVDF = new org.drip.quant.calculus.WengertJacobian (1, iNumParameters =
						wjPeriodPayDFDF.numParameters());

				double dblPeriodIncrementalCashFlow = getNotional (lpcf.getStartDate(), lpcf.getEndDate()) *
					(dblFairPremium * lpcf.accrualDCF() - 1. + lpcf.effectiveRecovery()) *
						(lpcf.startSurvival() - lpcf.endSurvival());

				for (int k = 0; k < iNumParameters; ++k) {
					if (!wjPeriodOnDefaultPVDF.accumulatePartialFirstDerivative (0, k,
						wjPeriodPayDFDF.getFirstDerivative (0, k) * dblPeriodIncrementalCashFlow))
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
		final org.drip.analytics.period.Period period,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams)
	{
		java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> sLPSub =
			org.drip.analytics.support.AnalyticsHelper.GenerateLossPeriods (this, valParams,
				pricerParams, period, period.getEndDate(), mktParams);

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == sLPSub || 0 == sLPSub.size() || null == dc) return null;

		PeriodLossMicroJack plmj = null;

		for (org.drip.analytics.period.LossPeriodCurveFactors lpcf : sLPSub) {
			double dblPeriodNotional = java.lang.Double.NaN;
			double dblPeriodIncrementalLoss = java.lang.Double.NaN;
			double dblPeriodIncrementalAccrual = java.lang.Double.NaN;
			double dblPeriodIncrementalSurvival = java.lang.Double.NaN;

			double dblPeriodEffectiveDate = 0.5 * (lpcf.getStartDate() + lpcf.getEndDate());

			org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dc.jackDDFDManifestMeasure
				(dblPeriodEffectiveDate + _crValParams._iDefPayLag, "Rate");

			try {
				dblPeriodNotional = getNotional (lpcf.getStartDate(), lpcf.getEndDate());

				dblPeriodIncrementalSurvival = lpcf.startSurvival() - lpcf.endSurvival();

				dblPeriodIncrementalLoss = dblPeriodNotional * (1. - lpcf.effectiveRecovery()) *
					dblPeriodIncrementalSurvival;

				dblPeriodIncrementalAccrual = dblPeriodNotional * lpcf.accrualDCF() *
					dblPeriodIncrementalSurvival;

				if (null == plmj) plmj = new PeriodLossMicroJack (wjPeriodPayDFDF.numParameters());

				plmj._dblAccrOnDef01 += dblPeriodIncrementalAccrual * dc.df (dblPeriodEffectiveDate +
					_crValParams._iDefPayLag);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			for (int k = 0; k < wjPeriodPayDFDF.numParameters(); ++k) {
				if (!plmj._wjLossPVMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodIncrementalLoss
					* wjPeriodPayDFDF.getFirstDerivative (0, k)))
					return null;

				if (!plmj._wjAccrOnDef01MicroJack.accumulatePartialFirstDerivative (0, k,
					dblPeriodIncrementalAccrual * wjPeriodPayDFDF.getFirstDerivative (0, k)))
					return null;
			}
		}

		return plmj;
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
	 * @param strIR IR Curve Name
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
		final java.lang.String strIR,
		final org.drip.product.params.CreditSetting crValParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == strIR || strIR.isEmpty() || null == crValParams ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEffective) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblMaturity) ||
					!org.drip.quant.common.NumberUtil.IsValid (dblCoupon))
			throw new java.lang.Exception ("CDSComponent constructor: Invalid params!");

		_strIR = strIR;
		_dblCoupon = dblCoupon;
		_dblNotional = dblNotional;
		_crValParams = crValParams;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		_lsCouponPeriod = org.drip.analytics.period.CashflowPeriod.GeneratePeriodsBackward (
			_dblEffective = dblEffective, // Effective
			_dblMaturity = dblMaturity, // Maturity
			dapEffective, // Effective DAP
			dapMaturity, // Maturity DAP
			dapPeriodStart, // Period Start DAP
			dapPeriodEnd, // Period End DAP
			dapAccrualStart, // Accrual Start DAP
			dapAccrualEnd, // Accrual End DAP
			dapPay, // Pay DAP
			dapReset, // Reset DAP
			iFreq, // Coupon Freq
			strCouponDC, // Coupon Day Count
			_bApplyCpnEOMAdj,
			strAccrualDC, // Accrual Day Count
			_bApplyAccEOMAdj,
			bConvCDS, // Full First Coupon Period?
			false, // Merge the first 2 Periods - create a long stub?
			false,
			strCalendar);
	}

	/**
	 * CDSComponent de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CDSComponent cannot be properly de-serialized
	 */

	public CDSComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CDSComponent de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Empty state");

		java.lang.String strSerializedCreditDefaultSwap = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedCreditDefaultSwap || strSerializedCreditDefaultSwap.isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedCreditDefaultSwap,
			getFieldDelimiter());

		if (null == astrField || 13 > astrField.length)
			throw new java.lang.Exception ("CDSComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate IR curve name");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_strIR = astrField[2];
		else
			_strIR = "";

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCode = astrField[3];
		else
			_strCode = "";

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate Apply Acc EOM Adj");

		_bApplyAccEOMAdj = new java.lang.Boolean (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate Apply Cpn EOM Adj");

		_bApplyCpnEOMAdj = new java.lang.Boolean (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate coupon");

		_dblCoupon = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate maturity date");

		_dblMaturity = new java.lang.Double (astrField[7]);

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate effective date");

		_dblEffective = new java.lang.Double (astrField[8]);

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate notional schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[9].getBytes());

		if (null == astrField[10] || astrField[10].isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate credit val params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[10]))
			_crValParams = null;
		else
			_crValParams = new org.drip.product.params.CreditSetting
				(astrField[10].getBytes());

		if (null == astrField[11] || astrField[11].isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[11]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[11].getBytes());

		if (null == astrField[12] || astrField[12].isEmpty())
			throw new java.lang.Exception ("CDSComponent de-serializer: Cannot locate the periods");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[12]))
			_lsCouponPeriod = null;
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[12],
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					if (null == _lsCouponPeriod)
						_lsCouponPeriod = new java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

					_lsCouponPeriod.add (new org.drip.analytics.period.CashflowPeriod
						(astrRecord[i].getBytes()));
				}
			}
		}
	}

	@Override public java.lang.String getPrimaryCode()
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

	@Override public java.lang.String getComponentName()
	{
		if (null != _strName && !_strName.isEmpty()) return _strName;

		return "CDS=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public java.lang.String getTreasuryCurveName()
	{
		return "";
	}

	@Override public java.lang.String getEDSFCurveName()
	{
		return "";
	}

	@Override public double getInitialNotional()
	{
		return _dblNotional;
	}

	@Override public double getNotional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("CDSComponent::getNotional => Bad date");

		return _notlSchedule.getFactor (dblDate);
	}

	@Override public double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("CDSComponent::getNotional => Bad date");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	@Override public double getRecovery (
		final double dblDate,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || null == cc)
			throw new java.lang.Exception ("CDSComponent::getRecovery => Bad inputs");

		return _crValParams._bUseCurveRec ? cc.getRecovery (dblDate) : _crValParams._dblRecovery;
	}

	@Override public double getRecovery (
		final double dblDateStart,
		final double dblDateEnd,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDateStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDateEnd) || null == cc)
			throw new java.lang.Exception ("CDSComponent::getRecovery: Bad inputs");

		return _crValParams._bUseCurveRec ? cc.getEffectiveRecovery (dblDateStart, dblDateEnd) :
			_crValParams._dblRecovery;
	}

	@Override public org.drip.product.params.CreditSetting getCRValParams()
	{
		return _crValParams;
	}

	@Override public double getCoupon (
		final double dblValue,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		return _dblCoupon;
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

	@Override public boolean setCurves (
		final java.lang.String strIR,
		final java.lang.String strIRTSY,
		final java.lang.String strCC)
	{
		if (null == strIR || strIR.isEmpty() || null == strCC || strCC.isEmpty()) return false;

		_strIR = strIR;
		_crValParams._strCC = strCC;
		return true;
	}

	@Override public java.lang.String getIRCurveName()
	{
		return _strIR;
	}

	@Override public java.lang.String getForwardCurveName()
	{
		return "";
	}

	@Override public java.lang.String getCreditCurveName()
	{
		return _crValParams._strCC;
	}

	@Override public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).getEndDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod()
	{
		return _lsCouponPeriod;
	}

	@Override public org.drip.param.valuation.CashSettleParams getCashSettleParams()
	{
		return _settleParams;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriodCurveFactors> getCouponFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams)
	{
		if (null == valParams || null == pricerParams || null == mktParams) return null;

		org.drip.analytics.definition.CreditCurve cc = mktParams.getCreditCurve();

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == cc || null == dc) return null;

		java.util.List<org.drip.analytics.period.CashflowPeriodCurveFactors> lsCP = new
			java.util.ArrayList<org.drip.analytics.period.CashflowPeriodCurveFactors>();

		double dblDFStart = java.lang.Double.NaN;
		double dblSurvProbStart = java.lang.Double.NaN;

		for (org.drip.analytics.period.CashflowPeriod fp : _lsCouponPeriod) {
			if (null == fp) continue;

			org.drip.analytics.period.CashflowPeriodCurveFactors cp = null;

			try {
				double dblSurvProbEnd = pricerParams._bSurvToPayDate ? cc.getSurvival (fp.getPayDate()) :
					cc.getSurvival (fp.getEndDate());

				if (!org.drip.quant.common.NumberUtil.IsValid (dblDFStart))
					dblDFStart = dc.df (fp.getStartDate());

				if (!org.drip.quant.common.NumberUtil.IsValid (dblSurvProbStart))
					dblSurvProbStart = cc.getSurvival (fp.getStartDate());

				double dblDFEnd = dc.df (fp.getPayDate());

				cp = new org.drip.analytics.period.CashflowPeriodCurveFactors (fp.getStartDate(),
					fp.getEndDate(), fp.getAccrualStartDate(), fp.getAccrualEndDate(), fp.getPayDate(),
						fp.getCouponDCF(), getCoupon (valParams.valueDate(), mktParams), getNotional
							(fp.getStartDate()), getNotional (fp.getEndDate()), dblDFStart, dblDFEnd,
								dblSurvProbStart, dblSurvProbEnd, java.lang.Double.NaN,
									java.lang.Double.NaN);

				dblDFStart = dblDFEnd;
				dblSurvProbStart = dblSurvProbEnd;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			if (null != cp) lsCP.add (cp);
		}

		return lsCP;
	}

	@Override public java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> getLossFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams)
	{
		if (null == valParams || null == pricerParams) return null;

		java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> sLP = new
			java.util.ArrayList<org.drip.analytics.period.LossPeriodCurveFactors>();

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			if (null == period || period.getEndDate() < valParams.valueDate()) continue;

			java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> sLPSub =
				org.drip.analytics.support.AnalyticsHelper.GenerateLossPeriods (this, valParams,
					pricerParams, period, period.getEndDate(), mktParams);

			if (null != sLPSub) sLP.addAll (sLPSub);
		}

		return sLP;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFairMeasures = calcMeasureSet
			("", valParams, pricerParams, mktParams, quotingParams);

		if (null == mapFairMeasures) return null;

		org.drip.quant.common.CollectionUtil.MergeWithMain (mapFairMeasures, org.drip.quant.common.CollectionUtil.PrefixKeys
			(mapFairMeasures, "Fair"));

		org.drip.param.definition.ComponentQuote cq = mktParams.getComponentQuote();

		if ((null != pricerParams && null != pricerParams._calibParams) || null == mapFairMeasures || null ==
			cq)
			return mapFairMeasures;

		double dblCreditBasis = java.lang.Double.NaN;
		double dblMarketMeasure = java.lang.Double.NaN;
		org.drip.analytics.definition.CreditCurve ccMarket = null;

		if (null != cq.getQuote ("Price"))
			mapFairMeasures.put ("MarketInputType=Price", dblMarketMeasure = cq.getQuote ("Price").getQuote
				("mid"));
		else if (null != cq.getQuote ("CleanPrice"))
			mapFairMeasures.put ("MarketInputType=CleanPrice", dblMarketMeasure = cq.getQuote
				("CleanPrice").getQuote ("mid"));
		else if (null != cq.getQuote ("Upfront"))
			mapFairMeasures.put ("MarketInputType=Upfront", dblMarketMeasure = cq.getQuote
				("Upfront").getQuote ("mid"));
		else if (null != cq.getQuote ("FairPremium"))
			mapFairMeasures.put ("MarketInputType=FairPremium", dblMarketMeasure = cq.getQuote
				("FairPremium").getQuote ("mid"));
		else if (null != cq.getQuote ("PV"))
			mapFairMeasures.put ("MarketInputType=PV", dblMarketMeasure = cq.getQuote ("PV").getQuote
				("mid"));
		else if (null != cq.getQuote ("CleanPV"))
			mapFairMeasures.put ("MarketInputType=CleanPV", dblMarketMeasure = cq.getQuote
				("CleanPV").getQuote ("mid"));

		try {
			SpreadCalibOP scop = new SpreadCalibrator (this,
				SpreadCalibrator.CALIBRATION_TYPE_NODE_PARALLEL_BUMP).calibrateHazardFromPrice (valParams,
					new org.drip.param.pricer.PricerParams (7,
						org.drip.param.definition.CalibrationParams.MakeStdCalibParams(), false,
							org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP), mktParams,
								quotingParams, dblMarketMeasure);

			if (null != scop) {
				ccMarket = scop._ccCalib;
				dblCreditBasis = scop._dblCalibResult;
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (org.drip.quant.common.NumberUtil.IsValid (dblCreditBasis)) {
			mapFairMeasures.put ("MarketCreditBasis", dblCreditBasis);

			try {
				ccMarket = (org.drip.analytics.definition.CreditCurve)
					mktParams.getCreditCurve().customTweakManifestMeasure (new
						org.drip.param.definition.ResponseValueTweakParams
							(org.drip.param.definition.ResponseValueTweakParams.MANIFEST_MEASURE_FLAT_TWEAK, false,
								dblCreditBasis));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = mapFairMeasures;

		if (null != ccMarket) {
			org.drip.param.definition.ComponentMarketParams cmpMarket =
				org.drip.param.creator.ComponentMarketParamsBuilder.CreateComponentMarketParams
				(mktParams.getDiscountCurve(), mktParams.getTSYDiscountCurve(),
					mktParams.getEDSFDiscountCurve(), ccMarket, mktParams.getComponentQuote(),
						mktParams.getTSYBenchmarkQuotes(), mktParams.getFixings());

			if (null != cmpMarket) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMarketMeasures =
					calcMeasureSet ("", valParams, pricerParams, cmpMarket, quotingParams);

				if (null != mapMarketMeasures) {
					org.drip.quant.common.CollectionUtil.MergeWithMain (mapMarketMeasures,
						org.drip.quant.common.CollectionUtil.PrefixKeys (mapMarketMeasures, "Market"));

					org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, mapMarketMeasures);
				}
			}
		}

		return mapMeasures;
	}

	@Override public java.util.Set<java.lang.String> getMeasureNames()
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
			final org.drip.param.definition.ComponentMarketParams mktParams,
			final org.drip.param.valuation.QuotingParams quotingParams,
			final double dblFixCoupon,
			final double dblQuotedSpread)
	{
		if (null == valParams || !org.drip.quant.common.NumberUtil.IsValid (dblFixCoupon) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblQuotedSpread))
			return null;

		org.drip.product.definition.CalibratableComponent[] aComp = new
			org.drip.product.definition.CalibratableComponent[] {this};
		org.drip.analytics.definition.CreditCurve cc = null;
		double[] adblRestorableCDSCoupon = new double[1];
		adblRestorableCDSCoupon[0] = _dblCoupon;
		_dblCoupon = dblFixCoupon;

		if (null != mktParams) {
			org.drip.product.definition.CalibratableComponent[] aMktComp = null;

			if (null != (cc = mktParams.getCreditCurve()) && null != (aMktComp = cc.calibComp())) {
				int iNumComp = aMktComp.length;

				if (0 != iNumComp) {
					aComp = aMktComp;
					adblRestorableCDSCoupon = new double[iNumComp];

					for (int i = 0; i < iNumComp; ++i) {
						if (null != aComp[i] && aComp[i] instanceof
							org.drip.product.definition.CreditDefaultSwap) {
							try {
								adblRestorableCDSCoupon[i] = aComp[i].getCoupon (valParams.valueDate(),
									mktParams);

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

		try {
			if (null == (ccQS = org.drip.param.creator.CreditScenarioCurveBuilder.CreateCreditCurve
				(getComponentName(), new org.drip.analytics.date.JulianDate (valParams.valueDate()), aComp,
					mktParams.getDiscountCurve(), adblQS, astrCalibMeasure, null != cc ? cc.getRecovery
						(valParams.valueDate()) : 0.4, false)))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapPV = value (valParams,
			pricerParams, org.drip.param.creator.ComponentMarketParamsBuilder.MakeCreditCMP
				(mktParams.getDiscountCurve(), ccQS), quotingParams);

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
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == mktParams) return null;

		org.drip.analytics.definition.CreditCurve cc = mktParams.getCreditCurve();

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == cc || null == dc) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value (valParams,
			pricerParams, mktParams, quotingParams);

		if (null == mapMeasures) return null;

		double dblPV = mapMeasures.get ("PV");

		double dblFairPremium = mapMeasures.get ("FairPremium");

		try {
			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = null;

			for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
				double dblPeriodPayDate = p.getPayDate();

				if (dblPeriodPayDate < valParams.valueDate()) continue;

				org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dc.jackDDFDManifestMeasure
					(dblPeriodPayDate, "Rate");

				org.drip.quant.calculus.WengertJacobian wjPeriodOnDefaultPVMicroJack =
					calcPeriodOnDefaultPVDFMicroJack (dblFairPremium, p, valParams, pricerParams, mktParams);

				if (null == wjPeriodPayDFDF | null == wjPeriodOnDefaultPVMicroJack) continue;

				if (null == wjPVDFMicroJack)
					wjPVDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
						wjPeriodPayDFDF.numParameters());

				double dblPeriodCashFlow = dblFairPremium * getNotional (p.getStartDate(), p.getEndDate()) *
					p.getCouponDCF() * cc.getSurvival (dblPeriodPayDate);

				for (int k = 0; k < wjPeriodPayDFDF.numParameters(); ++k) {
					if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodCashFlow *
						wjPeriodPayDFDF.getFirstDerivative (0, k) +
							wjPeriodOnDefaultPVMicroJack.getFirstDerivative (0, k)))
						return null;
				}
			}

			return adjustPVDFMicroJackForCashSettle (valParams.cashPayDate(), dblPV, dc, wjPVDFMicroJack) ?
				wjPVDFMicroJack : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcQuoteDFMicroJack (
		final java.lang.String strQuote,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strQuote || null ==
			mktParams)
			return null;

		org.drip.analytics.definition.CreditCurve cc = mktParams.getCreditCurve();

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == cc || null == dc) return null;

		if ("Rate".equalsIgnoreCase (strQuote) || "FairPremium".equalsIgnoreCase (strQuote) ||
			"ParSpread".equalsIgnoreCase (strQuote)) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value (valParams,
				pricerParams, mktParams, quotingParams);

			if (null == mapMeasures) return null;

			double dblFairPremium = mapMeasures.get ("FairPremium");

			try {
				double dblDV01 = 0.;
				org.drip.quant.calculus.WengertJacobian wjFairPremiumDFMicroJack = null;

				for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
					double dblPeriodPayDate = p.getPayDate();

					if (dblPeriodPayDate < valParams.valueDate()) continue;

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dc.jackDDFDManifestMeasure
						(p.getEndDate(), "Rate");

					PeriodLossMicroJack plmj = calcPeriodLossMicroJack (p, valParams, pricerParams,
						mktParams);

					if (null == wjPeriodPayDFDF | null == plmj) continue;

					if (null == wjFairPremiumDFMicroJack)
						wjFairPremiumDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodPayDFDF.numParameters());

					double dblPeriodCoupon01 = getNotional (p.getStartDate(), p.getEndDate()) *
						p.getCouponDCF() * cc.getSurvival (p.getEndDate());

					dblDV01 += dblPeriodCoupon01 * dc.df (p.getPayDate()) + plmj._dblAccrOnDef01;

					for (int k = 0; k < wjPeriodPayDFDF.numParameters(); ++k) {
						double dblPeriodNetLossJack = plmj._wjLossPVMicroJack.getFirstDerivative (0, k) -
							dblFairPremium * (plmj._wjAccrOnDef01MicroJack.getFirstDerivative (0, k) +
								dblPeriodCoupon01 * wjPeriodPayDFDF.getFirstDerivative (0, k));

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

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		return null;
	}

	/**
	 * Calibrate the CDS's flat spread from the calculated up-front points
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams ComponentMarketParams
	 * 
	 * @return Calibrated flat spread
	 * 
	 * @throws java.lang.Exception Thrown if cannot calibrate
	 */

	public double calibFlatSpread (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
		throws java.lang.Exception
	{
		SpreadCalibOP scop = new SpreadCalibrator (this,
			SpreadCalibrator.CALIBRATION_TYPE_FLAT_CURVE_NODES).calibrateHazardFromPrice (valParams,
				pricerParams, mktParams, quotingParams, calcMeasureValue (valParams, pricerParams, mktParams,
					quotingParams, "Upfront"));

		if (null == scop)
			throw new java.lang.Exception ("CDSComponent::calibFlatSpread => Cannot calibrate flat spread!");

		return scop._dblCalibResult;
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "!";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "&";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_dblNotional + getFieldDelimiter());

		if (null == _strIR || _strIR.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strIR + getFieldDelimiter());

		if (null == _strCode || _strCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strCode + getFieldDelimiter());

		sb.append (_bApplyAccEOMAdj + getFieldDelimiter());

		sb.append (_bApplyCpnEOMAdj + getFieldDelimiter());

		sb.append (_dblCoupon + getFieldDelimiter());

		sb.append (_dblMaturity + getFieldDelimiter());

		sb.append (_dblEffective + getFieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + getFieldDelimiter());

		if (null == _crValParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_crValParams.serialize()) + getFieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + getFieldDelimiter());

		if (null == _lsCouponPeriod)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbPeriods = new java.lang.StringBuffer();

			for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
				if (null == p) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbPeriods.append (getCollectionRecordDelimiter());

				sbPeriods.append (new java.lang.String (p.serialize()));
			}

			if (sbPeriods.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbPeriods.toString());
		}

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new CDSComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
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
		 * @param mktParams ComponentMarketParams
		 * @param dblPriceCalib Market price to be calibrated
		 * @param quotingParams Quoting Parameters
		 * 
		 * @return Calibrated hazard
		 * 
		 * @throws java.lang.Exception Thrown if calibration failed
		 */

		public SpreadCalibOP calibrateHazardFromPrice (
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.param.pricer.PricerParams pricerParams,
			final org.drip.param.definition.ComponentMarketParams mktParams,
			final org.drip.param.valuation.QuotingParams quotingParams,
			final double dblPriceCalib)
		{
			if (null == valParams || null == pricerParams || null == mktParams  || null ==
				mktParams.getCreditCurve() || !org.drip.quant.common.NumberUtil.IsValid (dblPriceCalib))
				return null;

			final org.drip.analytics.definition.CreditCurve ccOld = mktParams.getCreditCurve();

			org.drip.quant.function1D.AbstractUnivariate ofCDSPriceFromFlatSpread = new
				org.drip.quant.function1D.AbstractUnivariate (null) {
				public double evaluate (
					final double dblFlatSpread)
					throws java.lang.Exception
				{
					if (CALIBRATION_TYPE_NODE_PARALLEL_BUMP != _iCalibType)
						mktParams.setCreditCurve (ccOld.createFlatCurve (dblFlatSpread,
							CALIBRATION_TYPE_FLAT_CURVE_NODES == _iCalibType, java.lang.Double.NaN));
					else
						mktParams.setCreditCurve ((org.drip.analytics.definition.CreditCurve)
							ccOld.customTweakManifestMeasure (new
								org.drip.param.definition.ResponseValueTweakParams
									(org.drip.param.definition.ResponseValueTweakParams.MANIFEST_MEASURE_FLAT_TWEAK,
										false, dblFlatSpread)));

					return _cds.calcMeasureValue (valParams, pricerParams, mktParams, quotingParams,
						"Upfront") - dblPriceCalib;
				}

				@Override public double integrate (
					final double dblBegin,
					final double dblEnd)
					throws java.lang.Exception
				{
					return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
				}
			};

			try {
				org.drip.quant.solver1D.FixedPointFinderOutput rfop = new
					org.drip.quant.solver1D.FixedPointFinderBrent (0., ofCDSPriceFromFlatSpread,
						true).findRoot();

				if (null == rfop || !rfop.containsRoot() && !mktParams.setCreditCurve (ccOld))
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
