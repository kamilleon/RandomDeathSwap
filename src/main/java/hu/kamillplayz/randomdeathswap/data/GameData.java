package hu.kamillplayz.randomdeathswap.data;

import hu.kamillplayz.randomdeathswap.RandomDeathSwap;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
@Setter
public class GameData {

	private boolean isRunning = false;
	private boolean isSwapping = false;
	private int time = 0;
	private int maxTime = 0;
	private int swaps = 0;

	private BukkitTask task;
	private BukkitTask randomItemTask;
	private List<UUID> alivePlayers = new ArrayList<>();
	private BossBar bossBar;

	private final ConfigJson configJson = RandomDeathSwap.getInstance().getConfigJson();

	public void start() {
		isRunning = true;

		Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
		spawnLocation.setX(spawnLocation.getX() + (new Random().nextInt(1000) - 500));
		spawnLocation.setZ(spawnLocation.getZ() + (new Random().nextInt(1000) - 500));

		Bukkit.getOnlinePlayers().forEach(player -> {
			alivePlayers.add(player.getUniqueId());
			player.setGameMode(org.bukkit.GameMode.SURVIVAL);

			player.closeInventory();
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setMaxHealth(20);
			player.setHealth(20);
			player.setFoodLevel(20);

			Iterator<Advancement> advancementIterator = Bukkit.advancementIterator();
			while (advancementIterator.hasNext()) {
				Advancement advancement = advancementIterator.next();
				AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
				for (String criteria : advancementProgress.getAwardedCriteria()) {
					advancementProgress.revokeCriteria(criteria);
				}
			}

			Location spreadLocation = spawnLocation.clone();
			spreadLocation.setX(spreadLocation.getX() + (new Random().nextInt(250) - 125));
			spreadLocation.setZ(spreadLocation.getZ() + (new Random().nextInt(250) - 125));
			spreadLocation.setY(spreadLocation.getWorld().getHighestBlockYAt(spreadLocation)+1);

			player.teleport(spreadLocation);
		});

		newSwap();

		if (randomItemTask != null) randomItemTask.cancel();
		if (configJson.isRandomItems()) {
			randomItemTask = startRandomItemTask();
		}
	}

	private void newSwap() {
		maxTime = new Random().nextInt(configJson.getMaxSwapTime());
		maxTime = Math.max(maxTime, configJson.getMinSwapTime());

		time = 0;

		if (task != null) task.cancel();
		task = startCountdown();

		swaps++;

		isSwapping = true;
		Bukkit.getScheduler().runTaskLater(RandomDeathSwap.getInstance(), () -> {
			isSwapping = false;
		}, 3L);
	}

	private BukkitTask startRandomItemTask() {
		return Bukkit.getScheduler().runTaskTimer(RandomDeathSwap.getInstance(), () -> {
			if (!isRunning) {
				randomItemTask.cancel();
				return;
			}

			for (UUID alivePlayer : alivePlayers) {
				Player player = Bukkit.getPlayer(alivePlayer);

				ItemStack randomItem = new ItemStack(Material.values()[new Random().nextInt(Material.values().length)], 1);
				int amount = new Random().nextInt(8) + 1;
				randomItem.setAmount(amount);

				player.getInventory().addItem(randomItem);

				String friendlyName = randomItem.getType().name().toLowerCase().replace("_", " ");
				friendlyName = friendlyName.substring(0, 1).toUpperCase() + friendlyName.substring(1);

				String message = configJson.getMessage("randomItem", amount, friendlyName);
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));

				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.85f, 1);
			}
		}, 20L * configJson.getRandomItemsInterval(), 20L * configJson.getRandomItemsInterval());
	}

	private BukkitTask startCountdown() {

		if (configJson.isDecreaseHealth()) {
			Bukkit.getOnlinePlayers().forEach(player -> {
				if (player.getMaxHealth() > 2) player.setMaxHealth(20 - (swaps * 2));
			});
		}

		return Bukkit.getScheduler().runTaskTimer(RandomDeathSwap.getInstance(), () -> {
			if (!isRunning) {
				return;
			}

			if (alivePlayers.size() <= 1) {
				Bukkit.broadcastMessage(configJson.getMessage("gameEndWinner"));
				Bukkit.broadcastMessage(configJson.getMessage("winner", Bukkit.getPlayer(alivePlayers.get(0)).getName()));

				Bukkit.getOnlinePlayers().forEach(player -> {
					float pitch = (float) (0.85 + Math.random() * 0.3);

					player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, pitch);
				});

				stop();
				return;
			}

			if (time >= maxTime) {
				LinkedList<UUID> alivePlayers1 = new LinkedList<>(this.alivePlayers);
				alivePlayers1.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
				alivePlayers1.sort((o1, o2) -> new Random().nextInt(3) - 1);

				LinkedList<Location> locations = new LinkedList<>();

				for (UUID uuid : alivePlayers1) {
					locations.add(Bukkit.getPlayer(uuid).getLocation());
				}

				for (int i = 0; i < alivePlayers1.size(); i++) {
					Bukkit.getPlayer(alivePlayers1.get(i)).teleport(locations.get((i + 1) % locations.size()));
					Bukkit.getPlayer(alivePlayers1.get(i)).sendMessage(configJson.getMessage("swapped", Bukkit.getPlayer(alivePlayers1.get((i + 1) % locations.size())).getName()));
				}

				newSwap();
				return;
			}

			if (bossBar != null) bossBar.removeAll();
			if (maxTime - 10 <= time) {
				bossBar = Bukkit.createBossBar(configJson.getMessage("nextSwapSoon", (maxTime - time)), BarColor.RED, BarStyle.SOLID);
				bossBar.setProgress((double) (maxTime - time) / 10);

				Bukkit.getOnlinePlayers().forEach(player -> {
					float pitch = (float) (0.85 + Math.random() * 0.3);

					player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, pitch);
				});
			} else {
				String remainingTime;
				if (RandomDeathSwap.getInstance().getConfigJson().isShowRemainingTime()) {
					remainingTime = (maxTime - time) + configJson.getMessage("seconds");
				} else {
					remainingTime = "???";
				}

				bossBar = Bukkit.createBossBar(configJson.getMessage("nextSwap", remainingTime), BarColor.GREEN, BarStyle.SOLID);
				bossBar.setProgress(1);
			}

			for (UUID uuid : alivePlayers) {
				bossBar.addPlayer(Bukkit.getPlayer(uuid));

			}

			time++;
		}, 0, 20);
	}

	public void stop() {
		isRunning = false;
		time = 0;
		maxTime = 0;
		swaps = 0;
		alivePlayers.clear();

		if (task != null) task.cancel();
		task = null;

		if (randomItemTask != null) randomItemTask.cancel();
		randomItemTask = null;

		if (bossBar != null) bossBar.removeAll();
	}

}
