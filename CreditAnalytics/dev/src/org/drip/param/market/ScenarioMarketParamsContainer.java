
package org.drip.param.market;

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
 * ScenarioMarketParamsContainer extends MarketParams abstract class, and is the place holder for the
 *  comprehensive suite of the market set of curves for the given date. It exports the following
 *  functionality:
 * 	- add/remove/retrieve scenario discount curve
 * 	- add/remove/retrieve scenario Forward curve
 * 	- add/remove/retrieve scenario zero curve
 * 	- add/remove/retrieve scenario credit curve
 * 	- add/remove/retrieve scenario recovery curve
 * 	- add/remove/retrieve scenario FXForward curve
 * 	- add/remove/retrieve scenario FXBasis curve
 * 	- add/remove/retrieve scenario fixings
 * 	- add/remove/retrieve Treasury/component quotes
 * 	- retrieve scenario Market Parameters
 * 	- retrieve map of flat rates/credit/recovery Market Parameters
 * 	- retrieve double map of tenor rates/credit/recovery Market Parameters
 *  - retrieve rates/credit scenario generator
 *
 * @author Lakshmi Krishnamurthy
 */

public class ScenarioMarketParamsContainer extends org.drip.param.definition.ScenarioMarketParams {
	private static final int BASE = 0;
	private static final int BUMP_UP = 1;
	private static final int BUMP_DN = 2;
	private static final int RR_BUMP_UP = 4;
	private static final int RR_BUMP_DN = 8;

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		_mapTSYQuote = null;

