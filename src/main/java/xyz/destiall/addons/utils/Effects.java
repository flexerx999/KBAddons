package xyz.destiall.addons.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.color.AlphaColor;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustColorTransitionData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;

import java.util.Collection;

public class Effects {
    public static PacketEventsAPI<?> API;
    public static void setup(Addons plugin) {
        PacketEvents.setAPI(API = SpigotPacketEventsBuilder.build(plugin));
        API.load();
    }

    public static void sendPacket(Location location, double radius, ParticlePacket<? extends ParticleData> packet) {
        packet.sendTo(location.getWorld().getPlayers(), radius);
    }

    public static void spawnDust(Location location) {
        sendPacket(location, 60, new ParticlePacket<>(Type.DUST, location).longDistance(true).data(new ParticleDustData(1f, AlphaColor.WHITE)));
    }

    public static void spawnDust(Location location, int r, int g, int b) {
        sendPacket(location, 60, new ParticlePacket<>(Type.DUST, location).longDistance(true).data(new ParticleDustData(1f, new AlphaColor(r, g, b))));
    }

    public static void spawnDust(Location location, Color color) {
        sendPacket(location, 60, new ParticlePacket<>(Type.DUST, location).longDistance(true).data(new ParticleDustData(1f, new AlphaColor(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()))));
    }

    public static void spawnRecon(Location location) {
        sendPacket(location, 60, new ParticlePacket<>(Type.ELECTRIC_SPARK, location).longDistance(true));
    }

    public static void spawnSmoke(Location location) {
        sendPacket(location, 60, new ParticlePacket<>(Type.CLOUD, location).longDistance(true));
    }

    public static void spawnMiniExplosion(Location location) {
        sendPacket(location, 60, new ParticlePacket<>(Type.EXPLOSION_EMITTER, location).longDistance(true));
    }

    public static void spawnExplosion(Location location) {
        sendPacket(location, 60, new ParticlePacket<>(Type.EXPLOSION, location).longDistance(true));
    }

    public static void spawnCrit(Location location) {
        sendPacket(location, 60, new ParticlePacket<>(Type.CRIT, location).longDistance(true));
    }

    static class ParticlePacket<T extends ParticleData> {
        private final Type type;
        private final Particle<@org.jetbrains.annotations.NotNull T> particle;
        private final Location location;
        private final Vector offset = new Vector(0, 0, 0);
        private boolean longDistance = false;
        private int maxSpeed = 0;
        private int particleCount = 1;

        public ParticlePacket(Type type, Location location) {
            this.type = type;
            this.particle = new Particle<>((ParticleType<T>) type.getType());
            this.location = location;
        }

        public ParticlePacket<T> data(T data) {
            particle.setData(data);
            return this;
        }

        public ParticlePacket<T> longDistance(boolean longDistance) {
            this.longDistance = longDistance;
            return this;
        }

        public ParticlePacket<T> maxSpeed(int speed) {
            this.maxSpeed = speed;
            return this;
        }

        public ParticlePacket<T> count(int count) {
            this.particleCount = count;
            return this;
        }

        public ParticlePacket<T> offset(float x, float y, float z) {
            this.offset.setX(x).setY(y).setZ(z);
            return this;
        }

        public PacketWrapper<?> getPacket() {
            return new WrapperPlayServerParticle(particle,
                    longDistance,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    new Vector3f((float) offset.getX(), (float) offset.getY(), (float) offset.getZ()),
                    maxSpeed, particleCount);
        }

        public void sendTo(Player player) {
            PacketWrapper<?> packet = getPacket();
            API.getPlayerManager().getUser(player).sendPacket(packet);
        }

        public void sendTo(Collection<Player> players, double radius) {
            PacketWrapper<?> packet = getPacket();
            for (Player player : players) {
                if (location.distanceSquared(player.getLocation()) > radius * radius)
                    continue;

                API.getPlayerManager().getUser(player).sendPacket(packet);
            }
        }
    }

    enum Type {
        CRIT(org.bukkit.Particle.CRIT),
        EXPLOSION(org.bukkit.Particle.EXPLOSION),
        CLOUD(org.bukkit.Particle.CLOUD),
        EXPLOSION_EMITTER(org.bukkit.Particle.EXPLOSION_EMITTER),
        ELECTRIC_SPARK(org.bukkit.Particle.ELECTRIC_SPARK),
        DUST(org.bukkit.Particle.DUST, ParticleDustData.class),
        DUST_TRANSITION(org.bukkit.Particle.DUST_COLOR_TRANSITION, ParticleDustColorTransitionData.class);

        private final Class<? extends ParticleData> data;
        private final ParticleType<?> type;
        Type(org.bukkit.Particle spigotParticle) {
            type = SpigotConversionUtil.fromBukkitParticle(spigotParticle);
            data = null;
        }

        Type(org.bukkit.Particle spigotParticle, Class<? extends ParticleData> data) {
            type = SpigotConversionUtil.fromBukkitParticle(spigotParticle);
            this.data = data;
        }

        public Class<? extends ParticleData> getData() {
            return data;
        }

        public ParticleType<?> getType() {
            return type;
        }
    }
}
