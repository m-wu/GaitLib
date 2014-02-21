package org.spin.gaitlib.test;

import junit.framework.TestCase;

import org.spin.gaitlib.cadence.CadenceDetector;
import org.spin.gaitlib.cadence.DefaultCadenceDetector;
import org.spin.gaitlib.filter.FilterNotSetException;
import org.spin.gaitlib.filter.MedianFilter;

public class CadenceDetectorTest extends TestCase {

	private final CadenceDetector cadenceDetector;

	public CadenceDetectorTest(String name) {
		super(name);
		cadenceDetector = new DefaultCadenceDetector();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSetFilterFilterChoice() {
		assertNull(cadenceDetector.getFilter());
		cadenceDetector.setFilter(new MedianFilter());
		assertNotNull(cadenceDetector.getFilter());
		cadenceDetector.setFilter(null);
		assertNull(cadenceDetector.getFilter());
	}

	public void testGetCadence() {
		try {
			cadenceDetector.getCadence(true);
			fail("FilterNotSetException not thrown.");
		} catch (FilterNotSetException e) {

		}

		try {
			cadenceDetector.getCadence(false);
		} catch (FilterNotSetException e) {
			fail("FilterNotSetException thrown incorrectly.");
		}

		cadenceDetector.setFilter(new MedianFilter());
		try {
			cadenceDetector.getCadence(true);
		} catch (FilterNotSetException e) {
			fail("FilterNotSetException thrown incorrectly.");
		}
		cadenceDetector.setFilter(null);
	}

	public void testGetStrideLength() {
		try {
			cadenceDetector.getStrideLength(true);
			fail("FilterNotSetException not thrown.");
		} catch (FilterNotSetException e) {

		}

		try {
			cadenceDetector.getStrideLength(false);
		} catch (FilterNotSetException e) {
			fail("FilterNotSetException thrown incorrectly.");
		}

		cadenceDetector.setFilter(new MedianFilter());
		try {
			cadenceDetector.getStrideLength(true);
		} catch (FilterNotSetException e) {
			fail("FilterNotSetException thrown incorrectly.");
		}
		cadenceDetector.setFilter(null);
	}

	public void testGetSpeed() {

	}

	public void testGetCadenceConfidence() {
	}

	public void testGetCurrentCadenceState() {
		assertNotNull(cadenceDetector.getCurrentCadenceState());
	}

}
