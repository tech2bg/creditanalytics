
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
 * CreditComponent is the base abstract class on top of which all credit components are implemented. Its
 * 	methods expose Credit Valuation Parameters, product specific recovery, and coupon/loss cash flows.
 *  
 * @author Lakshmi Krishnamurthy
 */

public abstract class CreditComponent extends org.drip.product.definition.CalibratableComponent {

	/**
	 * Get the coupon flow for the credit component
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams Component Market Params
	 * 
	 * @return List of ProductCouponPeriodCurveMeasures
	 */

	public abstract java.util.List<org.drip.analytics.period.CashflowPeriodCurveFactors> getCouponFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams);

	/**
	 * Generate the loss flow for the credit component based on the pricer parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mktParams ComponentMarketParams
	 * 
	 * @return List of ProductLossPeriodCurveMeasures
	 */

	public abstract java.util.List<org.drip.analytics.period.LossPeriodCurveFactors> getLossFlow (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams);

	/**
	 * Get the recovery of the credit component for the given date
	 * 
	 * @param dblDate Double JulianDate
	 * @param cc Credit Curve
	 * 
	 * @return Recovery
	 * 
	 * @throws java.lang.Exception Thrown if recovery cannot be calculated
	 */

	public abstract double getRecovery (
		final double dblDate,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception;

	/**
	 * Get the time-weighted recovery of the credit component between the given dates
	 * 
	 * @param dblDate1 Double JulianDate
	 * @param dblDate2 Double JulianDate
	 * @param cc Credit Curve
	 * 
	 * @return Recovery
	 * 
	 * @throws java.lang.Exception Thrown if recovery cannot be calculated
	 */

	public abstract double getRecovery (
		final double dblDate1,
		final double dblDate2,
		final org.drip.analytics.definition.CreditCurve cc)
		throws java.lang.Exception;

	/**
	 * Get the credit component's Credit Valuation Parameters
	 * 
	 * @return CompCRValParams
	 */

	public abstract org.drip.product.params.CreditSetting getCRValParams();
}
