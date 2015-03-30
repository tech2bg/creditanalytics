
package org.drip.analytics.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * LatentStateStatic contains the Analytics Latent STate Static/Textual Identifiers.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateStatic {

	/**
	 * Forward Latent State
	 */

	public static final java.lang.String LATENT_STATE_FORWARD = "LATENT_STATE_FORWARD";

	/**
	 * Forward Latent State Quantification Metric - Forward Rate
	 */

	public static final java.lang.String FORWARD_QM_FORWARD_RATE = "FORWARD_QM_FORWARD_RATE";

	/**
	 * Forward Latent State Quantification Metric - Shifted Forward Rate
	 */

	public static final java.lang.String FORWARD_QM_SHIFTED_FORWARD_RATE = "FORWARD_QM_SHIFTED_FORWARD_RATE";

	/**
	 * Forward Latent State Quantification Metric - Instantaneous Forward Rate
	 */

	public static final java.lang.String FORWARD_QM_INSTANTANEOUS_FORWARD_RATE =
		"FORWARD_QM_INSTANTANEOUS_FORWARD_RATE";

	/**
	 * Funding Latent State
	 */

	public static final java.lang.String LATENT_STATE_FUNDING = "LATENT_STATE_FUNDING";

	/**
	 * Discount Latent State Quantification Metric - Discount Factor
	 */

	public static final java.lang.String DISCOUNT_QM_DISCOUNT_FACTOR = "DISCOUNT_QM_DISCOUNT_FACTOR";

	/**
	 * Discount Latent State Quantification Metric - Zero Rate
	 */

	public static final java.lang.String DISCOUNT_QM_ZERO_RATE = "DISCOUNT_QM_ZERO_RATE";

	/**
	 * Discount Latent State Quantification Metric - Compounded Short Rate
	 */

	public static final java.lang.String DISCOUNT_QM_COMPOUNDED_SHORT_RATE =
		"DISCOUNT_QM_COMPOUNDED_SHORT_RATE";

	/**
	 * Discount Latent State Quantification Metric - Forward Rate
	 */

	public static final java.lang.String DISCOUNT_QM_FORWARD_RATE = "DISCOUNT_QM_FORWARD_RATE";

	/**
	 * Volatility Latent State
	 */

	public static final java.lang.String LATENT_STATE_VOLATILITY = "LATENT_STATE_VOLATILITY";

	/**
	 * Volatility Latent State Quantification Metric - Discount Factor
	 */

	public static final java.lang.String VOLATILITY_QM_VOLATILITY = "VOLATILITY_QM_VOLATILITY";
}
