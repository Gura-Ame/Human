package com.github.guraame.human.senses.eye;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

public final class Eye {

    // --- 設定教學 ---
    // 1. 下載 OpenCV: 前往 https://opencv.org/releases/ 下載符合您作業系統的版本 (例如 4.x.x)。
    // 2. 設定專案:
    //    a. 將 opencv-4xx.jar 加入您專案的 build path (library)。
    //       - (Eclipse/IntelliJ) 右鍵專案 -> Properties/Project Structure -> Libraries -> Add External JARs...
    //       - JAR 檔案路徑: opencv/build/java/opencv-4xx.jar
    //    b. 設定 VM 選項以指向 native library (.dll, .so, .dylib)。
    //       - (Eclipse/IntelliJ) Run -> Edit Configurations... -> VM options
    //       - 新增: -Djava.library.path="C:/path/to/opencv/build/java/x64" (請換成您的實際路徑)
    // 3. 在程式碼中載入函式庫 (如下方 static 區塊所示)。

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

            // 處理圖像並生成SVG
            String svg = convertImageToSVGWithOpenCV(sourceImage);

            // 寫入SVG檔案
            try (FileWriter writer = new FileWriter("output_opencv_lines.svg")) {
                writer.write(svg);
            }

            System.out.println("SVG檔案已生成: output_opencv_lines.svg");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertImageToSVGWithOpenCV(Mat sourceImage) throws IOException {
        int width = sourceImage.width();
        int height = sourceImage.height();

        // --- 影像處理流程 ---

        // 1. 轉為灰階
        Mat grayImage = new Mat();
        Imgproc.cvtColor(sourceImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        // 2. 高斯模糊以去噪
        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(grayImage, blurredImage, new org.opencv.core.Size(5, 5), 0);

        // 3. Canny 邊緣偵測
        Mat edges = new Mat();
        // 這兩個閾值可以調整，值越低偵測到的邊緣越多
        Imgproc.Canny(blurredImage, edges, 30, 0);

        // 4. 霍夫變換 (Hough Transform) 來偵測直線
        // 這個方法會回傳一系列線段的起點和終點
        Mat lines = new Mat(); // 儲存偵測到的線條
        // threshold: 閾值，一條直線上至少要有多少個點才被視為直線，可調整
        // minLineLength: 線段的最小長度
        // maxLineGap: 線段之間的最大允許間隙，以將它們視為同一條線
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 0, 0, 5);

        // --- SVG 生成 ---

        // 將原始圖片轉換為Base64，以便嵌入SVG
        String base64Image = matToBase64(sourceImage, ".png");

        // 生成最終的SVG字符串
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n", width, height));

        // 添加原始圖片作為底圖
        svg.append(String.format("  <image x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" xlink:href=\"data:image/png;base64,%s\"/>\n",
                width, height, base64Image));

        // 繪製偵測到的綠色線條
        for (int i = 0; i < lines.rows(); i++) {
            double[] vec = lines.get(i, 0);
            double x1 = vec[0], y1 = vec[1];
            double x2 = vec[2], y2 = vec[3];
            svg.append(String.format("  <line x1=\"%.0f\" y1=\"%.0f\" x2=\"%.0f\" y2=\"%.0f\" stroke=\"green\" stroke-width=\"2\"/>\n",
                    x1, y1, x2, y2));
        }

        svg.append("</svg>");
        return svg.toString();
    }

    /**
     * 將 OpenCV Mat 物件轉換為 Base64 字符串。
     * @param mat OpenCV Mat 物件
     * @param fileExtension 圖像格式 (例如 ".png" 或 ".jpg")
     * @return Base64 編碼的字符串
     */
    private static String matToBase64(Mat mat, String fileExtension) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(fileExtension, mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }
}
