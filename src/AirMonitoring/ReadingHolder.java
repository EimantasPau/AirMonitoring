package AirMonitoring;

/**
* AirMonitoring/ReadingHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AirMonitoring.idl
* Tuesday, 17 April 2018 11:24:36 o'clock BST
*/

public final class ReadingHolder implements org.omg.CORBA.portable.Streamable
{
  public AirMonitoring.Reading value = null;

  public ReadingHolder ()
  {
  }

  public ReadingHolder (AirMonitoring.Reading initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AirMonitoring.ReadingHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AirMonitoring.ReadingHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AirMonitoring.ReadingHelper.type ();
  }

}
