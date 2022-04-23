package io.github.stuff_stuffs.tlm.client.render;

import io.github.stuff_stuffs.tlm.client.TLMClient;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorAccess;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorApi;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResource;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Optional;

//TODO only hits blocks in path not ones adjacent;
public final class ConveyedResourceHud {
    public static void render(final MatrixStack matrices, final float tickDelta) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Vec3d start = client.player.getCameraPosVec(tickDelta);
        final Vec3d end = start.add(client.player.getRotationVec(tickDelta).multiply(2.5));
        MathUtil.rayCast(start, end, pos -> {
            final ConveyorAccess access = ConveyorApi.CONVEYOR_ACCESS_BLOCK_API_LOOKUP.find(client.world, pos, null);
            if (access != null) {
                final Iterator<ConveyorTray> trays = access.getTrays();
                double bestDist = Double.POSITIVE_INFINITY;
                ConveyorTray bestTray = null;
                while (trays.hasNext()) {
                    final ConveyorTray tray = trays.next();
                    final Optional<Vec3d> raycast = tray.getBounds(tickDelta).raycast(start, end);
                    if (raycast.isPresent()) {
                        final Vec3d hit = raycast.get();
                        final double distance = hit.squaredDistanceTo(start);
                        if (distance < bestDist) {
                            bestDist = distance;
                            bestTray = tray;
                        }
                    }
                }
                if (bestTray != null) {
                    render(bestTray, matrices, tickDelta);
                    return new BlockHitResult(null, null, null, false);
                }
            }
            return null;
        });
    }

    private static void render(final ConveyorTray tray, final MatrixStack matrices, final float tickDelta) {
        final Optional<ConveyedResource<?, ?>> optional = tray.getResource();
        if (optional.isPresent()) {
            final ConveyedResource<?, ?> resource = optional.get();
            final TLMClient.ClientConveyedResourceInfo<?, ?> info = TLMClient.getInfo(resource.getType());
            info.hudRenderer.render(tray, matrices, tickDelta);
        }
    }

    private ConveyedResourceHud() {
    }
}
