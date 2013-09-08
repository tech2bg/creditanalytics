
package org.drip.analytics.calibration;

public class LinearCurveCalibrator {
	private org.drip.math.grid.SegmentBuilderParams _sbpRegular = null;
	private org.drip.math.grid.SegmentBuilderParams _sbpTransition = null;

	private static final org.drip.math.spline.SegmentNodeWeightConstraint GenerateSegmentConstraint (
		final org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc,
		final org.drip.math.grid.MultiSegmentSpan spanDF)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapResponsePredictorWeight =
			prlc.getResponsePredictorWeight();

		if (null == mapResponsePredictorWeight || 0 == mapResponsePredictorWeight.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> setRP =
			mapResponsePredictorWeight.entrySet();

		if (null == setRP) return null;

		double dblValue = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseWeight = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : setRP) {
			if (null == me) return null;

			double dblDate = me.getKey();

			if (null != spanDF && spanDF.isInRange (dblDate)) {
				try {
					dblValue -= spanDF.calcValue (dblDate) * me.getValue();
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				lsPredictor.add (dblDate);

				lsResponseWeight.add (me.getValue());
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblResponseWeight = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblResponseWeight[i] = lsResponseWeight.get (i);
		}

		try {
			return new org.drip.math.spline.SegmentNodeWeightConstraint (adblPredictor, adblResponseWeight,
				prlc.getValue() + dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public org.drip.math.grid.MultiSegmentSpan spanFromCashInstruments (
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return null;

		int iNumCalibComp = aCalibComp.length;
		org.drip.math.grid.MultiSegmentSpan span = null;

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return null;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return null;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return null;

			org.drip.math.spline.SegmentNodeWeightConstraint snwc = GenerateSegmentConstraint (prlc, span);

			if (null == snwc) return null;

			if (null == span) {
				if (null == (span = org.drip.math.grid.SpanBuilder.CreateUncalibratedSpanInterpolator (new
					double[] {valParams._dblValue, aCalibComp[i].getMaturityDate().getJulian()}, new
						org.drip.math.grid.SegmentBuilderParams[] {_sbpRegular})) || !span.setup (1., new
							org.drip.math.spline.SegmentNodeWeightConstraint[] {snwc},
								org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL,
									org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN))
					return null;
			} else {
				if (null == (span = org.drip.math.grid.SpanBuilder.AppendSegment (span,
					aCalibComp[i].getMaturityDate().getJulian(), snwc, _sbpRegular)))
					return null;
			}
		}

		return span;
	}

	public org.drip.math.grid.MultiSegmentSpan spanFromSwapInstruments (
		org.drip.math.grid.MultiSegmentSpan span,
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return null;

		int iNumCalibComp = aCalibComp.length;

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return null;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return null;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return null;

			org.drip.math.spline.SegmentNodeWeightConstraint snwc = GenerateSegmentConstraint (prlc, span);

			if (null == snwc) return null;

			org.drip.math.grid.SegmentBuilderParams sbp = 0 == i ? _sbpTransition : _sbpRegular;

			if (null == span) {
				if (null == (span = org.drip.math.grid.SpanBuilder.CreateUncalibratedSpanInterpolator (new
					double[] {valParams._dblValue, aCalibComp[i].getMaturityDate().getJulian()}, new
						org.drip.math.grid.SegmentBuilderParams[] {sbp})) || !span.setup (1., new
							org.drip.math.spline.SegmentNodeWeightConstraint[] {snwc},
								org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL,
									org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN))
					return null;
			} else {
				span = org.drip.math.grid.SpanBuilder.AppendSegment (span,
					aCalibComp[i].getMaturityDate().getJulian(), snwc, sbp);

				if (null == span) return null;
			}
		}

		return span;
	}

	public LinearCurveCalibrator (
		final double dblTensionRegular,
		final double dblTensionTransition)
		throws java.lang.Exception
	{
		_sbpRegular = new org.drip.math.grid.SegmentBuilderParams
			(org.drip.math.grid.SpanBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION, new
				org.drip.math.spline.ExponentialTensionBasisSetParams (dblTensionRegular), new
					org.drip.math.spline.SegmentInelasticParams (2, 2, null), new
						org.drip.math.function.RationalShapeControl (0.));

		_sbpTransition = new org.drip.math.grid.SegmentBuilderParams
			(org.drip.math.grid.SpanBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION, new
				org.drip.math.spline.ExponentialTensionBasisSetParams (dblTensionTransition), new
					org.drip.math.spline.SegmentInelasticParams (2, 2, null), new
						org.drip.math.function.RationalShapeControl (0.));

		_sbpTransition = new org.drip.math.grid.SegmentBuilderParams
			(org.drip.math.grid.SpanBuilder.BASIS_SPLINE_POLYNOMIAL, new
				org.drip.math.spline.PolynomialBasisSetParams (4), new
					org.drip.math.spline.SegmentInelasticParams (2, 2, null), new
						org.drip.math.function.RationalShapeControl (dblTensionTransition));
	}

	public org.drip.math.grid.MultiSegmentSpan spanningMarkovDiscountCurve (
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return null;

		int iNumCalibComp = aCalibComp.length;
		org.drip.math.grid.MultiSegmentSpan span = null;

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return null;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return null;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return null;

			org.drip.math.spline.SegmentNodeWeightConstraint snwc = GenerateSegmentConstraint (prlc, span);

			if (null == snwc) return null;

			if (null == span) {
				if (null == (span = org.drip.math.grid.SpanBuilder.CreateUncalibratedSpanInterpolator (new
					double[] {valParams._dblValue, aCalibComp[i].getMaturityDate().getJulian()}, new
						org.drip.math.grid.SegmentBuilderParams[] {_sbpRegular})) || !span.setup (1., new
							org.drip.math.spline.SegmentNodeWeightConstraint[] {snwc},
								org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL,
									org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN))
					return null;
			} else {
				if (null == (span = org.drip.math.grid.SpanBuilder.AppendSegment (span,
					aCalibComp[i].getMaturityDate().getJulian(), snwc, _sbpRegular)))
					return null;
			}
		}

		return span;
	}
}
