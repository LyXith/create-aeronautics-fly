package dev.ryanhcode.offroad.mixin_interface.client_level;

import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler.ClientBlockBreakingData;
import net.minecraft.core.BlockPos;

import java.util.Map;

public interface MultiMiningDestructionExtension {

    /**
     * Manually adds the given map of Block Positions and Client Block breaking data to the client level under the SAME ID <br/>
     * Bypasses vanilla's {@link net.minecraft.client.multiplayer.ClientLevel#destroyBlockProgress(int, BlockPos, int)}
     *
     * @param id The given id to be associated with all client breaking data
     * @param clientData The given data to be passed to client level
     */
    void offroad$manuallyAddMultiDestructionProgress(int id, Map<BlockPos, ClientBlockBreakingData> clientData);
}
