package domain;

public enum PlayerChoice {
    OVERTAKE, // обогнать ближайшего соперника впереди
    MELEE_ATTACK, // атаковать ближним оружием (только сосед)
    RANGED_ATTACK, // атаковать дальним оружием (любой)
    PASS // пропустить ход
}
