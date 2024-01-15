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
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import cubitect.Cubiomes;
import cubitect.Cubiomes.Pos;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class LocateCommand extends Command {

	private final static DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(o -> {
		if (o instanceof Cubiomes.StructureType) {
			return Text.literal(String.format(
					"%s not found.",
					Utils.nameToTitle(o.toString().replaceAll("_", "-"))));
		}
		return Text.literal("Not found.");
	});

	public LocateCommand() {
		super("locate", "Locates structures.", "loc");
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(literal("feature")
				.then(argument("feature", EnumArgumentType.enumArgument(Cubiomes.StructureType.Village)).executes(ctx -> {
					Cubiomes.StructureType feature = EnumArgumentType.getEnum(ctx, "feature", Cubiomes.StructureType.Village);
					BlockPos playerPos = mc.player.getBlockPos();
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
								Cubiomes.MCVersion.MC_1_20, 8);
					} else {
						BlockPos bpos = WorldGenUtils.locateFeature(feature, playerPos);
						pos = new Pos();
						pos.x = bpos.getX();
						pos.z = bpos.getZ();

					}
					if (pos != null) {
						MutableText text = Text.literal(String.format(
								"%s located at ",
								Utils.nameToTitle(feature.toString().replaceAll("_", "-"))));
						Vec3d coords = new Vec3d(pos.x, 0, pos.z);
						text.append(ChatUtils.formatCoords(coords));
						text.append(".");
						info(text);
						return SINGLE_SUCCESS;
					}
					throw NOT_FOUND.create(feature);
				})));
	}
}
