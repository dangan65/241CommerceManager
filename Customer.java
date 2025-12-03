import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Customer class represents a customer in the clothing store system.
 * Contains customer information, authentication, and shopping history.
 */
public class Customer {
    private String customerId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Address shippingAddress;
    private Address billingAddress;
    private LocalDateTime registrationDate;
    private boolean isActive;
    private CustomerType customerType;
    private double totalSpent;
    private List<String> orderHistory; // List of order IDs
    
    // Enum for customer types
    public enum CustomerType {
        REGULAR, PREMIUM, VIP
    }
    
    // Inner class for Address
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        
        public Address() {
            this("", "", "", "", "");
        }
        
        public Address(String street, String city, String state, String zipCode, String country) {
            this.street = street != null ? street : "";
            this.city = city != null ? city : "";
            this.state = state != null ? state : "";
            this.zipCode = zipCode != null ? zipCode : "";
            this.country = country != null ? country : "";
        }
        
        // Getters and setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street != null ? street : ""; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city != null ? city : ""; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state != null ? state : ""; }
        
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode != null ? zipCode : ""; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country != null ? country : ""; }
        
        public boolean isEmpty() {
            return street.isEmpty() && city.isEmpty() && state.isEmpty() && 
                   zipCode.isEmpty() && country.isEmpty();
        }
        
        @Override
        public String toString() {
            if (isEmpty()) return "No address provided";
            return String.format("%s, %s, %s %s, %s", street, city, state, zipCode, country);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Address address = (Address) obj;
            return Objects.equals(street, address.street) &&
                   Objects.equals(city, address.city) &&
                   Objects.equals(state, address.state) &&
                   Objects.equals(zipCode, address.zipCode) &&
                   Objects.equals(country, address.country);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(street, city, state, zipCode, country);
        }
    }
    
    // Constructors
    public Customer() {
        this("", "", "");
    }
    
    public Customer(String username, String password) {
        this(generateCustomerId(), username, password);
    }
    
    public Customer(String customerId, String username, String password) {
        this.customerId = customerId != null ? customerId : generateCustomerId();
        this.username = username != null ? username : "";
        this.password = password != null ? password : "";
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.phoneNumber = "";
        this.shippingAddress = new Address();
        this.billingAddress = new Address();
        this.registrationDate = LocalDateTime.now();
        this.isActive = true;
        this.customerType = CustomerType.REGULAR;
        this.totalSpent = 0.0;
        this.orderHistory = new ArrayList<>();
    }
    
    // Generate a unique customer ID
    private static String generateCustomerId() {
        return "CUST-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    // Authentication methods
    public boolean authenticate(String password) {
        return this.password != null && this.password.equals(password);
    }
    
    public void changePassword(String oldPassword, String newPassword) {
        if (authenticate(oldPassword) && newPassword != null && !newPassword.isEmpty()) {
            this.password = newPassword;
        } else {
            throw new IllegalArgumentException("Invalid old password or new password is empty");
        }
    }
    
    // Customer type management
    public void updateCustomerType() {
        if (totalSpent >= 1000.0) {
            customerType = CustomerType.VIP;
        } else if (totalSpent >= 500.0) {
            customerType = CustomerType.PREMIUM;
        } else {
            customerType = CustomerType.REGULAR;
        }
    }
    
    public double getDiscountRate() {
        switch (customerType) {
            case VIP: return 0.15; // 15% discount
            case PREMIUM: return 0.10; // 10% discount
            case REGULAR:
            default: return 0.0; // No discount
        }
    }
    
    // Order history management
    public void addOrderToHistory(String orderId, double orderTotal) {
        if (orderId != null && !orderId.isEmpty()) {
            orderHistory.add(orderId);
            totalSpent += orderTotal;
            updateCustomerType();
        }
    }
    
    public List<String> getOrderHistory() {
        return Collections.unmodifiableList(orderHistory);
    }
    
    public int getOrderCount() {
        return orderHistory.size();
    }
    
    // Profile validation
    public boolean isProfileComplete() {
        return !username.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && 
               !email.isEmpty() && !shippingAddress.isEmpty();
    }
    
    public List<String> getIncompleteFields() {
        List<String> missing = new ArrayList<>();
        if (username.isEmpty()) missing.add("Username");
        if (firstName.isEmpty()) missing.add("First Name");
        if (lastName.isEmpty()) missing.add("Last Name");
        if (email.isEmpty()) missing.add("Email");
        if (phoneNumber.isEmpty()) missing.add("Phone Number");
        if (shippingAddress.isEmpty()) missing.add("Shipping Address");
        return missing;
    }
    
    // Utility methods
    public String getFullName() {
        if (firstName.isEmpty() && lastName.isEmpty()) {
            return username;
        }
        return (firstName + " " + lastName).trim();
    }
    
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.equals(username) ? username : fullName + " (" + username + ")";
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void reactivate() {
        this.isActive = true;
    }
    
    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { 
        this.customerId = customerId != null ? customerId : ""; 
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username != null ? username : ""; 
    }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { 
        this.password = password != null ? password : ""; 
    }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName != null ? firstName : ""; 
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName != null ? lastName : ""; 
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email != null ? email : ""; 
    }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber != null ? phoneNumber : ""; 
    }
    
    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { 
        this.shippingAddress = shippingAddress != null ? shippingAddress : new Address(); 
    }
    
    public Address getBillingAddress() { return billingAddress; }
    public void setBillingAddress(Address billingAddress) { 
        this.billingAddress = billingAddress != null ? billingAddress : new Address(); 
    }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { 
        this.registrationDate = registrationDate != null ? registrationDate : LocalDateTime.now(); 
    }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { 
        this.customerType = customerType != null ? customerType : CustomerType.REGULAR; 
    }
    
    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { 
        this.totalSpent = Math.max(0, totalSpent); 
        updateCustomerType();
    }
    
    // toString, equals, and hashCode
    @Override
    public String toString() {
        return String.format("Customer{id='%s', username='%s', name='%s', type=%s, totalSpent=$%.2f, orders=%d, active=%s}", 
                           customerId, username, getFullName(), customerType, totalSpent, orderHistory.size(), isActive);
    }
    
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer Profile:\n");
        sb.append("=" .repeat(40)).append("\n");
        sb.append(String.format("Customer ID: %s\n", customerId));
        sb.append(String.format("Username: %s\n", username));
        sb.append(String.format("Full Name: %s\n", getFullName()));
        sb.append(String.format("Email: %s\n", email.isEmpty() ? "Not provided" : email));
        sb.append(String.format("Phone: %s\n", phoneNumber.isEmpty() ? "Not provided" : phoneNumber));
        sb.append(String.format("Customer Type: %s (%.0f%% discount)\n", customerType, getDiscountRate() * 100));
        sb.append(String.format("Registration Date: %s\n", registrationDate.toLocalDate()));
        sb.append(String.format("Total Spent: $%.2f\n", totalSpent));
        sb.append(String.format("Total Orders: %d\n", orderHistory.size()));
        sb.append(String.format("Account Status: %s\n", isActive ? "Active" : "Inactive"));
        sb.append(String.format("Profile Complete: %s\n", isProfileComplete() ? "Yes" : "No"));
        
        if (!isProfileComplete()) {
            sb.append("Missing Fields: ").append(String.join(", ", getIncompleteFields())).append("\n");
        }
        
        sb.append("\nShipping Address:\n");
        sb.append(shippingAddress.toString()).append("\n");
        
        if (!billingAddress.equals(shippingAddress)) {
            sb.append("\nBilling Address:\n");
            sb.append(billingAddress.toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return Objects.equals(customerId, customer.customerId) &&
               Objects.equals(username, customer.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, username);
    }
}
