package cz.interactiveblocks.model;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class InteractiveBlock {

    private final String name;
    private Material material;
    private String world;
    private int x;
    private int y;
    private int z;
    private double cooldown;
    private InteractionType interactionType;
    private String permission;
    private List<String> commands;
    private List<String> messages;

    public InteractiveBlock(String name, Material material, String world, int x, int y, int z, double cooldown, InteractionType interactionType, String permission, List<String> commands, List<String> messages) {
        this.name = name;
        this.material = material;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.cooldown = cooldown;
        this.interactionType = interactionType;
        this.permission = permission;
        this.commands = commands != null ? commands : new ArrayList<>();
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public double getCooldown() { return cooldown; }
    public void setCooldown(double cooldown) { this.cooldown = cooldown; }
    public InteractionType getInteractionType() { return interactionType; }
    public void setInteractionType(InteractionType interactionType) { this.interactionType = interactionType; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public List<String> getCommands() { return commands; }
    public List<String> getMessages() { return messages; }
}
