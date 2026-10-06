# ============================================================
# P1  动力网络 / Create 集成
# mixin: extra_kinetics.*(9) / auto_orientation.*(2) / torsion_spring.*(3) /
#        flicker_tally_removal / create_assembly.*(4) / redstone_link.* /
#        conditional_display_target.*(2) / dynamic_stress.KineticStatsMixin /
#        extra_kinetics.goggle_tooltips / smart_block_entity
# 摆放方向: 正南 (+Z)，z = 1..20
# 提醒: creative_motor 默认转速为 0，摆好后对着它滚轮给转速
# ============================================================
fill ~-1 ~-1 ~-3 ~3 ~-1 ~22 stone keep

setblock ~0 ~ ~1 create:creative_motor[facing=south]
setblock ~0 ~ ~2 create:shaft[axis=z]
setblock ~0 ~ ~3 create:shaft[axis=z]
setblock ~0 ~ ~4 create:cogwheel[axis=z]
setblock ~0 ~ ~5 create:large_cogwheel[axis=z]
setblock ~0 ~ ~6 create:stressometer[facing=south]
setblock ~0 ~ ~7 create:speedometer[facing=south]
setblock ~0 ~ ~8 create:shaft[axis=z]
setblock ~0 ~ ~9 simulated:torsion_spring[facing=south]
setblock ~0 ~ ~10 create:shaft[axis=z]
setblock ~0 ~ ~11 create:rotation_speed_controller[axis=z]
setblock ~0 ~ ~12 create:shaft[axis=z]
setblock ~0 ~ ~13 create:mechanical_bearing[facing=south]
setblock ~0 ~ ~16 create:display_link[facing=south]
setblock ~0 ~ ~18 create:redstone_link[facing=south]
