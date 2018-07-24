package cb_server;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;

public class IconServlet extends HttpServlet {
	private static final long serialVersionUID = 1205779103262021876L;

	public IconServlet() {

	}

	public void doGet___(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String fileName = "/icons/32-0.png";
		Image img = getImage(fileName);
		// your image servlet code here ü
		resp.setContentType("image/png");
		resp.setStatus(HttpServletResponse.SC_OK);

		BufferedImage image = null;
		image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(new java.awt.Color(1.0f, 1.0f, 1.0f, 0.75f));
		graphics.fillRoundRect(0, 0, 32, 32, 5, 5);
		graphics.drawImage(img, 0, 0, image.getWidth(), image.getHeight(), 0, 0, img.getWidth(null), img.getHeight(null), null);
		graphics.dispose();

		if (ImageIO.write(image, "png", resp.getOutputStream()))
			System.out.println("###############################");
		resp.getOutputStream().close();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		synchronized (this) {
			ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream();
			byte[] captchaBytes = null; // imageBytes

			String sQuery = request.getPathInfo().substring(1);
			try {
				int pos = sQuery.indexOf(".");
				if (pos > 0)
					sQuery = sQuery.substring(0, pos);
				String[] query = sQuery.split("_");

				boolean diffTerr = false;
				boolean cache = true;
				int cacheType = 0;
				boolean deactivated = false;
				boolean archived = false;
				boolean owner = false;
				boolean found = false;
				boolean solved = false;
				boolean hasStart = false;
				boolean selected = false;
				int difficulty = 0;
				int terrain = 0;
				int size = 0;
				int background = 0;
				for (int i = 0; i < query.length; i++) {
					switch (query[i].charAt(0)) {
					case 'C': // Cache
					case 'W': // Waypoint
						if (query[i].charAt(0) == 'W')
							cache = false;
						cacheType = Integer.parseInt(query[i].substring(1, 3));
						deactivated = query[i].contains("D");
						archived = query[i].contains("A");
						found = query[i].contains("F");
						owner = query[i].contains("O");
						solved = query[i].contains("S");
						hasStart = query[i].contains("T");
						selected = query[i].contains("L");
						break;
					case 'D': // Difficulty
						difficulty = Integer.parseInt(query[i].substring(1));
						break;
					case 'T': // Terrain
						terrain = Integer.parseInt(query[i].substring(1));
						break;
					case 'B': // Background size
						background = Integer.parseInt(query[i].substring(1));
						break;
					case 'S': // Image size
						size = Integer.parseInt(query[i].substring(1));
						break;
					case 'X': // Difficulty/Terrain
						diffTerr = true;
						break;
					}
				}
				if (background <= size) {
					// background wird == size �bergeben. Wenn background nicht
					// > size ist -> keinen Hintergrund
					background = 0;
				}

				String prefix = "32-"; // Prefix f�r die Icon-Dateien
				if (size <= 15)
					prefix = "15-";
				String postfix = "";
				if (solved)
					postfix = "S";
				String fileName = "/icons/" + prefix + cacheType + postfix + ".png";
				if (found) {
					fileName = "/icons/" + prefix + "Found.png";
				}

				response.setContentType("image/png");
				response.setStatus(HttpServletResponse.SC_OK);
				// response.setHeader("expires",
				// "Thu, 01 Dec 2099 00:00:00 GMT");

				Image img = null;
				Image img2 = null;

				BufferedImage image = null;

				if (diffTerr) {
					String id = String.valueOf(difficulty / 2);
					if (difficulty % 2 > 0) {
						id += "-5";
					}
					String id2 = String.valueOf(terrain / 2);
					if (terrain % 2 > 0) {
						id2 += "-5";
					}
					img = getImage("/icons/stars" + id + "small.png");

					int starHeight = img.getWidth(null);
					int starWidth = Math.round((float) (img.getHeight(null) * size) / starHeight);
					background = size + 2 * starWidth;
					image = new BufferedImage(starWidth, size, BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = (Graphics2D) image.getGraphics();
					graphics.setColor(new java.awt.Color(1.0f, 1.0f, 1.0f, 0.5f));
					graphics.fillRoundRect(0, 0, starWidth + 2, starHeight, starWidth / 2, starWidth / 2);
					// graphics.fillRoundRect(background - 2 - starWidth, 0,
					// background, starHeight, starWidth / 2, starWidth / 2);
					int dx = -size;
					int dy = 1;
					graphics.rotate(-Math.PI / 2);
					graphics.drawImage(img, dx, dy, dx + size - 1, dy + starWidth, 0, 0, img.getWidth(null), img.getHeight(null), null);
					dy += size + starWidth - 2;
					// graphics.drawImage(img2, dx, dy, dx + size - 1, dy +
					// starWidth, 0, 0, img.getWidth(null), img.getHeight(null),
					// null);
					graphics.dispose();
				} else {
					if (background > 0) {
						if (selected) {
							img = getImage("/icons/shaddowrect-selected.png");
						} else {
							img = getImage("/icons/shaddowrect.png");
						}
						img2 = getImage(fileName);
					} else {
						background = size;
						img = getImage(fileName);
					}

					image = new BufferedImage(background, background, BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = (Graphics2D) image.getGraphics();

					graphics.drawImage(img, 0, 0, image.getWidth(), image.getHeight(), 0, 0, img.getWidth(null), img.getHeight(null), null);
					if (img2 != null) {
						int dx = (background - size) / 2;
						int dy = (background - size) / 2;
						graphics.drawImage(img2, dx, dy, dx + size, dx + size, 0, 0, img2.getWidth(null), img2.getHeight(null), null);
					}
					if (deactivated || archived) {
						// Roter durchstreichen
						int rand = 5;
						int width = 4;
						if (background <= 16) {
							rand = 2;
							width = 2;
						}
						graphics.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
						graphics.setColor(java.awt.Color.red);
						graphics.drawLine(rand, rand, background - rand - 1, background - rand - 1);
					}
					graphics.dispose();
				}
				try {
					// ImageIO.write(image, "png", response.getOutputStream());
					// response.getOutputStream().close();
					ImageIO.write(image, "png", imgOutputStream);
					captchaBytes = imgOutputStream.toByteArray();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// response.setHeader("Cache-Control", "no-store");
			// response.setHeader("Pragma", "no-cache");

			response.setDateHeader("Expires", 0);

			response.setContentType("image/png");

			// Write the image to the client.
			ServletOutputStream outStream = response.getOutputStream();
			outStream.write(captchaBytes);
			outStream.flush();
			outStream.close();
			// FileOutputStream fs = new FileOutputStream(new File(sQuery +
			// ".png"));
			// fs.write(captchaBytes);
			// fs.close();
		}
	}

	protected void doGet_(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sQuery = request.getPathInfo();
		String[] query = sQuery.split("/");
		int size = Integer.parseInt(query[1]);
		int cacheTyp = Integer.parseInt(query[2]);
		boolean selected = query[3].equals("1");
		boolean deactivated = query[4].equals("0");
		deactivated = false;
		boolean archived = query.length > 5 ? query[5].equals("1") : false;
		boolean found = query.length > 6 ? query[6].equals("1") : false;
		boolean owner = query.length > 7 ? query[7].equals("1") : false;
		int backgroundSize = query.length > 8 ? Integer.parseInt(query[8]) : 0;
		int difficulty = query.length > 9 ? Integer.parseInt(query[9]) : 0;
		int terrain = query.length > 10 ? Integer.parseInt(query[10]) : 0;

		// Gr��en umrechnen
		switch (size) {
		case 0:
			size = 15;
			backgroundSize = 0;
			break;
		case 1:
			size = 16;
			if (backgroundSize > 0)
				backgroundSize = 20;
			break;
		case 2:
			size = 32;
			if (backgroundSize > 0)
				backgroundSize = 48;
			break;
		}

		response.setContentType("image/png");
		response.setStatus(HttpServletResponse.SC_OK);
		InputStream is = null;
		InputStream is2 = null;
		ResourceBitmap bmp = null;
		ResourceBitmap bmp2 = null;
		if (backgroundSize > 0) {
			if (selected) {
				is = getClass().getResourceAsStream("/icons/shaddowrect-selected.png");
			} else {
				is = getClass().getResourceAsStream("/icons/shaddowrect.png");
			}
			bmp = AwtGraphicFactory.INSTANCE.createResourceBitmap(is, 0);
			is2 = getClass().getResourceAsStream("/icons/" + cacheTyp + ".png");
			bmp2 = AwtGraphicFactory.INSTANCE.createResourceBitmap(is2, 0);
		} else {
			is = getClass().getResourceAsStream("/icons/" + cacheTyp + ".png");
			bmp = AwtGraphicFactory.INSTANCE.createResourceBitmap(is, 0);
			backgroundSize = size;
		}

		TileBitmap bitmap = AwtGraphicFactory.INSTANCE.createTileBitmap(backgroundSize, true);
		Canvas canvas = AwtGraphicFactory.INSTANCE.createCanvas();
		canvas.setBitmap(bitmap);
		Matrix matrix = AwtGraphicFactory.INSTANCE.createMatrix();
		matrix.scale((float) backgroundSize / (float) bmp.getWidth(), (float) backgroundSize / (float) bmp.getHeight());
		canvas.drawBitmap(bmp, matrix);
		if (bmp2 != null) {
			matrix = AwtGraphicFactory.INSTANCE.createMatrix();
			matrix.translate((backgroundSize - size) / 2, (backgroundSize - size) / 2);
			matrix.scale((float) size / (float) bmp2.getWidth(), (float) size / (float) bmp2.getHeight());
			canvas.drawBitmap(bmp2, matrix);
		}
		if (deactivated) {
			// Roter durchstreichen
			Paint p = AwtGraphicFactory.INSTANCE.createPaint();
			p.setColor(Color.RED);
			p.setStrokeWidth(4);
			p.setStyle(Style.STROKE);
			canvas.drawLine(5, 5, 42, 42, p);
		}
		try {
			bitmap.compress(response.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Image getImage(String name) {
		try {
			URL url = this.getClass().getResource(name);
			// URL url = Resources.class.getResource(name);
			// return Toolkit.getDefaultToolkit().getImage(url);
			return ImageIO.read(IconServlet.class.getResource(name));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
