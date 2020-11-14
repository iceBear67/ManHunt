package io.ib67.manhunt;

import io.ib67.manhunt.setting.MainConfig;
import io.ib67.manhunt.util.SimpleConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class ManHunt extends JavaPlugin {
    public static boolean debug = false;
    private SimpleConfig<MainConfig> mainConfig = new SimpleConfig<>(getDataFolder(), MainConfig.class);

    public static ManHunt get() {
        return ManHunt.getPlugin(ManHunt.class);
    }

    @Override
    public void onEnable() {
        mainConfig.saveDefault();
        mainConfig.reloadConfig();
        debug = mainConfig.get().verbose;
        loadAdditions();
    }

    private void loadListeners() {

    }

    private void loadAdditions() {
        //todo
    }
}
