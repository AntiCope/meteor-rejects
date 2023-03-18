package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class Rendering extends Module {

    public enum Shader {
        None,
        Notch,
        FXAA,
        Art,
        Bumpy,
        Blobs,
        Blobs2,
        Pencil,
        Vibrant,
        Deconverge,
        Flip,
        Invert,
        NTSC,
        Outline,
        Phosphor,
        Scanline,
        Sobel,
        Bits,
        Desaturate,
        Green,
        Blur,
        Wobble,
        Antialias,
        Creeper,
        Spider
    }

    private final SettingGroup sgInvisible = settings.createGroup("Invisible");
    private final SettingGroup sgFun = settings.createGroup("Fun");

    private final Setting<Boolean> structureVoid = sgInvisible.add(new BoolSetting.Builder()
			.name("structure-void")
			.description("Render structure void blocks.")
			.defaultValue(true)
            .onChanged(onChanged -> {
                if(this.isActive()) {
                    mc.worldRenderer.reload();
                }
            })
			.build()
	);

    private final Setting<Shader> shaderEnum = sgFun.add(new EnumSetting.Builder<Shader>()
        .name("shader")
        .description("Select which shader to use")
        .defaultValue(Shader.None)
        .onChanged(this::onChanged)
        .build()
    );

    private final Setting<Boolean> dinnerbone = sgFun.add(new BoolSetting.Builder()
			.name("dinnerbone")
			.description("Apply dinnerbone effects to all entities")
			.defaultValue(false)
			.build()
	);

    private final Setting<Boolean> deadmau5Ears = sgFun.add(new BoolSetting.Builder()
			.name("deadmau5-ears")
			.description("Add deadmau5 ears to all players")
			.defaultValue(false)
			.build()
	);

    private final Setting<Boolean> christmas = sgFun.add(new BoolSetting.Builder()
			.name("chrismas")
			.description("Chistmas chest anytime")
			.defaultValue(false)
			.build()
	);
    
    private PostEffectProcessor shader = null;
    
    public Rendering() {
        super(MeteorRejectsAddon.CATEGORY, "rendering", "Various Render Tweaks");
    }

    @Override
    public void onActivate() {
        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        mc.worldRenderer.reload();
    }

    public void onChanged(Shader s) {
        String name;
        if (s == Shader.Vibrant) name = "color_convolve";
        else if (s == Shader.Scanline) name = "scan_pincushion";
        else name = s.toString().toLowerCase();
        Identifier shaderID = new Identifier(String.format("shaders/post/%s.json", name));
        try {
            PostEffectProcessor shader = new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shaderID);
            this.shader = shader;
        } catch (IOException e) {
            this.shader = null;
        }
    }

    public boolean renderStructureVoid() {
        return this.isActive() && structureVoid.get();
    }

    public PostEffectProcessor getShaderEffect() {
        if (!this.isActive()) return null;
        return shader;
    }

    public boolean dinnerboneEnabled() {
        if (!this.isActive()) return false;
        return dinnerbone.get();
    }

    public boolean deadmau5EarsEnabled() {
        if (!this.isActive()) return false;
        return deadmau5Ears.get();
    }

    public boolean chistmas() {
        if (!this.isActive()) return false;
        return christmas.get();
    }
}
