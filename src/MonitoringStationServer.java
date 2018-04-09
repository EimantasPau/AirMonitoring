import AirMonitoring.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


class MonitoringStationServant extends MonitoringStationPOA {
    public StationInfo info;
    private MonitoringStationServer parent;
    ArrayList<Reading> reading_log;
    Timer timer;

    public MonitoringStationServant(MonitoringStationServer parentGUI) {
        parent = parentGUI;
        reading_log = new ArrayList<>();
    }

    @Override
    public Reading reading() {
        int readingValue = parent.getReadingValue();
        String station_name = getInfo().station_name;
        int time = (int)Math.floor(Math.random() *100);
        int date = (int)Math.floor(Math.random() *100);

        return new Reading(time, date, station_name, readingValue);
    }

    @Override
    public Reading[] reading_log() {
        return reading_log.toArray(new Reading[0]);
    }

    @Override
    public void take_reading() {
        reading_log.add(reading());
    }

    @Override
    public void setInfo(StationInfo info) {
        this.info = info;
    }

    @Override
    public StationInfo getInfo() {
        return info;
    }

    @Override
    public void turn_on() {
        timer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                take_reading();
                parent.populateLog();
            }
        });
        timer.start(); // Go go go!
    }

    @Override
    public void turn_off() {
        timer.stop();
    }

    @Override
    public void reset() {
        parent.clearLog();
        reading_log.clear();
    }
}

public class MonitoringStationServer extends JFrame {
    //GUI
    private JPanel panel;
    private JScrollPane scrollpane;
    private JTextArea textarea;
    private JButton takeReadingBtn;
//    private JTextField readingValue;
    private JButton turnOnBtn;
    private JButton turnOffBtn;
    private JButton resetBtn;
    private JSlider readingValue;

    //program arguments
    private String name;
    private String location;
    private String regionalStation;

    //server servant
    private MonitoringStationServant servant;

    public MonitoringStationServer(String[] args){

        if(args.length < 3) {
            System.out.println("Please provide all of the arguments to start the station.");
            return;
        }
        //get program args
        name = args[0];
        location = args[1];
        regionalStation = args[2];

        if(name == null || location == null || regionalStation == null) {
            System.out.print("Station information invalid");
            return;
        }
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

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
            //Check if an object with that name already exists in the naming service

            // create servant
            servant = new MonitoringStationServant(this);


            // get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            MonitoringStation cref = MonitoringStationHelper.narrow(ref);
            StationInfo newStationInfo = new StationInfo(name, location, "test");
            servant.setInfo(newStationInfo);

            // bind the station object in the Naming service
            NameComponent[] sName = nameService.to_name(name);
            nameService.rebind(sName, cref);

            RegionalCentre regionalServant = RegionalCentreHelper.narrow(nameService.resolve_str(regionalStation));
            regionalServant.registerMonitoringStation(newStationInfo);


            initGUI();
            initListeners();
            // remove the "orb.run()" command,
            // or the server will run but the GUI will not be visible
            // orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

    }

    public static void main(String args[]) {
        final String[] arguments = args;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MonitoringStationServer(arguments).setVisible(true);
            }
        });
    }

    public int getReadingValue() {
        return readingValue.getValue();
    }

    public void initGUI() {
        // set up the GUI
        textarea = new JTextArea(20,25);
        scrollpane = new JScrollPane(textarea);
        takeReadingBtn = new JButton("Take reading");
        turnOnBtn = new JButton("Turn on");
        turnOffBtn = new JButton("Turn off");
        resetBtn = new JButton("Reset");
        readingValue = new JSlider(0, 50);
        readingValue.setMajorTickSpacing(10);
        readingValue.setMinorTickSpacing(1);
        readingValue.setPaintTicks(true);
        readingValue.setPaintLabels(true);
//        readingValue = new JTextField("", 20);
        panel = new JPanel();
        panel.add(readingValue);
        panel.add(takeReadingBtn);
        panel.add(turnOnBtn);
        panel.add(turnOffBtn);
        panel.add(resetBtn);
        panel.add(scrollpane);
        getContentPane().add(panel, "Center");

        setSize(400, 500);
        setTitle(servant.getInfo().station_name + " - " + servant.getInfo().location);
        // wait for invocations from clients
        textarea.append("Server started.  Waiting for clients...\n\n");
    }

    public void initListeners() {
        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                System.exit(0);
            }
        } );

        takeReadingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                servant.take_reading();
                populateLog();
            }
        });

        turnOnBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                servant.turn_on();
            }
        });

        turnOffBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                servant.turn_off();
            }
        });

        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                servant.reset();
            }
        });
    }

    public void populateLog() {
        clearLog();
        for(Reading reading: servant.reading_log()){
            textarea.append("Reading taken! Reading value is " + reading.reading_value + "\n");
        }
    }

    public void clearLog() {
        textarea.setText("");
    }
}

