package anticope.rejects.utils.server;

import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MServerInfo {
    public String name;
    public String address;
    public String playerCountLabel;
    public int playerCount;
    public int playercountMax;
    public String label;
    public long ping;
    public int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    public String version = null;
    public List<Text> playerListSummary = Collections.emptyList();
    @Nullable
    private String icon;

    public MServerInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    @Nullable
    public String getIcon() {
        return this.icon;
    }

    public void setIcon(@Nullable String string) {
        this.icon = string;
    }
}
