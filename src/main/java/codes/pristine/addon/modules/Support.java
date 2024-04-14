package codes.pristine.addon.modules;

import codes.pristine.addon.Addon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.Util;

public class Support extends Module {
    public Support() {
        super(Addon.CATEGORY, "5C Support", "Join our Discord.");
    }

    @Override
    public void onActivate() {
        Util.getOperatingSystem().open("https://discord.gg/thefifthcolumn");
    }
}
