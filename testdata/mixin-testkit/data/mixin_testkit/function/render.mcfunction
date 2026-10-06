# ============================================================
# P0  渲染管线冒烟（针对「管线注册太晚 / 漏 register」那批修复）
# mixin: laser.LevelRendererMixin / burner_flame.LevelRendererMixin /
#        end_sea.LevelRendererMixin / sable_render.* / physics_staff.*
# 摆放方向: 正东 (+X)，x = 1..18
# 通过标准: 激光光束可见、传感器有反馈、二极管发光、
#           控制台 0 条 shader / pipeline 报错
# ============================================================
fill ~-1 ~-1 ~-2 ~20 ~-1 ~3 stone keep

# 红石块给激光指针供电
setblock ~0 ~ ~0 redstone_block
setblock ~1 ~ ~0 simulated:laser_pointer[facing=east]
setblock ~6 ~ ~0 simulated:optical_sensor[facing=west]
setblock ~9 ~ ~0 simulated:laser_sensor[facing=west]
setblock ~12 ~ ~0 simulated:redstone_accumulator
# 燃烧器需手动加燃料才会出火苗
setblock ~16 ~ ~0 aeronautics:adjustable_burner[powered=true]
