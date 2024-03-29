package com.csc.printTable.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.csc.printTable.dto.Block;
import com.csc.printTable.dto.PrintDto;
import com.csc.printTable.dto.ResponseDto;
import com.csc.printTable.dto.Template;
import com.freewayso.image.combiner.ImageCombiner;
import com.freewayso.image.combiner.element.TextElement;
import com.freewayso.image.combiner.enums.OutputFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static com.csc.printTable.utils.ImagePrinter.*;


@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("api")
public class PrintController {
    private static Color WHITE = new Color(255, 255, 255);
    @Value("#{T(java.lang.Boolean).parseBoolean('${usePrintConfig:true}')}")
    private Boolean usePrintConfig;
    @Autowired
    private Template template;
    @Value("${imageScale}")
    private BigDecimal imageScale;

    @PostMapping("print")
    public Object print(@RequestBody List<PrintDto> params) {

        try {
            checkParam(params);
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            PageFormat pageFormat = getPageFormat(printerJob, imageScale);
            log.info("获取到默认打印机:{}", printerJob.getPrintService().getName());
            log.info("打印任务名称:{}", printerJob.getJobName());
            //构建纸张背景图
            int backW = BigDecimal.valueOf(pageFormat.getImageableWidth()).multiply(imageScale).intValue();
            int backH = BigDecimal.valueOf(pageFormat.getImageableHeight()).multiply(imageScale).intValue();

            int templateH = BigDecimal.valueOf(template.getTemplateHeight()).divide(millimeterToPixelRatio, 0, RoundingMode.DOWN).intValue();
            int tH = templateH + template.getTemplateSpace();
            int count = backH / tH;
            BufferedImage backImage = getImage(backW, backH, WHITE);
            List<List<PrintDto>> partition = ListUtils.partition(params, count);
            for (List<PrintDto> printDtos : partition) {
                String names = printDtos.stream().map(o -> o.getUserName()).collect(Collectors.joining(","));
                printerJob.setJobName("[" + names + "]" + " - 工位牌打印");
                ImageCombiner combiner = new ImageCombiner(backImage, OutputFormat.PNG);
                int c = 0;
                for (PrintDto printDto : printDtos) {
                    BufferedImage combine = getTemplateImage(printDto);
                    //工牌内容与纸张背景合成
                    combiner.addImageElement(combine, 0, c++ * tH);
                    //写入缓存
                    writerTemp(combine, printDto.getUserName());
                }
                BufferedImage combinedImage = combiner.combine();
                //写入缓存
                writerTemp(combinedImage, names);
                //设置打印内容
                printerJob.setPrintable(buidlPrintable(combinedImage));
                try {
                    printerJob.print();//打印
                } catch (PrinterException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception ex) {
            log.error("打印异常：{}", ex.getMessage(), ex);
            return ResponseDto.fail(ex.getMessage());
        }
        return ResponseDto.success();
    }

    private BufferedImage getTemplateImage(PrintDto param) {
        //构建模板背景图
        int templateW = BigDecimal.valueOf(template.getTemplateWidth()).divide(millimeterToPixelRatio, 0, RoundingMode.DOWN).intValue();
        int templateH = BigDecimal.valueOf(template.getTemplateHeight()).divide(millimeterToPixelRatio, 0, RoundingMode.DOWN).intValue();
        BufferedImage templateBackImage = getImage(templateW, templateH, WHITE);
        Font deptFont = new Font("方正等线", template.getDeptName().getStyle(), ptToPx(template.getDeptName().getSize()));
        Font nameFont = new Font("方正等线", template.getUserName().getStyle(), ptToPx(template.getUserName().getSize()));

        Block topBlock = template.getTopDouble();
        //头部色条
        BufferedImage topImage = getImage(topBlock.getWidth(), topBlock.getHeight(), topBlock.getColorBean());

        Block bottomBlock = template.getBottomSingle();
        //底部色条
        BufferedImage bottomImage = getImage(bottomBlock.getWidth(), bottomBlock.getHeight(), bottomBlock.getColorBean());

        ImageCombiner combiner = new ImageCombiner(templateBackImage, OutputFormat.PNG);
        //模板底图
        combiner.addImageElement(templateBackImage, 0, 0);
        //顶边色条
        combiner.addImageElement(topImage, topBlock.getX(), topBlock.getY());
        //底边色条
        combiner.addImageElement(bottomImage, bottomBlock.getX(), bottomBlock.getY());

        //部门元素临时变量
        TextElement tempElement = new TextElement(param.getDeptNameTemp(), deptFont, 0, 0);
        boolean deptDouble = BigDecimal.valueOf(tempElement.getWidth()).divide(BigDecimal.valueOf(topBlock.getWidth()),2,RoundingMode.HALF_UP).doubleValue() >= 0.66;
        int deptAutoHeight = getAutoPoint(topBlock.getHeight(), tempElement.getHeight());

        //部门多行显示
        if (deptDouble) {
            Block deptNameBlock = template.getDeptName();
            //一级部门
            combiner.addTextElement(param.getDeptName(), deptFont, deptNameBlock.getX(), deptNameBlock.getY()).setColor(deptNameBlock.getColorBean());
            //二级部门
            Block deptNameBlock2 = template.getDeptName2();
            combiner.addTextElement(param.getDeptName2(), deptFont, deptNameBlock2.getX(), deptNameBlock2.getY()).setColor(deptNameBlock2.getColorBean());
        }else{
            //部门单行显示
            Block deptNameBlock = template.getDeptName();
            combiner.addTextElement(param.getDeptNameTemp(), deptFont, deptNameBlock.getX(), deptAutoHeight).setColor(deptNameBlock.getColorBean());
        }

        Block userNameBlock = template.getUserName();
        TextElement textElementName = new TextElement(param.getUserName(), nameFont, 0, 0).setSpace(userNameBlock.getSpace());
        //名字宽度
        Integer widthName = textElementName.setSpace(0F).getWidth();
        //名字X坐标
        int nameX = getAutoPoint(templateBackImage.getWidth(),Float.valueOf(widthName + widthName * userNameBlock.getSpace()).intValue());
        //名字Y坐标
        int nameY = getAutoPoint(templateBackImage.getHeight() - topBlock.getHeight() - bottomBlock.getHeight(),textElementName.getHeight())+ topBlock.getHeight();
        combiner.addTextElement(param.getUserName(), nameFont, nameX, nameY).setColor(userNameBlock.getColorBean()).setSpace(userNameBlock.getSpace());
        try {
            //合成模板内容
            return combiner.combine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getAutoPoint(int overallLength , int localLength) {
        //总长度一半
        int templateCentral = BigDecimal.valueOf(overallLength).multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_DOWN).intValue();
        int nameCentral = BigDecimal.valueOf(localLength).multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_DOWN).intValue();
        return templateCentral - nameCentral;
    }

    private void checkParam(List<PrintDto> params) {
        log.info("接收到参数：{}", JSON.toJSONString(params, SerializerFeature.PrettyFormat));
        Assert.isTrue(params != null && params.size() != 0, "参数不能为空");
        for (PrintDto param : params) {
            Assert.notNull(param, "部门和姓名不能为空");
            Assert.hasLength(param.getUserName(), "姓名不能为空");
            Assert.hasLength(param.getDeptName(), "部门不能为空");
            String[] split = param.getDeptName().split("/");
            if (split.length >= 2) {
                param.setDeptName(split[split.length - 1]);
                param.setDeptName2(split[0]);
            }
        }
    }

}
