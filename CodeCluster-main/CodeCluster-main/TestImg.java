import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TestImg {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("title.png"));
        System.out.println("Has Alpha: " + img.getColorModel().hasAlpha());
        System.out.println("Transparency: " + img.getColorModel().getTransparency());
        int argb = img.getRGB(0, 0);
        int alpha = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        System.out.println("Top left pixel - A:" + alpha + " R:" + r + " G:" + g + " B:" + b);
    }
}
