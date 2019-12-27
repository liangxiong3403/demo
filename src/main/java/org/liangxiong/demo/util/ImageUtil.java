package org.liangxiong.demo.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author liangxiong
 * @version 1.0.0
 * @date 2019-12-19
 * @description 图片相关操作
 **/
public class ImageUtil {

    private ImageUtil() {

    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        String sourceFile = "C:/Users/Administrator/Desktop/a.png";
        String targetFile = "C:/Users/Administrator/Desktop/2_142x142.png";
        // 图片裁剪为圆形
        changeRectangleToRound("C:/Users/Administrator/Desktop/demo.jpg", "C:/Users/Administrator/Desktop/a.png", "png", 1500);
        long end = System.currentTimeMillis();
        System.out.println("changeRectangleToRound time cost: " + (end - start));

        start = System.currentTimeMillis();
        // 生成缩略图
        changeToThumbImage(sourceFile, targetFile, StringUtils.getFilenameExtension(targetFile), 200, 200);
        end = System.currentTimeMillis();
        System.out.println("changeToThumbImage time cost: " + (end - start));

        start = System.currentTimeMillis();
        // 图片添加水印
        addWaterMarkForImage("C:/Users/Administrator/Desktop/1.jpg", "C:/Users/Administrator/Desktop/image_watermark_bottom_right.jpg", "C:/Users/Administrator/Desktop/2_142x142.png", 1280, 1000, 90, 50);
        end = System.currentTimeMillis();
        System.out.println("addWaterMarkForImage time cost: " + (end - start));

        start = System.currentTimeMillis();
        // 图片添加文字
        addTextToImage("C:/Users/Administrator/Desktop/image_watermark_bottom_right.jpg", "BKVITO", "Comic Sans MS", Font.BOLD, 30, Color.BLUE, 500, 200, 1f);
        end = System.currentTimeMillis();
        System.out.println("addTextToImage time cost: " + (end - start));

        start = System.currentTimeMillis();
        // 转换图片格式
        changeImageType("C:/Users/Administrator/Desktop/gray2-c.png", "C:/Users/Administrator/Desktop/gray2-d.jpg", "jpg");
        end = System.currentTimeMillis();
        System.out.println("changeImageType time cost: " + (end - start));

        start = System.currentTimeMillis();
        // 图片直角边框裁剪为圆角边框
        roundImageCorner("C:/Users/Administrator/Desktop/gray2.jpg", "C:/Users/Administrator/Desktop/gray1-r.jpg", 400, 283, 40);
        end = System.currentTimeMillis();
        System.out.println("roundImageCorner time cost: " + (end - start));
    }

    /**
     * 长方形图片裁剪为圆形图片
     *
     * @param sourceFile      输入文件
     * @param targetFile      输出文件
     * @param targetImageType 图片类型
     * @param cornerRadius    圆角弧度
     */
    public static String changeRectangleToRound(String sourceFile, String targetFile, String targetImageType, int cornerRadius) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(sourceFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            // 参数imageType控制灰度处理
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = output.createGraphics();
            // 宽度,高度,透明度
            output = g2.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.BITMASK);
            // 便于垃圾回收
            g2.dispose();
            g2 = output.createGraphics();
            // 渲染时间和质量平衡配置
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int maxValue = Integer.max(width, height);
            g2.fillRoundRect(0, 0, width, height, maxValue, maxValue);
            g2.setComposite(AlphaComposite.SrcIn);

