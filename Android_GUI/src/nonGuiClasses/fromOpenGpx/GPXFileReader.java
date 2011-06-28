package nonGuiClasses.fromOpenGpx;

import java.io.File;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Date;
import java.util.HashMap;



import com.ximpleware.extended.AutoPilotHuge;
import com.ximpleware.extended.NavExceptionHuge;
import com.ximpleware.extended.VTDExceptionHuge;
import com.ximpleware.extended.VTDGenHuge;
import com.ximpleware.extended.VTDNavHuge;
import com.ximpleware.extended.XMLBuffer;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.LogEntry;
import de.droidcachebox.Geocaching.Waypoint;

public class GPXFileReader 
{
	private String previousCacheCode;
	private boolean foundCacheRecord;
	private Cache	currentCache;
	
	
	/// <summary>
    /// Wird verwendet, um das Size-Feld in einen Datenbank-konformen Typ
    /// umzuwandeln
    /// </summary>
    private HashMap<String, Integer> sizeLookup = new HashMap<String, Integer>();
	
    
    /// <summary>
    /// Wird verwendet, um das Type-Feld der Logs nachzuschlagen
    /// </summary>
    private HashMap<String, Integer> logLookup = new HashMap<String, Integer>();

	
	
	/**
	 * 
	 */
	public GPXFileReader()
	{
        // Cachegröße. Hier hält sich GeoToad ausnahmsweise mal an den
        // Groundspeak-Standard
        sizeLookup.put("micro", 1);
        sizeLookup.put("small", 2);
        sizeLookup.put("regular", 3);
        sizeLookup.put("large", 4);
        sizeLookup.put("not chosen", 0);
        sizeLookup.put("virtual", 0);
        sizeLookup.put("other", 0);
        
        
     // Log-Typen (Groundspeak, GSAK)
        logLookup.put("found it", 0);
        logLookup.put("found", 0);
        logLookup.put("didn't find it", 1);
        logLookup.put("not found", 1);
        logLookup.put("write note", 2);
        logLookup.put("publish listing", 3);
        logLookup.put("enable listing", 4);
        logLookup.put("needs maintenance", 5);
        logLookup.put("temporarily disable listing", 6);
        logLookup.put("owner maintenance", 7);
        logLookup.put("update coordinates", 7);
        logLookup.put("will attend", 8);
        logLookup.put("attended", 9);
        logLookup.put("webcam photo taken", 10);
        logLookup.put("archive", 11);
        logLookup.put("unarchive", 11);
        logLookup.put("post reviewer note", 12);
        logLookup.put("needs archived", 13);

        // GeoToad
        logLookup.put("other", 2);
        logLookup.put("note", 2);
        logLookup.put("geocoins: ", 2);
        logLookup.put("cache disabled!", 6);
        logLookup.put("retract listing", 11);
        
	}

