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

		player.spigot().respawn();
		player.sendTitle(IridiumColorAPI.process("<SOLID:FF2A00>§lMeghaltál"), "§fA játékban maradt játékosok: §c" + gameData.getAlivePlayers().size());

		Bukkit.getScheduler().runTaskLater(RandomDeathSwap.getInstance(), () -> {
			player.setGameMode(GameMode.SPECTATOR);

			Player alivePlayer = Bukkit.getPlayer(gameData.getAlivePlayers().get(0));
			player.teleport(alivePlayer.getLocation());
		}, 5L);
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!gameData.isRunning()) {
			return;
		}

		if (!(e.getEntity() instanceof Player player)) {
			return;
		}

		if (gameData.isSwapping()) {
			e.setCancelled(true);
			return;
		}

		if (!gameData.getAlivePlayers().contains(player.getUniqueId())) {
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
			damager.sendMessage(IridiumColorAPI.process("&cNem üthetsz meg a játékosokat!"));
		}
	}

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent e) {
		if (gameData.isRunning()) {
			e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, IridiumColorAPI.process("&cA játék már elkezdődött!"));
		}
	}
}
