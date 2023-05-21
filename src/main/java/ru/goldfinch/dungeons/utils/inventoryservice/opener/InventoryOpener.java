package ru.goldfinch.dungeons.utils.inventoryservice.opener;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import ru.goldfinch.dungeons.utils.inventoryservice.ClickableItem;
import ru.goldfinch.dungeons.utils.inventoryservice.GUI;
import ru.goldfinch.dungeons.utils.inventoryservice.content.InventoryContents;


public interface InventoryOpener {

    Inventory open(GUI inv, Player player);
    boolean supports(InventoryType type);

    default void fill(Inventory handle, InventoryContents contents) {
        ClickableItem[][] items = contents.all();

        for(int row = 0; row < items.length; row++) {
            for(int column = 0; column < items[row].length; column++) {
                if(items[row][column] != null)
                    handle.setItem(9 * row + column, items[row][column].getItem());
            }
        }
    }

}
