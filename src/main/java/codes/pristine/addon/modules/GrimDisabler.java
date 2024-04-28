package codes.pristine.addon.modules;

import codes.pristine.addon.Addon;
import codes.pristine.addon.mixin.ClientPlayerInteractionManagerInvoker;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.MovementType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrimDisabler extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> grimDisable = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Grim")
            .description("Bypass grim movement checks")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> overdrive = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Overdrive")
            .description("Sends extra packets to guarantee enough velocity")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnEat = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Pause on eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnInventory = sgGeneral.add(
        new BoolSetting.Builder()
            .name("Pause on inventory")
            .description("Pauses when moving items in an inventory")
            .defaultValue(false)
            .build()
    );

    private int slot = 0;

    public GrimDisabler() {
        super(Addon.CATEGORY, "Grim Disabler", "Full grim movement disabler");
    }

    @Override
    public void onActivate() {
        slot = mc.player.getInventory().selectedSlot;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        int tridentSlot = InvUtils.findInHotbar(Items.TRIDENT).slot();

        if (tridentSlot == -1) return;
        if (pauseOnEat.get() && mc.player.isUsingItem()) return;
        if (pauseOnInventory.get() && !mc.player.currentScreenHandler.getCursorStack().isEmpty()) return;

        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(tridentSlot));
        for (int i = 0; i < (overdrive.get() ? 2 : 1); i++) {
            ((ClientPlayerInteractionManagerInvoker) mc.interactionManager).sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
        }

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

        if (mc.player.getInventory().selectedSlot != slot) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof UpdateSelectedSlotS2CPacket packet) {
            slot = packet.getSlot();
        } else if (event.packet instanceof CloseScreenS2CPacket && mc.currentScreen instanceof InventoryScreen && pauseOnInventory.get()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket packet) {
            slot = packet.getSelectedSlot();
        }
    }
}
