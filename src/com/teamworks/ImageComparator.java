package com.teamworks;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav Karpenko
 * Date: 12.03.2015
 * Time: 10:47
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageComparator {
    private final int THRESHOLD = 4;
    private Color borderColor = new Color(0xff0000);
    private Color areaColor = new Color(0xffff00);
    private BufferedImage imageData1;
    private BufferedImage imageData2;
    private BufferedImage result;
    private List<RectangleArea> rectangleAreas;

    public void loadImages(String file1, String file2) {
        imageData1 = readImage(file1);
        imageData2 = readImage(file2);
    }

    private BufferedImage readImage(String fileName) {
        BufferedImage img = null;
        File file = new File(fileName);
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("File not exist: " + fileName);
        }
        return img;
    }

    public boolean isDataValid() {
        if (imageData1 == null || imageData2 == null) {
            return false;
        }
        if (imageData1.getHeight() != imageData2.getHeight() || imageData1.getWidth() != imageData2.getWidth()) {
            System.out.println("Images have different high or width");
            return false;
        }
        return true;
    }

    public void compare() {
        if (!isDataValid()) {
            return;
        }

        findAndFillDifferentAreas();
        rectangleAreas = getRectanglesList();
        drawRectangles(imageData2);
    }

    public void drawRectangles(BufferedImage bufferedImage) {
        for (RectangleArea rectangleArea : rectangleAreas) {
            Graphics2D graph = bufferedImage.createGraphics();
            graph.setColor(borderColor);
            graph.drawRect(rectangleArea.getStartX(), rectangleArea.getStartY(), rectangleArea.getWidth(), rectangleArea.getHigh());

        }
    }

    private RectangleArea findRectangleArea(int startX, int startY) {
        RectangleArea rectangleArea = new RectangleArea(startX, startX, startY, startY);
        boolean isFinished = false;
        int goX = startX;
        int goY = startY;
        int width = result.getWidth();
        int high = result.getHeight();


        while (!isFinished) {
            result.setRGB(goX, goY, borderColor.getRGB());

            if (goX == startX && (goY - 1) == startY && result.getRGB(goX, goY) == borderColor.getRGB()) {
                isFinished = true;
                continue;
            }

            if ((goX + 1) == startX && (goY - 1) == startY && result.getRGB(goX, goY) == borderColor.getRGB()) {
                isFinished = true;
                continue;
            }

            //go right
            if ((goX + 1) < width && result.getRGB(goX + 1, goY) == areaColor.getRGB() && (goY == 0 || result.getRGB(goX, goY - 1) != areaColor.getRGB())) {
                goX++;
                rectangleArea.setPosition(goX, goY);
                continue;
            }

            //go down
            if ((goY + 1) < high && result.getRGB(goX, goY + 1) == areaColor.getRGB() && ((goX + 1) == width || result.getRGB(goX + 1, goY) != areaColor.getRGB())) {
                goY++;
                rectangleArea.setPosition(goX, goY);
                continue;
            }

            //go left
            if (goX > 0 && result.getRGB(goX - 1, goY) == areaColor.getRGB() && ((goY + 1) == high || result.getRGB(goX, goY + 1) != areaColor.getRGB())) {
                goX--;
                rectangleArea.setPosition(goX, goY);
                continue;
            }

            //go up
            if (goY > 0 && result.getRGB(goX, goY - 1) == areaColor.getRGB() && (goX == 0 || result.getRGB(goX - 1, goY) != areaColor.getRGB())) {
                goY--;
                rectangleArea.setPosition(goX, goY);
                continue;
            }

            isFinished = true;
        }

        return rectangleArea;
    }

    private List<RectangleArea> getRectanglesList() {
        List<RectangleArea> rectangleAreaList = new ArrayList<RectangleArea>();
        int width = result.getWidth();
        int high = result.getHeight();

        for (int y = 0; y < high; y++) {
            for (int x = 0; x < width; x++) {
                int rgbPixel = result.getRGB(x, y);

                //find is area already marked
                if (rgbPixel == borderColor.getRGB()) {
                    for (int dx = x +1; dx < width; dx++) {
                        if (result.getRGB(dx, y) == borderColor.getRGB() && ((dx+1)==width || result.getRGB(dx+1, y) != borderColor.getRGB()&&result.getRGB(dx+1, y) != areaColor.getRGB())) {
                            x = dx;
                            break;
                        }
                    }
                }

                //find non marked area
                if (rgbPixel == areaColor.getRGB()) {
                    RectangleArea rectangleArea = findRectangleArea(x, y);
                    rectangleAreaList.add(rectangleArea);
                    x = rectangleArea.getEndX() + 1;
                }
            }
        }
        return rectangleAreaList;
    }


    private void findAndFillDifferentAreas() {
        int width = imageData1.getWidth();
        int high = imageData1.getHeight();

        result = new BufferedImage(width, high, imageData1.getType());

        for (int y = 0; y < high; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgb1 = imageData1.getRaster().getPixel(x, y, new int[3]);
                int[] rgb2 = imageData2.getRaster().getPixel(x, y, new int[3]);
                double colorVector1 = Math.pow(Math.pow(rgb1[0], 3) + Math.pow(rgb1[1], 3) + Math.pow(rgb1[2], 3), 1 / 3d);
                double colorVector2 = Math.pow(Math.pow(rgb2[0], 3) + Math.pow(rgb2[1], 3) + Math.pow(rgb2[2], 3), 1 / 3d);
                double maxColorVector = Math.pow(Math.pow(255, 3) + Math.pow(255, 3) + Math.pow(255, 3), 1 / 3d);
                double difference = Math.abs(colorVector2 - colorVector1) / maxColorVector;

                if (difference > 0.1d) {
                    fillArea(result, x, y);
                }
            }
        }
    }

    private void fillArea(BufferedImage image, int xPosition, int yPosition) {
        int startX = xPosition - THRESHOLD;
        if (startX < 0) {
            startX = 0;
        }
        int endX = xPosition + THRESHOLD;
        if (endX > image.getWidth()) {
            endX = image.getWidth();
        }
        int startY = yPosition - THRESHOLD;
        if (startY < 0) {
            startY = 0;
        }
        int endY = yPosition + THRESHOLD;
        if (endY > image.getHeight()) {
            endY = image.getHeight();
        }

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                image.setRGB(x, y, areaColor.getRGB());
            }
        }
    }

    public void saveResult() {
        if (imageData2 == null) {
            return;
        }
        try {
            File outputFile = new File("result.bmp");
            ImageIO.write(imageData2, "png", outputFile);
        } catch (IOException e) {
            System.out.println("error writing file");
        }
    }

    private static class RectangleArea {
        private int startX;
        private int endX;
        private int startY;
        private int endY;

        public RectangleArea(int startX, int endX, int startY, int endY) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
        }

        public int getStartX() {
            return startX;
        }

        public int getEndX() {
            return endX;
        }

        public int getStartY() {
            return startY;
        }

        public void setPosition(int x, int y) {

            if (startX >= x) {
                startX = x;
            } else if (endX <= x) {
                endX = x;
            }
            if (startY >= y) {
                startY = y;
            } else if (endY <= y) {
                endY = y;
            }
        }

        public int getWidth() {
            return endX - startX;
        }

        public int getHigh() {
            return endY - startY;
        }
    }
}