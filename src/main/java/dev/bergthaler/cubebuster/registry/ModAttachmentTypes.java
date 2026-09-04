package dev.bergthaler.cubebuster.registry;

import dev.bergthaler.cubebuster.Cubebuster;
import dev.bergthaler.cubebuster.event.AggroState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cubebuster.MODID);

    // Player-attached, saved with the player's own data (survives disconnects and server restarts) rather than
    // the old in-memory map that reset on restart. See AggroManager for the read/write side.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AggroState>> AGGRO =
            ATTACHMENT_TYPES.register("aggro", () -> AttachmentType.builder(() -> AggroState.EMPTY)
                    .serialize(AggroState.CODEC)
                    .build());

    private ModAttachmentTypes() {
    }
}
