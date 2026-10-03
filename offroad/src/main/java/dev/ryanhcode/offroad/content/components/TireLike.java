package dev.ryanhcode.offroad.content.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.Create;
import dev.ryanhcode.offroad.Offroad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record TireLike(float radius, Vec3 rotation, Vec3 offset, Optional<Identifier> model) {
    public static final Codec<TireLike> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    Codec.FLOAT.optionalFieldOf("radius", 1.0f).forGetter(TireLike::radius),
                    Vec3.CODEC.optionalFieldOf("rotation", new Vec3(90, 0, 0)).forGetter(TireLike::rotation),
                    Vec3.CODEC.optionalFieldOf("offset", new Vec3(0, 0, 0)).forGetter(TireLike::offset),
                    Identifier.CODEC.optionalFieldOf("model").forGetter(TireLike::model)
            ).apply(i, TireLike::new));

    public TireLike(float radius, Vec3 rotation, Vec3 offset, @Nullable Identifier model) {
        this(radius, rotation, offset, Optional.ofNullable(model));
    }

    public TireLike(final float radius) {
        this(radius, new Vec3(90, 0, 0), new Vec3(0, 0, 0), Optional.empty());
    }

    public TireLike(final float radius, final Identifier model) {
        this(radius, new Vec3(90, 0, 0), new Vec3(0, 0, 0), Optional.of(model));
    }

    /*
    public static final TireLike SMALL_TIRE = new TireLike(9.0f / 16.0f, Offroad.path("item/small_tire/block"));
    public static final TireLike TIRE = new TireLike(15.5f / 16.0f, Offroad.path("item/tire/block"));
    public static final TireLike LARGE_TIRE = new TireLike(1.0f + 4.0f / 16.0f, Offroad.path("item/large_tire/block"));
    public static final TireLike MONSTROUS_TIRE = new TireLike(1.0f + 14.0f / 16.0f, Offroad.path("item/monstrous_tire/block"));
    */
    public static final TireLike SMALL_TIRE = new TireLike(12.0f / 16.0f, Offroad.path("item/small_tire/block"));
    public static final TireLike TIRE = new TireLike(15.5f / 16.0f, Offroad.path("item/tire/block"));
    public static final TireLike LARGE_TIRE = new TireLike(1.0f + 4.0f / 16.0f, Offroad.path("item/large_tire/block"));
    public static final TireLike MONSTROUS_TIRE = new TireLike(2.0f, Offroad.path("item/monstrous_tire/block"));
    public static final TireLike CRUSHING_WHEEL = new TireLike(1.0f, Identifier.fromNamespaceAndPath(Create.MOD_ID, "block/crushing_wheel/block"));
    public static final TireLike WATER_WHEEL = new TireLike(1.0f, Identifier.fromNamespaceAndPath(Create.MOD_ID, "block/water_wheel/wheel"));
    public static final TireLike FLYWHEEL = new TireLike(1.0f + 6.0f / 16.0f, Identifier.fromNamespaceAndPath(Create.MOD_ID, "block/flywheel/flywheel"));
    public static final TireLike LARGE_WATER_WHEEL = new TireLike(2.0f + 7.0f / 16.0f, Identifier.fromNamespaceAndPath(Create.MOD_ID, "block/large_water_wheel/block"));
    public static final TireLike ROCKCUTTING_WHEEL = new TireLike(0.8f, new Vec3(90, 0, 0), Vec3.ZERO, Offroad.path("block/rockcutting_wheel/wheel"));
    public static final TireLike MECHANICAL_ROLLER = new TireLike(0.7f, Vec3.ZERO, new Vec3(0, -0.5f, 0), Identifier.fromNamespaceAndPath(Create.MOD_ID, "block/mechanical_roller/wheel"));
}
