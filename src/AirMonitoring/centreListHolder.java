package AirMonitoring;


/**
* AirMonitoring/centreListHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 17 April 2018 11:24:36 o'clock BST
*/

public final class centreListHolder implements org.omg.CORBA.portable.Streamable
{
  public AirMonitoring.CentreInfo value[] = null;

  public centreListHolder ()
  {
  }

  public centreListHolder (AirMonitoring.CentreInfo[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AirMonitoring.centreListHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AirMonitoring.centreListHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AirMonitoring.centreListHelper.type ();
  }

}
