package cn.onedear.image;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author onedear
 * @date Dec 15, 2013 11:00:51 PM 
 */
public class RectManager {
	
	private static Logger logger = LoggerFactory.getLogger(RectManager.class);
	public static List<ImageRect> images = new ArrayList<ImageRect>();
	
	public static int width = 0;
	public static int height = 0;
	public static int area = 0;
	
	public final static float IMG_SCALE = 2;
	
	/**
	 * 此坐标是否已经被占用
	 * @param drawX
	 * @param drawY
	 * @return
	 */
	public static boolean isPointIn(int drawX, int drawY) {
		for (ImageRect image : images) {
			if (drawX >= image.drawX && drawX <= image.drawX + image.image.getWidth()
				&& drawY >= image.drawY && drawY <= image.drawY + image.image.getHeight()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 此区间是否塞得下此矩阵
	 * @param drawX
	 * @param drawY
	 * @param width
	 * @param height
	 * @return
	 */
	public static boolean isFix(int drawX, int drawY, int width, int height) {
//		简单判断方式一
		if (isPointIn(drawX, drawY)) {
			return false;
		}
		if (isPointIn(drawX + width, drawY)) {
			return false;
		}
		if (isPointIn(drawX, drawY + height)) {
			return false;
		}
		if (isPointIn(drawX + width, drawY + height)) {
			return false;
		}
//		遍历判断方式二
//		for (int x = drawX; x < drawX + width; x++) {
//			for (int y = drawY; y < drawY + height; y++) {
//				if (isPointIn(x, y)) {
//					return false;
//				}
//			}
//		}
		return true;
	}
	
	/**
	 * 添加图片
	 * @param image
	 */
	public static void addRect(BufferedImage image) {
		if (images.size() < 1) {
			ImageRect ir = new ImageRect();
			ir.drawX = 0;
			ir.drawY = 0;
			ir.image = image;
			images.add(ir);
			width = image.getWidth();
			height = image.getHeight();
			logger.debug("##初始化第一张图片, 现在的坐标是[{},{}],面积是[{}]", 
					new Object[]{ir.drawX, ir.drawY, width * height});
			return;
		}
		
		int step = 1;
		MaxRect maxRect = MaxRect.newMaxRect();
		maxRect.image = image;
		
		for (int x = 0; x < width + 5; x += step) {
			for (int y = 0; y < height + 5; y += step) {
				if(!isFix(x, y, image.getWidth(), image.getHeight())) {
					continue;
				}
				
				int maxX = (x + image.getWidth()) < width ? width : (x + image.getWidth());
				int maxY = (y + image.getHeight()) < height ? height : (y + image.getHeight());
				if ((maxX / maxY) >= IMG_SCALE || (maxX / maxY) <= (1/IMG_SCALE)) {
					continue;
				}
				area = maxX * maxY;
				if (area < maxRect.area) {
					logger.debug("##已有图片张数是[{}]张, 之前的坐标是[{},{}],最大面积是[{}], 现在的坐标是[{},{}],面积是[{}], 当前矩阵的体积是[{}]", 
							new Object[]{images.size(), maxRect.drawX, maxRect.drawY, maxRect.area, x, y, area, width * height});
					maxRect.area = area;
					maxRect.drawX = x;
					maxRect.drawY = y;
				}
			}
		}
		if (maxRect.drawX == 0 && maxRect.drawY == 0) {
//			说明怎么弄都不对, 直接塞到短的那一边
			if (width / height > IMG_SCALE) {
				maxRect.drawY = height + 1;
			} else {
				maxRect.drawX = width + 1;
			}
		}
		ImageRect ir = new ImageRect();
		ir.drawX = maxRect.drawX;
		ir.drawY = maxRect.drawY;
		ir.image = maxRect.image;
		images.add(ir);
		updateXY();
	}
	
	/**
	 * 更新当前的容器信息
	 */
	public static void updateXY() {
		int maxX = 0;
		int maxY = 0;
		for (int i = 0; i < images.size(); i++) {
			ImageRect image = images.get(i);
			if (maxX < (image.drawX + image.image.getWidth())) {
				maxX = image.drawX + image.image.getWidth();
			}
			if (maxY < (image.drawY + image.image.getHeight())) {
				maxY = image.drawY + image.image.getHeight();
			}
		}
		width = maxX;
		height = maxY;
	}
	
	private static BufferedImage findMax(List<BufferedImage> images) {
		BufferedImage max = null;
		if (images == null) {
			return null;
		}
		if (images.size() < 2) {
			images.get(0);
		}
		max = images.get(0);
		for (int i = 1; i < images.size(); i++) {
			BufferedImage image = images.get(i);
			if (max.getWidth() * max.getHeight() < image.getWidth() * image.getHeight()) {
				max = image;
			}
		}
		return max;
	}
	
	public static BufferedImage merge(List<BufferedImage> images) {
		logger.info("##开始分析图片, 共{}张", images.size());
		for (BufferedImage image : images) {
			logger.info("图片的大小分别是, x[{}], y[{}]", image.getWidth(), image.getHeight());
		}
		logger.info("##分析完毕.");
		
//		体积大的先放好
		List<BufferedImage> newImages = new ArrayList<BufferedImage>(images.size());
		newImages.addAll(images);
		
		while(newImages.size() > 0) {
			BufferedImage maxImage = findMax(newImages);
			logger.info("##体积最大的是, x[{}], y[{}]", maxImage.getWidth(), maxImage.getHeight());
			RectManager.addRect(maxImage);
			newImages.remove(maxImage);
		}
		
//		draw
		logger.info("######### 开始画图 ##########");
		for (ImageRect image : RectManager.images) {
			logger.info("具体图片大小是:width[{}], heigth[{}], 位置 drawX[{}], drawY[{}]", new Object[]{RectManager.width, 
					RectManager.height, image.drawX, image.drawY});
		}
		
		BufferedImage imageSaved = new BufferedImage(RectManager.width, RectManager.height, BufferedImage.TYPE_INT_RGB);
	    logger.info("##生成画布,长宽分别是[{},{}]", RectManager.width, RectManager.height);
	    Graphics2D g2d = imageSaved.createGraphics();  
	    imageSaved = g2d.getDeviceConfiguration().createCompatibleImage(imageSaved.getWidth(), imageSaved.getHeight(), Transparency.TRANSLUCENT);  
	    g2d.dispose();  
	    g2d = imageSaved.createGraphics();  
	    g2d.setStroke(new BasicStroke(1));  
	    g2d.drawImage(imageSaved, 0, 0, null);  
		
		for (ImageRect image : RectManager.images) {
			for (int i = 0; i < image.image.getWidth(); i++) {
				for (int j = 0; j < image.image.getHeight(); j++) {
					int rgb = image.image.getRGB(i, j);
					imageSaved.setRGB(i + image.drawX, j + image.drawY, rgb);
				}
			}
		}
		return imageSaved;
	}
	
}

/**
 * 临时用到的一个最大矩阵类
 * @author onedear
 * @date Jan 15, 2014 4:28:00 PM
 */
class MaxRect {
	public int area;
	public int drawX;
	public int drawY;
	public BufferedImage image;
	public static MaxRect newMaxRect() {
		MaxRect mr = new MaxRect();
		mr.area = Integer.MAX_VALUE;
		mr.drawX = 0;
		mr.drawY = 0;
		return mr;
	}
}	