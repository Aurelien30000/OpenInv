/*
 * Copyright (C) 2011-2023 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ViewOnly;
import com.lishid.openinv.util.InventoryAccess;
import com.lishid.openinv.util.Permissions;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for inventory-related events to prevent modification of inventories where not allowed.
 *
 * @author Jikoo
 */
record InventoryListener(OpenInv plugin) implements Listener {

    @Keep
    @EventHandler
    private void onInventoryClose(@NotNull final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (this.plugin.getSilentContainerStatus(player)
                && holder != null
                && this.plugin.getAnySilentContainer().isAnySilentContainer(holder)) {
            this.plugin.getAnySilentContainer().deactivateContainer(player);
        }

        ISpecialInventory specialInventory = InventoryAccess.getEnderChest(event.getInventory());
        if (specialInventory != null) {
            this.plugin.handleCloseInventory(specialInventory);
        } else {
            specialInventory = InventoryAccess.getPlayerInventory(event.getInventory());
            if (specialInventory != null) {
                this.plugin.handleCloseInventory(specialInventory);
            }
        }
    }

    @Keep
    @EventHandler(priority = EventPriority.LOWEST)
    private void onInventoryClick(@NotNull final InventoryClickEvent event) {
        handleInventoryInteract(event);
    }

    @Keep
    @EventHandler(priority = EventPriority.LOWEST)
    private void onInventoryDrag(@NotNull final InventoryDragEvent event) {
        handleInventoryInteract(event);
    }

    /**
     * Handle common InventoryInteractEvent functions.
     *
     * @param event the InventoryInteractEvent
     */
    private void handleInventoryInteract(@NotNull final InventoryInteractEvent event) {
        HumanEntity entity = event.getWhoClicked();

        // Un-cancel spectator interactions.
        if (entity.getGameMode() == GameMode.SPECTATOR && Permissions.SPECTATE_CLICK.hasPermission(entity)) {
            event.setCancelled(false);
        }

        if (event.isCancelled()) {
            return;
        }

        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof ViewOnly) {
            event.setCancelled(true);
        }
    }

}
