
package org.drip.param.pricer;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * JointStatePricerParams enhances PricerParams by holding the Flag that indicates what type of quanto
 *  adjustment needs to applied when computing the joint numeraire.
 *
 * @author Lakshmi Krishnamurthy
 */

public class JointStatePricerParams extends org.drip.param.pricer.PricerParams {

	/**
	 * Quanto Adjustment - No Adjustment Applied
	 */

	public static final int QUANTO_ADJUSTMENT_NONE = 0;

	/**
	 * Quanto Adjustment - Forward/Funding Volatility/Correlation
	 */

	public static final int QUANTO_ADJUSTMENT_FORWARD_FUNDING = 1;

	/**
	 * Quanto Adjustment - Forward/FX Volatility/Correlation
	 */

	public static final int QUANTO_ADJUSTMENT_FORWARD_FX = 2;

	/**
	 * Quanto Adjustment - Funding/FX Volatility/Correlation
	 */

	public static final int QUANTO_ADJUSTMENT_FUNDING_FX = 4;

	/**
	 * Quanto Adjustment - Forward/Funding/FX Volatility/Correlation
	 */

	public static final int QUANTO_ADJUSTMENT_FORWARD_FUNDING_FX = 8;

	/**
	 * Create the JointStatePricerParams Instance from the Quanto Adjustment Type Specified
	 * 
	 * @param iQuantoAdjustment The Quanto Adjustment Type to be applied
	 * 
	 * @return PricerParams object instance
	 */

	public static final JointStatePricerParams Make (
		final int iQuantoAdjustment)
	{
		return new JointStatePricerParams (7, null, false,
			org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP, iQuantoAdjustment);
	}

	private int _iQuantoAdjustment = QUANTO_ADJUSTMENT_NONE;

	/**
	 * Create the pricer parameters from the discrete unit size, calibration mode on/off, survival to
	 * 	pay/end date, and the discretization scheme
	 * 
	 * @param iUnitSize Discretization Unit Size
	 * @param calibParams Optional Calibration Params
	 * @param bSurvToPayDate Survival to Pay Date (True) or Period End Date (false)
	 * @param iDiscretizationScheme Discretization Scheme In Use
	 * @param iQuantoAdjustment The Quanto Adjustment Type to be applied
	 */

	public JointStatePricerParams (
		final int iUnitSize,
		final org.drip.param.definition.CalibrationParams calibParams,
		final boolean bSurvToPayDate,
		final int iDiscretizationScheme,
		final int iQuantoAdjustment)
	{
		super (iUnitSize, calibParams, bSurvToPayDate, iDiscretizationScheme);

		_iQuantoAdjustment = iQuantoAdjustment;
	}

	/**
	 * Retrieve the Quanto Adjustment Type to be applied
	 * 
	 * @return The Quanto Adjustment Type to be applied
	 */

	public int quantoAdjustment()
	{
		return _iQuantoAdjustment;
	}
}
