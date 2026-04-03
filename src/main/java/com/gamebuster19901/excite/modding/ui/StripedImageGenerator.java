package com.gamebuster19901.excite.modding.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class StripedImageGenerator {

    public static BufferedImage generateImage(int width, int height, LinkedHashMap<Color, Integer> colorWeights) {
        // Check for valid input
        if (width <= 0 || height <= 0 || colorWeights == null || colorWeights.isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        int totalWeight = 0;
        for (int weight : colorWeights.values()) {
            if (weight < 0) {
                throw new IllegalArgumentException("Color weights must be 0 or a positive integer");
            }
            totalWeight += weight;
        }

        if (totalWeight == 0) {
            totalWeight = height;
            colorWeights = new LinkedHashMap<Color, Integer>();
            colorWeights.put(Color.GRAY, totalWeight);
        }
        double scaleFactor = (double) height / totalWeight;

        LinkedHashMap<Color, Integer> colorHeights = new LinkedHashMap<>();
        for (Map.Entry<Color, Integer> entry : colorWeights.reversed().entrySet()) {
            Color color = entry.getKey();
            int weight = entry.getValue();
            int rowHeight = (int) Math.ceil(weight * scaleFactor);
            colorHeights.put(color, rowHeight);
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();

        int currentY = 0;
        for (Map.Entry<Color, Integer> entry : colorHeights.entrySet()) {
            Color color = entry.getKey();
            int rowHeight = entry.getValue();
            graphics.setColor(color);
            graphics.fillRect(0, currentY, width, rowHeight);
            currentY += rowHeight;
        }

        graphics.dispose();

        return image;
    }
}