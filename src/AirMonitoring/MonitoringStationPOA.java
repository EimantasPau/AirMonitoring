package AirMonitoring;


/**
* AirMonitoring/MonitoringStationPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Sunday, 8 April 2018 17:51:22 o'clock BST
*/

public abstract class MonitoringStationPOA extends org.omg.PortableServer.Servant
 implements AirMonitoring.MonitoringStationOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("reading", new java.lang.Integer (0));
    _methods.put ("_get_reading_log", new java.lang.Integer (1));
    _methods.put ("take_reading", new java.lang.Integer (2));
    _methods.put ("setInfo", new java.lang.Integer (3));
    _methods.put ("getInfo", new java.lang.Integer (4));
    _methods.put ("turn_on", new java.lang.Integer (5));
    _methods.put ("turn_off", new java.lang.Integer (6));
    _methods.put ("reset", new java.lang.Integer (7));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // AirMonitoring/MonitoringStation/reading
       {
         AirMonitoring.Reading $result = null;
         $result = this.reading ();
         out = $rh.createReply();
         AirMonitoring.ReadingHelper.write (out, $result);
         break;
       }

       case 1:  // AirMonitoring/MonitoringStation/_get_reading_log
       {
         AirMonitoring.Reading $result[] = null;
         $result = this.reading_log ();
         out = $rh.createReply();
         AirMonitoring.readingsHelper.write (out, $result);
         break;
       }

       case 2:  // AirMonitoring/MonitoringStation/take_reading
       {
         this.take_reading ();
         out = $rh.createReply();
         break;
       }

       case 3:  // AirMonitoring/MonitoringStation/setInfo
       {
         AirMonitoring.StationInfo info = AirMonitoring.StationInfoHelper.read (in);
         this.setInfo (info);
         out = $rh.createReply();
         break;
       }

       case 4:  // AirMonitoring/MonitoringStation/getInfo
       {
         AirMonitoring.StationInfo $result = null;
         $result = this.getInfo ();
         out = $rh.createReply();
         AirMonitoring.StationInfoHelper.write (out, $result);
         break;
       }

       case 5:  // AirMonitoring/MonitoringStation/turn_on
       {
         this.turn_on ();
         out = $rh.createReply();
         break;
       }

       case 6:  // AirMonitoring/MonitoringStation/turn_off
       {
         this.turn_off ();
         out = $rh.createReply();
         break;
       }

       case 7:  // AirMonitoring/MonitoringStation/reset
       {
         this.reset ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:AirMonitoring/MonitoringStation:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public MonitoringStation _this() 
  {
    return MonitoringStationHelper.narrow(
    super._this_object());
  }

  public MonitoringStation _this(org.omg.CORBA.ORB orb) 
  {
    return MonitoringStationHelper.narrow(
    super._this_object(orb));
  }


} // class MonitoringStationPOA