# Billing Software Implementation Roadmap

## 1. Core Requirements (MVP User Flow)

This MVP focuses entirely on the "Core Loop". Additional features such as history, reporting, and dashboard are deferred until this loop functions smoothly end-to-end.

1. **Start Invoice**: User opens the "New bill" screen.
2. **Customer Selection**: User types name/phone.
   *   Shows matches from the database (Autocomplete).
   *   Option to tap "+ New" to enter name and phone, which saves and auto-selects the customer.
3. **Add Products**: User types product name.
   *   Autocomplete identifies the product and auto-fills its price.
   *   User enters Quantity, and the Line Total calculates instantly.
   *   Repeat for multiple items.
4. **Live Totals**: The Subtotal, Tax, and Grand Total update automatically as products and quantities adjust.
5. **Save & Export**: User taps "Save".
   *   Invoice is stored to the database.
   *   Trigger Download PDF or Print immediately.

---

## 2. Technology Stack Strategy

*   **Framework**: Spring Boot (Java).
*   **Template Engine**: Thymeleaf for Server-Side Rendering (SSR).
*   **Client-Side Interactivity**: Vanilla JavaScript (Fetch API) combined with Thymeleaf fragments. This handles the dynamic search, autocomplete, line-item insertion, and live-calculating totals without requiring full page reloads.
*   **Styling**: Vanilla CSS (or Bootstrap/Tailwind if preferred). We will focus on a clean, modern aesthetic with smooth interactions.
*   **Database Engine**: H2 Database for local MVP execution (embedded, zero-setup), easily switchable to MySQL/PostgreSQL for production.
*   **Data Access**: Spring Data JPA.
*   **PDF Generation**: OpenPDF or Flying Saucer for server-side generation, OR simply client-side `window.print()` / `html2pdf.js` for the fastest MVP approach.

---

## 3. Core Database Entities (JPA)

1.  **Customers**
    *   `id` (Primary Key, Long)
    *   `name` (String)
    *   `phone` (String, indexed for fast lookup)
2.  **Products**
    *   `id` (Primary Key, Long)
    *   `name` (String)
    *   `price` (BigDecimal)
3.  **Invoices**
    *   `id` (Primary Key, Long)
    *   `customer_id` (ManyToOne -> Customers)
    *   `subtotal` (BigDecimal)
    *   `tax` (BigDecimal)
    *   `grand_total` (BigDecimal)
    *   `date_created` (LocalDateTime)
4.  **InvoiceItems**
    *   `id` (Primary Key, Long)
    *   `invoice_id` (ManyToOne -> Invoices)
    *   `product_id` (ManyToOne -> Products)
    *   `quantity` (Integer)
    *   `unit_price` (BigDecimal)
    *   `line_total` (BigDecimal)

*(Note: Storing `unit_price` and `line_total` in `InvoiceItems` protects historical data if standard product prices change in the future).*

---

## 4. Step-by-Step Implementation Plan

### Phase 1: Spring Boot Initialization and Data Tier
*   [ ] Initialize the Spring Boot project via Spring Initializr (Web, JPA, Thymeleaf, H2).
*   [ ] Configure `application.properties` for the H2 database and JPA settings.
*   [ ] Create JPA Entities (`Customer`, `Product`, `Invoice`, `InvoiceItem`) based on the schema above.
*   [ ] Create Spring Data JPA Repositories for the entities.
*   [ ] Insert mock data (using `data.sql` or a `CommandLineRunner`) to aid in frontend development.

### Phase 2: Web Layer & Base Layout
*   [ ] Set up global CSS tokens (colors, typography, spacing) for a premium look in `src/main/resources/static/css/`.
*   [ ] Build a base `layout.html` in Thymeleaf.
*   [ ] Create the `InvoiceController` referencing the layout to serve the `/new-bill` endpoint.

### Phase 3: The "New Bill" Interaction (JavaScript + Thymeleaf + API Endpoints)
*   [ ] Expose Search endpoints (`@RestController` returning JSON or Thymeleaf fragments):
    *   `/api/customers/search?q=`
    *   `/api/products/search?q=`
*   [ ] **Customer Component**: Handle keyboard input, fetch matches, render dropdown via JS. Build modal/form to POST new customer without leaving page.
*   [ ] **Product Lines Component**: "Add line" injects a new row. Typing fetches products list. Choosing a product assigns the `price`.
*   [ ] **Live Totals Area**: JavaScript function listening for quantity/price changes executing `subtotal`, `tax`, and `grandTotal` math on the frontend instantly.

### Phase 4: Persistence and PDF Export
*   [ ] Implement Vanilla Javascript POST request converting the generated invoice tree into JSON.
*   [ ] Create `@PostMapping("/invoices")` backend endpoint to validate, persist the `Invoice` and its `InvoiceItem`s to the DB.
*   [ ] Route the success-response to a `/invoices/{id}/pdf` endpoint generating the PDF or simple invoice-view page configured for browser `window.print()`.

### Phase 5: Future Roadmap (Post-MVP)
*   [ ] Invoice History List / Dashboard.
*   [ ] Product & Customer Management views (CRUD without being on an invoice).
*   [ ] Daily/Monthly Sales reporting.
