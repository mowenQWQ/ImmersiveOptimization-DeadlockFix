import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import java.io.*;
import java.nio.file.*;

/**
 * 字节码级修复 immersive_optimization 的死锁问题。
 *
 * 问题：ForgeForcedChunkLookup.getForcedChunks(ServerLevel) 在 Worker 线程被调用时，
 * 会读取 ForcedChunksSavedData / ForgeChunkManager tickets（内部持有存储锁），
 * 与主线程的区块管理(ServerChunkCache)抢锁导致死锁 (ServerHangWatchdog 60s)。
 *
 * 修复：把方法体替换为只读内存集合 level.getForcedChunks()（无锁），
 * 保留 forced-chunk culling 功能，同时完全不触碰任何带锁的存储数据。
 */
public class PatchForcedChunkLookup {

    static final String CLASS_INTERNAL = "net/conczin/immersive_optimization/ForgeForcedChunkLookup";
    static final String METHOD_NAME = "getForcedChunks";
    static final String METHOD_DESC = "(Lnet/minecraft/server/level/ServerLevel;)Lit/unimi/dsi/fastutil/longs/LongSet;";

    public static void main(String[] args) throws Exception {
        Path in = Paths.get(args[0]);
        Path out = args.length > 1 ? Paths.get(args[1]) : in;
        byte[] original = Files.readAllBytes(in);

        ClassReader cr = new ClassReader(original);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals(METHOD_NAME) && desc.equals(METHOD_DESC)) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            // 替换方法体：返回 new LongOpenHashSet(level.getForcedChunks())
                            // LongOpenHashSet 构造器接受 LongCollection
                            mv.visitTypeInsn(Opcodes.NEW, "it/unimi/dsi/fastutil/longs/LongOpenHashSet");
                            mv.visitInsn(Opcodes.DUP);
                            mv.visitVarInsn(Opcodes.ALOAD, 1); // level
                            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                "net/minecraft/server/level/ServerLevel",
                                "m_8902_", "()Lit/unimi/dsi/fastutil/longs/LongSet;", false);
                            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                "it/unimi/dsi/fastutil/longs/LongOpenHashSet",
                                "<init>", "(Lit/unimi/dsi/fastutil/longs/LongCollection;)V", false);
                            mv.visitInsn(Opcodes.ARETURN);
                            mv.visitMaxs(0, 0);
                            mv.visitEnd();
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        Files.write(out, cw.toByteArray());
        System.out.println("Patched: " + out);
    }
}
