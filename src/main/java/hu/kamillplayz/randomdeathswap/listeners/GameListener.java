package hu.kamillplayz.randomdeathswap.listeners;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import hu.kamillplayz.randomdeathswap.RandomDeathSwap;
import hu.kamillplayz.randomdeathswap.data.GameData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class GameListener implements Listener {

	private GameData gameData = RandomDeathSwap.getInstance().getGameData();

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if (!gameData.isRunning()) {
			return;
		}

		Player player = e.getEntity();

		if (!gameData.getAlivePlayers().contains(player.getUniqueId())) {
			return;
		}

		gameData.getAlivePlayers().remove(e.getEntity().getUniqueId());

		Bukkit.getScheduler().runTaskLater(RandomDeathSwap.getInstance(), () -> {
			player.spigot().respawn();

			player.setGameMode(GameMode.SPECTATOR);
			player.sendTitle(RandomDeathSwap.getInstance().getConfigJson().getMessage("playersLeftTitle"), RandomDeathSwap.getInstance().getConfigJson().getMessage("playersLeftSubtitle", gameData.getAlivePlayers().size()));

			Player alivePlayer = Bukkit.getPlayer(gameData.getAlivePlayers().get(0));
			player.teleport(alivePlayer.getLocation());
		}, 5L);
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) return;
		if (!RandomDeathSwap.getInstance().getConfigJson().isPvp()) return;

		if (!gameData.isRunning()) return;

		if (!(e.getEntity() instanceof Player player)) return;
		if (!gameData.getAlivePlayers().contains(player.getUniqueId())) return;

		if (gameData.isSwapping()) {
			e.setCancelled(true);
			return;
		}

		if (e.getDamager() instanceof Player damager) {
			if (!gameData.getAlivePlayers().contains(damager.getUniqueId())) {
				return;
			}

			if (damager.getUniqueId().equals(player.getUniqueId())) {
				return;
			}

			e.setCancelled(true);
			damager.sendMessage(RandomDeathSwap.getInstance().getConfigJson().getMessage("noPvP"));
		}
	}

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent e) {
		if (gameData.isRunning()) {
			e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, RandomDeathSwap.getInstance().getConfigJson().getMessage("gameRunning"));
		}
	}
}
