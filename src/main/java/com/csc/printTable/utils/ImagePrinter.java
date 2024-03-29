package com.csc.printTable.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.csc.printTable.dto.Template;
import com.freewayso.image.combiner.ImageCombiner;
import com.freewayso.image.combiner.element.TextElement;
import com.freewayso.image.combiner.enums.OutputFormat;
import com.freewayso.image.combiner.enums.ZoomMode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterResolution;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Slf4j
@Component
public class ImagePrinter {
    private static String printRecordPath = System.getProperty("user.dir") + "\\print_test\\%s.png";
    @Autowired
    private Template template;
    private static Template templateStatic;

    //毫米和像素比值
    public static BigDecimal millimeterToPixelRatio = BigDecimal.valueOf(0.08466);

    //毫米和点比值
    public static BigDecimal millimeterToPointRatio = BigDecimal.valueOf(0.35276);
    //点和像素比值
    public static BigDecimal pointToPixelRatio = BigDecimal.valueOf(4.166);

    @PostConstruct
    public void init() {
        templateStatic = template;
    }

    public static void main(String[] args) {
        try {
            float space = 0.1F;
            Color white = new Color(255, 255, 255);
            Color black = new Color(0, 0, 0);
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("Test print");
            PageFormat pageFormat = getPageFormat(printerJob, BigDecimal.valueOf(4.165));
            System.out.println("获取到默认打印机:" + printerJob.getPrintService().getName());
            System.out.println("打印任务名称:" + printerJob.getJobName());
            BigDecimal pt = BigDecimal.valueOf(4.165);
            System.out.println("调试0：" + BigDecimal.valueOf(pageFormat.getWidth()).multiply(pt) + "调试0__：" + BigDecimal.valueOf(pageFormat.getHeight()).multiply(pt));
            System.out.println("调试1：" + BigDecimal.valueOf(pageFormat.getWidth() - pageFormat.getImageableX() * 2).multiply(pt) + "调试1__：" + BigDecimal.valueOf(pageFormat.getHeight() - pageFormat.getImageableY() * 2).multiply(pt));
            System.out.println("调试2：" + BigDecimal.valueOf(pageFormat.getImageableWidth()).multiply(pt).intValue() + "调试2：" + BigDecimal.valueOf(pageFormat.getImageableHeight()).multiply(pt).intValue());
            //A4背景
            BufferedImage backImage = getImage(BigDecimal.valueOf(pageFormat.getImageableWidth()).multiply(pt).intValue(), BigDecimal.valueOf(pageFormat.getImageableHeight()).multiply(pt).intValue(), white);//点 72, 72, 451.19, 697.92   像素300,300,1880,2908
            //模板背景
            BufferedImage templateBackImage = getImage(1594, 969, white);
            //双倍上边
            BufferedImage topDoubleBlock = getImage(1594, 200, new Color(127, 0, 0));
            //单倍上边
            BufferedImage topSingleBlock = getImage(1594, 120, new Color(127, 0, 0));
            //单倍下边
            BufferedImage bottomSingleBlock = getImage(1594, 105, new Color(127, 0, 0));
            templateBackImage = addImage(templateBackImage, 0, 0, topDoubleBlock);
            ImageCombiner combiner = new ImageCombiner(templateBackImage, OutputFormat.PNG);
            combiner.addImageElement(bottomSingleBlock, 0, 870);

            Font fzdx14 = new Font("方正等线", 1, ptToPx(14));
            Font fzdx68 = new Font("方正等线", 1, ptToPx(76));
            combiner.addTextElement("信息技术部", fzdx14, 60, 40).setColor(white);
            combiner.addTextElement("业务综合管理系统开发组（DSS）", fzdx14, 60, 100).setColor(white);
            TextElement textElementName = new TextElement("鹿坦", fzdx68, 0, 0).setSpace(space);
            Integer widthName0 = textElementName.setSpace(0F).getWidth();
            int templateCentral = BigDecimal.valueOf(templateBackImage.getWidth()).multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_DOWN).intValue();
            int nameCentral = BigDecimal.valueOf(widthName0 + widthName0 * space).multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_DOWN).intValue();
            int nameX = templateCentral - nameCentral;
            int nameY = (templateBackImage.getHeight() - topDoubleBlock.getHeight() - bottomSingleBlock.getHeight()) / 2 - textElementName.getHeight() / 2 + topDoubleBlock.getHeight();
            combiner.addTextElement(textElementName.getText(), textElementName.getFont(), nameX, nameY).setColor(black).setSpace(space);

