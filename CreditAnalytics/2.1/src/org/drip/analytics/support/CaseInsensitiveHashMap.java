
package org.drip.analytics.support;

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
 * CaseInsensitiveMap implements a case insensitive key in a hash map
 * 
 * @author Michael Beer
 */

@SuppressWarnings ("serial") public class CaseInsensitiveHashMap<V> extends java.util.HashMap<java.lang.String, V>
{
    @Override public V put (
    	final java.lang.String strKey,
    	final V v)
    {
	    return null == strKey ? null : super.put (strKey.toLowerCase(), v);
    }

    @Override public V get (
    	final java.lang.Object objKey)
    {
    	return null == objKey ? null : super.get (((java.lang.String) objKey).toLowerCase());
    }

    @Override public boolean containsKey (
    	final java.lang.Object objKey)
    {
    	return null == objKey ? null : super.containsKey (((java.lang.String) objKey).toLowerCase());
    }
}
