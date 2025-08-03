package com.github.guraame.human.senses.eye;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class Eye {

    static {
        Loader.load(opencv_java.class);
    }

    public static void main(String[] args) {
        try {
            // 讀取圖像 (請替換為您的圖片路徑)
            String imagePath = "C:\\Users\\NOBTG\\Downloads\\f2af51e713e9571c07011a38c78f61e0.jpg";
            File inputFile = new File(imagePath);
            if (!inputFile.exists()) {
                System.err.println("錯誤：找不到輸入圖片檔案 " + inputFile.getPath());
                return;
            }

            // 使用 OpenCV 讀取圖片到 Mat 物件
            Mat sourceImage = Imgcodecs.imread(imagePath);
            if (sourceImage.empty()) {
                System.err.println("錯誤：無法使用 OpenCV 讀取圖片。");
                return;
            }

            // 處理圖像並生成SVG (使用 DE2000 優化版本)
            String svg = convertImageToSVGWithMultiThresholdDE2000(sourceImage);

            // 寫入SVG檔案
            try (FileWriter writer = new FileWriter("output_de2000_contours.svg")) {
                writer.write(svg);
            }

            System.out.println("SVG檔案已生成: output_de2000_contours.svg");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 計算基於 Delta E 2000 的邊緣圖
     */
    private static Mat calculateDE2000Edges(Mat labImage, double threshold) {
        int rows = labImage.rows();
        int cols = labImage.cols();
        Mat edges = Mat.zeros(rows, cols, CvType.CV_8UC1);

        // 方向向量 (8-連通性)
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int y = 1; y < rows - 1; y++) {
            for (int x = 1; x < cols - 1; x++) {
                // 取得中心像素的 LAB 值
                double[] centerLab = labImage.get(y, x);
                double maxDeltaE = 0.0;

                // 檢查所有鄰近像素
                for (int i = 0; i < 8; i++) {
                    int nx = x + dx[i];
                    int ny = y + dy[i];

                    if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                        double[] neighborLab = labImage.get(ny, nx);
                        double deltaE = calculateDeltaE2000(centerLab, neighborLab);
                        maxDeltaE = Math.max(maxDeltaE, deltaE);
                    }
                }

                // 如果最大的顏色差異超過閾值，標記為邊緣
                if (maxDeltaE > threshold) {
                    edges.put(y, x, 255);
                }
            }
        }

        return edges;
    }

    /**
     * 計算兩個 LAB 顏色之間的 Delta E 2000 差異
     * 這是簡化版本，完整版本會更複雜但這已經足夠實用
     */
    private static double calculateDeltaE2000(double[] lab1, double[] lab2) {
        double L1 = lab1[0], a1 = lab1[1], b1 = lab1[2];
        double L2 = lab2[0], a2 = lab2[1], b2 = lab2[2];

        // 轉換 LAB 為 LCH (極坐標)
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double H1 = Math.atan2(b1, a1) * 180.0 / Math.PI;
        double H2 = Math.atan2(b2, a2) * 180.0 / Math.PI;

        if (H1 < 0) H1 += 360;
        if (H2 < 0) H2 += 360;

        // 計算差異
        double deltaL = L2 - L1;
        double deltaC = C2 - C1;
        double deltaH = H2 - H1;

        // 處理色相角度差異
        if (Math.abs(deltaH) > 180) {
            if (deltaH > 0) {
                deltaH -= 360;
            } else {
                deltaH += 360;
            }
        }
        deltaH = 2 * Math.sqrt(C1 * C2) * Math.sin(Math.toRadians(deltaH / 2));

        // 簡化的 Delta E 2000 計算 (不包含所有加權因子)
        // 完整版本會考慮更多因子，但這個版本已經比標準 Delta E 好很多
        double avgL = (L1 + L2) / 2;
        double avgC = (C1 + C2) / 2;

        // 亮度加權
        double SL = 1 + (0.015 * Math.pow(avgL - 50, 2)) / Math.sqrt(20 + Math.pow(avgL - 50, 2));
        // 彩度加權
        double SC = 1 + 0.045 * avgC;
        // 色相加權
        double SH = 1 + 0.015 * avgC;

        // 加權參數 (可調整)
        double KL = 1.0, KC = 1.0, KH = 1.0;

        return Math.sqrt(
                Math.pow(deltaL / (KL * SL), 2) +
                        Math.pow(deltaC / (KC * SC), 2) +
                        Math.pow(deltaH / (KH * SH), 2)
        );
    }

    /**
     * 進階版本：結合多個閾值的 DE2000 檢測
     */
    public static String convertImageToSVGWithMultiThresholdDE2000(Mat sourceImage) throws IOException {
        int width = sourceImage.width();
        int height = sourceImage.height();

        Mat labImage = new Mat();
        Imgproc.cvtColor(sourceImage, labImage, Imgproc.COLOR_BGR2Lab);

        // 使用多個閾值來捕捉不同強度的顏色邊界
        double[] thresholds = {1.5, 3.0, 6.0, 10.0}; // JND (Just Noticeable Difference) 級別
        List<MatOfPoint> allContours = new ArrayList<>();

        for (double threshold : thresholds) {
            Mat edges = calculateDE2000Edges(labImage, threshold);

            // 根據閾值調整形態學操作
            int kernelSize = threshold < 5.0 ? 1 : 2;
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(kernelSize, kernelSize));
            Mat cleanEdges = new Mat();
            Imgproc.morphologyEx(edges, cleanEdges, Imgproc.MORPH_OPEN, kernel);

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(cleanEdges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

            // 過濾並添加到總列表
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                // 根據閾值調整最小面積要求
                double minArea = threshold < 5.0 ? 20 : 50;
                if (area >= minArea && contour.toArray().length >= 6) {
                    allContours.add(contour);
                }
            }
        }

        // 生成SVG
        String base64Image = matToBase64(sourceImage, ".png");
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n", width, height));
        svg.append(String.format("  <image x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" xlink:href=\"data:image/png;base64,%s\"/>\n",
                width, height, base64Image));

        // 繪製所有輪廓
        for (MatOfPoint contour : allContours) {
            Point[] points = contour.toArray();
            drawContourAsPolyline(svg, points);
        }

        svg.append("</svg>");
        return svg.toString();
    }

    /**
     * 將輪廓繪製為連續的線段
     */
    private static void drawContourAsPolyline(StringBuilder svg, Point[] points) {
        if (points.length < 2) return;

        StringBuilder polylinePoints = new StringBuilder();
        for (Point point : points) {
            polylinePoints.append(String.format("%.0f,%.0f ", point.x, point.y));
        }

        svg.append(String.format("  <polyline points=\"%s\" fill=\"none\" stroke=\"green\" stroke-width=\"1\" opacity=\"0.7\"/>\n",
                polylinePoints.toString().trim()));
    }

    /**
     * 將 OpenCV Mat 物件轉換為 Base64 字符串。
     */
    private static String matToBase64(Mat mat, String fileExtension) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(fileExtension, mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }
}