
package org.drip.product.rates;

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
 * GenericDualStreamComponent is the abstract class that extends the CalibratableFixedIncomeComponent on top of
 *  which all the dual stream rates components (fix-float, float-float, IRS etc.) are implemented.
 *  
 * @author Lakshmi Krishnamurthy
 */

public abstract class GenericDualStreamComponent extends
	org.drip.product.definition.CalibratableFixedIncomeComponent {

	/**
	 * Retrieve the Reference Stream
	 * 
	 * @return The Reference Stream
	 */

	public abstract org.drip.product.rates.GenericStream referenceStream();

	/**
	 * Retrieve the Derived Stream
	 * 
	 * @return The Derived Stream
	 */

	public abstract org.drip.product.rates.GenericStream derivedStream();
}
