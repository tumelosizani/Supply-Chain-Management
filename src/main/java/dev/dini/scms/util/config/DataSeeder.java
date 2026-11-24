package dev.dini.scms.util.config;

import dev.dini.scms.inventory.entity.Inventory;
import dev.dini.scms.inventory.repository.InventoryRepository;
import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.order.entity.CustomerOrderItem;
import dev.dini.scms.order.entity.CustomerOrderStatus;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import dev.dini.scms.payment.entity.Payment;
import dev.dini.scms.payment.enums.PaymentMethod;
import dev.dini.scms.payment.enums.PaymentStatus;
import dev.dini.scms.payment.repository.PaymentRepository;
import dev.dini.scms.procurement.entity.PurchaseOrder;
import dev.dini.scms.procurement.entity.PurchaseOrderItem;
import dev.dini.scms.procurement.entity.PurchaseOrderStatus;
import dev.dini.scms.procurement.repository.PurchaseOrderRepository;
import dev.dini.scms.product.entity.Product;
import dev.dini.scms.product.repository.ProductRepository;
import dev.dini.scms.shipment.entity.Shipment;
import dev.dini.scms.shipment.entity.ShipmentStatus;
import dev.dini.scms.shipment.repository.ShipmentRepository;
import dev.dini.scms.supplier.entity.Supplier;
import dev.dini.scms.supplier.repository.SupplierRepository;
import dev.dini.scms.util.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Populates the database with realistic initial supply chain data when empty.
 * Runs automatically outside the {@code test} profile unless disabled via {@code scms.seed.enabled=false}.
 */
