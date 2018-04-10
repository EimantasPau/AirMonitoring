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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

class EnvironmentalCentreServant extends EnvironmentalCentrePOA {
    private EnvironmentalCentreServer parent;
    public ArrayList<CentreInfo> connectedCentres;
    ArrayList<Reading> centreReadings;
    private ORB orb;
    private NamingContextExt nameService;


    public EnvironmentalCentreServant(EnvironmentalCentreServer parentGUI, ORB orb_val) {
        this.parent = parentGUI;
        connectedCentres = new ArrayList<>();
        centreReadings = new ArrayList<>();
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
    public Reading[] readings() {
        ArrayList<Reading> readings = new ArrayList<>();
        centreReadings.clear();
       for(int i = 0; i < connectedCentres.size(); i++){
           String centreName = connectedCentres.get(i).centre_name;
           try {
               RegionalCentre centreServant = RegionalCentreHelper.narrow(nameService.resolve_str(centreName));
               ArrayList<Reading> stationReadings = new ArrayList<>(Arrays.asList(centreServant.allReadings()));
               //check if we have any new readings
               if(stationReadings.size() != 0) {

                   Iterator<Reading> pulledIterator = stationReadings.iterator();

                   while(pulledIterator.hasNext()) {
                       Reading pulled_reading = pulledIterator.next();
                       if(!listContains(centreReadings, pulled_reading)) {
                           centreReadings.add(pulled_reading);
                       }
                   }
               }
           } catch (NotFound notFound) {
               notFound.printStackTrace();
           } catch (CannotProceed cannotProceed) {
               cannotProceed.printStackTrace();
           } catch (InvalidName invalidName) {
               invalidName.printStackTrace();
           }
       }
        centreReadings.addAll(readings);

        return centreReadings.toArray(new Reading[0]);
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
        parent.addToCentreList(info);
        System.out.println("New centre added. " + info.centre_name);
    }

    public boolean listContains(ArrayList<Reading> list, Reading r) {
        for(Reading reading: list){
            if((reading.reading_value == r.reading_value) &&
                    (reading.station_name.equals(r.station_name)) &&
                    (reading.date == r.date) &&
                    (reading.time == r.time)){
                return true;
            }
        }
        return false;
    }
}


public class EnvironmentalCentreServer extends JFrame {
    NamingContextExt nameService;
    EnvironmentalCentreServant servant;
    EnvironmentalCentreServer parentGUI;

    //main panel
    private JPanel panel;

