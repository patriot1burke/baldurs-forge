package org.baldurs.forge.builder;

public enum WeaponType {
    Battleaxe("WPN_Battleaxe"),
    Club("WPN_Club"),
    Dagger("WPN_Dagger"),
    Dart("WPN_Dart", true),
    Flail("WPN_Flail"),
    Glaive("WPN_Glaive"),
    Greataxe("WPN_Greataxe"),
    Greatclub("WPN_Greatclub"),
    Greatsword("WPN_Greatsword"),
    Halberd("WPN_Halberd"),
    Handaxe("WPN_Handaxe"),
    HandCrossbow("WPN_HandCrossbow", true),
    HeavyCrossbow("WPN_HeavyCrossbow", true),
    Javelin("WPN_Javelin", true),
    LightCrossbow("WPN_LightCrossbow", true),
    LightHammer("WPN_LightHammer"),
    Longbow("WPN_Longbow", true),
    Longsword("WPN_Longsword"),
    Mace("WPN_Mace"),
    Maul("WPN_Maul"),
    Morningstar("WPN_Morningstar"),
    Pike("WPN_Pike"),
    Quarterstaff("WPN_Quarterstaff"),
    Rapier("WPN_Rapier"),
    Scimitar("WPN_Scimitar"),
    Shortsword("WPN_Shortsword"),
    Shorbow("WPN_Shorbow", true),
    Sickle("WPN_Sickle"),
    Sling("WPN_Sling", true),
    Spear("WPN_Spear"),
    Trident("WPN_Trident"),
    Warhammer("WPN_Warhammer"),
    Warpick("WPN_Warpick")
    ;
    String baseStat;
    boolean ranged;

    WeaponType(String baseStat, boolean ranged) {
        this.baseStat = baseStat;
        this.ranged = ranged;
    }
    WeaponType(String baseStat) {
        this.baseStat = baseStat;
    }

    public String getBaseStat() {
        return baseStat;
    }
    public boolean isRanged() {
        return ranged;
    }

}
