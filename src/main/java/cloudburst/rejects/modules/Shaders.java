package cloudburst.rejects.modules;

import java.io.IOException;

import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.util.Identifier;

import cloudburst.rejects.MeteorRejectsAddon;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.systems.modules.Module;

public class Shaders extends Module {

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

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private ShaderEffect shader = null;

    public Shaders() {
        super(MeteorRejectsAddon.CATEGORY, "shaders", "1.7 Super secret settings");
    }

    private final Setting<Shader> shaderEnum = sgGeneral.add(new EnumSetting.Builder<Shader>()
        .name("shader")
        .description("Select which shader to use")
        .defaultValue(Shader.None)
        .onChanged(this::onChanged)
        .build()
    );


    public void onChanged(Shader s) {
        String name;
        if (s == Shader.Vibrant) name = "color_convolve";
        else if (s == Shader.Scanline) name = "scan_pincushion";
        else name = s.toString().toLowerCase();
        Identifier shaderID = new Identifier(String.format("shaders/post/%s.json", name));
        try {
            ShaderEffect shader = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shaderID);
            this.shader = shader;
        } catch (IOException e) {
            this.shader = null;
        }
    }

    public ShaderEffect getShaderEffect() {
        if (!this.isActive()) return null;
        return shader;
    }
}