	private org.drip.param.market.LatentStateFixingsContainer _lsfc = new
		org.drip.param.market.LatentStateFixingsContainer();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>
		_mapScenarioDiscountCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioForwardCurve>
		_mapScenarioForwardCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioForwardCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>
		_mapScenarioCreditCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		_mapQuote = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
		_mapScenarioMarketParams = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> dcSet (
		final int iBumpType)
	{
		if (null == _mapScenarioDiscountCurve.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meSDC :
			_mapScenarioDiscountCurve.entrySet()) {
			java.lang.String strKey = meSDC.getKey();

			org.drip.param.definition.ScenarioDiscountCurve sdc = meSDC.getValue();

			if (null == strKey || strKey.isEmpty() || null == sdc) continue;

			if (BASE == iBumpType)
				mapDC.put (strKey, sdc.base());
			else if (BUMP_UP == iBumpType)
				mapDC.put (strKey, sdc.bumpUp());
			else if (BUMP_DN == iBumpType)
				mapDC.put (strKey, sdc.bumpDown());
			}

		return mapDC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> fcSet (
		final int iBumpType)
	{
		if (null == _mapScenarioForwardCurve.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioForwardCurve> meSFC :
			_mapScenarioForwardCurve.entrySet()) {
			java.lang.String strKey = meSFC.getKey();

			org.drip.param.definition.ScenarioForwardCurve sfc = meSFC.getValue();

			if (null == strKey || strKey.isEmpty() || null == sfc) continue;

			if (BASE == iBumpType)
				mapFC.put (strKey, sfc.base());
			else if (BUMP_UP == iBumpType)
				mapFC.put (strKey, sfc.bumpUp());
			else if (BUMP_DN == iBumpType)
				mapFC.put (strKey, sfc.bumpDown());
			}

		return mapFC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		ccSet (
			final int iBumpType)
	{
		if (null == _mapScenarioCreditCurve.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meSCC :
			_mapScenarioCreditCurve.entrySet()) {
			java.lang.String strKey = meSCC.getKey();

			org.drip.param.definition.ScenarioCreditCurve scc = meSCC.getValue();

			if (null == strKey || strKey.isEmpty() || null == scc) continue;

			if (BASE == iBumpType)
				mapCC.put (strKey, scc.base());
			else if (BUMP_UP == iBumpType)
				mapCC.put (strKey, scc.bumpUp());
			else if (BUMP_DN == iBumpType)
				mapCC.put (strKey, scc.bumpDown());
			}

		return mapCC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		specificIRFlatBumpDCSet (
			final java.lang.String strIRCurve,
			final boolean bBumpUp)
	{
		if (null == strIRCurve || strIRCurve.isEmpty() || null == _mapScenarioDiscountCurve.get (strIRCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDC =
			dcSet (BASE);

		if (null == mapDC) return null;

		org.drip.param.definition.ScenarioDiscountCurve sdc = _mapScenarioDiscountCurve.get (strIRCurve);

		if (null == sdc) return null;

		mapDC.put (strIRCurve, bBumpUp ? sdc.bumpUp() : sdc.bumpDown());

		return mapDC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		specificForwardFlatBumpFCSet (
			final java.lang.String strForwardCurve,
			final boolean bBumpUp)
	{
		if (null == strForwardCurve || strForwardCurve.isEmpty() || null == _mapScenarioForwardCurve.get
			(strForwardCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFC =
			fcSet (BASE);

		if (null == mapFC) return null;

		org.drip.param.definition.ScenarioForwardCurve sfc = _mapScenarioForwardCurve.get (strForwardCurve);

		if (null == sfc) return null;

		mapFC.put (strForwardCurve, bBumpUp ? sfc.bumpUp() : sfc.bumpDown());

		return mapFC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		specificCreditFlatBumpCCSet (
			final java.lang.String strCreditCurve,
			final boolean bBumpUp)
	{
		if (null == strCreditCurve || strCreditCurve.isEmpty() || null == _mapScenarioCreditCurve.get (strCreditCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC =
			ccSet (BASE);

		if (null == mapCC) return null;

		org.drip.param.definition.ScenarioCreditCurve scc = _mapScenarioCreditCurve.get (strCreditCurve);

		if (null == scc) return null;

		mapCC.put (strCreditCurve, bBumpUp ? scc.bumpUp() : scc.bumpDown());

		return mapCC;
	}

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		specificCreditFlatBumpRRSet (
			final java.lang.String strCreditCurve,
			final boolean bBumpUp)
	{
		if (null == strCreditCurve || strCreditCurve.isEmpty() || null == _mapScenarioCreditCurve.get (strCreditCurve))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapCC =
			ccSet (BASE);

		org.drip.param.definition.ScenarioCreditCurve scc = _mapScenarioCreditCurve.get (strCreditCurve);

		if (null == scc) return null;

		mapCC.put (strCreditCurve, bBumpUp ? scc.bumpRecoveryUp() : scc.bumpRecoveryDown());

		return mapCC;
	}

	private org.drip.param.market.CurveSurfaceQuoteSet customMarketParams (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapDC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			mapCC)
	{
		org.drip.param.market.CurveSurfaceQuoteSet csqs = new org.drip.param.market.CurveSurfaceQuoteSet();

		if (null != mapDC && 0 != mapDC.size()) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve>
				meDC : mapDC.entrySet()) {
				if (null == meDC) continue;

				org.drip.analytics.rates.DiscountCurve dcFunding = meDC.getValue();

				if (null != dcFunding && !csqs.setFundingCurve (dcFunding)) return null;
			}
		}

		if (null != mapFC && 0 != mapFC.size()) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve>
				meFC : mapFC.entrySet()) {
				if (null == meFC) continue;

				org.drip.analytics.rates.ForwardCurve fc = meFC.getValue();

				if (null != fc && !csqs.setForwardCurve (fc)) return null;
			}
		}

		if (null != mapCC && 0 != mapCC.size()) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve>
				meCC : mapCC.entrySet()) {
				if (null == meCC) continue;

				org.drip.analytics.definition.CreditCurve cc = meCC.getValue();

				if (null != cc && !csqs.setCreditCurve (cc)) return null;
			}
		}

		return csqs.setFixings (_lsfc) ? csqs : null;
	}

	/**
	 * Construct an empty MarketParamsContainer instance
	 */

	public ScenarioMarketParamsContainer()
	{
	}

	@Override public boolean addScenarioDiscountCurve (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioDiscountCurve sdc)
	{
		if (null != strName && !strName.isEmpty() && null != sdc) {
			_mapScenarioDiscountCurve.put (strName, sdc);

			return true;
		}

		return false;
	}

	@Override public boolean removeScenarioDiscountCurve (
		final java.lang.String strName)
	{
		if (null != strName && !strName.isEmpty()) {
			_mapScenarioDiscountCurve.remove (strName);

			return true;
		}

		return false;
	}

	@Override public boolean addScenarioForwardCurve (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioForwardCurve sfc)
	{
		if (null != strName && !strName.isEmpty() && null != sfc) {
			_mapScenarioForwardCurve.put (strName, sfc);

			return true;
		}

		return false;
	}

	@Override public boolean removeScenarioForwardCurve (
		final java.lang.String strName)
	{
		if (null != strName && !strName.isEmpty()) {
			_mapScenarioForwardCurve.remove (strName);

			return true;
		}

		return false;
	}

	@Override public boolean addScenarioCreditCurve (
		final java.lang.String strName,
		final org.drip.param.definition.ScenarioCreditCurve scc)
	{
		if (null != strName && !strName.isEmpty() && null != scc) {
			_mapScenarioCreditCurve.put (strName, scc);

			return true;
		}

		return false;
	}

	@Override public boolean removeScenarioCreditCurve (
		final java.lang.String strName)
	{
		if (null != strName && !strName.isEmpty()) {
			_mapScenarioCreditCurve.remove (strName);

			return true;
		}

		return false;
	}

	@Override public boolean addTSYQuote (
		final java.lang.String strBenchmark,
		final org.drip.param.definition.ProductQuote pqTSY)
	{
		if (null == strBenchmark || strBenchmark.isEmpty() || null == pqTSY) return false;

		if (null == _mapTSYQuote)
			_mapTSYQuote = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		_mapTSYQuote.put (strBenchmark, pqTSY);

		return true;
	}

	@Override public boolean removeTSYQuote (
		final java.lang.String strBenchmark)
	{
		if (null == strBenchmark || strBenchmark.isEmpty()) return false;

		if (null == _mapTSYQuote) return true;

		_mapTSYQuote.remove (strBenchmark);

		return true;
	}

	@Override public boolean setTSYQuotes (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote> mapCQTSY)
	{
		_mapTSYQuote = mapCQTSY;
		return true;
	}

	@Override public org.drip.param.definition.ProductQuote tsyQuote (
		final java.lang.String strBenchmark)
	{
		if (null == _mapTSYQuote || null == strBenchmark || strBenchmark.isEmpty()) return null;

		return _mapTSYQuote.get (strBenchmark);
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			tsyQuotes()
	{
		return _mapTSYQuote;
	}

	@Override public boolean addFixing (
		final org.drip.analytics.date.JulianDate dtFix,
		final org.drip.state.identifier.LatentStateLabel lsl,
		final double dblFixing)
	{
		return _lsfc.add (dtFix, lsl, dblFixing);
	}

	@Override public boolean removeFixing (
		final org.drip.analytics.date.JulianDate dtFix,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		return _lsfc.remove (dtFix, lsl);
	}

	@Override public org.drip.param.market.LatentStateFixingsContainer fixings()
	{
		return _lsfc;
	}

	@Override public boolean addComponentQuote (
		final java.lang.String strComponentID,
		final org.drip.param.definition.ProductQuote cqComponent)
	{
		if (null == strComponentID || strComponentID.isEmpty() || null == cqComponent) return false;

		if (null == _mapQuote)
			_mapQuote = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

		_mapQuote.put (strComponentID, cqComponent);

		return true;
	}

	@Override public boolean removeComponentQuote (
		final java.lang.String strComponentID)
	{
		if (null == strComponentID || strComponentID.isEmpty()) return false;

		if (null == _mapQuote) return true;

		_mapQuote.remove (strComponentID);

		return true;
	}

	@Override public boolean addComponentQuote (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapComponentQuote)
	{
		_mapQuote = mapComponentQuote;
		return true;
	}

	@Override public org.drip.param.definition.ProductQuote componentQuote (
		final java.lang.String strComponentID)
	{
		if (null == _mapQuote || null == strComponentID || strComponentID.isEmpty()) return null;

		return _mapQuote.get (strComponentID);
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			componentQuotes()
	{
		return _mapQuote;
	}

	@Override public boolean addScenarioMarketParams (
		final java.lang.String strScenarioName,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == strScenarioName || strScenarioName.isEmpty() || null == csqs) return false;

		_mapScenarioMarketParams.put (strScenarioName, csqs);

		return true;
	}

	@Override public org.drip.param.market.CurveSurfaceQuoteSet scenarioMarketParams (
		final java.lang.String strScenarioName)
	{
		return null == strScenarioName || strScenarioName.isEmpty() ? null : _mapScenarioMarketParams.get
			(strScenarioName);
	}

	@Override public org.drip.param.market.CurveSurfaceQuoteSet scenarioMarketParams (
		final org.drip.product.definition.FixedIncomeComponent comp,
		final java.lang.String strScenario)
	{
		if (null == comp || null == strScenario || strScenario.isEmpty()) return null;

		org.drip.analytics.rates.ForwardCurve fc = null;
		org.drip.analytics.rates.DiscountCurve dc = null;
		org.drip.analytics.rates.DiscountCurve dcTSY = null;
		org.drip.analytics.definition.CreditCurve cc = null;

		java.lang.String strPayCurrency = comp.payCurrency();

		org.drip.param.definition.ScenarioDiscountCurve sdc = _mapScenarioDiscountCurve.get (strPayCurrency);

		if (null != sdc) {
			dc = dcTSY = sdc.base();

			if ("FlatIRBumpUp".equalsIgnoreCase (strScenario))
				dc = sdc.bumpUp();
			else if ("FlatIRBumpDn".equalsIgnoreCase (strScenario))
				dc = sdc.bumpDown();
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.state.identifier.ForwardLabel>
			mapForwardLabel = comp.forwardLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = null == mapForwardLabel || 0 ==
			mapForwardLabel.size() ? null : mapForwardLabel.get (0);

		org.drip.param.definition.ScenarioForwardCurve sfc = null == forwardLabel ? null :
			_mapScenarioForwardCurve.get (forwardLabel);

		if (null != sfc) {
			if ("FlatForwardBumpUp".equalsIgnoreCase (strScenario))
				fc = sfc.bumpUp();
			else if ("FlatForwardBumpDn".equalsIgnoreCase (strScenario))
				fc = sfc.bumpDown();
			else
				fc = sfc.base();
		}

		org.drip.state.identifier.CreditLabel creditLabel = comp.creditLabel();

		org.drip.param.definition.ScenarioCreditCurve scc = null == creditLabel ? null :
			_mapScenarioCreditCurve.get (creditLabel);

		if (null != scc) {
			if ("FlatCreditBumpUp".equalsIgnoreCase (strScenario))
				cc = scc.bumpUp();
			else if ("FlatCreditBumpDn".equalsIgnoreCase (strScenario))
				cc = scc.bumpDown();
			else
				cc = scc.base();
		}

		return org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc, comp.name(),
			_mapQuote.get (comp.name()), _mapTSYQuote, _lsfc);
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			fundingTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp)
	{
		if (null == comp) return null;

		java.lang.String strPayCurrency = comp.payCurrency();

		org.drip.param.definition.ScenarioDiscountCurve sdc = _mapScenarioDiscountCurve.get (strPayCurrency);

		if (null == sdc) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve> mapDCBumpUp
			= sdc.tenorBumpUp();

		if (bBumpUp && (null == mapDCBumpUp || 0 == mapDCBumpUp.size())) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapDCBumpDown = sdc.tenorBumpDown();

		if (!bBumpUp && (null == mapDCBumpDown || 0 == mapDCBumpDown.size())) return null;

		org.drip.analytics.rates.DiscountCurve dcTSY = sdc.base();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.state.identifier.ForwardLabel>
			mapForwardLabel = comp.forwardLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = null == mapForwardLabel || 0 ==
			mapForwardLabel.size() ? null : mapForwardLabel.get (0);

		org.drip.param.definition.ScenarioForwardCurve sfc = null == forwardLabel ? null :
			_mapScenarioForwardCurve.get (forwardLabel);

		org.drip.analytics.rates.ForwardCurve fc = null == sfc ? null : sfc.base();

		org.drip.state.identifier.CreditLabel creditLabel = comp.creditLabel();

		org.drip.param.definition.ScenarioCreditCurve scc = null == creditLabel ? null :
			_mapScenarioCreditCurve.get (creditLabel);

		org.drip.analytics.definition.CreditCurve cc = null == scc ? null : scc.base();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet> mapCSQS
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		java.lang.String strComponentName = comp.name();

		if (bBumpUp) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> meDC :
				mapDCBumpUp.entrySet()) {
				if (null == meDC) continue;

				java.lang.String strKey = meDC.getKey();

				org.drip.analytics.rates.DiscountCurve dc = meDC.getValue();

				if (null == dc || null == strKey || strKey.isEmpty()) continue;

				mapCSQS.put (strKey, org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc,
					strComponentName, _mapQuote.get (strComponentName), _mapTSYQuote, _lsfc));
			}
		} else {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> meDC :
				mapDCBumpDown.entrySet()) {
				if (null == meDC) continue;

				java.lang.String strKey = meDC.getKey();

				org.drip.analytics.rates.DiscountCurve dc = meDC.getValue();

				if (null == dc || null == strKey || strKey.isEmpty()) continue;

				mapCSQS.put (strKey, org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc,
					strComponentName, _mapQuote.get (strComponentName), _mapTSYQuote, _lsfc));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			forwardTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp)
	{
		if (null == comp) return null;

		java.lang.String strPayCurrency = comp.payCurrency();

		org.drip.param.definition.ScenarioDiscountCurve sdc = _mapScenarioDiscountCurve.get (strPayCurrency);

		if (null == sdc) return null;

		org.drip.analytics.rates.DiscountCurve dc = sdc.base();

		org.drip.analytics.rates.DiscountCurve dcTSY = dc;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.state.identifier.ForwardLabel>
			mapForwardLabel = comp.forwardLabel();

		if (null == mapForwardLabel || 0 == mapForwardLabel.size()) return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = mapForwardLabel.get (0);

		if (null == forwardLabel) return null;

		org.drip.param.definition.ScenarioForwardCurve sfc = _mapScenarioForwardCurve.get (forwardLabel);

		if (null == sfc) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve> mapFCBumpUp
			= sfc.tenorBumpUp();

		if (bBumpUp && (null == mapFCBumpUp || 0 == mapFCBumpUp.size())) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
			mapFCBumpDown = sfc.tenorBumpDown();

		if (!bBumpUp && (null == mapFCBumpDown || 0 == mapFCBumpDown.size())) return null;

		org.drip.state.identifier.CreditLabel creditLabel = comp.creditLabel();

		org.drip.param.definition.ScenarioCreditCurve scc = null == creditLabel ? null :
			_mapScenarioCreditCurve.get (creditLabel);

		org.drip.analytics.definition.CreditCurve cc = null == scc ? null : scc.base();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet> mapCSQS
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		java.lang.String strComponentName = comp.name();

		if (bBumpUp) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve> meFC :
				mapFCBumpUp.entrySet()) {
				if (null == meFC) continue;

				java.lang.String strKey = meFC.getKey();

				org.drip.analytics.rates.ForwardCurve fc = meFC.getValue();

				if (null == fc || null == strKey || strKey.isEmpty()) continue;

				mapCSQS.put (strKey, org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc,
					strComponentName, _mapQuote.get (strComponentName), _mapTSYQuote, _lsfc));
			}
		} else {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.ForwardCurve> meFC :
				mapFCBumpDown.entrySet()) {
				if (null == meFC) continue;

				java.lang.String strKey = meFC.getKey();

				org.drip.analytics.rates.ForwardCurve fc = meFC.getValue();

				if (null == fc || null == strKey || strKey.isEmpty()) continue;

				mapCSQS.put (strKey, org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc,
					strComponentName, _mapQuote.get (strComponentName), _mapTSYQuote, _lsfc));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			creditTenorMarketParams (
				final org.drip.product.definition.FixedIncomeComponent comp,
				final boolean bBumpUp)
	{
		if (null == comp) return null;

		org.drip.state.identifier.CreditLabel creditLabel = comp.creditLabel();

		if (null == creditLabel) return null;

		org.drip.param.definition.ScenarioCreditCurve scc = _mapScenarioCreditCurve.get (creditLabel);

		if (null == scc) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			mapCCBumpUp = scc.tenorBumpUp();

		if (bBumpUp && (null == mapCCBumpUp || 0 == mapCCBumpUp.size())) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			mapCCBumpDown = scc.tenorBumpDown();

		if (!bBumpUp && (null == mapCCBumpDown || 0 == mapCCBumpDown.size())) return null;

		java.lang.String strPayCurrency = comp.payCurrency();

		org.drip.param.definition.ScenarioDiscountCurve sdc = _mapScenarioDiscountCurve.get (strPayCurrency);

		if (null == sdc) return null;

		org.drip.analytics.rates.DiscountCurve dc = sdc.base();

		org.drip.analytics.rates.DiscountCurve dcTSY = dc;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.state.identifier.ForwardLabel>
			mapForwardLabel = comp.forwardLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = null == mapForwardLabel || 0 ==
			mapForwardLabel.size() ? null : mapForwardLabel.get (0);

		org.drip.param.definition.ScenarioForwardCurve sfc = null == forwardLabel ? null :
			_mapScenarioForwardCurve.get (forwardLabel);

		org.drip.analytics.rates.ForwardCurve fc = null == sfc ? null : sfc.base();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet> mapCSQS
			= new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		java.lang.String strComponentName = comp.name();

		if (bBumpUp) {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> meCC :
				mapCCBumpUp.entrySet()) {
				if (null == meCC) continue;

				java.lang.String strKey = meCC.getKey();

				org.drip.analytics.definition.CreditCurve cc = meCC.getValue();

				if (null == fc || null == strKey || strKey.isEmpty()) continue;

				mapCSQS.put (strKey, org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc,
					strComponentName, _mapQuote.get (strComponentName), _mapTSYQuote, _lsfc));
			}
		} else {
			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> meCC :
				mapCCBumpDown.entrySet()) {
				if (null == meCC) continue;

				java.lang.String strKey = meCC.getKey();

				org.drip.analytics.definition.CreditCurve cc = meCC.getValue();

				if (null == fc || null == strKey || strKey.isEmpty()) continue;

				mapCSQS.put (strKey, org.drip.param.creator.MarketParamsBuilder.Create (dc, fc, dcTSY, cc,
					strComponentName, _mapQuote.get (strComponentName), _mapTSYQuote, _lsfc));
			}
		}

		return mapCSQS;
	}

	@Override public org.drip.param.market.CurveSurfaceQuoteSet scenarioMarketParams (
		final org.drip.product.definition.BasketProduct bp,
		final java.lang.String strScenario)
	{
		if (null == strScenario || strScenario.isEmpty()) return null;

		if ("Base".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BASE), ccSet (BASE));

		if ("FlatIRBumpUp".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BUMP_UP), fcSet (BASE), ccSet (BASE));

		if ("FlatIRBumpDn".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BUMP_DN), fcSet (BASE), ccSet (BASE));

		if ("FlatForwardBumpUp".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BUMP_UP), ccSet (BASE));

		if ("FlatForwardBumpDn".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BUMP_DN), ccSet (BASE));

		if ("FlatCreditBumpUp".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BASE), ccSet (BUMP_UP));

		if ("FlatCreditBumpDn".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BASE), ccSet (BUMP_DN));

		if ("FlatRRBumpUp".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BASE), ccSet (RR_BUMP_UP));

		if ("FlatRRBumpDn".equalsIgnoreCase (strScenario))
			return customMarketParams (dcSet (BASE), fcSet (BASE), ccSet (RR_BUMP_DN));

		return null;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			fundingFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meSDC :
			_mapScenarioDiscountCurve.entrySet()) {
			if (null != meSDC) {
				java.lang.String strKey = meSDC.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapCSQS.put (strKey, customMarketParams (specificIRFlatBumpDCSet (strKey, bBump), fcSet
						(BASE), ccSet (BASE)));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			forwardFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioForwardCurve> meSFC :
			_mapScenarioForwardCurve.entrySet()) {
			if (null != meSFC) {
				java.lang.String strKey = meSFC.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapCSQS.put (strKey, customMarketParams (dcSet (BASE), specificForwardFlatBumpFCSet
						(strKey, bBump), ccSet (BASE)));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			creditFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meSCC :
			_mapScenarioCreditCurve.entrySet()) {
			if (null != meSCC) {
				java.lang.String strKey = meSCC.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapCSQS.put (strKey, customMarketParams (dcSet (BASE), fcSet (BASE),
						specificCreditFlatBumpCCSet (strKey, bBump)));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			recoveryFlatBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meSCC :
			_mapScenarioCreditCurve.entrySet()) {
			if (null != meSCC) {
				java.lang.String strKey = meSCC.getKey();

				if (null != strKey && !strKey.isEmpty())
					mapCSQS.put (strKey, customMarketParams (dcSet (BASE), fcSet (BASE),
						specificCreditFlatBumpRRSet (strKey, bBump)));
			}
		}

		return mapCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			fundingTenorBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		if (null == bp) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			mmFundingTenorCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioDiscountCurve> meSDC :
			_mapScenarioDiscountCurve.entrySet()) {
			if (null == meSDC) continue;

			java.lang.String strOuterKey = meSDC.getKey();

			org.drip.param.definition.ScenarioDiscountCurve sdc = meSDC.getValue();

			if (null == sdc || null == strOuterKey || strOuterKey.isEmpty()) continue;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
				mapDCBumpUp = sdc.tenorBumpUp();

			if (bBump && (null == mapDCBumpUp || 0 == mapDCBumpUp.size())) return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
				mapDCBumpDown = sdc.tenorBumpDown();

			if (!bBump && (null == mapDCBumpDown || 0 == mapDCBumpDown.size())) return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
				mapTenorCSQS = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> meDC : (bBump
				? mapDCBumpUp.entrySet() : mapDCBumpDown.entrySet())) {
				if (null == meDC) continue;

				java.lang.String strInnerKey = meDC.getKey();

				org.drip.analytics.rates.DiscountCurve dc = meDC.getValue();

				if (null == dc || null == strInnerKey || strInnerKey.isEmpty()) continue;

				org.drip.param.market.CurveSurfaceQuoteSet csqs = scenarioMarketParams (bp, "Base");

				if (null == csqs || !csqs.setFundingCurve (dc)) continue;

				mapTenorCSQS.put (strInnerKey, csqs);
			}

			mmFundingTenorCSQS.put (strOuterKey, mapTenorCSQS);
		}

		return mmFundingTenorCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			creditTenorBump (
				final org.drip.product.definition.BasketProduct bp,
				final boolean bBump)
	{
		if (null == bp) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>
			mmCreditTenorCSQS = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ScenarioCreditCurve> meSCC :
			_mapScenarioCreditCurve.entrySet()) {
			if (null == meSCC) continue;

			java.lang.String strOuterKey = meSCC.getKey();

			org.drip.param.definition.ScenarioCreditCurve scc = meSCC.getValue();

			if (null == scc || null == strOuterKey || strOuterKey.isEmpty()) continue;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
				mapCCBumpUp = scc.tenorBumpUp();

			if (bBump && (null == mapCCBumpUp || 0 == mapCCBumpUp.size())) return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
				mapCCBumpDown = scc.tenorBumpDown();

			if (!bBump && (null == mapCCBumpDown || 0 == mapCCBumpDown.size())) return null;

			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
				mapTenorCSQS = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>();

			for (java.util.Map.Entry<java.lang.String, org.drip.analytics.definition.CreditCurve> meCC :
				(bBump ? mapCCBumpUp.entrySet() : mapCCBumpDown.entrySet())) {
				if (null == meCC) continue;

				java.lang.String strInnerKey = meCC.getKey();

				org.drip.analytics.definition.CreditCurve cc = meCC.getValue();

				if (null == cc || null == strInnerKey || strInnerKey.isEmpty()) continue;

				org.drip.param.market.CurveSurfaceQuoteSet csqs = scenarioMarketParams (bp, "Base");

				if (null == csqs || !csqs.setCreditCurve (cc)) continue;

				mapTenorCSQS.put (strInnerKey, csqs);
			}

			mmCreditTenorCSQS.put (strOuterKey, mapTenorCSQS);
		}

		return mmCreditTenorCSQS;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioDiscountCurve>
			scenarioDiscountCurveMap()
	{
		return _mapScenarioDiscountCurve;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ScenarioCreditCurve>
			scenarioCreditCurveMap()
	{
		return _mapScenarioCreditCurve;
	}
}
