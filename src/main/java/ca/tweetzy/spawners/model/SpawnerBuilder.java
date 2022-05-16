package ca.tweetzy.spawners.model;

import ca.tweetzy.rose.utils.ChatUtil;
import ca.tweetzy.rose.utils.QuickItem;
import ca.tweetzy.spawners.Spawners;
import ca.tweetzy.spawners.api.LevelOption;
import ca.tweetzy.spawners.api.spawner.Level;
import ca.tweetzy.spawners.settings.Settings;
import ca.tweetzy.spawners.settings.Translation;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Date Created: May 15 2022
 * Time Created: 2:47 p.m.
 *
 * @author Kiran Hart
 */
@AllArgsConstructor
public final class SpawnerBuilder {

	public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private UUID ownerUUID;

	private String ownerName;

	private EntityType entityType;

	private boolean levelCanBeChanged;

	private final List<Level> levels = new ArrayList<>();

	public static SpawnerBuilder of(@NonNull final Player player) {
		return SpawnerBuilder.of(player, EntityType.PIG);
	}

	public static SpawnerBuilder of(@NonNull final Player player, final EntityType entityType) {
		return new SpawnerBuilder(player.getUniqueId(), player.getName(), entityType, true);
	}

	public SpawnerBuilder setNoOwner() {
		this.ownerName = Translation.SPAWNER_NO_OWNER.getString();
		this.ownerUUID = NULL_UUID;
		return this;
	}

	public SpawnerBuilder setOwner(@NonNull final Player player) {
		this.ownerUUID = player.getUniqueId();
		this.ownerName = player.getName();
		return this;
	}

	public SpawnerBuilder setEntity(@NonNull final EntityType entity) {
		this.entityType = entity;
		return this;
	}

	public SpawnerBuilder setLevelCanBeChanged(final boolean allowLevelUpgrade) {
		this.levelCanBeChanged = allowLevelUpgrade;
		return this;
	}


	public SpawnerBuilder addLevel(@NonNull final Level level) {
		this.levels.remove(level);
		this.levels.add(level);
		return this;
	}

	public SpawnerBuilder addLevel(@NonNull final LevelOption option, final int levelNumber) {
		final Level foundLevel = Spawners.getLevelManager().find(option, levelNumber);
		if (foundLevel != null) {
			this.levels.remove(foundLevel);
			this.levels.add(foundLevel);
		}

		return this;
	}

	public ItemStack make() {
		final QuickItem itemStack = QuickItem.of(Settings.SPAWNER_ITEM.getMaterial());

		final int delay = this.levels.stream().filter(level -> level.getLevelOption() == LevelOption.SPAWN_INTERVAL).findFirst().map(Level::getLevelNumber).orElse(1);
		final int spawnCount = this.levels.stream().filter(level -> level.getLevelOption() == LevelOption.SPAWN_COUNT).findFirst().map(Level::getLevelNumber).orElse(1);
		final int maxNearby = this.levels.stream().filter(level -> level.getLevelOption() == LevelOption.MAX_NEARBY_ENTITIES).findFirst().map(Level::getLevelNumber).orElse(1);
		final int activationRange = this.levels.stream().filter(level -> level.getLevelOption() == LevelOption.ACTIVATION_RANGE).findFirst().map(Level::getLevelNumber).orElse(1);

		itemStack.name(Translation.SPAWNER_NAME.getString("entity_type", ChatUtil.capitalizeFully(this.entityType)));
		itemStack.lore(Translation.SPAWNER_LORE.getList(
				"spawner_owner_name", this.ownerName,
				"spawner_spawn_delay_level", delay,
				"spawner_spawn_count_level", spawnCount,
				"spawner_max_nearby_entities_level", maxNearby,
				"spawner_player_activation_range_level", activationRange
		));

		itemStack.tag("Spawners:ownerName", this.ownerName);
		itemStack.tag("Spawners:ownerUUID", this.ownerUUID.toString());
		itemStack.tag("Spawners:entity", this.entityType.name());
		itemStack.tag("Spawners:upgradeable",  String.valueOf(this.levelCanBeChanged));

		itemStack.tag("Spawners:delay", String.valueOf(delay));
		itemStack.tag("Spawners:spawnCount", String.valueOf(spawnCount));
		itemStack.tag("Spawners:maxNearby", String.valueOf(maxNearby));
		itemStack.tag("Spawners:activationRange", String.valueOf(activationRange));

		itemStack.hideTags(true);
		return itemStack.make();
	}
}