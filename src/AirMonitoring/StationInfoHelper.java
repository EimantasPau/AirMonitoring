package AirMonitoring;


/**
* AirMonitoring/StationInfoHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Sunday, 8 April 2018 17:51:22 o'clock BST
*/

abstract public class StationInfoHelper
{
  private static String  _id = "IDL:AirMonitoring/StationInfo:1.0";

  public static void insert (org.omg.CORBA.Any a, AirMonitoring.StationInfo that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static AirMonitoring.StationInfo extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [3];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "station_name",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "location",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[2] = new org.omg.CORBA.StructMember (
            "IOR",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (AirMonitoring.StationInfoHelper.id (), "StationInfo", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static AirMonitoring.StationInfo read (org.omg.CORBA.portable.InputStream istream)
  {
    AirMonitoring.StationInfo value = new AirMonitoring.StationInfo ();
    value.station_name = istream.read_string ();
    value.location = istream.read_string ();
    value.IOR = istream.read_string ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, AirMonitoring.StationInfo value)
  {
    ostream.write_string (value.station_name);
    ostream.write_string (value.location);
    ostream.write_string (value.IOR);
  }

}
