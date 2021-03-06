package AirMonitoring;


/**
* AirMonitoring/centreListHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 17 April 2018 11:24:36 o'clock BST
*/

abstract public class centreListHelper
{
  private static String  _id = "IDL:AirMonitoring/centreList:1.0";

  public static void insert (org.omg.CORBA.Any a, AirMonitoring.CentreInfo[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static AirMonitoring.CentreInfo[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = AirMonitoring.CentreInfoHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (AirMonitoring.centreListHelper.id (), "centreList", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static AirMonitoring.CentreInfo[] read (org.omg.CORBA.portable.InputStream istream)
  {
    AirMonitoring.CentreInfo value[] = null;
    int _len0 = istream.read_long ();
    value = new AirMonitoring.CentreInfo[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = AirMonitoring.CentreInfoHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, AirMonitoring.CentreInfo[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      AirMonitoring.CentreInfoHelper.write (ostream, value[_i0]);
  }

}
