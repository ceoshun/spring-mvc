package com.ngn.mvc.util;

import javax.imageio.ImageIO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @Description:RedrawGridGraph
 * @Author: wuyunchen
 * @Create: 2018/1/9
 **/

public class RedrawGridGraph {
    private double leftLongitude;
    private double leftLatitude;
    private double rightLongitude;
    private double rightLatitude;
    //private String color;
    private JSONArray dataArray;
    private int radius;
    private int textSize;
    public BufferedImage redrawGridGraph(String path, JSONObject data) throws IOException {
        analyzeJsonData(data);
        File file = new File(path);
        BufferedImage bufferedImage = ImageIO.read(file);
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        double latitude,longitude;
        String color;
        String text;
        Graphics2D graphics = bufferedImage.createGraphics();
        double latitudeRate = (rightLatitude - leftLatitude)/imageHeight;
        double longitudeRate = (rightLongitude - leftLongitude)/imageWidth;

        graphics.setStroke(new BasicStroke(1.0f));
        for (int i = 0;i<dataArray.size();i++){
            JSONObject jsonObject = dataArray.getJSONObject(i);
            latitude = jsonObject.getDouble("latitude");
            longitude = jsonObject.getDouble("longitude");
            text = jsonObject.getString("text");
            color = jsonObject.getString("color").substring(1);

            graphics.setColor(new Color(Integer.parseInt(color, 16)));
            int y = (int)Math.round((latitude - leftLatitude)/latitudeRate);
            int x = (int)Math.round((longitude - leftLongitude)/longitudeRate);
            graphics.fillOval(x,y,radius,radius);
            Font font = new Font("TimesRoman", Font.PLAIN,textSize);
            graphics.setFont(font);
            FontRenderContext context = graphics.getFontRenderContext();
            Rectangle2D stringBounds = font.getStringBounds(text, context);
            double fontWidth = stringBounds.getWidth();
            if (x+radius+fontWidth>imageWidth){
                graphics.drawString(text, (int) (x - radius - fontWidth), y + textSize / 2);
            } else if ((y - textSize / 2)<0){
                graphics.drawString(text, x, y + textSize + radius);
            } else if ((y + textSize / 2)>imageHeight){
                graphics.drawString(text, x, y);
            }else{
                graphics.drawString(text, x + radius, y + textSize / 2);
            }

        }
        graphics.dispose();
        //String fileType = path.substring(path.lastIndexOf(".")+1,path.length());
        //ImageIO.write(bufferedImage, fileType, file);
        return bufferedImage;
        //ImageIO.write(bufferedImage, fileType, file);
    }
    private void analyzeJsonData(JSONObject data){
        leftLongitude = data.getDouble("leftLongitude");
        leftLatitude = data.getDouble("leftLatitude");

        rightLongitude = data.getDouble("rightLongitude");
        rightLatitude = data.getDouble("rightLatitude");

        dataArray = data.getJSONArray("data");

        radius = data.getInteger("radius");

        textSize = data.getInteger("textSize");
    }
}
