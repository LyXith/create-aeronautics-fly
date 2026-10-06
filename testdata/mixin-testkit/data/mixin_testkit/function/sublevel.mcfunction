# ============================================================
# P2  SubLevel / Sable 物理与坐标
# mixin: aeronautics create_sublevel.*(16) / balloon.*(2) /
#        simulated assembly_preventer / sable_hooks.*(2) /
#        rope.LevelExtractorMixin / rope.ClientSubLevelContainerMixin /
#        sable_render.*(3)
# 摆放方向: 正西 (-X)，x = -2..-24
# 说明: 这一组已有 18 个 @GameTest 覆盖（./gradlew :aeronautics:runGameTest），
#       本函数只用于人工复核「组装成 SubLevel 后的渲染 / 碰撞手感」
# ============================================================
fill ~-3 ~-1 ~-3 ~1 ~-1 ~3 stone keep

setblock ~-3 ~ ~0 simulated:physics_assembler
setblock ~-7 ~ ~0 simulated:swivel_bearing[facing=west]
setblock ~-11 ~ ~0 simulated:rope_winch[facing=west]
setblock ~-15 ~ ~0 create:mechanical_bearing[facing=west]
setblock ~-19 ~ ~0 create:linear_chassis
setblock ~-23 ~ ~0 simulated:docking_connector
