
package org.drip.param.pricer;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * PricerParams contains the pricer parameters - the discrete unit size, calibration mode on/off, survival to
 * 	pay/end date, and the discretization scheme
 *
 * @author Lakshmi Krishnamurthy
 */

public class PricerParams {

	/*
	 * Loss period Grid discretization scheme
	 */

	/**
	 * Minimum number of days per unit
	 */

	public static final int PERIOD_DAY_STEPS_MINIMUM = 7;

	/**
	 * Discretization as a sequence of day steps
	 */

	public static final int PERIOD_DISCRETIZATION_DAY_STEP = 1;

	/**
	 * Discretization as a sequence of time space divided periods
	 */

	public static final int PERIOD_DISCRETIZATION_PERIOD_STEP = 2;

	/**
	 * No discretization at all - just the full coupon period
	 */

	public static final int PERIOD_DISCRETIZATION_FULL_COUPON = 3;

	private int _iUnitSize = 7;
	private boolean _bSurvToPayDate = false;
	private boolean _bAmetranoBianchettiForwardQuanto = false;
	private int _iDiscretizationScheme = PERIOD_DISCRETIZATION_DAY_STEP;
	private org.drip.param.definition.CalibrationParams _calibParams = null;

	/**
	 * Create the standard pricer parameters object instance
	 * 
	 * @return PricerParams object instance
	 */

	public static final PricerParams Standard()
	{
		return new PricerParams (7, null, false, PERIOD_DISCRETIZATION_DAY_STEP, false);
	}

	/**
	 * Create the pricer parameters from the discrete unit size, calibration mode on/off, survival to
	 * 	pay/end date, and the discretization scheme
	 * 
	 * @param iUnitSize Discretization Unit Size
	 * @param calibParams Optional Calibration Params
	 * @param bSurvToPayDate Survival to Pay Date (True) or Period End Date (false)
	 * @param iDiscretizationScheme Discretization Scheme In Use
	 * @param bAmetranoBianchettiForwardQuanto TRUE => Ametrano-Bianchetti (2013) Forward Mode On
	 */

	public PricerParams (
		final int iUnitSize,
		final org.drip.param.definition.CalibrationParams calibParams,
		final boolean bSurvToPayDate,
		final int iDiscretizationScheme,
		final boolean bAmetranoBianchettiForwardQuanto)
	{
		_iUnitSize = iUnitSize;
		_calibParams = calibParams;
		_bSurvToPayDate = bSurvToPayDate;
		_iDiscretizationScheme = iDiscretizationScheme;
		_bAmetranoBianchettiForwardQuanto = bAmetranoBianchettiForwardQuanto;
	}

	/**
	 * Retrieve the Discretized Loss Unit Size
	 * 
	 * @return The Discretized Loss Unit Size
	 */

	public int unitSize()
	{
		return _iUnitSize;
	}

	/**
	 * Retrieve the Calibration Parameters Instance
	 * 
	 * @return The Calibration Parameters Instance
	 */

	public org.drip.param.definition.CalibrationParams calibParams()
	{
		return _calibParams;
	}

	/**
	 * Retrieve the flag indicating whether the Survival is to be computed to the Pay Date (TRUE) or not
	 * 
	 * @return TRUE => Survival is to be computed to the Pay Date
	 */

	public boolean survivalToPayDate()
	{
		return _bSurvToPayDate;
	}

	/**
	 * Retrieve the Discretization Scheme
	 * 
	 * @return The Discretization Scheme
	 */

	public int discretizationScheme()
	{
		return _iDiscretizationScheme;
	}

	/**
	 * Retrieve the Flag indicating if the Ametrano-Bianchetti Mode has been turned on
	 *  
	 * @return TRUE => Ametrano-Bianchetti Mode has been turned on
	 */

	public boolean ametranoBianchettiMode()
	{
		return _bAmetranoBianchettiForwardQuanto;
	}
}
