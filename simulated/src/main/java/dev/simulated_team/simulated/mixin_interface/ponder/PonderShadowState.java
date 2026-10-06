package dev.simulated_team.simulated.mixin_interface.ponder;

/**
 * 26.3 把平台阴影的 {@code 0x66000000} 颜色常量从 {@code SceneRenderer.renderScene}
 * 挪进了内部 record {@code SceneRenderer$ShadowRenderState.addBlackVertex}。
 * 两个方法分处不同的类，无法共享 mixin 局部状态，所以用这个持有器把
 * {@code renderScene} 算出来的淡出系数传给 {@code addBlackVertex}。
 *
 * <p>{@code renderScene} 在同一帧内先于 {@code ShadowRenderState.render} 执行，
 * 客户端渲染又是单线程，因此一个普通静态字段是安全的。
 */
public final class PonderShadowState {

    /** 平台阴影的淡出系数，1.0 表示完全不淡出。 */
    public static float fade = 1.0f;

    private PonderShadowState() {
    }
}
