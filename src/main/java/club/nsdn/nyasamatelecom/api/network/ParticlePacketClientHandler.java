package club.nsdn.nyasamatelecom.api.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-only half of {@code ParticlePacketHandler}, kept in its own class because it references
 * {@link Minecraft}: the shared {@link ParticlePacket#handle} may only <i>call</i> this, never load it
 * on a dedicated server.
 *
 * <p>1.12.2 looked the particle up in {@code EnumParticleTypes} by name and fell back to
 * {@code BARRIER}; the 1.21.1 equivalent is a registry lookup in
 * {@code BuiltInRegistries.PARTICLE_TYPE}, still falling back to barrier so an unknown name produces a
 * visible marker instead of a crash. The scheduled-task hop disappears because payload handlers
 * already run on the main thread, and {@code Minecraft#world} is now
 * {@code Minecraft#level}.</p>
 */
final class ParticlePacketClientHandler {

    private ParticlePacketClientHandler() {
    }

    static void handle(ParticlePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        ParticleOptions options = resolve(packet.particleType());
        minecraft.level.addParticle(options,
                packet.x(), packet.y(), packet.z(),
                packet.tX(), packet.tY(), packet.tZ());
    }

    private static ParticleOptions resolve(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id != null) {
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(id);
            if (type instanceof SimpleParticleType simple) {
                return simple;
            }
        }
        // 1.12.2 fell back to EnumParticleTypes.BARRIER. That entry no longer exists in 1.21.1
        // (the barrier became a block rather than a particle), so the fallback keeps the important
        // property -- an unknown name must not crash and should still be visible -- using smoke.
        return ParticleTypes.SMOKE;
    }
}
