package ca.tweetzy.spawners.listeners;

import ca.tweetzy.rose.comp.enums.CompMaterial;
import ca.tweetzy.spawners.Spawners;
import ca.tweetzy.spawners.api.spawner.Spawner;
import ca.tweetzy.spawners.api.spawner.SpawnerUser;
import ca.tweetzy.spawners.settings.Settings;
import ca.tweetzy.spawners.settings.Translation;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Date Created: May 10 2022
 * Time Created: 10:40 p.m.
 *
 * @author Kiran Hart
 */
public final class EggListeners implements Listener {

	@EventHandler
	public void onEggThrow(final PlayerInteractEvent event) {
		if (!Settings.ALLOW_SPAWN_EGG_THROW.getBoolean()) return;

		if (event.getAction() != Action.LEFT_CLICK_AIR) return;
		if (event.getHand() == EquipmentSlot.OFF_HAND) return;

		final Player player = event.getPlayer();
		final SpawnerUser spawnerUser = Spawners.getPlayerManager().findUser(player);
		final ItemStack hand = event.getItem();

		if (spawnerUser == null) return;
		if (hand == null) return;
		if (!hand.getType().name().endsWith("_SPAWN_EGG")) return;

		final EntityType entityType = EntityType.valueOf(hand.getType().name().replace("_SPAWN_EGG", ""));

		if (!spawnerUser.isAllowedToThrowSpawnEgg(player, entityType)) {
			Translation.NOT_ALLOWED_TO_THROW_EGG.send(player, "entity_type", StringUtils.capitalize(entityType.name().toLowerCase().replace("_", " ")));
			return;
		}

		final Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(1.35)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());

		final Egg egg = player.getWorld().spawn(loc, Egg.class);

		egg.getPersistentDataContainer().set(new NamespacedKey(Spawners.getInstance(), "SpawnersThrownEgg"), PersistentDataType.STRING, entityType.name());
		egg.setShooter(player);
		egg.setVelocity(player.getEyeLocation().getDirection().multiply(1.35));
	}

	@EventHandler
	public void onSpawnEggLand(final ProjectileHitEvent event) {
		final Projectile projectile = event.getEntity();
		final NamespacedKey key = new NamespacedKey(Spawners.getInstance(), "SpawnersThrownEgg");

		if (!projectile.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

		final EntityType entityType = EntityType.valueOf(projectile.getPersistentDataContainer().get(key, PersistentDataType.STRING));

		Location hitLocation = null;
		if (event.getHitEntity() != null)
			hitLocation = event.getHitEntity().getLocation();

		if (event.getHitBlock() != null)
			hitLocation = event.getHitBlock().getLocation();

		if (hitLocation == null) return;
		if (hitLocation.getWorld() == null) return; // not sure this would ever happen, but added coz intellij is yelling at me

		hitLocation.add(0, 1, 0);
		hitLocation.getWorld().spawnEntity(hitLocation, entityType);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onSpawnerClickWithEgg(final PlayerInteractEvent event) {
		if (!Settings.ALLOW_SPAWNER_CHANGE_WITH_EGG.getBoolean()) return;
		if (event.getClickedBlock() == null || event.getClickedBlock().getType() != CompMaterial.SPAWNER.parseMaterial()) return;
		if (event.getHand() == EquipmentSlot.OFF_HAND) return;

		final Player player = event.getPlayer();
		final SpawnerUser spawnerUser = Spawners.getPlayerManager().findUser(player);
		final ItemStack hand = event.getItem();

		if (hand == null) return;

		if (!hand.getType().name().endsWith("_SPAWN_EGG")) return;
		final EntityType entityType = EntityType.valueOf(hand.getType().name().replace("_SPAWN_EGG", ""));

		final Block block = event.getClickedBlock();
		final CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();

		// check spawner owner
		final Spawner spawner = Spawners.getSpawnerManager().find(block.getLocation());

		if (spawner != null && !spawner.getOwner().equals(player.getUniqueId())) {
			Translation.SPAWNER_NOT_OWNER_CHANGE_WITH_EGG.send(player, "owner_name", spawner.getOwnerName());
			event.setUseItemInHand(Event.Result.DENY);
			event.setCancelled(true);
			return;
		}

		if (!spawnerUser.isAllowedToChangeWithEgg(player, entityType)) {
			Translation.SPAWNER_CANNOT_CHANGE_WITH_EGG.send(player, "entity_type", StringUtils.capitalize(entityType.name().toLowerCase().replace("_", " ")));
			return;
		}

		creatureSpawner.setSpawnedType(entityType);
		creatureSpawner.update(true);

		if (spawner != null) {
			spawner.setEntityType(entityType);
			spawner.sync();
		}

		event.setUseItemInHand(Event.Result.DENY);

		if (Settings.REMOVE_EGG_ON_SPAWNER_CHANGE.getBoolean()) {
			if (hand.getAmount() >= 2) {
				hand.setAmount(hand.getAmount() - 1);
				player.getInventory().setItemInMainHand(hand);
				return;
			}

			player.getInventory().setItemInMainHand(CompMaterial.AIR.parseItem());
		}
	}
}
