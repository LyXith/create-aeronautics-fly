package dev.simulated_team.simulated.mixin.structure_template;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/** Restores passenger entities when structures (including Ponder fixtures) are placed. */
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @WrapOperation(
            method = "createEntityIgnoreException",
            at = @At(
                    value = "INVOKE",
                    // 26.3: EntitySpawnReason 在这条路径上换成了 EntitySpawnRequest
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Ljava/util/Optional;"
            )
    )
    private static Optional<Entity> simulated$loadEntityWithPassengers(
            final ValueInput input,
            final Level level,
            final EntitySpawnRequest spawnReason,
            final Operation<Optional<Entity>> original
    ) {
        return Optional.ofNullable(EntityType.loadEntityRecursive(
                input,
                level,
                spawnReason,
                EntityProcessor.NOP
        ));
    }
}
