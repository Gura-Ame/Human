package com.github.guraame.human.senses.eye;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.util.Base64;

public final class ObjectSeparation {
    private static final double DELTA_E_THRESHOLD = 1.51;
    private static final int MIN_BOUNDARY_DENSITY = 3;  // 最小邊界密度
    private static final int MAX_BOUNDARY_DENSITY = 15; // 最大邊界密度
    private static final int BOUNDARY_CHECK_RADIUS = 3; // 檢查邊界密度的半徑
    private static final int MIN_OBJECT_SIZE = 50;      // 最小物體大小（像素）

    public static void main(String[] args) {
        try {
            BufferedImage image = ImageIO.read(new File("input.jpg"));

            ObjectSeparationResult result = separateObjects(image);

            // 生成物體分離結果的SVG
            String svg = generateObjectSVG(image, result);

            try (FileWriter writer = new FileWriter("objects_separated.svg")) {
                writer.write(svg);
            }

            System.out.println("物體分離完成！");
            System.out.println("檢測到 " + result.objects.size() + " 個物體");
            System.out.println("SVG檔案已生成: objects_separated.svg");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObjectSeparationResult separateObjects(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 1. 建立區塊映射
        RegionAnalysis regionAnalysis = analyzeRegions(image);

        // 2. 計算邊界密度圖
        int[][] boundaryDensity = calculateBoundaryDensity(regionAnalysis.regionMap, width, height);

        // 3. 識別物體核心區域（邊界密度低的區域）
        boolean[][] objectCore = identifyObjectCores(boundaryDensity, width, height);

        // 4. 從核心區域擴展識別完整物體
        List<ObjectInfo> objects = growObjectsFromCores(image, regionAnalysis.regionMap,
                objectCore, boundaryDensity, width, height);

        // 5. 過濾小物體
        objects = filterSmallObjects(objects);

        return new ObjectSeparationResult(objects, regionAnalysis.regionMap, boundaryDensity);
    }

    private static RegionAnalysis analyzeRegions(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] regionMap = new int[height][width];
        Map<Integer, Color> regionColors = new HashMap<>();
        int nextRegionId = 1;

        // 初始化
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                regionMap[y][x] = -1;
            }
        }

