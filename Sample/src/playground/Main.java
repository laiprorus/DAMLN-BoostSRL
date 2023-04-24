package playground;

public class Main {
    public static void main(String[] args) {
        ToolBox toolBox = new ToolBox<>();
        toolBox.addTool(new Tool("Hammer 1", 2.0));
        toolBox.addTool(new Tool("Hammer 2", 2.0));
        toolBox.addTool(new Tool("Hammer 3", 2.0));
        toolBox.addTool(new Tool("Saw 1", 2.0));
        toolBox.addTool(new Tool("Saw 2", 2.0));
        toolBox.addTool(new Tool("Saw 3", 2.0));
        toolBox.printTools();
    }
}
