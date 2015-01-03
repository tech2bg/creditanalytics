
package org.drip.param.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * CreditScenarioCurveBuilder implements the construction, de-serialization, and building of the custom
 *  Scenario based credit curves.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CreditScenarioCurveBuilder {

	/**
	 * Create CreditScenarioCurve from the array of calibration instruments
	 * 
	 * @param aCalibInst Array of calibration instruments
	 * 
	 * @return CreditScenarioCurve object
	 */

	public static final org.drip.param.definition.ScenarioCreditCurve CreateCCSC (
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst)
	{
		try {
			return new org.drip.param.market.CreditCurveScenarioContainer (aCalibInst, 0.0001, 0.01);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calibrate the base credit curve from the input credit instruments, measures, and the quotes
	 * 
	 * @param strName Credit Curve Name
	 * @param dt Valuation Date
	 * @param aCalibInst Array of calibration instruments
	 * @param dc Discount Curve
	 * @param adblQuotes Array of instrument Quotes
	 * @param astrCalibMeasure Array of calibration Measures
	 * @param dblRecovery Recovery Rate
	 * @param bFlat Whether the Calibration is based off of a flat spread
	 * 
	 * @return The cooked Credit Curve
	 */

	public static final org.drip.analytics.definition.CreditCurve CreateCreditCurve (
		final java.lang.String strName,
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.product.definition.CalibratableFixedIncomeComponent[] aCalibInst,
		final org.drip.analytics.rates.DiscountCurve dc,
		final double[] adblQuotes,
		final java.lang.String[] astrCalibMeasure,
		final double dblRecovery,
		final boolean bFlat)
	{
		org.drip.param.definition.ScenarioCreditCurve ccsc = CreateCCSC (aCalibInst);

		if (null == ccsc || !ccsc.cookScenarioCC (strName,
			org.drip.param.valuation.ValuationParams.CreateValParams (dt, 0, "",
				org.drip.analytics.daycount.Convention.DATE_ROLL_ACTUAL), dc, null, adblQuotes, dblRecovery,
					astrCalibMeasure, null, null, bFlat,
						org.drip.param.definition.ScenarioCreditCurve.CC_BASE))
			return null;

		return ccsc.base();
	}
}
