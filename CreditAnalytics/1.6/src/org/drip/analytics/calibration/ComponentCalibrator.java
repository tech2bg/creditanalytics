
package org.drip.analytics.calibration;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * This interface defines the curve calibration methods – bootstrapping the discount rate and the hazard rate
 * 	from the individual component quotes. Calibration can be node-by-node (true bootstrapping) or flat.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface ComponentCalibrator {
	/**
	 * Bootstraps the interest rate curve from the component quote
	 * 
	 * @param dc Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param comp Calibration Component
	 * @param iInstr Bootstrap index
	 * @param valParams ValuationParams
	 * @param strMeasure Component measure to be calibrated
	 * @param dblCalibValue Component measure quote
	 * @param mmFixings Fixings object
	 * @param quotingParams Quoting Parameters
	 * @param bFlat Flat calibration (true), True bootstrapping (false)
	 * 
	 * @return Success (true), failure (false)
	 */

	public boolean bootstrapInterestRate (
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final org.drip.product.definition.Component comp,
		final int iInstr,
		final org.drip.param.valuation.ValuationParams valParams,
		final java.lang.String strMeasure,
		final double dblCalibValue,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final boolean bFlat);

	/**
	 * Bootstraps the hazard rate curve from the component quote
	 * 
	 * @param cc Credit Curve
	 * @param comp Calibration Component
	 * @param iInstr Bootstrap index
	 * @param valParams ValuationParams
	 * @param dc Base Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param pricerParams PricerParams
	 * @param strMeasure Component measure to be calibrated
	 * @param dblCalibValue Component measure quote
	 * @param mmFixings Fixings object
	 * @param quotingParams Quoting Parameters
	 * @param bFlat Flat calibration (true), True bootstrapping (false)
	 * 
	 * @return Success (true), failure (false)
	 */

	public boolean bootstrapHazardRate (
		org.drip.analytics.definition.CreditCurve cc,
		final org.drip.product.definition.Component comp,
		final int iInstr,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final org.drip.param.pricer.PricerParams pricerParams,
		final java.lang.String strMeasure,
		final double dblCalibValue,
		final java.util.Map<org.drip.analytics.date.JulianDate, java.util.Map<java.lang.String,
			java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final boolean bFlat);
}
