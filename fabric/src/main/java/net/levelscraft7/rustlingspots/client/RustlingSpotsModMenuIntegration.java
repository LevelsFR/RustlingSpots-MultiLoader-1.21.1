package net.levelscraft7.rustlingspots.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.levelscraft7.rustlingspots.configscreen.RustlingConfigClientState;

public final class RustlingSpotsModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return RustlingConfigClientState::createScreen;
    }
}
