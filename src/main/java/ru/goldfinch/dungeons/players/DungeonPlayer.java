package ru.goldfinch.dungeons.players;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.game.items.AbstractItem;
import ru.goldfinch.dungeons.game.items.ItemsManager;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.types.*;
import ru.goldfinch.dungeons.utils.ItemBuilder;
import ru.goldfinch.dungeons.utils.PlayerUtils;
import ru.goldfinch.dungeons.utils.inventoryservice.ClickableItem;
import ru.goldfinch.dungeons.utils.inventoryservice.GUI;
import ru.goldfinch.dungeons.utils.inventoryservice.content.Pagination;
import ru.goldfinch.dungeons.utils.inventoryservice.content.SlotIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor@Data
public class DungeonPlayer {

    private final static CodecRegistry codec = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder()
                    .automatic(true)
                    .build()));

    @Getter
    private final static MongoCollection<DungeonPlayer> playersCollection =
            Dungeons.getInstance().getMongo().getDatabase().withCodecRegistry(codec)
                    .getCollection("players", DungeonPlayer.class);

    private UUID id;
    private int level;
    private int experience;
    private int gold;

    private int maxHealth;
    private int health;

    private List<Document> storage;
    private List<Document> inventory;

    private int kills;
    private int deaths;
    private int raids;
    private int escapes;

    public DungeonPlayer(UUID id) {
        this.id = id;

        this.level = 1;
        this.experience = 0;
        this.gold = 0;

        this.maxHealth = 20;
        this.health = 20;

        this.storage = new ArrayList<>();
        this.inventory = new ArrayList<>();

        this.kills = 0;
        this.deaths = 0;
        this.escapes = 0;
        this.raids = 0;

        playersCollection.insertOne(this);
        load();
    }

    public static DungeonPlayer get(UUID id) {
        return Dungeons.getInstance().getPlayersManager().getPlayers().stream()
                .filter(dungeonPlayer -> dungeonPlayer.getId().equals(id))
                .findFirst().orElse(null);
    }

    public static DungeonPlayer find(UUID id) {
        DungeonPlayer dungeonPlayer = playersCollection.find(Filters.eq("_id", id)).first();
        if (dungeonPlayer == null) {
            return null;
        } else {
            dungeonPlayer.load();
            return dungeonPlayer;
        }
    }

    public void load() {
        System.out.println("load");
        this.loadInventory();
        Dungeons.getInstance().getPlayersManager().getPlayers().add(playersCollection.find(Filters.eq("_id", id)).first());
    }

    public void unload() {
        System.out.println("unload");
        this.saveInventory();
        this.save();
        Dungeons.getInstance().getPlayersManager().getPlayers().remove(this);

    }

    public void save() {
        playersCollection.replaceOne(Filters.eq("_id", id), this);
    }

    public void kill() {
        this.deaths++;
        if (toBukkitPlayer() != null) toBukkitPlayer().getInventory().clear();
        this.clearInventory();
        this.setHealth(this.getMaxHealth());
        this.unload();
    }

    public Player toBukkitPlayer() {
        return Bukkit.getPlayer(id);
    }

    public void addItem(AbstractItem item) {
        this.storage.add(item.toDocument());
    }

    public void removeItem(AbstractItem item) {
        this.storage.remove(item.toDocument());
    }

    public void openStorageMenu() {
        ItemStack nextPage = new ItemBuilder(Material.ARROW)
                .displayName(DungeonColor.GREEN + "Следующая страница")
                .build();

        ItemStack previousPage = new ItemBuilder(Material.ARROW)
                .displayName(DungeonColor.GREEN + "Предыдущая страница")
                .build();

        GUI gui = GUI.builder()
                .title("Хранилище")
                .size(6, 9)
                .provider(((player, contents) -> {
                    List<ClickableItem> clickableItems = new ArrayList<>();
                    Pagination pagination = contents.pagination();

                    storage.forEach(document -> {
                        ItemType itemType = ItemType.valueOf(document.getString("type"));
                        AbstractItem dungeonItem;
                        switch (itemType) {
                            case ARMOR:
                                dungeonItem = ArmorItem.fromDocument(document);
                                break;
                            case WEAPON:
                                dungeonItem = WeaponItem.fromDocument(document);
                                break;
                            case KEY:
                                dungeonItem = KeyItem.fromDocument(document);
                                break;
                            case GOLD:
                                dungeonItem = GoldItem.fromDocument(document);
                                break;
                            case OTHER:
                                dungeonItem = PlainItem.fromDocument(document);
                                break;
                            default:
                                dungeonItem = null;
                                break;
                        }

                        clickableItems.add(ClickableItem.of(dungeonItem.toItem(), e -> {
                            if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) return;

                            if (PlayerUtils.hasSpace(player, dungeonItem.toItem())) {
                                removeItem(dungeonItem);
                                player.getInventory().addItem(dungeonItem.toItem());
                                Dungeons.getInstance().getGuiManager().getInventory(player).get().open(player, pagination.getPage());

                                player.sendMessage(DungeonColor.GREEN + "Предмет успешно добавлен в инвентарь!");
                            } else {
                                player.sendMessage(DungeonColor.RED + "Недостаточно места в инвентаре!");
                            }
                        }));
                    });

                    ClickableItem openSlot = ClickableItem.of(new ItemBuilder(Material.STAINED_GLASS_PANE, 5).displayName("&aСвободный слот").build(), event -> {
                        ItemStack itemStack = event.getCursor();

                        if (!Dungeons.getInstance().getItemsManager().isDungeonItem(itemStack)) {
                            player.sendMessage("&aВы не можете положить этот предмет в хранилище!");
                            return;
                        }

                        AbstractItem dungeonItem = Dungeons.getInstance().getItemsManager().fromItem(itemStack);
                        addItem(dungeonItem);

                        player.getInventory().removeItem(itemStack);
                        player.setItemOnCursor(null);

                        Dungeons.getInstance().getGuiManager().getInventory(player).get().open(player, pagination.getPage());
                        player.sendMessage(DungeonColor.GREEN + "Предмет успешно добавлен в хранилище!");
                    });

                    for (int i = 0; i != 90; i++) clickableItems.add(openSlot);

                    SlotIterator slotIterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0);
                    pagination.setItemsPerPage(45);
                    pagination.setItems(clickableItems);
                    pagination.addToIterator(slotIterator);

                    if (!pagination.isFirst())
                        contents.set(5, 3, ClickableItem.of(previousPage, e -> Dungeons.getInstance().getGuiManager().getInventory(player).get().open(player, pagination.previous().getPage())));

                    if (!pagination.isLast())
                        contents.set(5, 5, ClickableItem.of(nextPage, e -> Dungeons.getInstance().getGuiManager().getInventory(player).get().open(player, pagination.next().getPage())));

                }))
                .build();

        gui.open(Bukkit.getPlayer(id));
    }

    public void loadInventory() {
        toBukkitPlayer().getInventory().clear();

        inventory.forEach(document -> {
            ItemType itemType = ItemType.valueOf(document.getString("type"));
            ItemStack itemStack;


            switch (itemType) {
                case WEAPON:
                    itemStack = WeaponItem.fromDocument(document).toItem();
                    break;
                case ARMOR:
                    itemStack = ArmorItem.fromDocument(document).toItem();
                    break;
                case KEY:
                    itemStack = KeyItem.fromDocument(document).toItem();
                    break;
                case GOLD:
                    itemStack = GoldItem.fromDocument(document).toItem();
                    break;
                case OTHER:
                    itemStack = PlainItem.fromDocument(document).toItem();
                    break;
                default:
                    itemStack = null;
                    break;
            }

            toBukkitPlayer().getInventory().setItem(document.getInteger("slot"), itemStack);
        });
    }

    public void saveInventory() {
        ItemsManager itemsManager = Dungeons.getInstance().getItemsManager();

        if (toBukkitPlayer() == null) return;

        clearInventory();

        for (int i = 0; i < toBukkitPlayer().getInventory().getSize(); i++) {
            ItemStack itemStack = toBukkitPlayer().getInventory().getItem(i);

            if (!itemsManager.isDungeonItem(itemStack))
                continue;

            if (itemsManager.decodeItem(itemStack) == null)
                continue;

            inventory.add(itemsManager.decodeItem(itemStack).toDocument().append("slot", i));
        }


    }

    public void clearInventory() {
        this.inventory.clear();
    }

}