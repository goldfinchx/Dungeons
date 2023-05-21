package ru.goldfinch.dungeons;

import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.goldfinch.dungeons.game.items.ItemsManager;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.game.items.AbstractItem;
import ru.goldfinch.dungeons.game.items.parameters.DamageType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.game.items.parameters.armor.ArmorParameter;
import ru.goldfinch.dungeons.game.items.parameters.weapon.AttackRange;
import ru.goldfinch.dungeons.game.items.parameters.weapon.AttackSpeed;
import ru.goldfinch.dungeons.game.items.parameters.weapon.WeaponParameter;
import ru.goldfinch.dungeons.game.items.types.*;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.generator.rooms.types.MobRoom;
import ru.goldfinch.dungeons.match.player.DummyPlayer;
import ru.goldfinch.dungeons.match.player.MatchPlayer;
import ru.goldfinch.dungeons.match.parameteres.PlayerRemoveReason;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TestCommand extends Command {
    protected TestCommand() {
        super("test");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return List.of("remove", "add");
        }

        return super.tabComplete(sender, alias, args);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player player = (Player) sender;

        switch (args[0]) {
            case "kill": {
                DummyPlayer dummyPlayer = (DummyPlayer) Dungeons.getInstance().getMatch().getAllPlayers().stream().filter(matchPlayer -> matchPlayer instanceof DummyPlayer).findFirst().get();
                Dungeons.getInstance().getMatch().removePlayer(dummyPlayer, PlayerRemoveReason.KILLED_BY_PLAYER, player);
                break;
            }
            case "add": {
                Dungeons.getInstance().getMatch().addDummy(new DummyPlayer(player));
                break;
            }
            case "spawn": {
                Room room = Dungeons.getInstance().getDungeon().findRoom(player.getLocation());
                if (room == null || room.getType() != RoomType.MOB) {
                    player.sendMessage("§cВы не находитесь в комнате с мобами!");
                } else {
                    MobRoom mobRoom = (MobRoom) room;
                    mobRoom.spawnMobs(5, mobRoom.getAvailableMobs().get(0));
                    player.sendMessage("§aМобы спавнены!");
                }
                break;
            }

            case "remove": {
                Dungeons.getInstance().getMatch().removeDummy();
                break;
            }

            case "clear": {
                DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());
                dungeonPlayer.clearInventory();
            }

            case "team": {
                MatchPlayer matchPlayer = MatchPlayer.get(player.getUniqueId());
                matchPlayer.openTeamMenu();
                break;
            }
            case "drop":
                ItemsManager.getItemsCollection().find().forEach(document -> {
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), Dungeons.getInstance().getItemsManager().getItem(document).toItem());
                });
                break;
            case "equipment":
                Dungeons.getInstance().getItemsManager().saveItem(new WeaponItem("Тестовый меч",
                        "Описание тестового меча",
                        Material.IRON_SWORD,
                        Rarity.COMMON,
                        1,
                        100,
                        AttackSpeed.NORMAL,
                        AttackRange.LONG,
                        Range.between(10, 100),
                        new HashMap<>() {{
                            put(WeaponParameter.CRITICAL, 1);
                            put(WeaponParameter.STUN, 5);
                        }}));

                Dungeons.getInstance().getItemsManager().saveItem(new WeaponItem("Тестовый меч",
                        "Описание тестового меча",
                        Material.DIAMOND_AXE,
                        Rarity.RARE,
                        5,
                        100,
                        AttackSpeed.NORMAL,
                        AttackRange.LONG,
                        Range.between(10, 100),
                        new HashMap<>() {{
                            put(WeaponParameter.CRITICAL, 1);
                            put(WeaponParameter.STUN, 5);
                        }}));

                Dungeons.getInstance().getItemsManager().saveItem(new ArmorItem("Тестовый шлем",
                        "Тестовый доспех",
                        Material.IRON_HELMET,
                        Rarity.COMMON,
                        1,
                        100,
                        new HashMap<>() {{
                            put(DamageType.PHYSICAL, 50);
                        }},
                        new HashMap<>() {{
                            put(ArmorParameter.ADDITIONAL_HP, 1);
                        }}));

                Dungeons.getInstance().getItemsManager().saveItem(new ArmorItem("ТестовыЙ нагрудник",
                        "Тестовый доспех",
                        Material.IRON_CHESTPLATE,
                        Rarity.COMMON,
                        1,
                        100,
                        new HashMap<>() {{
                            put(DamageType.PHYSICAL, 20);
                            put(DamageType.FREEZE, 10);
                        }},
                        new HashMap<>() {{
                            put(ArmorParameter.ADDITIONAL_HP, 1);
                        }}));

                Dungeons.getInstance().getItemsManager().saveItem(new ArmorItem("Тестовый штаны",
                        "Тестовый доспех",
                        Material.IRON_LEGGINGS,
                        Rarity.COMMON,
                        1,
                        100,
                        new HashMap<>() {{
                            put(DamageType.FIRE, 20);
                        }},
                        new HashMap<>() {{
                            put(ArmorParameter.ADDITIONAL_HP, 1);
                        }}));

                Dungeons.getInstance().getItemsManager().saveItem(new ArmorItem("Тестовые ботинки",
                        "Тестовый доспех",
                        Material.IRON_BOOTS,
                        Rarity.COMMON,
                        1,
                        100,
                        new HashMap<>() {{
                            put(DamageType.PHYSICAL, 20);
                        }},
                        new HashMap<>() {{
                            put(ArmorParameter.ADDITIONAL_HP, 1);
                        }}));


                break;

            case "item": {
                for (int i = 0; i != 25; i++) {
                    List<Material> materials = Arrays.stream(Material.values()).filter(material -> material.isItem() && material.isSolid()).collect(Collectors.toList());
                    PlainItem plainItem = new PlainItem("Тестовый предмет",
                            "Тестовый предмет",
                            materials.get(MathUtils.getRandomInteger(0, materials.size() - 1)),
                            MathUtils.getRandom(0.2) ? (MathUtils.getRandom(0.3) ? Rarity.EPIC : Rarity.RARE) : Rarity.COMMON,
                            MathUtils.getRandomInteger(0, 10));


                    materials = Arrays.stream(Material.values()).filter(material -> material.isItem() && (material.name().endsWith("_CHESTPLATE") || material.name().endsWith("_HELMET") || material.name().endsWith("_LEGGINGS") || material.name().endsWith("BOOTS"))).collect(Collectors.toList());
                    ArmorItem armorItem = new ArmorItem("Тестовый доспех",
                            "Тестовый доспех",
                            materials.get(MathUtils.getRandomInteger(0, materials.size() - 1)),
                            MathUtils.getRandom(0.2) ? (MathUtils.getRandom(0.3) ? Rarity.EPIC : Rarity.RARE) : Rarity.COMMON,
                            MathUtils.getRandomInteger(0, 10),
                            100,
                            new HashMap<>() {{
                                int i = MathUtils.getRandomInteger(1, 4);

                                for (int j = 0; j != i; j++) {
                                    put(DamageType.values()[MathUtils.getRandomInteger(0, ArmorParameter.values().length - 1)], MathUtils.getRandomInteger(10, 100));
                                }

                            }},
                            new HashMap<>());

                    materials = Arrays.stream(Material.values()).filter(material -> material.isItem() && (material.name().endsWith("_SWORD") || material.name().endsWith("_AXE"))).collect(Collectors.toList());
                    int range = MathUtils.getRandomInteger(10, 45);
                    WeaponItem weaponItem = new WeaponItem("Тестовый меч",
                            "Тестовый меч",
                            materials.get(MathUtils.getRandomInteger(0, materials.size() - 1)),
                            MathUtils.getRandom(0.2) ? (MathUtils.getRandom(0.3) ? Rarity.EPIC : Rarity.RARE) : Rarity.COMMON,
                            10,
                            100,
                            MathUtils.getRandom(0.2) ? (MathUtils.getRandom(0.3) ? AttackSpeed.VERY_FAST : AttackSpeed.FAST) : AttackSpeed.NORMAL,
                            MathUtils.getRandom(0.2) ? (MathUtils.getRandom(0.3) ? AttackRange.VERY_LONG : AttackRange.NORMAL) : AttackRange.SHORT,
                            Range.between(range, range + MathUtils.getRandomInteger(0, 50)),
                            new HashMap<>() {{
                                put(WeaponParameter.BLEEDING, 10);
                                put(WeaponParameter.CRITICAL, 15);
                            }});

                    Dungeons.getInstance().getItemsManager().saveItem(armorItem);
                    Dungeons.getInstance().getItemsManager().saveItem(weaponItem);
                    Dungeons.getInstance().getItemsManager().saveItem(plainItem);
                }

                KeyItem keyItem = new KeyItem("Тестовый ключ",
                        "Тестовый ключ",
                        MathUtils.getRandom(0.2) ? (MathUtils.getRandom(0.3) ? Rarity.EPIC : Rarity.RARE) : Rarity.COMMON,
                        MathUtils.getRandomInteger(0, 10));
                Dungeons.getInstance().getItemsManager().saveItem(keyItem);
                break;
            }

            case "generate": {
                HashMap<AbstractItem, Double> possibleLoot = Dungeons.getInstance().getItemsManager().gatherItems(DungeonSettings.MEDIEVAL);
                possibleLoot.put(new GoldItem(1 + MathUtils.getRandomInteger(0, 10)), 0.5);
                possibleLoot.put(new GoldItem(10 + MathUtils.getRandomInteger(0, 50)), 0.025);
                possibleLoot.put(new GoldItem(100 + MathUtils.getRandomInteger(0, 100)), .01);

                List<AbstractItem> items = Dungeons.getInstance().getItemsManager().getRandomLoot(possibleLoot, Integer.parseInt(args[1]));

                Inventory inventory = Bukkit.createInventory(null, 54, "Тестовый инвентарь");
                items.forEach(item -> inventory.addItem(item.toItem()));

                player.openInventory(inventory);
            }
        }

        return false;
    }
}
