package com.github.guraame.human.senses;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class Eye {
    public static void readImage(byte[][] buffer) {
        Map<Map.Entry<Integer, Integer>, Byte> cache = new HashMap<>();
        for (int x = 0; x < buffer.length; x++) {
            for (int y = 0; y < buffer[x].length; y++) {
                Optional<Color> centerPixelOptional = getColorOfPosition(x, y, buffer, buffer[x], cache);
                assert centerPixelOptional.isPresent();
                Color centerPixel = centerPixelOptional.get();

                Optional<Color> centerLeftPixel = getColorOfPosition(x - 1, y, buffer, buffer[x], cache),
                                centerRightPixel = getColorOfPosition(x + 1, y, buffer, buffer[x], cache),
                                centerUpPixel = getColorOfPosition(x, y + 1, buffer, buffer[x], cache),
                                centerDownPixel = getColorOfPosition(x, y - 1, buffer, buffer[x], cache),
                                centerLeftUpPixel = getColorOfPosition(x - 1, y + 1, buffer, buffer[x], cache),
                                centerLeftDownPixel = getColorOfPosition(x - 1, y - 1, buffer, buffer[x], cache),
                                centerRightUpPixel = getColorOfPosition(x + 1, y + 1, buffer, buffer[x], cache),
                                centerRightDownPixel = getColorOfPosition(x + 1, y + 1, buffer, buffer[x], cache);

            }
        }
    }

    public static Optional<Color> getColorOfPosition(int x, int y, byte[][] buffer, byte[] yBuffer, Map<Map.Entry<Integer, Integer>, Byte> cache) {
        AtomicReference<Optional<Color>> lookUpInMapResult = new AtomicReference<>(Optional.empty());
        cache.forEach((positionEntry, value) -> {
            if (positionEntry.getKey() == x && positionEntry.getValue() == y) {
                lookUpInMapResult.set(Optional.of(new Color(value)));
            }
        });
        if (x < buffer.length && y < yBuffer.length) {
            byte pixel = buffer[x][y];
            cache.put(Map.entry(x, y), pixel);
            return Optional.of(new Color(pixel));
        }
        return lookUpInMapResult.get();
    }
}
