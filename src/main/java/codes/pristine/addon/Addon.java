package codes.pristine.addon;

import codes.pristine.addon.modules.GrimDisabler;
import codes.pristine.addon.modules.Support;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("5C", Items.TNT.getDefaultStack());

    @Override
    public void onInitialize() {
        LOG.info("Initializing 5C Grim Trident");

        Modules.get().add(new GrimDisabler());
        Modules.get().add(new Support());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "codes.pristine.addon";
    }
}
