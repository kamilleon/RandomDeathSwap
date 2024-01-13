package hu.kamillplayz.randomdeathswap.data;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import hu.kamillplayz.randomdeathswap.RandomDeathSwap;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

		Bukkit.getOnlinePlayers().forEach(player -> {
			alivePlayers.add(player.getUniqueId());
			player.setGameMode(org.bukkit.GameMode.SURVIVAL);

			player.closeInventory();
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setMaxHealth(20);
			player.setHealth(20);
			player.setFoodLevel(20);
		});

		newSwap();

		if (randomItemTask != null) randomItemTask.cancel();
		randomItemTask = startRandomItemTask();
	}

	private void newSwap() {
		maxTime = new Random().nextInt(180);
		maxTime = Math.max(maxTime, 60);

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

				String message = IridiumColorAPI.process("<SOLID:77FF33>Kaptál " + amount + "x " + friendlyName + " tárgyat.");
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));

				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.85f, 1);
			}
		}, 20 * 10L, 20 * 10L);
	}

	private BukkitTask startCountdown() {

		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.getMaxHealth() > 2) player.setMaxHealth(20 - (swaps * 2));
		});

		return Bukkit.getScheduler().runTaskTimer(RandomDeathSwap.getInstance(), () -> {
			if (!isRunning) {
				return;
			}

			if (alivePlayers.size() <= 1) {
				Bukkit.broadcastMessage(IridiumColorAPI.process(configJson.getPrefix() + " <SOLID:F0F8FF>A játék véget ért!"));
				Bukkit.broadcastMessage(IridiumColorAPI.process(" &8➥ &fGyőztes: <SOLID:77FF33>" + Bukkit.getPlayer(alivePlayers.get(0)).getName()));

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
					Bukkit.getPlayer(alivePlayers1.get(i)).sendMessage(IridiumColorAPI.process(configJson.getPrefix() + " <SOLID:F0F8FF>Helyet cseréltél §6" + Bukkit.getPlayer(alivePlayers1.get((i + 1) % locations.size())).getName() + " <SOLID:F0F8FF>játékossal!"));
				}

				newSwap();
				return;
			}

			if (bossBar != null) bossBar.removeAll();
			if (maxTime - 10 <= time) {
				bossBar = Bukkit.createBossBar(IridiumColorAPI.process("<SOLID:E32636>A következő csere " + (maxTime - time) + " másodperc múlva lesz!"), BarColor.RED, BarStyle.SOLID);
				bossBar.setProgress((double) (maxTime - time) / 10);

				Bukkit.getOnlinePlayers().forEach(player -> {
					float pitch = (float) (0.85 + Math.random() * 0.3);

					player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.5f, pitch);
				});
			} else {
				String remainingTime = (maxTime - time) + " mp";
				if (!RandomDeathSwap.getInstance().getConfigJson().isShowRemainingTime()) {
					remainingTime = "???";
				}

				bossBar = Bukkit.createBossBar(IridiumColorAPI.process("<SOLID:77FF33>[Stabil] <SOLID:F0F8FF>Következő csere " + remainingTime), BarColor.GREEN, BarStyle.SOLID);
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
