package com.github.guraame.human.senses.eye;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class Eye {

    private static final double DELTA_E_THRESHOLD = 1.51;
    private static final int MAX_DISTANCE = 5;

    public static void main(String[] args) {
        try {
            // 讀取圖像
            BufferedImage image = ImageIO.read(new File("C:\\Users\\NOBTG\\Downloads\\f2af51e713e9571c07011a38c78f61e0.jpg"));

            // 轉換為SVG
            String svg = convertImageToSVG(image);

            // 寫入SVG檔案
            try (FileWriter writer = new FileWriter("output.svg")) {
                writer.write(svg);
            }

            System.out.println("SVG檔案已生成: output.svg");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertImageToSVG(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 建立區塊ID映射
        int[][] regionMap = new int[height][width];
        int nextRegionId = 1;

        // 初始化區塊映射
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                regionMap[y][x] = -1;
            }
        }

        // 使用flood fill算法分割相似顏色區塊
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (regionMap[y][x] == -1) {
                    Color pixelColor = new Color(image.getRGB(x, y));
                    floodFill(image, regionMap, x, y, nextRegionId, pixelColor);
                    nextRegionId++;
                }
            }
        }

        // 將原始圖片轉換為Base64
        String base64Image = imageToBase64(image);

        // 生成SVG
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n", width, height));

        // 添加原始圖片作為底圖
        svg.append(String.format("<image x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" xlink:href=\"data:image/png;base64,%s\"/>\n",
                width, height, base64Image));

        // 繪製邊界線
        Set<String> boundaries = findBoundaries(regionMap, width, height);
        for (String boundary : boundaries) {
            svg.append(boundary);
        }

        svg.append("</svg>");

        return svg.toString();
    }

    private static void floodFill(BufferedImage image, int[][] regionMap, int startX, int startY,
                                  int regionId, Color targetColor) {
        int width = image.getWidth();
        int height = image.getHeight();

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.x;
            int y = p.y;

            if (x < 0 || x >= width || y < 0 || y >= height || regionMap[y][x] != -1) {
                continue;
            }

            Color currentColor = new Color(image.getRGB(x, y));
            if (calculateDeltaE(targetColor, currentColor) > DELTA_E_THRESHOLD) {
                continue;
            }

            regionMap[y][x] = regionId;

            // 檢查相鄰像素
            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }

        // 處理距離限制：移除距離太遠的像素
        cleanupDistantPixels(regionMap, regionId, width, height);
    }

    private static void cleanupDistantPixels(int[][] regionMap, int regionId, int width, int height) {
        boolean[][] toRemove = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (regionMap[y][x] == regionId) {
                    if (!hasNearbyPixels(regionMap, x, y, regionId, width, height)) {
                        toRemove[y][x] = true;
                    }
                }
            }
        }

        // 移除標記的像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (toRemove[y][x]) {
                    regionMap[y][x] = -1;
                }
            }
        }
    }

    private static boolean hasNearbyPixels(int[][] regionMap, int x, int y, int regionId,
                                           int width, int height) {
        int count = 0;
        for (int dy = -MAX_DISTANCE; dy <= MAX_DISTANCE; dy++) {
            for (int dx = -MAX_DISTANCE; dx <= MAX_DISTANCE; dx++) {
                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (regionMap[ny][nx] == regionId) {
                        count++;
                        if (count > 1) return true; // 包括自己本身
                    }
                }
            }
        }
        return false;
    }

    private static Set<String> findBoundaries(int[][] regionMap, int width, int height) {
        Set<String> boundaries = new HashSet<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentRegion = regionMap[y][x];

                // 檢查右邊界
                if (x < width - 1 && regionMap[y][x + 1] != currentRegion) {
                    boundaries.add(String.format(
                            "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"red\" stroke-width=\"1\"/>\n",
                            x + 1, y, x + 1, y + 1));
                }

                // 檢查下邊界
                if (y < height - 1 && regionMap[y + 1][x] != currentRegion) {
                    boundaries.add(String.format(
                            "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"red\" stroke-width=\"1\"/>\n",
                            x, y + 1, x + 1, y + 1));
                }
            }
        }

        return boundaries;
    }

    // 計算Delta E (CIE76)
    private static double calculateDeltaE(Color c1, Color c2) {
        // 轉換RGB到LAB色彩空間
        double[] lab1 = rgbToLab(c1);
        double[] lab2 = rgbToLab(c2);

        // 計算Delta E
        double deltaL = lab1[0] - lab2[0];
        double deltaA = lab1[1] - lab2[1];
        double deltaB = lab1[2] - lab2[2];

        return Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
    }

    // RGB轉LAB色彩空間
    private static double[] rgbToLab(Color color) {
        // 先轉換到XYZ
        double[] xyz = rgbToXyz(color);

        // 再轉換到LAB
        double xn = 95.047;  // D65照明體白點
        double yn = 100.0;
        double zn = 108.883;

        double fx = xyz[0] / xn > 0.008856 ? Math.pow(xyz[0] / xn, 1.0/3.0) : (7.787 * xyz[0] / xn + 16.0/116.0);
        double fy = xyz[1] / yn > 0.008856 ? Math.pow(xyz[1] / yn, 1.0/3.0) : (7.787 * xyz[1] / yn + 16.0/116.0);
        double fz = xyz[2] / zn > 0.008856 ? Math.pow(xyz[2] / zn, 1.0/3.0) : (7.787 * xyz[2] / zn + 16.0/116.0);

        double L = 116.0 * fy - 16.0;
        double a = 500.0 * (fx - fy);
        double b = 200.0 * (fy - fz);

        return new double[]{L, a, b};
    }

    // RGB轉XYZ色彩空間
    private static double[] rgbToXyz(Color color) {
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;

        // Gamma校正
        r = r > 0.04045 ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
        g = g > 0.04045 ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
        b = b > 0.04045 ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;

        r *= 100;
        g *= 100;
        b *= 100;

        // 轉換矩陣 (sRGB to XYZ)
        double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
        double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
        double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

        return new double[]{x, y, z};
    }

    // 將BufferedImage轉換為Base64字符串
    private static String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}