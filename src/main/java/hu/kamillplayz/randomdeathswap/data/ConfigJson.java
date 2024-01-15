package hu.kamillplayz.randomdeathswap.data;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ConfigJson {

	private int minTeleportDistance = 500;
	private int maxTeleportDistance = 1000;
	private int minSpreadDistance = 125;
	private int maxSpreadDistance = 250;
	private int minSwapTime = 60;
	private int maxSwapTime = 180;
	private boolean showRemainingTime = false;
	private boolean randomItems = true;
	private int randomItemsInterval = 10;
	private boolean decreaseHealth = true;
	private boolean pvp = false;
	private int maxItemAmount = 16;

	private Map<String, String> messages = getDefaultMessages();

	public String getMessage(String message, Object... args) {
		return IridiumColorAPI.process(messages.get(message)
			.replace("%prefix%", messages.get("prefix"))
			.formatted(args));
	}

	public String getMessage(String message) {
		return IridiumColorAPI.process(messages.get(message)
			.replace("%prefix%", messages.get("prefix")));
	}

	private Map <String, String> getDefaultMessages() {
		Map<String, String> messages = new LinkedHashMap<>();

		messages.put("prefix", "<GRADIENT:FFE14D>&lDEATHSWAP</GRADIENT:FF6619> &8» &f");
		messages.put("gameStart", "%prefix% <SOLID:77FF33>A játék el lett indítva!");
		messages.put("gameRunning", "&cA játék már elkezdődött!");
		messages.put("gameEnd", "%prefix% <SOLID:E52B50>A játék le lett állítva!");
		messages.put("gameEndWinner", "%prefix% <SOLID:F0F8FF>A játék véget ért!");
		messages.put("winner", " &8➥ &fGyőztes: <SOLID:77FF33>%s");
		messages.put("randomItem", "<SOLID:77FF33>Kaptál %dx %s tárgyat.");
		messages.put("swapped", "%prefix% <SOLID:F0F8FF>Helyet cseréltél §6%s <SOLID:F0F8FF>játékossal!");
		messages.put("nextSwapSoon", "<SOLID:E32636>A következő csere %d másodperc múlva!");
		messages.put("nextSwap", "<SOLID:77FF33>[Biztonságos] <SOLID:F0F8FF>Következő csere: %s");
		messages.put("seconds", " mp");
		messages.put("reload", "%prefix% <SOLID:77FF33>A konfigurációs fájlok újratöltve!");
		messages.put("playersLeftTitle", "<SOLID:FF2A00>§lMeghaltál");
		messages.put("playersLeftSubtitle", "&fA játékban maradt játékosok: &c%d");
		messages.put("noPvP", "&cNem üthetsz meg a játékosokat!");
		messages.put("notEnoughPlayers", "%prefix% <SOLID:E32636>Nincs elég játékos a játék elindításához! &7(Minimum 2)");
		messages.put("alreadyRunning", "%prefix% <SOLID:E32636>A játék már el lett indítva!");
		messages.put("notRunning", "&cA játék már le lett állítva!");

		return messages;
	}

}
