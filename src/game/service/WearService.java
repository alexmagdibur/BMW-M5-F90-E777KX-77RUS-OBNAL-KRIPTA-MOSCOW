package service;

import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import domain.Engineer;
import domain.Track;
import util.RandomUtil;

import java.util.Map;

public class WearService {

    private static final Map<ComponentType, Double> WEAR_MULTIPLIER = Map.of(
        ComponentType.TIRES, 4.0,
        ComponentType.SUSPENSION, 2.5,
        ComponentType.ENGINE, 2.0,
        ComponentType.TRANSMISSION, 1.5,
        ComponentType.EXTRA, 1.5,
        ComponentType.AERO_PACKAGE, 1.0,
        ComponentType.CHASSIS, 0.75
    );


    public static void applyWear(Bolid bolid, Track track) {
        double base = track.getTotalLength() / 1000.0;
        for (Component c : bolid.getAllComponents()) {
            if (c.getType() == ComponentType.EXTRA) continue;
            double mult = WEAR_MULTIPLIER.getOrDefault(c.getType(), 1.0);
            double rawWear = base * mult * RandomUtil.nextDouble(0.75, 1.25);
            c.applyWear((int) Math.round(rawWear));
        }
    }


    public static int repairAmount(Engineer engineer) {
        return Math.min(60, 20 + engineer.getQualification() / 2);
    }


    public static long repairCost(Engineer engineer) {
        return Math.max(10_000L, 30_000L - (long) engineer.getQualification() * 200);
    }
}
