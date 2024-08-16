/*
 * Spawners
 * Copyright 2024 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ca.tweetzy.spawners.model.hook;

import ca.tweetzy.flight.gui.events.GuiClickEvent;
import ca.tweetzy.shops.Shops;
import ca.tweetzy.shops.api.shop.Shop;
import ca.tweetzy.shops.gui.user.ShopContentsGUI;
import ca.tweetzy.spawners.api.spawner.SpawnerUser;
import ca.tweetzy.spawners.guis.user.SpawnerShopGUI;
import ca.tweetzy.spawners.settings.Settings;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public final class ShopsHook {


	public boolean isShopsEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Shops");
	}

	public Shop getSpawnerShop() {
		if (!isShopsEnabled()) return null;
		return Shops.getShopManager().getById(Settings.SHOPS_ID.getString());
	}

	public void handleCommand(GuiClickEvent click, SpawnerUser spawnerUser) {
		final Shop shop = ShopsHook.getSpawnerShop();
		if (shop == null)
			click.manager.showGUI(click.player, new SpawnerShopGUI(spawnerUser));
		else
			Shops.getGuiManager().showGUI(click.player, new ShopContentsGUI(null, click.player, shop, true));
	}
}
