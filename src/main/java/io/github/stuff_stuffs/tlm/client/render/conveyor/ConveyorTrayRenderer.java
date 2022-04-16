package io.github.stuff_stuffs.tlm.client.render.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorTray;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public final class ConveyorTrayRenderer {
    public static final EntityModelLayer CONVEYOR_TRAY_LAYER = new EntityModelLayer(TLM.createId("conveyor/conveyor_tray"), "TLM:ConveyorTray");

    private ConveyorTrayRenderer() {
    }

    public static void render(final ConveyorTray tray, final ModelPart trayModel, final MatrixStack matrices, final int light, final VertexConsumerProvider vertexConsumers) {
        final VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(TLM.createId("textures/conveyor/conveyor_tray.png")));
        trayModel.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV);
    }

    public static TexturedModelData createTrayModel() {
        final ModelData data = new ModelData();
        final ModelPartData partData = data.getRoot();
        partData.addChild("base", ModelPartBuilder.create().uv(0, 0).cuboid(-6, 0, -6, 12, 1, 12, Dilation.NONE, 1.0F, 1.0F), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 64);
    }
}
