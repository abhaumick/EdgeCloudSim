/*
 * Title:        EdgeCloudSim - Nomadic Mobility model implementation
 * 
 * Description: 
 * MobilityModel implements basic nomadic mobility model where the
 * place of the devices are changed from time to time instead of a
 * continuous location update.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.sample_app5;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class SampleMobilityModel extends MobilityModel {
	private List<TreeMap<Double, Location>> treeMapArray;
	private ExponentialDistribution[] expRngList;
	private NodeList datacenterList;
	
	public SampleMobilityModel(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initialize() {
		System.out.println("SampleMobilityModelInitiLIZED----");
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
		

	}

	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
		
		Entry<Double, Location> e = treeMap.floorEntry(time);
	    
	    if(e == null){
	    	SimLogger.printLine("impossible is occured! no location is found for the device '" + deviceId + "' at " + time);
	    	System.exit(0);
	    }
	    
		return e.getValue();
	}

	@Override
	public double updateMobileDeviceLocation(int mobileDeviceId, double time) {
			double waitingTime =0;
			//System.out.println( String.format( "MetricMobility : Update for mobile # %d @ time %f", mobileDeviceId, time ) );
			Location currentLocation = getLocation(mobileDeviceId, time);
			//System.out.print( String.format( "\t\t From : %d - %d - %d - %d ", currentLocation.getServingWlanId(),
			//	currentLocation.getPlaceTypeIndex(), currentLocation.getXPos(), currentLocation.getYPos() ) );
			//	Select new edge datacenter node - by metric
			int[] wlanClients = SimManager.getInstance().getNetworkModel().getWlanClients();
			int nextBestDatacenter=Integer.MAX_VALUE;
			for(int client=0; client<wlanClients.length; client++) {
				if(wlanClients[client]<nextBestDatacenter && client!=currentLocation.getServingWlanId()) {
					nextBestDatacenter=client;
				}
			}
			//	Max = SimSettings.getInstance().getNumOfEdgeDatacenters()-1
			int newDatacenterId = nextBestDatacenter;

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
			
			waitingTime = expRngList[finalLocation.getServingWlanId()].sample();
			//System.out.println( String.format( " For %f", waitingTime ) );
					
			return waitingTime;

	}


	@Override
	public double getWaitTime(int mobileDeviceId, double time) {
		return 0;
	}


}
