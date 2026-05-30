package your.mod;

import game.boosting.*;
import game.battle.div.Div;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVES;
import init.type.HCLASS_RACE;
import init.type.HTYPES;
import script.SCRIPT;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import world.map.regions.Region;

import java.io.IOException;
import java.io.Serializable;

@SuppressWarnings("unused")
public final class MainScript implements SCRIPT {

    private static final String SLAVER_KEY = "ROOM__SLAVER";
    private static final String CANNIBAL_KEY = "ROOM__CANNIBAL";

    private double processedRatio = 0.0;

    public MainScript() {}

    @Override
    public CharSequence name() {
        return "sos-extended-boostables";
    }

    @Override
    public CharSequence desc() {
        return "Adds extended boostable keys for room types not covered by vanilla.";
    }

    @Override
    public boolean forceInit() {
        return true;
    }

    @Override
    public void initBeforeGameInited() {
        Boostable roomSlaver = ensureBoostable(SLAVER_KEY, "__SLAVER", "Slaver",
                "The effectiveness of your Slaver room. Higher values increase the submission of slaves processed through it.",
                UI.icons().s.slave);
        if (roomSlaver != null) {
            registerSlaverEffect(roomSlaver);
        }

        Boostable roomCannibal = ensureBoostable(CANNIBAL_KEY, "__CANNIBAL", "Cannibal",
                "Multiplies the amount of resources gained when a corpse is butchered at a Cannibal Room.",
                SETT.ROOMS().CANNIBAL.icon);
        if (roomCannibal != null) {
            registerCannibalEffect(roomCannibal);
        }
    }

    private static Boostable ensureBoostable(String fullKey, String pushKey, String name, String desc, SPRITE icon) {
        if (BOOSTING.MAP().tryGet(fullKey) == null) {
            BOOSTING.push(pushKey, 1.0, name, desc, icon, BOOSTABLES.ROOMS());
        }
        Boostable b = BOOSTING.MAP().tryGet(fullKey);
        if (b == null) {
            System.err.println("[sos-extended-boostables] Could not register boostable: " + fullKey);
        }
        return b;
    }

    private void registerSlaverEffect(Boostable roomSlaver) {
        Boostable submission = BOOSTABLES.BEHAVIOUR().SUBMISSION;
        BSourceInfo info = new BSourceInfo("Slaver Training", UI.icons().s.slave);

        BValue bv = new BValue() {
            @Override
            public double vGet(Induvidual indu) {
                if (indu.hType() != HTYPES.SLAVE()) return 0;
                if (STATS.POP().COUNT.arrive.get(indu) == CAUSE_ARRIVES.PAROLE())
                    return roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue;
                return 0;
            }

            @Override
            public double vGet(Player f) {
                return (roomSlaver.get(f) - roomSlaver.baseValue) * processedRatio;
            }

            // v71: BValue.PopTime was removed and replaced by the per-population-class
            // query vGet(HCLASS_RACE). Mirror the old PopTime aggregate value here.
            @Override
            public double vGet(HCLASS_RACE reg) {
                return (roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue) * processedRatio;
            }

            @Override public double vGet(FactionNPC f) { return 0; }
            @Override public double vGet(Region reg) { return 0; }
            @Override public double vGet(Div div) { return 0; }
        };

        new BoosterValue(bv, info, 2.0, false).add(submission);
    }

    private void registerCannibalEffect(Boostable roomCannibal) {
        int patched = 0;
        for (Race race : RACES.all()) {
            LIST<RES_AMOUNT> list = race.resources();
            if (!(list instanceof ArrayList)) continue;
            @SuppressWarnings("unchecked")
            ArrayList<RES_AMOUNT> al = (ArrayList<RES_AMOUNT>) list;
            for (int i = 0; i < al.size(); i++) {
                RES_AMOUNT a = al.get(i);
                if (a instanceof BoostedResAmount) continue;
                al.replace(i, new BoostedResAmount(a.resource(), a.amount(), roomCannibal));
                patched++;
            }
        }
        System.out.println("[sos-extended-boostables] Wrapped " + patched + " race RESOURCES entries with " + CANNIBAL_KEY + " multiplier.");
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new SCRIPT_INSTANCE() {
            private double timer = 0;

            @Override
            public void update(double ds) {
                timer -= ds;
                if (timer > 0) return;
                timer = 4.0;
                recomputeRatio();
            }

            @Override
            public void save(FilePutter file) {}

            @Override
            public void load(FileGetter file) throws IOException {
                recomputeRatio();
            }
        };
    }

    private void recomputeRatio() {
        int total = 0;
        int processed = 0;
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (!(e instanceof Humanoid)) continue;
            Humanoid h = (Humanoid) e;
            if (h.indu().hType() != HTYPES.SLAVE()) continue;
            total++;
            if (STATS.POP().COUNT.arrive.get(h.indu()) == CAUSE_ARRIVES.PAROLE())
                processed++;
        }
        processedRatio = total == 0 ? 0.0 : (double) processed / total;
    }

    private static final class BoostedResAmount implements RES_AMOUNT, Serializable {
        private static final long serialVersionUID = 1L;
        private final byte cIndex;
        private final int baseAmount;
        private final Boostable boost;

        BoostedResAmount(RESOURCE resource, int baseAmount, Boostable boost) {
            this.cIndex = resource.bIndex();
            this.baseAmount = baseAmount;
            this.boost = boost;
        }

        @Override
        public RESOURCE resource() {
            return RESOURCES.ALL().get(cIndex);
        }

        @Override
        public int amount() {
            double mult = boost.get(FACTIONS.player());
            if (mult <= 0) return 0;
            return (int) Math.round(baseAmount * mult);
        }
    }
}