@Slf4j
@Component
@Profile("!test")
@ConditionalOnProperty(name = "scms.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0 || supplierRepository.count() > 0) {
            log.info("Database already contains data — skipping DataSeeder.");
            return;
        }

        log.info("Seeding realistic supply chain data...");
        LocalDateTime now = LocalDateTime.now();

        List<Product> products = productRepository.saveAll(buildProducts());
        List<Supplier> suppliers = supplierRepository.saveAll(buildSuppliers());
        List<CustomerOrder> orders = customerOrderRepository.saveAll(buildCustomerOrders(products, now));

        // Keep inventory.quantityReserved aligned with active (PENDING / PROCESSING) customer orders.
        Map<Long, Integer> reservedByProductId = calculateReservedQuantities(orders);
        inventoryRepository.saveAll(buildInventories(products, reservedByProductId));

        purchaseOrderRepository.saveAll(buildPurchaseOrders(suppliers, products, now));
        shipmentRepository.saveAll(buildShipments(orders));
        paymentRepository.saveAll(buildPayments(orders, now));

        log.info("Seeded {} products, {} suppliers, {} customer orders, and {} purchase orders.",
                products.size(), suppliers.size(), orders.size(), 6);
    }

    private List<Product> buildProducts() {
        return List.of(
                product("Ergonomic Wireless Mouse", "2.4GHz wireless mouse with adjustable 800-3200 DPI sensor.", "25.99", "Electronics"),
                product("Portable Bluetooth Speaker", "Water-resistant speaker with 12-hour battery life and USB-C charging.", "49.99", "Electronics"),
                product("Mechanical Gaming Keyboard", "Compact TKL mechanical keyboard with tactile switches and RGB.", "79.99", "Electronics"),
                product("Noise-Cancelling Earbuds", "True wireless earbuds with active noise cancellation and charging case.", "59.99", "Electronics"),
                product("Fast Wireless Charger", "15W Qi-certified wireless charging pad with braided USB-C cable.", "29.99", "Electronics"),
                product("A5 Hardcover Notebook", "200-page ruled hardcover notebook with elastic closure.", "6.49", "Stationery"),
                product("Gel Ballpoint Pens (10-Pack)", "Smooth-writing 0.7mm black and blue gel pens.", "5.99", "Stationery"),
                product("LED Desk Lamp", "Dimmable LED task lamp with adjustable color temperature.", "24.99", "Home & Office"),
                product("Modular Desk Organizer", "Bamboo and steel multi-compartment desk tray.", "18.99", "Home & Office"),
                product("Non-Slip Yoga Mat", "6mm high-density TPE exercise mat with carry strap.", "29.99", "Fitness"),
                product("Insulated Water Bottle 1L", "Double-wall vacuum-insulated stainless steel bottle.", "19.49", "Fitness"),
                product("Programmable Coffee Maker", "12-cup drip coffee brewer with thermal carafe and timer.", "89.99", "Home Appliances"),
                product("1.7L Electric Kettle", "Stainless steel rapid-boil kettle with auto shut-off.", "34.99", "Home Appliances"),
                product("Water-Resistant Commuter Backpack", "22L laptop backpack with padded 16-inch sleeve.", "45.99", "Accessories"),
                product("10,000mAh Power Bank", "Slim dual-port USB-C PD portable charger.", "27.99", "Accessories")
        );
    }

    private List<Inventory> buildInventories(List<Product> products, Map<Long, Integer> reservedByProductId) {
        String[] warehouses = {
                "Johannesburg Hub", "Cape Town DC", "Durban Port",
                "Pretoria North", "Gqeberha Depot", "Bloemfontein Central"
        };
        int[] baseStock = {150, 200, 75, 90, 130, 320, 450, 180, 95, 120, 180, 60, 85, 240, 310};

        List<Inventory> inventories = new ArrayList<>(products.size());
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int reserved = reservedByProductId.getOrDefault(p.getId(), 0);
            int physical = Math.max(baseStock[i], reserved + 10);
            inventories.add(new Inventory(p, physical, reserved, warehouses[i % warehouses.length]));
        }
        return inventories;
    }

    private List<Supplier> buildSuppliers() {
        return List.of(
                supplier("Cape Town Supplies", "0211234567", "orders@capetownsupplies.co.za",
                        address("123 Long Street", "Cape Town", "Western Cape", "South Africa", "8001"),
                        "https://www.capetownsupplies.co.za", 4.6),
                supplier("Durban Coastal Distributors", "0317654321", "procurement@durbancoastal.co.za",
                        address("456 Marine Parade", "Durban", "KwaZulu-Natal", "South Africa", "4001"),
                        "https://www.durbancoastal.co.za", 4.3),
                supplier("Joburg Central Traders", "0119876543", "sales@joburgtraders.co.za",
                        address("789 Commissioner Street", "Johannesburg", "Gauteng", "South Africa", "2001"),
                        "https://www.joburgtraders.co.za", 4.8),
                supplier("Pretoria Tech Partners", "0123456789", "support@pretoriatech.co.za",
                        address("321 Church Square", "Pretoria", "Gauteng", "South Africa", "0002"),
                        "https://www.pretoriatech.co.za", 4.5),
                supplier("Bloemfontein Wholesale Goods", "0511239876", "info@bloemwholesale.co.za",
                        address("654 President Brand Street", "Bloemfontein", "Free State", "South Africa", "9301"),
                        "https://www.bloemwholesale.co.za", 4.0),
                supplier("Eastern Cape Logistics & Supply", "0417654321", "dispatch@eclogistics.co.za",
                        address("987 Beach Road", "Gqeberha", "Eastern Cape", "South Africa", "6001"),
                        "https://www.eclogistics.co.za", 4.4)
        );
    }

    private List<CustomerOrder> buildCustomerOrders(List<Product> products, LocalDateTime now) {
        return List.of(
                customerOrder(now.minusDays(14), CustomerOrderStatus.DELIVERED,
                        address("42 Bree Street", "Cape Town", "Western Cape", "South Africa", "8001"),
                        List.of(itemSpec(products.get(0), 4), itemSpec(products.get(2), 2))),
                customerOrder(now.minusDays(9), CustomerOrderStatus.SHIPPED,
                        address("18 Rivonia Road", "Sandton", "Gauteng", "South Africa", "2196"),
                        List.of(itemSpec(products.get(1), 3), itemSpec(products.get(14), 5))),
                customerOrder(now.minusDays(5), CustomerOrderStatus.CONFIRMED,
                        address("77 Florida Road", "Durban", "KwaZulu-Natal", "South Africa", "4001"),
                        List.of(itemSpec(products.get(5), 20), itemSpec(products.get(6), 15), itemSpec(products.get(8), 4))),
                customerOrder(now.minusDays(2), CustomerOrderStatus.PROCESSING,
                        address("105 Hatfield Street", "Pretoria", "Gauteng", "South Africa", "0028"),
                        List.of(itemSpec(products.get(3), 6), itemSpec(products.get(4), 6))),
                customerOrder(now.minusDays(1), CustomerOrderStatus.PENDING,
                        address("14 Nelson Mandela Drive", "Bloemfontein", "Free State", "South Africa", "9301"),
                        List.of(itemSpec(products.get(9), 8), itemSpec(products.get(10), 10))),
                customerOrder(now.minusHours(6), CustomerOrderStatus.PENDING,
                        address("88 Walmer Boulevard", "Gqeberha", "Eastern Cape", "South Africa", "6070"),
                        List.of(itemSpec(products.get(11), 2), itemSpec(products.get(12), 3), itemSpec(products.get(13), 4))),
                customerOrder(now.minusDays(7), CustomerOrderStatus.CANCELLED,
                        address("210 Jan Smuts Avenue", "Johannesburg", "Gauteng", "South Africa", "2193"),
                        List.of(itemSpec(products.get(7), 5)))
        );
    }

    private List<PurchaseOrder> buildPurchaseOrders(List<Supplier> suppliers, List<Product> products, LocalDateTime now) {
        return List.of(
                purchaseOrder(suppliers.get(2), now.minusDays(20), now.minusDays(13), PurchaseOrderStatus.FULLY_RECEIVED,
                        List.of(
                                poItemSpec(products.get(0), 100, 100, "16.50"),
                                poItemSpec(products.get(2), 50, 50, "52.00")
                        )),
                purchaseOrder(suppliers.get(0), now.minusDays(12), now.minusDays(5), PurchaseOrderStatus.PARTIALLY_RECEIVED,
                        List.of(
                                poItemSpec(products.get(1), 80, 50, "31.00"),
                                poItemSpec(products.get(3), 60, 60, "38.50")
                        )),
                purchaseOrder(suppliers.get(1), now.minusDays(6), now.plusDays(2), PurchaseOrderStatus.SHIPPED,
                        List.of(
                                poItemSpec(products.get(11), 30, 0, "58.00"),
                                poItemSpec(products.get(12), 40, 0, "21.00")
                        )),
                purchaseOrder(suppliers.get(3), now.minusDays(3), now.plusDays(5), PurchaseOrderStatus.APPROVED,
                        List.of(
                                poItemSpec(products.get(4), 120, 0, "18.00"),
                                poItemSpec(products.get(14), 150, 0, "16.75")
                        )),
                purchaseOrder(suppliers.get(4), now.minusDays(1), now.plusDays(7), PurchaseOrderStatus.PENDING,
                        List.of(
                                poItemSpec(products.get(5), 250, 0, "3.20"),
                                poItemSpec(products.get(6), 300, 0, "2.90")
                        )),
                purchaseOrder(suppliers.get(5), now.minusDays(15), now.minusDays(8), PurchaseOrderStatus.CANCELLED,
                        List.of(
                                poItemSpec(products.get(9), 40, 0, "17.50")
                        ))
        );
    }

    private List<Shipment> buildShipments(List<CustomerOrder> orders) {
        return List.of(
                new Shipment(orders.get(0), "TRK-ZA-884201", "The Courier Guy", ShipmentStatus.DELIVERED),
                new Shipment(orders.get(1), "TRK-ZA-884355", "RAM Hand-to-Hand", ShipmentStatus.SHIPPED),
                new Shipment(orders.get(2), "TRK-ZA-884419", "DHL Express", ShipmentStatus.PENDING)
        );
    }

    private List<Payment> buildPayments(List<CustomerOrder> orders, LocalDateTime now) {
        return List.of(
                payment(orders.get(0), PaymentStatus.COMPLETED, PaymentMethod.CREDIT_CARD, now.minusDays(14), "PAY-2026-0001"),
                payment(orders.get(1), PaymentStatus.COMPLETED, PaymentMethod.BANK_TRANSFER, now.minusDays(9), "PAY-2026-0002"),
                payment(orders.get(2), PaymentStatus.COMPLETED, PaymentMethod.DEBIT_CARD, now.minusDays(5), "PAY-2026-0003"),
                payment(orders.get(3), PaymentStatus.PENDING, PaymentMethod.CREDIT_CARD, now.minusDays(2), "PAY-2026-0004"),
                payment(orders.get(4), PaymentStatus.PENDING, PaymentMethod.BANK_TRANSFER, now.minusDays(1), "PAY-2026-0005"),
                payment(orders.get(6), PaymentStatus.REFUNDED, PaymentMethod.CREDIT_CARD, now.minusDays(6), "PAY-2026-0006")
        );
    }

    private Map<Long, Integer> calculateReservedQuantities(List<CustomerOrder> orders) {
        Map<Long, Integer> reserved = new HashMap<>();
        for (CustomerOrder order : orders) {
            if (order.getStatus() == CustomerOrderStatus.PENDING
                    || order.getStatus() == CustomerOrderStatus.PROCESSING) {
                for (CustomerOrderItem item : order.getItems()) {
                    reserved.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
                }
            }
        }
        return reserved;
    }

    private Product product(String name, String description, String unitPrice, String category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setUnitPrice(new BigDecimal(unitPrice));
        p.setCategory(category);
        return p;
    }

    private Supplier supplier(String name, String phone, String email, Address address, String website, double rating) {
        Supplier s = new Supplier();
        s.setName(name);
        s.setPhoneNumber(phone);
        s.setEmail(email);
        s.setAddress(address);
        s.setWebsite(website);
        s.setRating(rating);
        return s;
    }

    private Address address(String street, String city, String state, String country, String zipCode) {
        Address a = new Address();
        a.setStreet(street);
        a.setCity(city);
        a.setState(state);
        a.setCountry(country);
        a.setZipCode(zipCode);
        return a;
    }

    private CustomerOrder customerOrder(LocalDateTime orderDate, CustomerOrderStatus status, Address address, List<OrderItemSpec> specs) {
        CustomerOrder order = new CustomerOrder();
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setShippingAddress(address);

        BigDecimal total = BigDecimal.ZERO;
        List<CustomerOrderItem> items = new ArrayList<>(specs.size());
        for (OrderItemSpec spec : specs) {
            CustomerOrderItem item = new CustomerOrderItem();
            item.setCustomerOrder(order);
            item.setProduct(spec.product());
            item.setQuantity(spec.quantity());
            item.setPrice(spec.product().getUnitPrice());
            items.add(item);
            total = total.add(spec.product().getUnitPrice().multiply(BigDecimal.valueOf(spec.quantity())));
        }
        order.setItems(items);
        order.setTotalCost(total);
        return order;
    }

    private PurchaseOrder purchaseOrder(Supplier supplier, LocalDateTime orderDate, LocalDateTime expectedDelivery,
                                        PurchaseOrderStatus status, List<PoItemSpec> specs) {
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(orderDate);
        po.setExpectedDeliveryDate(expectedDelivery);
        po.setStatus(status);

        List<PurchaseOrderItem> items = new ArrayList<>(specs.size());
        for (PoItemSpec spec : specs) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(spec.product());
            item.setQuantity(spec.quantity());
            item.setQuantityReceived(spec.quantityReceived());
            item.setUnitPrice(spec.unitPrice());
            items.add(item);
        }
        po.setItems(items);
        return po;
    }

    private Payment payment(CustomerOrder order, PaymentStatus status, PaymentMethod method,
                            LocalDateTime paymentDate, String reference) {
        Payment p = new Payment();
        p.setCustomerOrder(order);
        p.setAmount(order.getTotalCost());
        p.setStatus(status);
        p.setPaymentMethod(method);
        p.setPaymentDate(paymentDate);
        p.setTransactionReference(reference);
        return p;
    }

    private OrderItemSpec itemSpec(Product product, int quantity) {
        return new OrderItemSpec(product, quantity);
    }

    private PoItemSpec poItemSpec(Product product, int quantity, int quantityReceived, String unitPrice) {
        return new PoItemSpec(product, quantity, quantityReceived, new BigDecimal(unitPrice));
    }

    private record OrderItemSpec(Product product, int quantity) {}

    private record PoItemSpec(Product product, int quantity, int quantityReceived, BigDecimal unitPrice) {}
}
