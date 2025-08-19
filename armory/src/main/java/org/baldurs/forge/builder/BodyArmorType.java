package org.baldurs.forge.builder;

public enum BodyArmorType {
    BreastPlate(ArmorCategory.Medium, "ARM_Breastplate_Body"),
    ChainMail(ArmorCategory.Heavy, "ARM_ChainMail_Body"),
    ChainShirt(ArmorCategory.Medium, "ARM_ChainShirt_Body"),
    HalfPlate(ArmorCategory.Medium, "ARM_HalfPlate_Body"),
    Hide(ArmorCategory.Medium, "ARM_Hide_Body"),
    Leather(ArmorCategory.Light, "ARM_Leather_Body"),
    None(ArmorCategory.None, "ARM_Robe_Body"),
    Padded(ArmorCategory.Light, "ARM_Padded_Body"),
    Plate(ArmorCategory.Heavy, "ARM_Plate_Body"),
    RingMail(ArmorCategory.Heavy, "ARM_RingMail_Body"),
    ScaleMail(ArmorCategory.Medium, "ARM_ScaleMail_Body"),
    Splint(ArmorCategory.Heavy, "ARM_Splint_Body"),
    StuddedLeather(ArmorCategory.Light, "ARM_StuddedLeather_Body")

    ;
    final private ArmorCategory category;
    final String baseStat;

    BodyArmorType(ArmorCategory category, String baseStat) {
        this.category = category;
        this.baseStat = baseStat;
    }
    public ArmorCategory getCategory() {
        return category;
    }
    public String getBaseStat() {
        return baseStat;
    }

}
