package your.mod.vassal;

import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.superb.SuperBoostable;
import game.boosting.superb.SuperSpec;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.royalty.Royalty;

/**
 * Adds the value of the {@code CIVIC_VASSAL_OPINION} boostable to the opinion of every royalty whose
 * faction is currently the player's vassal, and to nobody else.
 *
 * <p>Why this shape (full evidence: {@code SPEC_VASSAL_OPINION.md} §2 in the sos-scripting-template
 * control room):
 *
 * <ul>
 * <li><b>It has to be a {@code SuperSpec<Royalty>}, not a plain {@code Booster}.</b> Opinion is read
 *     through a parallel {@code SuperBoostable<Royalty>} pipeline
 *     ({@code ROPINION.get(f) -> get(roy) -> BOOST().get(roy) -> BUtil.value(all, roy, 1.5, 1, -100)}).
 *     Boosters hung on the plain {@code CIVIC_OPINION} boostable are pulled into that pipeline by
 *     {@code SuperSpec.Wrap}, whose {@code get(T o)} <i>discards</i> the royalty and evaluates at player
 *     scope ({@code SuperSpec.java:174-175}) — so there is no target to filter on and the existing
 *     {@code your.mod.targetfilter} pattern cannot be reused. Only a {@code SuperSpec<Royalty>} is
 *     handed the royalty being asked about.</li>
 * <li><b>Extends {@code SuperSpec}, never {@code SuperSpecImp}.</b> {@code Imp}'s constructor does
 *     {@code index = self.ups.add(this)} ({@code SuperSpec.java:71}), and {@code ups} is what
 *     {@code SuperBoostable}'s {@code Savable("BOOST_" + key)} persists ({@code SuperBoostable.java:34}) and
 *     what {@code SuperData} sizes its arrays from. Adding an {@code Imp} would shift indices and break
 *     saves made at a different mod version. A plain {@code SuperSpec} is purely derived and holds no
 *     state — <b>nothing in this feature is serialized.</b></li>
 * <li><b>Overrides {@code get}, not {@code pget}.</b> {@code BoosterAbs.get} routes through
 *     {@code SuperSpec.getValue}, which clamps its input to {@code [0,1]} and maps it onto
 *     {@code from -> to}; feeding a raw boostable value through {@code pget} would silently saturate
 *     anything above 1. {@code SuperSpec.Wrap} sidesteps it exactly the same way.</li>
 * </ul>
 *
 * <p>The constructor <b>self-registers</b> ({@code self.all.add(this)}, {@code SuperSpec.java:32}) —
 * constructing the object installs it; there is no separate add call. Because
 * {@code SuperBoostables} is rebuilt in the {@code GAME} constructor ({@code GAME.java:139}), the
 * install must happen per game, from {@code createInstance()} — see {@code MainScript}.
 *
 * <p>Gameplay note: this does not stop a vassal <i>leaving</i> the stance — no opinion threshold does
 * ({@code Stance.process} covers TRADE/PACT/ALLY only). It keeps their <b>trust</b> up: opinion feeds
 * trust ~1:1 ({@code RTrust.java:37-43}), vassals carry a hardcoded ×0.5 trust penalty
 * ({@code RTrust.java:96-106}), and {@code trust >= 1} is what stops a faction moving to war
 * ({@code DipWarPlayer.java:203}).
 */
public final class VassalOpinionSpec extends SuperSpec<Royalty> {

    /**
     * Hover-range display only ({@code from()}/{@code to()} feed the "(0 &lt;-&gt; 10)" span and
     * {@code BUtil.min|max}). {@link #get(Royalty)} bypasses {@code getValue}, so this does not cap the
     * actual bonus.
     */
    private static final double UI_MAX = 10.0;

    private static final CharSequence DESC =
            "Opinion granted because this faction is your vassal.";

    private final Boostable key;

    public VassalOpinionSpec(SuperBoostable<Royalty> target, Boostable key, BSourceInfo info) {
        super(target, info, DESC, 0.0, UI_MAX, false); // from, to, isMul=false (additive opinion points)
        this.key = key;
    }

    @Override
    public double get(Royalty roy) {
        if (roy == null || roy.court == null) return 0;
        if (DIP.overlord(roy.court.faction) != FACTIONS.player()) return 0;
        // Player-scope read: Faction implements BOOSTABLE_O (Faction.java:27). Base value is 0, so this
        // is exactly 0 until a tech/race grants CIVIC_VASSAL_OPINION>ADD.
        return key.get(FACTIONS.player());
    }

    @Override
    protected double pget(Royalty roy) {
        return 0; // unused — get() is overridden above
    }

    @Override
    public double secondsRemaining(Royalty roy) {
        return 0; // permanent while the vassal stance holds
    }

    @Override
    public double increase(Royalty roy) {
        return 0; // not a decaying/growing spec
    }
}