    private JPanel centresPanel;
    private JPanel stationsPanel;
    private JPanel readingsPanel;
    private JButton getCentreStations;
    private JButton getStationReadings;
    private JButton getAllReadings;
    private String centreName;
    private JList<CentreInfo> centreList;
    private JList<StationInfo> stationList;
    private JList<Reading> readingsList;
    DefaultListModel<CentreInfo> centreListModel;
    DefaultListModel<StationInfo> stationListModel;
    DefaultListModel<Reading> readingsListModel;
    public EnvironmentalCentreServer(String[] args) {
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
            servant = new EnvironmentalCentreServant(this, orb);

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
            //main panel
            panel = new JPanel();
            centresPanel = new JPanel();
            stationsPanel = new JPanel();
            readingsPanel = new JPanel();

            //centre panel
            centresPanel.setLayout(new BoxLayout(centresPanel, BoxLayout.PAGE_AXIS));
            centresPanel.setPreferredSize(new Dimension(250, 200));


            centreListModel = new DefaultListModel<>();
            centreList = new JList<>(centreListModel);
            centreList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            centreList.setVisibleRowCount(-1);
            CentreListCellRenderer centreRenderer = new CentreListCellRenderer();
            centreList.setCellRenderer(centreRenderer);
            JScrollPane centreListScroller = new JScrollPane(centreList);
            centreListScroller.setPreferredSize(new Dimension(250, 80));
            JLabel centreListTitle = new JLabel("Connected centres");
            getCentreStations = new JButton("Get stations at centre");

            centresPanel.add(centreListTitle);
            centresPanel.add(centreListScroller);
            centresPanel.add(getCentreStations);

            //station panel
            stationsPanel.setLayout(new BoxLayout(stationsPanel, BoxLayout.PAGE_AXIS));
            stationsPanel.setPreferredSize(new Dimension(250, 200));

            stationListModel = new DefaultListModel<>();
            stationList = new JList<>(stationListModel);
            stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            stationList.setVisibleRowCount(-1);
            StationListCellRenderer stationRenderer = new StationListCellRenderer();
            stationList.setCellRenderer(stationRenderer);
            JScrollPane stationListScroller = new JScrollPane(stationList);
            stationListScroller.setPreferredSize(new Dimension(250, 80));
            JLabel stationListTitle = new JLabel("Stations for the centre");
            getStationReadings = new JButton("Get station readings");
            getAllReadings = new JButton("Get all readings");
            stationsPanel.add(stationListTitle);
            stationsPanel.add(stationListScroller);
            stationsPanel.add(getStationReadings);
            stationsPanel.add(getAllReadings);

            //readings panel
            readingsPanel.setLayout(new BoxLayout(readingsPanel, BoxLayout.PAGE_AXIS));
            readingsPanel.setPreferredSize(new Dimension(250, 200));

            readingsListModel = new DefaultListModel<>();
            readingsList = new JList<>(readingsListModel);
            readingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            readingsList.setVisibleRowCount(-1);
            ReadingsListCellRenderer readingsRenderer = new ReadingsListCellRenderer();
            readingsList.setCellRenderer(readingsRenderer);
            JScrollPane readingsListScroller = new JScrollPane(readingsList);
            readingsListScroller.setPreferredSize(new Dimension(250, 80));
            JLabel readingsListTitle = new JLabel("Readings");

            readingsPanel.add(readingsListTitle);
            readingsPanel.add(readingsListScroller);




            panel.add(centresPanel);
            panel.add(stationsPanel);
            panel.add(readingsPanel);
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
        getCentreStations.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stationListModel.clear();
                CentreInfo selectedCentre = centreList.getSelectedValue();
                try {
                    RegionalCentre centreServant = RegionalCentreHelper.narrow(nameService.resolve_str(selectedCentre.centre_name));
                    StationInfo[] stationList = centreServant.connectedDevices();
                    for(int i = 0; i < stationList.length; i++) {
                        stationListModel.addElement(stationList[i]);
                    }

                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                }
            }
        });

        getStationReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readingsListModel.clear();
                try {
                    RegionalCentre regionalCentreServant = RegionalCentreHelper.narrow(nameService.resolve_str(centreList.getSelectedValue().centre_name));
                    Reading[] stationReadings = regionalCentreServant.readings(stationList.getSelectedValue().station_name);
                    for(int i = 0; i < stationReadings.length; i++) {
                       readingsListModel.addElement(stationReadings[i]);
                    }
                } catch (NotFound notFound) {
                    notFound.printStackTrace();
                } catch (CannotProceed cannotProceed) {
                    cannotProceed.printStackTrace();
                } catch (InvalidName invalidName) {
                    invalidName.printStackTrace();
                }

            }
        });

        getAllReadings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readingsListModel.clear();
                Reading[] readings = servant.readings();
                for(int i = 0; i < readings.length; i++) {
                    readingsListModel.addElement(readings[i]);
                }
            }
        });


    }
    public void addToCentreList(CentreInfo info) {
        centreListModel.addElement(info);
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

class CentreListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof CentreInfo) {
            CentreInfo centre = (CentreInfo)value;
            setText(centre.centre_name);
        }
        return this;
    }
}


class StationListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof StationInfo) {
            StationInfo station = (StationInfo)value;
            setText(station.station_name);
        }
        return this;
    }
}

class ReadingsListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Reading) {
            Reading reading = (Reading)value;
            setText(reading.station_name + " - " + reading.reading_value + " - " + reading.date + "/" + reading.time);
        }
        return this;
    }
}