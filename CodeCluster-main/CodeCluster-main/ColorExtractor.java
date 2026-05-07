import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ColorExtractor {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("title.png"));
        int width = img.getWidth();
        int height = img.getHeight();
        for (int i=1; i<=22; i+=2) {
            int x = width * i / 22;
            int rSum = 0, gSum = 0, bSum = 0;
            int count = 0;
            
            for(int dy=-5; dy<=5; dy++) {
                for(int dx=-5; dx<=5; dx++) {
                    int rgb = img.getRGB(x+dx, height/2 - 20 + dy); 
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    if(r>0 || g>0 || b>0) {
                        rSum += r; gSum += g; bSum += b;
                        count++;
                    }
                }
            }
            System.out.println("new Color(" + (rSum/count) + ", " + (gSum/count) + ", " + (bSum/count) + "),");
        }
    }
}
