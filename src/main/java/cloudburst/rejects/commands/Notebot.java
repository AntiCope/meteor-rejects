package cloudburst.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.commands.Command;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.Rotations;
import minegame159.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Notebot extends Command {

    private enum Stage {
        None,
        SetUp,
        Tune,
        Playing,
        Preview
    }

    private final List<BlockPos> possibleBlockPos = new java.util.ArrayList<BlockPos>(Collections.emptyList());

    private int tickDelay = 0;
    private int tempo = 5;
    private final List<Integer> song = new java.util.ArrayList<Integer>(Collections.emptyList());
    private final List<Integer> uniqueNotes = new java.util.ArrayList<Integer>(Collections.emptyList());
    private final HashMap<Integer, BlockPos> blockPositions = new HashMap<Integer, BlockPos>();
    private int ticks = 0;
    private int count = 0;
    private int currentNote = 0;
    private Stage stage = Stage.None;

    public Notebot() {
        super("notebot", "Plays noteblocks nicely.");
        for (int y = -5; y < 5; y++) {
            for (int x = -5; x < 5; x++) {
                if (y!=0||x!=0) {
                    BlockPos pos = new BlockPos(x, 0, y);
                    if (pos.getSquaredDistance(0, 0, 0, true) < (4.3*4.3)-0.5) {
                        possibleBlockPos.add(pos);
                    }
                }
            }
        }
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("tempo").then(argument("value", IntegerArgumentType.integer()).executes(ctx -> {
            tempo = ctx.getArgument("value",Integer.class);
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("load").then(argument("file", StringArgumentType.greedyString()).executes(ctx -> {
            String filepath = ctx.getArgument("file",String.class);
            File file = MeteorClient.FOLDER.toPath().resolve(String.format("notebot/%s.txt",filepath)).toFile();
            if (!file.exists() || !file.isFile()) {
                ChatUtils.prefixError("Notebot","File doesn't exist.");
                return SINGLE_SUCCESS;
            }
            if (!loadFile(file.toPath())) return SINGLE_SUCCESS;
            if (!setupBlocks()) return SINGLE_SUCCESS;
            MeteorClient.EVENT_BUS.subscribe(this);
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("preview").then(argument("file", StringArgumentType.greedyString()).executes(ctx -> {
            String filepath = ctx.getArgument("file",String.class);
            File file = MeteorClient.FOLDER.toPath().resolve(String.format("notebot/%s.txt",filepath)).toFile();
            if (!file.exists() || !file.isFile()) {
                ChatUtils.prefixError("Notebot","File doesn't exist.");
                return SINGLE_SUCCESS;
            }
            if (!loadFile(file.toPath())) return SINGLE_SUCCESS;
            stage = Stage.Preview;
            currentNote = 0;
            MeteorClient.EVENT_BUS.subscribe(this);
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("play").executes(ctx -> {
            startPlaying();
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("stop").executes(ctx -> {
            stopPlaying();
            return SINGLE_SUCCESS;
        }));
    }

    private boolean loadFile(Path file) {
        List<String> data;
        try {
            data = Files.readAllLines(file);
        } catch (IOException e) {
            ChatUtils.prefixError("Notebot","File error");
            return false;
        }
        song.clear();
        for (int i = 0; i < data.size(); i++) {
            try {
                int v = Integer.parseInt(data.get(i));
                song.add(v);
            } catch (NumberFormatException e) {
                ChatUtils.prefixError("Notebot", "Invalid character at line %d", i);
            }
            
        }
        return true;
    }

    private boolean setupBlocks() {
        for (int i = 0; i < song.size(); i++) {
            if (!uniqueNotes.contains(song.get(i))) {
                uniqueNotes.add(song.get(i));
            }
        }
        if (uniqueNotes.size() > possibleBlockPos.size()) {
            ChatUtils.prefixError("Notebot","Too many notes. %d is the maximum.", possibleBlockPos.size());
            return false;
        }
        currentNote = 0;
        stage = Stage.SetUp;
        return true;
    }

    private void startPlaying() {
        if (stage != Stage.Playing) {
            ChatUtils.prefixError("Notebot","You need to load a song first");
            return;
        }
        MeteorClient.EVENT_BUS.subscribe(this);
        ticks = 0;
        currentNote = 0;
    }

    private void stopPlaying() {
        ChatUtils.prefixInfo("Notebot","Stopped.");
        MeteorClient.EVENT_BUS.unsubscribe(this);
        stage = Stage.None;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ticks++;
        if (stage != Stage.Playing && stage != Stage.Preview) {
            if (ticks<tickDelay) return;
        } else {
            if (ticks<20/tempo) return;
        }

        ticks = 0;
        switch (stage) {
            case None:
                return;
            case Tune:
                onTickTune();
                return;
            case SetUp:
                onTickSetup();
                return;
            case Playing:
                onTickPlay();
                return;
            case Preview:
                onTickPreview();
                return;
        }
    }

    private void onTickPreview() {
        if (currentNote>=song.size()) {
            stopPlaying();
            return;
        }
        int note = song.get(currentNote);
        if (note==-1) {
            currentNote++;
            return;
        }
        mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, 2f, note);
        currentNote++;
    }

    private void onTickSetup() {
        if (currentNote>=uniqueNotes.size()) {
            stage = Stage.Playing;
            MeteorClient.EVENT_BUS.unsubscribe(this);
            return;
        }
        int slot = InvUtils.findItemInHotbar(Items.NOTE_BLOCK);
        BlockPos pos = mc.player.getBlockPos().add(possibleBlockPos.get(currentNote));
        if (slot == -1) {
            ChatUtils.prefixError("Notebot","Not enough noteblocks");
            stopPlaying();
            return;
        }
        if (uniqueNotes.get(currentNote) != -1) {
            if (!BlockUtils.place(pos,Hand.MAIN_HAND, slot, true, 100, true)) {
                ChatUtils.prefixError("Notebot","Couldn't place noteblock");
                stopPlaying();
                return;
            }
        }
        blockPositions.put(uniqueNotes.get(currentNote), pos);
        stage = Stage.Tune;
    }

    private void onTickTune() {
        BlockPos pos = mc.player.getBlockPos().add(possibleBlockPos.get(currentNote));
        Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), 100, this::tuneRotate);
    }

    private void tuneRotate() {
        BlockPos pos = mc.player.getBlockPos().add(possibleBlockPos.get(currentNote));
        if (!tuneBlock(pos, uniqueNotes.get(currentNote))) {
            stopPlaying();
        }
    }

    private void onTickPlay() {

        if (currentNote >= song.size()) {
            stopPlaying();
            return;
        }
        int note = song.get(currentNote);
        if (note==-1) {
            currentNote++;
            return;
        }
        BlockPos pos = blockPositions.get(note);
        Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), 100, this::playRotate);
    }

    private void playRotate() {
        int note = song.get(currentNote);
        BlockPos pos = blockPositions.get(note);

        mc.interactionManager.attackBlock(pos,Direction.DOWN);
        currentNote++;
    }

    private boolean tuneBlock(BlockPos pos, int note) {
        if (mc.world == null || mc.player == null) return false;
        if (count>60) {
            ChatUtils.prefixError("Notebot","Couldn't tune the noteblock.");
            return false;
        }

        BlockState block = mc.world.getBlockState(pos);
        if (block.getBlock() != Blocks.NOTE_BLOCK && note != -1) {
            ChatUtils.prefixError("Notebot","Block isn't a noteblock");
            return false;
        }
        
        if (block.get(NoteBlock.NOTE).equals(note) || note == -1) {
            currentNote++;
            stage=Stage.SetUp;
            count=0;
            return true;
        };
        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(
            mc.player.getPos(), Direction.UP, pos, true)
        );
        count++;
        return true;
    }
}
