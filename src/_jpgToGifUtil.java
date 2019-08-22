
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * 多张jpg图片合成一个gif
 * 
 * @author colorbin 创建时间: 2017年4月8日 上午11:01:19
 */
public class _jpgToGifUtil {
	public static void main(String[] args) {
		String[] pic = { "C:\\Users\\Administrator\\Desktop\\gif\\rank_yun1.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun2.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun3.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun4.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun5.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun6.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun7.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun8.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun9.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun10.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun11.jpg",
				"C:\\Users\\Administrator\\Desktop\\gif\\rank_yun12.jpg" };
		String newwPic = "D://1.gif";
		int playTime = 200;
		jpgToGif(pic, newwPic, playTime);
	}

	/**
	 * 把多张jpg图片合成一张
	 * 
	 * @param pic      String[] 多个jpg文件名 包含路径
	 * @param newPic   String 生成的gif文件名 包含路径
	 * @param playTime int 播放的延迟时间
	 */
	private synchronized static void jpgToGif(String pic[], String newPic, int playTime) {
		try {
			AnimatedGifEncoder e = new AnimatedGifEncoder();
			e.setRepeat(0);
			e.start(newPic);
			BufferedImage src[] = new BufferedImage[pic.length];
			for (int i = 0; i < src.length; i++) {
				e.setDelay(playTime); // 设置播放的延迟时间
				src[i] = ImageIO.read(new File(pic[i])); // 读入需要播放的jpg文件
				e.addFrame(src[i]); // 添加到帧中
			}
			e.finish();
		} catch (Exception e) {
			System.out.println("jpgToGif Failed:");
			e.printStackTrace();
		}
	}

}
