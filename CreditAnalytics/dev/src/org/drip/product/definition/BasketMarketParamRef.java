
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
 * BasketMarketParamRef interface provides stubs for component's IR and credit curves that constitute the
 *  basket.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface BasketMarketParamRef {

	/**
	 * Retrieve the set of the component IR curve names
	 * 
	 * @return The set of the component IR curve names
	 */

	public abstract java.util.Set<java.lang.String> getComponentIRCurveNames();

	/**
	 * Retrieve the set of the component Forward curve names
	 * 
	 * @return The set of the component Forward curve names
	 */

	public abstract java.util.Set<java.lang.String> getComponentForwardCurveNames();

	/**
	 * Retrieve the set of the component credit curve names
	 * 
	 * @return The set of the component credit curve names
	 */

	public abstract java.util.Set<java.lang.String> getComponentCreditCurveNames();

	/**
	 * Retrieve the set of the component Treasury curve names
	 * 
	 * @return The set of the component Treasury curve names
	 */

	public abstract java.util.Set<java.lang.String> getComponentTreasuryCurveNames();

	/**
	 * Retrieve the set of the component EDSF curve names
	 * 
	 * @return The set of the component EDSF curve names
	 */

	public abstract java.util.Set<java.lang.String> getComponentEDSFCurveNames();
}
