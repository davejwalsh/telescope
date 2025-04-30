package com.ska.telescopeSimulator.sky;

import com.ska.telescopeSimulator.dto.DeviceCoordinates;
import com.ska.telescopeSimulator.sky.SimulatedStar;

import java.util.List;
import java.util.Random;

public class SimulatedSky {

    private final int offset = 100;
    private final int sizeX = 201; // Covers -100 to 100
    private final int sizeY = 201;
    private final int sizeZ = 201;
    private final double[][][] sky;
    private final double maxBackground = 5;
    private final Random random = new Random();

    public SimulatedSky(List<SimulatedStar> stars) {
        this.sky = new double[sizeX][sizeY][sizeZ];
        initialiseBackground();
        stars.forEach(this::addStar);
    }

    private void initialiseBackground() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    sky[x][y][z] = random.nextDouble() * maxBackground;
                }
            }
        }
    }

    public void addStar(SimulatedStar star) {
        int startX = (int) Math.max(-offset, Math.floor(star.getCoordinates().getX() - star.getRadius()));
        int endX = (int) Math.min(offset, Math.ceil(star.getCoordinates().getX() + star.getRadius()));
        int startY = (int) Math.max(-offset, Math.floor(star.getCoordinates().getY() - star.getRadius()));
        int endY = (int) Math.min(offset, Math.ceil(star.getCoordinates().getY() + star.getRadius()));
        int startZ = (int) Math.max(-offset, Math.floor(star.getCoordinates().getZ() - star.getRadius()));
        int endZ = (int) Math.min(offset, Math.ceil(star.getCoordinates().getZ() + star.getRadius()));

        double sigma = star.getRadius() / 2.0;
        double twoSigmaSquared = 2 * sigma * sigma;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    double distanceSquared =
                            Math.pow(star.getCoordinates().getX() - x, 2)
                                    + Math.pow(star.getCoordinates().getY() - y, 2)
                                    + Math.pow(star.getCoordinates().getZ() - z, 2);

                    if (distanceSquared <= star.getRadius() * star.getRadius()) {
                        double diminishedIntensity = star.getIntensity() * Math.exp(-distanceSquared / twoSigmaSquared);

                        int ix = x + offset;
                        int iy = y + offset;
                        int iz = z + offset;

                        if (diminishedIntensity > sky[ix][iy][iz]) {
                            sky[ix][iy][iz] = diminishedIntensity;
                        }
                    }
                }
            }
        }
    }

    public double getValueAt(DeviceCoordinates coordinate) {
        int x = (int) Math.round(coordinate.getX()) + offset;
        int y = (int) Math.round(coordinate.getY()) + offset;
        int z = (int) Math.round(coordinate.getZ()) + offset;

        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
            throw new IllegalArgumentException("Coordinates out of bounds!");
        }

        double baseValue = sky[x][y][z];
        double noise = (random.nextDouble() * 2 - 1) * maxBackground;
        return baseValue + noise;
    }
}
