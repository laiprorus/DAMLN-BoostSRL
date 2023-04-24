package playground;

public class Tool implements Comparable<Tool> {
    public String name;
    public Double price;

    public Tool(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public int compareTo(Tool tool) {
        return this.name.compareTo(tool.name);
    }
}
