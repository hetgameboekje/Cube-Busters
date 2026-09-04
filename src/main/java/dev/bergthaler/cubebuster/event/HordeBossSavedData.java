package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-wide Horde Boss state: the total spawns-today count (for {@link Config#hordeBossServerDailyCap}) and
 * the day's active spawn clusters (for {@link Config#hordeBossClusterCap}/{@link Config#hordeBossClusterChunkRadius}).
 * <p>
 * This is genuinely server-wide (not per-player) state, so unlike {@link HordeBossState} it uses vanilla's own
 * {@link SavedData} mechanism rather than a data attachment - both persist to disk across restarts, SavedData is
 * just the correct tool for level/server-scoped data instead of player-scoped data. Stored on the Overworld
 * (Horde Boss spawn attempts are Overworld-only, see {@code HordeBossSpawnHandler}).
 */
public final class HordeBossSavedData extends SavedData {
    public static final String ID = "cubebuster_horde_boss";
    public static final Factory<HordeBossSavedData> FACTORY =
            new Factory<>(HordeBossSavedData::new, HordeBossSavedData::load, DataFixTypes.LEVEL);

    private int serverSpawnsToday;
    private long lastResetDay = -1;
    private final List<Cluster> clusters = new ArrayList<>();

    /**
     * One clustered group of spawn attempts for the current day: {@code anchorChunkX}/{@code anchorChunkZ} is
     * the chunk position of the first spawn that formed the cluster (later spawns just need to be within
     * {@link Config#hordeBossClusterChunkRadius} chunks of it, not of every member), {@code members} is every
     * player who has contributed a spawn to it, and {@code count} is the cluster's total spawns consumed so far
     * against {@link Config#hordeBossClusterCap}.
     */
    private static final class Cluster {
        int anchorChunkX;
        int anchorChunkZ;
        int count;
        final List<UUID> members = new ArrayList<>();
    }

    public static HordeBossSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        HordeBossSavedData data = new HordeBossSavedData();
        data.serverSpawnsToday = tag.getInt("ServerSpawnsToday");
        data.lastResetDay = tag.getLong("LastResetDay");
        ListTag clusterList = tag.getList("Clusters", Tag.TAG_COMPOUND);
        for (int i = 0; i < clusterList.size(); i++) {
            CompoundTag clusterTag = clusterList.getCompound(i);
            Cluster cluster = new Cluster();
            cluster.anchorChunkX = clusterTag.getInt("AnchorChunkX");
            cluster.anchorChunkZ = clusterTag.getInt("AnchorChunkZ");
            cluster.count = clusterTag.getInt("Count");
            ListTag memberList = clusterTag.getList("Members", Tag.TAG_STRING);
            for (int j = 0; j < memberList.size(); j++) {
                cluster.members.add(UUID.fromString(memberList.getString(j)));
            }
            data.clusters.add(cluster);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("ServerSpawnsToday", serverSpawnsToday);
        tag.putLong("LastResetDay", lastResetDay);
        ListTag clusterList = new ListTag();
        for (Cluster cluster : clusters) {
            CompoundTag clusterTag = new CompoundTag();
            clusterTag.putInt("AnchorChunkX", cluster.anchorChunkX);
            clusterTag.putInt("AnchorChunkZ", cluster.anchorChunkZ);
            clusterTag.putInt("Count", cluster.count);
            ListTag memberList = new ListTag();
            for (UUID member : cluster.members) {
                memberList.add(StringTag.valueOf(member.toString()));
            }
            clusterTag.put("Members", memberList);
            clusterList.add(clusterTag);
        }
        tag.put("Clusters", clusterList);
        return tag;
    }

    /** Rolls server-wide state (and clears the day's clusters) over to a new day if {@code currentDay} has advanced. */
    private void rolloverIfNeeded(long currentDay) {
        if (currentDay != lastResetDay) {
            serverSpawnsToday = 0;
            clusters.clear();
            lastResetDay = currentDay;
            setDirty();
        }
    }

    public boolean isServerCapReached(long currentDay) {
        rolloverIfNeeded(currentDay);
        return serverSpawnsToday >= Config.hordeBossServerDailyCap;
    }

    /**
     * Whether a spawn attempt at {@code chunkX}/{@code chunkZ} for {@code player} would be blocked by the
     * clustered group cap - i.e. it would join (or form) a cluster that's already at
     * {@link Config#hordeBossClusterCap}. Doesn't mutate state; call {@link #recordSpawn} once the spawn
     * actually happens.
     */
    public boolean isClusterCapReached(long currentDay, int chunkX, int chunkZ) {
        rolloverIfNeeded(currentDay);
        Cluster cluster = findCluster(chunkX, chunkZ);
        return cluster != null && cluster.count >= Config.hordeBossClusterCap;
    }

    /** Records a successful spawn: bumps the server total and joins/creates the chunk-position's cluster. */
    public void recordSpawn(long currentDay, int chunkX, int chunkZ, UUID player) {
        rolloverIfNeeded(currentDay);
        serverSpawnsToday++;
        Cluster cluster = findCluster(chunkX, chunkZ);
        if (cluster == null) {
            cluster = new Cluster();
            cluster.anchorChunkX = chunkX;
            cluster.anchorChunkZ = chunkZ;
            clusters.add(cluster);
        }
        cluster.count++;
        if (!cluster.members.contains(player)) {
            cluster.members.add(player);
        }
        setDirty();
    }

    private Cluster findCluster(int chunkX, int chunkZ) {
        for (Cluster cluster : clusters) {
            int dx = Math.abs(chunkX - cluster.anchorChunkX);
            int dz = Math.abs(chunkZ - cluster.anchorChunkZ);
            // Chebyshev (chessboard) distance: max of the axis deltas, not Euclidean/Manhattan - matches the
            // spec's "<=5 chunks = clustered" wording (a 5-chunk Chebyshev radius is a square, not a circle).
            if (Math.max(dx, dz) <= Config.hordeBossClusterChunkRadius) {
                return cluster;
            }
        }
        return null;
    }
}
