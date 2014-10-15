
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
 * BondRVMeasures encapsulates the comprehensive set of RV measures calculated for the bond to the
 *  appropriate exercise:
 * 	- Workout Information
 * 	- Price, Yield, and Yield01
 * 	- Spread Measures: Asset Swap/Credit/G/I/OAS/PECS/TSY/Z
 * 	- Basis Measures: Bond Basis, Credit Basis, Yield Basis
 * 	- Duration Measures: Macaulay/Modified Duration, Convexity
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondRVMeasures {

	/**
	 * Price
	 */

	public double _dblPrice = java.lang.Double.NaN;

	/**
	 * Bond Basis
	 */

	public double _dblBondBasis = java.lang.Double.NaN;

	/**
	 * Z Spread
	 */

	public double _dblZSpread = java.lang.Double.NaN;

	/**
	 * G Spread
	 */

	public double _dblGSpread = java.lang.Double.NaN;

	/**
	 * I Spread
	 */

	public double _dblISpread = java.lang.Double.NaN;

	/**
	 * Option Adjusted Spread
	 */

	public double _dblOASpread = java.lang.Double.NaN;

	/**
	 * Treasury Spread
	 */

	public double _dblTSYSpread = java.lang.Double.NaN;

	/**
	 * Discount Margin
	 */

	public double _dblDiscountMargin = java.lang.Double.NaN;

	/**
	 * Asset swap spread
	 */

	public double _dblAssetSwapSpread = java.lang.Double.NaN;

	/**
	 * Credit Basis
	 */

	public double _dblCreditBasis = java.lang.Double.NaN;

	/**
	 * PECS
	 */

	public double _dblPECS = java.lang.Double.NaN;

	/**
	 * Yield 01
	 */

	public double _dblYield01 = java.lang.Double.NaN;

	/**
	 * Macaulay Duration
	 */

	public double _dblMacaulayDuration = java.lang.Double.NaN;

	/**
	 * Modified Duration
	 */

	public double _dblModifiedDuration = java.lang.Double.NaN;

	/**
	 * Convexity
	 */

	public double _dblConvexity = java.lang.Double.NaN;

	/**
	 * Work-out info
	 */

	public org.drip.param.valuation.WorkoutInfo _wi = null;

	/**
	 * BondRVMeasures ctr
	 * 
	 * @param dblPrice BondRV Clean Price
	 * @param dblBondBasis BondRV Bond Basis
	 * @param dblZSpread BondRV Z Spread
	 * @param dblGSpread BondRV G Spread
	 * @param dblISpread BondRV I Spread
	 * @param dblOASpread BondRV OAS
	 * @param dblTSYSpread BondRV TSY Spread
	 * @param dblDiscountMargin BondRV Asset Swap Spread
	 * @param dblAssetSwapSpread BondRV Asset Swap Spread
	 * @param dblCreditBasis BondRV Credit Basis
	 * @param dblPECS BondRV PECS
	 * @param dblYield01 BondRV Yield01
	 * @param dblModifiedDuration BondRV Modified Duration
	 * @param dblMacaulayDuration BondRV Macaulay Duration
	 * @param dblConvexity BondRV Convexity
	 * @param wi BondRV work-out info
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public BondRVMeasures (
		final double dblPrice,
		final double dblBondBasis,
		final double dblZSpread,
		final double dblGSpread,
		final double dblISpread,
		final double dblOASpread,
		final double dblTSYSpread,
		final double dblDiscountMargin,
		final double dblAssetSwapSpread,
		final double dblCreditBasis,
		final double dblPECS,
		final double dblYield01,
		final double dblModifiedDuration,
		final double dblMacaulayDuration,
		final double dblConvexity,
		final org.drip.param.valuation.WorkoutInfo wi)
		throws java.lang.Exception
	{
		if (null == (_wi = wi)) throw new java.lang.Exception ("BondRVMeasures ctr: Invalid inputs!");

		_dblPECS = dblPECS;
		_dblPrice = dblPrice;
		_dblGSpread = dblGSpread;
		_dblISpread = dblISpread;
		_dblYield01 = dblYield01;
		_dblZSpread = dblZSpread;
		_dblOASpread = dblOASpread;
		_dblBondBasis = dblBondBasis;
		_dblConvexity = dblConvexity;
		_dblTSYSpread = dblTSYSpread;
		_dblCreditBasis = dblCreditBasis;
		_dblDiscountMargin = dblDiscountMargin;
		_dblAssetSwapSpread = dblAssetSwapSpread;
		_dblMacaulayDuration = dblMacaulayDuration;
		_dblModifiedDuration = dblModifiedDuration;
	}

	/**
	 * Return the state as a measure map
	 * 
	 * @param strPrefix RV Measure name prefix
	 * 
	 * @return Map of the RV measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> toMap (
		final java.lang.String strPrefix)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapRVMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapRVMeasures.put (strPrefix + "AssetSwapSpread", _dblAssetSwapSpread);

		mapRVMeasures.put (strPrefix + "ASW", _dblAssetSwapSpread);

		mapRVMeasures.put (strPrefix + "BondBasis", _dblBondBasis);

		mapRVMeasures.put (strPrefix + "Convexity", _dblConvexity);

		mapRVMeasures.put (strPrefix + "CreditBasis", _dblCreditBasis);

		mapRVMeasures.put (strPrefix + "DiscountMargin", _dblDiscountMargin);

		mapRVMeasures.put (strPrefix + "Duration", _dblModifiedDuration);

		mapRVMeasures.put (strPrefix + "GSpread", _dblGSpread);

		mapRVMeasures.put (strPrefix + "ISpread", _dblISpread);

		mapRVMeasures.put (strPrefix + "MacaulayDuration", _dblMacaulayDuration);

		mapRVMeasures.put (strPrefix + "ModifiedDuration", _dblModifiedDuration);

		mapRVMeasures.put (strPrefix + "OAS", _dblOASpread);

		mapRVMeasures.put (strPrefix + "OASpread", _dblOASpread);

		mapRVMeasures.put (strPrefix + "OptionAdjustedSpread", _dblOASpread);

		mapRVMeasures.put (strPrefix + "PECS", _dblPECS);

		mapRVMeasures.put (strPrefix + "Price", _dblPrice);

		mapRVMeasures.put (strPrefix + "TSYSpread", _dblTSYSpread);

		mapRVMeasures.put (strPrefix + "WorkoutDate", _wi.date());

		mapRVMeasures.put (strPrefix + "WorkoutFactor", _wi.factor());

		mapRVMeasures.put (strPrefix + "WorkoutType", (double) _wi.type());

		mapRVMeasures.put (strPrefix + "WorkoutYield", _wi.yield());

		mapRVMeasures.put (strPrefix + "Yield", _wi.yield());

		mapRVMeasures.put (strPrefix + "Yield01", _dblYield01);

		mapRVMeasures.put (strPrefix + "YieldBasis", _dblBondBasis);

		mapRVMeasures.put (strPrefix + "YieldSpread", _dblBondBasis);

		mapRVMeasures.put (strPrefix + "ZSpread", _dblZSpread);

		return mapRVMeasures;
	}
}
