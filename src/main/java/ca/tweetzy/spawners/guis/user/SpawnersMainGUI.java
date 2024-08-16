/*
 * Spawners
 * Copyright 2022 Kiran Hart
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
package ca.tweetzy.spawners.guis.user;

import ca.tweetzy.flight.comp.enums.CompMaterial;
import ca.tweetzy.flight.settings.TranslationManager;
import ca.tweetzy.flight.utils.Common;
import ca.tweetzy.flight.utils.QuickItem;
import ca.tweetzy.spawners.Spawners;
import ca.tweetzy.spawners.api.spawner.SpawnerUser;
import ca.tweetzy.spawners.guis.SpawnersBaseGUI;
import ca.tweetzy.spawners.model.hook.ShopsHook;
import ca.tweetzy.spawners.settings.Settings;
import ca.tweetzy.spawners.settings.Translations;
import lombok.NonNull;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.inventory.ItemStack;

/**
 * Date Created: June 07 2022
 * Time Created: 6:27 p.m.
 *
 * @author Kiran Hart
 */
public final class SpawnersMainGUI extends SpawnersBaseGUI {

	final SpawnerUser spawnerUser;

	public SpawnersMainGUI(@NonNull final SpawnerUser spawnerUser) {
		super(null, TranslationManager.string(Translations.GUI_MAIN_TITLE), Settings.GUI_MAIN_ROWS.getInt());
		setDefaultItem(QuickItem.of(Settings.GUI_MAIN_BG.getMaterial()).name(" ").make());
		this.spawnerUser = spawnerUser;
		draw();
	}

	@Override
	protected void draw() {
		Settings.GUI_MAIN_FILL_DECORATION.getStringList().forEach(deco -> {
			final String[] split = deco.split(":");

			if (split.length < 2) return;
			if (!NumberUtils.isNumber(split[0])) return;

			int slot = Integer.parseInt(split[0]);
			final ItemStack decoItem = CompMaterial.matchCompMaterial(split[1].toUpperCase()).orElse(Settings.GUI_MAIN_BG.getMaterial()).parseItem();

			setItem(slot, decoItem);
		});

		// 0 1 2 3 4 5 6 7 8

		setButton(1, Settings.ENABLE_SHOP.getBoolean() ? 2 : 4, QuickItem
				.of(CompMaterial.SPAWNER)
				.name(TranslationManager.string(Translations.GUI_MAIN_ITEMS_YOUR_SPAWNERS_NAME))
				.lore(TranslationManager.list(Translations.GUI_MAIN_ITEMS_YOUR_SPAWNERS_LORE, "total_placed_spawners", this.spawnerUser.getPlacedSpawners().size()))
				.hideTags(true)
				.make(), click -> click.manager.showGUI(click.player, new SpawnerListGUI(this.spawnerUser)));

		if (Settings.ENABLE_SHOP.getBoolean())
			setButton(1, 6, QuickItem.of(CompMaterial.EMERALD)
					.name(TranslationManager.string(Translations.GUI_MAIN_ITEMS_SHOP_NAME))
					.lore(TranslationManager.list(Translations.GUI_MAIN_ITEMS_SHOP_LORE))
					.make(), click -> {

				if (Spawners.getEconomy() == null) {
					Common.tell(click.player, "&cNo economy provider found, contact a server admin about this.");
					return;
				}

				if (Settings.USE_SHOPS_FOR_SELLING.getBoolean() && ShopsHook.isShopsEnabled()) {
					ShopsHook.handleCommand(click,this.spawnerUser);
					return;
				}

				click.manager.showGUI(click.player, new SpawnerShopGUI(this.spawnerUser));
			});
	}
}
