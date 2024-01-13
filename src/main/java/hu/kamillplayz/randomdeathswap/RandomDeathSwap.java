package hu.kamillplayz.randomdeathswap;

import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import hu.kamillplayz.randomdeathswap.commands.DeathSwapCommand;
import hu.kamillplayz.randomdeathswap.data.ConfigJson;
import hu.kamillplayz.randomdeathswap.data.GameData;
import hu.kamillplayz.randomdeathswap.listeners.GameListener;
import hu.kamillplayz.randomdeathswap.utils.JsonLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomDeathSwap extends JavaPlugin {

	@Getter private static RandomDeathSwap instance;
	@Getter private GameData gameData;
	@Getter private ConfigJson configJson;

	@Override
	public void onEnable() {
		instance = this;

		loadConfigs();
		gameData = new GameData();
		registerCommands();
		registerListeners();
	}

	@Override
	public void onDisable() {
		Drink.get(this).unregisterCommands();

		gameData = null;
		unloadConfigs();
		instance = null;
	}

	private void registerCommands() {
		CommandService cs = Drink.get(this);

		cs.register(new DeathSwapCommand(), "randomdeathswap", "rds", "ds", "deathswap").setDefaultCommandIsHelp(true).setPermission("randomdeathswap.use");

		cs.registerCommands();
	}

	private void registerListeners() {
		PluginManager pm = Bukkit.getPluginManager();

		pm.registerEvents(new GameListener(), this);
	}

	public void loadConfigs() {
		configJson = JsonLoader.loadOrDefault(getDataFolder(), "config.json", ConfigJson.class);
	}

	public void unloadConfigs() {
		configJson = null;
	}

}
