# ============================================================
# P3  飞行器 / Aeronautics
# mixin: aeronautics propeller_bearing.*(2) / propeller_collision /
#        levitite.*(4 common + 4 client) / steam_vent.*(2) /
#        potato_cannon_projectiles / shears_break_envelopes /
#        flywheel_block_entity / custom_situational_music.*(2) /
#        levitite.PonderUIMixin / levitite.TerrainParticleMixin
# 摆放方向: 正北 (-Z)，z = -2..-26
# 提醒: adjustable_burner 需手动加燃料；levitite 只对实体/粒子生效
# ============================================================
fill ~-3 ~-1 ~-1 ~3 ~-1 ~-28 stone keep

setblock ~0 ~ ~-3 aeronautics:adjustable_burner[powered=true]
setblock ~0 ~ ~-7 aeronautics:propeller_bearing[facing=north]
setblock ~0 ~ ~-11 aeronautics:steam_vent[facing=north]
setblock ~0 ~ ~-15 aeronautics:levitite
setblock ~0 ~ ~-19 aeronautics:pearlescent_levitite
setblock ~0 ~ ~-24 aeronautics:mounted_potato_cannon
