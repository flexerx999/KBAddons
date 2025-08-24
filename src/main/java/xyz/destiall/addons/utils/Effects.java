package xyz.destiall.addons.utils;

import com.github.fierioziy.particlenativeapi.api.packet.ParticlePacket;
import com.github.fierioziy.particlenativeapi.api.particle.ParticleList_1_13;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import org.bukkit.Color;
import org.bukkit.Location;
import xyz.destiall.addons.Addons;

public class Effects {
    private static ParticleList_1_13 API;
    public static void setup() {
        API = ParticleNativeCore.loadAPI(Addons.INSTANCE).LIST_1_13;
    }

    public static void sendPacket(Location location, double radius, ParticlePacket packet) {
        packet.sendTo(location.getWorld().getPlayers(), (p) -> p.getLocation().distance(location) <= radius);
    }

    public static void spawnDust(Location location) {
        sendPacket(location, 60, API.DUST.color(Color.WHITE, 1d).packet(true, location));
    }

    public static void spawnDust(Location location, int r, int g, int b) {
        sendPacket(location, 60, API.DUST.color(r, g, b, 1d).packet(true, location));
    }

    public static void spawnDust(Location location, Color color) {
        sendPacket(location, 60, API.DUST.color(color, 1d).packet(true, location));
    }

    public static void spawnRecon(Location location) {
        sendPacket(location, 60, API.ELECTRIC_SPARK.packet(true, location));
    }

    public static void spawnSmoke(Location location) {
        sendPacket(location, 60, API.CLOUD.packet(true, location));
    }

    public static void spawnMiniExplosion(Location location) {
        sendPacket(location, 60, API.EXPLOSION_EMITTER.packet(true, location));
    }

    public static void spawnExplosion(Location location) {
        sendPacket(location, 60, API.EXPLOSION.packet(true, location));
    }

    public static void spawnCrit(Location location) {
        sendPacket(location, 60, API.CRIT.packet(true, location));
    }
}
