package dev.dini.scms.seed;

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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Populates the database with 1,000+ realistic supply chain records when empty.
 * Runs automatically outside the {@code test} profile unless disabled via {@code scms.seed.enabled=false}.
 */
@Slf4j
@Component
@Profile("!test")
@ConditionalOnProperty(name = "scms.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final int PRODUCT_COUNT = 200;
    private static final int SUPPLIER_COUNT = 50;
    private static final int CUSTOMER_ORDER_COUNT = 350;
    private static final int PURCHASE_ORDER_COUNT = 150;

    private static final String[] WAREHOUSES = {
            "Johannesburg Hub", "Cape Town DC", "Durban Port",
            "Pretoria North", "Gqeberha Depot", "Bloemfontein Central",
            "Polokwane Terminal", "Nelspruit Depot", "East London Hub"
    };

    private static final String[] CARRIERS = {
            "The Courier Guy", "RAM Hand-to-Hand", "DHL Express",
            "FedEx South Africa", "Dawn Wing", "Aramex"
    };

    private static final CitySpec[] CITIES = {
            new CitySpec("Cape Town", "Western Cape", "8001", "021"),
            new CitySpec("Johannesburg", "Gauteng", "2001", "011"),
            new CitySpec("Sandton", "Gauteng", "2196", "010"),
            new CitySpec("Durban", "KwaZulu-Natal", "4001", "031"),
            new CitySpec("Pretoria", "Gauteng", "0002", "012"),
            new CitySpec("Gqeberha", "Eastern Cape", "6001", "041"),
            new CitySpec("Bloemfontein", "Free State", "9301", "051"),
            new CitySpec("Polokwane", "Limpopo", "0700", "015"),
            new CitySpec("Nelspruit", "Mpumalanga", "1200", "013"),
            new CitySpec("East London", "Eastern Cape", "5201", "043")
    };

    private static final String[] STREET_NAMES = {
            "Long Street", "Rivonia Road", "Marine Parade", "Commissioner Street",
            "Church Square", "Jan Smuts Avenue", "Bree Street", "Florida Road",
            "Hatfield Street", "Walmer Boulevard", "President Brand Street", "Market Street"
    };

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

        log.info("Seeding realistic supply chain dataset (1,000+ records)...");
        Random rng = new Random(42L);
        LocalDateTime now = LocalDateTime.now();

        List<Product> products = productRepository.saveAll(buildProducts(rng));
        List<Supplier> suppliers = supplierRepository.saveAll(buildSuppliers(rng));
        List<CustomerOrder> orders = customerOrderRepository.saveAll(buildCustomerOrders(products, rng, now));

        // Keep inventory.quantityReserved aligned with active (PENDING / PROCESSING) customer orders.
        Map<Long, Integer> reservedByProductId = calculateReservedQuantities(orders);
        List<Inventory> inventories = inventoryRepository.saveAll(buildInventories(products, reservedByProductId, rng));

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.saveAll(buildPurchaseOrders(suppliers, products, rng, now));
        List<Shipment> shipments = shipmentRepository.saveAll(buildShipments(orders, rng));
        List<Payment> payments = paymentRepository.saveAll(buildPayments(orders, rng, now));

        int orderItems = orders.stream().mapToInt(o -> o.getItems().size()).sum();
        int poItems = purchaseOrders.stream().mapToInt(po -> po.getItems().size()).sum();
        int total = products.size() + inventories.size() + suppliers.size()
                + orders.size() + orderItems + purchaseOrders.size() + poItems
                + shipments.size() + payments.size();

        log.info("Seeded {} total records ({} products, {} inventories, {} suppliers, {} customer orders [{} items], {} purchase orders [{} items], {} shipments, {} payments).",
                total, products.size(), inventories.size(), suppliers.size(),
                orders.size(), orderItems, purchaseOrders.size(), poItems,
                shipments.size(), payments.size());
    }

    private List<Product> buildProducts(Random rng) {
        String[] brands = {"ProLine", "Apex", "Nova", "ErgoTech", "Vanguard", "Summit", "Core", "Pulse"};
        String[] variants = {"Compact", "Standard", "Pro", "Ultra", "Lite", "Studio", "Industrial", "Eco"};
        ProductTemplate[] templates = {
                new ProductTemplate("Wireless Mouse", "2.4GHz ergonomic mouse with adjustable DPI", "Electronics", 18, 65),
                new ProductTemplate("Bluetooth Speaker", "Portable water-resistant speaker with USB-C charging", "Electronics", 35, 140),
                new ProductTemplate("Mechanical Keyboard", "Tactile mechanical keyboard with aluminum top plate", "Electronics", 55, 180),
                new ProductTemplate("Wireless Earbuds", "Active noise-cancelling earbuds with charging case", "Electronics", 40, 150),
                new ProductTemplate("USB-C Docking Station", "Multi-port hub with HDMI, Ethernet, and PD passthrough", "Electronics", 45, 130),
                new ProductTemplate("Hardcover Notebook", "200-page archival paper notebook with lay-flat binding", "Stationery", 5, 18),
                new ProductTemplate("Gel Pen Pack", "10-pack smooth-flow 0.7mm archival gel pens", "Stationery", 4, 15),
                new ProductTemplate("Sticky Note Cube", "400-sheet recycled adhesive memo block", "Stationery", 3, 12),
                new ProductTemplate("LED Desk Lamp", "Dimmable task lamp with adjustable color temperature", "Home & Office", 20, 75),
                new ProductTemplate("Modular Desk Organizer", "Bamboo and powder-coated steel desk tray", "Home & Office", 14, 45),
                new ProductTemplate("Monitor Riser Stand", "Ergonomic aluminum stand with cable channel", "Home & Office", 25, 80),
                new ProductTemplate("Yoga Mat", "6mm high-density non-slip TPE exercise mat", "Fitness", 22, 60),
                new ProductTemplate("Insulated Water Bottle", "Double-wall vacuum stainless steel 1L bottle", "Fitness", 15, 40),
                new ProductTemplate("Resistance Band Set", "5-level fabric resistance bands with carry pouch", "Fitness", 18, 48),
                new ProductTemplate("Programmable Coffee Maker", "12-cup drip brewer with thermal carafe", "Home Appliances", 65, 160),
                new ProductTemplate("Electric Kettle", "1.7L rapid-boil stainless steel kettle", "Home Appliances", 28, 70),
                new ProductTemplate("Commuter Backpack", "22L water-resistant laptop backpack", "Accessories", 35, 95),
                new ProductTemplate("Portable Power Bank", "10,000mAh dual-port USB-C PD battery pack", "Accessories", 22, 65),
                new ProductTemplate("Laptop Sleeve", "Shock-absorbing neoprene 15-inch protective sleeve", "Accessories", 14, 38),
                new ProductTemplate("Barcode Label Roll", "Thermal shipping labels, 500-pack 100x150mm", "Packaging", 9, 28)
        };

        List<Product> products = new ArrayList<>(PRODUCT_COUNT);
        for (int i = 0; i < PRODUCT_COUNT; i++) {
            ProductTemplate tpl = templates[i % templates.length];
            String brand = brands[(i / templates.length) % brands.length];
            String variant = variants[(i * 3 + rng.nextInt(variants.length)) % variants.length];
            int priceInt = tpl.minPrice() + rng.nextInt(tpl.maxPrice() - tpl.minPrice() + 1);
            BigDecimal price = BigDecimal.valueOf(priceInt).add(new BigDecimal("0.99"));

            Product p = new Product();
            p.setName(brand + " " + tpl.baseName() + " " + variant + " #" + (i + 1));
            p.setDescription(tpl.description() + " (" + brand + " " + variant + " series).");
            p.setUnitPrice(price);
            p.setCategory(tpl.category());
            products.add(p);
        }
        return products;
    }

    private List<Inventory> buildInventories(List<Product> products, Map<Long, Integer> reservedByProductId, Random rng) {
        List<Inventory> inventories = new ArrayList<>(products.size());
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int reserved = reservedByProductId.getOrDefault(p.getId(), 0);
            int physical = Math.max(40 + rng.nextInt(360), reserved + 25);
            inventories.add(new Inventory(p, physical, reserved, WAREHOUSES[i % WAREHOUSES.length]));
        }
        return inventories;
    }

    private List<Supplier> buildSuppliers(Random rng) {
        String[] prefixes = {"Metro", "Coastal", "Highveld", "Southern", "Atlas", "Vertex", "Horizon", "Premier", "Pinnacle", "United"};
        String[] suffixes = {"Supplies", "Distributors", "Traders", "Logistics", "Wholesale", "Partners", "Industries", "Enterprises"};

        List<Supplier> suppliers = new ArrayList<>(SUPPLIER_COUNT);
        for (int i = 0; i < SUPPLIER_COUNT; i++) {
            CitySpec city = CITIES[i % CITIES.length];
            String prefix = prefixes[i % prefixes.length];
            String suffix = suffixes[(i / prefixes.length) % suffixes.length];
            String name = city.city() + " " + prefix + " " + suffix;
            String slug = (prefix + "-" + suffix + "-" + (i + 1)).toLowerCase();

            Supplier s = new Supplier();
            s.setName(name);
            s.setPhoneNumber(city.areaCode() + String.format("%07d", 1000000 + rng.nextInt(8999999)));
            s.setEmail("orders@" + slug + ".co.za");
            s.setAddress(randomAddress(city, rng));
            s.setWebsite("https://www." + slug + ".co.za");
            s.setRating(BigDecimal.valueOf(3.5 + rng.nextDouble() * 1.5).setScale(1, RoundingMode.HALF_UP).doubleValue());
            suppliers.add(s);
        }
        return suppliers;
    }

    private List<CustomerOrder> buildCustomerOrders(List<Product> products, Random rng, LocalDateTime now) {
        CustomerOrderStatus[] statuses = {
                CustomerOrderStatus.DELIVERED, CustomerOrderStatus.DELIVERED, CustomerOrderStatus.DELIVERED,
                CustomerOrderStatus.SHIPPED, CustomerOrderStatus.SHIPPED,
                CustomerOrderStatus.CONFIRMED, CustomerOrderStatus.CONFIRMED,
                CustomerOrderStatus.PROCESSING,
                CustomerOrderStatus.PENDING, CustomerOrderStatus.PENDING,
                CustomerOrderStatus.CANCELLED
        };

        List<CustomerOrder> orders = new ArrayList<>(CUSTOMER_ORDER_COUNT);
        for (int i = 0; i < CUSTOMER_ORDER_COUNT; i++) {
            CustomerOrderStatus status = statuses[i % statuses.length];
            LocalDateTime orderDate = now.minusDays(1 + rng.nextInt(90)).minusHours(rng.nextInt(24));
            CitySpec city = CITIES[rng.nextInt(CITIES.length)];

            CustomerOrder order = new CustomerOrder();
            order.setOrderDate(orderDate);
            order.setStatus(status);
            order.setShippingAddress(randomAddress(city, rng));

            int itemCount = 1 + rng.nextInt(4);
            Set<Integer> usedProductIndices = new HashSet<>();
            List<CustomerOrderItem> items = new ArrayList<>(itemCount);
            BigDecimal total = BigDecimal.ZERO;

            for (int j = 0; j < itemCount; j++) {
                int prodIdx = rng.nextInt(products.size());
                if (!usedProductIndices.add(prodIdx)) {
                    continue;
                }
                Product product = products.get(prodIdx);
                int qty = 1 + rng.nextInt(8);

                CustomerOrderItem item = new CustomerOrderItem();
                item.setCustomerOrder(order);
                item.setProduct(product);
                item.setQuantity(qty);
                item.setPrice(product.getUnitPrice());
                items.add(item);

                total = total.add(product.getUnitPrice().multiply(BigDecimal.valueOf(qty)));
            }

            order.setItems(items);
            order.setTotalCost(total);
            orders.add(order);
        }
        return orders;
    }

    private List<PurchaseOrder> buildPurchaseOrders(List<Supplier> suppliers, List<Product> products, Random rng, LocalDateTime now) {
        PurchaseOrderStatus[] statuses = {
                PurchaseOrderStatus.FULLY_RECEIVED, PurchaseOrderStatus.FULLY_RECEIVED,
                PurchaseOrderStatus.PARTIALLY_RECEIVED,
                PurchaseOrderStatus.SHIPPED,
                PurchaseOrderStatus.APPROVED,
                PurchaseOrderStatus.PENDING,
                PurchaseOrderStatus.CANCELLED
        };

        List<PurchaseOrder> purchaseOrders = new ArrayList<>(PURCHASE_ORDER_COUNT);
        for (int i = 0; i < PURCHASE_ORDER_COUNT; i++) {
            PurchaseOrderStatus status = statuses[i % statuses.length];
            LocalDateTime orderDate = now.minusDays(1 + rng.nextInt(60));
            LocalDateTime expectedDelivery = status == PurchaseOrderStatus.FULLY_RECEIVED
                    ? orderDate.plusDays(5 + rng.nextInt(5))
                    : now.plusDays(1 + rng.nextInt(14));

            PurchaseOrder po = new PurchaseOrder();
            po.setSupplier(suppliers.get(rng.nextInt(suppliers.size())));
            po.setOrderDate(orderDate);
            po.setExpectedDeliveryDate(expectedDelivery);
            po.setStatus(status);

            int itemCount = 1 + rng.nextInt(4);
            Set<Integer> usedProductIndices = new HashSet<>();
            List<PurchaseOrderItem> items = new ArrayList<>(itemCount);

            for (int j = 0; j < itemCount; j++) {
                int prodIdx = rng.nextInt(products.size());
                if (!usedProductIndices.add(prodIdx)) {
                    continue;
                }
                Product product = products.get(prodIdx);
                int qty = 20 + rng.nextInt(130);
                int received = switch (status) {
                    case FULLY_RECEIVED -> qty;
                    case PARTIALLY_RECEIVED -> Math.max(1, qty / 2);
                    default -> 0;
                };

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(po);
                item.setProduct(product);
                item.setQuantity(qty);
                item.setQuantityReceived(received);
                item.setUnitPrice(product.getUnitPrice().multiply(new BigDecimal("0.65")).setScale(2, RoundingMode.HALF_UP));
                items.add(item);
            }

            po.setItems(items);
            purchaseOrders.add(po);
        }
        return purchaseOrders;
    }

    private List<Shipment> buildShipments(List<CustomerOrder> orders, Random rng) {
        List<Shipment> shipments = new ArrayList<>();
        int seq = 100001;
        for (CustomerOrder order : orders) {
            ShipmentStatus shipmentStatus = switch (order.getStatus()) {
                case DELIVERED -> ShipmentStatus.DELIVERED;
                case SHIPPED -> ShipmentStatus.SHIPPED;
                case CONFIRMED -> ShipmentStatus.PENDING;
                default -> null;
            };
            if (shipmentStatus == null) {
                continue;
            }
            LocalDateTime shippedAt = (shipmentStatus == ShipmentStatus.SHIPPED || shipmentStatus == ShipmentStatus.DELIVERED)
                    ? order.getOrderDate().plusDays(1)
                    : null;
            LocalDateTime deliveredAt = shipmentStatus == ShipmentStatus.DELIVERED
                    ? order.getOrderDate().plusDays(3)
                    : null;
            shipments.add(new Shipment(
                    order,
                    "TRK-ZA-" + (seq++),
                    CARRIERS[rng.nextInt(CARRIERS.length)],
                    shipmentStatus,
                    shippedAt,
                    deliveredAt
            ));
        }
        return shipments;
    }

    private List<Payment> buildPayments(List<CustomerOrder> orders, Random rng, LocalDateTime now) {
        PaymentMethod[] methods = PaymentMethod.values();
        List<Payment> payments = new ArrayList<>();
        int seq = 1;
        for (CustomerOrder order : orders) {
            // PENDING / NEW orders have not been paid yet; leaving them unpaid allows POST /api/payments
            // and DELETE /api/orders/{id} to work without unique/FK constraint violations.
            PaymentStatus paymentStatus = switch (order.getStatus()) {
                case DELIVERED, SHIPPED, CONFIRMED, PROCESSING -> PaymentStatus.COMPLETED;
                case CANCELLED -> rng.nextBoolean() ? (rng.nextBoolean() ? PaymentStatus.REFUNDED : PaymentStatus.FAILED) : null;
                case PENDING, NEW -> null;
            };
            if (paymentStatus == null) {
                continue;
            }

            Payment p = new Payment();
            p.setCustomerOrder(order);
            p.setAmount(order.getTotalCost());
            p.setStatus(paymentStatus);
            p.setPaymentMethod(methods[rng.nextInt(methods.length)]);
            p.setPaymentDate(order.getOrderDate().plusMinutes(5 + rng.nextInt(120)));
            p.setTransactionReference(String.format("PAY-%d-%05d", now.getYear(), seq++));
            payments.add(p);
        }
        return payments;
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

    private Address randomAddress(CitySpec city, Random rng) {
        Address a = new Address();
        a.setStreet((1 + rng.nextInt(450)) + " " + STREET_NAMES[rng.nextInt(STREET_NAMES.length)]);
        a.setCity(city.city());
        a.setState(city.province());
        a.setCountry("South Africa");
        a.setZipCode(city.zipCode());
        return a;
    }

    private record ProductTemplate(String baseName, String description, String category, int minPrice, int maxPrice) {}

    private record CitySpec(String city, String province, String zipCode, String areaCode) {}
}
