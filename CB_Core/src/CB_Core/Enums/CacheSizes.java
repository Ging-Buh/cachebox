package CB_Core.Enums;

public enum CacheSizes {
	other, // 0
    micro, // 1
    small, // 2
    regular, // 3
    large // 4
    // virtual, // 0 - Achtung, hier habe ich einen neuen Wert eingef�gt 
    // other // 6 - Achtung, hier habe ich einen neuen Wert eingef�gt
    ;

	public static CacheSizes parseInt(int value)
	{
		switch( value ) {
			case 1:
				return CacheSizes.micro;
			case 2:
				return CacheSizes.small;
			case 3:
				return CacheSizes.regular;
			case 4:
				return CacheSizes.large;
			default:
				return CacheSizes.other;
		}
	}
	
	public static CacheSizes parseString(String text)
	{
		// Groundspeak
		if( text.equalsIgnoreCase( "micro" ) ) {
			return CacheSizes.micro;
		}
		if( text.equalsIgnoreCase( "small" ) ) {
			return CacheSizes.small;
		}
		if( text.equalsIgnoreCase( "regular" ) ) {
			return CacheSizes.regular;
		}
		if( text.equalsIgnoreCase( "large" ) ) {
			return CacheSizes.large;
		}
		if( text.equalsIgnoreCase( "not chosen" ) ) {
			return CacheSizes.other;
		}
		if( text.equalsIgnoreCase( "virtual" ) ) {
			return CacheSizes.other;
		}
		if( text.equalsIgnoreCase( "other" ) ) {
			return CacheSizes.other;
		}
		// GCTour
		if( text.equalsIgnoreCase( "none" ) ) {
			return CacheSizes.other;
		}
		if( text.equalsIgnoreCase( "very large" ) ) {
			return CacheSizes.large;
		}
		return CacheSizes.other;
	}

}