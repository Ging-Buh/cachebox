/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\@Work\\Longri\\Gits\\cachebox\\Android-Launcher\\src\\de\\CB_PlugIn\\IPlugIn.aidl
 */
package de.CB_PlugIn;
public interface IPlugIn extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements de.CB_PlugIn.IPlugIn
{
private static final java.lang.String DESCRIPTOR = "de.CB_PlugIn.IPlugIn";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an de.CB_PlugIn.IPlugIn interface,
 * generating a proxy if needed.
 */
public static de.CB_PlugIn.IPlugIn asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof de.CB_PlugIn.IPlugIn))) {
return ((de.CB_PlugIn.IPlugIn)iin);
}
return new de.CB_PlugIn.IPlugIn.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_call:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.call(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements de.CB_PlugIn.IPlugIn
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public boolean call(java.lang.String Number) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(Number);
mRemote.transact(Stub.TRANSACTION_call, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_call = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public boolean call(java.lang.String Number) throws android.os.RemoteException;
}
