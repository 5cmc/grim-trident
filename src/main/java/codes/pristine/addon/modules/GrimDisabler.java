package codes.pristine.addon.modules;

import codes.pristine.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrimDisabler extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> tridentDelay = sgGeneral.add(
        new IntSetting.Builder()
            .name("Trident delay")
            .description("delay (in ticks) between trident uses")
            .sliderRange(0, 20)
            .range(0, 20)
            .defaultValue(0)
            .build()
    );

    private final Setting<Boolean> grimDisable = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Grim")
            .description("bypass grim movement checks")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnEat = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Pause on eat")
            .description("Pauses when eating (buggy)")
            .defaultValue(false)
            .build()
    );

    private int tick = 0;

    public GrimDisabler() {
        super(Addon.CATEGORY, "Grim Disabler", "Full grim movement disabler");
    }

    @Override
    public void onActivate() {
        tick = tridentDelay.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (tick <= 0) {
            if (tridentDelay.get() != 0) tick = tridentDelay.get();

            int tridentSlot = InvUtils.findInHotbar(Items.TRIDENT).slot();
            int oldSlot = mc.player.getInventory().selectedSlot;

            if (tridentSlot == -1 || (pauseOnEat.get() && mc.player.isUsingItem())) return;

            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(tridentSlot));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));

            if (!grimDisable.get()) {
                float f = mc.player.getYaw();
                float g = mc.player.getPitch();
                float h = -MathHelper.sin(f * (float) (Math.PI / 180.0)) * MathHelper.cos(g * (float) (Math.PI / 180.0));
                float k = -MathHelper.sin(g * (float) (Math.PI / 180.0));
                float l = MathHelper.cos(f * (float) (Math.PI / 180.0)) * MathHelper.cos(g * (float) (Math.PI / 180.0));
                float m = MathHelper.sqrt(h * h + k * k + l * l);
                float n = 3.0F;
                h *= n / m;
                k *= n / m;
                l *= n / m;
                mc.player.addVelocity(h, k, l);
                if (mc.player.isOnGround()) {
                    mc.player.move(MovementType.SELF, new Vec3d(0.0, 1.1999999F, 0.0));
                }
            }

            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));

        } else {
            tick--;
        }
    }
}
