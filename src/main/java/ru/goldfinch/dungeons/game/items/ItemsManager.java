package ru.goldfinch.dungeons.game.items;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.Range;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.game.items.types.*;
import ru.goldfinch.dungeons.generator.Dungeon;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ItemsManager {

    private Dungeons plugin;
    private HashMap<String, Range<Integer>> goldNames;

    private final static CodecRegistry codec = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder()
                    .automatic(true)
                    .build()));

    @Getter
    private final static MongoCollection<Document> itemsCollection =
            Dungeons.getInstance().getMongo().getDatabase().withCodecRegistry(codec)
                    .getCollection("items");

    public ItemsManager(Dungeons plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        File file = new File(plugin.getDataFolder(), "items.yml");
        if (!file.exists()) plugin.saveResource("items.yml", false);
        plugin.saveResource("items.yml", false);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        goldNames = new HashMap<>();
        config.getConfigurationSection("gold-naming").getKeys(false).forEach(key -> goldNames.put(config.getString("gold-naming." + key + ".title"), Range.between(config.getInt("gold-naming." + key + ".from"), config.getInt("gold-naming." + key + ".to"))));
    }

    public String getGoldName(int gold) {
        String goldName = "";

        for (Map.Entry<String, Range<Integer>> entry : this.goldNames.entrySet()) {
            String key = entry.getKey();
            Range<Integer> value = entry.getValue();
            if (gold >= value.getMinimum() && gold <= value.getMaximum()) goldName = key;
        }

        if (goldName.isEmpty()) {
            goldName = "Золото [" + gold + "]";
        } else {
            goldName += " золота [" + gold + "]";
        }

        return goldName;
    }

    public boolean isDungeonItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return false;
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.hasTag("type");
    }

    public AbstractItem decodeItem(ItemStack itemStack) {
        if (!isDungeonItem(itemStack))
            return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        ItemType type = ItemType.valueOf(nbtItem.getString("type"));

        switch (type) {
            case OTHER:
                return PlainItem.fromItem(itemStack);
            case WEAPON:
                return WeaponItem.fromItem(itemStack);
            case ARMOR:
                return ArmorItem.fromItem(itemStack);
            case KEY:
                return KeyItem.fromItem(itemStack);
            case GOLD:
                return GoldItem.fromItem(itemStack);
            default:
                return null;
        }
    }

    public void saveItem(AbstractItem item) {
        Document document = item.toDocument();
        itemsCollection.insertOne(document);
    }

    public void deleteItem(AbstractItem item) {
        Document document = item.toDocument();
        itemsCollection.deleteOne(document);
    }

    public AbstractItem getItem(Document document) {
        ItemType type = ItemType.valueOf(document.getString("type"));
        switch (type) {
            case OTHER:
                return PlainItem.fromDocument(document);
            case WEAPON:
                return WeaponItem.fromDocument(document);
            case ARMOR:
                return ArmorItem.fromDocument(document);
            case KEY:
                return KeyItem.fromDocument(document);
            case GOLD:
                return GoldItem.fromDocument(document);
            default:
                return null;
        }
    }

    public AbstractItem getItem(ItemStack item) {
        if (!isDungeonItem(item)) return null;
        NBTItem nbtItem = new NBTItem(item);
        ItemType type = ItemType.valueOf(nbtItem.getString("type"));

        switch (type) {
            case OTHER:
                return PlainItem.fromItem(item);
            case WEAPON:
                return WeaponItem.fromItem(item);
            case ARMOR:
                return ArmorItem.fromItem(item);
            case KEY:
                return KeyItem.fromItem(item);
            case GOLD:
                return GoldItem.fromItem(item);
            default:
                return null;
        }
    }

    public AbstractItem getItem(String _id) {
        Document document = itemsCollection.find(new Document("_id", _id)).first();
        if (document == null) return null;

        return getItem(document);
    }

    public AbstractItem fromItem(ItemStack itemStack) {
        if (!isDungeonItem(itemStack)) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        ItemType type = ItemType.valueOf(nbtItem.getString("type"));

        switch (type) {
            case OTHER:
                return PlainItem.fromItem(itemStack);
            case WEAPON:
                return WeaponItem.fromItem(itemStack);
            case ARMOR:
                return ArmorItem.fromItem(itemStack);
            case KEY:
                return KeyItem.fromItem(itemStack);
            case GOLD:
                return GoldItem.fromItem(itemStack);
            default:
                return null;
        }
    }

    public HashMap<AbstractItem, Double> gatherItems(DungeonSettings settings) {
        HashMap<AbstractItem, Double> items = new HashMap<>();
        HashMap<Rarity, Double> raritiesModifiers = new HashMap<>() {{
            put(Rarity.COMMON, 0.5);
            put(Rarity.RARE, 0.02);
            put(Rarity.EPIC, 0.003);
        }};

        HashMap<ItemType, Integer> itemTypeModifier = new HashMap<>() {{
            put(ItemType.OTHER, 1);
            put(ItemType.WEAPON, 3);
            put(ItemType.ARMOR, 2);
            put(ItemType.KEY, 5);
        }};

        int maxLvl = settings.getRequiredLevel() + 4;
        int minLvl = settings.ordinal() == 0 ? 1 : DungeonSettings.values()[settings.ordinal() - 1].getRequiredLevel();
        minLvl -= minLvl == 1 ? 0 : 4;

        Document filter = new Document();
        filter.append("level", new Document("$gte", minLvl).append("$lte", maxLvl));
        filter.append("rarity", new Document("$in", raritiesModifiers.keySet().stream().map(Enum::name).collect(Collectors.toList())));


        if (itemsCollection.countDocuments(filter) == 0) {
            plugin.getLogger().warning("No items found for dungeon settings: " + settings.name());
            return items;
        }

        itemsCollection.find(filter).forEach(document -> {
            AbstractItem item = getItem(document);
            Rarity rarity = item.getRarity();
            ItemType type = item.getType();

            if (item instanceof AbstractEquipment) {
                int durability;

                if (MathUtils.getRandom(0.5)) {
                    if (MathUtils.getRandom(0.5)) {
                        durability = MathUtils.getRandomInteger(50, 75);

                        if (MathUtils.getRandom(0.5))
                            durability = MathUtils.getRandomInteger(50, 100);

                    } else {
                        durability = MathUtils.getRandomInteger(25, 75);
                    }

                } else {
                    durability = MathUtils.getRandomInteger(5, 50);
                }

                ((AbstractEquipment) item).setDurability(durability);
            }

            items.put(item, raritiesModifiers.get(rarity) / itemTypeModifier.get(type));
        });

        return items;
    }

    public List<AbstractItem> getRandomLoot(HashMap<AbstractItem, Double> possibleLoot, int amount) {
        List<AbstractItem> loot = new ArrayList<>();

        while (loot.size() != amount) {
            AbstractItem item = new ArrayList<>(possibleLoot.keySet()).get(MathUtils.getRandomInteger(0, possibleLoot.size() - 1));

            if (loot.contains(item)) continue;

            if (MathUtils.getRandom(possibleLoot.get(item)))
                loot.add(item);

        }

        return loot;
    }

    public List<AbstractItem> getRandomLoot(DungeonSettings dungeonSettings, int amount) {
        List<AbstractItem> loot = new ArrayList<>();
        HashMap<AbstractItem, Double> possibleLoot = gatherItems(dungeonSettings);

        while (loot.size() != amount) {
            AbstractItem item = new ArrayList<>(possibleLoot.keySet()).get(MathUtils.getRandomInteger(0, possibleLoot.size() - 1));

            if (loot.contains(item)) continue;

            if (MathUtils.getRandom(possibleLoot.get(item)))
                loot.add(item);
        }

        return loot;
    }

    public List<AbstractItem> getRandomLoot(DungeonSettings dungeonSettings, int min, int max) {
        List<AbstractItem> loot = new ArrayList<>();
        HashMap<AbstractItem, Double> possibleLoot = gatherItems(dungeonSettings);

        int amount;
        if (min == 0) {
            amount = MathUtils.getRandom(0.7) ? 0 : MathUtils.getRandomInteger(min, max);
        } else {
            amount = MathUtils.getRandomInteger(min, max);
        }

        while (loot.size() != amount) {
            AbstractItem item = new ArrayList<>(possibleLoot.keySet()).get(MathUtils.getRandomInteger(0, possibleLoot.size() - 1));

            if (loot.contains(item)) continue;

            if (MathUtils.getRandom(possibleLoot.get(item)))
                loot.add(item);
        }

        return loot;
    }

    public List<AbstractItem> getRandomLoot(DungeonSettings dungeonSettings) {
        List<AbstractItem> loot = new ArrayList<>();
        HashMap<AbstractItem, Double> possibleLoot = gatherItems(dungeonSettings);

        possibleLoot.forEach((item, chance) -> {
            if (loot.contains(item)) return;

            if (MathUtils.getRandom(possibleLoot.get(item)))
                loot.add(item);

        });

        return loot;

    }

}
