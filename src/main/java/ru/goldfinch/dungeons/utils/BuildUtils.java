package ru.goldfinch.dungeons.utils;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ImmutableBlock;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import io.lumine.utils.serialize.BlockRegion;
import org.bukkit.Location;
import ru.goldfinch.dungeons.Dungeons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class BuildUtils {

    public static void setBlock(Location location, BlockType blockType) {
        setBlock(location, new BaseBlock(blockType.getID()));
    }

    public static void setBlock(Location location, BaseBlock baseBlock) {
        World world = FaweAPI.getWorld(location.getWorld().getName());

        EditSession editSession = new EditSessionBuilder(world)
                .world(world)
                .fastmode(true)
                .build();

        editSession.smartSetBlock(new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()).toBlockPoint(), baseBlock);
        editSession.flushQueue();
    }

    public static void pasteSchem(Location location, String name, String folder, int rotationX, int rotationY, int rotationZ) {
        World world = FaweAPI.getWorld(location.getWorld().getName());
        com.sk89q.worldedit.Vector to = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Clipboard clipboard = getSchematic(name, folder, location.getWorld(), false);

        EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateX(rotationX).rotateY(rotationY).rotateZ(rotationZ);

        ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, to);
        if (!transform.isIdentity()) copy.setTransform(transform);
        copy.setSourceMask(new ExistingBlockMask(clipboard));

        try {
            Operations.completeLegacy(copy);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
        extent.flushQueue();
    }

    public static void pasteSchem(Location location, Clipboard clipboard, int rotationX, int rotationY, int rotationZ) {
        World world = FaweAPI.getWorld(location.getWorld().getName());
        com.sk89q.worldedit.Vector to = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateX(rotationX).rotateY(rotationY).rotateZ(rotationZ);

        ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, to);
        if (!transform.isIdentity()) copy.setTransform(transform);
        copy.setSourceMask(new ExistingBlockMask(clipboard));

        try {
            Operations.completeLegacy(copy);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
        extent.flushQueue();


    }

    public static Clipboard getSchematic(String name, String folder, org.bukkit.World world, boolean centralised) {
        WorldData worldData = FaweAPI.getWorld(world.getName()).getWorldData();

        File schemFile;
        if (!new File(Dungeons.getInstance().getDataFolder() + "/" + folder + "/" + name + ".schematic").exists()) {
            if (!new File(Dungeons.getInstance().getDataFolder() + "/" + folder + "/" + name + ".schem").exists()) {
                throw new RuntimeException("Schematic " + name + " not found at " + Dungeons.getInstance().getDataFolder() + "/" + folder + "/" + name + ".schem");
            } else {
                schemFile = new File(Dungeons.getInstance().getDataFolder() + "/" + folder + "/" + name + ".schem");
            }
        } else {
            schemFile = new File(Dungeons.getInstance().getDataFolder() + "/" + folder + "/" + name + ".schematic");
        }

        try {
            Clipboard clipboard;
            try {
                clipboard = ClipboardFormat.SCHEMATIC.getReader(Files.newInputStream(schemFile.toPath())).read(worldData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (centralised) {
                clipboard.setOrigin(new Vector(clipboard.getMinimumPoint().getBlockX() + (clipboard.getMaximumPoint().getBlockX() - clipboard.getMinimumPoint().getBlockX()) / 2, clipboard.getMinimumPoint().getBlockY() + (clipboard.getMaximumPoint().getBlockY() - clipboard.getMinimumPoint().getBlockY()) / 2, clipboard.getMinimumPoint().getBlockZ() + (clipboard.getMaximumPoint().getBlockZ() - clipboard.getMinimumPoint().getBlockZ()) / 2));
            }
            return clipboard;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<BlockVector, BaseBlock> getSchematicBlocks(String name, String folder, org.bukkit.World world) {
        HashMap<BlockVector, BaseBlock> blocks = new HashMap<>();

        try {
            Clipboard clipboard = getSchematic(name, folder, world, true);
            if (clipboard == null) return null;
            BlockVector difference = new BlockVector(-clipboard.getMinimumPoint().getBlockX(), -clipboard.getMinimumPoint().getBlockY(), -clipboard.getMinimumPoint().getBlockZ());

            for (int x = clipboard.getMinimumPoint().getBlockX(); x < clipboard.getMaximumPoint().getBlockX() + 1; x++) {
                for (int y = clipboard.getMinimumPoint().getBlockY(); y < clipboard.getMaximumPoint().getBlockY() + 1; y++) {
                    for (int z = clipboard.getMinimumPoint().getBlockZ(); z < clipboard.getMaximumPoint().getBlockZ() + 1; z++) {
                        BaseBlock block = clipboard.getBlock(new BlockVector(x, y, z));
                        if (block == null || block.getId() == 0) continue;


                        BlockVector vector = new BlockVector(x+difference.getBlockX(), y+difference.getBlockY(), z+difference.getBlockZ());
                        blocks.put(vector, block);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return blocks;
    }

    public static CuboidRegion getSchematicRegion(String name, String folder, org.bukkit.World world) {
        try {
            Clipboard clipboard = getSchematic(name, folder, world, true);
            if (clipboard == null) return null;

            Vector difference = new Vector(-clipboard.getMinimumPoint().getBlockX(), -clipboard.getMinimumPoint().getBlockY(), -clipboard.getMinimumPoint().getBlockZ());
            Vector min = clipboard.getMinimumPoint().add(difference);
            Vector max = clipboard.getMaximumPoint().add(difference);

            return new CuboidRegion(FaweAPI.getWorld(world.getName()), min, max);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void replaceBlocks(Location minPoint, Location maxPoint, BlockType blockMask, Pattern pattern) {
        replaceBlocks(new CuboidRegion(FaweAPI.getWorld(minPoint.getWorld().getName()), new Vector(minPoint.getBlockX(), minPoint.getBlockY(), minPoint.getBlockZ()), new Vector(maxPoint.getBlockX(), maxPoint.getBlockY(), maxPoint.getBlockZ())), blockMask, pattern);
    }

    public static void replaceBlocks(CuboidRegion region, BlockType from, BlockType to) {
        RandomPattern pattern = new RandomPattern();
        pattern.add(new BaseBlock(to.getID()), 100);

        replaceBlocks(region, from, pattern);
    }

    public static void replaceBlocks(CuboidRegion region, BaseBlock from, BaseBlock to) {
        RandomPattern pattern = new RandomPattern();
        pattern.add(to, 100);

        replaceBlocks(region, from, pattern);
    }

    public static void replaceBlocks(CuboidRegion region, BlockType blockMask, Pattern pattern) {
        replaceBlocks(region, new BaseBlock(blockMask.getID()), pattern);
    }

    public static void replaceBlocks(CuboidRegion region, BaseBlock blockMask, Pattern pattern) {
        World world = FaweAPI.getWorld(region.getWorld().getName());

        EditSession editSession = new EditSessionBuilder(world)
                .world(world)
                .fastmode(true)
                .build();

        BlockMask mask = new BlockMask(editSession.getExtent());
        mask.add(blockMask);

        editSession.replaceBlocks(region, mask, pattern);
        editSession.flushQueue();
    }




}
