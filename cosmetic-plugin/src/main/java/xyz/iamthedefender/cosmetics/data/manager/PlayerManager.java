package xyz.iamthedefender.cosmetics.data.manager;

import lombok.Getter;
import xyz.iamthedefender.cosmetics.data.PlayerData;
import xyz.iamthedefender.cosmetics.data.PlayerOwnedData;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class PlayerManager {

    private final HashMap<UUID, PlayerData> playerDataHashMap;
    private final HashMap<UUID, PlayerOwnedData> playerOwnedDataHashMap;

    public PlayerManager() {
        playerDataHashMap = new HashMap<>();
        playerOwnedDataHashMap = new HashMap<>();

    }


    public void addPlayerData(PlayerData playerData) {
        playerDataHashMap.put(playerData.getUuid(), playerData);
    }

    public void addPlayerOwnedData(PlayerOwnedData playerOwnedData) {
        playerOwnedDataHashMap.put(playerOwnedData.getUuid(), playerOwnedData);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataHashMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public PlayerOwnedData getPlayerOwnedData(UUID uuid) {
        // The PlayerOwnedData constructor already loads from the DB once, on first
        // creation. Reloading on every call meant a blocking DB round-trip on the main
        // thread for every placeholder render, GUI open and 5s scheduler tick. Return
        // the cached instance instead; call load()/save() explicitly when a refresh is
        // actually needed.
        return playerOwnedDataHashMap.computeIfAbsent(uuid, PlayerOwnedData::new);
    }
}
