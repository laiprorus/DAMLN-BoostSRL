package playground;

import java.util.Set;
import java.util.TreeSet;

public class ToolBox<T extends Tool> {
    private Set<T> tools;

    public ToolBox() {
        this.tools = new TreeSet<>();
    }

    public void addTool(T tool) {
        this.tools.add(tool);
    }

    public void printTools() {
        System.out.println("ToolBox contents:");
        for (T tool : this.tools) {
            this.printTool(tool);
        }
    }

    private void printTool(T tool) {
        System.out.println("Tool 1: " + tool.name + ", price: " + tool.price + " EUR.");
    }
}
