
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * LatentStateQuoteSensitivity holds the Sensitivity of the Latent State to the Calibration Quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateQuoteSensitivity {
	private java.util.TreeMap<java.lang.Double, java.lang.Double> _mapLatentStateQuoteSensitivity = null;

	/**
	 * Empty LatentStateQuoteSensitivity constructor
	 */

	public LatentStateQuoteSensitivity()
	{
	}

	/**
	 * Set the Sensitivity corresponding to the Latent State's Univariate Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Latent State's Univariate Predictor Ordinate
	 * @param dblSensitivity The Sensitivity
	 * 
	 * @return TRUE => The Sensitivity Successfully set
	 */

	public boolean set (
		final double dblPredictorOrdinate,
		final double dblSensitivity)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblSensitivity))
			return false;

		if (null == _mapLatentStateQuoteSensitivity)
			_mapLatentStateQuoteSensitivity = new java.util.TreeMap<java.lang.Double, java.lang.Double>();

		_mapLatentStateQuoteSensitivity.put (dblPredictorOrdinate, dblSensitivity);

		return true;
	}
}
