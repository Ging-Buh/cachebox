/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_Core.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;

/**
 * Der Logger basiert auf einem Interface als CallBack und kann damit auch von nicht GUI Klassen implementiert werden, damit sie einen
 * Fehler Melden können. Wenn keine GUI zur Darstellung diese Meldung empfängt, ist es halt so. Also benutzt so häufig wie möglich den
 * Logger in euren Klassen, dass wird uns helfen Fehler aus der debug.txt auszulesen.
 * 
 * @author Longri
 */
public class Logger
{

	private static Boolean mDebug = false;
	private static ArrayList<ILog> list = new ArrayList<ILog>();

	/**
	 * Wird der Debug-Modus aktiviert (true), werden alle Meldungen weitergeleitet. Ansonsten werden die Debug Meldungen unterdrückt.
	 * Default = false
	 * 
	 * @param debug
	 */
	public static void setDebug(Boolean debug)
	{
		mDebug = debug;
	}

	/**
	 * Registriert eine Klasse zum Empfang der LogMeldungen
	 * 
	 * @param event
	 *            <pre>
	 * Beispiel:</b>
	 * 
	 * Log.Add(<span style=' color: Blue;'>this</span>);
	 * 
	 * </pre>
	 */
	public static void Add(ILog event)
	{
		list.add(event);
	}

	/**
	 * Löscht eine Registrierung zum Empfang von LogMeldungen
	 * 
	 * @param event
	 *            <pre>
	 * Beispiel:</b>
	 * 
	 * Log.Remove(<span style=' color: Blue;'>this</span>);
	 * 
	 * </pre>
	 */
	public static void Remove(ILog event)
	{
		list.remove(event);
	}

	/**
	 * Meldet einen Fehler
	 * 
	 * @param Name
	 *            (Name der Klasse und Methode in der der Fehler auftrat.)
	 * @param Msg
	 *            (Meldung die übergeben werden soll.)
	 * @param e
	 *            (Exeption die abgefangen wurde. null ist möglich!)
	 * 
	 *            <pre>
	 *  <span style=' color: Blue;'>try</span> 
	 * {
	 *     mp.setDataSource(Config.WorkPath + <span style=' color: Maroon;'>"/data/sound/"</span> + soundFile);
	 *     mp.prepare();
	 * } 
	 * <span style=' color: Blue;'>catch</span> (Exception e) 
	 * {
	 *     Log.Error(<span style=' color: Maroon;'>"Global.PlaySound()"</span>, Config.WorkPath + <span style=' color: Maroon;'>"/data/sound/"</span> + soundFile ,e);
	 *     e.printStackTrace();
	 * }
	 * </pre>
	 */
	public static void Error(String Name, String Msg, Exception e)
	{
		String Ex = "";
		if (e != null && e.getMessage() != null) Ex = "Ex = [" + e.getMessage() + "]";
		else if (e != null && e.getLocalizedMessage() != null) Ex = "Ex = [" + e.getLocalizedMessage() + "]";
		else
			Ex = "Ex = [" + e.toString() + "]";

		String Short = "[ERR]" + Name + " [" + Msg + "] ";
		Msg = "[ERROR]- at " + Name + "- [" + Msg + "] " + Ex;

		sendMsg(Msg, Short);
	}

	/**
	 * Meldet eine generelle Msg
	 * 
	 * @param Msg
	 */
	public static void General(String Msg)
	{
		String Short = "[GEN] [" + Msg + "] ";
		Msg = "[GENERAL]- [" + Msg + "] ";

		sendMsg(Msg, Short);
	}

	/**
	 * Meldet eine Msg nur wenn der Debug-Modus aktiviert ist.
	 * 
	 * @param Msg
	 */
	public static void DEBUG(String Msg)
	{
		if (mDebug)
		{
			String Short = "[DEB] [" + Msg + "] ";
			Msg = "[DEBUG]- [" + Msg + "] ";
			sendMsg(Msg, Short);
		}
	}

	/**
	 * Sendet die aufbereitete Msg von Error,Debug oder General
	 * 
	 * @param Msg
	 */
	private static void sendMsg(String Msg, String Short)
	{

		// add Timestamp
		Date now = new Date();

		SimpleDateFormat postFormater = new SimpleDateFormat("mm:ss");
		String dateString = postFormater.format(now);

		SimpleDateFormat postFormater2 = new SimpleDateFormat("dd/MM hh:mm:ss");
		String dateString2 = postFormater2.format(now);

		Short = dateString + Short + String.format("%n");
		Msg = dateString2 + " - " + Msg + String.format("%n");

		for (ILog event : list)
		{
			event.receiveLog(Msg);
			event.receiveShortLog(Short);
		}

		writeDebugMsgtoFile(Msg);
	}

	public static void Error(String name, String msg, OutOfMemoryError e)
	{
		Exception ex = null;
		Error(name, msg, ex);
	}

	public static void LogCat(String Msg)
	{
		for (ILog event : list)
		{
			event.receiveLogCat(Msg);
		}

	}

	/**
	 * nur True beim ersten schreiben! Dann müssen erst die Missing Lang Strings eingelesen werden!
	 */
	private static boolean firstWrite = true;

	private static void writeDebugMsgtoFile(String Msg)
	{
		File file = new File(Config.WorkPath + "/debug.txt");

		if (Config.settings.newInstall.getValue())
		{
			file.delete();
		}

		if (!file.exists())
		{
			// schreibe UI Sizes als erstes in die dbug.txt
			String br = GlobalCore.br;
			devicesSizes ui = UiSizes.ui;

			if (ui == null) return; // Bin noch nicht soweit!

			StringBuilder sb = new StringBuilder();
			sb.append("###################################" + br);
			sb.append("##  CB Version: " + GlobalCore.getVersionString() + "         ##" + br);
			sb.append("###################################" + br + br + br);
			sb.append("################  Ui Sizes ############" + br);
			sb.append("Window = " + ui.Window.toString() + br);
			sb.append("Density = " + ui.Density + br);
			sb.append("ButtonSize = " + ui.ButtonSize.toString() + br);
			sb.append("RefSize = " + ui.RefSize + br);
			sb.append("TextSize_Normal = " + ui.TextSize_Normal + br);
			sb.append("ButtonTextSize = " + ui.ButtonTextSize + br);
			sb.append("IconSize = " + ui.IconSize + br);
			sb.append("Margin = " + ui.Margin + br);
			sb.append("ArrowSizeList = " + ui.ArrowSizeList + br);
			sb.append("ArrowSizeMap = " + ui.ArrowSizeMap + br);
			sb.append("TB_IconSize = " + ui.TB_IconSize + br);
			sb.append("isLandscape = " + ui.isLandscape + br);
			sb.append("    " + br);
			sb.append("MapViewDPIFaktor = " + Config.settings.MapViewDPIFaktor.getValue() + br);
			sb.append("MapViewFontFaktor = " + Config.settings.MapViewFontFaktor.getValue() + br);
			sb.append("#######################################" + br + br);

			sb.append("##########  Missing Lang Strings ######" + br);

			sb.append("#######################################" + br + br);

			Msg = sb.toString() + Msg;

		}
		else
		{
			if (firstWrite)
			{
				try
				{
					GlobalCore.Translations.readMissingStringsFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				firstWrite = false;
			}
		}

		FileWriter writer;
		try
		{
			writer = new FileWriter(file, true);
			writer.write(Msg);
			writer.close();
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}

	}

}
