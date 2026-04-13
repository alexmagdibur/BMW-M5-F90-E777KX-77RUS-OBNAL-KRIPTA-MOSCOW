package service;

import domain.Bolid;
import domain.PlayerChoice;
import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import domain.WeaponType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SurvivalRaceService {

    private static Random RANDOM = new Random();

    // только для тестов
    public static void setRandom(Random random) { RANDOM = random; }

    private static final String[] BOT_NAMES = {
        "Джей Слик", "Борис Шумахер", "Макс Ударников", "Алёна Дрифт",
        "Виктор Газ", "Игорь Апекс", "Дарья Форсаж", "Паша Турбо"
    };

    // создание гонки

    public SurvivalRaceState createRace(Bolid bolid, int trackLength) {
        int playerPerf = bolid.getPerformanceScore();
        int meleeLevel = bolid.getWeapons().containsKey(WeaponType.MELEE)
                ? bolid.getWeapons().get(WeaponType.MELEE).getLevel() : 0;
        int rangedLevel = bolid.getWeapons().containsKey(WeaponType.RANGED)
                ? bolid.getWeapons().get(WeaponType.RANGED).getLevel() : 0;

        SurvivalParticipant player = new SurvivalParticipant(
            "Вы", true, playerPerf, meleeLevel, rangedLevel);

        List<SurvivalParticipant> participants = new ArrayList<>();
        participants.add(player);

        List<String> names = new ArrayList<>(List.of(BOT_NAMES));
        Collections.shuffle(names);
        for (int i = 0; i < 5; i++) {
            int perf = 200 + RANDOM.nextInt(301); // 200–500
            int botMelee = RANDOM.nextInt(3);
            int botRanged = RANDOM.nextInt(3);
            participants.add(new SurvivalParticipant(names.get(i), false, perf, botMelee, botRanged));
        }

        Collections.shuffle(participants); // случайный стартовый порядок
        return new SurvivalRaceState(participants, Math.max(5, trackLength / 1000));
    }

    // обгон

    // перемещает атакующего на одну позицию вперёд; возвращает true если удалось
    public boolean tryOvertake(SurvivalRaceState state, SurvivalParticipant attacker) {
        List<SurvivalParticipant> active = state.getActiveParticipants();
        int idx = active.indexOf(attacker);
        if (idx <= 0) return false; // уже первый

        if (RANDOM.nextDouble() < overtakeChance(attacker.getPerformanceScore(), attacker.isPlayer())) {
            SurvivalParticipant ahead = active.get(idx - 1);
            List<SurvivalParticipant> order = state.getOrder();
            Collections.swap(order, order.indexOf(attacker), order.indexOf(ahead));
            return true;
        }
        return false;
    }

    // атака

    // возвращает true если попадание (target помечается eliminated)
    public boolean tryAttack(SurvivalRaceState state, SurvivalParticipant attacker,
                             SurvivalParticipant target, int weaponLevel) {
        if (weaponLevel <= 0 || target.isEliminated()) return false;
        if (RANDOM.nextDouble() < attackChance(weaponLevel)) {
            target.eliminate();
            return true;
        }
        return false;
    }

    // выбор действия игрока

    // одно действие за ход: обгон и атака взаимоисключающи
    public boolean applyPlayerChoice(SurvivalRaceState state, SurvivalParticipant player,
                                      PlayerChoice choice, SurvivalParticipant target) {
        return switch (choice) {
            case OVERTAKE -> tryOvertake(state, player);
            case MELEE_ATTACK -> target != null && tryAttack(state, player,
                    target, player.getMeleeWeaponLevel());
            case RANGED_ATTACK -> target != null && tryAttack(state, player,
                    target, player.getRangedWeaponLevel());
            case PASS -> false;
        };
    }

    // ходы ботов

    // обрабатывает все боты за один шаг; возвращает список событий для вывода
    public List<String> processBotActions(SurvivalRaceState state) {
        List<String> events = new ArrayList<>();

        // копия — боты могут выбывать прямо в процессе итерации
        for (SurvivalParticipant bot : new ArrayList<>(state.getActiveParticipants())) {
            if (bot.isPlayer() || bot.isEliminated()) continue;
            if (RANDOM.nextDouble() >= 0.35) continue; // 35% шанс что-то делать

            if (!state.getActiveParticipants().contains(bot)) continue;

            if (bot.hasWeapon() && RANDOM.nextBoolean()) {
                SurvivalParticipant target = pickBotTarget(state, bot);
                if (target != null) {
                    int wLevel = Math.max(bot.getMeleeWeaponLevel(), bot.getRangedWeaponLevel());
                    boolean hit = tryAttack(state, bot, target, wLevel);
                    if (hit) {
                        String suffix = target.isPlayer() ? " (это вы!)" : "";
                        events.add(String.format("  [БОТ] %s атаковал %s — ПОПАДАНИЕ!%s %s выбывает.",
                            bot.getName(), target.getName(), suffix, target.getName()));
                    } else {
                        events.add(String.format("  [БОТ] %s атаковал %s — промах.",
                            bot.getName(), target.getName()));
                    }
                }
            } else {
                int before = state.getActivePosition(bot);
                if (tryOvertake(state, bot)) {
                    events.add(String.format("  [БОТ] %s совершил обгон! (%d → %d)",
                        bot.getName(), before, state.getActivePosition(bot)));
                }
            }
        }
        return events;
    }

    // вспомогательные

    // melee=true — только соседи; melee=false — все кроме атакующего
    public List<SurvivalParticipant> getValidTargets(
            SurvivalRaceState state, SurvivalParticipant attacker, boolean melee) {
        List<SurvivalParticipant> active = state.getActiveParticipants();
        int idx = active.indexOf(attacker);
        List<SurvivalParticipant> targets = new ArrayList<>();
        if (melee) {
            if (idx > 0) targets.add(active.get(idx - 1));
            if (idx < active.size()-1) targets.add(active.get(idx + 1));
        } else {
            for (SurvivalParticipant p : active) {
                if (p != attacker) targets.add(p);
            }
        }
        return targets;
    }

    private SurvivalParticipant pickBotTarget(SurvivalRaceState state, SurvivalParticipant bot) {
        boolean ranged = bot.getRangedWeaponLevel() > 0;
        List<SurvivalParticipant> targets = getValidTargets(state, bot, !ranged);
        return targets.isEmpty() ? null : targets.get(RANDOM.nextInt(targets.size()));
    }

    private double overtakeChance(int perf, boolean isPlayer) {
        double chance = 0.3 + perf / 600.0; // чем выше перф, тем лучше шанс
        if (!isPlayer) chance -= 0.10; // боты чуть слабее
        return Math.max(0.15, Math.min(0.75, chance));
    }

    private double attackChance(int level) {
        return switch (level) {
            case 1 -> 0.40;
            case 2 -> 0.65;
            case 3 -> 0.85;
            default -> 0.0;
        };
    }

    // для отображения шансов в UI
    public int overtakeChancePct(int perf) { return (int) Math.round(overtakeChance(perf, true) * 100); }
    public int attackChancePct(int level) { return (int) Math.round(attackChance(level) * 100); }
}
