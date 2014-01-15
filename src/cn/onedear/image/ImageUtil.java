package cn.onedear.image;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author onedear
 * @date Jan 2, 2014 7:23:01 PM 
 */
public class ImageUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);
	
	public final static String imageSuffixs = "jpeg|png|jpg|gif|bmp";
	
	/**
	 * 像素是否透明
	 * @param image
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean isTransparent(BufferedImage image, int x, int y) {
		return isTransparent(image.getRGB(x, y));
	}
	
	/**
	 * 是否透明
	 * @param rgb
	 * @return
	 */
	public static boolean isTransparent(int rgb) {
		return (rgb >>24) == 0x00 ? true : false;
	}
	
	/**
	 * 是否半透
	 * @param rgb
	 * @return
	 */
	public static boolean isAlpha(int rgb) {
		return getAlpha(rgb) == 255 ? false : true;
	}
	
	/**
	 * 获取alpha值
	 * @param rbg
	 * @return
	 */
	public static int getAlpha(int rbg) {
		return (rbg >> 24) & 0xff;
	}
	
	/**
	 * 从数据源找到目标图片的位置
	 * 用的是template matching算法
	 * http://en.wikipedia.org/wiki/Template_matching
	 * @param source
	 * @param target
	 * @return
	 */
	public static ImageRect findPoint(BufferedImage source, BufferedImage target) {
		int blurIncr = 10;
		int exactRange = 10;
		int minSad = Integer.MAX_VALUE;
		ImageRect ir = new ImageRect();
		ir.image = target;
		for (int i = 0; i <= source.getWidth() - target.getWidth(); i += 1) {
			for (int j = 0; j <= source.getHeight() - target.getHeight(); j += 1) {
				int blurWidthIncr = target.getWidth() / blurIncr;
				int blurHeightIncr = target.getHeight() / blurIncr;
				int sad = 0;
				for (int x = 0; x < target.getWidth(); x += blurWidthIncr) {
					for (int y = 0; y < target.getHeight(); y += blurHeightIncr) {
						int sRbg = source.getRGB(i + x, j + y);
						int tRbg = target.getRGB(x, y);
						if (isTransparent(tRbg)) {
							continue;
						}
						if (isAlpha(tRbg)) {
							continue;
						}
						sad += Math.abs(getGray(sRbg) - getGray(tRbg));
					}
				}
				if (sad < minSad) {
					logger.debug("坐标[{}, {}], sad[{}], minSad[{}]", new Object[]{i,j,sad, minSad});
					minSad = sad;
					ir.drawX = i;
					ir.drawY = j;
				}
			}
			if (i % 10 == 0) {
				logger.debug("模糊查询已执行了{}列, 共{}列", i, (source.getWidth() - target.getWidth()));
			}
		}
		logger.info("[模糊]查询查找了xy坐标分别是[{},{}]", ir.drawX, ir.drawY);
		
		minSad = Integer.MAX_VALUE;
		int iMax = (ir.drawX + exactRange) < (source.getWidth() - target.getWidth()) 
				? ir.drawX + exactRange : source.getWidth() - target.getWidth();
		int jMax = (ir.drawY + exactRange) < (source.getHeight() - target.getHeight()) 
				? ir.drawY + exactRange : source.getHeight() - target.getHeight();
		int iMin = (ir.drawX - exactRange) > 0 ? ir.drawX - exactRange: 0;
		int jMin = (ir.drawY - exactRange) > 0 ? ir.drawY - exactRange: 0;
		for (int i = iMin; i <= iMax; i++) {
			for (int j = jMin; j <= jMax; j++) {
				int sad = 0;
				for (int x = 0; x < target.getWidth(); x++) {
					for (int y = 0; y < target.getHeight(); y++) {
						int sRbg = source.getRGB(i + x, j + y);
						int tRbg = target.getRGB(x, y);
						if (isTransparent(tRbg)) {
							continue;
						}
						if (isAlpha(tRbg)) {
							continue;
						}
						sad += Math.abs(getGray(sRbg) - getGray(tRbg));
					}
				}
				if (sad < minSad) {
//					找到相应的图片
					minSad = sad;
					ir.drawX = i;
					ir.drawY = j;
				}
			}
			if (i % 10 == 0) {
				logger.debug("[精确]查询已执行了{}列, 共{}列", i, (source.getWidth() - target.getWidth()));
			}
		}
		logger.info("[精确]查询查找了xy坐标分别是[{}, {}]", ir.drawX, ir.drawY);
		return ir;
	}
	
	public static int getGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);
        int grayLevel = (r + g + b) / 3;
        return grayLevel;
	}
	
	/**
	 * 将图片转换成灰色
	 * @param img
	 */
	public static void toGray(BufferedImage img) {
		for (int x = 0; x < img.getWidth(); ++x) {
		    for (int y = 0; y < img.getHeight(); ++y) {
		        int rgb = img.getRGB(x, y);
		        int grayLevel = getGray(rgb);
		        int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
		        img.setRGB(x, y, gray);
		    }
		}
	}
	
	public static boolean savePngFile(BufferedImage image, String toPath) {
		BufferedImage imageSaved = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = imageSaved.createGraphics(); 
		imageSaved = g2d.getDeviceConfiguration().createCompatibleImage(imageSaved.getWidth(), imageSaved.getHeight(), Transparency.TRANSLUCENT);  
	    g2d.dispose();  
	    g2d = imageSaved.createGraphics();  
	                  
	    g2d.setColor(new Color(255,0,0));  
	    g2d.setStroke(new BasicStroke(1));  
	                  
	    g2d.setColor(Color.white);  
	    g2d.drawImage(imageSaved, 0, 0, null);  
		
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				int rgb = image.getRGB(i, j);
				imageSaved.setRGB(i, j, rgb);
			}
		}
		File out = new File(toPath);
		if (out.exists()) {
    		out.delete();
    	}
		try {
			ImageIO.write(imageSaved, "png", out);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isImageSuffix(String filename) {
		int index = filename.lastIndexOf(".");
		if (index < 1 || (index + 1) >= filename.length()) {
			return false;
		}
		return imageSuffixs.contains(filename.substring(index + 1)) ? true : false;
	}
	
}
