package org.spin.gaitlib.test;

import junit.framework.TestCase;

import org.spin.gaitlib.cadence.CadenceState;
import org.spin.gaitlib.filter.CadenceConfidenceFilter;
import org.spin.gaitlib.filter.MeanFilter;
import org.spin.gaitlib.filter.MedianFilter;

public class FiltersTest extends TestCase {

	private CadenceConfidenceFilter cadenceConfidenceFilter;
	private MedianFilter medianFilter;
	private MeanFilter meanFilter;

	public FiltersTest(String name) {
		super(name);
		cadenceConfidenceFilter = new CadenceConfidenceFilter();
		medianFilter = new MedianFilter();
		meanFilter = new MeanFilter();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetMostConfidentCadence() {
		CadenceState state1 = new CadenceState(1, 1, 1.0f,
				System.currentTimeMillis());
		CadenceState state2 = new CadenceState(1, 1, 0.5f,
				System.currentTimeMillis());
		CadenceState state3 = new CadenceState(1, 1, 0.1f,
				System.currentTimeMillis());
		CadenceState[] input = { state1, state2, state3 };
		CadenceState actual = cadenceConfidenceFilter
				.getMostConfidentCadence(input);
		assertEquals(state1, actual);
	}

	public void testGetMostConfidentCadenceWithTies() {
		try {
			CadenceState state1 = new CadenceState(1, 1, 0.9f,
					System.currentTimeMillis());
			Thread.sleep(1);
			CadenceState state2 = new CadenceState(1, 1, 0.5f,
					System.currentTimeMillis());
			Thread.sleep(1);
			CadenceState state3 = new CadenceState(1, 1, 0.1f,
					System.currentTimeMillis());
			Thread.sleep(1);
			CadenceState state4 = new CadenceState(1, 1, 0.9f,
					System.currentTimeMillis());
			CadenceState[] input = { state1, state2, state3, state4 };
			CadenceState actual = cadenceConfidenceFilter
					.getMostConfidentCadence(input);
			assertEquals(state4, actual);
		} catch (InterruptedException e) {
			fail(e.toString());
		}
	}

	public void testMeanFilter() {
		float[] input = { 0.1f, 0.2f, 0.8f, 0.9f };
		float actual = meanFilter.getFilteredValue(input);
		assertEquals(0.5f, actual);
	}

	public void testMedianFilterOdd() {
		float[] input = { 0.1f, 0.2f, 0.8f };
		float actual = medianFilter.getFilteredValue(input);
		assertEquals(0.2f, actual);
	}

	public void testMedianFilterEven() {
		float[] input = { 0.1f, 0.2f, 0.8f, 0.9f };
		float actual = medianFilter.getFilteredValue(input);
		assertEquals(0.5f, actual);
	}

}
