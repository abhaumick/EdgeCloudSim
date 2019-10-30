/**
 * 
 */


package edu.boun.edgecloudsim.mobility;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.mobility.NomadicMobility;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class MetricMobility extends NomadicMobility {

	public MetricMobility( int _numberOfMobileDevices, double _simulationTime ) {
		super( _numberOfMobileDevices, _simulationTime );
	}

	public void updateMobileDeviceLocation() {
		System.out.println( "MetrciMobility : UpdateMobileLocation" );
	}

}