package cloudburst.rejects.mixin;

import minegame159.meteorclient.systems.config.Config;
import minegame159.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 500)
public class TitleScreenMixin extends Screen {
    
    // Meteor should've used @Unique here
    // And because of that I have to rename the variables to avoid collision.
    // With @Unique, the variable names can stay the same without collision.
    
    private final int rejectsTextColor = Color.fromRGBA(255, 255, 255, 255);
    private final int rejectsCreditColor = Color.fromRGBA(175, 175, 175, 255);
    
    private String rejectsText1;
    private String rejectsText2;
    private String rejectsText3;
    private String rejectsText4;
    
    private int rejectsLength1;
    private int rejectsLength2;
    private int rejectsLength3;
    private int rejectsLength4;
    
    private int rejectsFullLength;
    private int rejectsPrevWidth;
    
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        
        rejectsText1 = "Meteor Rejects by ";
        rejectsText2 = "Cloudburst";
        rejectsText3 = " & ";
        rejectsText4 = "StormyBytes";
        
        
        rejectsLength1 = textRenderer.getWidth(rejectsText1);
        rejectsLength2 = textRenderer.getWidth(rejectsText2);
        rejectsLength3 = textRenderer.getWidth(rejectsText3);
        rejectsLength4 = textRenderer.getWidth(rejectsText4);

        rejectsFullLength = rejectsLength1 + rejectsLength2 + rejectsLength3 + rejectsLength4;
        rejectsPrevWidth = 0;
    }
    
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (!Config.get().titleScreenCredits) return;
        rejectsPrevWidth = 0;
        textRenderer.drawWithShadow(matrices, rejectsText1, width - rejectsFullLength - 3, 17, rejectsTextColor);
        rejectsPrevWidth += rejectsLength1;
        textRenderer.drawWithShadow(matrices, rejectsText2, width - rejectsFullLength + rejectsPrevWidth - 3, 17, rejectsCreditColor);
        rejectsPrevWidth += rejectsLength2;
        textRenderer.drawWithShadow(matrices, rejectsText3, width - rejectsFullLength + rejectsPrevWidth - 3, 17, rejectsTextColor);
        rejectsPrevWidth += rejectsLength3;
        textRenderer.drawWithShadow(matrices, rejectsText4, width - rejectsFullLength + rejectsPrevWidth - 3, 17, rejectsCreditColor);
    }
}