        // 區域分割
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (regionMap[y][x] == -1) {
                    Color pixelColor = new Color(image.getRGB(x, y));
                    floodFill(image, regionMap, x, y, nextRegionId, pixelColor);
                    regionColors.put(nextRegionId, pixelColor);
                    nextRegionId++;
                }
            }
        }

        return new RegionAnalysis(regionMap, regionColors);
    }

    private static int[][] calculateBoundaryDensity(int[][] regionMap, int width, int height) {
        int[][] density = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int boundaryCount = 0;

                // 檢查周圍區域的邊界數量
                for (int dy = -BOUNDARY_CHECK_RADIUS; dy <= BOUNDARY_CHECK_RADIUS; dy++) {
                    for (int dx = -BOUNDARY_CHECK_RADIUS; dx <= BOUNDARY_CHECK_RADIUS; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;

                        if (nx >= 0 && nx < width - 1 && ny >= 0 && ny < height - 1) {
                            // 檢查是否為邊界點
                            if (isBoundaryPoint(regionMap, nx, ny)) {
                                boundaryCount++;
                            }
                        }
                    }
                }

                density[y][x] = boundaryCount;
            }
        }

        return density;
    }

    private static boolean isBoundaryPoint(int[][] regionMap, int x, int y) {
        int currentRegion = regionMap[y][x];

        // 檢查四個方向的鄰居
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && nx < regionMap[0].length && ny >= 0 && ny < regionMap.length) {
                if (regionMap[ny][nx] != currentRegion) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean[][] identifyObjectCores(int[][] boundaryDensity, int width, int height) {
        boolean[][] cores = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 物體核心：邊界密度適中的區域（不是邊界交匯處，也不是完全無邊界）
                if (boundaryDensity[y][x] >= MIN_BOUNDARY_DENSITY &&
                        boundaryDensity[y][x] <= MAX_BOUNDARY_DENSITY) {
                    cores[y][x] = true;
                }
            }
        }

        return cores;
    }

    private static List<ObjectInfo> growObjectsFromCores(BufferedImage image, int[][] regionMap,
                                                         boolean[][] objectCore, int[][] boundaryDensity,
                                                         int width, int height) {
        List<ObjectInfo> objects = new ArrayList<>();
        boolean[][] visited = new boolean[height][width];
        int objectId = 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (objectCore[y][x] && !visited[y][x]) {
                    ObjectInfo obj = growSingleObject(image, regionMap, boundaryDensity,
                            visited, x, y, objectId++, width, height);
                    if (obj != null) {
                        objects.add(obj);
                    }
                }
            }
        }

        return objects;
    }

    private static ObjectInfo growSingleObject(BufferedImage image, int[][] regionMap,
                                               int[][] boundaryDensity, boolean[][] visited,
                                               int startX, int startY, int objectId,
                                               int width, int height) {
        List<Point> objectPixels = new ArrayList<>();
        Queue<Point> queue = new LinkedList<>();
        Set<Integer> objectRegions = new HashSet<>();

        queue.offer(new Point(startX, startY));
        visited[startY][startX] = true;

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            objectPixels.add(p);
            objectRegions.add(regionMap[p.y][p.x]);

            // 擴展到相鄰像素
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = p.x + dx;
                    int ny = p.y + dy;

                    if (nx >= 0 && nx < width && ny >= 0 && ny < height && !visited[ny][nx]) {
                        if (shouldIncludeInObject(regionMap, boundaryDensity, objectRegions, nx, ny)) {
                            visited[ny][nx] = true;
                            queue.offer(new Point(nx, ny));
                        }
                    }
                }
            }
        }

        if (objectPixels.size() >= MIN_OBJECT_SIZE) {
            return new ObjectInfo(objectId, objectPixels, calculateAverageColor(image, objectPixels));
        }

        return null;
    }

    private static boolean shouldIncludeInObject(int[][] regionMap, int[][] boundaryDensity,
                                                 Set<Integer> objectRegions, int x, int y) {
        // 包含條件：
        // 1. 屬於物體已有的區域，或
        // 2. 邊界密度不太高（不是複雜交界處），且顏色相似

        if (objectRegions.contains(regionMap[y][x])) {
            return true;
        }

        // 避免邊界密度過高的區域（物體交界處）
        return !(boundaryDensity[y][x] > MAX_BOUNDARY_DENSITY * 1.5);
    }

    private static Color calculateAverageColor(BufferedImage image, List<Point> pixels) {
        long r = 0, g = 0, b = 0;

        for (Point p : pixels) {
            Color c = new Color(image.getRGB(p.x, p.y));
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }

        int size = pixels.size();
        return new Color((int)(r/size), (int)(g/size), (int)(b/size));
    }

    private static List<ObjectInfo> filterSmallObjects(List<ObjectInfo> objects) {
        return objects.stream()
                .filter(obj -> obj.pixels.size() >= MIN_OBJECT_SIZE)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private static String generateObjectSVG(BufferedImage image, ObjectSeparationResult result) {
        int width = image.getWidth();
        int height = image.getHeight();

        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n", width, height));

        // 添加原始圖片
        String base64Image = imageToBase64(image);
        svg.append(String.format("<image x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" xlink:href=\"data:image/png;base64,%s\"/>\n",
                width, height, base64Image));

        // 為每個物體添加不同顏色的邊框
        Color[] objectColors = {
                new Color(255, 0, 0, 128),    // 紅色
                new Color(0, 255, 0, 128),    // 綠色
                new Color(0, 0, 255, 128),    // 藍色
                new Color(255, 255, 0, 128),  // 黃色
                new Color(255, 0, 255, 128),  // 紫色
                new Color(0, 255, 255, 128),  // 青色
                new Color(255, 128, 0, 128),  // 橙色
                new Color(128, 0, 255, 128),  // 靛色
        };

        for (int i = 0; i < result.objects.size(); i++) {
            ObjectInfo obj = result.objects.get(i);
            Color borderColor = objectColors[i % objectColors.length];
            String colorStr = String.format("rgba(%d,%d,%d,%.2f)",
                    borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(),
                    borderColor.getAlpha() / 255.0);

            // 畫物體邊界
            Set<String> boundaries = findObjectBoundaries(obj.pixels, width, height);
            for (String boundary : boundaries) {
                svg.append(boundary.replace("stroke=\"red\"", "stroke=\"" + colorStr + "\"")
                        .replace("stroke-width=\"1\"", "stroke-width=\"2\""));
            }

            // 添加物體標籤
            if (!obj.pixels.isEmpty()) {
                Point center = calculateCenter(obj.pixels);
                svg.append(String.format("<text x=\"%d\" y=\"%d\" fill=\"white\" stroke=\"black\" stroke-width=\"1\" font-size=\"14\" text-anchor=\"middle\">物體%d</text>\n",
                        center.x, center.y, obj.id));
            }
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private static Set<String> findObjectBoundaries(List<Point> pixels, int width, int height) {
        Set<String> boundaries = new HashSet<>();
        Set<Point> pixelSet = new HashSet<>(pixels);

        for (Point p : pixels) {
            // 檢查四個方向
            int[] dx = {1, 0, 1, 0};
            int[] dy = {0, 1, 0, 1};
            int[] x1 = {p.x + 1, p.x, p.x + 1, p.x};
            int[] y1 = {p.y, p.y + 1, p.y, p.y + 1};
            int[] x2 = {p.x + 1, p.x + 1, p.x + 1, p.x + 1};
            int[] y2 = {p.y + 1, p.y + 1, p.y + 1, p.y + 1};

            for (int i = 0; i < 4; i++) {
                Point neighbor = new Point(p.x + dx[i], p.y + dy[i]);
                if (neighbor.x >= 0 && neighbor.x < width && neighbor.y >= 0 && neighbor.y < height) {
                    if (!pixelSet.contains(neighbor)) {
                        boundaries.add(String.format(
                                "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"red\" stroke-width=\"1\"/>\n",
                                x1[i], y1[i], x2[i], y2[i]));
                    }
                }
            }
        }

        return boundaries;
    }

    private static Point calculateCenter(List<Point> pixels) {
        int sumX = 0, sumY = 0;
        for (Point p : pixels) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Point(sumX / pixels.size(), sumY / pixels.size());
    }

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

    // 使用原有的flood fill和顏色計算方法
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

            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }
    }

    private static double calculateDeltaE(Color c1, Color c2) {
        double[] lab1 = rgbToLab(c1);
        double[] lab2 = rgbToLab(c2);

        double deltaL = lab1[0] - lab2[0];
        double deltaA = lab1[1] - lab2[1];
        double deltaB = lab1[2] - lab2[2];

        return Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
    }

    private static double[] rgbToLab(Color color) {
        double[] xyz = rgbToXyz(color);

        double xn = 95.047;
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

    private static double[] rgbToXyz(Color color) {
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;

        r = r > 0.04045 ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
        g = g > 0.04045 ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
        b = b > 0.04045 ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;

        r *= 100;
        g *= 100;
        b *= 100;

        double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
        double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
        double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

        return new double[]{x, y, z};
    }

    // 內部類別定義
    static class RegionAnalysis {
        int[][] regionMap;
        Map<Integer, Color> regionColors;

        RegionAnalysis(int[][] regionMap, Map<Integer, Color> regionColors) {
            this.regionMap = regionMap;
            this.regionColors = regionColors;
        }
    }

    static class ObjectInfo {
        int id;
        List<Point> pixels;
        Color averageColor;

        ObjectInfo(int id, List<Point> pixels, Color averageColor) {
            this.id = id;
            this.pixels = pixels;
            this.averageColor = averageColor;
        }
    }

    public static class ObjectSeparationResult {
        List<ObjectInfo> objects;
        int[][] regionMap;
        int[][] boundaryDensity;

        ObjectSeparationResult(List<ObjectInfo> objects, int[][] regionMap, int[][] boundaryDensity) {
            this.objects = objects;
            this.regionMap = regionMap;
            this.boundaryDensity = boundaryDensity;
        }
    }
}