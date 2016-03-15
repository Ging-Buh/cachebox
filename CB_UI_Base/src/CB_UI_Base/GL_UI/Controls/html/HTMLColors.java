package CB_UI_Base.GL_UI.Controls.html;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import CB_Utils.Log.Log; import org.slf4j.LoggerFactory;

import CB_Utils.Util.HSV_Color;

/**
 * HTML color names. It's intended use is for parsing a name and return the
 * corresponding color or return a name for a given color.
 * @author Adrian Ber
 */
public class HTMLColors {

	final static org.slf4j.Logger log = LoggerFactory.getLogger(HTMLColors.class);

	/** Don't instantiate this, use only the static methods */
	private HTMLColors() {
	}

	/** map between color names and colors;
	 * tough there are fields for every color we use a map because is a faster
	 * way to get the color
	 */
	private static Map<String, HSV_Color> name2color = new HashMap<String, HSV_Color>();
	/** map between colors and color names;
	 * tough there are fields for every color we use a map because is a faster
	 * way to get the color
	 */
	private static Map<HSV_Color, String> color2name = new HashMap<HSV_Color, String>();

	/** Initialiase colors map */
	private static void initColorsMap() {
		Field[] fields = HTMLColors.class.getFields();
		for (Field field : fields) {
			if (field.getType().isAssignableFrom(HSV_Color.class)) {
				addColor(field.getName());
			}
		}
	}

	/** Used to initialize the map */
	private static void addColor(String colorName, HSV_Color color) {
		name2color.put(colorName, color);
		color2name.put(color, colorName);
	}

	/** Used to initialize the map */
	private static void addColor(String colorName) {
		addColor(colorName, getColorFromField(colorName));
	}

	//    /** Used to initialize the map */
	//    private static void addColor(String colorName, int colorRGB) {
	//	addColor(colorName, new HSV_Color(colorRGB));
	//    }

	/** Returns a color with the specified case-insensitive name. */
	private static HSV_Color getColorFromField(String name) {
		try {
			Field colorField = HTMLColors.class.getField(name.toLowerCase());
			return (HSV_Color) colorField.get(HTMLColors.class);
		} catch (NoSuchFieldException exc) {
		} catch (SecurityException exc) {
		} catch (IllegalAccessException exc) {
		} catch (IllegalArgumentException exc) {
		}
		return null;
	}

	/** Returns a color with the specified case-insensitive name.*/
	public static String getName(HSV_Color color) {
		return color2name.get(color);
	}

	/** Returns a color with the specified case-insensitive name.*/
	public static HSV_Color getColor(String name) {

		if (name.startsWith("rgb(")) {
			String sub = name.substring(4, name.length() - 1);
			String[] values = sub.split(",");

			int r = Integer.parseInt(values[0].trim());
			int g = Integer.parseInt(values[1].trim());
			int b = Integer.parseInt(values[2].trim());

			HSV_Color color = new HSV_Color(255, r, g, b);
			return color;

		}

		HSV_Color ret = name2color.get(name.toLowerCase());

		if (ret == null) {
			// unknown color, set to black
			Log.err(log, "unknown color name: " + name);
			ret = new HSV_Color(HSV_Color.BLACK);
		}

		return ret;
	}

	/** Returns a collection of all color names */
	public static Collection<String> colors() {
		return name2color.keySet();
	}

