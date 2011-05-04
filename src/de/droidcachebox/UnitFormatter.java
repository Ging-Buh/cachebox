package de.droidcachebox;

import java.text.NumberFormat;

public class UnitFormatter {

	
        public static boolean ImperialUnits = Config.GetBool("ImperialUnits");

        /// <summary>
        /// Erzeugt eine für den Menschen lesbare Form der Distanz
        /// </summary>
        /// <param name="distance"></param>
        /// <returns></returns>
        public static String DistanceString(float distance)
        {
            if (ImperialUnits)
                return DistanceStringImperial(distance);
            else
                return DistanceStringMetric(distance);
        }

        /// <summary>
        /// Erzeugt eine für den Menschen lesbare Form der Distanz
        /// </summary>
        /// <param name="distance"></param>
        /// <returns></returns>
        public static String DistanceStringMetric(float distance)
        {
        	
            if (distance <= 500)
                return String.format("%.0f", distance) + "m";

            if (distance < 10000)
                return String.format("%.2f", distance / 1000) + "km";

            return String.format("%.0f", distance / 1000) + "km";
        }

        /// <summary>
        /// Erzeugt eine für den Menschen lesbare Form der Distanz
        /// </summary>
        /// <param name="distance"></param>
        /// <returns></returns>
        public static String DistanceStringImperial(float distance)
        {

            float yards = distance / 0.9144f;
            float miles = yards / 1760;

            if (yards < 1000)
                return String.format( "%.0f", yards) + "yd";

            if (miles < 10)
                return String.format( "%.2f", miles) + "mi";

            return String.format( "%.1f", miles) + "mi";

        	
        }

        public static String SpeedString(float kmh)
        {
            if (ImperialUnits)
                return SpeedStringImperial(kmh);
            else
                return SpeedStringMetric(kmh);
        }

        public static String SpeedStringMetric(float kmh)
        {
            return String.format("%.2f km/h", kmh);
        }

        public static String SpeedStringImperial(float kmh)
        {
            return String.format("%.2f mph", kmh / 1.6093f);
        }
	
	
}
