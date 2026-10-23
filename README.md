# ImmersiveOptimization 死锁修复版

> 基于 [Luke100000/ImmersiveOptimization](https://github.com/Luke100000/ImmersiveOptimization) 的 GPL-3.0 修改，修复 1.20.1 Forge 版本的 ServerHangWatchdog 死锁问题。

> ⚠️ **AI 使用说明**：本作品使用 AI 辅助生成（生成时间：2026-09-11）。仅供学习与参考使用，发布、引用或投入使用前请人工核实关键信息。

## 这是什么

`ImmersiveOptimization` 是一个实体 tick 调度器（*a lightweight entity-tick-scheduler for doubling your TPS*）。官方版本存在已知并发死锁（[issue #33](https://github.com/Luke100000/ImmersiveOptimization/issues/33)，2026-05 开启，未关闭）：其 Worker 线程读取强制加载区块的存储数据时，与主线程的区块管理抢锁，导致单 tick 卡 60 秒被 watchdog 强制终止。

本仓库提供**字节码级修复**，仅改动 `ForgeForcedChunkLookup.getForcedChunks()` 一个方法，从根源消除锁竞争。

## 修复内容

- **问题**：`ForgeForcedChunkLookup.getForcedChunks` 在 Worker 线程读取 `ForcedChunksSavedData` / `ForgeChunkManager` 票据（内部持锁），与主线程 `ServerChunkCache` 死锁
- **修复**：改为只读内存中的强制加载区块集合（无锁），移除对存储层的读取
- **影响**：默认配置（`optimizeForceLoadedChunks=false`）下游戏行为完全一致；仅影响强加载区块实体的调度剔除优化

```java
// 修复后
public LongSet getForcedChunks(ServerLevel serverLevel) {
    return new LongOpenHashSet((LongCollection) serverLevel.getForcedChunks());
}
```

## 目录结构

```
├── src/                        # 反编译源码（CFR 0.152），已含修复
│   └── net/conczin/immersive_optimization/
├── patch/PatchForcedChunkLookup.java   # 字节码补丁工具源码（ASM 9.7）
├── LICENSE                     # GPL-3.0
└── README.md
```

> 修复后的 jar 以 GitHub Release 附件形式提供（见右侧 Releases）。

## 构建 / 使用

### 方式一：直接用修复后的 jar（推荐）

从 Releases 下载 `immersive_optimization-fixed.jar`，替换 mods 目录下的原 jar 即可。

### 方式二：从源码重新打补丁

用 ASM 9.7 对原 jar 执行补丁：

```bash
# 反编译（可选，看源码用）
java -jar cfr.jar immersive_optimization-forge-1.20.1-0.2.0.jar --outputdir src

# 字节码补丁
javac -cp "asm.jar;asm-commons.jar;asm-tree.jar" patch/PatchForcedChunkLookup.java
java -cp "asm.jar;asm-commons.jar;asm-tree.jar;patch" \
    PatchForcedChunkLookup \
    <原jar内ForgeForcedChunkLookup.class> <输出.class>

# 替换进新 jar
# （详见 patch/PatchForcedChunkLookup.java 顶部注释）
```

## 依赖工具

- **CFR 0.152**：反编译
- **ASM 9.7**（asm / asm-commons / asm-tree）：字节码注入

## License

[GPL-3.0](LICENSE)。本仓库为 [Luke100000/ImmersiveOptimization](https://github.com/Luke100000/ImmersiveOptimization) 的修改版，仅修复 `ForgeForcedChunkLookup` 死锁问题，未改动其余逻辑。

## 局限

- 未验证 `optimizeForceLoadedChunks=true` 场景（默认关闭，未测）
- 若死锁另有来源（如 `tickLevel` 的实体列表并发遍历），本补丁可能不完全覆盖，需进一步排查
