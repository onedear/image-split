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
 * @date Dec 15, 2013 8:33:16 PM 
 */
public class Main {
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void merge() throws IOException {
		String userDir = System.getProperty("user.dir");
    	String path = userDir + "/images";
    	String out = userDir + "/out/merge.png";
    	long start = System.currentTimeMillis();
    	
    	File dir = new File(path);
    	if(!dir.isDirectory()) {
    		throw new RuntimeException("路径不对");
    	}
    	File[] images = dir.listFiles();
    	if (images == null) {
    		throw new RuntimeException("目标路径没有图片");
    	}
    	List<BufferedImage> bufferedImages = new ArrayList<BufferedImage>(images.length);
    	for (int i = 0; i < images.length; i++) {
    		File image = images[i];
    		if (!ImageUtil.isImageSuffix(image.getName())) {
    			continue;
    		}
    		bufferedImages.add(ImageIO.read(image));
    	}
    	BufferedImage mergeImage = RectManager.merge(bufferedImages);
    	
    	ImageUtil.savePngFile(mergeImage, out);
    	
    	long end = System.currentTimeMillis();
    	logger.debug("共消耗{}ms", (end - start));
	}
	
    public static void findPoint() throws IOException {
    	String userDir = System.getProperty("user.dir");
		String sourceStr = userDir + "/match/source.png";
    	String targetStr = userDir + "/match/target1.png";
		
    	BufferedImage source = ImageIO.read(new File(sourceStr));
    	BufferedImage target = ImageIO.read(new File(targetStr));
    	long start = System.currentTimeMillis();
    	ImageRect ir = ImageUtil.findPoint(source, target);
    	logger.info("最终的坐标是[{},{}], 花费了{}秒", new Object[]{ir.drawX, ir.drawY, System.currentTimeMillis() - start});
    }
    
    public static void main(String[] args) throws IOException {
    	merge(); //合并图片
//    	findPoint(); //找坐标
	}
}
