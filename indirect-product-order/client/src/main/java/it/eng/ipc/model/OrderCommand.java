package it.eng.ipc.model;

import java.util.Objects;

public class OrderCommand {
    private String firstName;
    private String lastName;
    private String title;
    private String company;
    private String email;
    private String phone;
    private String internalOrder;
    private String internalCustomer;
    private String order;
    private String customer;
    private Integer color;
    private Integer simulate;
    private Integer giveaway;
    private Integer priority;
    private String type;
    private String operation;


    public OrderCommand(String operation, String firstName, String lastName, String title, String company, String email, String phone, String internalOrder, String internalCustomer, String order, String customer, Integer color, Integer simulate, Integer giveaway, Integer priority, String type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.company = company;
        this.email = email;
        this.phone = phone;
        this.internalOrder = internalOrder;
        this.internalCustomer = internalCustomer;
        this.order = order;
        this.customer = customer;
        this.color = color;
        this.simulate = simulate;
        this.giveaway = giveaway;
        this.priority = priority;
        this.type = type;
        this.operation = operation;
    }

    public OrderCommand() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String value) {
        this.firstName = value;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String value) {
        this.lastName = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String value) {
        this.company = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    public String getInternalOrder() {
        return internalOrder;
    }

    public void setInternalOrder(String value) {
        this.internalOrder = value;
    }

    public String getInternalCustomer() {
        return internalCustomer;
    }

    public void setInternalCustomer(String value) {
        this.internalCustomer = value;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String value) {
        this.order = value;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String value) {
        this.customer = value;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Integer getSimulate() {
        return simulate;
    }

    public void setSimulate(Integer simulate) {
        this.simulate = simulate;
    }

    public Integer getGiveaway() {
        return giveaway;
    }

    public void setGiveaway(Integer giveaway) {
        this.giveaway = giveaway;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderCommand that = (OrderCommand) o;
        return Objects.equals(order, that.order) &&
                Objects.equals(customer, that.customer) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, customer, type);
    }
}
