package your.mod;

import game.boosting.*;
import game.boosting.BValue.PopTime;
import game.battle.div.Div;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import script.SCRIPT;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.map.regions.Region;

import java.io.IOException;

@SuppressWarnings("unused")
public final class MainScript implements SCRIPT {

    private static final String BOOSTABLE_KEY = "ROOM__SLAVER";
    private double processedRatio = 0.0;

    public MainScript() {}

    @Override
    public CharSequence name() {
        return "sos-extended-boostables";
    }

    @Override
    public CharSequence desc() {
        return "Adds extended boostable support for the Slaver room.";
    }

    @Override
    public boolean forceInit() {
        return true;
    }

    @Override
    public void initBeforeGameInited() {
        if (BOOSTING.MAP().tryGet(BOOSTABLE_KEY) == null) {
            BOOSTING.push("__SLAVER", 1.0,
                    "Slaver",
                    "The effectiveness of your Slaver room. Higher values increase the submission of slaves processed through it.",
                    UI.icons().s.slave,
                    BOOSTABLES.ROOMS());
        }

        Boostable roomSlaver = BOOSTING.MAP().tryGet(BOOSTABLE_KEY);
        if (roomSlaver == null) {
            System.err.println("[sos-extended-boostables] Could not register boostable: " + BOOSTABLE_KEY);
            return;
        }

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

            @Override
            public double vGet(PopTime t) {
                return (roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue) * processedRatio;
            }

            @Override public double vGet(FactionNPC f) { return 0; }
            @Override public double vGet(Region reg) { return 0; }
            @Override public double vGet(Div div) { return 0; }
        };

        new BoosterValue(bv, info, 2.0, false).add(submission);
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
}
