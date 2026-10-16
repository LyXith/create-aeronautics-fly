package dev.simulated_team.simulated.fabric.service;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端专属的服务端访问入口，供 {@link FabricSimItemService} 在没有逻辑服务器时
 * （例如主菜单里的 Ponder 场景）回退到集成服务器。
 *
 * <p><b>为什么必须单独成类：</b>{@code FabricSimItemService} 是公共入口注册的常驻类，
 * 专用服务器同样会加载并<b>校验</b>它。原先它内联了
 * {@code Minecraft.getInstance().getSingleplayerServer()}，验证器要判定
 * {@code IntegratedServer <: MinecraftServer}，就会去加载服务端不存在的
 * {@code net.minecraft.client.server.IntegratedServer}，链接失败抛
 * {@code NoClassDefFoundError} —— 结果是 {@code SERVER_STARTED} 一进就崩、
 * 专用服务器起不来（生产 smoke 测试抓到的 {@code crash-2026-10-16_17.28.42-server}）。
 *
 * <p>把这个调用挪进独立类之后，{@code FabricSimItemService} 的字节码里不再出现任何客户端类型，
 * 服务端链接它毫无问题；而本类只有在客户端分支真的执行到 {@code invokestatic} 时才会被加载，
 * 专用服务器永远不会碰到。
 */
final class ClientServerAccess {

    private ClientServerAccess() {}

    static @Nullable ServerLevel overworldOrNull() {
        final Minecraft minecraft = Minecraft.getInstance();
        final MinecraftServer integrated = minecraft == null ? null : minecraft.getSingleplayerServer();
        return integrated == null ? null : integrated.overworld();
    }
}