	/**
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public Boolean read(String fileName)
	{
		boolean retVal = false;
	
		final File file = new File(fileName);
		

		if (file.exists())
		{
			try
			{
				final VTDGenHuge vgh = new VTDGenHuge();

				if (vgh.parseFile(fileName, true, VTDGenHuge.MEM_MAPPED))
				{
					
					final VTDNavHuge vn = vgh.getNav();
					retVal = readData(vn);
					
				}
			}
			catch (Exception e)
			{
				
				e.printStackTrace();
				return false;
			}
		}
		return retVal;
	}

	/**
	 * 
	 * @param byteArray
	 * @return
	 */
	public Boolean readByteArray(byte[] byteArray)
	{
		boolean retVal = false;
	
		

		try
		{
			final VTDGenHuge vgh = new VTDGenHuge();
			final XMLBuffer xmlBuffer = new XMLBuffer(byteArray);
			vgh.setDoc(xmlBuffer);
			vgh.parse(true);
			
			final VTDNavHuge vn = vgh.getNav();
			retVal = readData(vn);
			
		}
		catch (Exception e)
		{
			
			e.printStackTrace();
			return false;
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param vn
	 * @return
	 * @throws VTDExceptionHuge
	 */
	private boolean readData(final VTDNavHuge vn) throws VTDExceptionHuge
	{
		final AutoPilotHuge apWpt = new AutoPilotHuge(vn);

		apWpt.selectXPath("/gpx/wpt");

		while (apWpt.evalXPath() != -1)
		{
			vn.push();

			foundCacheRecord = false;
			currentCache = new Cache();
			final Waypoint wpt = new Waypoint();
			
			wpt.setLatitude(vn.parseDouble(vn.getAttrVal("lat")));
			wpt.setLongitude(vn.parseDouble(vn.getAttrVal("lon")));
			
			currentCache.waypoints.add(wpt);
			vn.toElement(VTDNavHuge.FIRST_CHILD);
			
			do
			{
				final String currentElementName = vn.toNormalizedString(vn.getCurrentIndex()).replace("groundspeak:", "");
				
				if (currentElementName.equalsIgnoreCase("time"))
				{
					final String data = vn.toNormalizedString(vn.getText());
					
					if (data.equals("null"))
						wpt.time = new Date();
					else
						wpt.time = ISODateTime.parseString(vn.toNormalizedString(vn.getText()));
				}
				else if (currentElementName.equalsIgnoreCase("name"))
				{
					wpt.Title = vn.toNormalizedString(vn.getText());
					currentCache.GcCode = wpt.Title;
					previousCacheCode = currentCache.GcCode;
				}
				else if (currentElementName.equalsIgnoreCase("desc"))
				{
					wpt.Description = vn.toNormalizedString(vn.getText());
					
					// This is a workaround for waypoints, which have no description set (resp. the description is the name)
					// e.g. gpx files generated from GCtour (http://gctour.madd.in/)
					if (wpt.Description == null || wpt.Description.length() == 0)
					{
						wpt.Description = wpt.Title;					
						String tmp = wpt.Description.toUpperCase().replace(" ", "");
						
						if (tmp.length() >= 2)
							tmp = tmp.substring(0, 2);
						else
							tmp = "ZZ";
						
						wpt.Title = tmp + previousCacheCode.substring(2);
					}
				}
				else if (currentElementName.equalsIgnoreCase("sym"))
				{
					//wpt.symbol = vn.toNormalizedString(vn.getText());
					vn.toNormalizedString(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("type"))
				{
					wpt.parseTypeString(vn.toNormalizedString(vn.getText()));
				}
				else if (currentElementName.equalsIgnoreCase("ele"))
				{
					//wpt.elevation = vn.parseInt(vn.getText());
					vn.parseInt(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("cmt"))
				{
					if (vn.getText() != -1)
					{
						//wpt.comment = vn.toNormalizedString(vn.getText());
						vn.toNormalizedString(vn.getText());
					}
				}
				else if (currentElementName.equalsIgnoreCase("cache") || currentElementName.equalsIgnoreCase("geocache"))
				{
					this.readCache(vn);
				}
				// opencaching.de puts the cache into a extensions node
				else if (currentElementName.equalsIgnoreCase("extensions"))
				{
					vn.push();
					vn.toElement(VTDNavHuge.FIRST_CHILD);
					
					final String elementName = vn.toNormalizedString(vn.getCurrentIndex()).replace("groundspeak:", "");
					
					if (elementName.equalsIgnoreCase("cache"))
					{
						
						this.readCache(vn);
					}

					vn.pop();
				}
			} while (vn.toElement(VTDNavHuge.NEXT_SIBLING));

			if (foundCacheRecord)
			{
				currentCache.WriteToDatabase();
			}
			else
			{
				wpt.WriteToDatabase();
			}

			vn.pop();
		}

		//cacheDatabase.commit();
		return true;
	}

	/**
	 * 
	 * @param vn
	 */
	private void readCache(final VTDNavHuge vn)
	{
		try
		{
			foundCacheRecord = true;
			
			int idx = -1;
			
			// currentCache.id = vn.parseInt(vn.getAttrVal("id"));
			// Note: some generated gpx files don't contain cache IDs ...
			if ((idx = vn.getAttrVal("id")) != -1)
			{
				final String strId = vn.toNormalizedString(vn.getAttrVal("id"));
				try 
				{
					currentCache.Id = Integer.parseInt(strId);
		        } 
				catch (NumberFormatException ex) { }						
			}
			
			if ((idx = vn.getAttrVal("archived")) != -1)
			{
				currentCache.Archived = Boolean.parseBoolean(vn.toNormalizedString(idx));
			}
			
			if ((idx = vn.getAttrVal("available")) != -1)
			{
				currentCache.Available = Boolean.parseBoolean(vn.toNormalizedString(idx));
			}
			
			if ((idx = vn.getAttrVal("status")) != -1)
			{
				//currentCache.parseOpencachingStatus(vn.toNormalizedString(idx));
				vn.toNormalizedString(idx);
			}
	
			vn.toElement(VTDNavHuge.FIRST_CHILD);
	
			do
			{
				final String currentElementName = vn.toNormalizedString(vn.getCurrentIndex()).replace("groundspeak:", "");
				
				if (currentElementName.equalsIgnoreCase("name"))
				{
					currentCache.Name = vn.toNormalizedString(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("placed_by"))
				{
					currentCache.PlacedBy = vn.toNormalizedString(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("owner"))
				{
					currentCache.Owner = vn.toNormalizedString(vn.getText());
					if (vn.getAttrVal("id") != -1) 
					{
						//currentCache.ownerId = vn.parseInt(vn.getAttrVal("id"));
					}
				}
				else if (currentElementName.equalsIgnoreCase("type"))
				{
					currentCache.parseCacheTypeString(vn.toNormalizedString(vn.getText()));
				}
				else if (currentElementName.equalsIgnoreCase("container"))
				{
					currentCache.Size= sizeLookup.get((vn.toNormalizedString(vn.getText())).toLowerCase());
				}
				else if (currentElementName.equalsIgnoreCase("difficulty"))
				{
					final String data = vn.toNormalizedString(vn.getText());
					
					if (data.equals("null")) 
						currentCache.Difficulty = 1f;
					else							
						currentCache.Difficulty = (float) vn.parseDouble(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("terrain"))
				{
					final String data = vn.toNormalizedString(vn.getText());
					
					if (data.equals("null")) 
						currentCache.Terrain = 1f;
					else							
						currentCache.Terrain = (float) vn.parseDouble(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("country"))
				{
					//currentCache.country = vn.toNormalizedString(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("state"))
				{
					final int i = vn.getText();
					if (i != -1)
					{
						//currentCache.state = vn.toNormalizedString(i);
					}
				}
				else if (currentElementName.equalsIgnoreCase("short_description") || currentElementName.equalsIgnoreCase("summary"))
				{
					try {
												
						currentCache.shortDescription = vn.toNormalizedString(vn.getText());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (currentElementName.equalsIgnoreCase("long_description") || currentElementName.equalsIgnoreCase("description"))
				{
					try {
						
						currentCache.longDescription = vn.toNormalizedString(vn.getText());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if ((currentElementName.equalsIgnoreCase("encoded_hints") || currentElementName.equalsIgnoreCase("hints")) && vn.getText() != -1)
				{
					currentCache.hint = vn.toNormalizedString(vn.getText());
				}
				else if (currentElementName.equalsIgnoreCase("logs"))
				{
					this.readLogs(vn);
				}
				else if (currentElementName.equalsIgnoreCase("attributes"))
				{
					this.readAttributes(vn);
				}
				else if (currentElementName.equalsIgnoreCase("travelbugs"))
				{
					//this.readTravelbugs(vn);
				}
			} while (vn.toElement(VTDNavHuge.NEXT_SIBLING));
			
		}
		catch (NavExceptionHuge e) 
		{
			
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param vn
	 */
	private void readAttributes(final VTDNavHuge vn)
	{
		try
		{
			vn.push();
			vn.toElement(VTDNavHuge.FIRST_CHILD);
			
			do
			{
				final String elementName = vn.toNormalizedString(vn.getCurrentIndex()).replace("groundspeak:", "");
				
				if (elementName.equalsIgnoreCase("attribute"))
				{
				
					
					int debugId = vn.parseInt(vn.getAttrVal("id"));
					Boolean debugFlag = (vn.toNormalizedString(vn.getAttrVal("inc")).equals("1"));
					String debugName = vn.toNormalizedString(vn.getText());

					//currentCache.addAttribute(attr);
				}
			} while (vn.toElement(VTDNavHuge.NEXT_SIBLING));

			vn.pop();
		}
		catch (NavExceptionHuge e) 
		{
			
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param vn
	 */
	private void readLogs(final VTDNavHuge vn)
	{
		try 
		{
			vn.push();
			vn.toElement(VTDNavHuge.FIRST_CHILD);
			
			do
			{
				final String elementName = vn.toNormalizedString(vn.getCurrentIndex()).replace("groundspeak:", "");
				
				if (elementName.equalsIgnoreCase("log"))
				{
					vn.push();
					
					final LogEntry logEntry = new LogEntry();
					
					logEntry.CacheId=currentCache.Id;
	
					vn.toElement(VTDNavHuge.FIRST_CHILD);
					
					do
					{
						final String subElementName = vn.toNormalizedString(vn.getCurrentIndex()).replace("groundspeak:", "");
						// Log.d(TAG, currentElementName);
						if (subElementName.equalsIgnoreCase("time") || subElementName.equalsIgnoreCase("date"))
						{
							final String dateString = vn.toNormalizedString(vn.getText());
							logEntry.Timestamp = ISODateTime.parseString(dateString);
							if (logEntry.Timestamp == null) logEntry.Timestamp = LocalDateTime.parseString(dateString);
								
						}
						else if (subElementName.equalsIgnoreCase("type"))
						{
							logEntry.Type= sizeLookup.get((vn.toNormalizedString(vn.getText())).toLowerCase());
						}
						else if (subElementName.equalsIgnoreCase("finder") || subElementName.equalsIgnoreCase("geocacher"))
						{
							logEntry.Finder = vn.toNormalizedString(vn.getText());
						}
						else if (subElementName.equalsIgnoreCase("text") && vn.getText() != -1)
						{
							logEntry.Comment = vn.toNormalizedString(vn.getText());
						}
						else if (subElementName.equalsIgnoreCase("log_wpt"))
						{
							//logEntry.latitude = vn.parseDouble(vn.getAttrVal("lat"));
							//logEntry.longitude = vn.parseDouble(vn.getAttrVal("lon"));
						}													
					} while (vn.toElement(VTDNavHuge.NEXT_SIBLING));
					
					vn.pop();
					
					logEntry.writeToDB();
				}
			} while (vn.toElement(VTDNavHuge.NEXT_SIBLING));
	
			vn.pop();
		} 
		catch (NavExceptionHuge e) 
		{
			
			e.printStackTrace();
		}
	}
	
	
	
	
}
