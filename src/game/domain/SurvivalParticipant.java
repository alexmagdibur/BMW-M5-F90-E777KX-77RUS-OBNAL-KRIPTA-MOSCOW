package domain;

public class SurvivalParticipant {

    private final String  name;
    private final boolean isPlayer;
    private final int     performanceScore;
    private final int     meleeWeaponLevel;   // 0 = нет оружия
    private final int     rangedWeaponLevel;  // 0 = нет оружия
    private boolean       eliminated;

    public SurvivalParticipant(String name, boolean isPlayer, int performanceScore,
                                int meleeWeaponLevel, int rangedWeaponLevel) {
        this.name              = name;
        this.isPlayer          = isPlayer;
        this.performanceScore  = performanceScore;
        this.meleeWeaponLevel  = meleeWeaponLevel;
        this.rangedWeaponLevel = rangedWeaponLevel;
        this.eliminated        = false;
    }

    public String  getName()              { return name; }
    public boolean isPlayer()             { return isPlayer; }
    public int     getPerformanceScore()  { return performanceScore; }
    public int     getMeleeWeaponLevel()  { return meleeWeaponLevel; }
    public int     getRangedWeaponLevel() { return rangedWeaponLevel; }
    public boolean isEliminated()         { return eliminated; }
    public void    eliminate()            { this.eliminated = true; }

    public boolean hasWeapon() {
        return meleeWeaponLevel > 0 || rangedWeaponLevel > 0;
    }
}
