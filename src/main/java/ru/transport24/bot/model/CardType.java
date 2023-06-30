package ru.transport24.bot.model;

// Тип банковской карты.
public enum CardType {
    BANKING("Банковская карта"),
    TRANSPORT("Транспортная карта"),
    SOCIAL("Социальная карта");

    // Получить название типа карты на русском
    private final String inRussian;
    CardType(String inRussian) {
        this.inRussian = inRussian;
    }
    public String getInRussian() {
        return inRussian;
    }
}