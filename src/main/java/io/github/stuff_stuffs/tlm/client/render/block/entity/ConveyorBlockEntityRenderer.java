package io.github.stuff_stuffs.tlm.client.render.block.entity;

import io.github.stuff_stuffs.tlm.client.render.conveyor.ConveyorTrayRenderer;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.block.entity.ConveyorBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;

public class ConveyorBlockEntityRenderer implements BlockEntityRenderer<ConveyorBlockEntity> {
    private final ModelPart trayModel;

    public ConveyorBlockEntityRenderer(final BlockEntityRendererFactory.Context context) {
        trayModel = context.getLayerModelPart(ConveyorTrayRenderer.CONVEYOR_TRAY_LAYER);
    }

    @Override
    public void render(final ConveyorBlockEntity entity, final float tickDelta, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final int light, final int overlay) {
        final Iterator<ConveyorTray> iterator = entity.getConveyorAccess().getTrays();
        matrices.push();
        final BlockPos pos = entity.getPos();
        matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        while (iterator.hasNext()) {
            final ConveyorTray tray = iterator.next();
            matrices.push();
            final Vec3d trans = tray.interpolatePosition(tickDelta);
            matrices.translate(trans.x, trans.y, trans.z);
            ConveyorTrayRenderer.render(tray, trayModel, matrices, light, vertexConsumers);
            matrices.pop();
        }
        matrices.pop();
    }
}
