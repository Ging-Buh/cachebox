package CB_Translation_Base.TranslationEngine;

/**
 * Extends the Class Translations with hold the ID as String
 * 
 * @author Longri
 */
public class MissingTranslation extends Translations
{

	final String stringId;

	public MissingTranslation(String ID, String Trans)
	{
		super(ID, Trans);
		stringId = ID;
	}

	public String getMissingString()
	{
		return stringId;
	}

	@Override
	public String toString()
	{
		return stringId;
	}

}
