# ============================================================
# P4  交互 / 显示 / 传输
# mixin: conditional_display_target.*(2) / nav_table_compat.*(2) /
#        linked_controller_binding / schematicannon_fix /
#        redstone_link.LinkBehaviourMixin / redstone_link.RedstoneLinkNetworkHandlerMixin /
#        display_link screens / linked_typewriter.PonderTooltipHandlerMixin
# 摆放方向: 正东 (+X)，x = 24..44（与 render 错开，可与其它 kit 同时摆）
# ============================================================
fill ~23 ~-1 ~-3 ~46 ~-1 ~3 stone keep

setblock ~24 ~ ~0 create:display_link[facing=east]
setblock ~28 ~ ~0 create:redstone_link[facing=east]
setblock ~31 ~ ~0 create:blue_nixie_tube
setblock ~35 ~ ~0 create:schematicannon
setblock ~39 ~ ~0 create:schematic_table
setblock ~43 ~ ~0 simulated:linked_typewriter