            //这里绘画原型图
            Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, width, height);
            g2.setClip(shape);

            g2.drawImage(image, 0, 0, width, height, null);
            g2.dispose();
            try {
                ImageIO.write(output, targetImageType, new File(targetFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return targetFile;
        }
        return null;
    }

    /**
     * 生成缩略图
     *
     * @param width  宽度
     * @param height 高度
     */
    public static void changeToThumbImage(String sourceFile, String targetFile, String targetImageType, int width, int height) {
        try {
            Thumbnails
                    // 输入源
                    .of(sourceFile)
                    // 强制调整长宽比
                    .size(width, height).keepAspectRatio(false)
                    // 顺时针旋转角度
                    .rotate(0)
                    // 压缩质量
                    .outputQuality(0.8f)
                    // 转换格式
                    .outputFormat(targetImageType)
                    // 输出到文件
                    .toFile(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转换图片格式
     *
     * @param sourceFile      输入图片
     * @param targetFile      输出图片
     * @param targetImageType 格式
     */
    public static void changeImageType(String sourceFile, String targetFile, String targetImageType) {
        try {
            Thumbnails.of(sourceFile)
                    // 操作中心点位
                    //.sourceRegion()
                    .scale(1).outputFormat(targetImageType).toFile(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片直角边框变圆角边框
     *
     * @param sourceFile   原始图片
     * @param targetFile   输出图片
     * @param cornerRadius 圆角弧度
     * @param targetWidth  缩放宽度
     * @param targetHeight 缩放高度
     * @return
     */
    public static void roundImageCorner(String sourceFile, String targetFile, int targetWidth, int targetHeight, int cornerRadius) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(sourceFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image != null) {
            // 先造一张空白大小的正方形白色图片
            BufferedImage whiteImage = buildWhiteImage(targetWidth, targetHeight, image.getType());
            BufferedImage image11;
            BufferedImage image111;
            try {
                image11 = Thumbnails.of(image).width(targetWidth).height(targetHeight).outputQuality(1).asBufferedImage();
                image111 = Thumbnails.of(image11).sourceRegion(Positions.CENTER, image11.getWidth(), 570).scale(1).outputQuality(1).asBufferedImage();
                //第一张图 切圆角
                BufferedImage roundImage = roundImageCorner(image111, image111.getWidth(), image111.getHeight(), cornerRadius);
                Thumbnails.of(whiteImage).watermark((int enclosingWidth,
                                                     int enclosingHeight,
                                                     int w,
                                                     int h,
                                                     int insetLeft,
                                                     int insetRight,
                                                     int insetTop,
                                                     int insetBottom) -> {
                    return new Point(0, 0);
                }, roundImage, 1).scale(1).outputQuality(1).toFile(targetFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 构建白底图片
     *
     * @param width     宽度
     * @param height    高度
     * @param imageType 图片类型
     * @return
     */
    private static BufferedImage buildWhiteImage(int width,
                                                 int height,
                                                 int imageType) {
        // 方式一
        //ColorModel cm = ColorModel.getRGBdefault();
        //WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
        //BufferedImage bufferedImage = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
        // 方式二
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        // 设置笔刷白色
        graphics.setColor(Color.WHITE);
        // 填充整个屏幕
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        return bufferedImage;
    }

    /**
     * 图片直角边框变圆角边框
     *
     * @param image        待处理图片
     * @param width        宽度
     * @param height       高度
     * @param cornerRadius 圆角弧度
     * @return
     */
    private static BufferedImage roundImageCorner(BufferedImage image, int width, int height, int cornerRadius) {
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = outputImage.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return outputImage;
    }


    /**
     * 为图片新增水印
     */
    public static void addWaterMarkForImage(String sourceFile, String targetFile, String waterMarkFile, int width, int height, int x, int y) {
        //把生成的圆形图片，当水印贴到背景图中，ab为圆形图片应该到背景图的x轴y轴的坐标        
        try {
            Thumbnails.of(sourceFile)
                    .size(width, height).keepAspectRatio(false)
                    // 水印(操作坐标点位,水印图片,水印不透明度)
                    .watermark((int enclosingWidth, int enclosingHeight, int w, int h, int insetLeft, int insetRight, int insetTop, int insetBottom) -> new Point(x, y),
                            ImageIO.read(new File(waterMarkFile)), 1f)
                    .outputQuality(0.8f).toFile(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  
     * 添加文字水印  
     *
     * @param sourceFile 目标图片路径
     * @param text       水印文字
     * @param fontName   字体名称，    如：宋体  
     * @param fontStyle  字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC)  
     * @param fontSize   字体大小，单位为像素  
     * @param color      字体颜色  
     * @param x          水印文字距离目标图片左侧的偏移量，如果x<0, 则在正中间  
     * @param y          水印文字距离目标图片上侧的偏移量，如果y<0, 则在正中间  
     * @param alpha      透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)  
     */
    public static void addTextToImage(String sourceFile, String text, String fontName, int fontStyle, int fontSize, Color color, int x, int y, float alpha) {
        try {
            File file = new File(sourceFile);
            Image image = ImageIO.read(file);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setColor(color);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            int width_wi = fontSize * getTextLength(text);
            int height_wi = fontSize;

            int widthDiff = width - width_wi;
            int heightDiff = height - height_wi;
            if (x < 0) {
                x = widthDiff / 2;
            } else if (x > widthDiff) {
                x = widthDiff;
            }
            if (y < 0) {
                y = heightDiff / 2;
            } else if (y > heightDiff) {
                y = heightDiff;
            }
            g.drawString(text, x, y + height_wi);//水印文件  
            g.dispose();
            ImageIO.write(bufferedImage, "JPEG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算文字像素长度 
     *
     * @param text  输入文本
     * @return 
     */
    private static int getTextLength(String text) {
        int textLength = text.length();
        int length = textLength;
        for (int i = 0; i < textLength; i++) {
            int wordLength = String.valueOf(text.charAt(i)).getBytes().length;
            if (wordLength > 1) {
                length += (wordLength - 1);
            }
        }
        return length % 2 == 0 ? length / 2 : length / 2 + 1;
    }
}
