package your.mod.boostcolor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Installs the Low-Positive tooltip recoloring (see {@link LowPositiveColors}).
 *
 * <p>The color decision lives in hardcoded engine methods with no modding seam, and the script
 * loader forbids shadowing game classes, so the only way to change it is to rewrite those methods at
 * runtime. This class is a Java agent (both {@code premain} and {@code agentmain}) plus a
 * retransform-capable {@link ClassFileTransformer} that uses Javassist to:
 * <ul>
 *   <li>route reads of {@code GCOLOR.T().IGOOD} / {@code .IBAD} in {@code BoosterAbs.hover} and
 *       {@code BoostSpecs.hover} through {@link LowPositiveColors#pick(snake2d.util.color.COLOR, boolean)}, and</li>
 *   <li>push/pop the boostable key being rendered in the boostable-aware callers
 *       ({@code BoostSpecs.hover}, {@code Boostable.hover}/{@code hoverDetailed}, the tech Boosts
 *       browser {@code InfoBonuses$Boo.hoverInfoGet}).</li>
 * </ul>
 *
 * <p><b>Delivery.</b> {@link #install()} tries to self-attach (no setup for the player). If the JVM
 * blocks self-attach, it logs how to enable it with {@code -javaagent} and otherwise leaves the game
 * exactly as vanilla — the rest of the mod is unaffected. Every step is guarded; this never throws.
 */
public final class ColorAgent {

    /** Engine classes whose bytecode we rewrite (binary names). */
    private static final String BOOSTER_ABS = "game.boosting.BoosterAbs";
    private static final String BOOST_SPECS = "game.boosting.BoostSpecs";
    private static final String BOOSTABLE   = "game.boosting.Boostable";
    private static final String INFO_BOO    = "view.ui.tech.InfoBonuses$Boo";

    private static final String HELPER = "your.mod.boostcolor.LowPositiveColors";
    private static final String GCOLOR_TEXT = "util.colors.GCOLOR_TEXT";

    private static volatile boolean transformerInstalled = false;

    private ColorAgent() {}

    // ---- agent entry points -------------------------------------------------

    /** Entry point when loaded via {@code -javaagent}: classes are patched as they load. */
    public static void premain(String args, Instrumentation inst) {
        registerTransformer(inst);
    }

    /** Entry point when loaded by self-attach: classes already loaded must be retransformed. */
    public static void agentmain(String args, Instrumentation inst) {
        registerTransformer(inst);
        retransformLoadedTargets(inst);
    }

    private static synchronized void registerTransformer(Instrumentation inst) {
        if (transformerInstalled)
            return;
        try {
            inst.addTransformer(new Transformer(), true);
            transformerInstalled = true;
            System.out.println("[sos-extended-boostables] Low-Positive tooltip recolor: transformer registered.");
        } catch (Throwable t) {
            System.err.println("[sos-extended-boostables] could not register color transformer: " + t);
        }
    }

    private static void retransformLoadedTargets(Instrumentation inst) {
        try {
            List<Class<?>> targets = new ArrayList<Class<?>>();
            for (Class<?> c : inst.getAllLoadedClasses()) {
                String n = c.getName();
                if ((BOOSTER_ABS.equals(n) || BOOST_SPECS.equals(n) || BOOSTABLE.equals(n) || INFO_BOO.equals(n))
                        && inst.isModifiableClass(c)) {
                    targets.add(c);
                }
            }
            if (!targets.isEmpty())
                inst.retransformClasses(targets.toArray(new Class<?>[0]));
        } catch (Throwable t) {
            System.err.println("[sos-extended-boostables] retransform of loaded classes failed: " + t);
        }
    }

    // ---- self-attach --------------------------------------------------------

    /**
     * Attempts to enable the recoloring by self-attaching this jar as a Java agent. Returns
     * {@code true} if the transformer is active afterwards. Never throws; on any failure the game is
     * left as vanilla. Idempotent.
     */
    public static boolean install() {
        if (transformerInstalled)
            return true;

        // Best-effort: some JVMs only allow self-attach when this is set.
        try {
            System.setProperty("jdk.attach.allowAttachSelf", "true");
        } catch (Throwable ignored) {
        }

        String jar = ownJarPath();
        if (jar == null) {
            System.err.println("[sos-extended-boostables] Low-Positive recolor disabled: cannot locate own jar.");
            return false;
        }

        try {
            String pid = currentPid();
            // Reflection so the mod still loads on a JVM without the jdk.attach module.
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            Object vm = vmClass.getMethod("attach", String.class).invoke(null, pid);
            try {
                vmClass.getMethod("loadAgent", String.class).invoke(vm, jar);
            } finally {
                try {
                    vmClass.getMethod("detach").invoke(vm);
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable t) {
            // Not an error: the game's bundled JRE has no jdk.attach module (no
            // com.sun.tools.attach.VirtualMachine), so self-attach can't work here. Log to stdout
            // (not stderr) so this stays out of the game's ERROR LOG. The recolor stays inactive; it
            // can still be enabled by launching with the -javaagent arg below (premain path).
            System.out.println("[sos-extended-boostables] Low-Positive tooltip recolor inactive "
                    + "(JVM blocked self-attach; harmless). To enable it, add to the game's Java arguments:\n"
                    + "    -javaagent:\"" + jar + "\"\n"
                    + "Reason: " + t);
            return false;
        }

        if (!transformerInstalled)
            System.err.println("[sos-extended-boostables] self-attach completed but transformer did not install.");
        return transformerInstalled;
    }

    private static String currentPid() {
        // ManagementFactory works on Java 8+ ("<pid>@<host>").
        String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        int at = name.indexOf('@');
        return at > 0 ? name.substring(0, at) : name;
    }

    private static String ownJarPath() {
        try {
            java.net.URL loc = ColorAgent.class.getProtectionDomain().getCodeSource().getLocation();
            if (loc == null)
                return null;
            File f = new File(loc.toURI());
            return f.isFile() ? f.getAbsolutePath() : null; // must be a jar, not an exploded dir
        } catch (Throwable t) {
            return null;
        }
    }

    // ---- bytecode transform -------------------------------------------------

    private static final class Transformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain pd, byte[] classfileBuffer) {
            if (className == null)
                return null;
            String cn = className.replace('/', '.');
            if (!(BOOSTER_ABS.equals(cn) || BOOST_SPECS.equals(cn) || BOOSTABLE.equals(cn) || INFO_BOO.equals(cn)))
                return null;

            CtClass cc = null;
            try {
                ClassPool pool = new ClassPool(true);
                if (loader != null)
                    pool.appendClassPath(new LoaderClassPath(loader));
                pool.appendClassPath(new LoaderClassPath(ColorAgent.class.getClassLoader()));

                cc = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
                if (cc.isFrozen())
                    cc.defrost();

                boolean changed;
                if (BOOSTER_ABS.equals(cn))
                    changed = patchBoosterAbs(cc);
                else if (BOOST_SPECS.equals(cn))
                    changed = patchBoostSpecs(cc);
                else if (BOOSTABLE.equals(cn))
                    changed = patchBoostable(cc);
                else
                    changed = patchInfoBoo(cc);

                if (!changed)
                    return null;

                byte[] out = cc.toBytecode();
                System.out.println("[sos-extended-boostables] recolor patch applied to " + cn);
                return out;
            } catch (Throwable t) {
                System.err.println("[sos-extended-boostables] recolor patch failed for " + cn + " (left as vanilla): " + t);
                return null; // null => keep the class unchanged
            } finally {
                if (cc != null)
                    cc.detach();
            }
        }
    }

    /** Route IGOOD/IBAD reads through {@link LowPositiveColors#pick}; key comes from the caller's push. */
    private static boolean patchBoosterAbs(CtClass cc) throws Exception {
        CtMethod m = findMethod(cc, "hover", true,
                "snake2d.util.gui.GUI_BOX", "double", "int", "snake2d.util.sprite.SPRITE", "boolean", "java.lang.CharSequence");
        if (m == null)
            return false;
        wrapColorReads(m);
        return true;
    }

    /** BoostSpecs.hover(GBox, BoostSpec, double, int): has the boostable directly, so push + recolor here. */
    private static boolean patchBoostSpecs(CtClass cc) throws Exception {
        CtMethod m = findMethod(cc, "hover", false,
                "util.gui.misc.GBox", "game.boosting.BoostSpec", "double", "int");
        if (m == null)
            return false;
        m.insertBefore(HELPER + ".pushKey($2.boostable.key);");
        wrapColorReads(m);
        m.insertAfter(HELPER + ".popKey();", true); // asFinally
        return true;
    }

    /** Boostable.hover/hoverDetailed render one boostable's factors via BoosterAbs.hover; push this.key. */
    private static boolean patchBoostable(CtClass cc) throws Exception {
        boolean any = false;
        for (String name : new String[] {"hover", "hoverDetailed"}) {
            CtMethod m = findMethod(cc, name, false,
                    "snake2d.util.gui.GUI_BOX", "game.boosting.BOOSTABLE_O", "java.lang.CharSequence", "boolean");
            if (m == null)
                continue;
            m.insertBefore(HELPER + ".pushKey($0.key);");
            m.insertAfter(HELPER + ".popKey();", true);
            any = true;
        }
        return any;
    }

    /** Tech "Boosts" browser: a Boo tooltip is about one boostable (its {@code bo} field); push bo.key. */
    private static boolean patchInfoBoo(CtClass cc) throws Exception {
        CtMethod m = findMethod(cc, "hoverInfoGet", false, "snake2d.util.gui.GUI_BOX");
        if (m == null)
            return false;
        m.insertBefore(HELPER + ".pushKey($0.bo.key);");
        m.insertAfter(HELPER + ".popKey();", true);
        return true;
    }

    /** Rewrites every read of GCOLOR_TEXT.IGOOD/.IBAD in {@code m} to go through the helper. */
    private static void wrapColorReads(CtMethod m) throws Exception {
        m.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws javassist.CannotCompileException {
                if (!f.isReader() || !GCOLOR_TEXT.equals(f.getClassName()))
                    return;
                if ("IGOOD".equals(f.getFieldName()))
                    f.replace("$_ = " + HELPER + ".pick($proceed(), true);");
                else if ("IBAD".equals(f.getFieldName()))
                    f.replace("$_ = " + HELPER + ".pick($proceed(), false);");
            }
        });
    }

    /** Finds a declared method by name, static-ness and exact parameter type names, else null. */
    private static CtMethod findMethod(CtClass cc, String name, boolean wantStatic, String... paramTypeNames) {
        for (CtMethod m : cc.getDeclaredMethods()) {
            if (!m.getName().equals(name))
                continue;
            if (wantStatic != Modifier.isStatic(m.getModifiers()))
                continue;
            try {
                CtClass[] ps = m.getParameterTypes();
                if (ps.length != paramTypeNames.length)
                    continue;
                boolean ok = true;
                for (int i = 0; i < ps.length; i++) {
                    if (!ps[i].getName().equals(paramTypeNames[i])) {
                        ok = false;
                        break;
                    }
                }
                if (ok)
                    return m;
            } catch (NotFoundException e) {
                // a parameter type could not be resolved; skip this overload
            }
        }
        return null;
    }
}
