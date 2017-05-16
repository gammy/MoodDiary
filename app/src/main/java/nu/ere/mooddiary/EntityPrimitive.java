package nu.ere.mooddiary;

public class EntityPrimitive {
    public int id;
    public String name;
    public int isNumber;
    public int enabled;

    public EntityPrimitive(int id, String name, int isNumber, int enabled) {
        this.id = id;
        this.isNumber = isNumber;
        this.name = name;
        this.enabled = enabled;
    }
}

