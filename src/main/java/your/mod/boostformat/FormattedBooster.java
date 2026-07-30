package your.mod.boostformat;

import game.boosting.BOOSTABLE_O;
import game.boosting.Booster;
import snake2d.util.color.COLOR;
import util.colors.GCOLOR;
import util.gui.misc.GText;

/**
 * A faithful display-only wrapper around another {@link Booster} that changes nothing except the
 * <b>colour</b> of the value rendered in a tech node's effect list.
 *
 * <h2>Why this works</h2>
 * The tech-node tooltip ({@code view.ui.tech.Node.hoverInfoGet}) does not use the hardcoded
 * {@code BoosterAbs.hover} colouring — it renders each effect with
 * {@code bb.booster.format(b.text(), v)}. {@code BoosterAbs.format} is a <b>public, non-final instance
 * method</b> and the call is virtual on the booster object, so whoever owns the {@link Booster} in
 * {@code tech.boosters} owns that line's colour. {@code format} itself sets no colour, but it delegates
 * to {@code GFORMAT.f1} (multiplicative) / {@code GFORMAT.iIncr}/{@code f0} (additive), and those do:
 * {@code >1 → IGREAT, <1 → IWORST, ==1 → WHITE85} (and {@code >0 → IGOOD, <0 → IBAD, 0 → WHITE85}).
 *
 * <p>{@link util.gui.misc.GText#color(COLOR)} stores <em>one</em> colour for the whole text object
 * (bound at render time), so we let the engine format the number exactly as vanilla does and then
 * simply re-set the colour — the last call wins. The displayed number is never altered.
 *
 * <h2>Why it is safe</h2>
 * A {@code TECH}'s {@code BoostSpecs} is constructed with {@code connect == false}
 * ({@code init/tech/TECH.java:122}), so {@code push}/{@code remove} on it never touch the target
 * {@link game.boosting.Boostable}'s factor list — the real effect is held by the ORIGINAL booster
 * object, which {@code Boostable.addFactor} stored directly. Swapping specs inside
 * {@code tech.boosters} is therefore a pure display-list operation with no gameplay consequence.
 * Every value-bearing method here still forwards to the wrapped booster anyway, so even a caller that
 * does read through this wrapper gets identical numbers.
 */
public final class FormattedBooster extends Booster {

    /** Colour policy for a wrapped line. */
    public enum Mode {
        /** Always render the value in the engine's neutral colour, whatever the value is. */
        NEUTRAL,
        /** Swap the good/bad colours — for boostables where a LOWER value is the better outcome. */
        INVERTED,
    }

    private final Booster inner;
    private final Mode mode;

    public FormattedBooster(Booster inner, Mode mode) {
        super(inner.info, inner.isMul);
        this.inner = inner;
        this.mode = mode;
    }

    /** The booster this one wraps (used to keep the swap idempotent across repeated passes). */
    public Booster inner() {
        return inner;
    }

    // ---- faithful forwarding -------------------------------------------------
    // Note get(..) is overridden directly rather than pget(..): pget is protected in game.boosting,
    // so it cannot be invoked on another instance from this package. Overriding the public get(..)
    // forwards the whole computation and leaves pget unreachable.

    @Override
    public double from() {
        return inner.from();
    }

    @Override
    public double to() {
        return inner.to();
    }

    @Override
    public double getValue(double input) {
        return inner.getValue(input);
    }

    @Override
    public double get(BOOSTABLE_O o) {
        return inner.get(o);
    }

    @Override
    protected double pget(BOOSTABLE_O o) {
        return 0; // unreachable: get(..) above never calls it
    }

    // ---- the one thing we actually change ------------------------------------

    @Override
    public GText format(GText t, double value) {
        GText r = super.format(t, value); // vanilla number formatting (and vanilla colour)
        COLOR c = colorFor(value);
        if (c != null)
            r.color(c); // one colour per GText -> this overrides what GFORMAT just set
        return r;
    }

    private COLOR colorFor(double value) {
        if (mode == Mode.NEUTRAL)
            return COLOR.WHITE85; // the same neutral the engine uses for a no-op (x1.0 / +0) line
        double neutral = isMul ? 1.0 : 0.0;
        if (value < neutral)
            return GCOLOR.T().IGOOD; // lower is better -> green
        if (value > neutral)
            return GCOLOR.T().IBAD;  // higher is worse -> red
        return COLOR.WHITE85;
    }
}
