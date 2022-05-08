package ca.tweetzy.spawners.model.manager;

import ca.tweetzy.spawners.Spawners;
import ca.tweetzy.spawners.api.spawner.Spawner;
import ca.tweetzy.spawners.settings.Settings;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Date Created: May 05 2022
 * Time Created: 10:47 a.m.
 *
 * @author Kiran Hart
 */
public final class SpawnerManager implements Manager {

	private final Map<Location, Spawner> spawners = new ConcurrentHashMap<>();

	public void addSpawner(@NonNull final Spawner spawner) {
		if (this.spawners.containsKey(spawner.getLocation())) return;
		this.spawners.put(spawner.getLocation(), spawner);
	}

	public void removeSpawner(@NonNull final Location location) {
		if (!this.spawners.containsKey(location)) return;
		this.spawners.remove(location);
	}

	public Spawner findSpawner(@NonNull final Location location) {
		return this.spawners.getOrDefault(location, null);
	}

	public Spawner findSpawner(@NonNull final Spawner spawner) {
		return findSpawner(spawner.getLocation());
	}

	/*
	=================== DATABASE CALLS ===================
	 */

	public void createSpawner(@NonNull final Spawner spawner, final BiConsumer<Boolean, Spawner> consumer) {
		Spawners.getDataManager().insertSpawner(spawner, (error, created) -> {
			if (error == null)
				this.addSpawner(created);

			if (consumer != null)
				consumer.accept(error == null, created);
		});
	}

	public void deleteSpawner(@NonNull final Spawner spawner, final Consumer<Boolean> success) {
		Spawners.getDataManager().deleteSpawner(spawner.getID(), (error, deleted) -> {
			if (error == null && deleted)
				this.removeSpawner(spawner.getLocation());

			if (success != null)
				success.accept(error == null && deleted);
		});
	}

	public List<Spawner> getSpawners() {
		return List.copyOf(this.spawners.values());
	}

	public void applySpawnerDefaults(@NonNull final CreatureSpawner spawner, final boolean update) {
		spawner.setDelay(Settings.DEFAULT_SPAWNER_DELAY.getInt());
		spawner.setSpawnCount(Settings.DEFAULT_SPAWNER_SPAWN_COUNT.getInt());
		spawner.setRequiredPlayerRange(Settings.DEFAULT_SPAWNER_ACTIVATION_RANGE.getInt());
		spawner.setMaxNearbyEntities(Settings.DEFAULT_SPAWNER_MAX_NEARBY_ENTITIES.getInt());

		if (update)
			spawner.update(true);
	}

	@Override
	public void load() {
		this.spawners.clear();

		Spawners.getDataManager().getSpawners((error, result) -> {
			if (error == null)
				result.forEach(this::addSpawner);
		});
	}
}
