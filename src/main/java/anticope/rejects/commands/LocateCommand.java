package anticope.rejects.commands;

import anticope.rejects.arguments.EnumArgumentType;
import anticope.rejects.utils.WorldGenUtils;
import anticope.rejects.utils.seeds.Seeds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.seedfinding.mccore.version.MCVersion;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import cubitect.Cubiomes;
import cubitect.Cubiomes.Pos;

public class LocateCommand extends Command {

	private final static DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(o -> {
		if (o instanceof Cubiomes.StructureType) {
			return Component.literal(String.format(
					"%s not found.",
					Utils.nameToTitle(o.toString().replaceAll("_", "-"))));
		}
		return Component.literal("Not found.");
	});

	public LocateCommand() {
		super("locate", "Locates structures.", "loc");
	}

	@Override
	public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
		builder.then(literal("feature")
				.then(argument("feature", EnumArgumentType.enumArgument(Cubiomes.StructureType.Village)).executes(ctx -> {
					Cubiomes.StructureType feature = EnumArgumentType.getEnum(ctx, "feature", Cubiomes.StructureType.Village);
					BlockPos playerPos = mc.player.blockPosition();
					long seed = Seeds.get().getSeed().seed;
					MCVersion version = Seeds.get().getSeed().version;
					Cubiomes.MCVersion cubiomesVersion = null;
					if (version.isNewerOrEqualTo(MCVersion.v1_20)) {
						cubiomesVersion = Cubiomes.MCVersion.MC_1_20;
					} else if (version.isNewerOrEqualTo(MCVersion.v1_19)) {
						switch (version) {
							case v1_19:
							case v1_19_1:
								cubiomesVersion = Cubiomes.MCVersion.MC_1_19;
								break;
							case v1_19_2:
							case v1_19_3:
							case v1_19_4:
								cubiomesVersion = Cubiomes.MCVersion.MC_1_19_2;
								break;
							default:
								throw new IllegalStateException("Unexpected value: " + version);
						}
					} else if (version.isNewerOrEqualTo(MCVersion.v1_18)) {
						cubiomesVersion = Cubiomes.MCVersion.MC_1_18;
					}
					Pos pos = null;
					if (cubiomesVersion != null) {
						pos = Cubiomes.GetNearestStructure(feature, playerPos.getX(), playerPos.getZ(), seed,
								cubiomesVersion);
					} else {
						BlockPos bpos = WorldGenUtils.locateFeature(feature, playerPos);
						pos = new Pos();
						pos.x = bpos.getX();
						pos.z = bpos.getZ();

					}
					if (pos != null) {
						// Calculate distance
						int distance = (int) Math.hypot(pos.x - playerPos.getX(), pos.z - playerPos.getZ());
						MutableComponent text = Component.literal(String.format(
								"%s located at ",
								Utils.nameToTitle(feature.toString().replaceAll("_", "-"))));
						Vec3 coords = new Vec3(pos.x, 0, pos.z);
						text.append(ChatUtils.formatCoords(coords));
						text.append(".");
						if (distance > 0) {
							text.append(String.format(" (%d blocks away)", distance));
						}
						info(text);
						return SINGLE_SUCCESS;
					}
					throw NOT_FOUND.create(feature);
				})));
	}
}