            combiner.combine();
            //模板图
            BufferedImage combinedImage = combiner.getCombinedImage();
            //模板写入纸张背景
            BufferedImage bufferedImage = addImage(backImage, 0, 0, combinedImage);
            bufferedImage = addImage(bufferedImage, 0, 1019, combinedImage);
            bufferedImage = addImage(bufferedImage, 0, 2038, combinedImage);
            //写入缓存
            writerTemp(combinedImage, "");
            writerTemp(bufferedImage, "back");
            //设置打印对象
            printerJob.setPrintable(buidlPrintable(bufferedImage));
            //开始打印
            printerJob.print();
        } catch (PrinterException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writerTemp(BufferedImage combinedImage, String fileName) {
        try {
            File file = new File(new File(printRecordPath).getParent());
            if (!file.exists()) {
                file.mkdirs();
            }
            ImageIO.write(combinedImage, "png", new File(String.format(printRecordPath, fileName + "_" + DateFormatUtils.format(new Date(), "yyyyMMdd_Hhmmss"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static PageFormat getPageFormat(PrinterJob printerJob, BigDecimal imageScale) {
        PageFormat pageFormat = printerJob.defaultPage();
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        //单位毫米
        int pageWidth = templateStatic.getPageWidth();
        int pageHeight = templateStatic.getPageHeight();
        int marginValue = templateStatic.getMargin();
        double width = BigDecimal.valueOf(pageWidth).divide(millimeterToPointRatio, 1, RoundingMode.DOWN).doubleValue();
        double height = BigDecimal.valueOf(pageHeight).divide(millimeterToPointRatio, 1, RoundingMode.DOWN).doubleValue();
        double margin = BigDecimal.valueOf(marginValue).divide(millimeterToPointRatio, 1, RoundingMode.DOWN).doubleValue();
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(margin, margin, width - margin * 2, height - margin * 2);
        pageFormat.setPaper(paper);
        printerJob.setPrintable(null, pageFormat);
        log.info("pageFormat:" + JSON.toJSONString(pageFormat, SerializerFeature.PrettyFormat));
        return pageFormat;
    }

    public static Printable buidlPrintable(final BufferedImage contentImage) {

        return (g, pf, page) -> {
            System.out.println("page:" + page);
            if (page > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            // 获取打印页面的图形上下文
            Graphics2D g2d = (Graphics2D) g;
            // 转换坐标系统以匹配打印页面
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            // 缩放图片以适应页面大小（如果需要）
            double scaleX = pf.getImageableWidth() / contentImage.getWidth();
            System.out.println("pf.getImageableWidth(): " + pf.getImageableWidth() + " | contentImage.getWidth(): " + contentImage.getWidth());
            double scaleY = pf.getImageableHeight() / contentImage.getHeight();
            System.out.println("pf.getImageableHeight(): " + pf.getImageableHeight() + " | contentImage.getHeight(): " + contentImage.getHeight());
            double scale = Math.min(scaleX, scaleY);
            System.out.println("scaleX:" + scaleX);
            System.out.println("scaleY:" + scaleY);
            System.out.println("scale:" + scale);
            System.out.println("Paper:" + JSON.toJSONString(pf.getPaper(), SerializerFeature.PrettyFormat));
            g2d.scale(scale, scale);
            // 绘制图片
            g2d.drawImage(contentImage, 0, 0, null);
            return Printable.PAGE_EXISTS;
        };
    }

    public static class PrintableImpl implements Printable {

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            return 0;
        }
    }


    public static BufferedImage addImage(BufferedImage backImage, int x, int y, BufferedImage bufferedImage) {
        try {
            ImageCombiner combiner = new ImageCombiner(backImage, OutputFormat.PNG);
            combiner.addImageElement(bufferedImage, x, y, bufferedImage.getWidth(), bufferedImage.getHeight(), ZoomMode.Origin);
            combiner.combine();
            return combiner.getCombinedImage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A4:2480, 3508
     * 白色：new Color(255,255,255)
     *
     * @param height
     * @param height
     * @param backColor
     * @return
     */
    public static BufferedImage getImage(int width, int height, Color backColor) {
        try {
            //获取背景色
            //创建模板需要的照片大小的背景图（按照片的背景颜色）
            BufferedImage back = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = back.createGraphics();
            graphics.setColor(backColor);
            graphics.fillRect(0, 0, width, height);
            graphics.dispose();
            return back;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int ptToPx(int pound) {
        return BigDecimal.valueOf(pound).divide(BigDecimal.valueOf(72), 6, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(300)).setScale(0, RoundingMode.HALF_DOWN).intValue();
    }
}