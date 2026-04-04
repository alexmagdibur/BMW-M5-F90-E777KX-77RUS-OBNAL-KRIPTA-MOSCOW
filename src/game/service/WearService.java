package service;

import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import domain.Engineer;
import domain.Track;
import util.RandomUtil;

import java.util.Map;

/**
 * Manages component wear after races and repair logic.
 *
 * Wear per race = (trackLength / 1000) * typeMultiplier * randomFactor
 *   trackLength=4000m → base=4
 *   TIRES get ~16% per race, CHASSIS ~3%, others in between.
 *
 * Repair:
 *   Amount = min(60, 20 + qualification/2)  →  skill 1 ≈ 20%, skill 100 ≈ 60%
 *   Cost   = max(10_000, 30_000 - qualification*200) → skill 100 = 10k, skill 1 ≈ 30k
 */
public class WearService {

    private static final Map<ComponentType, Double> WEAR_MULTIPLIER = Map.of(
        ComponentType.TIRES,        4.0,
        ComponentType.SUSPENSION,   2.5,
        ComponentType.ENGINE,       2.0,
        ComponentType.TRANSMISSION, 1.5,
        ComponentType.EXTRA,        1.5,
        ComponentType.AERO_PACKAGE, 1.0,
        ComponentType.CHASSIS,      0.75
    );

    /**
     * Applies post-race wear to all components installed in the bolid.
     * Wear is proportional to track length and component type.
     */
    public static void applyWear(Bolid bolid, Track track) {
        double base = track.getTotalLength() / 1000.0;
        for (Component c : bolid.getAllComponents()) {
            if (c.getType() == ComponentType.EXTRA) continue;
            double mult = WEAR_MULTIPLIER.getOrDefault(c.getType(), 1.0);
            double rawWear = base * mult * RandomUtil.nextDouble(0.75, 1.25);
            c.applyWear((int) Math.round(rawWear));
        }
    }

    /**
     * How many percent of wear the engineer can remove in one repair session.
     */
    public static int repairAmount(Engineer engineer) {
        return Math.min(60, 20 + engineer.getQualification() / 2);
    }

    /**
     * Cost in rubles for one repair operation (one component).
     */
    public static long repairCost(Engineer engineer) {
        return Math.max(10_000L, 30_000L - (long) engineer.getQualification() * 200);
    }
}
