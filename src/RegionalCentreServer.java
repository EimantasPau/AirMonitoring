import AirMonitoring.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


class RegionalCentreServant extends RegionalCentrePOA {
    ArrayList<StationInfo> connectedDevices;
    ArrayList<Reading> station_readings;
    private CentreInfo info;
    private ORB orb;
    private NamingContextExt nameService;
    public RegionalCentreServant(ORB orb_val) {
        connectedDevices = new ArrayList<>();
        station_readings = new ArrayList<>();
        try {
            orb = orb_val;
            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references ("NameService");
            if (nameServiceObj == null) {
                System.out.println("nameServiceObj = null");
                return;
            }

            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            nameService = NamingContextExtHelper.narrow(nameServiceObj);
            if (nameService == null) {
                System.out.println("nameService = null");
                return;
            }

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }


    @Override
    public StationInfo[] connectedDevices() {
        return connectedDevices.toArray(new StationInfo[0]);
    }

    @Override
    public void connectedDevices(StationInfo[] newConnectedDevices) {

    }

    @Override
    public CentreInfo centreInfo() {
        return info;
    }

    @Override
    public void setInfo(CentreInfo info) {
        this.info = info;
    }

    //returns an array of centreReadings from all stations connected to this centre
    public Reading[] readings() {
        //loop through all connected stations
        for(StationInfo station : connectedDevices) {
            String station_name = station.station_name;
            try {
                //find the reference to the station
                MonitoringStation monitoringStation = MonitoringStationHelper.narrow(nameService.resolve_str(station_name));


                ArrayList<Reading> pulled_readings = new ArrayList<>(Arrays.asList(monitoringStation.reading_log()));
                //check if we have any new readings
                if(pulled_readings.size() != 0) {
                    //check if this centre has any readings at all
                    int initialSize = station_readings.size();
                    if(initialSize > 0){
                        for (int j = 0; j <= initialSize - 1; j++) {
                            Reading station_reading = station_readings.get(j);

                            //if it does compare the held readings with the new ones to avoid duplicates
                            int pulledInitialSize = pulled_readings.size();
                            for (int i = 0; i <= pulledInitialSize - 1; i++) {
                                Reading pulled_reading = pulled_readings.get(i);
                                if((station_reading.reading_value == pulled_reading.reading_value) &&
                                        (station_reading.station_name.equals(pulled_reading.station_name)) &&
                                        (station_reading.date == pulled_reading.date) &&
                                        (station_reading.time == pulled_reading.time)) {
                                    pulled_readings.remove(pulled_reading);
                                    break;
                                }
                                pulled_readings.remove(pulled_reading);
                                station_readings.add(pulled_reading);
                            }
                        }

                    } else {
                        //if the station_reading is of length, we can safely add all of the pulled in readings
                        station_readings.addAll(pulled_readings);
                    }

                }
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return station_readings.toArray(new Reading[0]);
    }

    @Override
    public Reading[] alarms() {
        return new Reading[0];
    }

    @Override
    public void registerMonitoringStation(StationInfo info) {
        connectedDevices.add(info);
        System.out.println("New station added." + info.station_name);
    }
}

public class RegionalCentreServer {
    //program arguments
    private String centreName;
    private String location;
    private String environmentalCentre;

    public RegionalCentreServer(String[] args){


        if(args.length < 3) {
            System.out.println("Please provide all of the arguments to start the station.");
            return;
        }
        //get program args
        centreName = args[0];
        location = args[1];
        environmentalCentre = args[2];
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            RegionalCentreServant servant = new RegionalCentreServant(orb);

            // get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            RegionalCentre cref = RegionalCentreHelper.narrow(ref);

            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references ("NameService");
            if (nameServiceObj == null) {
                System.out.println("nameServiceObj = null");
                return;
            }

            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);
            if (nameService == null) {
                System.out.println("nameService = null");
                return;
            }

            // bind the centre object in the Naming service
            NameComponent[] sName = nameService.to_name(centreName);
            nameService.rebind(sName, cref);

            //register with the environmental centre
            EnvironmentalCentre environmentalServant = EnvironmentalCentreHelper.narrow(nameService.resolve_str(environmentalCentre));
            CentreInfo newCentre = new CentreInfo(centreName, location, "someid");
            environmentalServant.registerRegionalCentre(newCentre);

            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

    }
    public static void main(String args[]) {
        new RegionalCentreServer(args);
    }
}


