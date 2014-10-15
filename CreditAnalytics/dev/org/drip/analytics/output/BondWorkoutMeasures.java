
package org.drip.analytics.output;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * BondWorkoutMeasures encapsulates the parsimonius yet complete set of measures generated out of a full bond
 * 	analytics run to a given work-out. It contains the following:
 * 	- Credit Risky/Credit Riskless Clean/Dirty Coupon Measures
 * 	- Credit Risky/Credit Riskless Par/Principal PV
 * 	- Loss Measures such as expected Recovery, Loss on instantaneous default, and default exposure
 * 		with/without recovery
 * 	- Unit Coupon measures such as Accrued 01, first coupon/index rate
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondWorkoutMeasures {

	/**
	 * Clean Credit Risky Bond Coupon Measures
	 */

	public BondCouponMeasures _bcmCreditRiskyClean = null;

	/**
	 * Dirty Credit Risky Bond Coupon Measures
	 */

	public BondCouponMeasures _bcmCreditRiskyDirty = null;

	/**
	 * Clean Credit Risk-less Bond Coupon Measures
	 */

	public BondCouponMeasures _bcmCreditRisklessClean = null;

	/**
	 * Dirty Credit Risk-less Bond Coupon Measures
	 */

	public BondCouponMeasures _bcmCreditRisklessDirty = null;

	/**
	 * Credit Risky Par PV
	 */

	public double _dblCreditRiskyParPV = java.lang.Double.NaN;

	/**
	 * Credit Risk-less Par PV
	 */

	public double _dblCreditRisklessParPV = java.lang.Double.NaN;

	/**
	 * Credit Risky Principal PV
	 */

	public double _dblCreditRiskyPrincipalPV = java.lang.Double.NaN;

	/**
	 * Credit Risk-less Principal PV
	 */

	public double _dblCreditRisklessPrincipalPV = java.lang.Double.NaN;

	/**
	 * Recovery PV
	 */

	public double _dblRecoveryPV = java.lang.Double.NaN;

	/**
	 * Expected Recovery
	 */

	public double _dblExpectedRecovery = java.lang.Double.NaN;

	/**
	 * Default Exposure - Same as PV on instantaneous default
	 */

	public double _dblDefaultExposure = java.lang.Double.NaN;

	/**
	 * Default Exposure without recovery - Same as PV on instantaneous default without recovery
	 */

	public double _dblDefaultExposureNoRec = java.lang.Double.NaN;

	/**
	 * Loss On Instantaneous Default
	 */

	public double _dblLossOnInstantaneousDefault = java.lang.Double.NaN;

	/**
	 * Accrued 01
	 */

	public double _dblAccrued01 = java.lang.Double.NaN;

	/**
	 * First Coupon Rate
	 */

	public double _dblFirstCouponRate = java.lang.Double.NaN;

	/**
	 * First Index Rate
	 */

	public double _dblFirstIndexRate = java.lang.Double.NaN;

	/**
	 * BondWorkoutMeasures constructor
	 * 
	 * @param bcmCreditRiskyDirty Dirty credit risky BondMeasuresCoupon
	 * @param bcmCreditRisklessDirty Dirty credit risk-less BondMeasuresCoupon
	 * @param dblCreditRiskyParPV Credit risky Par PV
	 * @param dblCreditRisklessParPV Credit risk-less par PV
	 * @param dblCreditRiskyPrincipalPV Credit Risky Principal PV
	 * @param dblCreditRisklessPrincipalPV Credit Risk-less Principal PV
	 * @param dblRecoveryPV Recovery PV
	 * @param dblExpectedRecovery Expected Recovery
	 * @param dblDefaultExposure PV on instantaneous default
	 * @param dblDefaultExposureNoRec PV on instantaneous default with zero recovery
	 * @param dblLossOnInstantaneousDefault Loss On Instantaneous Default
	 * @param dblAccrued01 Accrued01
	 * @param dblFirstCouponRate First Coupon Rate
	 * @param dblFirstIndexRate First Index Rate
	 * @param dblCashPayDF Cash Pay Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public BondWorkoutMeasures (
		final BondCouponMeasures bcmCreditRiskyDirty,
		final BondCouponMeasures bcmCreditRisklessDirty,
		final double dblCreditRiskyParPV,
		final double dblCreditRisklessParPV,
		final double dblCreditRiskyPrincipalPV,
		final double dblCreditRisklessPrincipalPV,
		final double dblRecoveryPV,
		final double dblExpectedRecovery,
		final double dblDefaultExposure,
		final double dblDefaultExposureNoRec,
		final double dblLossOnInstantaneousDefault,
		final double dblAccrued01,
		final double dblFirstCouponRate,
		final double dblFirstIndexRate,
		final double dblCashPayDF)
		throws java.lang.Exception
	{
		if (null == (_bcmCreditRisklessDirty = bcmCreditRisklessDirty) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblCreditRisklessParPV = dblCreditRisklessParPV) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblCreditRisklessPrincipalPV =
					dblCreditRisklessPrincipalPV) || !org.drip.quant.common.NumberUtil.IsValid (_dblAccrued01
						= dblAccrued01) || !org.drip.quant.common.NumberUtil.IsValid (_dblFirstCouponRate =
							dblFirstCouponRate))
			throw new java.lang.Exception ("BondWorkoutMeasures ctr: Invalid Inputs!");

		_dblRecoveryPV = dblRecoveryPV;
		_dblFirstIndexRate = dblFirstIndexRate;
		_dblDefaultExposure = dblDefaultExposure;
		_dblExpectedRecovery = dblExpectedRecovery;
		_bcmCreditRiskyDirty = bcmCreditRiskyDirty;
		_dblCreditRiskyParPV = dblCreditRiskyParPV;
		_dblDefaultExposureNoRec = dblDefaultExposureNoRec;
		_dblCreditRiskyPrincipalPV = dblCreditRiskyPrincipalPV;
		_dblLossOnInstantaneousDefault = dblLossOnInstantaneousDefault;

		if (!(_bcmCreditRisklessClean = new org.drip.analytics.output.BondCouponMeasures
			(_bcmCreditRisklessDirty._dblDV01, _bcmCreditRisklessDirty._dblIndexCouponPV,
				_bcmCreditRisklessDirty._dblCouponPV, _bcmCreditRisklessDirty._dblPV)).adjustForSettlement
					(dblCashPayDF))
			throw new java.lang.Exception
				("BondWorkoutMeasures ctr: Cannot successfully set up BCM CreditRisklessClean");

		if (!_bcmCreditRisklessClean.adjustForAccrual (_dblAccrued01, _dblFirstCouponRate, dblFirstIndexRate,
			false))
			throw new java.lang.Exception
				("BondWorkoutMeasures ctr: Cannot successfully set up BCM CreditRisklessClean");

		if (null != _bcmCreditRiskyDirty && ((!(_bcmCreditRiskyClean = new BondCouponMeasures
			(_bcmCreditRiskyDirty._dblDV01, _bcmCreditRiskyDirty._dblIndexCouponPV,
				_bcmCreditRiskyDirty._dblCouponPV, _bcmCreditRiskyDirty._dblPV)).adjustForSettlement
					(dblCashPayDF)) || !_bcmCreditRiskyClean.adjustForAccrual (_dblAccrued01,
						_dblFirstCouponRate, _dblFirstCouponRate, false)))
			throw new java.lang.Exception
				("BondWorkoutMeasures ctr: Cannot successfully set up BCM CreditRiskyClean");
	}

	/**
	 * Return the state as a measure map
	 * 
	 * @param strPrefix Measure name prefix
	 * 
	 * @return Map of the measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> toMap (
		final java.lang.String strPrefix)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapMeasures.put (strPrefix + "Accrued", _dblAccrued01 * _dblFirstCouponRate);

		mapMeasures.put (strPrefix + "Accrued01", _dblAccrued01);

		mapMeasures.put (strPrefix + "CleanCouponPV", _bcmCreditRisklessClean._dblCouponPV);

		mapMeasures.put (strPrefix + "CleanDV01", _bcmCreditRisklessClean._dblDV01);

		mapMeasures.put (strPrefix + "CleanIndexCouponPV", _bcmCreditRisklessClean._dblIndexCouponPV);

		mapMeasures.put (strPrefix + "CleanPrice", _bcmCreditRisklessClean._dblPV);

		mapMeasures.put (strPrefix + "CleanPV", _bcmCreditRisklessClean._dblPV);

		mapMeasures.put (strPrefix + "CreditRisklessParPV", _dblCreditRisklessParPV);

		mapMeasures.put (strPrefix + "CreditRisklessPrincipalPV", _dblCreditRisklessPrincipalPV);

		mapMeasures.put (strPrefix + "CreditRiskyParPV", _dblCreditRiskyParPV);

		mapMeasures.put (strPrefix + "CreditRiskyPrincipalPV", _dblCreditRiskyPrincipalPV);

		mapMeasures.put (strPrefix + "DefaultExposure", _dblDefaultExposure);

		mapMeasures.put (strPrefix + "DefaultExposureNoRec", _dblDefaultExposureNoRec);

		mapMeasures.put (strPrefix + "DirtyCouponPV", _bcmCreditRisklessDirty._dblCouponPV);

		mapMeasures.put (strPrefix + "DirtyDV01", _bcmCreditRisklessDirty._dblDV01);

		mapMeasures.put (strPrefix + "DirtyIndexCouponPV", _bcmCreditRisklessDirty._dblIndexCouponPV);

		mapMeasures.put (strPrefix + "DirtyPrice", _bcmCreditRisklessDirty._dblPV);

		mapMeasures.put (strPrefix + "DirtyPV", _bcmCreditRisklessDirty._dblPV);

		mapMeasures.put (strPrefix + "DV01", _bcmCreditRisklessClean._dblDV01);

		mapMeasures.put (strPrefix + "ExpectedRecovery", _dblExpectedRecovery);

		mapMeasures.put (strPrefix + "FirstCouponRate", _dblFirstCouponRate);

		mapMeasures.put (strPrefix + "FirstIndexRate", _dblFirstIndexRate);

		mapMeasures.put (strPrefix + "LossOnInstantaneousDefault", _dblLossOnInstantaneousDefault);

		mapMeasures.put (strPrefix + "ParPV", _dblCreditRisklessParPV);

		mapMeasures.put (strPrefix + "PrincipalPV", _dblCreditRisklessPrincipalPV);

		mapMeasures.put (strPrefix + "PV", _bcmCreditRisklessClean._dblPV);

		mapMeasures.put (strPrefix + "RecoveryPV", _dblRecoveryPV);

		org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, _bcmCreditRisklessDirty.toMap (strPrefix +
			"RisklessDirty"));

		org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, _bcmCreditRisklessClean.toMap (strPrefix +
			"RisklessClean"));

		if (null != _bcmCreditRiskyDirty) {
			mapMeasures.put (strPrefix + "CleanCouponPV", _bcmCreditRiskyClean._dblCouponPV);

			mapMeasures.put (strPrefix + "CleanDV01", _bcmCreditRiskyClean._dblDV01);

			mapMeasures.put (strPrefix + "CleanIndexCouponPV", _bcmCreditRiskyClean._dblIndexCouponPV);

			mapMeasures.put (strPrefix + "CleanPrice", _bcmCreditRiskyClean._dblPV);

			mapMeasures.put (strPrefix + "CleanPV", _bcmCreditRiskyClean._dblPV);

			mapMeasures.put (strPrefix + "DirtyCouponPV", _bcmCreditRiskyDirty._dblCouponPV);

			mapMeasures.put (strPrefix + "DirtyDV01", _bcmCreditRiskyDirty._dblDV01);

			mapMeasures.put (strPrefix + "DirtyIndexCouponPV", _bcmCreditRiskyDirty._dblIndexCouponPV);

			mapMeasures.put (strPrefix + "DirtyPrice", _bcmCreditRiskyDirty._dblPV);

			mapMeasures.put (strPrefix + "DirtyPV", _bcmCreditRiskyDirty._dblPV);

			mapMeasures.put (strPrefix + "DV01", _bcmCreditRiskyClean._dblDV01);

			mapMeasures.put (strPrefix + "ParPV", _dblCreditRiskyParPV);

			mapMeasures.put (strPrefix + "PrincipalPV", _dblCreditRiskyPrincipalPV);

			mapMeasures.put (strPrefix + "PV", _bcmCreditRiskyClean._dblPV);

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, _bcmCreditRiskyDirty.toMap (strPrefix +
				"RiskyDirty"));

			org.drip.quant.common.CollectionUtil.MergeWithMain (mapMeasures, _bcmCreditRiskyClean.toMap (strPrefix +
				"RiskyClean"));
		}

		return mapMeasures;
	}
}
