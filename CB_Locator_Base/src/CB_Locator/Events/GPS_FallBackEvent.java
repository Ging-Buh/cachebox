package CB_Locator.Events;

public interface GPS_FallBackEvent
{
	public void FallBackToNetworkProvider();

	public void Fix();

}
