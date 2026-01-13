package anticope.rejects.settings;

import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.GameType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameModeListSetting extends Setting<List<GameType>> {
    public GameModeListSetting(String name, String description, List<GameType> defaultValue, Consumer<List<GameType>> onChanged, Consumer<Setting<List<GameType>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected List<GameType> parseImpl(String str) {
        String[] values = str.split(",");
        List<GameType> modes = new ArrayList<>(values.length);
        for (String s : values) {
            GameType mode = GameType.byName(s);
            if (mode != null) modes.add(mode);
        }
        return modes;
    }

    @Override
    protected boolean isValueValid(List<GameType> value) {
        return true;
    }

    @Override
    protected void resetImpl() {
        value = new ArrayList<>();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (GameType mode : get()) {
            valueTag.add(StringTag.valueOf(mode.getName()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public List<GameType> load(CompoundTag tag) {
        get().clear();

        tag.getList("value").ifPresent(valueTag -> {
            for (Tag tagI : valueTag) {
                tagI.asString().ifPresent(str -> {
                    GameType mode = GameType.byName(str);
                    if (mode != null)
                        get().add(mode);
                });
            }
        });

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<GameType>, GameModeListSetting> {
        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(List<GameType> map) {
            this.defaultValue = map;
            return this;
        }

        @Override
        public GameModeListSetting build() {
            return new GameModeListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
