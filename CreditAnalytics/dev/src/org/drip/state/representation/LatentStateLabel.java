
package org.drip.state.representation;

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
 * LatentStateLabel is an empty interface that contains the labels inside the sub-stretch of the alternate
 * 	state.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface LatentStateLabel {

	/**
	 * Retrieve the Fully Qualified Name
	 * 
	 * @return The Fully Qualified Name
	 */

	public abstract java.lang.String fullyQualifiedName();

	/**
	 * Indicate whether this Label matches the supplied.
	 * 
	 * @param lslOther The Supplied Label
	 * 
	 * @return TRUE => The Supplied Label matches this.
	 */

	public abstract boolean match (
		final LatentStateLabel lslOther);
}
