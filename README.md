# Basic Supply Chain Management (SCM) System

This project is a basic Supply Chain Management (SCM) system built using Java 21, Gradle, and MapStruct. It aims to provide core functionalities for managing products, inventory, customer orders, procurement, and shipments. This project is intended for learning purposes and to showcase development skills.

## Technologies Used

* **Java:** 21
* **Build Tool:** Gradle
* **Dependency Injection:** Spring Boot
* **Persistence:** Spring Data JPA (with your chosen JPA provider, e.g., Hibernate)
* **Object Mapping:** MapStruct
* **Logging:** SLF4j with Logback
* **Testing:** (Add your testing framework if you have one, e.g., JUnit, Mockito)

## Modules

The system is currently structured into the following modules:

* **Product:** Manages product information (creation, retrieval, update, deletion, categorization, search). Includes pagination and sorting for product summaries.
* **Inventory:** Manages inventory levels, including creating, updating, deleting inventory records, checking availability, and handling reservations.
* **Order:** Manages customer orders, including creation, updating, deletion, adding/updating/removing order items, checking inventory, reserving inventory, confirming, and cancelling orders.
* **Procurement:** Manages the purchasing process, including creating and updating purchase orders and purchase order items, linked to suppliers and products.
* **Shipment:** Manages the shipment of customer orders, including creating, updating, deleting, and retrieving shipment records linked to customer orders.
* **Supplier:** Manages information about suppliers (creation, retrieval, update, deletion, listing).
* **Util:** Contains utility classes, including `BaseEntity` and `InventoryUtil`, and custom exception classes.

## Setup and Running

1.  **Prerequisites:**
    * **Java Development Kit (JDK):** Ensure you have Java 21 installed on your system. You can download it from [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/) or an open-source distribution like [OpenJDK](https://openjdk.java.net/).
    * **Gradle:** Ensure you have Gradle installed. You can download it from [Gradle Releases](https://gradle.org/releases/) or use a package manager. Spring Boot projects often include a Gradle wrapper (`gradlew` and `gradlew.bat`), which automatically downloads the correct Gradle version for the project.

2.  **Getting the Code:**
    ```bash
    git clone https://github.com/tumelosizani/Supply-Chain-Management.git
    ```

3.  **Building the Project:**
    If you have Gradle installed globally:
    ```bash
    gradle build
    ```
    If you are using the Gradle wrapper (recommended):
    ```bash
    ./gradlew build
    ```

4.  **Running the Application:**
    After a successful build, you can run the Spring Boot application using the Gradle bootRun task:
    If you have Gradle installed globally:
    ```bash
    gradle bootRun
    ```
    If you are using the Gradle wrapper:
    ```bash
    ./gradlew bootRun
    ```

    The application should start, and you can typically access its API endpoints (if you have controllers implemented) at `http://localhost:8080` (or a different port configured in your `application.properties` or `application.yml` file).



## Future Enhancements

This is a basic version of the SCM system. Potential future enhancements include:

* More detailed product attributes (variants, images, units of measure).
* Location tracking for inventory.
* Inventory transaction history.
* Low stock alerts.
* More detailed purchase order workflow (status transitions, receiving).
* Shipping tracking and integration with carriers.
* Payment processing.
* User authentication and authorization.
* More comprehensive error handling and validation.
* Unit and integration tests.

## Show Your Support

If you find this project helpful or interesting, feel free to:

* Star the repository.
* Fork the repository and contribute.
* Provide feedback and suggestions.

Thank you for checking out this basic SCM system!# Supply-Chain-Management
