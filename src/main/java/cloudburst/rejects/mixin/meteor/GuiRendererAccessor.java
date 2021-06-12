package cloudburst.rejects.mixin.meteor;

import minegame159.meteorclient.gui.renderer.GuiRenderer;
import minegame159.meteorclient.renderer.Renderer2D;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {
    @Accessor("r")
    Renderer2D getRenderer2D();
}
