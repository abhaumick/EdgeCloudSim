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

import edu.boun.edgecloudsim.edge_client.Task;

public class MetricMobility extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private NodeList datacenterList;
	private ExponentialDistribution[] expRngList;

	public MetricMobility( int _numberOfMobileDevices, double _simulationTime ) {
		super( _numberOfMobileDevices, _simulationTime );
	}


	@Override
	public void initialize() {
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		
		expRngList = new ExponentialDistribution[SimSettings.getInstance().getNumOfEdgeDatacenters()];

		//create random number generator for each place
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		datacenterList = doc.getElementsByTagName("datacenter");
		for (int i = 0; i < datacenterList.getLength(); i++) {
			Node datacenterNode = datacenterList.item(i);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			int placeTypeIndex = Integer.parseInt(attractiveness);
			
			expRngList[i] = new ExponentialDistribution(SimSettings.getInstance().getMobilityLookUpTable()[placeTypeIndex]);
		}
		
		//initialize tree maps and position of mobile devices
		for(int i=0; i<numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());
			
			int randDatacenterId = SimUtils.getRandomNumber(0, SimSettings.getInstance().getNumOfEdgeDatacenters()-1);
			Node datacenterNode = datacenterList.item(randDatacenterId);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			int placeTypeIndex = Integer.parseInt(attractiveness);
			int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
			int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
			int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());

			//start locating user shortly after the simulation started (e.g. 10 seconds)
			treeMapArray.get(i).put(SimSettings.CLIENT_ACTIVITY_START_TIME, new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
		}

		/* Removed logic for updating tree map for random changes in location after waiting time */

	}

	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);

		Entry<Double, Location> e = treeMap.floorEntry(time);

		if (e == null) {
			SimLogger.printLine(
					"impossible is occured! no location is found for the device '" + deviceId + "' at " + time);
			System.exit(0);
		}

		return e.getValue();
	}


	public double updateMobileDeviceLocation( int mobileDeviceId, double time ) {
		
		//System.out.println( String.format( "MetricMobility : Update for mobile # %d @ time %f", mobileDeviceId, time ) );
		Location currentLocation = getLocation(mobileDeviceId, time);
		//System.out.print( String.format( "\t\t From : %d - %d - %d - %d ", currentLocation.getServingWlanId(),
		//	currentLocation.getPlaceTypeIndex(), currentLocation.getXPos(), currentLocation.getYPos() ) );
		//	Select new edge datacenter node - by metric
		int newDatacenterId = ( currentLocation.getServingWlanId() + 1 ) % (SimSettings.getInstance().getNumOfEdgeDatacenters());
		//	Max = SimSettings.getInstance().getNumOfEdgeDatacenters()-1


		//	Update treeMap
		Node datacenterNode = datacenterList.item(newDatacenterId);
		Element datacenterElement = (Element) datacenterNode;
		Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
		String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
		int placeTypeIndex = Integer.parseInt(attractiveness);
		int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
		int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
		int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
		
		TreeMap<Double, Location> treeMap = treeMapArray.get(mobileDeviceId);
		treeMap.put(time, new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
		
		//	Calculate time at next edge node
		Location finalLocation = getLocation(mobileDeviceId, time);

		//System.out.print( String.format( "\t To : %d - %d - %d - %d ", finalLocation.getServingWlanId(),
		//	finalLocation.getPlaceTypeIndex(), finalLocation.getXPos(), finalLocation.getYPos() ) );
		
		double waitingTime = expRngList[finalLocation.getServingWlanId()].sample();
		//System.out.println( String.format( " For %f", waitingTime ) );
				
		return waitingTime;
	}

	public double getWaitTime( int mobileDeviceId, double time ) {

		TreeMap<Double, Location> treeMap = treeMapArray.get(mobileDeviceId);
		
		Location currentLocation = getLocation(mobileDeviceId, time);
		double waitingTime = expRngList[currentLocation.getServingWlanId()].sample();

		return waitingTime;
		
	}

}