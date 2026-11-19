package com.belobina.app.utils;

public class Constants {
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ORDERS = "orders";
    public static final String COLLECTION_ORDER_ITEM = "order_item";
    public static final String COLLECTION_FURNITURE = "product";
    public static final String COLLECTION_ASSEMBLERS = "assembler";

    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_ADMIN = "admin";

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ASSIGNED = "assigned";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_REJECTED = "rejected";

    public static final double ASSEMBLY_FEE_PER_ITEM = 50.0;

    public static final String COLLECTION_CART = "cart";

    public static final String FIELD_USER_ID = "userID";
    public static final String FIELD_ORDER_ID = "orderID";
    public static final String FIELD_ASS_ID = "assID";
    public static final String FIELD_ORDER_ITEM_ID = "orderItemID";
    public static final String FIELD_ITEM_QUANTITY = "itemQuantity";
    public static final String FIELD_UNIT_PRICE = "unitPrice";
    public static final String FIELD_PROD_ID = "prodID";
    public static final String FIELD_CUSTOMER_ID = FIELD_USER_ID;
    public static final String FIELD_FURNITURE_ID = "furnitureId";
    public static final String FIELD_QUANTITY = "quantity";
    public static final String FIELD_ASSEMBLY_REQUESTED = "assemblyRequested";
    public static final String FIELD_ADDED_AT = "addedAt";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_STATUS = "orderStatus";
    public static final String FIELD_ORDER_DATE = "orderDate";
    public static final String FIELD_ORDER_AMOUNT = "orderAmount";
    public static final String FIELD_ORDER_ASSEMBLY_REQ = "orderAssemblyReq";
    public static final String FIELD_ROLE = "userRole";
    public static final String FIELD_ASSEMBLER_NAME = "assemblerName";
    public static final String FIELD_COMPLETION_DATE = "completionDate";
}
