package anticope.rejects.settings;

import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GameModeListSetting extends Setting<List<GameMode>> {
    public GameModeListSetting(String name, String description, List<GameMode> defaultValue, Consumer<List<GameMode>> onChanged, Consumer<Setting<List<GameMode>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected List<GameMode> parseImpl(String str) {
        String[] values = str.split(",");
        List<GameMode> modes = new ArrayList<>(values.length);
        for (String s : values) {
            GameMode mode = GameMode.byId(s);
            if (mode != null) modes.add(mode);
        }
        return modes;
    }

    @Override
    protected boolean isValueValid(List<GameMode> value) {
        return true;
    }

    @Override
    protected void resetImpl() {
        value = new ArrayList<>();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (GameMode mode : get()) {
            valueTag.add(NbtString.of(mode.getId()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public List<GameMode> load(NbtCompound tag) {
        get().clear();

        Optional<NbtElement> optionalValueTag = Optional.ofNullable(tag.get("value"));
        if (optionalValueTag.isPresent() && optionalValueTag.get() instanceof NbtList valueTag) {
            for (NbtElement tagI : valueTag) {
                GameMode mode = GameMode.byId(String.valueOf(tagI.asString()));
                if (mode != null)
                    get().add(mode);
            }
        }

        return get();
    }


    public static class Builder extends SettingBuilder<Builder, List<GameMode>, GameModeListSetting> {
        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(List<GameMode> map) {
            this.defaultValue = map;
            return this;
        }

        @Override
        public GameModeListSetting build() {
            return new GameModeListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
