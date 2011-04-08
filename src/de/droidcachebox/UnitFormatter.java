package de.droidcachebox;

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
/*        	
            if (distance <= 500)
                return String.Format(NumberFormatInfo.InvariantInfo, "{0:0}m", distance);

            if (distance < 10000)
                return String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00}km", distance / 1000);

            return String.Format(NumberFormatInfo.InvariantInfo, "{0:0.0}km", distance / 1000);
*/
        	return "not implemented";
        }

        /// <summary>
        /// Erzeugt eine für den Menschen lesbare Form der Distanz
        /// </summary>
        /// <param name="distance"></param>
        /// <returns></returns>
        public static String DistanceStringImperial(float distance)
        {
/*
            float yards = distance / 0.9144f;
            float miles = yards / 1760;

            if (yards < 1000)
                return String.Format(NumberFormatInfo.InvariantInfo, "{0:0}yd", yards);

            if (miles < 10)
                return String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00}mi", miles);

            return String.Format(NumberFormatInfo.InvariantInfo, "{0:0.0}mi", miles);
*/
        	return "not implemented";
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
/*
            return String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00} km/h", kmh);
*/
        	return "not implemented";
        }

        public static String SpeedStringImperial(float kmh)
        {
/*
             return String.Format(NumberFormatInfo.InvariantInfo, "{0:0.00} mph", kmh / 1.6093f);
 */
        	return "not implemented";
        }
	
	
}
