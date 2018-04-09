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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

class EnvironmentalCentreServant extends EnvironmentalCentrePOA {
    public ArrayList<CentreInfo> connectedCentres;
    ArrayList<Reading> pulled_readings;

    public EnvironmentalCentreServant() {
        connectedCentres = new ArrayList<>();
        pulled_readings = new ArrayList<>();
    }

    @Override
    public Reading[] readings() {
        return pulled_readings.toArray(new Reading[0]);
    }

    @Override
    public CentreInfo[] connectedCentres() {
        return connectedCentres.toArray(new CentreInfo[0]);
    }

    @Override
    public void connectedCentres(CentreInfo[] newConnectedCentres) {

    }

    @Override
    public void registerRegionalCentre(CentreInfo info) {
        connectedCentres.add(info);
        System.out.println("New centre added. " + info.centre_name);
    }
}


public class EnvironmentalCentreServer extends JFrame {
    NamingContextExt nameService;
    EnvironmentalCentreServant servant;
    ArrayList<Reading> centreReadings;
    private JPanel panel;
    private JButton getConnectedCentres;
    private JButton getReadings;
    private String centreName;
    public EnvironmentalCentreServer(String[] args) {
        //EnglandEnvironmentalCentre
        centreReadings = new ArrayList<>();
        centreName = args[0];
        try {
            if(centreName == null) {
                System.out.print("Station information invalid");
                return;
            }
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            servant = new EnvironmentalCentreServant();

            // get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            EnvironmentalCentre cref = EnvironmentalCentreHelper.narrow(ref);

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

            // bind the Count object in the Naming service
            NameComponent[] sName = nameService.to_name(centreName);
            nameService.rebind(sName, cref);


            //GUI
            panel = new JPanel();
            getConnectedCentres = new JButton("Get centre list");
            getReadings = new JButton("Get all centreReadings");
            panel.add(getConnectedCentres);
            panel.add(getReadings);
            getContentPane().add(panel, "Center");
            initListeners();
            setSize(400, 500);

            addWindowListener (new java.awt.event.WindowAdapter () {
                public void windowClosing (java.awt.event.WindowEvent evt) {
                    System.exit(0);
                }
            } );

            //orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }

    public void initListeners() {
        getConnectedCentres.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(servant.connectedCentres().length > 0) {
                    for(int i = 0; i<servant.connectedCentres().length; i++) {
                        System.out.println(servant.connectedCentres()[i].centre_name);
                    }
                }
            }
        });

        getReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(servant.connectedCentres().length > 0) {
                    for(int x = 0; x<servant.connectedCentres().length; x++) {
                        String centre_name = servant.connectedCentres()[x].centre_name;
                        try {

                            RegionalCentre regional_servant = RegionalCentreHelper.narrow(nameService.resolve_str(centre_name));

                            ArrayList<Reading> pulled_readings = new ArrayList<>(Arrays.asList(regional_servant.readings()));

                            //check if we have any new readings
                            if(pulled_readings.size() != 0) {
                                //check if this centre has any readings at all
                                int initialSize = centreReadings.size();
                                if (initialSize > 0) {
                                    for (int j = 0; j <= initialSize - 1; j++) {
                                        Reading station_reading = centreReadings.get(j);

                                        //if it does compare the held readings with the new ones to avoid duplicates
                                        int pulledInitialSize = pulled_readings.size();
                                        for (int i = 0; i <= pulledInitialSize - 1; i++) {
                                            Reading pulled_reading = pulled_readings.get(i);
                                            if ((station_reading.reading_value == pulled_reading.reading_value) &&
                                                    (station_reading.station_name.equals(pulled_reading.station_name)) &&
                                                    (station_reading.date == pulled_reading.date) &&
                                                    (station_reading.time == pulled_reading.time)) {
                                                pulled_readings.remove(pulled_reading);
                                                break;
                                            }
                                            pulled_readings.remove(pulled_reading);
                                            centreReadings.add(pulled_reading);
                                        }
                                    }

                                } else {
                                    //if the station_reading is of length, we can safely add all of the pulled in readings
                                    centreReadings.addAll(pulled_readings);
                                }
                            }
//                            System.out.println("Readings from regional centre:" + regional_servant.centreInfo().centre_name + "/ Stations connected: " + centreReadings.size());
                            for(Reading reading : centreReadings) {
                                System.out.println(reading.station_name);
                                System.out.println(reading.reading_value);
                                System.out.println(reading.date);
                                System.out.println(reading.time);
                                System.out.println("-------------------------------");

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
                }
            }
        });
    }

    public static void main(String args[]) {
        final String[] arguments = args;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EnvironmentalCentreServer(arguments).setVisible(true);
            }
        });
    }
}
