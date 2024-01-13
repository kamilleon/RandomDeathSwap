package hu.kamillplayz.randomdeathswap.commands;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import hu.kamillplayz.randomdeathswap.RandomDeathSwap;
import hu.kamillplayz.randomdeathswap.data.GameData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathSwapCommand {

	private GameData gameData = RandomDeathSwap.getInstance().getGameData();

	@Command(name = "start", desc = "Elindítja a játékot.")
	@Require("randomdeathswap.start")
	public void start(@Sender CommandSender sender) {
		gameData.start();

		Bukkit.broadcastMessage(IridiumColorAPI.process(RandomDeathSwap.getInstance().getConfigJson().getPrefix() + " <SOLID:77FF33>A játék el lett indítva!"));
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		}
	}

	@Command(name = "stop", desc = "Leállítja a játékot.")
	@Require("randomdeathswap.stop")
	public void stop(@Sender CommandSender sender) {
		gameData.stop();

		Bukkit.broadcastMessage(IridiumColorAPI.process(RandomDeathSwap.getInstance().getConfigJson().getPrefix() + " <SOLID:E52B50>A játék le lett állítva!"));
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 0.5f);
		}
	}

	@Command(name = "reload", desc = "Újratölti a konfigurációs fájlokat.")
	@Require("randomdeathswap.reload")
	public void reload(@Sender CommandSender sender) {
		RandomDeathSwap.getInstance().unloadConfigs();
		RandomDeathSwap.getInstance().loadConfigs();

		sender.sendMessage(IridiumColorAPI.process("<SOLID:77FF33>A konfigurációs fájlok újratöltve!"));
	}
}
