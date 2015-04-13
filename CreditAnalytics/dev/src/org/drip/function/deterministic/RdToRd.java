
package org.drip.function.deterministic;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * RdToRd provides the evaluation of the R^d -> R^d objective function and its derivatives for a specified
 * 	set of R^d variates. Default implementation of the derivatives are for non-analytical black box objective
 * 	functions.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class RdToRd {

	/**
	 * Evaluate for the given Input R^d Variates
	 * 
	 * @param adblVariate Array of Input R^d Variates
	 *  
	 * @return The Output R^d Variates
	 */

	public abstract double[] evaluate (
		final double[] adblVariate);
}
