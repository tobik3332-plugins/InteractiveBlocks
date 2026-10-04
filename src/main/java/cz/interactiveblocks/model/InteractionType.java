package cz.interactiveblocks.model;

public enum InteractionType {
    LEFT_CLICK,
    RIGHT_CLICK,
    CLICK;

    public static InteractionType fromString(String text) {
        for (InteractionType type : values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
