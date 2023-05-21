package ru.goldfinch.dungeons.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {

    public static List<Location> getEquidistantLocations(Location centralLocation, int n, double minRadius, double maxRadius, double minHeight, double maxHeight) {
        List<Location> locations = new ArrayList<>();
        double angleIncrement = 2 * Math.PI / n;

        for (int i = 0; i < n; i++) {
            double angle = i * angleIncrement;
            double radius = minRadius + Math.random() * (maxRadius - minRadius);
            double height = minHeight + Math.random() * (maxHeight - minHeight);
            double x = centralLocation.getX() + radius * Math.cos(angle);
            double y = centralLocation.getY() + height;
            double z = centralLocation.getZ() + radius * Math.sin(angle);
            locations.add(new Location(centralLocation.getWorld(), x, y, z));
        }

        return locations;
    }

    public static List<Location> getEquidistantLocations(Location centralLocation, int n, double minRadius, double maxRadius) {
        return getEquidistantLocations(centralLocation, n, minRadius, maxRadius, minRadius, maxRadius);
    }

    public static com.sk89q.worldedit.Vector getRotatedVector(com.sk89q.worldedit.Vector vector, int rotation) {
        switch (rotation) {
            case 0:
                return vector;
            case 90:
                return new com.sk89q.worldedit.Vector(vector.getZ(), vector.getY(), -vector.getX());
            case 180:
                return new com.sk89q.worldedit.Vector(-vector.getX(), vector.getY(), -vector.getZ());
            case 270:
                return new com.sk89q.worldedit.Vector(-vector.getZ(), vector.getY(), vector.getX());
        }
        return null;
    }

    public static Direction getDirectionBasedOnCenter(com.sk89q.worldedit.Vector toFind, com.sk89q.worldedit.Vector center) {
        if (toFind.getBlockX() == center.getBlockX())
            if (toFind.getBlockZ() > center.getBlockZ()) return Direction.UP;
            else return Direction.DOWN;
        else if (toFind.getBlockZ() == center.getBlockZ())
            if (toFind.getBlockX() > center.getBlockX()) return Direction.RIGHT;
            else return Direction.LEFT;

        return null;
    }

    public static int summarizeAxisLength(Point point, boolean isXAxis, List<Point> points, int depth) {
        int length = 1;

        if (isXAxis) {
            for (int i = 1; i != -depth; i--)
                if (points.contains(new Point(point.x + i, point.y))) length++;
                else break;

            for (int i = 1; i != depth; i++) {
                if (points.contains(new Point(point.x + i, point.y))) length++;
                else break;
            }

        } else {
            for (int i = 1; i != -depth; i--)
                if (points.contains(new Point(point.x, point.y + i))) length++;
                else break;

            for (int i = 1; i != -depth; i++) {
                if (points.contains(new Point(point.x, point.y + i))) length++;
                else break;
            }
        }

        return length;
    }

    public static List<Point> getFarthestPoints(int n, List<Point> points) {
        List<Point> farthestPoints = new ArrayList<>();
        n += 1;

        int numPoints = points.size();
        double[][] distances = new double[numPoints][numPoints];
        for (int i = 0; i < numPoints; i++) {
            Point p1 = points.get(i);
            for (int j = i + 1; j < numPoints; j++) {
                Point p2 = points.get(j);
                double distance = p1.distance(p2);
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }

        while (farthestPoints.size() < n) {
            Point farthestPoint = null;
            double maxMinDistance = Double.MIN_VALUE;
            for (Point point : points) {
                if (farthestPoints.contains(point))
                    continue;

                double minDistance = Double.MAX_VALUE;
                for (Point fp : farthestPoints) {
                    double distance = distances[points.indexOf(point)][points.indexOf(fp)];
                    if (distance < minDistance)
                        minDistance = distance;

                }
                if (minDistance > maxMinDistance) {
                    maxMinDistance = minDistance;
                    farthestPoint = point;
                }
            }

            farthestPoints.add(farthestPoint);
        }

        farthestPoints.remove(new Point(0, 0));

        return farthestPoints;
    }

    public static Point getMiddleOfPoints(List<Point> pointsToFindMiddleOf, List<Point> dungeonLayout) {
        List<Point> farthestPoints = getFarthestPoints(pointsToFindMiddleOf.size(), pointsToFindMiddleOf);

        Point middlePoint = new Point(0, 0);
        int count = 0;

        for (Point point : farthestPoints) {
            if (dungeonLayout.contains(point)) {
                middlePoint.x += point.x;
                middlePoint.y += point.y;
                count++;
            }
        }

        if (count == 0 || pointsToFindMiddleOf.stream().anyMatch(point -> getShortestPath(point, middlePoint, dungeonLayout) <= 3) || dungeonLayout.contains(middlePoint)) {
            // возвращай самую удаленную точку от всех точек

            while (true) {
                Point randomPoint = dungeonLayout.get(ThreadLocalRandom.current().nextInt(dungeonLayout.size()));
                if (pointsToFindMiddleOf.stream().allMatch(point -> getShortestPath(point, randomPoint, dungeonLayout) > 3) && !pointsToFindMiddleOf.contains(randomPoint))
                    return randomPoint;
            }
        }

        middlePoint.x /= count;
        middlePoint.y /= count;

        return middlePoint;
    }

    public static int getShortestPath(Point start, Point end, List<Point> points) {
        Map<Point, Integer> distance = new HashMap<>();
        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingInt(p -> distance.getOrDefault(p, Integer.MAX_VALUE)));
        distance.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) {
                return distance.get(end);
            }
            for (Point neighbor : getNeighbors(current, points)) {
                int tentativeDistance = distance.get(current) + 1;
                if (tentativeDistance < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distance.put(neighbor, tentativeDistance);
                    queue.add(neighbor);
                }
            }
        }
        return -1;
    }

    private static List<Point> getNeighbors(Point p, List<Point> points) {
        List<Point> neighbors = new ArrayList<>();
        for (Point point : points) {
            if (p.distance(point) <= 1) {
                neighbors.add(point);
            }
        }
        return neighbors;
    }

    public static Location getLocationBetween(Location from, Location to, double height) {
        double x = from.getX() + (to.getX() - from.getX()) / 2;
        double y = from.getY() + (to.getY() - from.getY()) / 2 + height;
        double z = from.getZ() + (to.getZ() - from.getZ()) / 2;

        return new Location(from.getWorld(), x, y, z);
    }

    public static int getYawForTwoPoints(Location from, Location to) {
        double x = to.getX() - from.getX();
        double z = to.getZ() - from.getZ();

        double yaw = Math.toDegrees(Math.atan2(z, x)) - 90;

        return (int) yaw;
    }

    public static int getRandomInteger(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascending) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        if (ascending) list.sort(Map.Entry.comparingByValue());
        else list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());

        return result;
    }

    public static Location getNearbyLocation(Location target, Location sideLoc, double radius, int height) {
            Vector direction = target.toVector().subtract(sideLoc.toVector());
            direction.normalize();

            Vector offset = direction.multiply(radius).setY(height);

        return sideLoc.clone().add(offset);
    }

    public static boolean getRandom(double chance) {
        return Math.random() < chance;
    }

    public static boolean getRandom(int chance) {
        return Math.random() < chance / 100.0;
    }


    public static int getPercentFromInteger(double toFind, double from) {
        double percent = toFind/from;
        return (int) Math.round(percent*100);
    }

    public static Location getRandomLocation(World world, int minX, int maxX, int minZ, int maxZ) {
        double x = MathUtils.getRandomInteger(minX, maxX);
        double y = MathUtils.getRandomInteger(200, 250);
        double z = MathUtils.getRandomInteger(minZ, maxZ);

        return new Location(world, x, y, z);
    }

    public static boolean isInRadius(int radius, Location location, Location playerLocation) {
        return location.distance(playerLocation) <= radius;
    }
}