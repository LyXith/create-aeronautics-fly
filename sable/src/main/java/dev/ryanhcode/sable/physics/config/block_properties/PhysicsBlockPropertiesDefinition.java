package dev.ryanhcode.sable.physics.config.block_properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The definition of the physics block properties for a block
 */
public record PhysicsBlockPropertiesDefinition(ExtraCodecs.TagOrElementLocation selector,
                                               int priority,
                                               Map<Identifier, Object> properties,
                                               Optional<Map<BlockStateConditionSet, Map<Identifier, Object>>> overrides) {

    public static final Codec<Map<Identifier, Object>> PROPERTIES_CODEC =
            Codec.dispatchedMap(Identifier.CODEC, PhysicsBlockPropertyTypes::getPropertyCodec);

    public static final Codec<PhysicsBlockPropertiesDefinition> CODEC =
            RecordCodecBuilder.create(i -> i.group(
                    ExtraCodecs.TAG_OR_ELEMENT_ID.fieldOf("selector").forGetter(PhysicsBlockPropertiesDefinition::selector),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("priority", 1000).forGetter(PhysicsBlockPropertiesDefinition::priority),
                    PROPERTIES_CODEC.fieldOf("properties").forGetter(PhysicsBlockPropertiesDefinition::properties),
                    Codec.dispatchedMap(BlockStateConditionSet.CODEC, (ignored) -> PROPERTIES_CODEC)
                            .optionalFieldOf("overrides").forGetter(PhysicsBlockPropertiesDefinition::overrides)
            ).apply(i, PhysicsBlockPropertiesDefinition::new));

    public static final StreamCodec<ByteBuf, PhysicsBlockPropertiesDefinition> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC);

    @Override
    public int hashCode() {
        return Objects.hash(this.selector);
    }

    @Override
    public String toString() {
        return "PhysicsBlockPropertiesDefinition{selector=%s, properties=%s}".formatted(this.selector, this.properties);
    }
}
