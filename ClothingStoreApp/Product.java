public class Product {
    private String ID;
    private String category;
    private String name;
    private double price;
    private int quantity;

    //constructors
    public Product(String ID, String category, String name, double price, int quantity) {
        this.ID = ID;
        this.category = category;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Product() {
        this.ID = null;
        this.category = null;
        this.name = null;
        this.price = 0.0;
        this.quantity = 0;
    }

    public void delete() {
        this.ID = null;
        this.category = null;
        this.name = null;
        this.price = 0.0;
        this.quantity = 0;
    }


    //getters and setters
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    //toString, equals, hashCode
    @Override
    public String toString() {
        return "Product{" +
                "ID='" + ID + '\'' +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Product product = (Product) obj;

        if (Double.compare(product.price, price) != 0) return false;
        if (quantity != product.quantity) return false;
        if (ID != null ? !ID.equals(product.ID) : product.ID != null) return false;
        if (category != null ? !category.equals(product.category) : product.category != null) return false;
        return name != null ? name.equals(product.name) : product.name == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = ID != null ? ID.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + quantity;
        return result;
    }
}