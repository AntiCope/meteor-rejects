package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
    Ported from: https://github.com/BleachDrinker420/BleachHack/blob/master/BleachHack-Fabric-1.16/src/main/java/bleach/hack/module/mods/NewChunks.java
*/
public class NewChunks extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
	private final SettingGroup sgRender = settings.createGroup("Render");

	// general

	private final Setting<Boolean> remove = sgGeneral.add(new BoolSetting.Builder()
        .name("remove")
        .description("Removes the cached chunks when disabling the module.")
        .defaultValue(true)
        .build()
    );

	// render
	public final Setting<Integer> renderHeight = sgRender.add(new IntSetting.Builder()
			.name("render-height")
			.description("The height at which new chunks will be rendered")
			.defaultValue(0)
			.min(-64)
			.sliderRange(-64,319)
			.build()
	);

	private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
			.name("shape-mode")
			.description("How the shapes are rendered.")
			.defaultValue(ShapeMode.Both)
			.build()
	);

	private final Setting<SettingColor> newChunksSideColor = sgRender.add(new ColorSetting.Builder()
			.name("new-chunks-side-color")
			.description("Color of the chunks that are (most likely) completely new.")
			.defaultValue(new SettingColor(255, 0, 0, 75))
			.visible(() -> shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both)
			.build()
	);

	private final Setting<SettingColor> oldChunksSideColor = sgRender.add(new ColorSetting.Builder()
			.name("old-chunks-side-color")
			.description("Color of the chunks that have (most likely) been loaded before.")
			.defaultValue(new SettingColor(0, 255, 0, 75))
			.visible(() -> shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both)
			.build()
	);

	private final Setting<SettingColor> newChunksLineColor = sgRender.add(new ColorSetting.Builder()
			.name("new-chunks-line-color")
			.description("Color of the chunks that are (most likely) completely new.")
			.defaultValue(new SettingColor(255, 0, 0, 255))
			.visible(() -> shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both)
			.build()
	);

	private final Setting<SettingColor> oldChunksLineColor = sgRender.add(new ColorSetting.Builder()
			.name("old-chunks-line-color")
			.description("Color of the chunks that have (most likely) been loaded before.")
			.defaultValue(new SettingColor(0, 255, 0, 255))
			.visible(() -> shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both)
			.build()
	);

    private final Set<ChunkPos> newChunks = Collections.synchronizedSet(new HashSet<>());
    private final Set<ChunkPos> oldChunks = Collections.synchronizedSet(new HashSet<>());
    private static final Direction[] searchDirs = new Direction[] { Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP };
	private final Executor taskExecutor = Executors.newSingleThreadExecutor();

    public NewChunks() {
        super(MeteorRejectsAddon.CATEGORY,"new-chunks", "Detects completely new chunks using certain traits of them");
    }

	@Override
	public void onDeactivate() {
		if (remove.get()) {
			newChunks.clear();
			oldChunks.clear();
		}
		super.onDeactivate();
	}

	@EventHandler
	private void onRender(Render3DEvent event) {
		if (newChunksLineColor.get().a > 5 || newChunksSideColor.get().a > 5) {
			synchronized (newChunks) {
				for (ChunkPos c : newChunks) {
					if (mc.getCameraEntity().blockPosition().closerThan(c.getWorldPosition(), 1024)) {
						render(new AABB(Vec3.atLowerCornerOf(c.getWorldPosition()), Vec3.atLowerCornerOf(c.getWorldPosition().offset(16, renderHeight.get(), 16))), newChunksSideColor.get(), newChunksLineColor.get(), shapeMode.get(), event);
					}
				}
			}
		}

		if (oldChunksLineColor.get().a > 5 || oldChunksSideColor.get().a > 5){
			synchronized (oldChunks) {
				for (ChunkPos c : oldChunks) {
					if (mc.getCameraEntity().blockPosition().closerThan(c.getWorldPosition(), 1024)) {
						render(new AABB(Vec3.atLowerCornerOf(c.getWorldPosition()), Vec3.atLowerCornerOf(c.getWorldPosition().offset(16, renderHeight.get(), 16))), oldChunksSideColor.get(), oldChunksLineColor.get(), shapeMode.get(), event);
					}
				}
			}
		}
	}

	private void render(AABB box, Color sides, Color lines, ShapeMode shapeMode, Render3DEvent event) {
		event.renderer.box(
			box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, lines, shapeMode, 0);
	}

	@EventHandler
	private void onChunkData(ChunkDataEvent event) {
	    ChunkPos pos = event.chunk().getPos();
	    if (newChunks.contains(pos) || oldChunks.contains(pos)) return;

	    LevelChunk chunk = event.chunk();
	    taskExecutor.execute(() -> {
	        for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++) {
	            for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
	                for (int y = mc.level.getMinY(); y < mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z); y++) {
	                    FluidState fluid = chunk.getFluidState(x, y, z);
	                    if (!fluid.isEmpty() && !fluid.isSource()) {
	                        oldChunks.add(pos);
	                        return;
	                    }
	                }
	            }
	        }
	    });
	}

	@EventHandler
	private void onReadPacket(PacketEvent.Receive event) {
	    if (mc.level == null) return;

	    if (event.packet instanceof ClientboundSectionBlocksUpdatePacket packet) {
	        packet.runUpdates((pos, state) -> {
	            FluidState fluid = state.getFluidState();
	            if (fluid.isEmpty() || fluid.isSource()) return;
	            ChunkPos chunkPos = ChunkPos.containing(pos);
	            if (!newChunks.contains(chunkPos) && !oldChunks.contains(chunkPos))
	                newChunks.add(chunkPos);
	        });
	    } else if (event.packet instanceof ClientboundBlockUpdatePacket packet) {
	        FluidState fluid = packet.getBlockState().getFluidState();
	        if (fluid.isEmpty() || fluid.isSource()) return;
	        ChunkPos chunkPos = ChunkPos.containing(packet.getPos());
	        if (!newChunks.contains(chunkPos) && !oldChunks.contains(chunkPos))
	            newChunks.add(chunkPos);
	    }
	}
}