	public static final HSV_Color aliceblue = new HSV_Color("f0f8ff");
	public static final HSV_Color antiquewhite = new HSV_Color("faebd7");
	public static final HSV_Color aqua = new HSV_Color("00ffff");
	public static final HSV_Color aquamarine = new HSV_Color("7fffd4");
	public static final HSV_Color azure = new HSV_Color("f0ffff");
	public static final HSV_Color beige = new HSV_Color("f5f5dc");
	public static final HSV_Color bisque = new HSV_Color("ffe4c4");
	public static final HSV_Color black = new HSV_Color("000000");
	public static final HSV_Color blanchedalmond = new HSV_Color("ffebcd");
	public static final HSV_Color blue = new HSV_Color("0000ff");
	public static final HSV_Color blueviolet = new HSV_Color("8a2be2");
	public static final HSV_Color brown = new HSV_Color("a52a2a");
	public static final HSV_Color burlywood = new HSV_Color("deb887");
	public static final HSV_Color cadetblue = new HSV_Color("5f9ea0");
	public static final HSV_Color chartreuse = new HSV_Color("7fff00");
	public static final HSV_Color chocolate = new HSV_Color("d2691e");
	public static final HSV_Color coral = new HSV_Color("ff7f50");
	public static final HSV_Color cornflowerblue = new HSV_Color("6495ed");
	public static final HSV_Color cornsilk = new HSV_Color("fff8dc");
	public static final HSV_Color crimson = new HSV_Color("dc143c");
	public static final HSV_Color cyan = new HSV_Color("00ffff");
	public static final HSV_Color darkblue = new HSV_Color("00008b");
	public static final HSV_Color darkcyan = new HSV_Color("008b8b");
	public static final HSV_Color darkgoldenrod = new HSV_Color("b8860b");
	public static final HSV_Color darkgray = new HSV_Color("a9a9a9");
	public static final HSV_Color darkgrey = new HSV_Color("a9a9a9");
	public static final HSV_Color darkgreen = new HSV_Color("006400");
	public static final HSV_Color darkkhaki = new HSV_Color("bdb76b");
	public static final HSV_Color darkmagenta = new HSV_Color("8b008b");
	public static final HSV_Color darkolivegreen = new HSV_Color("556b2f");
	public static final HSV_Color darkorange = new HSV_Color("ff8c00");
	public static final HSV_Color darkorchid = new HSV_Color("9932cc");
	public static final HSV_Color darkred = new HSV_Color("8b0000");
	public static final HSV_Color darksalmon = new HSV_Color("e9967a");
	public static final HSV_Color darkseagreen = new HSV_Color("8fbc8f");
	public static final HSV_Color darkslateblue = new HSV_Color("483d8b");
	public static final HSV_Color darkslategray = new HSV_Color("2f4f4f");
	public static final HSV_Color darkslategrey = new HSV_Color("2f4f4f");
	public static final HSV_Color darkturquoise = new HSV_Color("00ced1");
	public static final HSV_Color darkviolet = new HSV_Color("9400d3");
	public static final HSV_Color deeppink = new HSV_Color("ff1493");
	public static final HSV_Color deepskyblue = new HSV_Color("00bfff");
	public static final HSV_Color dimgray = new HSV_Color("696969");
	public static final HSV_Color dimgrey = new HSV_Color("696969");
	public static final HSV_Color dodgerblue = new HSV_Color("1e90ff");
	public static final HSV_Color firebrick = new HSV_Color("b22222");
	public static final HSV_Color floralwhite = new HSV_Color("fffaf0");
	public static final HSV_Color forestgreen = new HSV_Color("228b22");
	public static final HSV_Color fuchsia = new HSV_Color("ff00ff");
	public static final HSV_Color gainsboro = new HSV_Color("dcdcdc");
	public static final HSV_Color ghostwhite = new HSV_Color("f8f8ff");
	public static final HSV_Color gold = new HSV_Color("ffd700");
	public static final HSV_Color goldenrod = new HSV_Color("daa520");
	public static final HSV_Color gray = new HSV_Color("808080");
	public static final HSV_Color grey = new HSV_Color("808080");
	public static final HSV_Color green = new HSV_Color("008000");
	public static final HSV_Color greenyellow = new HSV_Color("adff2f");
	public static final HSV_Color honeydew = new HSV_Color("f0fff0");
	public static final HSV_Color hotpink = new HSV_Color("ff69b4");
	public static final HSV_Color indianred = new HSV_Color("cd5c5c");
	public static final HSV_Color indigo = new HSV_Color("4b0082");
	public static final HSV_Color ivory = new HSV_Color("fffff0");
	public static final HSV_Color khaki = new HSV_Color("f0e68c");
	public static final HSV_Color lavender = new HSV_Color("e6e6fa");
	public static final HSV_Color lavenderblush = new HSV_Color("fff0f5");
	public static final HSV_Color lawngreen = new HSV_Color("7cfc00");
	public static final HSV_Color lemonchiffon = new HSV_Color("fffacd");
	public static final HSV_Color lightblue = new HSV_Color("add8e6");
	public static final HSV_Color lightcoral = new HSV_Color("f08080");
	public static final HSV_Color lightcyan = new HSV_Color("e0ffff");
	public static final HSV_Color lightgoldenrodyellow = new HSV_Color("fafad2");
	public static final HSV_Color lightgreen = new HSV_Color("90ee90");
	public static final HSV_Color lightgrey = new HSV_Color("d3d3d3");
	public static final HSV_Color lightgray = new HSV_Color("d3d3d3");
	public static final HSV_Color lightpink = new HSV_Color("ffb6c1");
	public static final HSV_Color lightsalmon = new HSV_Color("ffa07a");
	public static final HSV_Color lightseagreen = new HSV_Color("20b2aa");
	public static final HSV_Color lightskyblue = new HSV_Color("87cefa");
	public static final HSV_Color lightslategray = new HSV_Color("778899");
	public static final HSV_Color lightslategrey = new HSV_Color("778899");
	public static final HSV_Color lightsteelblue = new HSV_Color("b0c4de");
	public static final HSV_Color lightyellow = new HSV_Color("ffffe0");
	public static final HSV_Color lime = new HSV_Color("00ff00");
	public static final HSV_Color limegreen = new HSV_Color("32cd32");
	public static final HSV_Color linen = new HSV_Color("faf0e6");
	public static final HSV_Color magenta = new HSV_Color("ff00ff");
	public static final HSV_Color maroon = new HSV_Color("800000");
	public static final HSV_Color mediumaquamarine = new HSV_Color("66cdaa");
	public static final HSV_Color mediumblue = new HSV_Color("0000cd");
	public static final HSV_Color mediumorchid = new HSV_Color("ba55d3");
	public static final HSV_Color mediumpurple = new HSV_Color("9370db");
	public static final HSV_Color mediumseagreen = new HSV_Color("3cb371");
	public static final HSV_Color mediumslateblue = new HSV_Color("7b68ee");
	public static final HSV_Color mediumspringgreen = new HSV_Color("00fa9a");
	public static final HSV_Color mediumturquoise = new HSV_Color("48d1cc");
	public static final HSV_Color mediumvioletred = new HSV_Color("c71585");
	public static final HSV_Color midnightblue = new HSV_Color("191970");
	public static final HSV_Color mintcream = new HSV_Color("f5fffa");
	public static final HSV_Color mistyrose = new HSV_Color("ffe4e1");
	public static final HSV_Color moccasin = new HSV_Color("ffe4b5");
	public static final HSV_Color navajowhite = new HSV_Color("ffdead");
	public static final HSV_Color navy = new HSV_Color("000080");
	public static final HSV_Color oldlace = new HSV_Color("fdf5e6");
	public static final HSV_Color olive = new HSV_Color("808000");
	public static final HSV_Color olivedrab = new HSV_Color("6b8e23");
	public static final HSV_Color orange = new HSV_Color("ffa500");
	public static final HSV_Color orangered = new HSV_Color("ff4500");
	public static final HSV_Color orchid = new HSV_Color("da70d6");
	public static final HSV_Color palegoldenrod = new HSV_Color("eee8aa");
	public static final HSV_Color palegreen = new HSV_Color("98fb98");
	public static final HSV_Color paleturquoise = new HSV_Color("afeeee");
	public static final HSV_Color palevioletred = new HSV_Color("db7093");
	public static final HSV_Color papayawhip = new HSV_Color("ffefd5");
	public static final HSV_Color peachpuff = new HSV_Color("ffdab9");
	public static final HSV_Color peru = new HSV_Color("cd853f");
	public static final HSV_Color pink = new HSV_Color("ffc0cb");
	public static final HSV_Color plum = new HSV_Color("dda0dd");
	public static final HSV_Color powderblue = new HSV_Color("b0e0e6");
	public static final HSV_Color purple = new HSV_Color("800080");
	public static final HSV_Color red = new HSV_Color("ff0000");
	public static final HSV_Color rosybrown = new HSV_Color("bc8f8f");
	public static final HSV_Color royalblue = new HSV_Color("4169e1");
	public static final HSV_Color saddlebrown = new HSV_Color("8b4513");
	public static final HSV_Color salmon = new HSV_Color("fa8072");
	public static final HSV_Color sandybrown = new HSV_Color("f4a460");
	public static final HSV_Color seagreen = new HSV_Color("2e8b57");
	public static final HSV_Color seashell = new HSV_Color("fff5ee");
	public static final HSV_Color sienna = new HSV_Color("a0522d");
	public static final HSV_Color silver = new HSV_Color("c0c0c0");
	public static final HSV_Color skyblue = new HSV_Color("87ceeb");
	public static final HSV_Color slateblue = new HSV_Color("6a5acd");
	public static final HSV_Color slategray = new HSV_Color("708090");
	public static final HSV_Color slategrey = new HSV_Color("708090");
	public static final HSV_Color snow = new HSV_Color("fffafa");
	public static final HSV_Color springgreen = new HSV_Color("00ff7f");
	public static final HSV_Color steelblue = new HSV_Color("4682b4");
	public static final HSV_Color tan = new HSV_Color("d2b48c");
	public static final HSV_Color teal = new HSV_Color("008080");
	public static final HSV_Color thistle = new HSV_Color("d8bfd8");
	public static final HSV_Color tomato = new HSV_Color("ff6347");
	public static final HSV_Color turquoise = new HSV_Color("40e0d0");
	public static final HSV_Color violet = new HSV_Color("ee82ee");
	public static final HSV_Color wheat = new HSV_Color("f5deb3");
	public static final HSV_Color white = new HSV_Color("ffffff");
	public static final HSV_Color whitesmoke = new HSV_Color("f5f5f5");
	public static final HSV_Color yellow = new HSV_Color("ffff00");
	public static final HSV_Color yellowgreen = new HSV_Color("9acd32");

	static {
		initColorsMap();
	}

}
